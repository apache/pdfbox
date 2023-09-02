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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * A PDStream represents a stream in a PDF document. Streams are tied to a single PDF document.
 * 
 * @author Ben Litchfield
 */
public class PDStream implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(PDStream.class);

    private final COSStream stream;
    
    /**
     * Creates a new empty PDStream object.
     * 
     * @param document The document that the stream will be part of.
     */
    public PDStream(PDDocument document)
    {
        stream = document.getDocument().createCOSStream();
    }

    /**
     * Creates a new empty PDStream object.
     *
     * @param document The document that the stream will be part of.
     */
    public PDStream(COSDocument document)
    {
        stream = document.createCOSStream();
    }

    /**
     * Creates a PDStream which wraps the given COSStream.
     * 
     * @param str The stream parameter.
     */
    public PDStream(COSStream str)
    {
        stream = str;
    }

    /**
     * Constructor. Reads all data from the input stream and embeds it into the document. This
     * method closes the InputStream.
     *
     * @param doc The document that will hold the stream.
     * @param input The stream parameter.
     * @throws IOException If there is an error creating the stream in the document.
     */
    public PDStream(PDDocument doc, InputStream input) throws IOException
    {
        this(doc, input, (COSBase)null);
    }
    
    /**
     * Constructor. Reads all data from the input stream and embeds it into the document with the
     * given filter applied. This method closes the InputStream.
     *
     * @param doc The document that will hold the stream.
     * @param input The stream parameter.
     * @param filter Filter to apply to the stream.
     * @throws IOException If there is an error creating the stream in the document.
     */
    public PDStream(PDDocument doc, InputStream input, COSName filter) throws IOException
    {
        this(doc, input, (COSBase)filter);
    }

    /**
     * Constructor. Reads all data from the input stream and embeds it into the document with the
     * given filters applied. This method closes the InputStream.
     *
     * @param doc The document that will hold the stream.
     * @param input The stream parameter.
     * @param filters Filters to apply to the stream.
     * @throws IOException If there is an error creating the stream in the document.
     */
    public PDStream(PDDocument doc, InputStream input, COSArray filters) throws IOException
    {
        this(doc, input, (COSBase)filters);
    }

    /**
     * Constructor. Reads all data from the input stream and embeds it into the document with the
     * given filters applied, if any. This method closes the InputStream.
     */
    private PDStream(PDDocument doc, InputStream input, COSBase filters) throws IOException
    {
        stream = doc.getDocument().createCOSStream();
        try (OutputStream output = stream.createOutputStream(filters))
        {
            IOUtils.copy(input, output);
        }
        finally
        {
            input.close();
        }
    }

    /**
     * Get the cos stream associated with this object.
     *
     * @return The cos object that matches this Java object.
     *
     */
    @Override
    public COSStream getCOSObject()
    {
        return stream;
    }

    /**
     * This will get a stream that can be written to.
     * 
     * @return An output stream to write data to.
     * @throws IOException If an IO error occurs during writing.
     */
    public OutputStream createOutputStream() throws IOException
    {
        return stream.createOutputStream();
    }

    /**
     * This will get a stream that can be written to, with the given filter.
     *
     * @param filter the filter to be used.
     * @return An output stream to write data to.
     * @throws IOException If an IO error occurs during writing.
     */
    public OutputStream createOutputStream(COSName filter) throws IOException
    {
        return stream.createOutputStream(filter);
    }

    /**
     * This will get a stream that can be read from.
     * 
     * @return An input stream that can be read from.
     * @throws IOException If an IO error occurs during reading.
     */
    public COSInputStream createInputStream() throws IOException
    {
        return stream.createInputStream();
    }

    public COSInputStream createInputStream(DecodeOptions options) throws IOException
    {
        return stream.createInputStream(options);
    }

    /**
     * This will get a stream with some filters applied but not others. This is
     * useful when doing images, ie filters = [flate,dct], we want to remove
     * flate but leave dct
     * 
     * @param stopFilters  A list of filters to stop decoding at.
     * @return A stream with decoded data.
     * @throws IOException If there is an error processing the stream.
     */
    public InputStream createInputStream(List<String> stopFilters) throws IOException
    {
        InputStream is = stream.createRawInputStream();
        List<Filter> someFilters = new ArrayList<>();
        List<COSName> filters = getFilters();
        for (COSName nextFilter : filters)
        {
            if (stopFilters != null && stopFilters.contains(nextFilter.getName()))
            {
                break;
            }
            someFilters.add(FilterFactory.INSTANCE.getFilter(nextFilter));
        }
        if (someFilters.isEmpty())
        {
            return is;
        }
        RandomAccessRead decoded = Filter.decode(is, someFilters, getCOSObject(),
                DecodeOptions.DEFAULT, null);
        return new RandomAccessInputStream(decoded);
    }

    /**
     * This will get the length of the filtered/compressed stream. This is
     * readonly in the PD Model and will be managed by this class.
     * 
     * @return The length of the filtered stream.
     */
    public int getLength()
    {
        return stream.getInt(COSName.LENGTH, 0);
    }

    /**
     * This will get the list of filters that are associated with this stream.
     * 
     * @return A (possibly empty) list of all encoding filters to apply to this stream, never null.
     */
    public List<COSName> getFilters()
    {
        COSBase filters = stream.getFilters();
        if (filters instanceof COSName)
        {
            return Collections.singletonList((COSName) filters);
        } 
        else if (filters instanceof COSArray)
        {
            return (List<COSName>)((COSArray) filters).toList();
        }
        return Collections.emptyList();
    }

    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters The filters that are part of this stream.
     */
    public void setFilters(List<COSName> filters)
    {
        stream.setItem(COSName.FILTER, new COSArray(filters));
    }

    /**
     * Get the list of decode parameters. Each entry in the list will refer to
     * an entry in the filters list.
     * 
     * @return The list of decode parameters.
     * @throws IOException if there is an error retrieving the parameters.
     */
    public List<Object> getDecodeParms() throws IOException
    {
        // See PDF Ref 1.5 implementation note 7, /DP is sometimes used instead.
        return internalGetDecodeParams(COSName.DECODE_PARMS, COSName.DP);
    }

    /**
     * Get the list of decode parameters. Each entry in the list will refer to
     * an entry in the filters list.
     * 
     * @return The list of decode parameters.
     * @throws IOException if there is an error retrieving the parameters.
     */
    public List<Object> getFileDecodeParams() throws IOException
    {
        return internalGetDecodeParams(COSName.F_DECODE_PARMS, null);
    }

    private List<Object> internalGetDecodeParams(COSName name1, COSName name2) throws IOException
    {
        COSBase dp = stream.getDictionaryObject(name1, name2);

        if (dp instanceof COSDictionary)
        {
            Map<?, ?> map = COSDictionaryMap.convertBasicTypesToMap((COSDictionary) dp);
            return new COSArrayList<>(map, dp, stream, name1);
        }

        if (dp instanceof COSArray)
        {
            COSArray array = (COSArray) dp;
            List<Object> actuals = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++)
            {
                COSBase base = array.getObject(i);
                if (base instanceof COSDictionary)
                {
                    actuals.add(COSDictionaryMap.convertBasicTypesToMap((COSDictionary) base));
                }
                else
                {
                    LOG.warn("Expected COSDictionary, got " + base + ", ignored");
                }
            }
            return new COSArrayList<>(actuals, array);
        }

        return null;
    }

    /**
     * This will set the list of decode parameters.
     * 
     * @param decodeParams The list of decode parameters.
     */
    public void setDecodeParms(List<?> decodeParams)
    {
        stream.setItem(COSName.DECODE_PARMS,
                COSArrayList.converterToCOSArray(decodeParams));
    }

    /**
     * This will get the file specification for this stream. This is only
     * required for external files.
     * 
     * @return The file specification.
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        COSBase f = stream.getDictionaryObject(COSName.F);
        return PDFileSpecification.createFS(f);
    }

    /**
     * Set the file specification.
     * 
     * @param f The file specification.
     */
    public void setFile(PDFileSpecification f)
    {
        stream.setItem(COSName.F, f);
    }

    /**
     * This will get the list of filters that are associated with this stream. The list is empty if there are none.
     * 
     * @return A list of all encoding filters to apply to this stream.
     */
    public List<String> getFileFilters()
    {
        COSBase filters = stream.getDictionaryObject(COSName.F_FILTER);
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            return Collections.singletonList(name.getName());
        }
        else if (filters instanceof COSArray)
        {
            return ((COSArray) filters).toCOSNameStringList();
        }
        return Collections.emptyList();
    }

    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters The filters that are part of this stream.
     */
    public void setFileFilters(List<String> filters)
    {
        COSBase obj = COSArray.ofCOSNames(filters);
        stream.setItem(COSName.F_FILTER, obj);
    }

    /**
     * This will set the list of decode params.
     * 
     * @param decodeParams The list of decode params.
     */
    public void setFileDecodeParams(List<?> decodeParams)
    {
        stream.setItem(COSName.F_DECODE_PARMS, COSArrayList.converterToCOSArray(decodeParams));
    }

    /**
     * This will copy the stream into a byte array.
     * 
     * @return The byte array of the filteredStream.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] toByteArray() throws IOException
    {
        try (InputStream is = createInputStream())
        {
            return is.readAllBytes();
        }
    }
    
    /**
     * Get the metadata that is part of the document catalog. This will return
     * null if there is no meta data for this object.
     * 
     * @return The metadata for this object.
     * @throws IllegalStateException if the value of the metadata entry is different from a stream
     *                               or null
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSBase mdStream = stream.getDictionaryObject(COSName.METADATA);
        if (mdStream instanceof COSStream)
        {
            retval = new PDMetadata((COSStream) mdStream);
        } 
        else if (mdStream instanceof COSNull)
        {
            // null is authorized
        } 
        else if (mdStream != null)
        {
            throw new IllegalStateException("Expected a COSStream but was a "
                            + mdStream.getClass().getSimpleName());
        }
        return retval;
    }

    /**
     * Set the metadata for this object. This can be null.
     * 
     * @param meta The meta data for this object.
     */
    public void setMetadata(PDMetadata meta)
    {
        stream.setItem(COSName.METADATA, meta);
    }

    /**
     * Get the decoded stream length.
     *
     * @return the decoded stream length
     */
    public int getDecodedStreamLength()
    {
        return this.stream.getInt(COSName.DL);
    }

    /**
     * Set the decoded stream length.
     *
     * @param decodedStreamLength the decoded stream length
     */
    public void setDecodedStreamLength(int decodedStreamLength)
    {
        this.stream.setInt(COSName.DL, decodedStreamLength);
    }
}
