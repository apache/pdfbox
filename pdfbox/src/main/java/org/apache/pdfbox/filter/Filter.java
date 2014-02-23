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
    private static final Log log = LogFactory.getLog(Filter.class);

    protected Filter()
    {
    }

    /**
     * Decodes data, producing the original non-encoded data.
     * @param encoded the encoded byte stream
     * @param decoded the stream where decoded data will be written
     * @param parameters the parameters used for decoding
     * @return repaired parameters dictionary, or the original parameters dictionary
     * @throws IOException if the stream cannot be decoded
     */
    public final DecodeResult decode(InputStream encoded, OutputStream decoded, COSDictionary parameters,
                            int index) throws IOException
    {
        COSDictionary params = new COSDictionary();
        params.addAll(parameters);
        params.setItem(COSName.DECODE_PARMS, getDecodeParams(params, index));
        return decode(encoded, decoded, params.asUnmodifiableDictionary());
    }

    // implemented in subclasses
    protected abstract DecodeResult decode(InputStream encoded, OutputStream decoded,
                                   COSDictionary parameters) throws IOException;

    /**
     * Encodes data.
     * @param input the byte stream to encode
     * @param encoded the stream where encoded data will be written
     * @param parameters the parameters used for encoding
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
    private COSDictionary getDecodeParams(COSDictionary dictionary, int index)
    {
        COSBase obj = dictionary.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        if (obj instanceof COSDictionary)
        {
            return (COSDictionary)obj;
        }
        else if (obj instanceof COSArray)
        {
            COSArray array = (COSArray)obj;
            if (index < array.size())
            {
                return (COSDictionary)array.getObject(index);
            }
        }
        else if (obj != null)
        {
            log.error("Expected DecodeParams to be an Array or Dictionary but found " +
                      obj.getClass().getName());
        }
        return new COSDictionary();
    }
}
