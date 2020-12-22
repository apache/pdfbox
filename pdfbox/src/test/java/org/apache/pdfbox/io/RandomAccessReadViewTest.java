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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link org.apache.pdfbox.io.RandomAccessReadView}
 */
class RandomAccessReadViewTest
{
    @Test
    void testPositionSkip() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        assertEquals(0, randomAccessReadView.getPosition());
        assertEquals(10, randomAccessReadView.peek());
        randomAccessReadView.skip(5);
        assertEquals(5, randomAccessReadView.getPosition());
        assertEquals(15, randomAccessReadView.peek());

        randomAccessReadView.close();
        randomAccessSource.close();
    }

    @Test
    void testPositionRead() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        assertEquals(0, randomAccessReadView.getPosition());
        assertEquals(10, randomAccessReadView.read());
        assertEquals(11, randomAccessReadView.read());
        assertEquals(12, randomAccessReadView.read());
        assertEquals(3, randomAccessReadView.getPosition());

        assertFalse(randomAccessReadView.isClosed());
        randomAccessReadView.close();
        assertTrue(randomAccessReadView.isClosed());

        randomAccessSource.close();
    }

    @Test
    void testSeekEOF() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        randomAccessReadView.seek(3);
        assertEquals(3, randomAccessReadView.getPosition());

        try
        {
            randomAccessReadView.seek(-1);
            fail("seek should have thrown an IOException");
        }
        catch (final IOException e)
        {

        }

        assertFalse(randomAccessReadView.isEOF());
        randomAccessReadView.seek(20);
        assertTrue(randomAccessReadView.isEOF());
        assertEquals(-1, randomAccessReadView.read());
        assertEquals(-1, randomAccessReadView.read(new byte[1], 0, 1));

        randomAccessReadView.close();
        randomAccessSource.close();
        try
        {
            randomAccessReadView.read();
            fail("checkClosed should have thrown an IOException");
        }
        catch (final IOException e)
        {

        }
    }

    @Test
    void testPositionReadBytes() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        assertEquals(0, randomAccessReadView.getPosition());
        final byte[] buffer = new byte[4];
        randomAccessReadView.read(buffer);
        assertEquals(10, buffer[0]);
        assertEquals(13, buffer[3]);
        assertEquals(4, randomAccessReadView.getPosition());

        randomAccessReadView.read(buffer, 1, 2);
        assertEquals(10, buffer[0]);
        assertEquals(14, buffer[1]);
        assertEquals(15, buffer[2]);
        assertEquals(13, buffer[3]);
        assertEquals(6, randomAccessReadView.getPosition());

        randomAccessReadView.close();
        randomAccessSource.close();
    }

    @Test
    void testPositionPeek() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        assertEquals(0, randomAccessReadView.getPosition());
        randomAccessReadView.skip(6);
        assertEquals(6, randomAccessReadView.getPosition());

        assertEquals(16, randomAccessReadView.peek());
        assertEquals(6, randomAccessReadView.getPosition());

        randomAccessReadView.close();
        randomAccessSource.close();
    }

    @Test
    void testPositionUnreadBytes() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);

        assertEquals(0, randomAccessReadView.getPosition());
        randomAccessReadView.read();
        randomAccessReadView.read();
        final byte[] readBytes = new byte[6];
        assertEquals(readBytes.length, randomAccessReadView.read(readBytes));
        assertEquals(8, randomAccessReadView.getPosition());
        randomAccessReadView.rewind(readBytes.length);
        assertEquals(2, randomAccessReadView.getPosition());
        assertEquals(12, randomAccessReadView.read());
        assertEquals(3, randomAccessReadView.getPosition());
        randomAccessReadView.read(readBytes, 2, 4);
        assertEquals(12, readBytes[0]);
        assertEquals(13, readBytes[2]);
        assertEquals(16, readBytes[5]);
        assertEquals(7, randomAccessReadView.getPosition());
        randomAccessReadView.rewind(4);
        assertEquals(3, randomAccessReadView.getPosition());

        randomAccessReadView.close();
        randomAccessSource.close();
    }

    @Test
    void testCreateView() throws IOException
    {
        final byte[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20 };
        final RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayInputStream(values));
        final RandomAccessReadView randomAccessReadView = new RandomAccessReadView(randomAccessSource, 10,
                20);
        try
        {
            randomAccessReadView.createView(0, 20);
            fail("CreateView() should have throw an IOException");
        }
        catch (final IOException exception)
        {

        }
        finally
        {
            randomAccessReadView.close();
            randomAccessSource.close();
        }
    }
}
