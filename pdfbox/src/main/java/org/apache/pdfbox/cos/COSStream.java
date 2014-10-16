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
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessFileInputStream;
import org.apache.pdfbox.io.RandomAccessFileOutputStream;
import org.apache.pdfbox.pdfparser.PDFStreamParser;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author Ben Litchfield
 */
public class COSStream extends COSDictionary implements Closeable
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(COSStream.class);

    private static final int BUFFER_SIZE=16384;

    /**
     * internal buffer, either held in memory or within a scratch file.
     */
    private RandomAccess buffer;
    /**
     * The stream with all of the filters applied.
     */
    private RandomAccessFileOutputStream filteredStream;

    /**
     * The stream with no filters, this contains the useful data.
     */
    private RandomAccessFileOutputStream unFilteredStream;
    private DecodeResult decodeResult;

    /**
     * Constructor.  Creates a new stream with an empty dictionary.
     *
     */
    public COSStream( )
    {
        this(false, null);
    }

    /**
     * Constructor.
     *
     * @param dictionary The dictionary that is associated with this stream.
     * 
     */
    public COSStream( COSDictionary dictionary )
    {
        this(dictionary, false, null);
    }

    /**
     * Constructor.  Creates a new stream with an empty dictionary.
     * 
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * @param scratchDirectory directory to be used to create the scratch file. If null java.io.temp is used instead.
     *     
     */
    public COSStream( boolean useScratchFiles, File scratchDirectory )
    {
        super();
        if (useScratchFiles)
        {
            createScratchFile(scratchDirectory);
        }
        if (buffer == null)
        {
            buffer = new RandomAccessBuffer();
        }
    }

    /**
     * Constructor.
     *
     * @param dictionary The dictionary that is associated with this stream.
     * @param useScratchFiles enables the usage of a scratch file if set to true
     * @param scratchDirectory directory to be used to create the scratch file. If null java.io.temp is used instead.
     * 
     */
    public COSStream( COSDictionary dictionary, boolean useScratchFiles, File scratchDirectory  )
    {
        super( dictionary );
        if (useScratchFiles)
        {
            createScratchFile(scratchDirectory);
        }
        if (buffer == null)
        {
            buffer = new RandomAccessBuffer();
        }
    }

    /**
     * Create a scratch file to be used as buffer to decrease memory foot print.
     * 
     * @param scratchDirectory directory to be used to create the scratch file. If null java.io.temp is used instead.
     * 
     */
    private void createScratchFile(File scratchDirectory)
    {
        try 
        {
            File scratchFile = File.createTempFile("PDFBox", null, scratchDirectory);
            // mark scratch file to deleted automatically after usage
            scratchFile.deleteOnExit();
            buffer = new RandomAccessFile(scratchFile, "rw");
        }
        catch (IOException exception)
        {
            LOG.error("Can't create temp file, using memory buffer instead", exception);
        }
    }

    /**
     * This will get all the tokens in the stream.
     *
     * @return All of the tokens in the stream.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    public List<Object> getStreamTokens() throws IOException
    {
        PDFStreamParser parser = new PDFStreamParser( this );
        parser.parse();
        return parser.getTokens();
    }

    /**
     * This will get the stream with all of the filters applied.
     *
     * @return the bytes of the physical (encoded) stream
     *
     * @throws IOException when encoding/decoding causes an exception
     */
    public InputStream getFilteredStream() throws IOException
    {
        if( filteredStream == null )
        {
            doEncode();
        }
        long position = filteredStream.getPosition();
        long length = filteredStream.getLengthWritten();

        RandomAccessFileInputStream input =
            new RandomAccessFileInputStream( buffer, position, length );
        return new BufferedInputStream( input, BUFFER_SIZE );
    }

    /**
     * This will get the length of the encoded stream.
     * 
     * @return the length of the encoded stream as long
     *
     * @throws IOException 
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
     * This will set the expected length of the encoded stream. Call this method
     * if the previously set expected length is wrong, to avoid further trouble.
     * 
     * @param length the expected length of the encoded stream.
     */
    public void setFilteredLength(long length)
    {
        filteredStream.setExpectedLength(COSInteger.get(length));
    }

    /**
     * This will get the length of the data written in the encoded stream.
     *
     * @return the length of the data written in the encoded stream as long
     *
     * @throws IOException
     */
    public long getFilteredLengthWritten() throws IOException
    {
        if (filteredStream == null)
        {
            doEncode();
        }
        return filteredStream.getLengthWritten();
    }
    

    /**
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     *
     * @throws IOException when encoding/decoding causes an exception
     */
    public InputStream getUnfilteredStream() throws IOException
    {
        InputStream retval;
        if( unFilteredStream == null )
        {
            doDecode();
        }

        //if unFilteredStream is still null then this stream has not been
        //created yet, so we should return null.
        if( unFilteredStream != null )
        {
            long position = unFilteredStream.getPosition();
            long length = unFilteredStream.getLengthWritten();
            RandomAccessFileInputStream input =
                new RandomAccessFileInputStream( buffer, position, length );
            retval = new BufferedInputStream( input, BUFFER_SIZE );
        }
        else
        {
            // We should check if the COSStream contains data, maybe it
            // has been created with a RandomAccessFile - which is not
            // necessary empty.
            // In this case, the creation was been done as an input, this should
            // be the unfiltered file, since no filter has been applied yet.
//            if ( (file != null) &&
//                    (file.length() > 0) )
//            {
//                retval = new RandomAccessFileInputStream( file,
//                                                          0,
//                                                          file.length() );
//            }
//            else
//            {
                //if there is no stream data then simply return an empty stream.
                retval = new ByteArrayInputStream( new byte[0] );
//            }
        }
        return retval;
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
            throw new IOException("Stream was not read");
        }
        else
        {
            return decodeResult;
        }
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
// FIXME: We shouldn't keep the same reference?
        unFilteredStream = filteredStream;

        COSBase filters = getFilters();
        if( filters == null )
        {
            //then do nothing
            decodeResult = DecodeResult.DEFAULT;
        }
        else if( filters instanceof COSName )
        {
            doDecode( (COSName)filters, 0 );
        }
        else if( filters instanceof COSArray )
        {
            COSArray filterArray = (COSArray)filters;
            for( int i=0; i<filterArray.size(); i++ )
            {
                COSName filterName = (COSName)filterArray.get( i );
                doDecode( filterName, i );
            }
        }
        else
        {
            throw new IOException( "Error: Unknown filter type:" + filters );
        }
    }

    /**
     * This will decode applying a single filter on the stream.
     *
     * @param filterName The name of the filter.
     * @param filterIndex The index of the current filter.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    private void doDecode( COSName filterName, int filterIndex ) throws IOException
    {
        Filter filter = FilterFactory.INSTANCE.getFilter( filterName );

        boolean done = false;
        IOException exception = null;
        long position = unFilteredStream.getPosition();
        long length = unFilteredStream.getLength();
        // in case we need it later
        long writtenLength = unFilteredStream.getLengthWritten();  

        if (length == 0 && writtenLength == 0)
        {
            //if the length is zero then don't bother trying to decode
            //some filters don't work when attempting to decode
            //with a zero length stream.  See zlib_error_01.pdf
            IOUtils.closeQuietly(unFilteredStream);
            unFilteredStream = new RandomAccessFileOutputStream( buffer );
            done = true;
        }
        else
        {
            //ok this is a simple hack, sometimes we read a couple extra
            //bytes that shouldn't be there, so we encounter an error we will just
            //try again with one less byte.
            for (int tryCount = 0; length > 0 && !done && tryCount < 5; tryCount++)
            {
                InputStream input = null;
                try
                {
                    input = new BufferedInputStream(
                        new RandomAccessFileInputStream( buffer, position, length ), BUFFER_SIZE );
                    IOUtils.closeQuietly(unFilteredStream);
                    unFilteredStream = new RandomAccessFileOutputStream( buffer );
                    decodeResult = filter.decode( input, unFilteredStream, this, filterIndex );
                    done = true;
                }
                catch( IOException io )
                {
                    length--;
                    exception = io;
                }
                finally
                {
                    IOUtils.closeQuietly(input);
                }
            }
            if( !done )
            {
                //if no good stream was found then lets try again but with the
                //length of data that was actually read and not length
                //defined in the dictionary
                length = writtenLength;
                for( int tryCount=0; !done && tryCount<5; tryCount++ )
                {
                    InputStream input = null;
                    try
                    {
                        input = new BufferedInputStream(
                            new RandomAccessFileInputStream( buffer, position, length ), BUFFER_SIZE );
                        IOUtils.closeQuietly(unFilteredStream);
                        unFilteredStream = new RandomAccessFileOutputStream( buffer );
                        decodeResult = filter.decode( input, unFilteredStream, this, filterIndex);
                        done = true;
                    }
                    catch( IOException io )
                    {
                        length--;
                        exception = io;
                    }
                    finally
                    {
                        IOUtils.closeQuietly(input);
                    }
                }
            }
        }
        if( !done )
        {
            throw exception;
        }
    }

    /**
     * This will encode the logical byte stream applying all of the filters to the stream.
     *
     * @throws IOException If there is an error applying a filter to the stream.
     */
    private void doEncode() throws IOException
    {
        filteredStream = unFilteredStream;

        COSBase filters = getFilters();
        if( filters == null )
        {
            //there is no filter to apply
        }
        else if( filters instanceof COSName )
        {
            doEncode( (COSName)filters, 0 );
        }
        else if( filters instanceof COSArray )
        {
            // apply filters in reverse order
            COSArray filterArray = (COSArray)filters;
            for( int i=filterArray.size()-1; i>=0; i-- )
            {
                COSName filterName = (COSName)filterArray.get( i );
                doEncode( filterName, i );
            }
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
    private void doEncode( COSName filterName, int filterIndex ) throws IOException
    {
        Filter filter = FilterFactory.INSTANCE.getFilter( filterName );

        InputStream input = new BufferedInputStream(
            new RandomAccessFileInputStream( buffer, filteredStream.getPosition(),
                                                   filteredStream.getLength() ), BUFFER_SIZE );
        IOUtils.closeQuietly(filteredStream);
        filteredStream = new RandomAccessFileOutputStream( buffer );
        filter.encode( input, filteredStream, this, filterIndex );
        IOUtils.closeQuietly(input);
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
        filteredStream = new RandomAccessFileOutputStream( buffer );
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
        unFilteredStream = new RandomAccessFileOutputStream( buffer );
        return new BufferedOutputStream( unFilteredStream, BUFFER_SIZE );
    }
    
    @Override
    public void close()
    {
        try
        {
            if (buffer != null)
            {
                buffer.close();
                buffer = null;
            }
        }
        catch (IOException exception)
        {
            LOG.error("Exception occured when closing the file.", exception);
        }
        if (filteredStream != null)
        {
            IOUtils.closeQuietly(filteredStream);
        }
        if (unFilteredStream != null)
        {
            IOUtils.closeQuietly(unFilteredStream);
        }
        clear();
    }
}
