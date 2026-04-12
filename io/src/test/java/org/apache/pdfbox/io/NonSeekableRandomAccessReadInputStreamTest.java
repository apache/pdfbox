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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link org.apache.pdfbox.io.NonSeekableRandomAccessReadInputStream}
 */
class NonSeekableRandomAccessReadInputStreamTest
{
    @Test
    void testPositionSkip() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.skip(5);
            assertEquals(5, randomAccessSource.read());
            assertEquals(6, randomAccessSource.getPosition());
        }
    }

    @Test
    void testPositionRead() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais);

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

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
            Assertions.assertThrows(IOException.class, () -> randomAccessSource.seek(3),
                    "seek should have thrown an IOException");
        }
    }

    @Test
    void testPositionReadBytes() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
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
        }
    }

    @Test
    void testPositionPeek() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.skip(6);
            assertEquals(6, randomAccessSource.getPosition());

            assertEquals(6, randomAccessSource.peek());
            assertEquals(6, randomAccessSource.getPosition());
        }
    }

    @Test
    void testPositionUnreadBytes() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
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

            // PDFBOX-5965: check that it also works near EOF
            assertEquals(3, randomAccessSource.read());
            assertEquals(4, randomAccessSource.read());
            assertEquals(5, randomAccessSource.read());
            assertEquals(6, randomAccessSource.read());
            assertEquals(7, randomAccessSource.read());
            assertEquals(8, randomAccessSource.read());
            assertEquals(9, randomAccessSource.read());
            assertEquals(10, randomAccessSource.read());
            assertEquals(-1, randomAccessSource.read());
            assertTrue(randomAccessSource.isEOF());
            randomAccessSource.rewind(4);
            assertFalse(randomAccessSource.isEOF());
            assertEquals(7, randomAccessSource.read());
            assertEquals(8, randomAccessSource.read());
            assertEquals(9, randomAccessSource.read());
            assertEquals(10, randomAccessSource.read());
            assertEquals(-1, randomAccessSource.read());
        }
    }

    @Test
    void testView() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (NonSeekableRandomAccessReadInputStream randomAccessSource = new NonSeekableRandomAccessReadInputStream(
                bais))
        {
            Assertions.assertThrows(IOException.class, () -> randomAccessSource.createView(3, 5),
                    "createView should have thrown an IOException");
        }
    }

    @Test
    void testBufferSwitch() throws IOException
    {
        byte[] original = createRandomData();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(original);
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(
                byteArrayInputStream))
        {
            rar.skip(4098);
            assertEquals(4098, rar.getPosition());
            rar.rewind(4);
            assertEquals(4094, rar.getPosition());
            assertEquals(original[4094] & 0xFF, rar.read());
        }
    }

    @Test
    void testRewindException() throws IOException
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(createRandomData());
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(
                byteArrayInputStream))
        {
            rar.skip(10000);
            assertEquals(10000, rar.getPosition());
            rar.rewind(4096);
            assertEquals(5904, rar.getPosition());
            Assertions.assertThrows(IOException.class, () -> rar.rewind(4096),
                    "createView should have thrown an IOException");
        }
    }

    @Test
    void testRewindAcrossBuffers() throws IOException
    {
        byte[] ba = new byte[4096 + 5];
        int rewSize = 7;
        byte testVal = 123;
        ba[ba.length - rewSize] = testVal;
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(bais))
        {
            int len = rar.read(new byte[ba.length - rewSize]);
            assertEquals(ba.length - rewSize, len);
            len = rar.read(new byte[rewSize]);
            assertEquals(rewSize, len);
            int by = rar.read();
            assertEquals(-1, by);
            assertTrue(rar.isEOF());
            rar.rewind(len);
            by = rar.read(); // went ArrayIndexOutOfBoundsException here
            assertEquals(testVal, by);
        }
    }

    @Test
    void testRewindAcrossBuffers2() throws IOException
    {
        byte[] ba = new byte[4096 * 2];
        ba[4095] = 1;
        ba[4096] = 2;
        ba[4097] = 3;
        ba[4096 * 2 - 1] = 4;
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(bais))
        {
            assertEquals(4096 * 2, rar.length());
            int len = rar.read(new byte[4096 + 1]);
            assertEquals(4096 * 2, rar.length());
            assertEquals(4096 + 1, len);
            rar.rewind(2);
            assertEquals(1, rar.read());
            assertEquals(2, rar.read());
            assertEquals(3, rar.read());
            assertEquals(4096 * 2, rar.length());

            byte[] buf = new byte[4096];
            len = rar.read(buf);
            assertEquals(4096 - 2, len);
            assertEquals(4, buf[len-1]);
            assertEquals(-1, rar.read());
            assertEquals(-1, rar.read(new byte[1]));
        }
    }

    @Test
    void testAccessClosed() throws IOException
    {
        byte[] ba = new byte[1];
        ba[0] = 1;
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(bais);
        assertEquals(1, rar.read());
        assertEquals(-1, rar.read());
        rar.close();
        Assertions.assertThrows(IOException.class, rar::read,
                    "read() should have thrown an IOException");
    }

    /**
     * Verify that all methods which require an open stream throw IOException after close().
     */
    @Test
    void testClosedStreamMethods() throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais);
        rar.close();

        Assertions.assertThrows(IOException.class, rar::read,
                "read() on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, () -> rar.read(new byte[1], 0, 1),
                "read(byte[], int, int) on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, () -> rar.readFully(new byte[1], 0, 1),
                "readFully() on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, rar::getPosition,
                "getPosition() on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, rar::available,
                "available() on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, rar::length,
                "length() on closed stream should throw IOException");
        Assertions.assertThrows(IOException.class, rar::isEOF,
                "isEOF() on closed stream should throw IOException");
    }

    /**
     * Verify parameter validation in read(byte[], int, int) as required by the InputStream contract.
     */
    @Test
    void testReadBytesParameterValidation() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            // null buffer must throw NullPointerException
            Assertions.assertThrows(NullPointerException.class, () -> rar.read(null, 0, 1),
                    "null buffer should throw NullPointerException");

            byte[] buf = new byte[4];

            // negative offset must throw IndexOutOfBoundsException
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> rar.read(buf, -1, 2),
                    "negative offset should throw IndexOutOfBoundsException");

            // negative length must throw IndexOutOfBoundsException
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> rar.read(buf, 0, -1),
                    "negative length should throw IndexOutOfBoundsException");

            // offset + length beyond buffer end must throw IndexOutOfBoundsException
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> rar.read(buf, 2, 4),
                    "offset + length > buf.length should throw IndexOutOfBoundsException");

            // length == 0 must return 0 immediately without advancing position
            assertEquals(0, rar.read(buf, 0, 0));
            assertEquals(0, rar.getPosition());
        }
    }

    /**
     * Verify that readFully() reads exactly the requested number of bytes across a buffer boundary.
     */
    @Test
    void testReadFully() throws IOException
    {
        byte[] inputValues = new byte[10];
        for (int i = 0; i < inputValues.length; i++)
        {
            inputValues[i] = (byte) i;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            byte[] buf = new byte[10];
            rar.readFully(buf, 0, 10);
            for (int i = 0; i < 10; i++)
            {
                assertEquals(i, buf[i]);
            }
            assertEquals(10, rar.getPosition());
        }
    }

    /**
     * Verify that readFully() throws EOFException when the stream ends before the requested
     * number of bytes are available.
     */
    @Test
    void testReadFullyEOF() throws IOException
    {
        byte[] inputValues = { 0, 1, 2 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            Assertions.assertThrows(EOFException.class, () -> rar.readFully(new byte[10], 0, 10),
                    "readFully() should throw EOFException when stream ends before length bytes");
        }
    }

    /**
     * Verify that skip() silently stops at EOF without throwing an exception.
     */
    @Test
    void testSkipPastEOF() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            // skipping far beyond the end of the stream should not throw
            rar.skip(100);
            assertEquals(5, rar.getPosition());
            assertTrue(rar.isEOF());
        }
    }

    /**
     * Verify that available() accounts for bytes buffered internally as well as bytes remaining
     * in the underlying stream, and returns 0 at EOF.
     */
    @Test
    void testAvailable() throws IOException
    {
        byte[] inputValues = new byte[10];
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            // before any read, available() reflects is.available() since nothing is buffered yet
            assertEquals(10, rar.available());

            // read one byte: the fetch pulls all 10 bytes into the internal buffer,
            // so available = 9 buffered + 0 remaining in the underlying stream
            rar.read();
            assertEquals(9, rar.available());

            // consume all remaining bytes
            while (rar.read() != -1) {}
            assertEquals(0, rar.available());
        }
    }

    /**
     * Verify that length() returns the exact total after the stream is fully consumed,
     * at which point size holds the true count and is.available() is 0.
     */
    @Test
    void testLengthAfterFullConsumption() throws IOException
    {
        byte[] inputValues = new byte[100];
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);
        try (NonSeekableRandomAccessReadInputStream rar =
                new NonSeekableRandomAccessReadInputStream(bais))
        {
            while (rar.read() != -1) {}
            assertTrue(rar.isEOF());
            assertEquals(100, rar.length());
        }
    }

    private byte[] createRandomData()
    {
        final long seed = new Random().nextLong();
        final Random random = new Random(seed);
        final int numBytes = 10000 + random.nextInt(20000);
        byte[] original = new byte[numBytes];

        int upto = 0;
        while (upto < numBytes)
        {
            final int left = numBytes - upto;
            if (random.nextBoolean() || left < 2)
            {
                // Fill w/ pseudo-random bytes:
                final int end = upto + Math.min(left, 10 + random.nextInt(100));
                while (upto < end)
                {
                    original[upto++] = (byte) random.nextInt();
                }
            }
            else
            {
                // Fill w/ very predictable bytes:
                final int end = upto + Math.min(left, 2 + random.nextInt(10));
                final byte value = (byte) random.nextInt(4);
                while (upto < end)
                {
                    original[upto++] = value;
                }
            }
        }
        return original;
    }

    /**
     * PDFBOX-5158: endless loop reading a stream of a multiple of 4096 bytes from a FileInputStream. Test does not fail
     * with a ByteArrayInputStream, so we need to create a temp file.
     *
     * @throws IOException
     */
    @Test
    void testPDFBOX5158() throws IOException
    {
        Path path = Files.createTempFile("len4096", ".pdf");
        try (OutputStream os = Files.newOutputStream(path))
        {
            os.write(new byte[4096]);
        }
        assertEquals(4096, path.toFile().length());
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(
                Files.newInputStream(path)))
        {
            assertEquals(0, rar.read());
        }
        Files.delete(path);
    }

    /**
     * PDFBOX-5161: failure to read bytes after reading a multiple of 4096. Construction source must be an InputStream.
     *
     * @throws IOException
     */
    @Test
    void testPDFBOX5161() throws IOException
    {
        try (RandomAccessRead rar = new NonSeekableRandomAccessReadInputStream(
                new ByteArrayInputStream(new byte[4099])))
        {
            byte[] buf = new byte[4096];
            int bytesRead = rar.read(buf);
            assertEquals(4096, bytesRead);
            bytesRead = rar.read(buf, 0, 3);
            assertEquals(3, bytesRead);
        }
    }

}
