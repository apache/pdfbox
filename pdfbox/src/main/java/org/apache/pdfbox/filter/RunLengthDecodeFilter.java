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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;

/**
 * Decompresses data encoded using a byte-oriented run-length encoding algorithm,
 * reproducing the original text or binary data
 *
 * @author Ben Litchfield
 */
final class RunLengthDecodeFilter extends Filter
{
    private static final Log LOG = LogFactory.getLog(RunLengthDecodeFilter.class);
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
                while(amountToCopy > 0)
                {
                    compressedRead = encoded.read(buffer, 0, amountToCopy);
                    decoded.write(buffer, 0, compressedRead);
                    amountToCopy -= compressedRead;
                }
            }
            else
            {
                int dupByte = encoded.read();
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
        LOG.warn("RunLengthDecodeFilter.encode is not implemented yet, skipping this stream.");
    }
}
