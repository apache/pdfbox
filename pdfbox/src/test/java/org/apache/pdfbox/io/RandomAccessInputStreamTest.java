/*
 * Copyright 2014 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link org.apache.pdfbox.io.RandomAccessInputStream}
 */
class RandomAccessInputStreamTest
{
    @Test
    void testPositionSkip() throws IOException
    {
        final byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        final ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        final RandomAccessInputStream randomAccessInputStream = new RandomAccessInputStream(
                new RandomAccessReadBuffer(bais));

        assertEquals(11, randomAccessInputStream.available());
        randomAccessInputStream.skip(5);
        assertEquals(5, randomAccessInputStream.read());
        assertEquals(5, randomAccessInputStream.available());
        assertEquals(0, randomAccessInputStream.skip(-10));

        randomAccessInputStream.close();
    }

    @Test
    void testPositionRead() throws IOException
    {
        final byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        final ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        final RandomAccessInputStream randomAccessInputStream = new RandomAccessInputStream(
                new RandomAccessReadBuffer(bais));

        assertEquals(11, randomAccessInputStream.available());
        assertEquals(0, randomAccessInputStream.read());
        assertEquals(1, randomAccessInputStream.read());
        assertEquals(2, randomAccessInputStream.read());
        assertEquals(8, randomAccessInputStream.available());

        randomAccessInputStream.close();
    }

    @Test
    void testSeekEOF() throws IOException
    {
        final byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        final ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        final RandomAccessInputStream randomAccessInputStream = new RandomAccessInputStream(
                new RandomAccessReadBuffer(bais));

        assertEquals(12, randomAccessInputStream.skip(inputValues.length + 1));
        
        assertEquals(0, randomAccessInputStream.available());
        assertEquals(-1, randomAccessInputStream.read());
        assertEquals(-1, randomAccessInputStream.read(new byte[1], 0, 1));

        randomAccessInputStream.close();
    }

    @Test
    void testPositionReadBytes() throws IOException
    {
        final byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        final ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        final RandomAccessInputStream randomAccessInputStream = new RandomAccessInputStream(
                new RandomAccessReadBuffer(bais));

        assertEquals(11, randomAccessInputStream.available());
        final byte[] buffer = new byte[4];
        randomAccessInputStream.read(buffer);
        assertEquals(0, buffer[0]);
        assertEquals(3, buffer[3]);
        assertEquals(7, randomAccessInputStream.available());

        randomAccessInputStream.read(buffer, 1, 2);
        assertEquals(0, buffer[0]);
        assertEquals(4, buffer[1]);
        assertEquals(5, buffer[2]);
        assertEquals(3, buffer[3]);
        assertEquals(5, randomAccessInputStream.available());

        randomAccessInputStream.close();
    }

    @Test
    void testEmptyBuffer() throws IOException
    {
        final RandomAccessInputStream randomAccessInputStream = new RandomAccessInputStream(
                new RandomAccessReadBuffer(new ByteArrayOutputStream().toByteArray()));

        assertEquals(-1, randomAccessInputStream.read());
        assertEquals(-1, randomAccessInputStream.read(new byte[6]));
        assertEquals(0, randomAccessInputStream.available());
        randomAccessInputStream.close();
    }
}
