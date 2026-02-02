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
import java.io.OutputStream;

/**
 * An OutputStream which writes to a RandomAccessWrite.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class RandomAccessOutputStream extends OutputStream
{
    private final RandomAccessWrite writer;

    /**
     * Constructor to create a new output stream which writes to the given RandomAccessWrite.
     *
     * @param writer The random access writer for output
     */
    public RandomAccessOutputStream(RandomAccessWrite writer)
    {
        this.writer = writer;
        // we don't have to maintain a position, as each COSStream can only have one writer.
    }
    
    @Override
    public void write(byte[] b, int offset, int length) throws IOException
    {
        writer.write(b, offset, length);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        writer.write(b);
    }

    @Override
    public void write(int b) throws IOException
    {
        writer.write(b);
    }
}
