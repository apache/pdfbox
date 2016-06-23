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
import org.apache.pdfbox.io.IOUtils;

/**
 * Decodes image data that has been encoded using either Group 3 or Group 4
 * CCITT facsimile (fax) encoding, and encodes image data to Group 4.
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
        byte[] decompressed = new byte[arraySize];
        CCITTFaxDecoderStream s;
        int type;
        long tiffOptions;
        if (k == 0)
        {
            tiffOptions = encodedByteAlign ? TIFFExtension.GROUP3OPT_BYTEALIGNED : 0;
            type = TIFFExtension.COMPRESSION_CCITT_MODIFIED_HUFFMAN_RLE;
        }
        else
        {
            if (k > 0)
            {
                tiffOptions = encodedByteAlign ? TIFFExtension.GROUP3OPT_BYTEALIGNED : 0;
                tiffOptions |= TIFFExtension.GROUP3OPT_2DENCODING;
                type = TIFFExtension.COMPRESSION_CCITT_T4;
            }
            else
            {
                // k < 0
                tiffOptions = encodedByteAlign ? TIFFExtension.GROUP4OPT_BYTEALIGNED : 0;
                type = TIFFExtension.COMPRESSION_CCITT_T6;
            }
        }
        s = new CCITTFaxDecoderStream(encoded, cols, type, TIFFExtension.FILL_LEFT_TO_RIGHT, tiffOptions);
        readFromDecoderStream(s, decompressed);

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

    void readFromDecoderStream(CCITTFaxDecoderStream decoderStream, byte[] result)
            throws IOException
    {
        int pos = 0;
        int read;
        while ((read = decoderStream.read(result, pos, result.length - pos)) > -1)
        {
            pos += read;
            if (pos >= result.length)
            {
                break;
            }
        }
        decoderStream.close();
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
        int cols = parameters.getInt(COSName.COLUMNS);
        int rows = parameters.getInt(COSName.ROWS);
        CCITTFaxEncoderStream ccittFaxEncoderStream = 
                new CCITTFaxEncoderStream(encoded, cols, rows, TIFFExtension.FILL_LEFT_TO_RIGHT);
        IOUtils.copy(input, ccittFaxEncoderStream);
        input.close();
    }
}
