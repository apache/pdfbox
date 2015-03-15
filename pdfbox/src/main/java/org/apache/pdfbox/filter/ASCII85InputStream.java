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
package org.apache.pdfbox.filter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents an ASCII85 stream.
 *
 * @author Ben Litchfield
 *
 */
final class ASCII85InputStream extends FilterInputStream
{
    private int index;
    private int n;
    private boolean eof;

    private byte[] ascii;
    private byte[] b;

    private static final char TERMINATOR = '~';
    private static final char OFFSET = '!';
    private static final char NEWLINE = '\n';
    private static final char RETURN = '\r';
    private static final char SPACE = ' ';
    private static final char PADDING_U = 'u';
    private static final char Z = 'z';

    /**
     * Constructor.
     *
     * @param is The input stream to actually read from.
     */
    ASCII85InputStream(InputStream is)
    {
        super(is);
        index = 0;
        n = 0;
        eof = false;
        ascii = new byte[5];
        b = new byte[4];
    }

    /**
     * This will read the next byte from the stream.
     *
     * @return The next byte read from the stream.
     *
     * @throws IOException If there is an error reading from the wrapped stream.
     */
    @Override
    public int read() throws IOException
    {
        if (index >= n)
        {
            if (eof)
            {
                return -1;
            }
            index = 0;
            int k;
            byte z;
            do
            {
                int zz = (byte) in.read();
                if (zz == -1)
                {
                    eof = true;
                    return -1;
                }
                z = (byte) zz;
            } while (z == NEWLINE || z == RETURN || z == SPACE);

            if (z == TERMINATOR)
            {
                eof = true;
                ascii = b = null;
                n = 0;
                return -1;
            }
            else if (z == Z)
            {
                b[0] = b[1] = b[2] = b[3] = 0;
                n = 4;
            }
            else
            {
                ascii[0] = z; // may be EOF here....
                for (k = 1; k < 5; ++k)
                {
                    do
                    {
                        int zz = (byte) in.read();
                        if (zz == -1)
                        {
                            eof = true;
                            return -1;
                        }
                        z = (byte) zz;
                    } while (z == NEWLINE || z == RETURN || z == SPACE);
                    ascii[k] = z;
                    if (z == TERMINATOR)
                    {
                        // don't include ~ as padding byte
                        ascii[k] = (byte) PADDING_U;
                        break;
                    }
                }
                n = k - 1;
                if (n == 0)
                {
                    eof = true;
                    ascii = null;
                    b = null;
                    return -1;
                }
                if (k < 5)
                {
                    for (++k; k < 5; ++k)
                    {
                        // use 'u' for padding
                        ascii[k] = (byte) PADDING_U;
                    }
                    eof = true;
                }
                // decode stream
                long t = 0;
                for (k = 0; k < 5; ++k)
                {
                    z = (byte) (ascii[k] - OFFSET);
                    if (z < 0 || z > 93)
                    {
                        n = 0;
                        eof = true;
                        ascii = null;
                        b = null;
                        throw new IOException("Invalid data in Ascii85 stream");
                    }
                    t = (t * 85L) + z;
                }
                for (k = 3; k >= 0; --k)
                {
                    b[k] = (byte) (t & 0xFFL);
                    t >>>= 8;
                }
            }
        }
        return b[index++] & 0xFF;
    }

    /**
     * This will read a chunk of data.
     *
     * @param data The buffer to write data to.
     * @param offset The offset into the data stream.
     * @param len The number of byte to attempt to read.
     *
     * @return The number of bytes actually read.
     *
     * @throws IOException If there is an error reading data from the underlying stream.
     */
    @Override
    public int read(byte[] data, int offset, int len) throws IOException
    {
        if (eof && index >= n)
        {
            return -1;
        }
        for (int i = 0; i < len; i++)
        {
            if (index < n)
            {
                data[i + offset] = b[index++];
            }
            else
            {
                int t = read();
                if (t == -1)
                {
                    return i;
                }
                data[i + offset] = (byte) t;
            }
        }
        return len;
    }

    /**
     * This will close the underlying stream and release any resources.
     *
     * @throws IOException If there is an error closing the underlying stream.
     */
    @Override
    public void close() throws IOException
    {
        ascii = null;
        eof = true;
        b = null;
        super.close();
    }

    /**
     * non supported interface methods.
     *
     * @return False always.
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }

    /**
     * Unsupported.
     *
     * @param nValue ignored.
     *
     * @return Always zero.
     */
    @Override
    public long skip(long nValue)
    {
        return 0;
    }

    /**
     * Unsupported.
     *
     * @return Always zero.
     */
    @Override
    public int available()
    {
        return 0;
    }

    /**
     * Unsupported.
     *
     * @param readlimit ignored.
     */
    @Override
    public void mark(int readlimit)
    {
    }

    /**
     * Unsupported.
     *
     * @throws IOException telling that this is an unsupported action.
     */
    @Override
    public void reset() throws IOException
    {
        throw new IOException("Reset is not supported");
    }
}
