/**
 * Copyright (c) 2005, www.fontbox.org
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
 * 3. Neither the name of fontbox; nor the names of its
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
 * http://www.fontbox.org
 *
 */
package org.fontbox.ttf;

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
