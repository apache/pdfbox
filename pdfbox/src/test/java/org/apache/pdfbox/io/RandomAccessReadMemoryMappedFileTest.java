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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link org.apache.pdfbox.io.RandomAccessReadMemoryMappedFile}
 */
class RandomAccessReadMemoryMappedFileTest
{
    @Test
    void testPositionSkip() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.skip(5);
        assertEquals('5', randomAccessSource.read());
        assertEquals(6, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testPositionRead() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
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
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        randomAccessSource.seek(3);
        assertEquals(3, randomAccessSource.getPosition());
        
        try
        {
            randomAccessSource.seek(-1);
            fail("seek should have thrown an IOException");
        }
        catch (final IOException e)
        {
            
        }
        
        assertFalse(randomAccessSource.isEOF());
        randomAccessSource.seek(randomAccessSource.length());
        assertTrue(randomAccessSource.isEOF());
        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.read(new byte[1], 0, 1));

        randomAccessSource.close();
        try
        {
            randomAccessSource.read();
            fail("checkClosed should have thrown an IOException");
        }
        catch (final IOException e)
        {

        }
    }

    @Test
    void testPositionReadBytes() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        assertEquals(0, randomAccessSource.getPosition());
        final byte[] buffer = new byte[4];
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

        randomAccessSource.close();
    }

    @Test
    void testPositionPeek() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.skip(6);
        assertEquals(6, randomAccessSource.getPosition());

        assertEquals('6', randomAccessSource.peek());
        assertEquals(6, randomAccessSource.getPosition());

        randomAccessSource.close();
    }

    @Test
    void testPositionUnreadBytes() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        assertEquals(0, randomAccessSource.getPosition());
        randomAccessSource.read();
        randomAccessSource.read();
        final byte[] readBytes = new byte[6];
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

        randomAccessSource.close();
    }

    @Test
    void testEmptyBuffer() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadEmptyFile.txt").toURI()));

        assertEquals(-1, randomAccessSource.read());
        assertEquals(-1, randomAccessSource.peek());
        final byte[] readBytes = new byte[6];
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
        catch (final IOException e)
        {

        }

        randomAccessSource.close();
    }

    @Test
    void testUnmapping() throws IOException
    {
        // This is a special test case for some unmapping issues limited to windows enviroments
        // see https://bugs.openjdk.java.net/browse/JDK-4724038
        final Path tempFile = Files.createTempFile("PDFBOX", "txt");
        final BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, StandardOpenOption.WRITE);
        bufferedWriter.write("Apache PDFBox test");
        bufferedWriter.close();

        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                tempFile.toFile());
        assertEquals(65, randomAccessSource.read());
        randomAccessSource.close();

        Files.delete(tempFile);
    }

    @Test
    void testView() throws IOException, URISyntaxException
    {
        final RandomAccessRead randomAccessSource = new RandomAccessReadMemoryMappedFile(
                new File(getClass().getResource("RandomAccessReadFile1.txt").toURI()));

        final RandomAccessReadView view = randomAccessSource.createView(3, 10);
        assertEquals(0, view.getPosition());
        assertEquals('3', view.read());
        assertEquals('4', view.read());
        assertEquals('5', view.read());
        assertEquals(3, view.getPosition());

        view.close();
        randomAccessSource.close();
    }
}
