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
package org.apache.pdfbox.pdfviewer;

import org.apache.pdfbox.cos.COSName;


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
