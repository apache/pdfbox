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

/* $Id$ */

package org.apache.pdfbox.io.ccitt;

import junit.framework.TestCase;

/**
 * This is a unit test for {@link PackedBitArray}.
 * @version $Revision$
 */
public class TestPackedBitArray extends TestCase
{

    /**
     * Tests the {@link PackedBitArray} class.
     */
    public void testPackedBitArray()
    {
        PackedBitArray bits = new PackedBitArray(19);
        assertEquals(19, bits.getBitCount());
        assertEquals(3, bits.getByteCount());
        assertEquals("0000000000000000000", bits.toString());

        bits.set(1);
        assertEquals("0100000000000000000", bits.toString());

        bits.clear(1);
        assertEquals("0000000000000000000", bits.toString());

        bits.setBits(4, 4);
        assertEquals("0000111100000000000", bits.toString());

        bits.setBits(2, 1);
        assertEquals("0010111100000000000", bits.toString());

        bits.setBits(9, 9, 1);
        assertEquals("0010111101111111110", bits.toString());

        bits.clearBits(15, 2);
        assertEquals("0010111101111110010", bits.toString());

        bits.setBits(9, 9, 0);
        assertEquals("0010111100000000000", bits.toString());

        bits.clear();
        assertEquals("0000000000000000000", bits.toString());

        bits.setBits(1, 18);
        assertEquals("0111111111111111111", bits.toString());

        bits.clearBits(3, 1);
        assertEquals("0110111111111111111", bits.toString());

        try
        {
            bits.setBits(1, 19);
            fail("Expecting IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            //good
        }
    }

}
