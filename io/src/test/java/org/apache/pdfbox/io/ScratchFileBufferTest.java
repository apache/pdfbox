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

import java.io.EOFException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Regression test to check the known bugs in {@link ScratchFileBuffer}.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
class ScratchFileBufferTest
{

    private static final int PAGE_SIZE = 4096;
    private static final int NUM_ITERATIONS = 3;

    /**
     * PDFBOX-4756: test positions are correct when seeking and that no EOFException is thrown in
     * ScratchFileBuffer.seek() beyond last page.
     *
     * @throws IOException
     */
    @Test
    void testEOFBugInSeek() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            RandomAccess scratchFileBuffer = scratchFile.createBuffer();
            byte[] bytes = new byte[PAGE_SIZE];
            for (int i = 0; i < NUM_ITERATIONS; i++)
            {
                long p0 = scratchFileBuffer.getPosition();
                scratchFileBuffer.write(bytes);
                long p1 = scratchFileBuffer.getPosition();
                assertEquals(PAGE_SIZE, p1 - p0);
                scratchFileBuffer.write(bytes);
                long p2 = scratchFileBuffer.getPosition();
                assertEquals(PAGE_SIZE, p2 - p1);
                scratchFileBuffer.seek(0);
                scratchFileBuffer.seek(i * 2 * PAGE_SIZE);
            }
        }
    }

    @Test
    void testBufferLength() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            byte[] bytes = new byte[PAGE_SIZE];
            RandomAccess scratchFileBuffer1 = scratchFile.createBuffer();
            scratchFileBuffer1.write(bytes);
            assertEquals(PAGE_SIZE, scratchFileBuffer1.length());
        }
    }

    @Test
    void testBufferSeek() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            byte[] bytes = new byte[PAGE_SIZE];
            RandomAccess scratchFileBuffer1 = scratchFile.createBuffer();
            scratchFileBuffer1.write(bytes);
            assertThrows(IOException.class, () -> scratchFileBuffer1.seek(-1));
            assertThrows(EOFException.class, () -> scratchFileBuffer1.seek(PAGE_SIZE + 1));
        }
    }

    @Test
    void testBufferEOF() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            byte[] bytes = new byte[PAGE_SIZE];
            RandomAccess scratchFileBuffer1 = scratchFile.createBuffer();
            scratchFileBuffer1.write(bytes);
            scratchFileBuffer1.seek(0);
            assertFalse(scratchFileBuffer1.isEOF());
            scratchFileBuffer1.seek(PAGE_SIZE);
            assertTrue(scratchFileBuffer1.isEOF());
        }
    }

    @Test
    void testAlreadyClose() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            byte[] bytes = new byte[PAGE_SIZE];
            RandomAccess scratchFileBuffer = scratchFile.createBuffer();
            scratchFileBuffer.write(bytes);
            scratchFileBuffer.close();
            assertThrows(IOException.class, () -> scratchFileBuffer.seek(0));
        }
    }

    @Test
    void testBuffersClosed() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            byte[] bytes = new byte[PAGE_SIZE];
            RandomAccess scratchFileBuffer1 = scratchFile.createBuffer();
            scratchFileBuffer1.write(bytes);
            RandomAccess scratchFileBuffer2 = scratchFile.createBuffer();
            scratchFileBuffer2.write(bytes);
            RandomAccess scratchFileBuffer3 = scratchFile.createBuffer();
            scratchFileBuffer3.write(bytes);
            RandomAccess scratchFileBuffer4 = scratchFile.createBuffer();
            scratchFileBuffer4.write(bytes);

            // close two of the buffers explicitly
            scratchFileBuffer1.close();
            scratchFileBuffer3.close();

            // check status
            assertTrue(scratchFileBuffer1.isClosed());
            assertFalse(scratchFileBuffer2.isClosed());
            assertTrue(scratchFileBuffer3.isClosed());
            assertFalse(scratchFileBuffer4.isClosed());

            // closing ScratchFile shall close all remaining buffers which aren't closed yet
            scratchFile.close();
            assertTrue(scratchFileBuffer2.isClosed());
            assertTrue(scratchFileBuffer4.isClosed());
        }
    }

    @Test
    void testView() throws IOException
    {
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly()))
        {
            RandomAccess scratchFileBuffer = scratchFile.createBuffer();
            byte[] inputValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            scratchFileBuffer.write(inputValues);
            assertThrows(UnsupportedOperationException.class,
                    () -> scratchFileBuffer.createView(0, 10));
        }
    }

}
