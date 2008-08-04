/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.util;

import org.pdfbox.cos.COSDictionary;

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
     */
    public static final void setFlag( COSDictionary dic, String field, int bitFlag, boolean value )
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
     */
    public static final boolean getFlag(COSDictionary dic, String field, int bitFlag)
    {
        int ff = dic.getInt( field, 0 );
        return (ff & bitFlag) == bitFlag;
    }
}