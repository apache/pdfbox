/**
 * Copyright (c) 2003, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.filter;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pdfbox.cos.COSName;

/**
 * This will contain manage all the different types of filters that are available.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.13 $
 */
public class FilterManager
{
    private Map filters = new HashMap();

    /**
     * Constructor.
     */
    public FilterManager()
    {
        Filter flateFilter = new FlateFilter();
        Filter dctFilter = new DCTFilter();
        Filter ccittFaxFilter = new CCITTFaxDecodeFilter();
        Filter lzwFilter = new LZWFilter();
        Filter asciiHexFilter = new ASCIIHexFilter();
        Filter ascii85Filter = new ASCII85Filter();
        Filter runLengthFilter = new RunLengthDecodeFilter();

        addFilter( COSName.FLATE_DECODE, flateFilter );
        addFilter( COSName.FLATE_DECODE_ABBREVIATION, flateFilter );
        addFilter( COSName.DCT_DECODE, dctFilter );
        addFilter( COSName.DCT_DECODE_ABBREVIATION, dctFilter );
        addFilter( COSName.CCITTFAX_DECODE, ccittFaxFilter );
        addFilter( COSName.CCITTFAX_DECODE_ABBREVIATION, ccittFaxFilter );
        addFilter( COSName.LZW_DECODE, lzwFilter );
        addFilter( COSName.LZW_DECODE_ABBREVIATION, lzwFilter );
        addFilter( COSName.ASCII_HEX_DECODE, asciiHexFilter );
        addFilter( COSName.ASCII_HEX_DECODE_ABBREVIATION, asciiHexFilter );
        addFilter( COSName.ASCII85_DECODE, ascii85Filter );
        addFilter( COSName.ASCII85_DECODE_ABBREVIATION, ascii85Filter );
        addFilter( COSName.RUN_LENGTH_DECODE, runLengthFilter );
        addFilter( COSName.RUN_LENGTH_DECODE_ABBREVIATION, runLengthFilter );

    }

    /**
     * This will get all of the filters that are available in the system.
     *
     * @return All available filters in the system.
     */
    public Collection getFilters()
    {
        return filters.values();
    }

    /**
     * This will add an available filter.
     *
     * @param filterName The name of the filter.
     * @param filter The filter to use.
     */
    public void addFilter( COSName filterName, Filter filter )
    {
        filters.put( filterName, filter );
    }

    /**
     * This will get a filter by name.
     *
     * @param filterName The name of the filter to retrieve.
     *
     * @return The filter that matches the name.
     *
     * @throws IOException If the filter could not be found.
     */
    public Filter getFilter( COSName filterName ) throws IOException
    {
        Filter filter = (Filter)filters.get( filterName );
        if( filter == null )
        {
            throw new IOException( "Unknown stream filter:" + filterName );
        }

        return filter;
    }
    
    /**
     * This will get a filter by name.
     *
     * @param filterName The name of the filter to retrieve.
     *
     * @return The filter that matches the name.
     *
     * @throws IOException If the filter could not be found.
     */
    public Filter getFilter( String filterName ) throws IOException
    {
        return getFilter( COSName.getPDFName( filterName ) );
    }
}