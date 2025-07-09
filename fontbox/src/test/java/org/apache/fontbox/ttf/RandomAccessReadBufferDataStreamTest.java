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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class RandomAccessReadBufferDataStreamTest
{
    @Test
    void testEOF() throws IOException
    {
        byte[] byteArray = new byte[10];
        RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(byteArray);
        try (RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(
                randomAccessReadBuffer))
        {
            int value = dataStream.read();
            while (value > -1)
            {
                value = dataStream.read();
            }
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            fail("EOF not detected!");
        }
    }

    @Test
    void testEOFUnsignedShort() throws IOException
    {
        byte[] byteArray = new byte[3];
        RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(byteArray);
        try(RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(
                randomAccessReadBuffer))
        {
            dataStream.readUnsignedShort();
            assertThrows(EOFException.class, dataStream::readUnsignedShort);
        }
    }

    @Test
    void testEOFUnsignedInt() throws IOException
    {
        byte[] byteArray = new byte[5];
        RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(byteArray);
        try (RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(
                randomAccessReadBuffer))
        {
            dataStream.readUnsignedInt();
            assertThrows(EOFException.class, dataStream::readUnsignedInt);
        }
    }

    @Test
    void testEOFUnsignedByte() throws IOException
    {
        byte[] byteArray = new byte[2];
        RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(byteArray);
        try (RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(
                randomAccessReadBuffer))
        {
            dataStream.readUnsignedByte();
            dataStream.readUnsignedByte();
            assertThrows(EOFException.class, dataStream::readUnsignedByte);
        }
    }
    /**
     * Test of PDFBOX-4242: make sure that the Closeable.close() contract is fulfilled.
     * 
     * @throws IOException
     */
    @Test
    void testDoubleClose() throws IOException
    {
        RandomAccessRead randomAccessRead = new RandomAccessReadBufferedFile(
                "src/test/resources/ttf/LiberationSans-Regular.ttf");
        RandomAccessReadDataStream randomAccessReadDataStream = new RandomAccessReadDataStream(
                randomAccessRead);
        randomAccessReadDataStream.close();
        assertDoesNotThrow(randomAccessReadDataStream::close);
    }

    /**
     * Before solving PDFBOX-3605, this test never ended.
     * 
     * @throws IOException
     */
    @Test
    void ensureReadFinishes() throws IOException
    {
        final File file = File.createTempFile("apache-pdfbox", ".dat");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            final String content = "1234567890";
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        final byte[] readBuffer = new byte[2];
        RandomAccessRead randomAccessRead = new RandomAccessReadBufferedFile(file);
        try (RandomAccessReadDataStream randomAccessReadDataStream = new RandomAccessReadDataStream(
                randomAccessRead))
        {
            int amountRead;
            int totalAmountRead = 0;
            while ((amountRead = randomAccessReadDataStream.read(readBuffer, 0, 2)) != -1)
            {
                totalAmountRead += amountRead;
            }
            assertEquals(10, totalAmountRead);
        }
        file.delete();
    }

    /**
     * Test several reading patterns, both reading within a buffer and across buffer.
     *
     * @throws IOException
     */
    @Test
    void testReadBuffer() throws IOException
    {
        final File file = File.createTempFile("apache-pdfbox", ".dat");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            final String content = "012345678A012345678B012345678C012345678D";
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        RandomAccessRead randomAccessRead = new RandomAccessReadBufferedFile(file);

        final byte[] readBuffer = new byte[40];
        try (RandomAccessReadDataStream randomAccessReadDataStream = new RandomAccessReadDataStream(
                randomAccessRead))
        {
            int count = 4;
            int bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(4, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("0123", new String(readBuffer, 0, count));

            count = 6;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(10, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("45678A", new String(readBuffer, 0, count));

            count = 10;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(20, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("012345678B", new String(readBuffer, 0, count));

            count = 10;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(30, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("012345678C", new String(readBuffer, 0, count));

            count = 10;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(40, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("012345678D", new String(readBuffer, 0, count));

            assertEquals(-1, randomAccessReadDataStream.read());

            randomAccessReadDataStream.seek(0);
            randomAccessReadDataStream.read(readBuffer, 0, 7);
            assertEquals(7, randomAccessReadDataStream.getCurrentPosition());

            count = 16;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(23, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("78A012345678B012", new String(readBuffer, 0, count));

            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, 99);
            assertEquals(40, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(17, bytesRead);
            assertEquals("345678C012345678D", new String(readBuffer, 0, 17));

            assertEquals(-1, randomAccessReadDataStream.read());

            randomAccessReadDataStream.seek(0);
            randomAccessReadDataStream.read(readBuffer, 0, 7);
            assertEquals(7, randomAccessReadDataStream.getCurrentPosition());

            count = 23;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(30, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("78A012345678B012345678C", new String(readBuffer, 0, count));

            randomAccessReadDataStream.seek(0);
            randomAccessReadDataStream.read(readBuffer, 0, 10);
            assertEquals(10, randomAccessReadDataStream.getCurrentPosition());
            count = 23;
            bytesRead = randomAccessReadDataStream.read(readBuffer, 0, count);
            assertEquals(33, randomAccessReadDataStream.getCurrentPosition());
            assertEquals(count, bytesRead);
            assertEquals("012345678B012345678C012", new String(readBuffer, 0, count));
        }
        file.delete();
    }

}
