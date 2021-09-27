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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;


/**
 * Regression test to check the known bugs in {@link ScratchFileBuffer}.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
@RunWith(Parameterized.class)
public class ScratchFileBufferTest
{

    private static final int PAGE_SIZE = 4096;
    private static final int NUM_ITERATIONS = 3;
    private final MemoryUsageSetting memoryUsageSetting;
    @Parameters(name = "{0}")
    public static Collection<Object[]> testData()
    {
        List<Object[]> params = new ArrayList<Object[]>();
        params.add(new Object[] {"setupTempFileOnly", MemoryUsageSetting.setupTempFileOnly()});
        params.add(new Object[] {"setupMainMemoryOnly", MemoryUsageSetting.setupMainMemoryOnly()});
        return params;
    }

    public ScratchFileBufferTest(final String name, MemoryUsageSetting memoryUsageSetting)
    {
        this.memoryUsageSetting = memoryUsageSetting;
    }

    /**
     * PDFBOX-4756: test positions are correct when seeking and that no EOFException is thrown in
     * ScratchFileBuffer.seek() beyond last page.
     *
     * @throws IOException
     */
    @Test
    public void testEOFBugInSeek() throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memoryUsageSetting);
        try
        {
            ScratchFileBuffer scratchFileBuffer = new ScratchFileBuffer(scratchFile);
            byte[] bytes = new byte[PAGE_SIZE];
            for (int i = 0; i < NUM_ITERATIONS; i++)
            {
                long p0 = scratchFileBuffer.getPosition();
                scratchFileBuffer.write(bytes);
                long p1 = scratchFileBuffer.getPosition();
                Assert.assertEquals(PAGE_SIZE, p1 - p0);
                scratchFileBuffer.write(bytes);
                long p2 = scratchFileBuffer.getPosition();
                Assert.assertEquals(PAGE_SIZE, p2 - p1);
                scratchFileBuffer.seek(0);
                scratchFileBuffer.seek(i * 2 * PAGE_SIZE);
            }
        }
        finally
        {
            scratchFile.close();
        }
    }

    @Test
    public void testRestorePageOnWrite() throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memoryUsageSetting);
        try
        {
            ScratchFileBuffer scratchFileBuffer = new ScratchFileBuffer(scratchFile);
            int pagesCount = 4;
            byte[] sourceData = new byte[PAGE_SIZE * pagesCount];
            for (int i = 0; i < PAGE_SIZE ; i++)
            {
                for (int pageNumber = 0; pageNumber < pagesCount; pageNumber++)
                {
                    sourceData[pageNumber * PAGE_SIZE + i] = (byte) ((i + pageNumber) & 255);
                }
            }
            for (int i = 0; i < pagesCount; i++)
            {
                int currentPageOffset = PAGE_SIZE * i;
                //Write array with offset and length
                scratchFileBuffer.write(sourceData, currentPageOffset, PAGE_SIZE - 20);
                scratchFileBuffer.cleanupMemory();

                //Write array
                byte[] array = Arrays.copyOfRange(
                        sourceData,
                        currentPageOffset + PAGE_SIZE - 20,
                        currentPageOffset + PAGE_SIZE - 2
                );
                scratchFileBuffer.write(array);
                scratchFileBuffer.cleanupMemory();

                //Write single bytes
                scratchFileBuffer.write(sourceData[currentPageOffset + PAGE_SIZE - 2]);
                scratchFileBuffer.cleanupMemory();
                scratchFileBuffer.write(sourceData[currentPageOffset + PAGE_SIZE - 1]);
                scratchFileBuffer.cleanupMemory();
            }
            scratchFileBuffer.seek(0);
            scratchFileBuffer.cleanupMemory();

            Assert.assertArrayEquals(sourceData, scratchFileBuffer.readFully(sourceData.length));
        }
        finally
        {
            scratchFile.close();
        }
    }

    @Test
    public void testRestorePageOnRead() throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memoryUsageSetting);
        try
        {
            ScratchFileBuffer scratchFileBuffer = new ScratchFileBuffer(scratchFile);
            int pagesCount = 4;
            byte[] sourceData = new byte[PAGE_SIZE * pagesCount];
            for (int i = 0; i < sourceData.length ; i++)
            {
                sourceData[i] = (byte) (i & 255);
            }
            scratchFileBuffer.write(sourceData);
            scratchFileBuffer.seek(0);

            byte[] readData = new byte[PAGE_SIZE * pagesCount];

            for (int i = 0; i < pagesCount; i++)
            {
                int currentPageOffset = PAGE_SIZE * i;
                //Read array with offset and length
                scratchFileBuffer.read(readData, currentPageOffset, PAGE_SIZE - 20);
                scratchFileBuffer.cleanupMemory();

                //Write array
                byte[] array = new byte[18];
                scratchFileBuffer.read(array);
                scratchFileBuffer.cleanupMemory();
                System.arraycopy(array, 0, readData, currentPageOffset + PAGE_SIZE - 20, array.length);

                //Read single bytes
                readData[currentPageOffset + PAGE_SIZE - 2] = (byte)scratchFileBuffer.read();
                scratchFileBuffer.cleanupMemory();
                readData[currentPageOffset + PAGE_SIZE - 1] = (byte)scratchFileBuffer.read();
                scratchFileBuffer.cleanupMemory();
            }

            Assert.assertArrayEquals(sourceData, readData);
        }
        finally
        {
            scratchFile.close();
        }
    }

    @Test
    public void testWritePageOnCleanup() throws IOException {
        ScratchFile scratchFile = Mockito.spy(new ScratchFile(memoryUsageSetting));
        byte[] data = new byte[PAGE_SIZE];
        int bytesForWrite = 1000;
        for (int i = 0; i < bytesForWrite; i++)
        {
            data[i] = (byte) (i & 255);
        }
        try
        {
            ScratchFileBuffer scratchFileBuffer = new ScratchFileBuffer(scratchFile);
            scratchFileBuffer.write(data, 0, bytesForWrite);
            scratchFileBuffer.cleanupMemory();

            Mockito.verify(scratchFile).writePage(0, data);
        }
        finally
        {
            scratchFile.close();
        }
    }
}
