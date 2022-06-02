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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;

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
            return new COSInputStream(in, Collections.<DecodeResult>emptyList());
        }

        List<DecodeResult> results = new ArrayList<>(filters.size());
        InputStream input = in;
        if (filters.size() > 1)
        {
            Set<Filter> filterSet = new HashSet<>(filters);
            if (filterSet.size() != filters.size())
            {
                throw new IOException("Duplicate");
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // apply filters
        for (int i = 0; i < filters.size(); i++)
        {
            output.reset();
            results.add(filters.get(i).decode(input, output, parameters, i, options));
            input = new ByteArrayInputStream(output.toByteArray());
        }
        return new COSInputStream(input, results);
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
