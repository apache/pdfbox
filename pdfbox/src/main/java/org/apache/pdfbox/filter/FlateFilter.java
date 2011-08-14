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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.EOFException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This is the used for the FlateDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Marcel Kammer
 * @version $Revision: 1.12 $
 */
public class FlateFilter implements Filter
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(FlateFilter.class);

    private static final int    BUFFER_SIZE    = 2048;

    /**
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex )
    throws IOException
    {
        COSBase baseObj = options.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        COSDictionary dict = null;
        if( baseObj instanceof COSDictionary )
        {
            dict = (COSDictionary)baseObj;
        }
        else if( baseObj instanceof COSArray )
        {
            COSArray paramArray = (COSArray)baseObj;
            if( filterIndex < paramArray.size() )
            {
                dict = (COSDictionary)paramArray.getObject( filterIndex );
            }
        }
        else if( baseObj == null )
        {
            //do nothing
        }
        else
        {
            throw new IOException( "Error: Expected COSArray or COSDictionary and not "
                    + baseObj.getClass().getName() );
        }


        int predictor = -1;
        int colors = -1;
        int bitsPerPixel = -1;
        int columns = -1;
        InflaterInputStream decompressor = null;
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        if (dict!=null)
        {
            predictor = dict.getInt(COSName.PREDICTOR);
            if(predictor > 1)
            {
                colors = dict.getInt(COSName.COLORS);
                bitsPerPixel = options.getInt(COSName.BITS_PER_COMPONENT);
                columns = dict.getInt(COSName.COLUMNS);
            }
        }

        try
        {
            // Decompress data to temporary ByteArrayOutputStream
            decompressor = new InflaterInputStream(compressedData);
            int amountRead;
            int mayRead = compressedData.available();

            if (mayRead > 0)
            {
                byte[] buffer = new byte[Math.min(mayRead,BUFFER_SIZE)];

                // Decode data using given predictor
                if (predictor==-1 || predictor == 1 )
                {
                    try
                    {
                        // decoding not needed
                        while ((amountRead = decompressor.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
                        {
                            result.write(buffer, 0, amountRead);
                        }
                    }
                    catch (OutOfMemoryError exception)
                    {
                        // if the stream is corrupt an OutOfMemoryError may occur
                        log.error("Stop reading corrupt stream");
                    }
                    catch (ZipException exception)
                    {
                        // if the stream is corrupt an OutOfMemoryError may occur
                        log.error("Stop reading corrupt stream");
                    }
                    catch (EOFException exception)
                    {
                        // if the stream is corrupt an OutOfMemoryError may occur
                        log.error("Stop reading corrupt stream");
                    }
                }
                else
                {
                    /*
                     * Reverting back to default values
                     */
                    if( colors == -1 )
                    {
                        colors = 1;
                    }
                    if( bitsPerPixel == -1 )
                    {
                        bitsPerPixel = 8;
                    }
                    if( columns == -1 )
                    {
                        columns = 1;
                    }

                    baos = new ByteArrayOutputStream();
                    while ((amountRead = decompressor.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
                    {
                        baos.write(buffer, 0, amountRead);
                    }
                    baos.flush();

                    // Copy data to ByteArrayInputStream for reading
                    bais = new ByteArrayInputStream(baos.toByteArray());
                    baos.close();
                    baos = null;

                    byte[] decodedData = decodePredictor(predictor, colors, bitsPerPixel, columns, bais);
                    bais.close();
                    bais = null;

                    result.write(decodedData);
                }
            }

            result.flush();
        }
        finally
        {
            if (decompressor != null)
            {
                decompressor.close();
            }
            if (bais != null)
            {
                bais.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
    }

    private byte[] decodePredictor(int predictor, int colors, int bitsPerComponent, int columns, InputStream data)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        if (predictor == 1 )
        {
            // No prediction
            int i = 0;
            while ((i = data.read(buffer)) != -1)
            {
                baos.write(buffer, 0, i);
            }
        }
        else
        {
            // calculate sizes
            int bitsPerPixel = colors * bitsPerComponent;
            int bytesPerPixel = (bitsPerPixel + 7 ) / 8;
            int rowlength = (columns * bitsPerPixel + 7) / 8;
            byte[] actline = new byte[rowlength];
            // Initialize lastline with Zeros according to PNG-specification
            byte[] lastline = new byte[rowlength];

            boolean done = false;
            int linepredictor = predictor;

            while (!done && data.available() > 0)
            {
                // test for PNG predictor; each value >= 10 (not only 15) indicates usage of PNG predictor
                if (predictor >= 10)
                {
                    // PNG predictor; each row starts with predictor type (0, 1, 2, 3, 4)
                    linepredictor = data.read();// read per line predictor
                    if (linepredictor == -1)
                    {
                        done = true;// reached EOF
                        break;
                    }
                    else
                    {
                        linepredictor += 10; // add 10 to tread value 0 as 10, 1 as 11, ...
                    }
                }

                // read line
                int i = 0;
                int offset = 0;
                while (offset < rowlength && ((i = data.read(actline, offset, rowlength - offset)) != -1))
                {
                    offset += i;
                }

                // Do prediction as specified in PNG-Specification 1.2
                switch (linepredictor)
                {
                    case 2:// PRED TIFF SUB
                        /**
                         * @TODO decode tiff with bitsPerComponent != 8;
                         * e.g. for 4 bpc each nibble must be subtracted separately
                         */
                        if ( bitsPerComponent != 8 )
                        {
                            throw new IOException("TIFF-Predictor with " + bitsPerComponent
                                    + " bits per component not supported");
                        }
                        // for 8 bits per component it is the same algorithm as PRED SUB of PNG format
                        for (int p = 0; p < rowlength; p++)
                        {
                            int sub = actline[p] & 0xff;
                            int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff : 0;
                            actline[p] = (byte) (sub + left);
                        }
                        break;
                    case 10:// PRED NONE
                        // do nothing
                        break;
                    case 11:// PRED SUB
                        for (int p = 0; p < rowlength; p++)
                        {
                            int sub = actline[p];
                            int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel]: 0;
                            actline[p] = (byte) (sub + left);
                        }
                        break;
                    case 12:// PRED UP
                        for (int p = 0; p < rowlength; p++)
                        {
                            int up = actline[p] & 0xff;
                            int prior = lastline[p] & 0xff;
                            actline[p] = (byte) ((up + prior) & 0xff);
                        }
                        break;
                    case 13:// PRED AVG
                        for (int p = 0; p < rowlength; p++)
                        {
                            int avg = actline[p] & 0xff;
                            int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff: 0;
                            int up = lastline[p] & 0xff;
                            actline[p] = (byte) ((avg +  (int)Math.floor( (left + up)/2 ) ) & 0xff);
                        }
                        break;
                    case 14:// PRED PAETH
                        for (int p = 0; p < rowlength; p++)
                        {
                            int paeth = actline[p] & 0xff;
                            int a = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff : 0;// left
                            int b = lastline[p] & 0xff;// upper
                            int c = p - bytesPerPixel >= 0 ? lastline[p - bytesPerPixel] & 0xff : 0;// upperleft
                            int value = a + b - c;
                            int absa = Math.abs(value - a);
                            int absb = Math.abs(value - b);
                            int absc = Math.abs(value - c);

                            if (absa <= absb && absa <= absc)
                            {
                                actline[p] = (byte) ((paeth + a) & 0xff);
                            }
                            else if (absb <= absc)
                            {
                                actline[p] = (byte) ((paeth + b) & 0xff);
                            }
                            else
                            {
                                actline[p] = (byte) ((paeth + c) & 0xff);
                            }
                        }
                        break;
                    default:
                        break;
                }
                lastline = actline.clone();
                baos.write(actline, 0, actline.length);
            }
        }
        return baos.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options, int filterIndex )
    throws IOException
    {
        DeflaterOutputStream out = new DeflaterOutputStream(result);
        int amountRead = 0;
        int mayRead = rawData.available();
        if (mayRead > 0)
        {
            byte[] buffer = new byte[Math.min(mayRead,BUFFER_SIZE)];
            while ((amountRead = rawData.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
            {
                out.write(buffer, 0, amountRead);
            }
        }
        out.close();
        result.flush();
    }
}
