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

package org.apache.pdfbox.pdfparser;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is only for the readUntilEndStream method, to prevent a
 * final CR LF or LF (but not a final CR!) from being written to the output,
 * unless the beginning of the stream is assumed to be ASCII.
 * Only the 3-param write() method is implemented. This solves
 * PDFBOX-2079 and PDFBOX-2120 and avoids making readUntilEndStream() 
 * even more complex than it already is.
 *
 * @author Tilman Hausherr
 */
class EndstreamOutputStream extends BufferedOutputStream
{
    //TODO: replace this class with a PullBackOutputStream class if there ever is one
    
    private boolean hasCR = false;
    private boolean hasLF = false;
    private int pos = 0;
    private boolean mustFilter = true;

    EndstreamOutputStream(OutputStream out)
    {
        super(out);
    }

    /**
     * Write CR and/or LF that were kept, then writes len bytes from the 
     * specified byte array starting at offset off to this output stream,
     * except trailing CR, CR LF, or LF. No filtering will be done for the
     * entire stream if the beginning is assumed to be ASCII.
     * @param b byte array.
     * @param off offset.
     * @param len length of segment to write.
     * @throws IOException 
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (pos == 0 && len > 10)
        {
            // PDFBOX-2120 Don't filter if ASCII, i.e. keep a final CR LF or LF
            mustFilter = false;
            for (int i = 0; i < 10; ++i)
            {
                // Heuristic approach, taken from PDFStreamParser, PDFBOX-1164
                if ((b[i] < 0x09) || ((b[i] > 0x0a) && (b[i] < 0x20) && (b[i] != 0x0d)))
                {
                    // control character or > 0x7f -> we have binary data
                    mustFilter = true;
                    break;
                }
            }
        }
        if (mustFilter)
        {
            // first write what we kept last time
            if (hasCR)
            {
                // previous buffer ended with CR
                hasCR = false;
                if (!hasLF && len == 1 && b[off] == '\n')
                {
                    // actual buffer contains only LF so it will be the last one
                    // => we're done
                    // reset hasCR done too to avoid CR getting written in the flush
                    return;
                }
                super.write('\r');               
            }
            if (hasLF)
            {
                super.write('\n');
                hasLF = false;
            }
            // don't write CR, LF, or CR LF if at the end of the buffer
            if (len > 0)
            {
                if (b[off + len - 1] == '\r')
                {
                    hasCR = true;
                    --len;
                }
                else if (b[off + len - 1] == '\n')
                {
                    hasLF = true;
                    --len;
                    if (len > 0 && b[off + len - 1] == '\r')
                    {
                        hasCR = true;
                        --len;
                    }
                }
            }
        }
        super.write(b, off, len);
        pos += len;
    }

    /**
     * write out a single CR if one was kept. Don't write kept CR LF or LF, 
     * and then call the base method to flush.
     * 
     * @throws IOException 
     */
    @Override
    public void flush() throws IOException
    {
        // if there is only a CR and no LF, write it
        if (hasCR && !hasLF)
        {
            super.write('\r');
            ++pos;
        }
        hasCR = false;
        hasLF = false;
        super.flush();
    }
}
