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

public class RandomAccessReadView implements RandomAccessRead
{
    private RandomAccessRead randomAccessRead;
    private final long startPosition;
    private final long streamLength;
    private long currentPosition = 0;
    
    public RandomAccessReadView(RandomAccessRead randomAccessRead, long startPosition,
            long streamLength)
    {
        this.randomAccessRead = randomAccessRead;
        this.startPosition = startPosition;
        this.streamLength = streamLength;
    }

    @Override
    public long getPosition()
    {
        return currentPosition;
    }
    
    @Override
    public void seek( final long newOffset ) throws IOException
    {
        if (newOffset < streamLength)
        {
            randomAccessRead.seek(startPosition + newOffset);
            currentPosition = newOffset;
        }
    }
    
    @Override
    public int read() throws IOException
    {
        if (currentPosition >= streamLength)
        {
            return -1;
        }
        restorePosition();
        int readValue = randomAccessRead.read();
        if (readValue > -1)
        {
            currentPosition++;
        }
        return readValue;
    }
    
    @Override
    public int read(byte[] b) throws IOException
    {
        if (currentPosition >= streamLength)
        {
            return 0;
        }
        restorePosition();
        return read(b, 0, b.length);
    }
    
    @Override
    public int read( byte[] b, int off, int len ) throws IOException
    {
        restorePosition();
        int readBytes = randomAccessRead.read(b, off, Math.min(len, available()));
        currentPosition += readBytes;
        return readBytes;
    }
    
    @Override
    public int available() throws IOException
    {
        return (int) (streamLength - currentPosition);
    }
    
    @Override
    public long length() throws IOException
    {
        return streamLength;
    }
    
    @Override
    public void close() throws IOException
    {
        randomAccessRead = null;
    }

    @Override
    public boolean isClosed()
    {
        return randomAccessRead == null || randomAccessRead.isClosed();
    }

    @Override
    public int peek() throws IOException
    {
        restorePosition();
        return randomAccessRead.peek();
    }

    @Override
    public void rewind(int bytes) throws IOException
    {
        restorePosition();
        randomAccessRead.rewind(bytes);
        currentPosition -= bytes;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return currentPosition >= streamLength;
    }

    private void restorePosition() throws IOException
    {
        randomAccessRead.seek(startPosition + currentPosition);
    }
}
