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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * This will test the CodeSpaceRange implementation.
 *
 */
class TestCodespaceRange
{

    /**
     * Check whether the code length calculation works.
     */
    @Test
    void testCodeLength()
    {
        byte[] startBytes1 = { 0x00 };
        byte[] endBytes1 = { 0x20 };
        CodespaceRange range1 = new CodespaceRange(startBytes1, endBytes1);
        assertEquals(1, range1.getCodeLength());

        byte[] startBytes2 = { 0x00, 0x00 };
        byte[] endBytes2 = { 0x01, 0x20 };
        CodespaceRange range2 = new CodespaceRange(startBytes2, endBytes2);
        assertEquals(2, range2.getCodeLength());
    }

    /**
     * Check whether the constructor checks the length of the start and end bytes
     */
    @Test
    void testConstructor()
    {
        // PDFBOX-4923 "1 begincodespacerange <00> <ffff> endcodespacerange" case is accepted
        byte[] startBytes1 = { 0x00 };
        byte[] endBytes2 = { -1, -1 };
        new CodespaceRange(startBytes1, endBytes2);

        // other cases of different lengths are not
        byte[] startBytes3 = { 0x01 };
        byte[] endBytes4 = { 0x01, 0x20 };
        try
        {
            new CodespaceRange(startBytes3, endBytes4);
            fail("The constructor should have thrown an IllegalArgumentException exception.");
        }
        catch (IllegalArgumentException exception)
        {
            // everything is fine as the expected exception is thrown
        }
    }

    @Test
    void testMatches()
    {
        byte[] startBytes1 = { 0x00 };
        byte[] endBytes1 = { (byte) 0xA0 };
        CodespaceRange range1 = new CodespaceRange(startBytes1, endBytes1);
        // check start and end value
        assertTrue(range1.matches(new byte[] { 0x00 }));
        assertTrue(range1.matches(new byte[] { (byte) 0xA0 }));
        // check any value within range
        assertTrue(range1.matches(new byte[] { 0x10 }));
        // check first value out of range
        assertFalse(range1.matches(new byte[] { (byte) 0xA1 }));
        // check any value out of range
        assertFalse(range1.matches(new byte[] { (byte) 0xD0 }));
        // check any value with a different code length
        assertFalse(range1.matches(new byte[] { 0x00, 0x10 }));

        byte[] startBytes2 = { (byte) 0x81, 0x40 };
        byte[] endBytes2 = { (byte) 0x9F, (byte) 0xFC };
        CodespaceRange range2 = new CodespaceRange(startBytes2, endBytes2);
        // check lower start and end value
        assertTrue(range2.matches(new byte[] { (byte) 0x81, 0x40 }));
        assertTrue(range2.matches(new byte[] { (byte) 0x81, (byte) 0xFC }));
        // check higher start and end value
        assertTrue(range2.matches(new byte[] { (byte) 0x81, 0x40 }));
        assertTrue(range2.matches(new byte[] { (byte) 0x9F, 0x40 }));
        // check any value within lower range
        assertTrue(range2.matches(new byte[] { (byte) 0x81, 0x65 }));
        // check any value within higher range
        assertTrue(range2.matches(new byte[] { (byte) 0x90, 0x40 }));
        // check first value out of lower range
        assertFalse(range2.matches(new byte[] { (byte) 0x81, (byte) 0xFD }));
        // check first value out of higher range
        assertFalse(range2.matches(new byte[] { (byte) 0xA0, 0x40 }));
        // check any value out of lower range
        assertFalse(range2.matches(new byte[] { (byte) 0x81, 0x20 }));
        // check any value out of higher range
        assertFalse(range2.matches(new byte[] { 0x10, 0x40 }));
        // check value between start and end but not within the rectangular
        assertFalse(range2.matches(new byte[] { (byte) 0x82, 0x20 }));
        // check any value with a different code length
        assertFalse(range2.matches(new byte[] { 0x00 }));
    }

}
