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

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;



/**
 * A PDStream represents a stream in a PDF document.  Streams are tied to a single
 * PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDObjectStream extends PDStream
{

    /**
     * Constructor.
     *
     * @param str The stream parameter.
     */
    public PDObjectStream( COSStream str )
    {
        super( str );
    }

    /**
     * This will create a new PDStream object.
     *
     * @param document The document that the stream will be part of.
     * @return A new stream object.
     */
    public static PDObjectStream createStream( PDDocument document )
    {
        COSStream cosStream = new COSStream( document.getDocument().getScratchFile() );
        PDObjectStream strm = new PDObjectStream( cosStream );
        strm.getStream().setName( "Type", "ObjStm" );
        return strm;
    }

    /**
     * Get the type of this object, should always return "ObjStm".
     *
     * @return The type of this object.
     */
    public String getType()
    {
        return getStream().getNameAsString( "Type" );
    }

    /**
     * Get the number of compressed object.
     *
     * @return The number of compressed objects.
     */
    public int getNumberOfObjects()
    {
        return getStream().getInt( "N", 0 );
    }

    /**
     * Set the number of objects.
     *
     * @param n The new number of objects.
     */
    public void setNumberOfObjects( int n )
    {
        getStream().setInt( "N", n );
    }

    /**
     * The byte offset (in the decoded stream) of the first compressed object.
     *
     * @return The byte offset to the first object.
     */
    public int getFirstByteOffset()
    {
        return getStream().getInt( "First", 0 );
    }

    /**
     * The byte offset (in the decoded stream) of the first compressed object.
     *
     * @param n The byte offset to the first object.
     */
    public void setFirstByteOffset( int n )
    {
        getStream().setInt( "First", n );
    }

    /**
     * A reference to an object stream, of which the current object stream is
     * considered an extension.
     *
     * @return The object that this stream is an extension.
     */
    public PDObjectStream getExtends()
    {
        PDObjectStream retval = null;
        COSStream stream = (COSStream)getStream().getDictionaryObject( COSName.EXTENDS );
        if( stream != null )
        {
            retval = new PDObjectStream( stream );
        }
        return retval;

    }

    /**
     * A reference to an object stream, of which the current object stream is
     * considered an extension.
     *
     * @param stream The object stream extension.
     */
    public void setExtends( PDObjectStream stream )
    {
        getStream().setItem( COSName.EXTENDS, stream );
    }
}
