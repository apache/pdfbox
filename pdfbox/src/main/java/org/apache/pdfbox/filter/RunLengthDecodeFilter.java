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
 * This is a filter for the RunLength Decoder.
 *
 * From the PDF Reference
 * <pre>
 * The RunLengthDecode filter decodes data that has been encoded in a simple
 * byte-oriented format based on run length. The encoded data is a sequence of
 * runs, where each run consists of a length byte followed by 1 to 128 bytes of data. If
 * the length byte is in the range 0 to 127, the following length + 1 (1 to 128) bytes
 * are copied literally during decompression. If length is in the range 129 to 255, the
 * following single byte is to be copied 257 ? length (2 to 128) times during decompression.
 * A length value of 128 denotes EOD.
 *
 * The compression achieved by run-length encoding depends on the input data. In
 * the best case (all zeros), a compression of approximately 64:1 is achieved for long
 * files. The worst case (the hexadecimal sequence 00 alternating with FF) results in
 * an expansion of 127:128.
 * </pre>
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class RunLengthDecodeFilter implements Filter
{
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(RunLengthDecodeFilter.class);

    private static final int RUN_LENGTH_EOD = 128;

    /**
     * Constructor.
     */
    public RunLengthDecodeFilter()
    {
        //default constructor
    }

    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        int dupAmount = -1;
        byte[] buffer = new byte[128];
        while( (dupAmount = compressedData.read()) != -1 && dupAmount != RUN_LENGTH_EOD )
        {
            if( dupAmount <= 127 )
            {
                int amountToCopy = dupAmount+1;
                int compressedRead = 0;
                while( amountToCopy > 0 )
                {
                    compressedRead = compressedData.read( buffer, 0, amountToCopy );
                    result.write( buffer, 0, compressedRead );
                    amountToCopy -= compressedRead;
                }
            }
            else
            {
                int dupByte = compressedData.read();
                for( int i=0; i<257-dupAmount; i++ )
                {
                    result.write( dupByte );
                }
            }
        }
    }

     /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        log.warn( "RunLengthDecodeFilter.encode is not implemented yet, skipping this stream." );
    }
}
