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
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the RandomAccess interface to store data in memory.
 * The data will be stored in chunks organized in an ArrayList.
 */
public class RandomAccessBuffer implements RandomAccess, Cloneable
{
    // default chunk size is 1kb
    private static final int DEFAULT_CHUNK_SIZE = 1024;
    // use the default chunk size
    private int chunkSize = DEFAULT_CHUNK_SIZE;
    // list containing all chunks
    private List<byte[]> bufferList = null;
    // current chunk
    private byte[] currentBuffer;
    // current pointer to the whole buffer
    private long pointer;
    // current pointer for the current chunk
    private int currentBufferPointer;
    // size of the whole buffer
    private long size;
    // current chunk list index
    private int bufferListIndex;
    // maximum chunk list index
    private int bufferListMaxIndex;

    /**
     * Default constructor.
     */
    public RandomAccessBuffer()
    {
        this(DEFAULT_CHUNK_SIZE);
    }

    /**
     * Default constructor.
     */
    private RandomAccessBuffer(int definedChunkSize)
    {
        // starting with one chunk
        bufferList = new ArrayList<>();
        chunkSize = definedChunkSize;
        currentBuffer = new byte[chunkSize];
        bufferList.add(currentBuffer);
        pointer = 0;
        currentBufferPointer = 0;
        size = 0;
        bufferListIndex = 0;
        bufferListMaxIndex = 0;
    }

    /**
     * Create a random access buffer using the given byte array.
     * 
     * @param input the byte array to be read
     */
    public RandomAccessBuffer(byte[] input)
    {
        // this is a special case. The given byte array is used as the one
        // and only chunk.
        bufferList = new ArrayList<>(1);
        chunkSize = input.length;
        currentBuffer = input;
        bufferList.add(currentBuffer);
        pointer = 0;
        currentBufferPointer = 0;
        size = chunkSize;
        bufferListIndex = 0;
        bufferListMaxIndex = 0;
    }

    /**
     * Create a random access buffer of the given input stream by copying the data.
     * 
     * @param input the input stream to be read
     * @throws IOException if something went wrong while copying the data
     */
    public RandomAccessBuffer(InputStream input) throws IOException
    {
        this();
        byte[] byteBuffer = new byte[8192];
        int bytesRead = 0;
        while ((bytesRead = input.read(byteBuffer)) > -1)
        {
            write(byteBuffer, 0, bytesRead);
        }
        seek(0);
    }

    @Override
    public RandomAccessBuffer clone()
    {
        RandomAccessBuffer copy = new RandomAccessBuffer(chunkSize);

        copy.bufferList = new ArrayList<>(bufferList.size());
        for (byte [] buffer : bufferList)
        {
            byte [] newBuffer = new byte [buffer.length];
            System.arraycopy(buffer,0,newBuffer,0,buffer.length);
            copy.bufferList.add(newBuffer);
        }
        if (currentBuffer!=null)
        {
            copy.currentBuffer = copy.bufferList.get(copy.bufferList.size()-1);
        }
        else
        {
            copy.currentBuffer = null;
        }
        copy.pointer = pointer;
        copy.currentBufferPointer = currentBufferPointer;
        copy.size = size;
        copy.bufferListIndex = bufferListIndex;
        copy.bufferListMaxIndex = bufferListMaxIndex;

        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        currentBuffer = null;
        bufferList.clear();
        pointer = 0;
        currentBufferPointer = 0;
        size = 0;
        bufferListIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        bufferList.clear();
        currentBuffer = new byte[chunkSize];
        bufferList.add(currentBuffer);
        pointer = 0;
        currentBufferPointer = 0;
        size = 0;
        bufferListIndex = 0;
        bufferListMaxIndex = 0;
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
            throw new IOException("Invalid position "+position);
        }
        pointer = position;
        if (pointer < size)
        {
            // calculate the chunk list index
            bufferListIndex = (int)(pointer / chunkSize);
            currentBufferPointer = (int)(pointer % chunkSize);
            currentBuffer = bufferList.get(bufferListIndex);
        }
        else
        {
            // it is allowed to jump beyond the end of the file
            // jump to the end of the buffer
            bufferListIndex = bufferListMaxIndex;
            currentBuffer = bufferList.get(bufferListIndex);
            currentBufferPointer = (int)(size % chunkSize);
        }
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
        return currentBuffer[currentBufferPointer++] & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        if (pointer >= size)
        {
            return 0;
        }
        int bytesRead = readRemainingBytes(b, offset, length);
        while (bytesRead < length && available() > 0)
        {
            bytesRead += readRemainingBytes(b, offset + bytesRead, length - bytesRead);
            if (currentBufferPointer == chunkSize)
            {
                nextBuffer();
            }
        }
        return bytesRead;
    }

    private int readRemainingBytes(byte[] b, int offset, int length) throws IOException
    {
        if (pointer >= size)
        {
            return 0;
        }
        int maxLength = (int) Math.min(length, size-pointer);
        int remainingBytes = chunkSize - currentBufferPointer;
        // no more bytes left
        if (remainingBytes == 0)
        {
            return 0;
        }
        if (maxLength >= remainingBytes)
        {
            // copy the remaining bytes from the current buffer
            System.arraycopy(currentBuffer, currentBufferPointer, b, offset, remainingBytes);
            // end of file reached
            currentBufferPointer += remainingBytes;
            pointer += remainingBytes;
            return remainingBytes;
        }
        else
        {
            // copy the remaining bytes from the whole buffer
            System.arraycopy(currentBuffer, currentBufferPointer, b, offset, maxLength);
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
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException
    {
        checkClosed();
        // end of buffer reached?
        if (currentBufferPointer >= chunkSize)
        {
            if (pointer + chunkSize >= Integer.MAX_VALUE)
            {
                throw new IOException("RandomAccessBuffer overflow");
            }
            expandBuffer();
        }
        currentBuffer[currentBufferPointer++] = (byte)b;
        pointer++;
        if (pointer > this.size)
        {
            this.size = pointer;
        }
        // end of buffer reached now?
        if (currentBufferPointer >= chunkSize)
        {
            if (pointer + chunkSize >= Integer.MAX_VALUE)
            {
                throw new IOException("RandomAccessBuffer overflow");
            }
            expandBuffer();
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
    public void write(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        long newSize = pointer + length;
        int remainingBytes = chunkSize - currentBufferPointer;
        if (length >= remainingBytes)
        {
            if (newSize > Integer.MAX_VALUE)
            {
                throw new IOException("RandomAccessBuffer overflow");
            }
            // copy the first bytes to the current buffer
            System.arraycopy(b, offset, currentBuffer, currentBufferPointer, remainingBytes);
            int newOffset = offset + remainingBytes;
            long remainingBytes2Write = length - remainingBytes;
            // determine how many buffers are needed for the remaining bytes
            int numberOfNewArrays = (int)remainingBytes2Write / chunkSize;
            for (int i=0;i<numberOfNewArrays;i++)
            {
                expandBuffer();
                System.arraycopy(b, newOffset, currentBuffer, currentBufferPointer, chunkSize);
                newOffset += chunkSize;
            }
            // are there still some bytes to be written?
            remainingBytes2Write -= numberOfNewArrays * (long) chunkSize;
            if (remainingBytes2Write >= 0)
            {
                expandBuffer();
                if (remainingBytes2Write > 0)
                {
                    System.arraycopy(b, newOffset, currentBuffer, currentBufferPointer, (int)remainingBytes2Write);
                }
                currentBufferPointer = (int)remainingBytes2Write;
            }
        }
        else
        {
            System.arraycopy(b, offset, currentBuffer, currentBufferPointer, length);
            currentBufferPointer += length;
        }
        pointer += length;
        if (pointer > this.size)
        {
            this.size = pointer;
        }
    }

    /**
     * create a new buffer chunk and adjust all pointers and indices.
     */
    private void expandBuffer() throws IOException
    {
        if (bufferListMaxIndex > bufferListIndex)
        {
            // there is already an existing chunk
            nextBuffer();
        }
        else
        {
            // create a new chunk and add it to the buffer
            currentBuffer = new byte[chunkSize];
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
    }
    
    /**
     * Ensure that the RandomAccessBuffer is not closed
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (currentBuffer==null)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        return (int) Math.min(length() - getPosition(), Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int peek() throws IOException
    {
        int result = read();
        if (result != -1)
        {
            rewind(1);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewind(int bytes) throws IOException
    {
        checkClosed();
        seek(getPosition() - bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readFully(int length) throws IOException
    {
        byte[] b = new byte[length];
        int bytesRead = read(b);
        while (bytesRead < length)
        {
            bytesRead += read(b, bytesRead, length - bytesRead);
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
}
