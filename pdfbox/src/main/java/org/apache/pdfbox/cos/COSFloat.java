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
import java.math.BigDecimal;

/**
 * This class represents a floating point number in a PDF document.
 *
 * @author Ben Litchfield
 * 
 */
public class COSFloat extends COSNumber
{
    private BigDecimal value;
    private String valueAsString;

    /**
     * Constructor.
     *
     * @param aFloat The primitive float object that this object wraps.
     */
    public COSFloat( float aFloat )
    {
        // use a BigDecimal as intermediate state to avoid 
        // a floating point string representation of the float value
        value = new BigDecimal(String.valueOf(aFloat));
        valueAsString = removeNullDigits(value.toPlainString());
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
            valueAsString = aFloat; 
            value = new BigDecimal( valueAsString );
        }
        catch( NumberFormatException e )
        {
            if (aFloat.startsWith("0.00000-"))
            {
                // PDFBOX-2990 has 0.00000-33917698
                // Let's wait what other floats will be coming before doing a more general workaround.
                try
                {
                    valueAsString = "-0.00000" + aFloat.substring(8);
                    value = new BigDecimal(valueAsString);
                }
                catch (NumberFormatException e2)
                {
                    throw new IOException("Error expected floating point number actual='" + aFloat + "'", e2);
                }
            }
            else
            {
                throw new IOException("Error expected floating point number actual='" + aFloat + "'", e);
            }
        }
    }

    private String removeNullDigits(String plainStringValue)
    {
        // remove fraction digit "0" only
        if (plainStringValue.indexOf('.') > -1 && !plainStringValue.endsWith(".0"))
        {
            while (plainStringValue.endsWith("0") && !plainStringValue.endsWith(".0"))
            {
                plainStringValue = plainStringValue.substring(0,plainStringValue.length()-1);
            }
        }
        return plainStringValue;
    }

    /**
     * The value of the float object that this one wraps.
     *
     * @return The value of this object.
     */
    @Override
    public float floatValue()
    {
        return value.floatValue();
    }

    /**
     * The value of the double object that this one wraps.
     *
     * @return The double of this object.
     */
    @Override
    public double doubleValue()
    {
        return value.doubleValue();
    }

    /**
     * This will get the long value of this object.
     *
     * @return The long value of this object,
     */
    @Override
    public long longValue()
    {
        return value.longValue();
    }

    /**
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    @Override
    public int intValue()
    {
        return value.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        return o instanceof COSFloat && 
                Float.floatToIntBits(((COSFloat)o).value.floatValue()) == Float.floatToIntBits(value.floatValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSFloat{" + valueAsString + "}";
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public Object accept(ICOSVisitor visitor) throws IOException
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
        output.write(valueAsString.getBytes("ISO-8859-1"));
    }
}
