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

package org.apache.fontbox_ai2.ttf;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for a TTF stream inside a TTC file, does not close the underlying shared stream.
 *
 * @author John Hewson
 */
class TTCDataStream extends TTFDataStream
{
    private final TTFDataStream stream; 
    
    TTCDataStream(TTFDataStream stream)
    {
        this.stream = stream;
    }
    
    @Override
    public int read() throws IOException
    {
        return stream.read();
    }

    @Override
    public long readLong() throws IOException
    {
        return stream.readLong();
    }

    @Override
    public int readUnsignedShort() throws IOException
    {
        return stream.readUnsignedShort();
    }

    @Override
    public short readSignedShort() throws IOException
    {
        return stream.readSignedShort();
    }

    @Override
    public void close() throws IOException
    {
        // don't close the underlying stream, as it is shared by all fonts from the same TTC
        // TrueTypeCollection.close() must be called instead
    }

    @Override
    public void seek(long pos) throws IOException
    {
        stream.seek(pos);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return stream.read(b, off, len);
    }

    @Override
    public long getCurrentPosition() throws IOException
    {
        return stream.getCurrentPosition();
    }

    @Override
    public InputStream getOriginalData() throws IOException
    {
        return stream.getOriginalData();
    }
}
