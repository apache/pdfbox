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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.ccitt.CCITTFaxG31DDecodeInputStream;
import org.apache.pdfbox.filter.ccitt.FillOrderChangeInputStream;
import org.apache.pdfbox.filter.ccitt.TIFFFaxDecoder;
import org.apache.pdfbox.io.IOUtils;

/**
 * Decodes image data that has been encoded using either Group 3 or Group 4
 * CCITT facsimile (fax) encoding.
 *
 * @author Ben Litchfield
 * @author Marcel Kammer
 * @author Paul King
 */
final class CCITTFaxFilter extends Filter
{
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        DecodeResult result = new DecodeResult(new COSDictionary());
        result.getParameters().addAll(parameters);

        // get decode parameters
        COSDictionary decodeParms = getDecodeParams(parameters, index);

        // parse dimensions
        int cols = decodeParms.getInt(COSName.COLUMNS, 1728);
        int rows = decodeParms.getInt(COSName.ROWS, 0);
        int height = parameters.getInt(COSName.HEIGHT, COSName.H, 0);
        if (rows > 0 && height > 0)
        {
            // ensure that rows doesn't contain implausible data, see PDFBOX-771
            rows = Math.min(rows, height);
        }
        else
        {
            // at least one of the values has to have a valid value
            rows = Math.max(rows, height);
        }

        // decompress data
        int k = decodeParms.getInt(COSName.K, 0);
        boolean encodedByteAlign = decodeParms.getBoolean(COSName.ENCODED_BYTE_ALIGN, false);
        int arraySize = (cols + 7) / 8 * rows;
        // TODO possible options??
        long tiffOptions = 0;
        byte[] decompressed;
        if (k == 0)
        {
            InputStream in = new CCITTFaxG31DDecodeInputStream(encoded, cols, rows, encodedByteAlign);
            in = new FillOrderChangeInputStream(in);
            decompressed = IOUtils.toByteArray(in);
            in.close();
        }
        else
        {
            TIFFFaxDecoder faxDecoder = new TIFFFaxDecoder(1, cols, rows);
            byte[] compressed = IOUtils.toByteArray(encoded);
            decompressed = new byte[arraySize];
            if (k > 0)
            {
                faxDecoder.decode2D(decompressed, compressed, 0, rows, tiffOptions);
            }
            else
            {
                // k < 0
                faxDecoder.decodeT6(decompressed, compressed, 0, rows, tiffOptions, encodedByteAlign);
            }
        }

        // invert bitmap
        boolean blackIsOne = decodeParms.getBoolean(COSName.BLACK_IS_1, false);
        if (!blackIsOne)
        {
            // Inverting the bitmap
            // Note the previous approach with starting from an IndexColorModel didn't work
            // reliably. In some cases the image wouldn't be painted for some reason.
            // So a safe but slower approach was taken.
            invertBitmap(decompressed);
        }

        // repair missing color space
        if (!parameters.containsKey(COSName.COLORSPACE))
        {
            result.getParameters().setName(COSName.COLORSPACE, COSName.DEVICEGRAY.getName());
        }

        decoded.write(decompressed);
        return new DecodeResult(parameters);
    }

    private void invertBitmap(byte[] bufferData)
    {
        for (int i = 0, c = bufferData.length; i < c; i++)
        {
            bufferData[i] = (byte) (~bufferData[i] & 0xFF);
        }
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("CCITTFaxFilter encoding not implemented, use the CCITTFactory methods instead");
    }
}
