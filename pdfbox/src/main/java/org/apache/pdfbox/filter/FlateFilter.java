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
        COSBase baseObj = options.getDictionaryObject(new String[] {"DecodeParms","DP"});
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
            predictor = dict.getInt("Predictor");
            if(predictor > 1)
            {
                colors = dict.getInt("Colors");
                bitsPerPixel = options.getInt("BitsPerComponent");
                columns = dict.getInt("Columns");
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
                if (predictor==-1 || predictor == 1 || predictor == 10)
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
                    bais = new ByteArrayInputStream(decodedData);

                    // write decoded data to result
                    while ((amountRead = bais.read(buffer)) != -1)
                    {
                        result.write(buffer, 0, amountRead);
                    }
                    bais.close();
                    bais = null;
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

        if (predictor == 1 || predictor == 10)
        {
            // No prediction or PNG NONE
            int i = 0;
            while ((i = data.read(buffer)) != -1)
            {
                baos.write(buffer, 0, i);
            }
        }
        else
        {
            // calculate sizes
            int bpp = (colors * bitsPerComponent + 7) / 8;
            int rowlength = (columns * colors * bitsPerComponent + 7) / 8 + bpp;
            byte[] actline = new byte[rowlength];
            byte[] lastline = new byte[rowlength];// Initialize lastline with
                                                    // Zeros according to
                                                    // PNG-specification
            boolean done = false;
            int linepredictor = predictor;

            while (!done && data.available() > 0)
            {
                if (predictor == 15)
                {
                    linepredictor = data.read();// read per line predictor
                    if (linepredictor == -1)
                    {
                        done = true;// reached EOF
                        break;
                    }
                    else
                    {
                        linepredictor += 10; // add 10 to tread value 1 as 11
                    }
                    // (instead of PRED NONE) and 2
                    // as 12 (instead of PRED TIFF)
                }

                // read line
                int i = 0;
                int offset = bpp;
                while (offset < rowlength && ((i = data.read(actline, offset, rowlength - offset)) != -1))
                {
                    offset += i;
                }

                // Do prediction as specified in PNG-Specification 1.2
                switch (linepredictor)
                {
                    case 2:// PRED TIFF SUB
                        /**
                         * @todo decode tiff
                         */
                        throw new IOException("TIFF-Predictor not supported");
                    case 11:// PRED SUB
                        for (int p = bpp; p < rowlength; p++)
                        {
                            int sub = actline[p] & 0xff;
                            int left = actline[p - bpp] & 0xff;
                            actline[p] = (byte) (sub + left);
                        }
                        break;
                    case 12:// PRED UP
                        for (int p = bpp; p < rowlength; p++)
                        {
                            int up = actline[p] & 0xff;
                            int prior = lastline[p] & 0xff;
                            actline[p] = (byte) (up + prior);
                        }
                        break;
                    case 13:// PRED AVG
                        for (int p = bpp; p < rowlength; p++)
                        {
                            int avg = actline[p] & 0xff;
                            int left = actline[p - bpp] & 0xff;
                            int up = lastline[p] & 0xff;
                            actline[p] = (byte) (avg + ((left + up) / 2));
                        }
                        break;
                    case 14:// PRED PAETH
                        for (int p = bpp; p < rowlength; p++)
                        {
                            int paeth = actline[p] & 0xff;
                            int a = actline[p - bpp] & 0xff;// left
                            int b = lastline[p] & 0xff;// upper
                            int c = lastline[p - bpp] & 0xff;// upperleft
                            int value = a + b - c;
                            int absa = Math.abs(value - a);
                            int absb = Math.abs(value - b);
                            int absc = Math.abs(value - c);

                            if (absa <= absb && absa <= absc)
                            {
                                actline[p] = (byte) (paeth + absa);
                            }
                            else if (absb <= absc)
                            {
                                actline[p] += (byte) (paeth + absb);
                            }
                            else
                            {
                                actline[p] += (byte) (paeth + absc);
                            }
                        }
                        break;
                    default:
                        break;
                }

                lastline = (byte[])actline.clone();
                baos.write(actline, bpp, actline.length - bpp);
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
