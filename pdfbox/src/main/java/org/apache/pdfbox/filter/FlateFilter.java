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
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * Decompresses data encoded using the zlib/deflate compression method,
 * reproducing the original text or binary data.
 *
 * @author Ben Litchfield
 * @author Marcel Kammer
 */
final class FlateFilter extends Filter
{
    private static final Log LOG = LogFactory.getLog(FlateFilter.class);
    private static final int BUFFER_SIZE = 16348;

    @Override
    protected final DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters) throws IOException
    {
        int predictor = -1;
        int colors = -1;
        int bitsPerPixel = -1;
        int columns = -1;

        COSDictionary decodeParams = (COSDictionary)
                parameters.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);

        if (decodeParams != null)
        {
            predictor = decodeParams.getInt(COSName.PREDICTOR);
            if (predictor > 1)
            {
                colors = decodeParams.getInt(COSName.COLORS);
                bitsPerPixel = decodeParams.getInt(COSName.BITS_PER_COMPONENT);
                columns = decodeParams.getInt(COSName.COLUMNS);
            }
        }

        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        try
        {
            baos = decompress(encoded);

            // decode data using given predictor
            if (predictor == -1 || predictor == 1)
            {
                decoded.write(baos.toByteArray());
            }
            else
            {
                // reverting back to default values
                if (colors == -1)
                {
                    colors = 1;
                }

                if (bitsPerPixel == -1)
                {
                    bitsPerPixel = 8;
                }

                if (columns == -1)
                {
                    columns = 1;
                }

                // copy data to ByteArrayInputStream for reading
                bais = new ByteArrayInputStream(baos.toByteArray());

                byte[] decodedData = decodePredictor(predictor, colors, bitsPerPixel, columns, bais);
                bais.close();
                bais = null;

                decoded.write(decodedData);
            }
            decoded.flush();
        } 
        catch (DataFormatException e)
        {
            // if the stream is corrupt a DataFormatException may occur
            LOG.error("FlateFilter: stop reading corrupt stream due to a DataFormatException");

            // re-throw the exception
            throw new IOException(e);
        }
        finally
        {
            if (bais != null)
            {
                bais.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
        return new DecodeResult(parameters);
    }

    // Use Inflater instead of InflateInputStream to avoid an EOFException due to a probably
    // missing Z_STREAM_END, see PDFBOX-1232 for details
    private ByteArrayOutputStream decompress(InputStream in) throws IOException, DataFormatException 
    { 
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        byte[] buf = new byte[2048]; 
        int read = in.read(buf); 
        if (read > 0) 
        { 
            Inflater inflater = new Inflater(); 
            inflater.setInput(buf,0,read); 
            byte[] res = new byte[2048]; 
            while (true) 
            { 
                int resRead = inflater.inflate(res); 
                if (resRead != 0) 
                { 
                    out.write(res,0,resRead); 
                    continue; 
                } 
                if (inflater.finished() || inflater.needsDictionary() || in.available() == 0) 
                {
                    break;
                } 
                read = in.read(buf); 
                inflater.setInput(buf,0,read); 
            }
        }
        out.close();
        return out;
    } 
    
    private byte[] decodePredictor(int predictor, int colors, int bitsPerComponent, int columns, InputStream data)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        if (predictor == 1)
        {
            // no prediction
            int i;
            while ((i = data.read(buffer)) != -1)
            {
                baos.write(buffer, 0, i);
            }
        }
        else
        {
            // calculate sizes
            int bitsPerPixel = colors * bitsPerComponent;
            int bytesPerPixel = (bitsPerPixel + 7) / 8;
            int rowlength = (columns * bitsPerPixel + 7) / 8;
            byte[] actline = new byte[rowlength];
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
                int i, offset = 0;
                while (offset < rowlength && ((i = data.read(actline, offset, rowlength - offset)) != -1))
                {
                    offset += i;
                }

                // do prediction as specified in PNG-Specification 1.2
                switch (linepredictor)
                {
                    case 2:// PRED TIFF SUB
                        // TODO decode tiff with bitsPerComponent != 8;
                        // e.g. for 4 bpc each nibble must be subtracted separately
                        if (bitsPerComponent != 8)
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
                            actline[p] = (byte) ((avg +  (int)Math.floor((left + up)/2)) & 0xff);
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

    @Override
    protected final void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        DeflaterOutputStream out = new DeflaterOutputStream(encoded);
        int amountRead;
        int mayRead = input.available();
        if (mayRead > 0)
        {
            byte[] buffer = new byte[Math.min(mayRead,BUFFER_SIZE)];
            while ((amountRead = input.read(buffer, 0, Math.min(mayRead,BUFFER_SIZE))) != -1)
            {
                out.write(buffer, 0, amountRead);
            }
        }
        out.close();
        encoded.flush();
    }
}
