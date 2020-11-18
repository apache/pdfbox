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

package org.apache.pdfbox.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Unittest for org.apache.pdfbox.io.RandomAccessReadBuffer
 */
class RandomAccessReadBufferTest
{
    @Test
    void testPositionSkip() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.skip(5);
        assertEquals(5, randomAccessSource.read());
        assertEquals(6, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testPositionRead() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        assertEquals(0, randomAccessSource.getPosition());
        assertEquals(0, randomAccessSource.read());
        assertEquals(1, randomAccessSource.read());
        assertEquals(2, randomAccessSource.read());
        assertEquals(3, randomAccessSource.getPosition());

        assertFalse(randomAccessSource.isClosed());
        randomAccessSource.close();
        assertTrue(randomAccessSource.isClosed());
    }

    @Test
    void testSeekEOF() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        randomAccessSource.seek(3);
        assertEquals(3, randomAccessSource.getPosition());
        
        try
        {
            randomAccessSource.seek(-1);
            fail("seek should have thrown an IOException");
        }
        catch (IOException e)
        {
            
        }
        
        assertFalse(randomAccessSource.isEOF());
        randomAccessSource.seek(20);
        assertTrue(randomAccessSource.isEOF());
        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.read(new byte[1], 0, 1));

        randomAccessSource.close();
        try
        {
            randomAccessSource.read();
            fail("checkClosed should have thrown an IOException");
        }
        catch (IOException e)
        {

        }
    }

    @Test
    void testPositionReadBytes() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        assertEquals(0, randomAccessSource.getPosition());
        byte[] buffer = new byte[4];
        randomAccessSource.read(buffer);
        assertEquals(0, buffer[0]);
        assertEquals(3, buffer[3]);
        assertEquals(4, randomAccessSource.getPosition());

        randomAccessSource.read(buffer, 1, 2);
        assertEquals(0, buffer[0]);
        assertEquals(4, buffer[1]);
        assertEquals(5, buffer[2]);
        assertEquals(3, buffer[3]);
        assertEquals(6, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testPositionPeek() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.skip(6);
        assertEquals(6, randomAccessSource.getPosition());

        assertEquals(6, randomAccessSource.peek());
        assertEquals(6, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testPositionUnreadBytes() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.read();
        randomAccessSource.read();
        byte[] readBytes = new byte[6];
        assertEquals(readBytes.length, randomAccessSource.read(readBytes));
        assertEquals(8, randomAccessSource.getPosition());
        randomAccessSource.rewind(readBytes.length);
        assertEquals(2, randomAccessSource.getPosition());
        assertEquals(2, randomAccessSource.read());
        assertEquals(3, randomAccessSource.getPosition());
        randomAccessSource.read(readBytes, 2, 4);
        assertEquals(7, randomAccessSource.getPosition());
        randomAccessSource.rewind(4);
        assertEquals(3, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testEmptyBuffer() throws IOException
    {
        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayOutputStream().toByteArray());

        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.peek());
        byte[] readBytes = new byte[6];
        assertEquals(-1, randomAccessSource.read(readBytes));
        randomAccessSource.seek(0);
        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.seek(6);
        assertEquals(0, randomAccessSource.getPosition());
        assertTrue(randomAccessSource.isEOF());

        try
        {
            randomAccessSource.rewind(3);
            fail("seek should have thrown an IOException");
        }
        catch (IOException e)
        {

        }

        randomAccessSource.close();
    }

    @Test
    void testView() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);

        RandomAccessReadView view = randomAccessSource.createView(3, 5);
        assertEquals(0, view.getPosition());
        assertEquals(3, view.read());
        assertEquals(4, view.read());
        assertEquals(5, view.read());
        assertEquals(3, view.getPosition());

        view.close();
        randomAccessSource.close();
    }

}
