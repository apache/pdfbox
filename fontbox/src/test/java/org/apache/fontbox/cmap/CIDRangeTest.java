/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CIDRangeTest
{

    @Test
    void testCIDRangeOneByte()
    {
        CIDRange cidRange = new CIDRange(0, 20, 65, 1);
        assertEquals(1, cidRange.getCodeLength());

        assertEquals(65, cidRange.map(new byte[] { 0 }));
        assertEquals(75, cidRange.map(new byte[] { 10 }));
        // out of range
        assertEquals(-1, cidRange.map(new byte[] { 30 }));
        // wrong code length
        assertEquals(-1, cidRange.map(new byte[] { 0, 10 }));

        assertEquals(65, cidRange.map(0, 1));
        assertEquals(75, cidRange.map(10, 1));
        // out of range
        assertEquals(-1, cidRange.map(30, 1));
        // wrong code length
        assertEquals(-1, cidRange.map(10, 2));

        assertEquals(0, cidRange.unmap(65));
        assertEquals(10, cidRange.unmap(75));
        // out of range
        assertEquals(-1, cidRange.unmap(100));
    }

    @Test
    void testCIDRangeTwoByte()
    {
        CIDRange cidRange = new CIDRange(256, 280, 65, 2);
        assertEquals(2, cidRange.getCodeLength());

        assertEquals(65, cidRange.map(new byte[] { 1, 0 }));
        assertEquals(75, cidRange.map(new byte[] { 1, 10 }));
        // out of range
        assertEquals(-1, cidRange.map(new byte[] { 1, 30 }));
        // wrong code length
        assertEquals(-1, cidRange.map(new byte[] { 10 }));

        assertEquals(65, cidRange.map(256, 2));
        assertEquals(75, cidRange.map(266, 2));
        // out of range
        assertEquals(-1, cidRange.map(290, 2));
        // wrong code length
        assertEquals(-1, cidRange.map(256, 1));

        assertEquals(256, cidRange.unmap(65));
        assertEquals(266, cidRange.unmap(75));
        // out of range
        assertEquals(-1, cidRange.unmap(100));
    }

}
