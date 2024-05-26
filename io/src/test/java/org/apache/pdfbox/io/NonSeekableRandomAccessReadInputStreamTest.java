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
