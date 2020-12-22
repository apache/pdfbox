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
 * Range of continuous CIDs between two Unicode characters.
 */
class CIDRange
{

    private final int from;
    private int to;
    private final int unicode;
    private final int codeLength;

    /**
     * Constructor.
     * 
     * @param from       start value of COD range
     * @param to         end value of CID range
     * @param unicode        unicode start value
     * @param codeLength byte length of CID values
     */
    CIDRange(final int from, final int to, final int unicode, final int codeLength)
    {
        this.from = from;
        this.to = to;
        this.unicode = unicode;
        this.codeLength = codeLength;
    }

    /**
     * Returns the byte length of the codes of the CID range.
     * 
     * @return the code length
     */
    public int getCodeLength()
    {
        return codeLength;
    }

    /**
     * Maps the given Unicode character to the corresponding CID in this range.
     *
     * @param bytes Unicode character
     * @return corresponding CID, or -1 if the character is out of range
     */
    public int map(final byte[] bytes)
    {
        if (bytes.length == codeLength)
        {
            final int ch = CMap.toInt(bytes);
            if (from <= ch && ch <= to)
            {
                return unicode + (ch - from);
            }
        }
        return -1;
    }

    /**
     * Maps the given Unicode character to the corresponding CID in this range.
     *
     * @param code   Unicode character
     * @param length origin byte length of the code
     * @return corresponding CID, or -1 if the character is out of range
     */
    public int map(final int code, final int length)
    {
        if (length == codeLength && from <= code && code <= to)
        {
            return unicode + (code - from);
        }
        return -1;
    }

    /**
     * Maps the given CID to the corresponding Unicode character in this range.
     *
     * @param code CID
     * @return corresponding Unicode character, or -1 if the CID is out of range
     */
    public int unmap(final int code)
    {
        if (unicode <= code && code <= unicode + (to - from))
        {
            return from + (code - unicode);
        }
        return -1;
    }

    /**
     * Check if the given values represent a consecutive range of the given range. If so, extend the given range instead of
     * creating a new one.
     * 
     * @param newFrom start value of the new range
     * @param newTo   end value of the new range
     * @param newCid  start CID value of the range
     * @param length  byte length of CIDs
     * @return true if the given range was extended
     */
    public boolean extend(final int newFrom, final int newTo, final int newCid, final int length)
    {
        if (codeLength == length && (newFrom == to + 1) && (newCid == unicode + to - from + 1))
        {
            to = newTo;
            return true;
        }
        return false;
    }

}
