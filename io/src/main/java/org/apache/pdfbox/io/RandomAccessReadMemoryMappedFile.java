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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An implementation of the RandomAccess interface backed by a memory mapped file channel. The whole file is mapped to
 * memory and the max size is limited to Integer.MAX_VALUE.
 */
public class RandomAccessReadMemoryMappedFile implements RandomAccessRead
{

    // mapped byte buffer
    private ByteBuffer mappedByteBuffer;

    // size of the whole file
    private final long size;

    // file channel of the file to be read
    private final FileChannel fileChannel;

    // function to unmap the byte buffer
    private final Consumer<? super ByteBuffer> unmapper;

    /**
     * Create a random access memory mapped file instance for the file with the given name.
     * 
     * @param filename the filename of the file to be read
     * 
     * @throws IOException If there is an IO error opening the file.
     */
    public RandomAccessReadMemoryMappedFile(String filename) throws IOException
    {
        this(new File(filename));
    }

    /**
     * Create a random access memory mapped file instance for the given file.
     * 
     * @param file the file to be read
     * 
     * @throws IOException If there is an IO error opening the file.
     */
    public RandomAccessReadMemoryMappedFile(File file) throws IOException
    {
        this(file.toPath());
    }

    /**
     * Create a random access memory mapped file instance using the given path.
     * 
     * @param path path of the file to be read.
     * 
     * @throws IOException If there is an IO error opening the file.
     */
    public RandomAccessReadMemoryMappedFile(Path path) throws IOException
    {
        fileChannel = FileChannel.open(path, EnumSet.of(StandardOpenOption.READ));
        size = fileChannel.size();
        // TODO only ints are allowed -> implement paging
        if (size > Integer.MAX_VALUE)
        {
            throw new IOException(getClass().getName() + " doesn't yet support files bigger than "
                    + Integer.MAX_VALUE);
        }
        // map the whole file to memory
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
        unmapper = IOUtils::unmap;
    }

    private RandomAccessReadMemoryMappedFile(RandomAccessReadMemoryMappedFile parent)
    {
        mappedByteBuffer = parent.mappedByteBuffer.duplicate();
        size = parent.size;
        mappedByteBuffer.rewind();
        // unmap doesn't work on duplicate, see Unsafe#invokeCleaner
        unmapper = null;
        fileChannel = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if (fileChannel != null)
        {
            fileChannel.close();
        }
        if (mappedByteBuffer != null)
        {
            Optional.ofNullable(unmapper).ifPresent(u -> u.accept(mappedByteBuffer));
            mappedByteBuffer = null;
        }
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
        // it is allowed to jump beyond the end of the file
        // jump to the end of the reader
        mappedByteBuffer.position((int) Math.min(position, size));
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
            return -1;
        }
        int remainingBytes = (int)size - mappedByteBuffer.position();
        remainingBytes = Math.min(remainingBytes, length);
        mappedByteBuffer.get(b, offset, remainingBytes);
        return remainingBytes;
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
     * @throws IOException If RandomAccessBuffer already closed
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
        return mappedByteBuffer == null;
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

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength)
    {
        return new RandomAccessReadView(new RandomAccessReadMemoryMappedFile(this), startPosition,
                streamLength, true);
    }
}
