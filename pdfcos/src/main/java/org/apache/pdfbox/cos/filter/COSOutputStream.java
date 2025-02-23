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

package org.apache.pdfbox.cos.filter;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessOutputStream;
import org.apache.pdfbox.io.RandomAccessStreamCache;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An OutputStream which writes to an encoded COS stream.
 *
 * @author John Hewson
 */
public final class COSOutputStream extends FilterOutputStream
{
    private final List<Filter> filters;
    private final COSDictionary parameters;
    private final RandomAccessStreamCache streamCache;
    private RandomAccess buffer;

    /**
     *      * TODO: fix so that this is in a filter class, using this object as input.
     * Returns a new OutputStream for writing stream data, using the current filters.
     *
     * @return OutputStream for un-encoded stream data.
     * @throws IOException If the output stream could not be created.
     */
//    public OutputStream createOutputStream() throws IOException
//    {
//        return createOutputStream(null);
//    }
//
//    /**
//     * Returns a new OutputStream for writing stream data, using and the given filters.
//     *
//     * @param filters COSArray or COSName of filters to be used.
//     * @return OutputStream for un-encoded stream data.
//     * @throws IOException If the output stream could not be created.
//     */
//    public OutputStream createOutputStream( COSBase filters) throws IOException
//    {
//        checkClosed();
//        if (isWriting)
//        {
//            throw new IllegalStateException("Cannot have more than one open stream writer.");
//        }
//        // apply filters, if any
//        if (filters != null)
//        {
//            setItem(COSName.FILTER, filters);
//        }
//        if (randomAccess != null)
//            randomAccess.clear();
//        else
//            randomAccess = getStreamCache().createBuffer();
//        OutputStream randomOut = new RandomAccessOutputStream( randomAccess);
//        OutputStream cosOut = new COSOutputStream( getFilterList(), this, randomOut,
//                                                   getStreamCache());
//        isWriting = true;
//        return new FilterOutputStream( cosOut)
//        {
//            @Override
//            public void write(byte[] b, int off, int len) throws IOException
//            {
//                this.out.write(b, off, len);
//            }
//
//            @Override
//            public void close() throws IOException
//            {
//                super.close();
//                setInt(COSName.LENGTH, (int)randomAccess.length());
//                isWriting = false;
//            }
//        };
//    }

    /**
     * Creates a new COSOutputStream writes to an encoded COS stream.
     * 
     * @param filters Filters to apply.
     * @param parameters Filter parameters.
     * @param output Encoded stream.
     * @param streamCache Stream cache to use.
     * 
     * @throws IOException If there was an error creating a temporary buffer
     */
    public COSOutputStream( List<Filter> filters, COSDictionary parameters, OutputStream output,
                            RandomAccessStreamCache streamCache ) throws IOException
    {
        super(output);
        this.filters = filters;
        this.parameters = parameters;
        this.streamCache = streamCache;
        buffer = filters.isEmpty() ? null : streamCache.createBuffer();
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
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
        if (buffer == null)
        {
            super.flush();
        }
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            if (buffer != null)
            {
                try
                {
                    // apply filters in reverse order
                    for (int i = filters.size() - 1; i >= 0; i--)
                    {
                        try (InputStream unfilteredIn = new RandomAccessInputStream(buffer))
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
                                RandomAccess filteredBuffer = streamCache.createBuffer();
                                try (OutputStream filteredOut = new RandomAccessOutputStream(filteredBuffer))
                                {
                                    filters.get(i).encode(unfilteredIn, filteredOut, parameters, i);
                                }
                                finally
                                {
                                    buffer.close();
                                    buffer = filteredBuffer;
                                }
                            }
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
