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
package org.apache.pdfbox.pdmodel.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.ICOSVisitor;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;

import org.apache.pdfbox.pdfparser.PDFStreamParser;

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
     * @return the number of streams
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
        return "COSStream{}";
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
        Vector<InputStream> inputStreams = new Vector<InputStream>();
        byte[] inbetweenStreamBytes = "\n".getBytes("ISO-8859-1");

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
    
    /**
     * Insert the given stream at the beginning of the existing stream array.
     * @param streamToBeInserted
     */
    public void insertCOSStream(PDStream streamToBeInserted)
    {
        COSArray tmp = new COSArray();
        tmp.add(streamToBeInserted);
        tmp.addAll(streams);
        streams.clear();
        streams = tmp;
    }

}
