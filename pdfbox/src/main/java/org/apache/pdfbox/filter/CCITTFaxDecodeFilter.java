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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.ccitt.CCITTFaxG31DDecodeInputStream;
import org.apache.pdfbox.io.ccitt.FillOrderChangeInputStream;

/**
 * This is a filter for the CCITTFax Decoder.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Marcel Kammer
 * @author Paul King
 * @version $Revision: 1.13 $
 */
public class CCITTFaxDecodeFilter implements Filter
{
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(CCITTFaxDecodeFilter.class);

    /**
     * Constructor.
     */
    public CCITTFaxDecodeFilter()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex)
    throws IOException
    {

        COSBase decodeP = options.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        COSDictionary decodeParms = null;
        if (decodeP instanceof COSDictionary)
        {
            decodeParms = (COSDictionary)decodeP;
        }
        else if (decodeP instanceof COSArray)
        {
            decodeParms =  (COSDictionary)((COSArray)decodeP).getObject(filterIndex);
        }
        int cols = 1728, rows = 0;
        if (decodeParms != null)
        {
            cols = decodeParms.getInt(COSName.COLUMNS, 1728);
            rows = decodeParms.getInt(COSName.ROWS, 0);
        }
        int height = options.getInt(COSName.HEIGHT, COSName.H, 0);
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
        int k = 0;
        boolean encodedByteAlign = false;
        boolean blackIsOne = false;
        if (decodeParms != null)
        {
            k = decodeParms.getInt(COSName.K, 0);
            encodedByteAlign = decodeParms.getBoolean(COSName.ENCODED_BYTE_ALIGN, false);        
            blackIsOne = decodeParms.getBoolean(COSName.BLACK_IS_1, false);
        }
        int arraySize = (cols + 7) / 8 * rows;
        TIFFFaxDecoder faxDecoder = new TIFFFaxDecoder(1, cols, rows);
        // TODO possible options??
        long tiffOptions = 0;
        byte[] compressed = IOUtils.toByteArray(compressedData);
        byte[] decompressed = null;
        if (k == 0)
        {
            InputStream in = new CCITTFaxG31DDecodeInputStream(
                    new ByteArrayInputStream(compressed), cols, rows, encodedByteAlign);
            in = new FillOrderChangeInputStream(in); //Decorate to change fill order
            decompressed = IOUtils.toByteArray(in);
            in.close();
        }
        else if (k > 0)
        {
            decompressed = new byte[arraySize];
            faxDecoder.decode2D(decompressed, compressed, 0, rows, tiffOptions);
        }
        else if (k < 0)
        {
            decompressed = new byte[arraySize];
            faxDecoder.decodeT6(decompressed, compressed, 0, rows, tiffOptions, encodedByteAlign);
        }

        // invert bitmap
        if (!blackIsOne)
        {
            // Inverting the bitmap
            // Note the previous approach with starting from an IndexColorModel didn't work
            // reliably. In some cases the image wouldn't be painted for some reason.
            // So a safe but slower approach was taken.
            invertBitmap(decompressed);
        }
        
        // repair missing color space
        if (!options.containsKey(COSName.COLORSPACE))
        {
            options.setName(COSName.COLORSPACE, COSName.DEVICEGRAY.getName());
        }        
        
        result.write(decompressed);        
    }

    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options, int filterIndex )
        throws IOException
    {
        log.warn("CCITTFaxDecode.encode is not implemented yet, skipping this stream.");
    }

    private void invertBitmap(byte[] bufferData)
    {
        for (int i = 0, c = bufferData.length; i < c; i++)
        {
            bufferData[i] = (byte) (~bufferData[i] & 0xFF);
        }
    }

}
