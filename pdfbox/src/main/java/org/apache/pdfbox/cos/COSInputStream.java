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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessRead;

/**
 * An InputStream which reads from an encoded COS stream.
 *
 * @author John Hewson
 */
public final class COSInputStream extends FilterInputStream
{
    /**
     * Creates a new COSInputStream from an encoded input stream.
     *
     * @param filters Filters to be applied.
     * @param parameters Filter parameters.
     * @param in Encoded input stream.
     * @return Decoded stream.
     * @throws IOException If the stream could not be read.
     */
    static COSInputStream create(List<Filter> filters, COSDictionary parameters, InputStream in)
            throws IOException
    {
        return create(filters, parameters, in, DecodeOptions.DEFAULT);
    }

    /**
     * Creates a new COSInputStream from an encoded input stream.
     *
     * @param filters Filters to be applied.
     * @param parameters Filter parameters.
     * @param in Encoded input stream.
     * @param options decode options for the encoded stream
     * @return Decoded stream.
     * @throws IOException If the stream could not be read.
     */
    static COSInputStream create(List<Filter> filters, COSDictionary parameters, InputStream in,
            DecodeOptions options) throws IOException
    {
        if (filters.isEmpty())
        {
            return new COSInputStream(in, Collections.emptyList());
        }
        List<DecodeResult> results = new ArrayList<>(filters.size());
        RandomAccessRead decoded = Filter.decode(in, filters, parameters, options, results);
        return new COSInputStream(new RandomAccessInputStream(decoded), results);
    }

    private final List<DecodeResult> decodeResults;

    /**
     * Constructor.
     * 
     * @param input decoded stream
     * @param decodeResults results of decoding
     */
    private COSInputStream(InputStream input, List<DecodeResult> decodeResults)
    {
        super(input);
        this.decodeResults = decodeResults;
    }
    
    /**
     * Returns the result of the last filter, for use by repair mechanisms.
     * 
     * @return the result of the last filter
     */
    public DecodeResult getDecodeResult()
    {
        if (decodeResults.isEmpty())
        {
            return DecodeResult.createDefault();
        }
        else
        {
            return decodeResults.get(decodeResults.size() - 1);
        }
    }
}
