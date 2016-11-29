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
import java.io.FileNotFoundException;
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
    private final byte buffer[];
    private int bufend = 0;
    private int bufpos = 0;
    
    /**
     * The position inside the actual file.
     */
    private long realpos = 0;
    
    /**
     * Buffer size.
     */
    private final int BUFSIZE;

    /**
     * Creates a new instance of the BufferedRandomAccessFile.
     *
     * @param filename The path of the file to open.
     * @param mode Specifies the mode to use ("r", "rw", etc.) See the BufferedLineReader
     * documentation for more information.
     * @param bufsize The buffer size (in bytes) to use.
     * @throws FileNotFoundException If the mode is "r" but the given string does not denote an
     * existing regular file, or if the mode begins with "rw" but the given string does not denote
     * an existing, writable regular file and a new regular file of that name cannot be created, or
     * if some other error occurs while opening or creating the file.
     */
    public BufferedRandomAccessFile(String filename, String mode, int bufsize)
            throws FileNotFoundException
    {
        super(filename, mode);
        BUFSIZE = bufsize;
        buffer = new byte[BUFSIZE];
    }

    /**
     * Creates a new instance of the BufferedRandomAccessFile.
     *
     * @param file The file to open.
     * @param mode Specifies the mode to use ("r", "rw", etc.) See the BufferedLineReader
     * documentation for more information.
     * @param bufsize The buffer size (in bytes) to use.
     * @throws FileNotFoundException If the mode is "r" but the given file path does not denote an
     * existing regular file, or if the mode begins with "rw" but the given file path does not denote
     * an existing, writable regular file and a new regular file of that name cannot be created, or
     * if some other error occurs while opening or creating the file.
     */
    public BufferedRandomAccessFile(File file, String mode, int bufsize)
            throws FileNotFoundException
    {
        super(file, mode);
        BUFSIZE = bufsize;
        buffer = new byte[BUFSIZE];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int read() throws IOException
    {
        if (bufpos >= bufend && fillBuffer() < 0)
        {
            return -1;
        }
        if (bufend == 0)
        {
            return -1;
        }
        // FIX to handle unsigned bytes
        return (buffer[bufpos++] + 256) & 0xFF;
        // End of fix
    }

    /**
     * Reads the next BUFSIZE bytes into the internal buffer.
     *
     * @return The total number of bytes read into the buffer, or -1 if there is no more data
     * because the end of the file has been reached.
     * @throws IOException If the first byte cannot be read for any reason other than end of file,
     * or if the random access file has been closed, or if some other I/O error occurs.
     */
    private int fillBuffer() throws IOException
    {
        int n = super.read(buffer, 0, BUFSIZE);

        if (n >= 0)
        {
            realpos += n;
            bufend = n;
            bufpos = 0;
        }
        return n;
    }

    /**
     * Clears the local buffer.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void invalidate() throws IOException
    {
        bufend = 0;
        bufpos = 0;
        realpos = super.getFilePointer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
        int leftover = bufend - bufpos;
        if (len <= leftover)
        {
            System.arraycopy(buffer, bufpos, b, off, len);
            bufpos += len;
            return len;
        }
        System.arraycopy(buffer, bufpos, b, off, leftover);
        bufpos += leftover;
        if (fillBuffer() > 0)
        {
            int bytesRead = read(b, off + leftover, len - leftover);
            if (bytesRead > 0)
            {
                leftover += bytesRead;
            }
        }
        return leftover > 0 ? leftover : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFilePointer() throws IOException
    {
        return realpos - bufend + bufpos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long pos) throws IOException
    {
        int n = (int) (realpos - pos);
        if (n >= 0 && n <= bufend)
        {
            bufpos = bufend - n;
        }
        else
        {
            super.seek(pos);
            invalidate();
        }
    }
}
