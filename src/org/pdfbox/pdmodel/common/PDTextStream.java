/**
 * Copyright (c) 2004, www.pdfbox.org
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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;

/**
 * A PDTextStream class is used when the PDF specification supports either
 * a string or a stream for the value of an object.  This is usually when
 * a value could be large or small, for example a JavaScript method.  This
 * class will help abstract that and give a single unified interface to
 * those types of fields.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDTextStream implements COSObjectable
{
    private COSString string;
    private COSStream stream;

    /**
     * Constructor.
     *
     * @param str The string parameter.
     */
    public PDTextStream( COSString str )
    {
        string = str;
    }

    /**
     * Constructor.
     *
     * @param str The string parameter.
     */
    public PDTextStream( String str )
    {
        string = new COSString( str );
    }

    /**
     * Constructor.
     *
     * @param str The stream parameter.
     */
    public PDTextStream( COSStream str )
    {
        stream = str;
    }

    /**
     * This will create the text stream object.  base must either be a string
     * or a stream.
     *
     * @param base The COS text stream object.
     *
     * @return A PDTextStream that wraps the base object.
     */
    public static PDTextStream createTextStream( COSBase base )
    {
        PDTextStream retval = null;
        if( base instanceof COSString )
        {
            retval = new PDTextStream( (COSString) base );
        }
        else if( base instanceof COSStream )
        {
            retval = new PDTextStream( (COSStream)base );
        }
        return retval;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        COSBase retval = null;
        if( string == null )
        {
            retval = stream;
        }
        else
        {
            retval = string;
        }
        return retval;
    }

    /**
     * This will get this value as a string.  If this is a stream then it
     * will load the entire stream into memory, so you should only do this when
     * the stream is a manageable size.
     *
     * @return This value as a string.
     *
     * @throws IOException If an IO error occurs while accessing the stream.
     */
    public String getAsString() throws IOException
    {
        String retval = null;
        if( string != null )
        {
            retval = string.getString();
        }
        else
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[ 1024 ];
            int amountRead = -1;
            InputStream is = stream.getUnfilteredStream();
            while( (amountRead = is.read( buffer ) ) != -1 )
            {
                out.write( buffer, 0, amountRead );
            }
            retval = new String( out.toByteArray() );
        }
        return retval;
    }

    /**
     * This is the preferred way of getting data with this class as it uses
     * a stream object.
     *
     * @return The stream object.
     *
     * @throws IOException If an IO error occurs while accessing the stream.
     */
    public InputStream getAsStream() throws IOException
    {
        InputStream retval = null;
        if( string != null )
        {
            retval = new ByteArrayInputStream( string.getBytes() );
        }
        else
        {
            retval = stream.getUnfilteredStream();
        }
        return retval;
    }
}