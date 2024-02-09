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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
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

        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais))
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

        Assertions.assertThrows(IOException.class, () -> randomAccessSource.seek(-1),
                "seek should have thrown an IOException");

        assertFalse(randomAccessSource.isEOF());
        randomAccessSource.seek(20);
        assertTrue(randomAccessSource.isEOF());
        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.read(new byte[1], 0, 1));

        randomAccessSource.close();
        Assertions.assertThrows(IOException.class, () -> randomAccessSource.read(),
                "checkClosed should have thrown an IOException");
    }

    @Test
    void testPositionReadBytes() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais))
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

        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais))
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

        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais))
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
    void testEmptyBuffer() throws IOException
    {
        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(
                new ByteArrayOutputStream().toByteArray()))
        {
            assertEquals(-1, randomAccessSource.read());
            assertEquals(-1, randomAccessSource.peek());
            byte[] readBytes = new byte[6];
            assertEquals(-1, randomAccessSource.read(readBytes));
            randomAccessSource.seek(0);
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.seek(6);
            assertEquals(0, randomAccessSource.getPosition());
            assertTrue(randomAccessSource.isEOF());
            Assertions.assertThrows(IOException.class, () -> randomAccessSource.rewind(3),
                    "seek should have thrown an IOException");
        }
    }

    @Test
    void testView() throws IOException
    {
        byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        try (RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(bais);
             RandomAccessReadView view = randomAccessSource.createView(3, 5))
        {
            assertEquals(0, view.getPosition());
            assertEquals(3, view.read());
            assertEquals(4, view.read());
            assertEquals(5, view.read());
            assertEquals(3, view.getPosition());
        }
    }

    @Test
    void testPDFBOX5111() throws IOException, URISyntaxException
    {
        try (InputStream is = new URI(
                "https://issues.apache.org/jira/secure/attachment/13017227/stringwidth.pdf")
                        .toURL().openStream();
             RandomAccessReadBuffer randomAccessSource = new RandomAccessReadBuffer(is))
        {
            assertEquals(34060, randomAccessSource.length());
        }
    }

    /**
     * PDFBOX-5158: endless loop reading a stream of a multiple of 4096 bytes from a
     * FileInputStream. Test does not fail with a ByteArrayInputStream, so we need to create a temp
     * file.
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
        try (RandomAccessRead rar = new RandomAccessReadBuffer(Files.newInputStream(path)))
        {
            assertEquals(0, rar.read());
        }
        Files.delete(path);
    }
    
    /**
     * PDFBOX-5161: failure to read bytes after reading a multiple of 4096. Construction source
     * must be an InputStream.
     *
     * @throws IOException
     */
    @Test
    void testPDFBOX5161() throws IOException
    {
        try (RandomAccessRead rar = new RandomAccessReadBuffer(new ByteArrayInputStream(new byte[4099])))
        {
            byte[] buf = new byte[4096];
            int bytesRead = rar.read(buf);
            assertEquals(4096, bytesRead);
            bytesRead = rar.read(buf, 0, 3);
            assertEquals(3, bytesRead);
        }
    }

    /**
     * PDFBOX-5764: constructor has to use the limit of the given buffer as chunksize instead of the capacity.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBOX5764() throws IOException
    {
        int bufferSize = 4096;
        int limit = 2048;
        ByteBuffer buffer = ByteBuffer.wrap(new byte[bufferSize]);
        buffer.limit(limit);
        try (RandomAccessRead rar = new RandomAccessReadBuffer(buffer))
        {
            byte[] buf = new byte[bufferSize];
            int bytesRead = rar.read(buf);
            assertEquals(limit, bytesRead);
        }
    }

}
