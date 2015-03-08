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

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a boolean value in the PDF document.
 *
 * @author Ben Litchfield
 */
public final class COSBoolean extends COSBase
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

    private final boolean value;

    /**
     * Constructor.
     *
     * @param aValue The boolean value.
     */
    private COSBoolean(boolean aValue)
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
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public Object accept(ICOSVisitor  visitor) throws IOException
    {
        return visitor.visitFromBoolean(this);
    }

    /**
     * Return a string representation of this object.
     *
     * @return The string value of this object.
     */
    @Override
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
