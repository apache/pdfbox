/**
 * Copyright (c) 2003, www.pdfbox.org
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
package org.pdfbox.pdfviewer;

import org.pdfbox.cos.COSName;


/**
 * This is a simple class that will contain a key and a value.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class MapEntry
{
    private Object key;
    private Object value;

    /**
     * Get the key for this entry.
     *
     * @return The entry's key.
     */
    public Object getKey()
    {
        return key;
    }

    /**
     * This will set the key for this entry.
     *
     * @param k the new key for this entry.
     */
    public void setKey(Object k)
    {
        key = k;
    }

    /**
     * This will get the value for this entry.
     *
     * @return The value for this entry.
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * This will set the value for this entry.
     *
     * @param val the new value for this entry.
     */
    public void setValue(Object val)
    {
        this.value = val;
    }

    /**
     * This will output a string representation of this class.
     *
     * @return A string representation of this class.
     */
    public String toString()
    {
        String retval = null;
        if( key instanceof COSName )
        {
            retval = ((COSName)key).getName();
        }
        else
        {
            retval = "" +key;
        }
        return retval;
    }
}