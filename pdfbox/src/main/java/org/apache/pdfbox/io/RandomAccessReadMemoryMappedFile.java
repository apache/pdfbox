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

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

/**
 * An implementation of the RandomAccess interface backed by a memory mapped file channel. The whole file is mapped to
 * memory and the max size is limited to Integer.MAX_VALUE.
 */
public class RandomAccessReadMemoryMappedFile implements RandomAccessRead
{

    // mapped byte buffer
    private MappedByteBuffer mappedByteBuffer;

    // size of the whole file
    private final long size;

    // file channel of the file to be read
    private final FileChannel fileChannel;

    /**
     * Default constructor.
     */
    public RandomAccessReadMemoryMappedFile(String filename) throws IOException
    {
        this(new File(filename));
    }

    /**
     * Default constructor.
     */
    public RandomAccessReadMemoryMappedFile(File file) throws IOException
    {
        fileChannel = FileChannel.open(file.toPath(), EnumSet.of(StandardOpenOption.READ));
        size = fileChannel.size();
        // TODO only ints are allowed -> implement paging
        if (size > Integer.MAX_VALUE)
        {
            throw new IOException(getClass().getName()
                    + " doesn't yet support files bigger than "
                    + Integer.MAX_VALUE);
        }
        // map the whole file to memory
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
        mappedByteBuffer.position((int) position);
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
        if (isEOF())
        {
            return -1;
        }
        return mappedByteBuffer.get() & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        if (isEOF())
        {
            return 0;
        }
        int remainingBytes = (int)size - mappedByteBuffer.position();
        mappedByteBuffer.get(b, offset, Math.min(remainingBytes, length));
        return Math.min(remainingBytes, length);
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
     * Ensure that the RandomAccessReadMemoryMappedFile is not closed
     * 
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (isClosed())
        {
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
        return mappedByteBuffer.position() >= size;
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
