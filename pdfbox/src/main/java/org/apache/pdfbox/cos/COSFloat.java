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
import java.nio.charset.StandardCharsets;

/**
 * This class represents a floating point number in a PDF document.
 *
 * @author Ben Litchfield
 *
 */
public class COSFloat extends COSNumber
{
    private Float value;
    private String valueAsString;

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
            value = Float.parseFloat(aFloat);
            valueAsString = checkMinMaxValues() ? null : aFloat;
        }
        catch( NumberFormatException e )
        {
            if (aFloat.startsWith("--"))
            {
                // PDFBOX-4289 has --16.33
                aFloat = aFloat.substring(1);
            }
            else if (aFloat.matches("^0\\.0*-\\d+"))
            {
                // PDFBOX-2990 has 0.00000-33917698
                // PDFBOX-3369 has 0.00-35095424
                // PDFBOX-3500 has 0.-262
                aFloat = "-" + aFloat.replaceFirst("-", "");
            }
            else
            {
                throw new IOException("Error expected floating point number actual='" + aFloat + "'", e);
            }

            try
            {
                value = Float.parseFloat(aFloat);
                checkMinMaxValues();
            }
            catch (NumberFormatException e2)
            {
                throw new IOException("Error expected floating point number actual='" + aFloat + "'", e2);
            }
        }

    }

    /**
     * Check and coerce the value field to be between MIN_NORMAL and MAX_VALUE. Returns "true" if the value was
     * replaced.
     * 
     * @return true if the value was replaced
     */
    private boolean checkMinMaxValues()
    {
        if (value == Float.POSITIVE_INFINITY)
        {
            value = Float.MAX_VALUE;
        }
        else if (value == Float.NEGATIVE_INFINITY)
        {
            value = -Float.MAX_VALUE;
        }
        else if (Math.abs(value) < Float.MIN_NORMAL)
        {
            // values smaller than the smallest possible float value are converted to 0
            // see PDF spec, chapter 2 of Appendix C Implementation Limits
            value = 0f;
        }
        else
        {
            return false;
        }
        return true;
    }
    
    /**
     * If the string represents a floating point number, this will remove all trailing zeros
     * 
     * @param plainStringValue a decimal number
     */
    private String trimZeros(String plainStringValue)
    {
        int lastIndex = plainStringValue.lastIndexOf('.');
        if (lastIndex > 0)
        {
            int i = plainStringValue.length() - 1;
            while (i > lastIndex + 1 && plainStringValue.charAt(i) == '0')
            {
                i--;
            }
            return plainStringValue.substring(0, i + 1);
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
        return value;
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
                Float.floatToIntBits(((COSFloat)o).value) == Float.floatToIntBits(value);
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
        return "COSFloat{" + formatString() + "}";
    }

    /**
     * Builds, if needed, and returns the string representation of the current value.
     * @return current value as string.
     */
    private String formatString()
    {
        if (valueAsString == null)
        {
            valueAsString = trimZeros(new BigDecimal(String.valueOf(value)).toPlainString());
        }
        return valueAsString;
    }

    /**
     * Visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromFloat(this);
    }

    /**
     * This will output this string as a PDF object.
     *
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        output.write(formatString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
