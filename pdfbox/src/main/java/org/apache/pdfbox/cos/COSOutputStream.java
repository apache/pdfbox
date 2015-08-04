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

package org.apache.pdfbox.cos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.io.ScratchFile;

/**
 * An OutputStream which writes to an encoded COS stream.
 *
 * @author John Hewson
 */
public final class COSOutputStream extends FilterOutputStream
{
    private final List<Filter> filters;
    private final COSDictionary parameters;
    // todo: this is an in-memory buffer, should use scratch file (if any) instead
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    /**
     * Creates a new COSOutputStream writes to an encoded COS stream.
     * 
     * @param filters Filters to apply.
     * @param parameters Filter parameters.
     * @param output Encoded stream.
     * @param scratchFile Scratch file to use, or null.
     */
    COSOutputStream(List<Filter> filters, COSDictionary parameters, OutputStream output,
                    ScratchFile scratchFile)
    {
        super(output);
        this.filters = filters;
        this.parameters = parameters;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        buffer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        buffer.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException
    {
        buffer.write(b);
    }

    @Override
    public void flush() throws IOException
    {
    }
    
    @Override
    public void close() throws IOException
    {
        // apply filters in reverse order
        for (int i = filters.size() - 1; i >= 0; i--)
        {
            // todo: this is an in-memory buffer, should use scratch file (if any) instead
            ByteArrayInputStream input = new ByteArrayInputStream(buffer.toByteArray());
            buffer = new ByteArrayOutputStream();
            filters.get(i).encode(input, buffer, parameters, i);
        }
        // flush the entire stream
        out.write(buffer.toByteArray());
        super.close();
    }
}
