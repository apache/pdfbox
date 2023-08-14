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
    private final int[] start;
    private final int[] end;
    private final int codeLength;
    
    /**
     * Creates a new instance of CodespaceRange. The length of both arrays has to be the same.<br>
     * For one byte ranges startBytes and endBytes define a linear range of values. Double byte values define a
     * rectangular range not a linear range. Examples: <br>
     * &lt;00&gt; &lt;20&gt; defines a linear range from 0x00 up to 0x20.<br>
     * &lt;8140&gt; to &lt;9FFC&gt; defines a rectangular range. The high byte has to be within 0x81 and 0x9F and the
     * low byte has to be within 0x40 and 0xFC
     * 
     * @param startBytes start of the range
     * @param endBytes start of the range
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
     * Returns the length of the codes of the codespace.
     * 
     * @return the code length
     */
    public int getCodeLength()
    {
        return codeLength;
    }

    /**
     * Returns true if the given code bytes match this codespace range.
     * 
     * @param code the code bytes to be matched
     * 
     * @return true if the given code bytes match this codespace range
     */
    public boolean matches(byte[] code)
    {
        return isFullMatch(code, code.length);
    }

    /**
     * Returns true if the given number of code bytes match this codespace range.
     *
     * @param code the code bytes to be matched
     * @param codeLen the code length to be used for matching
     * 
     * @return true if the given number of code bytes match this codespace range
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
