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
import java.io.PushbackInputStream;

/**
 * A RandomAccessRead backed by an InputStream.
 */
public final class InputStreamRandomAccessRead implements RandomAccessRead
{
    private final PushbackInputStream input;
    private int position;
    private boolean isClosed = false;

    /**
     * Constructor.
     * 
     * @param input The input stream to wrap.
     */
    public InputStreamRandomAccessRead(InputStream input)
    {
        this.input = new PushbackInputStream(input, 32767); // maximum length of a PDF string
        this.position = 0;
    }

    @Override
    public int read() throws IOException
    {
        int b = input.read();
        position++;
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        int n = input.read(b);
        if (n > 0)
        {
            position += n;
            return n;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        int n = input.read(b, offset, length);
        if (n > 0)
        {
            position += n;
            return n;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getPosition() throws IOException
    {
        return position;
    }

    @Override
    public int peek() throws IOException
    {
        int b = input.read();
        if (b != -1)
        {
            input.unread(b);
        }
        return b;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return peek() == -1;
    }

    @Override
    public void close() throws IOException
    {
        input.close();
        isClosed = true;
    }


    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    @Override
    public int available() throws IOException
    {
        return input.available();
    }

    @Override
    public void unread(int b) throws IOException
    {
        input.unread(b);
        position--;
    }

    @Override
    public void unread(byte[] bytes) throws IOException
    {
        input.unread(bytes);
        position -= bytes.length;
    }

    @Override
    public void unread(byte[] bytes, int start, int len) throws IOException
    {
        input.unread(bytes, start, len);
        position -= len;
    }

    @Override
    public boolean seekSupported()
    {
        return false;
    }

    @Override
    public void rewind(int bytes) throws IOException
    {
        throw new UnsupportedOperationException(getClass().getName() + ".rewind is not supported!");
    }

    @Override
    public void seek(long position) throws IOException
    {
        throw new UnsupportedOperationException(getClass().getName() + ".seek is not supported!");
    }

    @Override
    public long length() throws IOException
    {
        throw new UnsupportedOperationException(getClass().getName() + ".length is not supported!");
    }
}
