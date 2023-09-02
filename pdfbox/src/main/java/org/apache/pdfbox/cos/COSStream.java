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

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessOutputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadView;
import org.apache.pdfbox.io.RandomAccessStreamCache;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author Ben Litchfield
 */
public class COSStream extends COSDictionary implements Closeable
{
    // backing store, in-memory or on-disk
    private RandomAccess randomAccess;
    // used as a temp buffer when creating a new stream
    private RandomAccessStreamCache streamCache;
    // indicates if the stream cache was created within this COSStream instance
    private boolean closeStreamCache = false;
    // true if there's an open OutputStream
    private boolean isWriting;
    // random access view to be read from
    private RandomAccessReadView randomAccessReadView;
    
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
        this(null);
    }
    
    /**
     * Creates a new stream with an empty dictionary. Data is stored in the given scratch file.
     *
     * @param streamCache Stream cache for writing stream data.
     */
    public COSStream(RandomAccessStreamCache streamCache)
    {
        setInt(COSName.LENGTH, 0);
        this.streamCache = streamCache;
    }

    /**
     * Creates a new stream with an empty dictionary. Data is read from the given random accessview. Written data is
     * stored in the given scratch file.
     *
     * @param streamCache Stream cache for writing stream data.
     * @param randomAccessReadView source for the data to be read
     * @throws IOException if the length of the random access view isn't available
     */
    public COSStream(RandomAccessStreamCache streamCache, RandomAccessReadView randomAccessReadView)
            throws IOException
    {
        this(streamCache);
        this.randomAccessReadView = randomAccessReadView;
        setInt(COSName.LENGTH, (int) randomAccessReadView.length());
    }

    /**
     * Throws if the random access backing store has been closed. Helpful for catching cases where
     * a user tries to use a COSStream which has outlived its COSDocument.
     */
    private void checkClosed() throws IOException
    {
        if (randomAccess != null && randomAccess.isClosed())
        {
            throw new IOException("COSStream has been closed and cannot be read. " +
                                  "Perhaps its enclosing PDDocument has been closed?");
            // Tip for debugging: look at the destination file with an editor, you'll see an 
            // incomplete stream at the bottom.
        }
    }

    private RandomAccessStreamCache getStreamCache() throws IOException
    {
        if (streamCache == null)
        {
            streamCache = IOUtils.createMemoryOnlyStreamCache().create();
            closeStreamCache = true;
        }
        return streamCache;
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
        if (randomAccess == null)
        {
            if (randomAccessReadView != null)
            {
                randomAccessReadView.seek(0);
                return new RandomAccessInputStream(randomAccessReadView);
            }
            else
            {
                throw new IOException(
                        "Create InputStream called without data being written before to stream.");
            }
        }
        else
        {
            return new RandomAccessInputStream(randomAccess);
        }
    }

    /**
     * Returns a new InputStream which reads the decoded stream data.
     * 
     * @return InputStream containing decoded stream data.
     * @throws IOException If the stream could not be read.
     */
    public COSInputStream createInputStream() throws IOException
    {
        return createInputStream(DecodeOptions.DEFAULT);
    }

    public COSInputStream createInputStream(DecodeOptions options) throws IOException
    {
        InputStream input = createRawInputStream();
        return COSInputStream.create(getFilterList(), this, input, options);
    }

    /**
     * Returns a new RandomAccessRead which reads the decoded stream data.
     * 
     * @return RandomAccessRead containing decoded stream data.
     * @throws IOException If the stream could not be read.
     */
    public RandomAccessRead createView() throws IOException
    {
        List<Filter> filterList = getFilterList();
        if (filterList.isEmpty())
        {
            if (randomAccess == null && randomAccessReadView != null)
            {
                return new RandomAccessReadView(randomAccessReadView, 0,
                        randomAccessReadView.length());
            }
            else
            {
                return new RandomAccessReadBuffer(createRawInputStream());
            }
        }
        return Filter.decode(createRawInputStream(), filterList, this, DecodeOptions.DEFAULT, null);
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
        if (randomAccess != null)
            randomAccess.clear();
        else
            randomAccess = getStreamCache().createBuffer();
        OutputStream randomOut = new RandomAccessOutputStream(randomAccess);
        OutputStream cosOut = new COSOutputStream(getFilterList(), this, randomOut,
                getStreamCache());
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
        if (randomAccess != null)
            randomAccess.clear();
        else
            randomAccess = getStreamCache().createBuffer();
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
        List<Filter> filterList;
        COSBase filters = getFilters();
        if (filters instanceof COSName)
        {
            filterList = new ArrayList<>(1);
            filterList.add(FilterFactory.INSTANCE.getFilter((COSName)filters));
        }
        else if (filters instanceof COSArray)
        {
            COSArray filterArray = (COSArray)filters;
            filterList = new ArrayList<>(filterArray.size());
            for (int i = 0; i < filterArray.size(); i++)
            {
                COSBase base = filterArray.get(i);
                if (!(base instanceof COSName))
                {
                    throw new IOException("Forbidden type in filter array: " + 
                            (base == null ? "null" : base.getClass().getName()));
                }
                filterList.add(FilterFactory.INSTANCE.getFilter((COSName) base));
            }
        }
        else
        {
            filterList = new ArrayList<>();
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
            throw new IllegalStateException("There is an open OutputStream associated with this " +
                                            "COSStream. It must be closed before querying the " +
                                            "length of this COSStream.");
        }
        return getInt(COSName.LENGTH, 0);
    }

    /**
     * This will return the filters to apply to the byte stream.
     * The method will return
     * <ul>
     * <li>null if no filters are to be applied
     * <li>a COSName if one filter is to be applied
     * <li>a COSArray containing COSNames if multiple filters are to be applied
     * </ul>
     *
     * @return the COSBase object representing the filters
     */
    public COSBase getFilters()
    {
        return getDictionaryObject(COSName.FILTER);
    }
    
    /**
     * Returns the contents of the stream as a PDF "text string".
     * 
     * @return the PDF string representation of the stream content
     */
    public String toTextString()
    {
        try (InputStream input = createInputStream())
        {
            byte[] array = input.readAllBytes();
            COSString string = new COSString(array);
            return string.getString();
        }
        catch (IOException e)
        {
            LOG.debug("An exception occurred trying to get the content - returning empty string instead", e);
            return "";
        }
    }
    
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromStream(this);
    }
    
    /**
     * {@inheritDoc}
     *
     * Called by PDFBox when the PDDocument is closed, this closes the stream and removes the data. You will usually not
     * need this.
     *
     * @throws IOException if something went wrong when closing the stream
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            if (closeStreamCache && streamCache != null)
            {
                streamCache.close();
                streamCache = null;
            }
        }
        finally
        {
            try
            {
                // marks the scratch file pages as free
                if (randomAccess != null)
                {
                    randomAccess.close();
                    randomAccess = null;
                }
            }
            finally
            {
                if (randomAccessReadView != null)
                {
                    randomAccessReadView.close();
                    randomAccessReadView = null;
                }
            }
        }
    }

    /**
     * Indicates whether the stream contains any data or not.
     * 
     * @return true if the stream contains any data
     */
    public boolean hasData()
    {
        return randomAccess != null || randomAccessReadView != null;
    }
}
