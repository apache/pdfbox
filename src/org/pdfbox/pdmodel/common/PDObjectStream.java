/**
 * Copyright (c) 2004-2005, www.pdfbox.org
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

import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.PDDocument;



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
        COSStream stream = (COSStream)getStream().getDictionaryObject( "Extends" );
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
        getStream().setItem( "Extends", stream );
    }
}