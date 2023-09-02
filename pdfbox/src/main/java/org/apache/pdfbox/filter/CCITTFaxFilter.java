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
import java.io.PushbackInputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

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
        // get decode parameters
        COSDictionary decodeParms = getDecodeParams(parameters, index);

        // parse dimensions
        int cols = decodeParms.getInt(COSName.COLUMNS, 1728);
        int rows = decodeParms.getInt(COSName.ROWS, 0);
        int height = parameters.getInt(COSName.HEIGHT, COSName.H, 0);
        if (rows > 0 && height > 0)
        {
            // PDFBOX-771, PDFBOX-3727: rows in DecodeParms sometimes contains an incorrect value
            rows = height;
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
        long tiffOptions = 0;
        if (k == 0)
        {
            type = TIFFExtension.COMPRESSION_CCITT_T4; // Group 3 1D
            byte[] streamData = new byte[20];
            int bytesRead = encoded.read(streamData);
            PushbackInputStream pushbackInputStream = new PushbackInputStream(encoded, streamData.length);
            pushbackInputStream.unread(streamData, 0, bytesRead);
            encoded = pushbackInputStream;
            if (streamData[0] != 0 || (streamData[1] >> 4 != 1 && streamData[1] != 1))
            {
                // leading EOL (0b000000000001) not found, search further and try RLE if not
                // found
                type = TIFFExtension.COMPRESSION_CCITT_MODIFIED_HUFFMAN_RLE;
                short b = (short) (((streamData[0] << 8) + (streamData[1] & 0xff)) >> 4);
                for (int i = 12; i < bytesRead * 8; i++)
                {
                    b = (short) ((b << 1) + ((streamData[(i / 8)] >> (7 - (i % 8))) & 0x01));
                    if ((b & 0xFFF) == 1)
                    {
                        type = TIFFExtension.COMPRESSION_CCITT_T4;
                        break;
                    }
                }
            }
        }
        else if (k > 0)
        {
            // Group 3 2D
            type = TIFFExtension.COMPRESSION_CCITT_T4;
            tiffOptions = TIFFExtension.GROUP3OPT_2DENCODING;
        }
        else
        {
            // Group 4
            type = TIFFExtension.COMPRESSION_CCITT_T6;
        }
        s = new CCITTFaxDecoderStream(encoded, cols, type, tiffOptions, encodedByteAlign);
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
        input.transferTo(ccittFaxEncoderStream);
    }
}
