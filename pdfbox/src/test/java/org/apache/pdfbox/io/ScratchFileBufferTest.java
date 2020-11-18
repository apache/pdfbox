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
        try (ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupTempFileOnly()))
        {
            ScratchFileBuffer scratchFileBuffer = new ScratchFileBuffer(scratchFile);
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
}
