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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import org.apache.pdfbox.exceptions.COSVisitorException;

/**
 * This class represents a floating point number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.17 $
 */
public class COSFloat extends COSNumber
{
    private float value;

    /**
     * Constructor.
     *
     * @param aFloat The primitive float object that this object wraps.
     */
    public COSFloat( float aFloat )
    {
        value = aFloat;
    }

    /**
     * Constructor.
     *
     * @param aFloat The primitive float object that this object wraps.
     *
     * @throws IOException If aFloat is not a float.
     */
    public COSFloat( String aFloat ) throws IOException
    {
        try
        {
            value = Float.parseFloat( aFloat );
        }
        catch( NumberFormatException e )
        {
            throw new IOException( "Error expected floating point number actual='" +aFloat + "'" );
        }
    }

    /**
     * Set the value of the float object.
     *
     * @param floatValue The new float value.
     */
    public void setValue( float floatValue )
    {
        value = floatValue;
    }

    /**
     * The value of the float object that this one wraps.
     *
     * @return The value of this object.
     */
    public float floatValue()
    {
        return value;
    }

    /**
     * The value of the double object that this one wraps.
     *
     * @return The double of this object.
     */
    public double doubleValue()
    {
        return value;
    }

    /**
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    public long longValue()
    {
        return (long)value;
    }

    /**
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    public int intValue()
    {
        return (int)value;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        return o instanceof COSFloat && Float.floatToIntBits(((COSFloat)o).value) == Float.floatToIntBits(value);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return Float.floatToIntBits(value);
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "COSFloat{" + value + "}";
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
        return visitor.visitFromFloat(this);
    }

    /**
     * This will output this string as a PDF object.
     *
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        DecimalFormat formatDecimal = (DecimalFormat)NumberFormat.getNumberInstance();
        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
        DecimalFormatSymbols symbols = formatDecimal.getDecimalFormatSymbols();
        symbols.setDecimalSeparator( '.' );
        formatDecimal.setDecimalFormatSymbols( symbols );
        output.write(formatDecimal.format( value ).getBytes("ISO-8859-1"));
    }
}
