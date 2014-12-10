/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.pdfbox.io.IOUtils;

/**
 * Helper class to contain predictor decoding used by Flate and LZW filter. 
 * To see the history, look at the FlateFilter class.
 */
public class Predictor
{
    static void decodePredictor(int predictor, int colors, int bitsPerComponent, int columns, InputStream in, OutputStream out)
            throws IOException
    {
        if (predictor == 1)
        {
            // no prediction
            IOUtils.copy(in, out);
        }
        else
        {
            // calculate sizes
            final int bitsPerPixel = colors * bitsPerComponent;
            final int bytesPerPixel = (bitsPerPixel + 7) / 8;
            final int rowlength = (columns * bitsPerPixel + 7) / 8;
            byte[] actline = new byte[rowlength];
            byte[] lastline = new byte[rowlength];

            int linepredictor = predictor;

            while (in.available() > 0)
            {
                // test for PNG predictor; each value >= 10 (not only 15) indicates usage of PNG predictor
                if (predictor >= 10)
                {
                    // PNG predictor; each row starts with predictor type (0, 1, 2, 3, 4)
                    linepredictor = in.read();// read per line predictor
                    if (linepredictor == -1)
                    {
                        return;
                    }
                    else
                    {
                        linepredictor += 10; // add 10 to tread value 0 as 10, 1 as 11, ...
                    }
                }

                // read line
                int i, offset = 0;
                while (offset < rowlength && ((i = in.read(actline, offset, rowlength - offset)) != -1))
                {
                    offset += i;
                }

                // do prediction as specified in PNG-Specification 1.2
                switch (linepredictor)
                {
                    case 2:// PRED TIFF SUB
                        // TODO decode tiff with bitsPerComponent < 8;
                        // e.g. for 4 bpc each nibble must be subtracted separately
                        if (bitsPerComponent == 16)
                        {
                            for (int p = 0; p < rowlength; p += 2)
                            {
                                int sub = ((actline[p] & 0xff) << 8) + (actline[p + 1] & 0xff);
                                int left = p - bytesPerPixel >= 0
                                        ? (((actline[p - bytesPerPixel] & 0xff) << 8)
                                        + (actline[p - bytesPerPixel + 1] & 0xff))
                                        : 0;
                                actline[p] = (byte) (((sub + left) >> 8) & 0xff);
                                actline[p + 1] = (byte) ((sub + left) & 0xff);
                            }
                            break;
                        }
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
                            int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] : 0;
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
                            int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff : 0;
                            int up = lastline[p] & 0xff;
                            actline[p] = (byte) ((avg + (int) Math.floor((left + up) / 2)) & 0xff);
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
                System.arraycopy(actline, 0, lastline, 0, rowlength);
                out.write(actline, 0, actline.length);
            }
        }
    }

}
