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

import java.io.EOFException;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link org.apache.pdfbox.io.RandomAccessReadBufferedFile}
 */
class RandomAccessReadBufferedFileTest
{
    @Test
    void testPositionSkip() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.skip(5);
            assertEquals('5', randomAccessSource.read());
            assertEquals(6, randomAccessSource.getPosition());
        }
    }

    @Test
    void testPositionRead() throws IOException, URISyntaxException
    {
        RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        assertEquals(0, randomAccessSource.getPosition());
        assertEquals('0', randomAccessSource.read());
        assertEquals('1', randomAccessSource.read());
        assertEquals('2', randomAccessSource.read());
        assertEquals(3, randomAccessSource.getPosition());

        assertFalse(randomAccessSource.isClosed());
        randomAccessSource.close();
        assertTrue(randomAccessSource.isClosed());
    }

    @Test
    void testSeekEOF() throws IOException, URISyntaxException
    {
        RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        randomAccessSource.seek(3);
        assertEquals(3, randomAccessSource.getPosition());

        Assertions.assertThrows(IOException.class, () -> randomAccessSource.seek(-1),
                "seek should have thrown an IOException");

        assertFalse(randomAccessSource.isEOF());
        randomAccessSource.seek(randomAccessSource.length());
        assertTrue(randomAccessSource.isEOF());
        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.read(new byte[1], 0, 1));

        randomAccessSource.close();
        Assertions.assertThrows(IOException.class, randomAccessSource::read,
                "checkClosed should have thrown an IOException");
    }

    @Test
    void testPositionReadBytes() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(0, randomAccessSource.getPosition());
            byte[] buffer = new byte[4];
            randomAccessSource.read(buffer);
            assertEquals('0', buffer[0]);
            assertEquals('3', buffer[3]);
            assertEquals(4, randomAccessSource.getPosition());
            
            randomAccessSource.read(buffer, 1, 2);
            assertEquals('0', buffer[0]);
            assertEquals('4', buffer[1]);
            assertEquals('5', buffer[2]);
            assertEquals('3', buffer[3]);
            assertEquals(6, randomAccessSource.getPosition());
        }
    }

    @Test
    void testPositionPeek() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.skip(6);
            assertEquals(6, randomAccessSource.getPosition());
            
            assertEquals('6', randomAccessSource.peek());
            assertEquals(6, randomAccessSource.getPosition());
        }
    }

    @Test
    void testPathConstructor() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                Paths.get(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(130, randomAccessSource.length());
        }
    }

    @Test
    void testPositionUnreadBytes() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(0, randomAccessSource.getPosition());
            randomAccessSource.read();
            randomAccessSource.read();
            byte[] readBytes = new byte[6];
            assertEquals(readBytes.length, randomAccessSource.read(readBytes));
            assertEquals(8, randomAccessSource.getPosition());
            randomAccessSource.rewind(readBytes.length);
            assertEquals(2, randomAccessSource.getPosition());
            assertEquals('2', randomAccessSource.read());
            assertEquals(3, randomAccessSource.getPosition());
            randomAccessSource.read(readBytes, 2, 4);
            assertEquals(7, randomAccessSource.getPosition());
            randomAccessSource.rewind(4);
            assertEquals(3, randomAccessSource.getPosition());
        }
    }

    @Test
    void testEmptyBuffer() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadEmptyFile.txt").toURI())))
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
    void testView() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));
             RandomAccessReadView view = randomAccessSource.createView(3, 10))
        {
            assertEquals(0, view.getPosition());
            assertEquals('3', view.read());
            assertEquals('4', view.read());
            assertEquals('5', view.read());
            assertEquals(3, view.getPosition());
        }
    }

    @Test
    void testReadFully1() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            byte[] b = new byte[10];
            randomAccessSource.seek(1);
            randomAccessSource.readFully(b);
            String s = new String(b, StandardCharsets.US_ASCII);
            assertEquals("1234567890", s);
        }
    }

    @Test
    void testReadFully2() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            byte[] b = new byte[10];
            randomAccessSource.readFully(b, 2, 8);
            String s = new String(b, 2, 8, StandardCharsets.US_ASCII);
            assertEquals("01234567", s);
            assertEquals(0, b[0]);
            assertEquals(0, b[1]);
        }
    }

    @Test
    void testReadFully3() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            byte[] b = new byte[10];
            randomAccessSource.seek(randomAccessSource.length() - b.length);
            randomAccessSource.readFully(b);
            String s = new String(b, StandardCharsets.US_ASCII);
            assertEquals("0123456789", s);
        }
    }

    @Test
    void testReadFullyEOF() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            byte[] b = new byte[10];
            randomAccessSource.seek(randomAccessSource.length() - b.length + 1);
            Assertions.assertThrows(EOFException.class, () -> randomAccessSource.readFully(b));
        }
    }

    @Test
    void testReadFullyExact() throws IOException, URISyntaxException
    {
        Path path = Paths.get(getClass().getResource("RandomAccessReadFile1.txt").toURI());
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(path))
        {
            int length = (int) randomAccessSource.length();
            byte[] b = new byte[length];
            randomAccessSource.readFully(b);
            byte[] allBytes = Files.readAllBytes(path);
            Assertions.assertArrayEquals(allBytes, b);
        }
    }

    @Test
    void testReadFullyAcrossBuffers() throws IOException
    {
        int bufferLen;
        File file = new File("src/test/java/org/apache/pdfbox/io/NonSeekableRandomAccessReadInputStreamTest.java");
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(file))
        {
            int length = (int) randomAccessSource.length();
            byte[] b = new byte[length];
            bufferLen = randomAccessSource.read(b);
            // make sure that the double buffer size is smaller than the length
            // if this test fails, we'll need a larger file
            assertTrue(bufferLen * 2 < length);
        }
        byte[] expectedBytes;
        try (InputStream is = new FileInputStream(file))
        {
            long skipped = is.skip(bufferLen / 2);
            assertEquals(skipped, bufferLen / 2);
            expectedBytes = new byte[bufferLen * 2];
            int actualRead = is.read(expectedBytes, 1, expectedBytes.length - 1);
            assertEquals(expectedBytes.length - 1, actualRead);
        }
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(file))
        {
            randomAccessSource.seek(bufferLen / 2);
            byte[] b = new byte[bufferLen * 2];
            randomAccessSource.readFully(b, 1, b.length - 1);
            Assertions.assertArrayEquals(expectedBytes, b);
        }
    }

    @Test
    void testReadFullyNothing() throws IOException, URISyntaxException
    {
        try (RandomAccessRead randomAccessSource = new RandomAccessReadBufferedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI())))
        {
            assertEquals(0, randomAccessSource.getPosition());
            byte[] b = new byte[0];
            randomAccessSource.readFully(b);
            assertEquals(0, randomAccessSource.getPosition());
        }
    }
}
