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

package org.apache.pdfbox.pdfparser;

import java.io.IOException;
import org.apache.pdfbox.io.RandomAccessRead;

/**
 * A SequentialSource backed by a RandomAccessRead.
 */
final class RandomAccessSource implements SequentialSource
{
    private final RandomAccessRead reader;

    /**
     * Constructor.
     * 
     * @param reader The random access reader to wrap.
     */
    RandomAccessSource(RandomAccessRead reader)
    {
        this.reader = reader;
    }

    @Override
    public int read() throws IOException
    {
        return reader.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return reader.read(b);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        return reader.read(b, offset, length);
    }

    @Override
    public long getPosition() throws IOException
    {
        return reader.getPosition();
    }

    @Override
    public int peek() throws IOException
    {
        return reader.peek();
    }

    @Override
    public void unread(int b) throws IOException
    {
        reader.rewind(1);
    }

    @Override
    public void unread(byte[] bytes) throws IOException
    {
        reader.rewind(bytes.length);
    }

    @Override
    public void unread(byte[] bytes, int start, int len) throws IOException
    {
        reader.rewind(len - start);
    }

    @Override
    public byte[] readFully(int length) throws IOException
    {
        return reader.readFully(length);
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return reader.isEOF();
    }

    @Override
    public void close() throws IOException
    {
        reader.close();
    }
}
