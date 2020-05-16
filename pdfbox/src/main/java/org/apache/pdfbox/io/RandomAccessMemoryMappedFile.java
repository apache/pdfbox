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
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

/**
 * An implementation of the RandomAccess interface to store data in memory.
 * The data will be stored in chunks organized in an ArrayList.
 */
public class RandomAccessMemoryMappedFile implements RandomAccessRead
{

    // max buffer size is 2Gb
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
    // default buffer size is 128kb
    private static final int DEFAULT_BUFFER_SIZE = 2 ^ 17;

    // current chunk
    private MappedByteBuffer mappedByteBuffer;

    // current pointer for the current buffer
    private long currentBufferPointer;
    // start file position of the buffer
    private long startPositionBuffer = 0;
    // end file position of the buffer
    private long endPositionBuffer;
    // size of the whole file
    private long size;

    private final FileChannel fileChannel;

    /**
     * Default constructor.
     */
    public RandomAccessMemoryMappedFile(String filename) throws IOException
    {
        fileChannel = (FileChannel) Files
                .newByteChannel(Paths.get(
                filename),
                EnumSet.of(StandardOpenOption.READ));
        size = fileChannel.size();
        startPositionBuffer = 0;
        endPositionBuffer = size;
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        fileChannel.close();
        mappedByteBuffer = null;
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
        // TODO only ints are allowed -> max buf size
        mappedByteBuffer.position((int) position);
        // if (position >= startPositionBuffer && position < endPositionBuffer)
        // {
        // currentBufferPointer = position;
        // }
        // else if (position < size)
        // {
        // newBuffer(position);
        // }
        // else
        // {
        // // TODO jump to end of buffer
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
       checkClosed();
       return mappedByteBuffer.position();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();
        // if (mappedByteBuffer.position() >= endPositionBuffer - startPositionBuffer)
        // {
        // newBuffer(getPosition());
        // }
        int result = -1;
        try
        {
            result = mappedByteBuffer.get() & 0xff;
        }
        catch (BufferUnderflowException exception)
        {
            System.out.println("Size: " + size);
            System.out.println("Position: " + mappedByteBuffer.position());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        if (isEOF())
        {
            return 0;
        }
        if (mappedByteBuffer.position() >= size)
            System.err.println("GOTCHA");
        mappedByteBuffer.get(b, offset, length);
        return length;
    }

    // private int readRemainingBytes(byte[] b, int offset, int length)
    // {
    // if (isEOF())
    // {
    // // TODO return -1 ??
    // return 0;
    // }
    // int maxLength = (int) Math.min(length, size-pointer);
    // int remainingBytes = chunkSize - currentBufferPointer;
    // // no more bytes left
    // if (remainingBytes == 0)
    // {
    // return 0;
    // }
    // if (maxLength >= remainingBytes)
    // {
    // // copy the remaining bytes from the current buffer
    // System.arraycopy(currentBuffer, currentBufferPointer, b, offset, remainingBytes);
    // // end of file reached
    // currentBufferPointer += remainingBytes;
    // pointer += remainingBytes;
    // return remainingBytes;
    // }
    // else
    // {
    // // copy the remaining bytes from the whole buffer
    // System.arraycopy(currentBuffer, currentBufferPointer, b, offset, maxLength);
    // // end of file reached
    // currentBufferPointer += maxLength;
    // pointer += maxLength;
    // return maxLength;
    // }
    // }

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
     * switch to the next buffer chunk and reset the buffer pointer.
     */
    // private void newBuffer(long startPosition) throws IOException
    // {
    // startPositionBuffer = startPosition;
    // endPositionBuffer = fileChannel.read(currentBuffer,
    // startPositionBuffer)
    // + startPositionBuffer;
    // }
    
    /**
     * Ensure that the RandomAccessBuffer is not closed
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (isClosed())
        {
            // consider that the rab is closed if there is no current buffer
            throw new IOException(getClass().getSimpleName() + " already closed");
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return !fileChannel.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return fileChannel.position() == size;
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
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
}
