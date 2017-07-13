/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessOutputStream;
import org.apache.pdfbox.io.ScratchFile;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author Ben Litchfield
 */
public class COSStream extends COSDictionary implements Closeable
{
    private RandomAccess randomAccess;      // backing store, in-memory or on-disk
    private final ScratchFile scratchFile;  // used as a temp buffer during decoding
    private boolean isWriting;              // true if there's an open OutputStream
    
    private static final Log LOG = LogFactory.getLog(COSStream.class);
    
    /**
     * Creates a new stream with an empty dictionary.
     * <p>
     * Try to avoid using this constructor because it creates a new scratch file in memory. Instead,
     * use {@link COSDocument#createCOSStream() document.getDocument().createCOSStream()} which will
     * use the existing scratch file (in memory or in temp file) of the document.
     * </p>
     */
    public COSStream()
    {
        this(ScratchFile.getMainMemoryOnlyInstance());
    }
    
    /**
     * Creates a new stream with an empty dictionary. Data is stored in the given scratch file.
     *
     * @param scratchFile Scratch file for writing stream data.
     */
    public COSStream(ScratchFile scratchFile)
    {
        setInt(COSName.LENGTH, 0);
        this.scratchFile = scratchFile != null ? scratchFile : ScratchFile.getMainMemoryOnlyInstance();
    }
    
    /**
     * Throws if the random access backing store has been closed. Helpful for catching cases where
     * a user tries to use a COSStream which has outlived its COSDocument.
     */
    private void checkClosed() throws IOException
    {
        if ((randomAccess != null) && randomAccess.isClosed())
        {
            throw new IOException("COSStream has been closed and cannot be read. " +
                                  "Perhaps its enclosing PDDocument has been closed?");
        }
    }

    /**
     * This will get the stream with all of the filters applied.
     *
     * @return the bytes of the physical (encoded) stream
     * @throws IOException when encoding causes an exception
     * @deprecated Use {@link #createRawInputStream()} instead.
     */
    @Deprecated
    public InputStream getFilteredStream() throws IOException
    {
        return createRawInputStream();
    }

    /**
     * Ensures {@link #randomAccess} is not <code>null</code> by creating a
     * buffer from {@link #scratchFile} if needed.
     * 
     * @param forInputStream  if <code>true</code> and {@link #randomAccess} is <code>null</code>
     *                        a debug message is logged - input stream should be retrieved after
     *                        data being written to stream
     * @throws IOException
     */
    private void ensureRandomAccessExists(boolean forInputStream) throws IOException
    {
        if (randomAccess == null)
        {
            if (forInputStream && LOG.isDebugEnabled())
            {
                // no data written to stream - maybe this should be an exception
                LOG.debug("Create InputStream called without data being written before to stream.");
            }
            randomAccess = scratchFile.createBuffer();
        }
    }
    
    /**
     * Returns a new InputStream which reads the encoded PDF stream data. Experts only!
     * 
     * @return InputStream containing raw, encoded PDF stream data.
     * @throws IOException If the stream could not be read.
     */
    public InputStream createRawInputStream() throws IOException
    {
        checkClosed();
        if (isWriting)
        {
            throw new IllegalStateException("Cannot read while there is an open stream writer");
        }
        ensureRandomAccessExists(true);
        return new RandomAccessInputStream(randomAccess);
    }

    /**
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     * @throws IOException when decoding causes an exception
     * @deprecated Use {@link #createInputStream()} instead.
     */
    @Deprecated
    public InputStream getUnfilteredStream() throws IOException
    {
        return createInputStream();
    }

    /**
     * Returns a new InputStream which reads the decoded stream data.
     * 
     * @return InputStream containing decoded stream data.
     * @throws IOException If the stream could not be read.
     */
    public COSInputStream createInputStream() throws IOException
    {
        checkClosed();
        if (isWriting)
        {
            throw new IllegalStateException("Cannot read while there is an open stream writer");
        }
        ensureRandomAccessExists(true);
        InputStream input = new RandomAccessInputStream(randomAccess);
        return COSInputStream.create(getFilterList(), this, input, scratchFile);
    }

    /**
     * This will create an output stream that can be written to.
     *
     * @return An output stream which raw data bytes should be written to.
     * @throws IOException If there is an error creating the stream.
     * @deprecated Use {@link #createOutputStream()} instead.
     */
    @Deprecated
    public OutputStream createUnfilteredStream() throws IOException
    {
        return createOutputStream();
    }

    /**
     * Returns a new OutputStream for writing stream data, using the current filters.
     *
     * @return OutputStream for un-encoded stream data.
     * @throws IOException If the output stream could not be created.
     */
    public OutputStream createOutputStream() throws IOException
    {
        return createOutputStream(null);
    }
    
    /**
     * Returns a new OutputStream for writing stream data, using and the given filters.
     * 
     * @param filters COSArray or COSName of filters to be used.
     * @return OutputStream for un-encoded stream data.
     * @throws IOException If the output stream could not be created.
     */
    public OutputStream createOutputStream(COSBase filters) throws IOException
    {
        checkClosed();
        if (isWriting)
        {
            throw new IllegalStateException("Cannot have more than one open stream writer.");
        }
        // apply filters, if any
        if (filters != null)
        {
            setItem(COSName.FILTER, filters);
        }
        randomAccess = scratchFile.createBuffer(); // discards old data - TODO: close existing buffer?
        OutputStream randomOut = new RandomAccessOutputStream(randomAccess);
        OutputStream cosOut = new COSOutputStream(getFilterList(), this, randomOut, scratchFile);
        isWriting = true;
        return new FilterOutputStream(cosOut)
        {
            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                this.out.write(b, off, len);
            }
            
            @Override
            public void close() throws IOException
            {
                super.close();
                setInt(COSName.LENGTH, (int)randomAccess.length());
                isWriting = false;
            }
        };
    }
    
    /**
     * This will create a new stream for which filtered byte should be
     * written to. You probably don't want this but want to use the
     * createUnfilteredStream, which is used to write raw bytes to.
     *
     * @return A stream that can be written to.
     * @throws IOException If there is an error creating the stream.
     * @deprecated Use {@link #createRawOutputStream()} instead.
     */
    @Deprecated
    public OutputStream createFilteredStream() throws IOException
    {
        return createRawOutputStream();
    }

    /**
     * Returns a new OutputStream for writing encoded PDF data. Experts only!
     * 
     * @return OutputStream for raw PDF stream data.
     * @throws IOException If the output stream could not be created.
     */
    public OutputStream createRawOutputStream() throws IOException
    {
        checkClosed();
        if (isWriting)
        {
            throw new IllegalStateException("Cannot have more than one open stream writer.");
        }
        randomAccess = scratchFile.createBuffer(); // discards old data - TODO: close existing buffer?
        OutputStream out = new RandomAccessOutputStream(randomAccess);
        isWriting = true;
        return new FilterOutputStream(out)
        {
            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                this.out.write(b, off, len);
            }
            
            @Override
            public void close() throws IOException
            {
                super.close();
                setInt(COSName.LENGTH, (int)randomAccess.length());
                isWriting = false;
            }
        };
    }
    
    /**
     * Returns the list of filters.
     */
    private List<Filter> getFilterList() throws IOException
    {
        List<Filter> filterList = new ArrayList<>();
        COSBase filters = getFilters();
        if (filters instanceof COSName)
        {
            filterList.add(FilterFactory.INSTANCE.getFilter((COSName)filters));
        }
        else if (filters instanceof COSArray)
        {
            COSArray filterArray = (COSArray)filters;
            for (int i = 0; i < filterArray.size(); i++)
            {
                COSName filterName = (COSName)filterArray.get(i);
                filterList.add(FilterFactory.INSTANCE.getFilter(filterName));
            }
        }
        return filterList;
    }
    
    /**
     * Returns the length of the encoded stream.
     *
     * @return length in bytes
     */
    public long getLength()
    {
        if (isWriting)
        {
            throw new IllegalStateException("There is an open OutputStream associated with " +
                                            "this COSStream. It must be closed before querying" +
                                            "length of this COSStream.");
        }
        return getInt(COSName.LENGTH, 0);
    }

    /**
     * This will return the filters to apply to the byte stream.
     * The method will return
     * - null if no filters are to be applied
     * - a COSName if one filter is to be applied
     * - a COSArray containing COSNames if multiple filters are to be applied
     *
     * @return the COSBase object representing the filters
     */
    public COSBase getFilters()
    {
        return getDictionaryObject(COSName.FILTER);
    }
    
    /**
     * Sets the filters to be applied when encoding or decoding the stream.
     *
     * @param filters The filters to set on this stream.
     * @throws IOException If there is an error clearing the old filters.
     * @deprecated Use {@link #createOutputStream(COSBase)} instead.
     */
    @Deprecated
    public void setFilters(COSBase filters) throws IOException
    {
        setItem(COSName.FILTER, filters);
    }

    /**
     * Returns the contents of the stream as a text string.
     * 
     * @deprecated Use {@link #toTextString()} instead.
     */
    @Deprecated
    public String getString()
    {
        return toTextString();
    }
    
    /**
     * Returns the contents of the stream as a PDF "text string".
     */
    public String toTextString()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try
        {
            input = createInputStream();
            IOUtils.copy(input, out);
        }
        catch (IOException e)
        {
            return "";
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }
        COSString string = new COSString(out.toByteArray());
        return string.getString();
    }
    
    @Override
    public Object accept(ICOSVisitor visitor) throws IOException
    {
        return visitor.visitFromStream(this);
    }
    
    @Override
    public void close() throws IOException
    {
        // marks the scratch file pages as free
        if (randomAccess != null)
        {
            randomAccess.close();
        }
    }
}
