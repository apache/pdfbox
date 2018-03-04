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

    private final char from;

    private char to;

    private final int cid;

    CIDRange(char from, char to, int cid)
    {
        this.from = from;
        this.to = to;
        this.cid = cid;
    }

    /**
     * Maps the given Unicode character to the corresponding CID in this range.
     *
     * @param ch Unicode character
     * @return corresponding CID, or -1 if the character is out of range
     */
    public int map(char ch)
    {
        if (from <= ch && ch <= to)
        {
            return cid + (ch - from);
        }
        return -1;
    }

    /**
     * Maps the given CID to the corresponding Unicode character in this range.
     *
     * @param code CID
     * @return corresponding Unicode character, or -1 if the CID is out of range
     */
    public int unmap(int code)
    {
        if (cid <= code && code <= cid + (to - from))
        {
            return from + (code - cid);
        }
        return -1;
    }

    /**
     * Check if the given values represent a consecutive range of the given range. If so, extend the given range instead
     * of creating a new one.
     * 
     * @param newFrom start value of the new range
     * @param newTo end value of the new range
     * @param newCid start CID value of the range
     * @return true if the given range was extended
     */
    public boolean extend(char newFrom, char newTo, int newCid)
    {
        if ((newFrom == to + 1) && (newCid == cid + to - from + 1))
        {
            to = newTo;
            return true;
        }
        return false;
    }

}
