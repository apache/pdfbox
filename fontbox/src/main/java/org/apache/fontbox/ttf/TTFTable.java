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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class TTFTable 
{
    private String tag;
    private long checkSum;
    private long offset;
    private long length;
      
    /**
     * @return Returns the checkSum.
     */
    public long getCheckSum() 
    {
        return checkSum;
    }
    /**
     * @param checkSumValue The checkSum to set.
     */
    public void setCheckSum(long checkSumValue) 
    {
        this.checkSum = checkSumValue;
    }
    /**
     * @return Returns the length.
     */
    public long getLength() 
    {
        return length;
    }
    /**
     * @param lengthValue The length to set.
     */
    public void setLength(long lengthValue) 
    {
        this.length = lengthValue;
    }
    /**
     * @return Returns the offset.
     */
    public long getOffset() 
    {
        return offset;
    }
    /**
     * @param offsetValue The offset to set.
     */
    public void setOffset(long offsetValue) 
    {
        this.offset = offsetValue;
    }
    /**
     * @return Returns the tag.
     */
    public String getTag() 
    {
        return tag;
    }
    /**
     * @param tagValue The tag to set.
     */
    public void setTag(String tagValue) 
    {
        this.tag = tagValue;
    }
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
    }
}
