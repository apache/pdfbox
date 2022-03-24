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

package org.apache.pdfbox.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unittest for org.apache.pdfbox.io.SequenceRandomAccessRead
 * 
 */
class SequenceRandomAccessReadTest
{

    @Test
    void TestCreateAndRead() throws IOException
    {
        String input1 = "This is a test string number 1";
        RandomAccessReadBuffer randomAccessReadBuffer1 = new RandomAccessReadBuffer(
                input1.getBytes());
        String input2 = "This is a test string number 2";
        RandomAccessReadBuffer randomAccessReadBuffer2 = new RandomAccessReadBuffer(
                input2.getBytes());
        List<RandomAccessRead> inputList = Arrays.asList(randomAccessReadBuffer1,
                randomAccessReadBuffer2);
        try (SequenceRandomAccessRead sequenceRandomAccessRead = new SequenceRandomAccessRead(inputList))
        {
            assertThrows(UnsupportedOperationException.class, () -> sequenceRandomAccessRead.createView(0, 10));
            
            int overallLength = input1.length() + input2.length();
            assertEquals(overallLength, sequenceRandomAccessRead.length());
            
            byte[] bytesRead = new byte[overallLength];
            
            assertEquals(overallLength, sequenceRandomAccessRead.read(bytesRead));
            assertEquals(input1 + input2, new String(bytesRead));
        }

        // test missing parameter
        assertThrows(IllegalArgumentException.class, () -> new SequenceRandomAccessRead(null));

        // test empty list
        List<RandomAccessRead> emptyList = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> new SequenceRandomAccessRead(emptyList));

        // test problematic list
        assertThrows(IllegalArgumentException.class, () -> new SequenceRandomAccessRead(inputList));
    }

    @Test
    void TestSeekPeekAndRewind() throws IOException
    {
        String input1 = "01234567890123456789";
        RandomAccessReadBuffer randomAccessReadBuffer1 = new RandomAccessReadBuffer(
                input1.getBytes());
        String input2 = "abcdefghijklmnopqrst";
        RandomAccessReadBuffer randomAccessReadBuffer2 = new RandomAccessReadBuffer(
                input2.getBytes());
        List<RandomAccessRead> inputList = Arrays.asList(randomAccessReadBuffer1,
                randomAccessReadBuffer2);
        // test seek, rewind and peek in the first part of the sequence
        try (SequenceRandomAccessRead sequenceRandomAccessRead = new SequenceRandomAccessRead(inputList))
        {
            // test seek, rewind and peek in the first part of the sequence
            sequenceRandomAccessRead.seek(4);
            assertEquals(4, sequenceRandomAccessRead.getPosition());
            assertEquals('4', sequenceRandomAccessRead.read());
            assertEquals(5, sequenceRandomAccessRead.getPosition());
            sequenceRandomAccessRead.rewind(1);
            assertEquals(4, sequenceRandomAccessRead.getPosition());
            assertEquals('4', sequenceRandomAccessRead.read());
            assertEquals('5', sequenceRandomAccessRead.peek());
            assertEquals(5, sequenceRandomAccessRead.getPosition());
            assertEquals('5', sequenceRandomAccessRead.read());
            assertEquals(6, sequenceRandomAccessRead.getPosition());
            // test seek, rewind and peek in the second part of the sequence
            sequenceRandomAccessRead.seek(24);
            assertEquals(24, sequenceRandomAccessRead.getPosition());
            assertEquals('e', sequenceRandomAccessRead.read());
            sequenceRandomAccessRead.rewind(1);
            assertEquals('e', sequenceRandomAccessRead.read());
            assertEquals('f', sequenceRandomAccessRead.peek());
            assertEquals('f', sequenceRandomAccessRead.read());
            assertThrows(IOException.class, () -> sequenceRandomAccessRead.seek(-1));
        }
    }

    @Test
    void TestBorderCases() throws IOException
    {
        String input1 = "01234567890123456789";
        RandomAccessReadBuffer randomAccessReadBuffer1 = new RandomAccessReadBuffer(
                input1.getBytes());
        String input2 = "abcdefghijklmnopqrst";
        RandomAccessReadBuffer randomAccessReadBuffer2 = new RandomAccessReadBuffer(
                input2.getBytes());
        List<RandomAccessRead> inputList = Arrays.asList(randomAccessReadBuffer1,
                randomAccessReadBuffer2);
        // jump to the last byte of the first part of the sequence
        try (SequenceRandomAccessRead sequenceRandomAccessRead = new SequenceRandomAccessRead(inputList))
        {
            // jump to the last byte of the first part of the sequence
            sequenceRandomAccessRead.seek(19);
            assertEquals('9', sequenceRandomAccessRead.read());
            sequenceRandomAccessRead.rewind(1);
            assertEquals('9', sequenceRandomAccessRead.read());
            assertEquals('a', sequenceRandomAccessRead.peek());
            assertEquals('a', sequenceRandomAccessRead.read());
            
            // jump back to the first sequence
            sequenceRandomAccessRead.seek(17);
            byte[] bytesRead = new byte[6];
            assertEquals(6, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("789abc", new String(bytesRead));
            assertEquals(23, sequenceRandomAccessRead.getPosition());
            
            // rewind back to the first sequence
            sequenceRandomAccessRead.rewind(6);
            assertEquals(17, sequenceRandomAccessRead.getPosition());
            bytesRead = new byte[6];
            assertEquals(6, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("789abc", new String(bytesRead));
            
            // jump to the start of the sequence
            sequenceRandomAccessRead.seek(0);
            bytesRead = new byte[6];
            assertEquals(6, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("012345", new String(bytesRead));
        }
    }

    @Test
    void TestEOF() throws IOException
    {
        String input1 = "01234567890123456789";
        RandomAccessReadBuffer randomAccessReadBuffer1 = new RandomAccessReadBuffer(
                input1.getBytes());
        String input2 = "abcdefghijklmnopqrst";
        RandomAccessReadBuffer randomAccessReadBuffer2 = new RandomAccessReadBuffer(
                input2.getBytes());
        List<RandomAccessRead> inputList = Arrays.asList(randomAccessReadBuffer1,
                randomAccessReadBuffer2);
        SequenceRandomAccessRead sequenceRandomAccessRead = new SequenceRandomAccessRead(inputList);

        int overallLength = input1.length() + input2.length();

        sequenceRandomAccessRead.seek(overallLength - 1);
        assertFalse(sequenceRandomAccessRead.isEOF());
        assertEquals('t', sequenceRandomAccessRead.peek());
        assertFalse(sequenceRandomAccessRead.isEOF());
        assertEquals('t', sequenceRandomAccessRead.read());
        assertTrue(sequenceRandomAccessRead.isEOF());
        assertEquals(-1, sequenceRandomAccessRead.read());
        assertEquals(-1, sequenceRandomAccessRead.read(new byte[1], 0, 1));
        // rewind
        sequenceRandomAccessRead.rewind(5);
        assertFalse(sequenceRandomAccessRead.isEOF());
        byte[] bytesRead = new byte[5];
        assertEquals(5, sequenceRandomAccessRead.read(bytesRead));
        assertEquals("pqrst", new String(bytesRead));
        assertTrue(sequenceRandomAccessRead.isEOF());

        // seek to a position beyond the end of the input
        sequenceRandomAccessRead.seek(overallLength + 10);
        assertTrue(sequenceRandomAccessRead.isEOF());
        assertEquals(overallLength, sequenceRandomAccessRead.getPosition());

        assertFalse(sequenceRandomAccessRead.isClosed());
        sequenceRandomAccessRead.close();
        assertTrue(sequenceRandomAccessRead.isClosed());
        // closing a SequenceRandomAccessRead twice shouldn't be a problem
        sequenceRandomAccessRead.close();

        assertThrows(IOException.class, () -> sequenceRandomAccessRead.read(),
                "checkClosed should have thrown an IOException");
    }

    @Test
    void TestEmptyStream() throws IOException
    {
        String input1 = "01234567890123456789";
        RandomAccessReadBuffer randomAccessReadBuffer1 = new RandomAccessReadBuffer(
                input1.getBytes());
        String input2 = "abcdefghijklmnopqrst";
        RandomAccessReadBuffer randomAccessReadBuffer2 = new RandomAccessReadBuffer(
                input2.getBytes());
        RandomAccessReadBuffer emptyBuffer = new RandomAccessReadBuffer("".getBytes());

        List<RandomAccessRead> inputList = Arrays.asList(randomAccessReadBuffer1, emptyBuffer,
                randomAccessReadBuffer2);

        try (SequenceRandomAccessRead sequenceRandomAccessRead = new SequenceRandomAccessRead(inputList))
        {
            // check length
            assertEquals(sequenceRandomAccessRead.length(), input1.length() + input2.length());
            
            // read from both parts of the sequence
            byte[] bytesRead = new byte[10];
            sequenceRandomAccessRead.seek(15);
            assertEquals(10, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("56789abcde", new String(bytesRead));
            
            // rewind and read again
            sequenceRandomAccessRead.rewind(15);
            bytesRead = new byte[5];
            assertEquals(5, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("01234", new String(bytesRead));
            
            // check EOF when reading
            bytesRead = new byte[5];
            sequenceRandomAccessRead.seek(38);
            assertEquals(2, sequenceRandomAccessRead.read(bytesRead));
            assertEquals("st", new String(bytesRead, 0, 2));
            
            // check EOF after seek
            sequenceRandomAccessRead.seek(40);
            assertTrue(sequenceRandomAccessRead.isEOF());
        }
    }
}
