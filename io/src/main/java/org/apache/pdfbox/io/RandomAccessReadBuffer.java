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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of the RandomAccessRead interface to store data in memory. The data will be stored in chunks
 * organized in an ArrayList.
 */
public class RandomAccessReadBuffer implements RandomAccessRead
{
    // default chunk size is 4kb
    public static final int DEFAULT_CHUNK_SIZE_4KB = 1 << 12;
    // use the default chunk size
    protected int chunkSize = DEFAULT_CHUNK_SIZE_4KB;
    // list containing all chunks
    private final List<ByteBuffer> bufferList;
    // current chunk
    protected ByteBuffer currentBuffer;
    // current pointer to the whole buffer
    protected long pointer = 0;
    // current pointer for the current chunk
    protected int currentBufferPointer = 0;
    // size of the whole buffer
    protected long size = 0;
    // current chunk list index
    private int bufferListIndex = 0;
    // maximum chunk list index
    private int bufferListMaxIndex = 0;

    // map holding all copies of the current buffer
    private final ConcurrentMap<Long, RandomAccessReadBuffer> rarbCopies = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    protected RandomAccessReadBuffer()
    {
        this(DEFAULT_CHUNK_SIZE_4KB);
    }

    /**
     * Default constructor.
     */
    protected RandomAccessReadBuffer(int definedChunkSize)
    {
        // starting with one chunk
        chunkSize = definedChunkSize;
        currentBuffer = ByteBuffer.allocate(chunkSize);
        bufferList = new ArrayList<>(1);
        bufferList.add(currentBuffer);
    }

    /**
     * Create a random access buffer using the given byte array.
     * 
     * @param input the byte array to be read
     */
    public RandomAccessReadBuffer(byte[] input)
    {
        // this is a special case. Wrap the given byte array to a single ByteBuffer.
        this(ByteBuffer.wrap(input));
    }

    /**
     * Create a random access buffer using the given ByteBuffer.
     * 
     * @param input the ByteBuffer to be read
     */
    public RandomAccessReadBuffer(ByteBuffer input)
    {
        chunkSize = input.limit();
        size = chunkSize;
        currentBuffer = input;
        bufferList = new ArrayList<>(1);
        bufferList.add(currentBuffer);
    }

    /**
     * Create a random access read buffer of the given input stream by copying the data to the memory.
     * 
     * @param input the input stream to be read
     * @throws IOException if something went wrong while copying the data
     */
    public RandomAccessReadBuffer(InputStream input) throws IOException
    {
        this();
        int bytesRead = 0;
        int remainingBytes = chunkSize;
        int offset = 0;
        byte[] eofCheck = new byte[1];
        while (remainingBytes > 0 &&
                (bytesRead = input.read(currentBuffer.array(), offset, remainingBytes)) > -1)
        {
            remainingBytes -= bytesRead;
            offset += bytesRead;
            size += bytesRead;
            if (remainingBytes == 0 && input.read(eofCheck) > 0)
            {
                expandBuffer();
                currentBuffer.put(eofCheck);
                offset = 1;
                remainingBytes = chunkSize - 1;
                size++;
            }
        }
        currentBuffer.limit(offset);
        seek(0);
    }

    private RandomAccessReadBuffer(RandomAccessReadBuffer parent)
    {
        chunkSize = parent.chunkSize;
        size = parent.size;
        bufferListMaxIndex = parent.bufferListMaxIndex;
        bufferList = new ArrayList<>(parent.bufferList.size());
        for (ByteBuffer buffer : parent.bufferList)
        {
            ByteBuffer newBuffer = buffer.duplicate();
            newBuffer.rewind();
            bufferList.add(newBuffer);
        }
        currentBuffer = bufferList.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        rarbCopies.values().forEach(IOUtils::closeQuietly);
        rarbCopies.clear();
        currentBuffer = null;
        bufferList.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long position) throws IOException
    {
        checkClosed();
        if (position < 0)
        {
            throw new IOException("Invalid position " + position);
        }
        if (position < size)
        {
            pointer = position;
            // calculate the chunk list index
            bufferListIndex = chunkSize > 0 ? (int) (pointer / chunkSize) : 0;
            currentBufferPointer = chunkSize > 0 ? (int) (pointer % chunkSize) : 0;
            currentBuffer = bufferList.get(bufferListIndex);
        }
        else
        {
            // it is allowed to jump beyond the end of the file
            // jump to the end of the buffer
            pointer = size;
            bufferListIndex = bufferListMaxIndex;
            currentBuffer = bufferList.get(bufferListIndex);
            currentBufferPointer = chunkSize > 0 ? (int) (size % chunkSize) : 0;
        }
        currentBuffer.position(currentBufferPointer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return pointer;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();
        if (pointer >= this.size)
        {
            return -1;
        }
        if (currentBufferPointer >= chunkSize)
        {
            if (bufferListIndex >= bufferListMaxIndex)
            {
                return -1;
            }
            else
            {
                currentBuffer = bufferList.get(++bufferListIndex);
                currentBufferPointer = 0;
            }
        }
        pointer++;
        return currentBuffer.get(currentBufferPointer++) & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        int bytesRead = readRemainingBytes(b, offset, length);
        if (bytesRead == -1)
        {
            if (available() > 0)
            {
                bytesRead = 0;
            }
            else
            {
                return -1;
            }
        }
        while (bytesRead < length && available() > 0)
        {
            if (currentBufferPointer == chunkSize)
            {
                nextBuffer();
            }
            bytesRead += readRemainingBytes(b, offset + bytesRead, length - bytesRead);
        }
        return bytesRead;
    }

    private int readRemainingBytes(byte[] b, int offset, int length)
    {
        if (pointer >= size)
        {
            return -1;
        }
        int maxLength = (int) Math.min(length, size - pointer);
        int remainingBytes = chunkSize - currentBufferPointer;
        // no more bytes left
        if (remainingBytes == 0)
        {
            return -1;
        }
        if (maxLength >= remainingBytes)
        {
            // copy the remaining bytes from the current buffer
            currentBuffer.position(currentBufferPointer);
            currentBuffer.get(b, offset, remainingBytes);
            // end of file reached
            currentBufferPointer += remainingBytes;
            pointer += remainingBytes;
            return remainingBytes;
        }
        else
        {
            // copy the remaining bytes from the whole buffer
            currentBuffer.position(currentBufferPointer);
            currentBuffer.get(b, offset, maxLength);
            // end of file reached
            currentBufferPointer += maxLength;
            pointer += maxLength;
            return maxLength;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() throws IOException
    {
        checkClosed();
        return size;
    }

    /**
     * create a new buffer chunk and adjust all pointers and indices.
     */
    protected void expandBuffer() throws IOException
    {
        if (bufferListMaxIndex > bufferListIndex)
        {
            // there is already an existing chunk
            nextBuffer();
        }
        else
        {
            // create a new chunk and add it to the buffer
            currentBuffer = ByteBuffer.allocate(chunkSize);
            bufferList.add(currentBuffer);
            currentBufferPointer = 0;
            bufferListMaxIndex++;
            bufferListIndex++;
        }
    }

    /**
     * switch to the next buffer chunk and reset the buffer pointer.
     */
    private void nextBuffer() throws IOException
    {
        if (bufferListIndex == bufferListMaxIndex)
        {
            throw new IOException("No more chunks available, end of buffer reached");
        }
        currentBufferPointer = 0;
        currentBuffer = bufferList.get(++bufferListIndex);
        currentBuffer.rewind();
    }
    
    /**
     * Ensure that the RandomAccessBuffer is not closed
     * @throws IOException If RandomAccessBuffer already closed
     */
    protected void checkClosed() throws IOException
    {
        if (currentBuffer == null)
        {
            // consider that the rab is closed if there is no current buffer
            throw new IOException("RandomAccessBuffer already closed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return currentBuffer == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return pointer >= size;
    }

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException
    {
        Long currentThreadID = Thread.currentThread().getId();
        RandomAccessReadBuffer randomAccessReadBuffer = rarbCopies.get(currentThreadID);
        if (randomAccessReadBuffer == null || randomAccessReadBuffer.isClosed())
        {
            randomAccessReadBuffer = new RandomAccessReadBuffer(this);
            rarbCopies.put(currentThreadID, randomAccessReadBuffer);
        }
        return new RandomAccessReadView(randomAccessReadBuffer, startPosition, streamLength);
    }

    /**
     * Create e new RandomAccessReadBuffer using the given InputStream. The data is copied to the memory and the
     * InputStream is closed.
     * 
     * @param inputStream the InputStream holding the data to be copied
     * 
     * @return the RandomAccessReadBuffer holding the data of the InputStream
     * @throws IOException if something went wrong while copying the data
     */
    public static RandomAccessReadBuffer createBufferFromStream(InputStream inputStream) throws IOException
    {
        RandomAccessReadBuffer randomAccessRead = null;
        try
        {
            randomAccessRead = new RandomAccessReadBuffer(inputStream);
        }
        finally
        {
            inputStream.close();
        }
        return randomAccessRead;
    }

    /**
     * Reset to position 0 and remove all buffers but the first one.
     */
    protected void resetBuffers()
    {
        size = 0;
        pointer = 0;
        currentBuffer = bufferList.get(0);
        currentBuffer.position(0);
        currentBufferPointer = 0;
        bufferListIndex = 0;
        bufferListMaxIndex = 0;
        bufferList.clear();
        bufferList.add(currentBuffer);
    }
}
