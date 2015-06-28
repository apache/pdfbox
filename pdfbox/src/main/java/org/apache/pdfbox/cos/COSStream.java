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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessReadInputStream;
import org.apache.pdfbox.io.RandomAccessFileOutputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadWrapper;
import org.apache.pdfbox.io.ScratchFile;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author Ben Litchfield
 */
public class COSStream extends COSDictionary implements Closeable
{
    private static final int BUFFER_SIZE=16384;

    /**
     * internal buffer, either held in memory or within a scratch file.
     */
    private RandomAccess tempBuffer;
    private RandomAccess filteredBuffer;
    private RandomAccess unfilteredBuffer;

    /**
     * The stream with all of the filters applied.
     */
    private RandomAccessFileOutputStream filteredStream;

    /**
     * The stream with no filters, this contains the useful data.
     */
    private RandomAccessFileOutputStream unFilteredStream;
    private DecodeResult decodeResult;

    private final ScratchFile scratchFile;

    /**
     * Constructor.  Creates a new stream with an empty dictionary.
     *
     */
    public COSStream( )
    {
        this((ScratchFile)null);
    }

    /**
     * Constructor.
     *
     * @param dictionary The dictionary that is associated with this stream.
     *
     */
    public COSStream( COSDictionary dictionary )
    {
        this(dictionary, null);
    }

    /**
     * Constructor.  Creates a new stream with an empty dictionary.
     *
     * @param scratchFile scratch file to use.
     *
     */
    public COSStream( ScratchFile scratchFile )
    {
        super();
        this.scratchFile = scratchFile;
    }

    /**
     * Constructor.
     *
     * @param dictionary The dictionary that is associated with this stream.
     * @param scratchFile The scratch file to use.
     *
     */
    public COSStream( COSDictionary dictionary, ScratchFile scratchFile )
    {
        super( dictionary );
        this.scratchFile = scratchFile;
    }

    private RandomAccess createBuffer() throws IOException
    {
        if (scratchFile != null)
        {
            return scratchFile.createBuffer();
        }
        else
        {
            return new RandomAccessBuffer();
        }
    }

    /**
     * This will get the stream with all of the filters applied.
     *
     * @return the bytes of the physical (encoded) stream
     *
     * @throws IOException when encoding causes an exception
     */
    public InputStream getFilteredStream() throws IOException
    {
        checkFilteredBuffer();
        InputStream retval;
        if (filteredStream != null)
        {
            long position = filteredStream.getPosition();
            long length = filteredStream.getLengthWritten();
            retval = new BufferedInputStream(
                    new RandomAccessReadInputStream( getFilteredBuffer(), position, length ), BUFFER_SIZE );
        }
        else
        {
            retval = new ByteArrayInputStream( new byte[0] );
        }
        return retval;
    }

    /**
     * This will get the data with all of the filters applied.
     *
     * @return the data of the physical (encoded) stream
     *
     * @throws IOException when encoding causes an exception
     */
    public RandomAccessRead getFilteredRandomAccess() throws IOException
    {
        checkFilteredBuffer();
        RandomAccessRead retval;
        if (filteredStream != null)
        {
            long position = filteredStream.getPosition();
            long length = filteredStream.getLengthWritten();
            retval = new RandomAccessReadWrapper(getFilteredBuffer(), position, length );
        }
        else
        {
            retval = new RandomAccessBuffer();
        }
        return retval;
    }
    
    private void checkFilteredBuffer() throws IOException
    {
        if (getFilteredBuffer() != null && getFilteredBuffer().isClosed())
        {
            throw new IOException("COSStream has been closed and cannot be read. " +
                                  "Perhaps its enclosing PDDocument has been closed?");
        }

        if( filteredStream == null )
        {
            doEncode();
        }
    }

    /**
     * This will get the length of the encoded stream.
     *
     * @return the length of the encoded stream as long
     *
     * @throws IOException if something went wrong
     */
    public long getFilteredLength() throws IOException
    {
        if (filteredStream == null)
        {
            doEncode();
        }
        return filteredStream.getLength();
    }

    /**
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     *
     * @throws IOException when decoding causes an exception
     */
    public InputStream getUnfilteredStream() throws IOException
    {
        checkUnfilteredBuffer();
        InputStream retval;
        if( unFilteredStream != null )
        {
            long position = unFilteredStream.getPosition();
            long length = unFilteredStream.getLengthWritten();
            retval = new BufferedInputStream(
                    new RandomAccessReadInputStream( getUnfilteredBuffer(), position, length ), BUFFER_SIZE );
        }
        else
        {
            retval = new ByteArrayInputStream( new byte[0] );
        }
        return retval;
    }

    /**
     * This will get the logical content with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     *
     * @throws IOException when decoding causes an exception
     */
    public RandomAccessRead getUnfilteredRandomAccess() throws IOException
    {
        checkUnfilteredBuffer();
        RandomAccessRead retval;

        if( unFilteredStream != null )
        {
            long position = unFilteredStream.getPosition();
            long length = unFilteredStream.getLengthWritten();
            retval = new RandomAccessReadWrapper( getUnfilteredBuffer(), position, length );
        }
        else
        {
            retval = new RandomAccessBuffer();
        }
        return retval;
    }
    
    private void checkUnfilteredBuffer() throws IOException
    {
        if (getUnfilteredBuffer() != null && getUnfilteredBuffer().isClosed())
        {
            throw new IOException("COSStream has been closed and cannot be read. " +
                                "Perhaps its enclosing PDDocument has been closed?");
        }

        if( unFilteredStream == null )
        {
            doDecode();
        }
    }
    /**
     * Returns the repaired stream parameters dictionary.
     *
     * @return the repaired stream parameters dictionary
     * @throws IOException when encoding/decoding causes an exception
     */
    public DecodeResult getDecodeResult() throws IOException
    {
        if (unFilteredStream == null)
        {
            doDecode();
        }

        if (unFilteredStream == null || decodeResult == null)
        {
            StringBuilder filterInfo = new StringBuilder();
            COSBase filters = getFilters();
            if (filters != null)
            {
                filterInfo.append(" - filter: ");
                if (filters instanceof COSName)
                {
                    filterInfo.append(((COSName) filters).getName());
                }
                else if (filters instanceof COSArray)
                {
                    COSArray filterArray = (COSArray) filters;
                    for (int i = 0; i < filterArray.size(); i++)
                    {
                        if (filterArray.size() > 1)
                        {
                            filterInfo.append(", ");
                        }
                        filterInfo.append(((COSName) filterArray.get(i)).getName());
                    }
                }
            }
            String subtype = getNameAsString(COSName.SUBTYPE);
            throw new IOException(subtype + " stream was not read" + filterInfo);
        }
        return decodeResult;
    }

    @Override
    public Object accept(ICOSVisitor visitor) throws IOException
    {
        return visitor.visitFromStream(this);
    }

    /**
     * This will decode the physical byte stream applying all of the filters to the stream.
     *
     * @throws IOException If there is an error applying a filter to the stream.
     */
    private void doDecode() throws IOException
    {
        COSBase filters = getFilters();
        if( filters == null )
        {
            // do nothing but copying the data
            unFilteredStream = new RandomAccessFileOutputStream(getUnfilteredBuffer(true));
            IOUtils.copy(getFilteredStream(), unFilteredStream);
            decodeResult = DecodeResult.DEFAULT;
        }
        else if( filters instanceof COSName )
        {
            copyBuffer(getFilteredBuffer(false), filteredStream.getPosition());
            doDecode( (COSName)filters, 0, getFilteredBuffer().length(), getUnfilteredBuffer(true) );
        }
        else if( filters instanceof COSArray )
        {
            copyBuffer(getFilteredBuffer(false),  filteredStream.getPosition() );
            COSArray filterArray = (COSArray)filters;
            int filterArraysize = filterArray.size();
            for( int i=0; i<filterArraysize; i++ )
            {
                COSName filterName = (COSName)filterArray.get( i );
                doDecode( filterName, i, getFilteredBuffer().length(), getUnfilteredBuffer(true) );
                if (i < filterArraysize-1)
                {
                    copyBuffer(getUnfilteredBuffer(false), 0);
                }
            }
            tempBuffer.close();
        }
        else
        {
            throw new IOException( "Error: Unknown filter type:" + filters );
        }
    }

    private void copyBuffer(RandomAccess srcBuffer, long position) throws IOException
    {
        int length = (int)(srcBuffer.length() - position);
        byte[] byteBuffer = new byte[length];
        srcBuffer.seek(position);
        srcBuffer.read(byteBuffer, 0, length);
        if (tempBuffer == null)
        {
            tempBuffer = new RandomAccessBuffer();
        }
        else
        {
            tempBuffer.clear();
        }
        tempBuffer.write(byteBuffer, 0, length);
        tempBuffer.seek(0);
    }
    /**
     * This will decode applying a single filter on the stream.
     *
     * @param filterName The name of the filter.
     * @param filterIndex The index of the current filter.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    private RandomAccess doDecode(COSName filterName, int filterIndex, long length, RandomAccess destBuffer)
            throws IOException
    {
        RandomAccess result = destBuffer;
        Filter filter = FilterFactory.INSTANCE.getFilter(filterName);
        if (length == 0)
        {
            if (result == null)
            {
                result = createBuffer();
            }
        }
        else
        {
            result = attemptDecode(filter, filterIndex, result);
        }
        return result;
    }

    // attempts to decode the stream at the given position and length
    private RandomAccess attemptDecode(Filter filter, int filterIndex, RandomAccess destBuffer) throws IOException
    {
        InputStream input = null;
        RandomAccess result = null;
        try
        {
            input = new BufferedInputStream(
                    new RandomAccessReadInputStream(tempBuffer, 0, tempBuffer.length()), BUFFER_SIZE);
            IOUtils.closeQuietly(unFilteredStream);
            if (destBuffer == null)
            {
                result = createBuffer();
            }
            else
            {
                result = destBuffer;
            }
            unFilteredStream = new RandomAccessFileOutputStream(result);
            decodeResult = filter.decode(input, unFilteredStream, this, filterIndex);
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }
        return result;
    }

    /**
     * This will encode the logical byte stream applying all of the filters to the stream.
     *
     * @throws IOException If there is an error applying a filter to the stream.
     */
    private void doEncode() throws IOException
    {
        COSBase filters = getFilters();
        if( filters == null )
        {
            // there is no filter to apply
            // do nothing but copying the data
            filteredStream = new RandomAccessFileOutputStream(getFilteredBuffer(true));
            IOUtils.copy(getUnfilteredStream(), filteredStream);
        }
        else if( filters instanceof COSName )
        {
            copyBuffer(getUnfilteredBuffer(false), unFilteredStream.getPosition());
            doEncode( (COSName)filters, 0, getFilteredBuffer(true) );
        }
        else if( filters instanceof COSArray )
        {
            copyBuffer(getUnfilteredBuffer(false), unFilteredStream.getPosition() );
            // apply filters in reverse order
            COSArray filterArray = (COSArray)filters;
            for( int i=filterArray.size()-1; i>=0; i-- )
            {
                COSName filterName = (COSName)filterArray.get( i );
                doEncode( filterName, i, getFilteredBuffer(true) );
                if ( i > 0 )
                {
                    copyBuffer(getFilteredBuffer(false), 0);
                }
            }
            tempBuffer.close();
        }
    }

    /**
     * This will encode applying a single filter on the stream.
     *
     * @param filterName The name of the filter.
     * @param filterIndex The index to the filter.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    private RandomAccess doEncode( COSName filterName, int filterIndex, RandomAccess destBuffer ) throws IOException
    {
        RandomAccess result = null;
        Filter filter = FilterFactory.INSTANCE.getFilter( filterName );

        InputStream input = new BufferedInputStream(
            new RandomAccessReadInputStream( tempBuffer, 0, tempBuffer.length()), BUFFER_SIZE );
        IOUtils.closeQuietly(filteredStream);
        if (destBuffer == null)
        {
            result = createBuffer();
        }
        else
        {
            result = destBuffer;
        }
        filteredStream = new RandomAccessFileOutputStream( result );
        filter.encode( input, filteredStream, this, filterIndex );
        IOUtils.closeQuietly(input);
        return result;
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
     * Returns the contents of the stream as a text string.
     */
    public String getString()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try
        {
            input = getUnfilteredStream();
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

    /**
     * This will create a new stream for which filtered byte should be
     * written to.  You probably don't want this but want to use the
     * createUnfilteredStream, which is used to write raw bytes to.
     *
     * @return A stream that can be written to.
     *
     * @throws IOException If there is an error creating the stream.
     */
    public OutputStream createFilteredStream() throws IOException
    {
        IOUtils.closeQuietly(unFilteredStream);
        unFilteredStream = null;
        IOUtils.closeQuietly(filteredStream);
        filteredStream = new RandomAccessFileOutputStream( getFilteredBuffer(true) );
        return new BufferedOutputStream( filteredStream, BUFFER_SIZE );
    }

    /**
     * This will create a new stream for which filtered byte should be
     * written to.  You probably don't want this but want to use the
     * createUnfilteredStream, which is used to write raw bytes to.
     *
     * @param expectedLength An entry where a length is expected.
     *
     * @return A stream that can be written to.
     *
     * @throws IOException If there is an error creating the stream.
     */
    public OutputStream createFilteredStream( COSBase expectedLength ) throws IOException
    {
        OutputStream out = createFilteredStream();
        filteredStream.setExpectedLength(expectedLength);
        return out;
    }

    /**
     * set the filters to be applied to the stream.
     *
     * @param filters The filters to set on this stream.
     *
     * @throws IOException If there is an error clearing the old filters.
     */
    public void setFilters(COSBase filters) throws IOException
    {
        if (unFilteredStream == null)
        {
            // don't lose stream contents
            doDecode();
        }
        setItem(COSName.FILTER, filters);
        // kill cached filtered streams
        IOUtils.closeQuietly(filteredStream);
        filteredStream = null;
    }

    /**
     * This will create an output stream that can be written to.
     *
     * @return An output stream which raw data bytes should be written to.
     *
     * @throws IOException If there is an error creating the stream.
     */
    public OutputStream createUnfilteredStream() throws IOException
    {
        IOUtils.closeQuietly(filteredStream);
        filteredStream = null;
        IOUtils.closeQuietly(unFilteredStream);
        unFilteredStream = new RandomAccessFileOutputStream( getUnfilteredBuffer(true) );
        return new BufferedOutputStream( unFilteredStream, BUFFER_SIZE );
    }

    private RandomAccess getFilteredBuffer()
    {
        return filteredBuffer;
    }

    private RandomAccess getFilteredBuffer(boolean clear) throws IOException
    {
        if (filteredBuffer == null)
        {
            filteredBuffer = createBuffer();
        }
        else if (clear)
        {
            filteredBuffer.clear();
        }
        return filteredBuffer;
    }

    private RandomAccess getUnfilteredBuffer()
    {
        return unfilteredBuffer;
    }

    private RandomAccess getUnfilteredBuffer(boolean clear) throws IOException
    {
        if (unfilteredBuffer == null)
        {
            unfilteredBuffer = createBuffer();
        }
        else if (clear)
        {
            unfilteredBuffer.clear();
        }
        return unfilteredBuffer;
    }


    @Override
    public void close() throws IOException
    {
        if (filteredStream != null)
        {
            filteredStream.close();
        }
        if (unFilteredStream != null)
        {
            unFilteredStream.close();
        }

        if (unfilteredBuffer != null)
        {
            unfilteredBuffer.close();
        }
        if (filteredBuffer != null)
        {
            filteredBuffer.close();
        }
    }
}
