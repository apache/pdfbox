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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A filter for stream data.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public abstract class Filter
{
    private static final Log LOG = LogFactory.getLog(Filter.class);

    /**
     * Compression Level System Property. Set this to a value from 0 to 9 to change the zlib deflate
     * compression level used to compress /Flate streams. The default value is -1 which is
     * {@link Deflater#DEFAULT_COMPRESSION}. To set maximum compression, use
     * {@code System.setProperty(Filter.SYSPROP_DEFLATELEVEL, "9");}
     */
    public static final String SYSPROP_DEFLATELEVEL = "org.apache.pdfbox.filter.deflatelevel";

    /**
     * Constructor.
     */
    protected Filter()
    {
    }

    /**
     * Decodes data, producing the original non-encoded data.
     * @param encoded the encoded byte stream
     * @param decoded the stream where decoded data will be written
     * @param parameters the parameters used for decoding
     * @param index the index to the filter being decoded
     * @return repaired parameters dictionary, or the original parameters dictionary
     * @throws IOException if the stream cannot be decoded
     */
    public abstract DecodeResult decode(InputStream encoded, OutputStream decoded, COSDictionary parameters,
                    int index) throws IOException;

    /**
     * Decodes data, with optional DecodeOptions. Not all filters support all options, and so
     * callers should check the options' <code>honored</code> flag to test if they were applied.
     *
     * @param encoded the encoded byte stream
     * @param decoded the stream where decoded data will be written
     * @param parameters the parameters used for decoding
     * @param index the index to the filter being decoded
     * @param options additional options for decoding
     * @return repaired parameters dictionary, or the original parameters dictionary
     * @throws IOException if the stream cannot be decoded
     */
    public DecodeResult decode(InputStream encoded, OutputStream decoded, COSDictionary parameters,
            int index, DecodeOptions options) throws IOException
    {
        return decode(encoded, decoded, parameters, index);
    }

    /**
     * Encodes data.
     * @param input the byte stream to encode
     * @param encoded the stream where encoded data will be written
     * @param parameters the parameters used for encoding
     * @param index the index to the filter being encoded
     * @throws IOException if the stream cannot be encoded
     */
    public final void encode(InputStream input, OutputStream encoded, COSDictionary parameters,
                            int index) throws IOException
    {
        encode(input, encoded, parameters.asUnmodifiableDictionary());
    }

    // implemented in subclasses
    protected abstract void encode(InputStream input, OutputStream encoded,
                                   COSDictionary parameters) throws IOException;

    // gets the decode params for a specific filter index, this is used to
    // normalise the DecodeParams entry so that it is always a dictionary
    protected COSDictionary getDecodeParams(COSDictionary dictionary, int index)
    {
        COSBase filter = dictionary.getDictionaryObject(COSName.FILTER, COSName.F);
        COSBase obj = dictionary.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        if (filter instanceof COSName && obj instanceof COSDictionary)
        {
            // PDFBOX-3932: The PDF specification requires "If there is only one filter and that 
            // filter has parameters, DecodeParms shall be set to the filter’s parameter dictionary" 
            // but tests show that Adobe means "one filter name object".
            return (COSDictionary)obj;
        }
        else if (filter instanceof COSArray && obj instanceof COSArray)
        {
            COSArray array = (COSArray)obj;
            if (index < array.size())
            {
                COSBase objAtIndex = array.getObject(index);
                if (objAtIndex instanceof COSDictionary)
                {
                    return (COSDictionary)array.getObject(index);
                }
            }
        }
        else if (obj != null && !(filter instanceof COSArray || obj instanceof COSArray))
        {
            LOG.error("Expected DecodeParams to be an Array or Dictionary but found " +
                      obj.getClass().getName());
        }
        return new COSDictionary();
    }

    /**
     * Finds a suitable image reader for a format.
     *
     * @param formatName The format to search for.
     * @param errorCause The probably cause if something goes wrong.
     * @return The image reader for the format.
     * @throws MissingImageReaderException if no image reader is found.
     */
    protected static ImageReader findImageReader(String formatName, String errorCause) throws MissingImageReaderException
    {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(formatName);
        ImageReader reader = null;
        while (readers.hasNext())
        {
            reader = readers.next();
            if (reader != null && reader.canReadRaster())
            {
                break;
            }
        }
        if (reader == null)
        {
            throw new MissingImageReaderException("Cannot read " + formatName + " image: " + errorCause);
        }
        return reader;
    }

    /**
     * @return the ZIP compression level configured for PDFBox
     */
    public static int getCompressionLevel()
    {
        int compressionLevel = Deflater.DEFAULT_COMPRESSION;
        try
        {
            compressionLevel = Integer.parseInt(System.getProperty(Filter.SYSPROP_DEFLATELEVEL, "-1"));
        }
        catch (NumberFormatException ex)
        {
            LOG.warn(ex.getMessage(), ex);
        }
        return Math.max(-1, Math.min(Deflater.BEST_COMPRESSION, compressionLevel));
    }
}
