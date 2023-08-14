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
import java.nio.charset.StandardCharsets;

/**
 * This class represents an integer number in a PDF document.
 *
 * @author Ben Litchfield
 */
public final class COSInteger extends COSNumber
{

    /**
     * The lowest integer to be kept in the {@link #STATIC} array.
     */
    private static final int LOW = -100;

    /**
     * The highest integer to be kept in the {@link #STATIC} array.
     */
    private static final int HIGH = 256;

    /**
     * Static instances of all COSIntegers in the range from {@link #LOW}
     * to {@link #HIGH}.
     */
    private static final COSInteger[] STATIC = new COSInteger[HIGH - LOW + 1];

    /**
     * Constant for the number zero.
     * @since Apache PDFBox 1.1.0
     */
    public static final COSInteger ZERO = get(0); 

    /**
     * Constant for the number one.
     * @since Apache PDFBox 1.1.0
     */
    public static final COSInteger ONE = get(1); 

    /**
     * Constant for the number two.
     * @since Apache PDFBox 1.1.0
     */
    public static final COSInteger TWO = get(2); 

    /**
     * Constant for the number three.
     * @since Apache PDFBox 1.1.0
     */
    public static final COSInteger THREE = get(3); 

    /**
     * Constant for an out of range value which is bigger than Log.MAX_VALUE.
     */
    protected static final COSInteger OUT_OF_RANGE_MAX = getInvalid(true);

    /**
     * Constant for an out of range value which is smaller than Log.MIN_VALUE.
     */
    protected static final COSInteger OUT_OF_RANGE_MIN = getInvalid(false);

    /**
     * Returns a COSInteger instance with the given value.
     *
     * @param val integer value
     * @return COSInteger instance
     */
    public static COSInteger get(long val)
    {
        if (LOW <= val && val <= HIGH)
        {
            int index = (int) val - LOW;
            // no synchronization needed
            if (STATIC[index] == null)
            {
                STATIC[index] = new COSInteger(val, true);
            }
            return STATIC[index];
        }
        return new COSInteger(val, true);
    }

    private static COSInteger getInvalid(boolean maxValue)
    {
        return maxValue ? new COSInteger(Long.MAX_VALUE, false)
                : new COSInteger(Long.MIN_VALUE, false);
    }

    private final long value;
    private final boolean isValid;

    /**
     * constructor.
     *
     * @param val The integer value of this object.
     * @param valid indicates if the value is valid.
     */
    private COSInteger(long val, boolean valid)
    {
        value = val;
        isValid = valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        return o instanceof COSInteger && ((COSInteger)o).intValue() == intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        //taken from java.lang.Long
        return (int)(value ^ (value >> 32));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSInt{" + value + "}";
    }

    /**
     * polymorphic access to value as float.
     *
     * @return The float value of this object.
     */
    @Override
    public float floatValue()
    {
        return value;
    }

    /**
     * Polymorphic access to value as int
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    @Override
    public int intValue()
    {
        return (int)value;
    }

    /**
     * Polymorphic access to value as int
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
    @Override
    public long longValue()
    {
        return value;
    }

    /**
     * Indicates whether this instance represents a valid value.
     * 
     * @return true if the value is valid
     */
    public boolean isValid()
    {
        return isValid;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromInt(this);
    }

    /**
     * This will output this string as a PDF object.
     *
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        output.write(String.valueOf(value).getBytes(StandardCharsets.ISO_8859_1));
    }

}
