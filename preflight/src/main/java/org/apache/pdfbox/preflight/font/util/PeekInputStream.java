/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class PeekInputStream extends InputStream
{
    private byte[] content = new byte[0];
    private int position = 0;

    public PeekInputStream(InputStream source) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            IOUtils.copyLarge(source, bos);
            content = bos.toByteArray();
        }
        finally
        {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(bos);
        }
    }

    @Override
    public int read() throws IOException
    {
        if (position >= content.length)
        {
            throw new IOException("No more content in this stream");
        }

        int currentByte = (content[position] & 0xFF);
        ++position;
        return currentByte;
    }

    public int peek() throws IOException
    {
        if (position >= content.length)
        {
            throw new IOException("No more content in this stream");
        }

        return (content[position] & 0xFF);
    }

    public byte[] peek(int numberOfBytes) throws IOException
    {
        if (numberOfBytes < 0 || (position + numberOfBytes) >= content.length)
        {
            throw new IOException("No more content in this stream, can't return the next " + numberOfBytes + " bytes");
        }

        byte[] nextBytes = new byte[numberOfBytes];
        System.arraycopy(this.content, this.position, nextBytes, 0, numberOfBytes);
        return nextBytes;
    }
}
