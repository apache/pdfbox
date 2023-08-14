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

/**
 * An implementation of the RandomAccess interface to store data in memory. The data will be stored in chunks organized
 * in an ArrayList. The data can be read after writing.
 */
public class RandomAccessReadWriteBuffer extends RandomAccessReadBuffer implements RandomAccess
{
    /**
     * Default constructor.
     */
    public RandomAccessReadWriteBuffer()
    {
        super();
    }

    /**
     * Default constructor.
     */
    public RandomAccessReadWriteBuffer(int definedChunkSize)
    {
        super(definedChunkSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws IOException
    {
        checkClosed();
        resetBuffers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException
    {
        checkClosed();

        if (chunkSize - currentBufferPointer <= 0)
        {
            expandBuffer();
        }
        currentBuffer.put((byte) b);
        currentBufferPointer++;
        pointer++;
        if (pointer > size)
        {
            size = pointer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkClosed();

        int remain = len;
        int bOff = off;

        while (remain > 0)
        {
            int bytesToWrite = Math.min(remain, chunkSize - currentBufferPointer);
            if (bytesToWrite <= 0)
            {
                expandBuffer();
                bytesToWrite = Math.min(remain, chunkSize - currentBufferPointer);
            }
            if (bytesToWrite > 0)
            {
                currentBuffer.put(b, bOff, bytesToWrite);
                currentBufferPointer += bytesToWrite;
                pointer += bytesToWrite;
            }
            bOff += bytesToWrite;
            remain -= bytesToWrite;
        }
        if (pointer > size)
        {
            size = pointer;
        }
    }

}
