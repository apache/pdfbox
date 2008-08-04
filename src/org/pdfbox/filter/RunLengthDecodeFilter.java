/**
 * Copyright (c) 2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pdfbox.cos.COSDictionary;

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
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
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
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
    {
        System.err.println( "Warning: RunLengthDecodeFilter.encode is not implemented yet, skipping this stream." );
    }
}