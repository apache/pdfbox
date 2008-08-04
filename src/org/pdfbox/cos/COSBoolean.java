/**
 * Copyright (c) 2003, www.pdfbox.org
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
package org.pdfbox.cos;

import java.io.IOException;
import java.io.OutputStream;

import org.pdfbox.exceptions.COSVisitorException;

/**
 * This class represents a boolean value in the PDF document.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public class COSBoolean extends COSBase
{
    /**
     * The true boolean token.
     */
    public static final byte[] TRUE_BYTES = new byte[]{ 116, 114, 117, 101 }; //"true".getBytes( "ISO-8859-1" );
    /**
     * The false boolean token.
     */
    public static final byte[] FALSE_BYTES = new byte[]{ 102, 97, 108, 115, 101 }; //"false".getBytes( "ISO-8859-1" );
    
    /**
     * The PDF true value.
     */
    public static final COSBoolean TRUE = new COSBoolean( true );

    /**
     * The PDF false value.
     */
    public static final COSBoolean FALSE = new COSBoolean( false );

    private boolean value;

    /**
     * Constructor.
     *
     * @param aValue The boolean value.
     */
    private COSBoolean(boolean aValue )
    {
        value = aValue;
    }

    /**
     * This will get the value that this object wraps.
     *
     * @return The boolean value of this object.
     */
    public boolean getValue()
    {
        return value;
    }

    /**
     * This will get the value that this object wraps.
     *
     * @return The boolean value of this object.
     */
    public Boolean getValueAsObject()
    {
        return (value?Boolean.TRUE:Boolean.FALSE);
    }

    /**
     * This will get the boolean value.
     *
     * @param value Parameter telling which boolean value to get.
     *
     * @return The single boolean instance that matches the parameter.
     */
    public static COSBoolean getBoolean( boolean value )
    {
        return (value?TRUE:FALSE);
    }

    /**
     * This will get the boolean value.
     *
     * @param value Parameter telling which boolean value to get.
     *
     * @return The single boolean instance that matches the parameter.
     */
    public static COSBoolean getBoolean( Boolean value )
    {
        return getBoolean( value.booleanValue() );
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public Object accept(ICOSVisitor  visitor) throws COSVisitorException
    {
        return visitor.visitFromBoolean(this);
    }

    /**
     * Return a string representation of this object.
     *
     * @return The string value of this object.
     */
    public String toString()
    {
        return String.valueOf( value );
    }
    
    /**
     * This will write this object out to a PDF stream.
     * 
     * @param output The stream to write this object out to.
     * 
     * @throws IOException If an error occurs while writing out this object.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        if( value )
        {
            output.write( TRUE_BYTES );
        }
        else 
        {
            output.write( FALSE_BYTES );
        }
    }
}