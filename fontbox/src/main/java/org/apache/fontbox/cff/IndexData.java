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
package org.apache.fontbox.cff;

import java.util.Arrays;

/**
 * Class holding the IndexData of a CFF font. 
 */
public class IndexData
{
    private int count;
    private int[] offset;
    private int[] data;

    /**
     * Constructor.
     * 
     * @param count number of index values
     */
    public IndexData(int count)
    {
        this.count = count;
        this.offset = new int[count+1];
    }

    public byte[] getBytes(int index)
    {
        int length = offset[index + 1] - offset[index];
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
        {
            bytes[i] = (byte) data[offset[index] - 1 + i];
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "[count=" + count  
                + ", offset=" + Arrays.toString(offset)
                + ", data=" + Arrays.toString(data) + "]";
    }
    
    /**
     * Returns the count value.
     * @return the count value
     */
    public int getCount() 
    {
        return count;
    }
    
    /**
     * Sets the offset value to the given value.
     * @param index the index of the offset value
     * @param value the given offset value
     */
    public void setOffset(int index, int value) 
    {
        offset[index] = value;
    }

    /**
     * Returns the offset at the given index.
     * @param index the index
     * @return the offset value at the given index
     */
    public int getOffset(int index) 
    {
        return offset[index];
    }

    /**
     * Initializes the data array with the given size.
     * @param dataSize the size of the data array
     */
    public void initData(int dataSize) 
    {
        data = new int[dataSize];
    }
    
    /**
     * Sets the data value to the given value.
     * @param index the index of the data value
     * @param value the given data value
     */
    public void setData(int index, int value) 
    {
        data[index] = value;
    }
}