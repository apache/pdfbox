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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Unittest for org.apache.pdfbox.io.RandomAccessReadWriteBuffer
 */
class RandomAccessReadWriteBufferTest
{

    private static final int NUM_ITERATIONS = 3;

    @Test
    void testClose() throws IOException
    {
        RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer();
        randomAccessReadWrite.write(new byte[] { 1, 2, 3, 4 });
        assertFalse(randomAccessReadWrite.isClosed());
        randomAccessReadWrite.close();
        assertTrue(randomAccessReadWrite.isClosed());
    }

    @Test
    void testClear() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer(4))
        {
            randomAccessReadWrite.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
            assertEquals(10, randomAccessReadWrite.length());
            assertEquals(10, randomAccessReadWrite.getPosition());
            randomAccessReadWrite.clear();
            assertFalse(randomAccessReadWrite.isClosed());
            assertEquals(0, randomAccessReadWrite.length());
            assertEquals(0, randomAccessReadWrite.getPosition());
        }
        catch (Throwable throwable)
        {
            fail("Unexpected exception " + throwable.getMessage());
        }
    }

    @Test
    void testLengthWriteByte() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            assertEquals(0, randomAccessReadWrite.length());
            randomAccessReadWrite.write(1);
            randomAccessReadWrite.write(2);
            randomAccessReadWrite.write(3);
            assertEquals(3, randomAccessReadWrite.length());
        }
        catch (Throwable throwable)
        {
            fail("Unexpected exception " + throwable.getMessage());
        }
    }

    @Test
    void testLengthWriteBytes() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            assertEquals(0, randomAccessReadWrite.length());
            randomAccessReadWrite.write(new byte[] { 1, 2, 3, 4, 5, 6, 7 });
            assertEquals(7, randomAccessReadWrite.length());
            randomAccessReadWrite.write(new byte[] { 8, 9, 10, 11, });
            assertEquals(11, randomAccessReadWrite.length());
        }
        catch (Throwable throwable)
        {
            fail("Unexpected exception " + throwable.getMessage());
        }
    }

    @Test
    void testPaging() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer(5))
        {
            assertEquals(0, randomAccessReadWrite.length());
            randomAccessReadWrite.write(new byte[] { 1, 2, 3, 4, 5, 6, 7 });
            assertEquals(7, randomAccessReadWrite.length());
            randomAccessReadWrite.write(new byte[] { 8, 9, 10, 11, });
            assertEquals(11, randomAccessReadWrite.length());
        }
        catch (Throwable throwable)
        {
            fail("Unexpected exception " + throwable.getMessage());
        }
    }

    @Test
    void testRandomAccessRead() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            randomAccessReadWrite.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
            assertEquals(11, randomAccessReadWrite.length());
            randomAccessReadWrite.seek(0);
            assertEquals(11, randomAccessReadWrite.length());
            byte[] bytesRead = new byte[11];
            assertEquals(11, randomAccessReadWrite.read(bytesRead));
            assertEquals(1, bytesRead[0]);
            assertEquals(7, bytesRead[6]);
            assertEquals(8, bytesRead[7]);
            assertEquals(11, bytesRead[10]);
        }
        catch (Throwable throwable)
        {
            fail("Unexpected exception " + throwable.getMessage());
        }
    }

    /**
     * PDFBOX-4756: test positions are correct when seeking and that no EOFException is thrown in
     * ScratchFileBuffer.seek() beyond last page.
     *
     * @throws IOException
     */
    @Test
    void testEOFBugInSeek() throws IOException
    {
        try (RandomAccess randomAccessRwedWrite = new RandomAccessReadWriteBuffer())
        {

            byte[] bytes = new byte[RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB];
            for (int i = 0; i < NUM_ITERATIONS; i++)
            {
                long p0 = randomAccessRwedWrite.getPosition();
                randomAccessRwedWrite.write(bytes);
                long p1 = randomAccessRwedWrite.getPosition();
                assertEquals(RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB, p1 - p0);
                randomAccessRwedWrite.write(bytes);
                long p2 = randomAccessRwedWrite.getPosition();
                assertEquals(RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB, p2 - p1);
                randomAccessRwedWrite.seek(0);
                randomAccessRwedWrite.seek(i * 2 * RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB);
            }
        }
    }

    @Test
    void testBufferLength() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            byte[] bytes = new byte[RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB];
            randomAccessReadWrite.write(bytes);
            assertEquals(RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB, randomAccessReadWrite.length());
        }
    }

    @Test
    void testBufferSeek() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            byte[] bytes = new byte[RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB];
            randomAccessReadWrite.write(bytes);
            assertThrows(IOException.class, () -> randomAccessReadWrite.seek(-1));
        }
    }

    @Test
    void testBufferEOF() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            byte[] bytes = new byte[RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB];
            randomAccessReadWrite.write(bytes);
            randomAccessReadWrite.seek(0);
            assertFalse(randomAccessReadWrite.isEOF());
            randomAccessReadWrite.seek(RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB);
            assertTrue(randomAccessReadWrite.isEOF());
        }
    }

    @Test
    void testAlreadyClose() throws IOException
    {
        try (RandomAccess randomAccessReadWrite = new RandomAccessReadWriteBuffer())
        {
            byte[] bytes = new byte[RandomAccessReadBuffer.DEFAULT_CHUNK_SIZE_4KB];
            randomAccessReadWrite.write(bytes);
            randomAccessReadWrite.close();
            assertThrows(IOException.class, () -> randomAccessReadWrite.seek(0));
        }
    }

}
