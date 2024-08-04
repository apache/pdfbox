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
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stream based decoder for the flate filter which uses zlib/deflate compression.
 * 
 * Use Inflater instead of InflateInputStream to avoid an EOFException due to a probably missing Z_STREAM_END, see
 * PDFBOX-1232 for details.
 * 
 */
public final class FlateFilterDecoderStream extends FilterInputStream
{
    private static final Logger LOG = LogManager.getLogger(FlateFilterDecoderStream.class);

    private boolean isEOF = false;
    private int currentDataIndex = 0;
    private int bytesDecoded = 0;

    private byte[] buffer = new byte[2048];
    private byte[] decodedData = new byte[4096];
    // use nowrap mode to bypass zlib-header and checksum to avoid a DataFormatException
    private final Inflater inflater = new Inflater(true);

    /**
     * Constructor.
     *
     * @param inputStream The input stream to actually read from.
     */
    public FlateFilterDecoderStream(InputStream inputStream) throws IOException
    {
        super(inputStream);
        // skip zlib header
        in.read();
        in.read();
    }

    private boolean fetch() throws IOException
    {
        currentDataIndex = 0;
        if (isEOF || inflater.finished())
        {
            isEOF = true;
            bytesDecoded = 0;
            return false;
        }
        if (inflater.needsInput())
        {
            int bytesRead = in.read(buffer);
            if (bytesRead > -1)
            {
                inflater.setInput(buffer, 0, bytesRead);
            }
            else
            {
                isEOF = true;
                return false;
            }
        }
        try
        {
            // overwrite formerly read bytes
            if (bytesDecoded > 0)
            {
                Arrays.fill(decodedData, 0, bytesDecoded, (byte) 0);
            }
            bytesDecoded = inflater.inflate(decodedData);
        }
        catch (DataFormatException exception)
        {
            isEOF = true;
            // check if some bytes could be read at all
            int countZeros = 0;
            for (int i = 0; i < decodedData.length; i++)
            {
                if (decodedData[i] == 0)
                {
                    countZeros++;
                }
                else
                {
                    countZeros = 0;
                }
            }
            bytesDecoded = decodedData.length - countZeros;
            // don't throw an exception, use the already read data or an empty stream
            LOG.warn("FlateFilter: premature end of stream due to a DataFormatException = {}",
                    exception.getMessage());
            return bytesDecoded > 0;
        }
        return true;
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
        if (isEOF)
        {
            return -1;
        }
        if (currentDataIndex == bytesDecoded && !fetch())
        {
            return -1;
        }
        return decodedData[currentDataIndex++] & 0xFF;
    }

    /**
     * This will read a chunk of data.
     *
     * @param data The buffer to write data to.
     * @param offset The offset into the data stream.
     * @param length The number of byte to attempt to read.
     *
     * @return The number of bytes actually read.
     *
     * @throws IOException If there is an error reading data from the underlying stream.
     */
    @Override
    public int read(byte[] data, int offset, int length) throws IOException
    {
        if (isEOF)
        {
            return -1;
        }
        int numberOfBytesRead = 0;
        while (numberOfBytesRead < length)
        {
            int available = bytesDecoded - currentDataIndex;
            if (available > 0)
            {
                int bytes2Copy = Math.min(length - numberOfBytesRead, available);
                System.arraycopy(decodedData, currentDataIndex, data, numberOfBytesRead + offset,
                        bytes2Copy);
                currentDataIndex += bytes2Copy;
                numberOfBytesRead += bytes2Copy;
            }
            else if (!fetch())
            {
                break;
            }
        }
        return numberOfBytesRead;
    }

    /**
     * This will close the underlying stream and release any resources.
     *
     * @throws IOException If there is an error closing the underlying stream.
     */
    @Override
    public void close() throws IOException
    {
        inflater.end();
        super.close();
    }

    /**
     * mark/reset isn't supported.
     *
     * @return always false.
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }

    /**
     * Unsupported.
     *
     * @param n ignored.
     *
     * @return always zero.
     */
    @Override
    public long skip(long n)
    {
        return 0;
    }

    /**
     * Unsupported.
     *
     * @return always zero.
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
    public synchronized void mark(int readlimit)
    {
    }

    /**
     * Unsupported.
     *
     * @throws IOException always throw as reset is an unsupported feature.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        throw new IOException("reset is not supported");
    }
}
