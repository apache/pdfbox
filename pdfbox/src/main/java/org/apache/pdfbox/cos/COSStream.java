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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterManager;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFileInputStream;
import org.apache.pdfbox.io.RandomAccessFileOutputStream;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class COSStream extends COSDictionary
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(COSStream.class);

    private static final int BUFFER_SIZE=16384;

    private RandomAccess file;
    /**
     * The stream with all of the filters applied.
     */
    private RandomAccessFileOutputStream filteredStream;

    /**
     * The stream with no filters, this contains the useful data.
     */
    private RandomAccessFileOutputStream unFilteredStream;

    /**
     * Constructor.  Creates a new stream with an empty dictionary.
     *
     * @param storage The intermediate storage for the stream.
     */
    public COSStream( RandomAccess storage )
    {
        super();
        file = storage;
    }

    /**
     * Constructor.
     *
     * @param dictionary The dictionary that is associated with this stream.
     * @param storage The intermediate storage for the stream.
     */
    public COSStream( COSDictionary dictionary, RandomAccess storage )
    {
        super( dictionary );
        file = storage;
    }

    /**
     * This will replace this object with the data from the new object.  This
     * is used to easily maintain referential integrity when changing references
     * to new objects.
     *
     * @param stream The stream that have the new values in it.
     */
    public void replaceWithStream( COSStream stream )
    {
        this.clear();
        this.addAll( stream );
        file = stream.file;
        filteredStream = stream.filteredStream;
        unFilteredStream = stream.unFilteredStream;
    }

    /**
     * This will get the scratch file associated with this stream.
     *
     * @return The scratch file where this stream is being stored.
     */
    public RandomAccess getScratchFile()
    {
        return file;
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
     * @return the bytes of the physical (endoced) stream
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
        long length = filteredStream.getLength();

        RandomAccessFileInputStream input =
            new RandomAccessFileInputStream( file, position, length );
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
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     *
     * @throws IOException when encoding/decoding causes an exception
     */
    public InputStream getUnfilteredStream() throws IOException
    {
        InputStream retval = null;
        if( unFilteredStream == null )
        {
            doDecode();
        }

        //if unFilteredStream is still null then this stream has not been
        //created yet, so we should return null.
        if( unFilteredStream != null )
        {
            long position = unFilteredStream.getPosition();
            long length = unFilteredStream.getLength();
            RandomAccessFileInputStream input =
                new RandomAccessFileInputStream( file, position, length );
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
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public Object accept(ICOSVisitor visitor) throws COSVisitorException
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
        FilterManager manager = getFilterManager();
        Filter filter = manager.getFilter( filterName );

        boolean done = false;
        IOException exception = null;
        long position = unFilteredStream.getPosition();
        long length = unFilteredStream.getLength();
        // in case we need it later
        long writtenLength = unFilteredStream.getLengthWritten();  

        if( length == 0 )
        {
            //if the length is zero then don't bother trying to decode
            //some filters don't work when attempting to decode
            //with a zero length stream.  See zlib_error_01.pdf
        	IOUtils.closeQuietly(unFilteredStream);
            unFilteredStream = new RandomAccessFileOutputStream( file );
            done = true;
        }
        else
        {
            //ok this is a simple hack, sometimes we read a couple extra
            //bytes that shouldn't be there, so we encounter an error we will just
            //try again with one less byte.
            for( int tryCount=0; !done && tryCount<5; tryCount++ )
            {
                InputStream input = null;
                try
                {
                    input = new BufferedInputStream(
                        new RandomAccessFileInputStream( file, position, length ), BUFFER_SIZE );
                	IOUtils.closeQuietly(unFilteredStream);
                    unFilteredStream = new RandomAccessFileOutputStream( file );
                    filter.decode( input, unFilteredStream, this, filterIndex );
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
                            new RandomAccessFileInputStream( file, position, length ), BUFFER_SIZE );
                    	IOUtils.closeQuietly(unFilteredStream);
                        unFilteredStream = new RandomAccessFileOutputStream( file );
                        filter.decode( input, unFilteredStream, this, filterIndex );
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
        FilterManager manager = getFilterManager();
        Filter filter = manager.getFilter( filterName );

        InputStream input = new BufferedInputStream(
            new RandomAccessFileInputStream( file, filteredStream.getPosition(),
                                                   filteredStream.getLength() ), BUFFER_SIZE );
        IOUtils.closeQuietly(filteredStream);
        filteredStream = new RandomAccessFileOutputStream( file );
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
        filteredStream = new RandomAccessFileOutputStream( file );
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
      	IOUtils.closeQuietly(unFilteredStream);
       	unFilteredStream = null;
    	IOUtils.closeQuietly(filteredStream);
        filteredStream = new RandomAccessFileOutputStream( file );
        filteredStream.setExpectedLength( expectedLength );
        return new BufferedOutputStream( filteredStream, BUFFER_SIZE );
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
        unFilteredStream = new RandomAccessFileOutputStream( file );
        return new BufferedOutputStream( unFilteredStream, BUFFER_SIZE );
    }
    
    public void close()
    {
    	try
    	{
    		if (file != null)
    		{
    			file.close();
    			file = null;
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
