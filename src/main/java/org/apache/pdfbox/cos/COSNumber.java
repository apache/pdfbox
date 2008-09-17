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

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an abstract number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public abstract class COSNumber extends COSBase
{
    /**
     * ZERO.
    */
    public static final COSInteger ZERO = new COSInteger( 0 );
    /**
     * ONE.
    */
    public static final COSInteger ONE = new COSInteger( 1 );
    private static final Map COMMON_NUMBERS = new HashMap();

    static
    {
        COMMON_NUMBERS.put( "0", ZERO );
        COMMON_NUMBERS.put( "1", ONE );
    }

    /**
     * This will get the float value of this number.
     *
     * @return The float value of this object.
     */
    public abstract float floatValue();

    /**
     * This will get the double value of this number.
     *
     * @return The double value of this number.
     */
    public abstract double doubleValue();

    /**
     * This will get the integer value of this number.
     *
     * @return The integer value of this number.
     */
    public abstract int intValue();

    /**
     * This will get the long value of this number.
     *
     * @return The long value of this number.
     */
    public abstract long longValue();

    /**
     * This factory method will get the appropriate number object.
     *
     * @param number The string representation of the number.
     *
     * @return A number object, either float or int.
     *
     * @throws IOException If the string is not a number.
     */
    public static COSNumber get( String number ) throws IOException
    {
        COSNumber result = (COSNumber)COMMON_NUMBERS.get( number );
        if( result == null )
        {
            if (number.indexOf('.') >= 0)
            {
                result = new COSFloat( number );
            }
            else
            {
                result = new COSInteger( number );
            }
        }
        return result;
    }
}
