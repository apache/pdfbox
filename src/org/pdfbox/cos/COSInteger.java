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
package org.pdfbox.cos;

import java.io.IOException;
import java.io.OutputStream;

import org.pdfbox.exceptions.COSVisitorException;

/**
 *
 * This class represents an integer number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.12 $
 */
public class COSInteger extends COSNumber
{

    private long value;

    /**
     * constructor.
     *
     * @param val The integer value of this object.
     */
    public COSInteger( long val )
    {
        value = val;
    }

    /**
     * constructor.
     *
     * @param val The integer value of this object.
     */
    public COSInteger( int val )
    {
        this( (long)val );
    }

    /**
     * This will create a new PDF Int object using a string.
     *
     * @param val The string value of the integer.
     *
     * @throws IOException If the val is not an integer type.
     */
    public COSInteger( String val ) throws IOException
    {
        try
        {
            value = Long.parseLong( val );
        }
        catch( NumberFormatException e )
        {
            throw new IOException( "Error: value is not an integer type actual='" + val + "'" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
        return o instanceof COSInteger && ((COSInteger)o).intValue() == intValue();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        //taken from java.lang.Long
        return (int)(value ^ (value >> 32));
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "COSInt{" + value + "}";
    }
    
    /**
     * Change the value of this reference.
     * 
     * @param newValue The new value.
     */
    public void setValue( long newValue )
    {
        value = newValue;
    }

    /**
     * polymorphic access to value as float.
     *
     * @return The float value of this object.
     */
    public float floatValue()
    {
        return value;
    }

    /**
     * polymorphic access to value as float.
     *
     * @return The double value of this object.
     */
    public double doubleValue()
    {
        return value;
    }

    /**
     * Polymorphic access to value as int
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
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
    public long longValue()
    {
        return value;
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
        return visitor.visitFromInt(this);
    }
    
    /**
     * This will output this string as a PDF object.
     *  
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        output.write(String.valueOf(value).getBytes());
    }
}