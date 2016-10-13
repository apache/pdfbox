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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * A PDStream represents a stream in a PDF document. Streams are tied to a single PDF document.
 * 
 * @author Ben Litchfield
 */
public class PDStream implements COSObjectable
{
    private final COSStream stream;
    
    /**
     * Creates a new PDStream object.
     * 
     * @param document The document that the stream will be part of.
     */
    public PDStream(PDDocument document)
    {
        stream = document.getDocument().createCOSStream();
    }

    /**
     * Creates a new PDStream object.
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
        OutputStream output = null;
        try
        {
            stream = doc.getDocument().createCOSStream();
            output = stream.createOutputStream(filters);
            IOUtils.copy(input, output);
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
            if (input != null)
            {
                input.close();
            }
        }
    }

    /**
     * If there are not compression filters on the current stream then this will
     * add a compression filter, flate compression for example.
     * 
     * @deprecated This method is inefficient. To copying an existing InputStream, use
     *             {@link #PDStream(PDDocument, InputStream, COSName)} instead, with
     *             COSName.FLATE_DECODE as the final argument.
     *             
     *             Otherwise, to write new compressed data, use {@link #createOutputStream(COSName)},
     *             with COSName.FLATE_DECODE as the argument.
     */
    @Deprecated
    public void addCompression()
    {
        List<COSName> filters = getFilters();
        if (filters == null)
        {
            if (stream.getLength() > 0)
            {
                OutputStream out = null;
                try
                {
                    byte[] bytes = IOUtils.toByteArray(stream.createInputStream());
                    out = stream.createOutputStream(COSName.FLATE_DECODE);
                    out.write(bytes);
                }
                catch (IOException e)
                {
                    // not much else we can do here without breaking the existing API, sorry.
                    throw new RuntimeException(e);
                }
                finally
                {
                    IOUtils.closeQuietly(out);
                }
            }
            else
            {
                filters = new ArrayList<COSName>();
                filters.add(COSName.FLATE_DECODE);
                setFilters(filters);
            }
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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<COSName> filters = getFilters();
        if (filters != null)
        {
            for (int i = 0; i < filters.size(); i++)
            {
                COSName nextFilter = filters.get(i);
                if ((stopFilters != null) && stopFilters.contains(nextFilter.getName()))
                {
                    break;
                }
                else
                {
                    Filter filter = FilterFactory.INSTANCE.getFilter(nextFilter);
                    filter.decode(is, os, stream, i);
                    IOUtils.closeQuietly(is);
                    is = new ByteArrayInputStream(os.toByteArray());
                    os.reset();
                }
            }
        }
        return is;
    }

    /**
     * Get the cos stream associated with this object.
     *
     * @return The cos object that matches this Java object.
     * 
     * @deprecated use {@link #getCOSObject() }
     */
    @Deprecated
    public COSStream getStream()
    {
        return stream;
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
     * Or null if there are none.
     * 
     * @return A list of all encoding filters to apply to this stream.
     */
    public List<COSName> getFilters()
    {
        List<COSName> retval = null;
        COSBase filters = stream.getFilters();
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            retval = new COSArrayList<COSName>(name, name, stream, COSName.FILTER);
        } 
        else if (filters instanceof COSArray)
        {
            retval = (List<COSName>) ((COSArray) filters).toList();
        }
        return retval;
    }

    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters The filters that are part of this stream.
     */
    public void setFilters(List<COSName> filters)
    {
        COSBase obj = COSArrayList.converterToCOSArray(filters);
        stream.setItem(COSName.FILTER, obj);
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
        List<Object> retval = null;

        COSBase dp = stream.getDictionaryObject(COSName.DECODE_PARMS);
        if (dp == null)
        {
            // See PDF Ref 1.5 implementation note 7, the DP is sometimes used
            // instead.
            dp = stream.getDictionaryObject(COSName.DP);
        }
        if (dp instanceof COSDictionary)
        {
            Map<?, ?> map = COSDictionaryMap
                    .convertBasicTypesToMap((COSDictionary) dp);
            retval = new COSArrayList<Object>(map, dp, stream,
                    COSName.DECODE_PARMS);
        } 
        else if (dp instanceof COSArray)
        {
            COSArray array = (COSArray) dp;
            List<Object> actuals = new ArrayList<Object>();
            for (int i = 0; i < array.size(); i++)
            {
                actuals.add(COSDictionaryMap
                        .convertBasicTypesToMap((COSDictionary) array
                                .getObject(i)));
            }
            retval = new COSArrayList<Object>(actuals, array);
        }

        return retval;
    }

    /**
     * This will set the list of decode parameterss.
     * 
     * @param decodeParams The list of decode parameterss.
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
     * This will get the list of filters that are associated with this stream.
     * Or null if there are none.
     * 
     * @return A list of all encoding filters to apply to this stream.
     */
    public List<String> getFileFilters()
    {
        List<String> retval = null;
        COSBase filters = stream.getDictionaryObject(COSName.F_FILTER);
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            retval = new COSArrayList<String>(name.getName(), name, stream,
                    COSName.F_FILTER);
        } 
        else if (filters instanceof COSArray)
        {
            retval = COSArrayList
                    .convertCOSNameCOSArrayToList((COSArray) filters);
        }
        return retval;
    }

    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters The filters that are part of this stream.
     */
    public void setFileFilters(List<String> filters)
    {
        COSBase obj = COSArrayList.convertStringListToCOSNameCOSArray(filters);
        stream.setItem(COSName.F_FILTER, obj);
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
        List<Object> retval = null;

        COSBase dp = stream.getDictionaryObject(COSName.F_DECODE_PARMS);
        if (dp instanceof COSDictionary)
        {
            Map<?, ?> map = COSDictionaryMap
                    .convertBasicTypesToMap((COSDictionary) dp);
            retval = new COSArrayList<Object>(map, dp, stream,
                    COSName.F_DECODE_PARMS);
        } 
        else if (dp instanceof COSArray)
        {
            COSArray array = (COSArray) dp;
            List<Object> actuals = new ArrayList<Object>();
            for (int i = 0; i < array.size(); i++)
            {
                actuals.add(COSDictionaryMap
                        .convertBasicTypesToMap((COSDictionary) array
                                .getObject(i)));
            }
            retval = new COSArrayList<Object>(actuals, array);
        }

        return retval;
    }

    /**
     * This will set the list of decode params.
     * 
     * @param decodeParams The list of decode params.
     */
    public void setFileDecodeParams(List<?> decodeParams)
    {
        stream.setItem("FDecodeParams",
                COSArrayList.converterToCOSArray(decodeParams));
    }

    /**
     * This will copy the stream into a byte array.
     * 
     * @return The byte array of the filteredStream.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] toByteArray() throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream is = null;
        try
        {
            is = createInputStream();
            IOUtils.copy(is, output);
        } 
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        return output.toByteArray();
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
        if (mdStream != null)
        {
            if (mdStream instanceof COSStream)
            {
                retval = new PDMetadata((COSStream) mdStream);
            } 
            else if (mdStream instanceof COSNull)
            {
                // null is authorized
            } 
            else
            {
                throw new IllegalStateException(
                        "Expected a COSStream but was a "
                                + mdStream.getClass().getSimpleName());
            }
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
