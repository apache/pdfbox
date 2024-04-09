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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper class to combine several RandomAccessRead instances so that they can be accessed as one big RandomAccessRead.
 */
public class SequenceRandomAccessRead implements RandomAccessRead
{
    private final List<RandomAccessRead> readerList;
    private final long[] startPositions;
    private final long[] endPositions;
    private final int numberOfReader;
    private int currentIndex = 0;
    private long currentPosition = 0;
    private long totalLength = 0;
    private boolean isClosed = false;
    private RandomAccessRead currentRandomAccessRead = null;
    
    public SequenceRandomAccessRead(List<RandomAccessRead> randomAccessReadList)
    {
        if (randomAccessReadList == null)
        {
            throw new IllegalArgumentException("Missing input parameter");
        }
        if (randomAccessReadList.isEmpty())
        {
            throw new IllegalArgumentException("Empty list");
        }
        readerList = randomAccessReadList.stream() //
                .filter(r -> {
                    try
                    {
                        return r.length() > 0;
                    }
                    catch (IOException e)
                    {
                        throw new IllegalArgumentException("Problematic list", e);
                    }
                }).collect(Collectors.toList());
        currentRandomAccessRead = readerList.get(currentIndex);
        numberOfReader = readerList.size();
        startPositions = new long[numberOfReader];
        endPositions = new long[numberOfReader];
        for(int i=0;i<numberOfReader;i++) 
        {
            try
            {
                startPositions[i] = totalLength;
                totalLength += readerList.get(i).length();
                endPositions[i] = totalLength - 1;
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Problematic list", e);
            }
        }
    }

    @Override
    public void close() throws IOException
    {
        for (RandomAccessRead randomAccessRead : readerList)
        {
            randomAccessRead.close();
        }
        readerList.clear();
        currentRandomAccessRead = null;
        isClosed = true;
    }

    private RandomAccessRead getCurrentReader() throws IOException
    {
        if (currentRandomAccessRead.isEOF() && currentIndex < numberOfReader - 1)
        {
            currentIndex++;
            currentRandomAccessRead = readerList.get(currentIndex);
            currentRandomAccessRead.seek(0);
        }
        return currentRandomAccessRead;
    }

    @Override
    public int read() throws IOException
    {
        checkClosed();
        RandomAccessRead randomAccessRead = getCurrentReader();
        int value = randomAccessRead.read();
        if (value > -1)
        {
            currentPosition++;
        }
        return value;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        int maxAvailBytes = Math.min(available(), length);
        if (maxAvailBytes == 0)
        {
            return -1;
        }
        RandomAccessRead randomAccessRead = getCurrentReader();
        int bytesRead = randomAccessRead.read(b, offset, maxAvailBytes);
        while (bytesRead > -1 && bytesRead < maxAvailBytes)
        {
            randomAccessRead = getCurrentReader();
            bytesRead += randomAccessRead.read(b, offset + bytesRead, maxAvailBytes - bytesRead);
        }
        currentPosition += bytesRead;
        return bytesRead;
    }

    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return currentPosition;
    }

    @Override
    public void seek(long position) throws IOException
    {
        checkClosed();
        if (position < 0)
        {
            throw new IOException("Invalid position " + position);
        }
        // it is allowed to jump beyond the end of the file
        // jump to the end of the reader
        if (position >= totalLength)
        {
            currentIndex = numberOfReader - 1;
            currentPosition = totalLength;
        }
        else
        {
            // search forward/backwards if the new position is after/before the current position
            int increment = position < currentPosition ? -1 : 1;
            for (int i = currentIndex; i < numberOfReader && i >= 0; i += increment)
            {
                if (position >= startPositions[i] && position <= endPositions[i])
                {
                    currentIndex = i;
                    break;
                }
            }
            currentPosition = position;
        }
        currentRandomAccessRead = readerList.get(currentIndex);
        currentRandomAccessRead.seek(currentPosition - startPositions[currentIndex]);
    }

    @Override
    public long length() throws IOException
    {
        checkClosed();
        return totalLength;
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    /**
     * Ensure that the SequenceRandomAccessRead is not closed
     * 
     * @throws IOException If RandomAccessBuffer already closed
     */
    private void checkClosed() throws IOException
    {
        if (isClosed)
        {
            // consider that the rab is closed if there is no current buffer
            throw new IOException("RandomAccessBuffer already closed");
        }
    }

    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return currentPosition >= totalLength;
    }

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException
    {
        throw new UnsupportedOperationException(getClass().getName() + ".createView isn't supported.");
    }

}
