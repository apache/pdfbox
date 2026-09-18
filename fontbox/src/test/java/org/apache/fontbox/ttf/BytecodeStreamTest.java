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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link BytecodeStream} cursor, including bounds checking.
 */
class BytecodeStreamTest
{
    @Test
    void testSequentialReads()
    {
        BytecodeStream s = new BytecodeStream(new byte[] { (byte) 0xB0, 0x05, (byte) 0xFF, 0x01 });
        assertTrue(s.hasNext());
        assertEquals(0xB0, s.nextByte());
        assertEquals(5, s.nextByte());
        // 0xFF01 as a signed word is negative
        assertEquals((short) 0xFF01, s.nextWord());
        assertFalse(s.hasNext());
    }

    @Test
    void testByteReadPastEndThrows()
    {
        BytecodeStream s = new BytecodeStream(new byte[] { 0x01 });
        assertEquals(1, s.nextByte());
        assertThrows(HintingException.class, s::nextByte);
    }

    @Test
    void testWordReadPastEndThrows()
    {
        // only one byte, but a word needs two
        BytecodeStream s = new BytecodeStream(new byte[] { 0x01 });
        assertThrows(HintingException.class, s::nextWord);
    }

    @Test
    void testSeekOutOfRangeThrows()
    {
        BytecodeStream s = new BytecodeStream(new byte[] { 0x01, 0x02 });
        s.seek(2); // end position is valid
        assertFalse(s.hasNext());
        assertThrows(HintingException.class, () -> s.seek(3));
        assertThrows(HintingException.class, () -> s.seek(-1));
    }

    @Test
    void testInstructionStartTracking()
    {
        BytecodeStream s = new BytecodeStream(new byte[] { 0x10, 0x11, 0x12 });
        s.nextByte();
        s.markInstructionStart();
        assertEquals(1, s.instructionStart());
        assertEquals(0x11, s.nextByte());
    }
}
