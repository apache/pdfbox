/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.ttf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is a version of the one published at
 * https://code.google.com/p/jmzreader/wiki/BufferedRandomAccessFile augmented to handle unsigned
 * bytes. The original class is published under Apache 2.0 license. Fix is marked below
 *
 * This is an optimized version of the RandomAccessFile class as described by Nick Zhang on
 * JavaWorld.com. The article can be found at
 * http://www.javaworld.com/javaworld/javatips/jw-javatip26.html
 *
 * @author jg
 */
public class BufferedRandomAccessFile extends RandomAccessFile
{
    /**
     * Uses a byte instead of a char buffer for efficiency reasons.
     */
    private byte buffer[];
    private int buf_end = 0;
    private int buf_pos = 0;
    
    /**
     * The position inside the actual file.
     */
    private long real_pos = 0;
    
    /**
     * Buffer size.
     */
    private final int BUF_SIZE;

    /**
     * Creates a new instance of the BufferedRandomAccessFile.
     *
     * @param filename The path of the file to open.
     * @param mode Specifies the mode to use ("r", "rw", etc.) See the BufferedLineReader
     * documentation for more information.
     * @param bufsize The buffer size (in bytes) to use.
     * @throws IOException
     */
    public BufferedRandomAccessFile(String filename, String mode, int bufsize)
            throws IOException
    {
        super(filename, mode);
        invalidate();
        BUF_SIZE = bufsize;
        buffer = new byte[BUF_SIZE];
    }

    public BufferedRandomAccessFile(File file, String mode, int bufsize)
            throws IOException
    {
        this(file.getAbsolutePath(), mode, bufsize);
    }

    /**
     * Reads one byte form the current position
     *
     * @return The read byte or -1 in case the end was reached.
     */
    @Override
    public final int read() throws IOException
    {
        if (buf_pos >= buf_end)
        {
            if (fillBuffer() < 0)
            {
                return -1;
            }
        }
        if (buf_end == 0)
        {
            return -1;
        }
        else
        {
            // FIX to handle unsigned bytes
            return (buffer[buf_pos++] + 256) & 0xFF;
            // End of fix
        }
    }

    /**
     * Reads the next BUF_SIZE bytes into the internal buffer.
     *
     * @return
     * @throws IOException
     */
    private int fillBuffer() throws IOException
    {
        int n = super.read(buffer, 0, BUF_SIZE);

        if (n >= 0)
        {
            real_pos += n;
            buf_end = n;
            buf_pos = 0;
        }
        return n;
    }

    /**
     * Clears the local buffer.
     *
     * @throws IOException
     */
    private void invalidate() throws IOException
    {
        buf_end = 0;
        buf_pos = 0;
        real_pos = super.getFilePointer();
    }

    /**
     * Reads the set number of bytes into the passed buffer.
     *
     * @param b The buffer to read the bytes into.
     * @param off Byte offset within the file to start reading from
     * @param len Number of bytes to read into the buffer.
     * @return Number of bytes read.
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
        int leftover = buf_end - buf_pos;
        if (len <= leftover)
        {
            System.arraycopy(buffer, buf_pos, b, off, len);
            buf_pos += len;
            return len;
        }
        for (int i = 0; i < len; i++)
        {
            int c = this.read();
            if (c != -1)
            {
                b[off + i] = (byte) c;
            }
            else
            {
                return i;
            }
        }
        return len;
    }

    /**
     * Returns the current position of the pointer in the file.
     *
     * @return The byte position of the pointer in the file.
     */
    @Override
    public long getFilePointer() throws IOException
    {
        long l = real_pos;
        return (l - buf_end + buf_pos);
    }

    /**
     * Moves the internal pointer to the passed (byte) position in the file.
     *
     * @param pos The byte position to move to.
     */
    @Override
    public void seek(long pos) throws IOException
    {
        int n = (int) (real_pos - pos);
        if (n >= 0 && n <= buf_end)
        {
            buf_pos = buf_end - n;
        }
        else
        {
            super.seek(pos);
            invalidate();
        }
    }
}
