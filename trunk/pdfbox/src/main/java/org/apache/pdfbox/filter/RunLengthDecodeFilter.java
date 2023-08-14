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

/**
 * Decompresses data encoded using a byte-oriented run-length encoding algorithm,
 * reproducing the original text or binary data
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
final class RunLengthDecodeFilter extends Filter
{
    private static final int RUN_LENGTH_EOD = 128;

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        int dupAmount;
        byte[] buffer = new byte[128];
        while ((dupAmount = encoded.read()) != -1 && dupAmount != RUN_LENGTH_EOD)
        {
            if (dupAmount <= 127)
            {
                int amountToCopy = dupAmount + 1;
                int compressedRead;
                while (amountToCopy > 0)
                {
                    compressedRead = encoded.read(buffer, 0, amountToCopy);
                    // EOF reached?
                    if (compressedRead == -1)
                    {
                        break;
                    }
                    decoded.write(buffer, 0, compressedRead);
                    amountToCopy -= compressedRead;
                }
            }
            else
            {
                int dupByte = encoded.read();
                // EOF reached?
                if (dupByte == -1)
                {
                    break;
                }
                for (int i = 0; i < 257 - dupAmount; i++)
                {
                    decoded.write(dupByte);
                }
            }
        }
        return new DecodeResult(parameters);
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        // Not used in PDFBox except for testing the decoder.
        int lastVal = -1;
        int byt;
        int count = 0;
        boolean equality = false;

        // buffer for "unequal" runs, size between 2 and 128
        byte[] buf = new byte[128];

        while ((byt = input.read()) != -1)
        {
            if (lastVal == -1)
            {
                // first time
                lastVal = byt;
                count = 1;
            }
            else
            {
                if (count == 128)
                {
                    if (equality)
                    {
                        // max length of equals
                        encoded.write(129); // = 257 - 128
                        encoded.write(lastVal);
                    }
                    else
                    {
                        // max length of unequals
                        encoded.write(127);
                        encoded.write(buf, 0, 128);
                    }
                    equality = false;
                    lastVal = byt;
                    count = 1;
                }
                else if (count == 1)
                {
                    if (byt == lastVal)
                    {
                        equality = true;
                    }
                    else
                    {
                        buf[0] = (byte) lastVal;
                        buf[1] = (byte) byt;
                        lastVal = byt;
                    }
                    count = 2;
                }
                else
                {
                    // 1 < count < 128
                    if (byt == lastVal)
                    {
                        if (equality)
                        {
                            ++count;
                        }
                        else
                        {
                            // write all we got except the last
                            encoded.write(count - 2);
                            encoded.write(buf, 0, count - 1);
                            count = 2;
                            equality = true;
                        }
                    }
                    else
                    {
                        if (equality)
                        {
                            // equality ends here
                            encoded.write(257 - count);
                            encoded.write(lastVal);
                            equality = false;
                            count = 1;
                        }
                        else
                        {
                            buf[count] = (byte) byt;
                            ++count;
                        }
                        lastVal = byt;
                    }
                }
            }
        }
        if (count > 0)
        {
            if (count == 1)
            {
                encoded.write(0);
                encoded.write(lastVal);
            }
            else if (equality)
            {
                encoded.write(257 - count);
                encoded.write(lastVal);
            }
            else
            {
                encoded.write(count - 1);
                encoded.write(buf, 0, count);
            }
        }
        encoded.write(RUN_LENGTH_EOD);
    }
}
