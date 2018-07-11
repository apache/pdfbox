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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessOutputStream;
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
    private final ScratchFile scratchFile;
    private RandomAccess buffer;

    /**
     * Creates a new COSOutputStream writes to an encoded COS stream.
     * 
     * @param filters Filters to apply.
     * @param parameters Filter parameters.
     * @param output Encoded stream.
     * @param scratchFile Scratch file to use.
     * 
     * @throws IOException If there was an error creating a temporary buffer
     */
    COSOutputStream(List<Filter> filters, COSDictionary parameters, OutputStream output,
                    ScratchFile scratchFile) throws IOException
    {
        super(output);
        this.filters = filters;
        this.parameters = parameters;
        this.scratchFile = scratchFile;

        if (filters.isEmpty())
        {
            this.buffer = null;
        }
        else
        {
            this.buffer = scratchFile.createBuffer();
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        if (buffer != null)
        {
            buffer.write(b);
        }
        else
        {
            super.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (buffer != null)
        {
            buffer.write(b, off, len);
        }
        else
        {
            super.write(b, off, len);
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        if (buffer != null)
        {
            buffer.write(b);
        }
        else
        {
            super.write(b);
        }
    }

    @Override
    public void flush() throws IOException
    {
    }

    @Override
    public void close() throws IOException
    {
        try {
            if (buffer != null)
            {
                try
                {
                    // apply filters in reverse order
                    for (int i = filters.size() - 1; i >= 0; i--)
                    {
                        InputStream unfilteredIn = new RandomAccessInputStream(buffer);
                        try
                        {
                            if (i == 0)
                            {
                                /*
                                 * The last filter to run can encode directly to the enclosed output
                                 * stream.
                                 */
                                filters.get(i).encode(unfilteredIn, out, parameters, i);
                            }
                            else
                            {
                                RandomAccess filteredBuffer = scratchFile.createBuffer();
                                try
                                {
                                    OutputStream filteredOut = new RandomAccessOutputStream(filteredBuffer);
                                    try
                                    {
                                        filters.get(i).encode(unfilteredIn, filteredOut, parameters, i);
                                    }
                                    finally
                                    {
                                        filteredOut.close();
                                    }

                                    RandomAccess tmpSwap = filteredBuffer;
                                    filteredBuffer = buffer;
                                    buffer = tmpSwap;
                                }
                                finally
                                {
                                    filteredBuffer.close();
                                }
                            }
                        }
                        finally
                        {
                            unfilteredIn.close();
                        }
                    }
                }
                finally
                {
                    buffer.close();
                    buffer = null;
                }
            }
        }
        finally
        {
            super.close();
        }
    }
}
