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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSName;

/**
 * Factory for Filter classes.
 *
 * @author Ben Litchfield
 */
public final class FilterFactory
{
    /**
     * Singleton instance.
     */
    public static final FilterFactory INSTANCE = new FilterFactory();

    private final Map<COSName, Filter> filters = new HashMap<>();

    private FilterFactory()
    {
        Filter flate = new FlateFilter();
        Filter dct = new DCTFilter();
        Filter ccittFax = new CCITTFaxFilter();
        Filter lzw = new LZWFilter();
        Filter asciiHex = new ASCIIHexFilter();
        Filter ascii85 = new ASCII85Filter();
        Filter runLength = new RunLengthDecodeFilter();
        Filter crypt = new CryptFilter();
        Filter jpx = new JPXFilter();
        Filter jbig2 = new JBIG2Filter();

        filters.put(COSName.FLATE_DECODE, flate);
        filters.put(COSName.FLATE_DECODE_ABBREVIATION, flate);
        filters.put(COSName.DCT_DECODE, dct);
        filters.put(COSName.DCT_DECODE_ABBREVIATION, dct);
        filters.put(COSName.CCITTFAX_DECODE, ccittFax);
        filters.put(COSName.CCITTFAX_DECODE_ABBREVIATION, ccittFax);
        filters.put(COSName.LZW_DECODE, lzw);
        filters.put(COSName.LZW_DECODE_ABBREVIATION, lzw);
        filters.put(COSName.ASCII_HEX_DECODE, asciiHex);
        filters.put(COSName.ASCII_HEX_DECODE_ABBREVIATION, asciiHex);
        filters.put(COSName.ASCII85_DECODE, ascii85);
        filters.put(COSName.ASCII85_DECODE_ABBREVIATION, ascii85);
        filters.put(COSName.RUN_LENGTH_DECODE, runLength);
        filters.put(COSName.RUN_LENGTH_DECODE_ABBREVIATION, runLength);
        filters.put(COSName.CRYPT, crypt);
        filters.put(COSName.JPX_DECODE, jpx);
        filters.put(COSName.JBIG2_DECODE, jbig2);
    }

    /**
     * Returns a filter instance given its name as a string.
     * @param filterName the name of the filter to retrieve
     * @return the filter that matches the name
     * @throws IOException if the filter name was invalid
     */
    public Filter getFilter(String filterName) throws IOException
    {
        return getFilter(COSName.getPDFName(filterName));
    }

    /**
     * Returns a filter instance given its COSName.
     * @param filterName the name of the filter to retrieve
     * @return the filter that matches the name
     * @throws IOException if the filter name was invalid
     */
    public Filter getFilter(COSName filterName) throws IOException
    {
        Filter filter = filters.get(filterName);
        if (filter == null)
        {
            throw new IOException("Invalid filter: " + filterName);
        }
        return filter;
    }

    // returns all available filters, for testing
    Collection<Filter> getAllFilters()
    {
        return filters.values();
    }
}
