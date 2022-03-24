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

import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An InputStream which reads from a RandomAccessRead.
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class RandomAccessInputStream extends InputStream
{
    private static final Log LOG = LogFactory.getLog(RandomAccessInputStream.class);

    private final RandomAccessRead input;
    private long position;

    /**
     * Creates a new RandomAccessInputStream, with a position of zero. The InputStream will maintain
     * its own position independent of the RandomAccessRead.
     *
     * @param randomAccessRead The RandomAccessRead to read from.
     */
    public RandomAccessInputStream(RandomAccessRead randomAccessRead)
    {
        input = randomAccessRead;
        position = 0;
    }

    void restorePosition() throws IOException
    {
        input.seek(position);
    }
    
    @Override
    public int available() throws IOException
    {
        return (int) Math.max(0, Math.min(input.length() - position, Integer.MAX_VALUE));
    }

    @Override
    public int read() throws IOException
    {
        restorePosition();
        if (input.isEOF())
        {
            return -1;
        }
        int b = input.read();
        if (b != -1)
        {
            position += 1;
        }
        else
        {
            // should never happen due to prior isEOF() check
            // unless there is an unsynchronized concurrent access
            LOG.error("read() returns -1, assumed position: " +
                       position + ", actual position: " + input.getPosition());
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        restorePosition();
        if (input.isEOF())
        {
            return -1;
        }
        int n = input.read(b, off, len);
        if (n != -1)
        {
            position += n;
        }
        else
        {
            // should never happen due to prior isEOF() check
            // unless there is an unsynchronized concurrent access
            LOG.error("read() returns -1, assumed position: " +
                       position + ", actual position: " + input.getPosition());
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (n <= 0)
        {
            return 0;
        }
        restorePosition();
        input.seek(position + n);
        position += n;
        return n;
    }
}
