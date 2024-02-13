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
    private final float value;
    private String valueAsString;

    public static final COSFloat ZERO = new COSFloat(0f, "0.0");
    public static final COSFloat ONE = new COSFloat(1f, "1.0");

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
     * An internal constructor to avoid formatting for the predefined constants.
     *
     * @param aFloat
     * @param valueString
     */
    private COSFloat(float aFloat, String valueString)
    {
        value = aFloat;
        valueAsString = valueString;
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
        float parsedValue;
        String stringValue = null;
        try
        {
            float f = Float.parseFloat(aFloat);
            parsedValue = coerce(f);
            stringValue = f == parsedValue ? aFloat : null;
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
                parsedValue = coerce(Float.parseFloat(aFloat));
            }
            catch (NumberFormatException e2)
            {
                throw new IOException("Error expected floating point number actual='" + aFloat + "'", e2);
            }
        }
        value = parsedValue;
        valueAsString = stringValue;
    }

    /**
     * Check and coerce the value field to be between MIN_NORMAL and MAX_VALUE.
     * 
     * @param floatValue the value to be checked
     * @return the coerced value
     */
    private float coerce(float floatValue)
    {
        if (floatValue == Float.POSITIVE_INFINITY)
        {
            return Float.MAX_VALUE;
        }
        if (floatValue == Float.NEGATIVE_INFINITY)
        {
            return -Float.MAX_VALUE;
        }
        if (Math.abs(floatValue) < Float.MIN_NORMAL)
        {
            // values smaller than the smallest possible float value are converted to 0
            // see PDF spec, chapter 2 of Appendix C Implementation Limits
            return 0f;
        }
        return floatValue;
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
        return (long) value;
    }

    /**
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    @Override
    public int intValue()
    {
        return (int) value;
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
        return Float.hashCode(value);
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
            String s = String.valueOf(value);
            boolean simpleFormat = s.indexOf('E') < 0;
            valueAsString = simpleFormat ? s
                    : new BigDecimal(s).stripTrailingZeros().toPlainString();
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
