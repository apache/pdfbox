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
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A RandomAccess implementation which allows data to be stored in a scratch file on the disk to
 * reduce memory consumption.
 *
 * @author Ben Litchfield
 */
public class RandomAccessFile implements RandomAccess
{
    private final java.io.RandomAccessFile ras;
    private boolean isClosed;

    /**
     * Constructor.
     *
     * @param file The file to write the data to.
     * @param mode The writing mode.
     * @throws FileNotFoundException If the file cannot be created.
     */
    public RandomAccessFile(File file, String mode) throws FileNotFoundException
    {
        ras = new java.io.RandomAccessFile(file, mode);
    }
    
    @Override
    public void close() throws IOException
    {
        ras.close();
        isClosed = true;
    }
    
    @Override
    public void clear() throws IOException
    {
        checkClosed();
        ras.seek(0);
        ras.setLength(0);
    }
    
    @Override
    public void seek(long position) throws IOException
    {
        checkClosed();
        ras.seek(position);
    }
    
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return ras.getFilePointer();
    }
    
    @Override
    public int read() throws IOException
    {
        checkClosed();
        return ras.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException
    {
        checkClosed();
        return ras.read(b);
    }
    
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        return ras.read(b, offset, length);
    }
    
    @Override
    public long length() throws IOException
    {
        checkClosed();
        return ras.length();
    }

    /**
     * Ensure that the RandomAccessFile is not closed
     * 
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException("RandomAccessFile already closed");
        }

    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }
    
    @Override
    public void write(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        ras.write(b, offset, length);
    }
    
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
    
    @Override
    public void write(int b) throws IOException
    {
        checkClosed();
        ras.write(b);
    }
    
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

    @Override
    public void rewind(int bytes) throws IOException
    {
        checkClosed();
        ras.seek(ras.getFilePointer() - bytes);
    }
    
    @Override
    public byte[] readFully(int length) throws IOException
    {
        checkClosed();
        byte[] b = new byte[length];
        ras.readFully(b);
        return b;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return peek() == -1;
    }

    @Override
    public int available() throws IOException
    {
        checkClosed();
        return (int) Math.min(ras.length() - getPosition(), Integer.MAX_VALUE);
    }
}
