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
package org.apache.fontbox.cmap;

/**
 * This represents a single entry in the codespace range.
 *
 * @author Ben Litchfield
 */
public class CodespaceRange
{
    private byte[] startBytes;
    private byte[] endBytes;
    private int[] start;
    private int[] end;
    private int codeLength = 0;
    
    /**
     * Creates a new instance of CodespaceRange. The length of both arrays has to be the same.<br>
     * For one byte ranges startBytes and endBytes define a linear range of values. Double byte values define a
     * rectangular range not a linear range. Examples: <br>
     * &lt;00&gt; &lt;20&gt; defines a linear range from 0x00 up to 0x20.<br>
     * &lt;8140&gt; to &lt;9FFC&gt; defines a rectangular range. The high byte has to be within 0x81 and 0x9F and the
     * low byte has to be within 0x40 and 0xFC
     * 
     * @param startBytes
     * @param endBytes
     */
    public CodespaceRange(byte[] startBytes, byte[] endBytes)
    {
        byte[] correctedStartBytes = startBytes;
        if (startBytes.length != endBytes.length && startBytes.length == 1 && startBytes[0] == 0)
        {
            correctedStartBytes = new byte[endBytes.length];
        }
        else if (startBytes.length != endBytes.length)
        {
            throw new IllegalArgumentException(
                    "The start and the end values must not have different lengths.");
        }
        start = new int[correctedStartBytes.length];
        end = new int[endBytes.length];
        for (int i = 0; i < correctedStartBytes.length; i++)
        {
            start[i] = correctedStartBytes[i] & 0xFF;
            end[i] = endBytes[i] & 0xFF;
        }
        codeLength = endBytes.length;
    }

    /**
     * Creates a new instance of CodespaceRange.
     * 
     * @deprecated to be removed in the next major release.
     */
    public CodespaceRange()
    {
    }

    /**
     * Returns the length of the codes of the codespace.
     * 
     * @return the code length
     */
    public int getCodeLength()
    {
        return codeLength;
    }

    /**
     * Getter for property end.
     * 
     * @return Value of property end.
     *
     * @deprecated to be removed in the next major release
     */
    public byte[] getEnd()
    {
        return endBytes;
    }

    /**
     * Setter for property end.
     * 
     * @param endBytes New value of property end.
     *
     * @deprecated to be removed in the next major release
     */
    void setEnd(byte[] endBytes)
    {
        this.endBytes = endBytes;
        end = new int[endBytes.length];
        for (int i = 0; i < endBytes.length; i++)
        {
            end[i] = endBytes[i] & 0xFF;
        }
    }

    /**
     * Getter for property start.
     * 
     * @return Value of property start.
     *
     * @deprecated to be removed in the next major release
     */
    public byte[] getStart()
    {
        return startBytes;
    }

    /**
     * Setter for property start.
     * 
     * @param startBytes New value of property start.
     *
     * @deprecated to be removed in the next major release
     */
    void setStart(byte[] startBytes)
    {
        this.startBytes = startBytes;
        start = new int[startBytes.length];
        for (int i = 0; i < startBytes.length; i++)
        {
            start[i] = startBytes[i] & 0xFF;
        }
        codeLength = startBytes.length;
    }

    /**
     * Returns true if the given code bytes match this codespace range.
     */
    public boolean matches(byte[] code)
    {
        return isFullMatch(code, code.length);
    }

    /**
     * Returns true if the given code bytes match this codespace range.
     */
    public boolean isFullMatch(byte[] code, int codeLen)
    {
        // code must be the same length as the bounding codes
        if (codeLength != codeLen)
        {
            return false;
        }
        for (int i = 0; i < codeLength; i++)
        {
            int codeAsInt = code[i] & 0xFF;
            if (codeAsInt < start[i] || codeAsInt > end[i])
            {
                return false;
            }
        }
        return true;
    }
    
}
