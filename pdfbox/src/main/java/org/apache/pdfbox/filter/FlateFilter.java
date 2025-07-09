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
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import org.apache.pdfbox.cos.COSDictionary;

/**
 * Decompresses data encoded using the zlib/deflate compression method,
 * reproducing the original text or binary data.
 *
 * @author Ben Litchfield
 * @author Marcel Kammer
 */
final class FlateFilter extends Filter
{
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        final COSDictionary decodeParams = getDecodeParams(parameters, index);

        try (FlateFilterDecoderStream decoderStream = new FlateFilterDecoderStream(encoded))
        {
            OutputStream wrapPredictor = Predictor.wrapPredictor(decoded, decodeParams);
            decoderStream.transferTo(wrapPredictor);
            wrapPredictor.flush();
        }
        return new DecodeResult(parameters);
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        int compressionLevel = getCompressionLevel();
        Deflater deflater = new Deflater(compressionLevel);
        try (DeflaterOutputStream out = new DeflaterOutputStream(encoded,deflater))
        {
            input.transferTo(out);
        }
        encoded.flush();
        deflater.end();
    }
}
