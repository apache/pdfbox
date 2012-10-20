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
package org.apache.pdfbox.util;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This class will be used for bit flag operations.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class BitFlagHelper
{
    private BitFlagHelper()
    {
        //helper class should not be constructed
    }

    /**
     * Sets the given boolean value at bitPos in the flags.
     *
     * @param dic The dictionary to set the value into.
     * @param field The name of the field to set the value into.
     * @param bitFlag the bit position to set the value in.
     * @param value the value the bit position should have.
     * 
     * @deprecated  use {@link #setFlag(COSDictionary, COSName, int, boolean)} using COSName constants instead
     */
    public static final void setFlag( COSDictionary dic, String field, int bitFlag, boolean value )
    {
       setFlag(dic, COSName.getPDFName(field), bitFlag, value);
    }
    
    /**
     * Sets the given boolean value at bitPos in the flags.
     *
     * @param dic The dictionary to set the value into.
     * @param field The COSName of the field to set the value into.
     * @param bitFlag the bit position to set the value in.
     * @param value the value the bit position should have.
     */
    public static final void setFlag( COSDictionary dic, COSName field, int bitFlag, boolean value )
    {
        int currentFlags = dic.getInt( field, 0 );
        if( value )
        {
            currentFlags = currentFlags | bitFlag;
        }
        else
        {
            currentFlags = currentFlags &= ~bitFlag;
        }
        dic.setInt( field, currentFlags );
    }

    /**
     * Gets the boolean value from the flags at the given bit
     * position.
     *
     * @param dic The dictionary to get the field from.
     * @param field The name of the field to get the flag from.
     * @param bitFlag the bitPosition to get the value from.
     *
     * @return true if the number at bitPos is '1'
     *
     * @deprecated  use {@link #getFlag(COSDictionary, COSName, boolean)} using COSName constants instead
     */
    public static final boolean getFlag(COSDictionary dic, String field, int bitFlag)
    {
        return getFlag(dic, COSName.getPDFName(field), bitFlag);
    }
    
    /**
     * Gets the boolean value from the flags at the given bit
     * position.
     *
     * @param dic The dictionary to get the field from.
     * @param field The COSName of the field to get the flag from.
     * @param bitFlag the bitPosition to get the value from.
     *
     * @return true if the number at bitPos is '1'
     */
    public static final boolean getFlag(COSDictionary dic, COSName field, int bitFlag)
    {
        int ff = dic.getInt( field, 0 );
        return (ff & bitFlag) == bitFlag;
    }
}
