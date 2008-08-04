/**
 * Copyright (c) 2003-2007, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.ICOSVisitor;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.io.RandomAccess;

import org.pdfbox.pdfparser.PDFStreamParser;

/**
 * This will take an array of streams and sequence them together.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class COSStreamArray extends COSStream
{
    private COSArray streams;

    /**
     * The first stream will be used to delegate some of the methods for this
     * class.
     */
    private COSStream firstStream;

    /**
     * Constructor.
     *
     * @param array The array of COSStreams to concatenate together.
     */
    public COSStreamArray( COSArray array )
    {
        super( new COSDictionary(), null );
        streams = array;
        if( array.size() > 0 )
        {
            firstStream = (COSStream)array.getObject( 0 );
        }
    }
    
    /**
     * This will get a stream (or the reference to a stream) from the array.
     * 
     * @param index The index of the requested stream
     * @return The stream object or a reference to a stream
     */
    public COSBase get( int index )
    {
        return streams.get( index );
    }
    
    /**
     * This will get the number of streams in the array.
     * 
     * @return the numer of streams
     */
    public int getStreamCount()
    {
        return streams.size();
    }
    
    /**
     * This will get the scratch file associated with this stream.
     *
     * @return The scratch file where this stream is being stored.
     */
    public RandomAccess getScratchFile()
    {
        return firstStream.getScratchFile();
    }

    /**
     * This will get an object from this streams dictionary.
     *
     * @param key The key to the object.
     *
     * @return The dictionary object with the key or null if one does not exist.
     */
    public COSBase getItem( COSName key )
    {
        return firstStream.getItem( key );
    }

   /**
     * This will get an object from this streams dictionary and dereference it
     * if necessary.
     *
     * @param key The key to the object.
     *
     * @return The dictionary object with the key or null if one does not exist.
     */
    public COSBase getDictionaryObject( COSName key )
    {
        return firstStream.getDictionaryObject( key );
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        String result = "COSStream{}";
        return result;
    }

    /**
     * This will get all the tokens in the stream.
     *
     * @return All of the tokens in the stream.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    public List getStreamTokens() throws IOException
    {
        List retval = null;
        if( streams.size() > 0 )
        {
            PDFStreamParser parser = new PDFStreamParser( this );
            parser.parse();
            retval = parser.getTokens();
        }
        else
        {
            retval = new ArrayList();
        }
        return retval;
    }

    /**
     * This will get the dictionary that is associated with this stream.
     *
     * @return the object that is associated with this stream.
     */
    public COSDictionary getDictionary()
    {
        return firstStream;
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
        throw new IOException( "Error: Not allowed to get filtered stream from array of streams." );
        /**
        Vector inputStreams = new Vector();
        byte[] inbetweenStreamBytes = "\n".getBytes();

        for( int i=0;i<streams.size(); i++ )
        {
            COSStream stream = (COSStream)streams.getObject( i );
        }

        return new SequenceInputStream( inputStreams.elements() );
        **/
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
        Vector inputStreams = new Vector();
        byte[] inbetweenStreamBytes = "\n".getBytes();

        for( int i=0;i<streams.size(); i++ )
        {
            COSStream stream = (COSStream)streams.getObject( i );
            inputStreams.add( stream.getUnfilteredStream() );
            //handle the case where there is no whitespace in the
            //between streams in the contents array, without this
            //it is possible that two operators will get concatenated
            //together
            inputStreams.add( new ByteArrayInputStream( inbetweenStreamBytes ) );
        }

        return new SequenceInputStream( inputStreams.elements() );
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
        return streams.accept( visitor );
    }


    /**
     * This will return the filters to apply to the byte stream
     * the method will return.
     * - null if no filters are to be applied
     * - a COSName if one filter is to be applied
     * - a COSArray containing COSNames if multiple filters are to be applied
     *
     * @return the COSBase object representing the filters
     */
    public COSBase getFilters()
    {
        return firstStream.getFilters();
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
        return firstStream.createFilteredStream();
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
        return firstStream.createFilteredStream( expectedLength );
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
        //should this be allowed?  Should this
        //propagate to all streams in the array?
        firstStream.setFilters( filters );
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
        return firstStream.createUnfilteredStream();
    }
    
    /**
     * Appends a new stream to the array that represents this object's stream.
     * 
     * @param streamToAppend The stream to append.
     */
    public void appendStream(COSStream streamToAppend)
    {
        streams.add(streamToAppend);
    }

}