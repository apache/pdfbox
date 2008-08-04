/**
 * Copyright (c) 2003, www.pdfbox.org
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
package org.pdfbox.pdfwriter;



import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * simple output stream with some minor features for generating "pretty"
 * pdf files.
 *
 * @author Michael Traut
 * @version $Revision: 1.5 $
 */
public class COSStandardOutputStream extends FilterOutputStream
{

    /**
     * To be used when 2 byte sequence is enforced.
     */
    public static final byte[] CRLF = "\r\n".getBytes();

    /**
     * Line feed character.
     */
    public static final byte[] LF = "\n".getBytes();

    /**
     * standard line separator on this platform.
     */
    public static final byte[] EOL = System.getProperty("line.separator").getBytes();

    // current byte pos in the output stream
    private long pos = 0;
    // flag to prevent generating two newlines in sequence
    private boolean onNewLine = false;

    /**
     * COSOutputStream constructor comment.
     *
     * @param out The underlying stream to write to.
     */
    public COSStandardOutputStream(OutputStream out)
    {
        super(out);
    }
    /**
     * This will get the current position in the stream.
     *
     * @return The current position in the stream.
     */
    public long getPos()
    {
        return pos;
    }
    /**
     * This will tell if we are on a newling.
     *
     * @return true If we are on a newline.
     */
    public boolean isOnNewLine()
    {
        return onNewLine;
    }
    /**
     * This will set a flag telling if we are on a newline.
     *
     * @param newOnNewLine The new value for the onNewLine attribute.
     */
    public void setOnNewLine(boolean newOnNewLine)
    {
        onNewLine = newOnNewLine;
    }

    /**
     * This will write some byte to the stream.
     *
     * @param b The source byte array.
     * @param off The offset into the array to start writing.
     * @param len The number of bytes to write.
     *
     * @throws IOException If the underlying stream throws an exception.
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        setOnNewLine(false);
        out.write(b, off, len);
        pos += len;
    }

    /**
     * This will write a single byte to the stream.
     *
     * @param b The byte to write to the stream.
     *
     * @throws IOException If there is an error writing to the underlying stream.
     */
    public void write(int b) throws IOException
    {
        setOnNewLine(false);
        out.write(b);
        pos++;
    }

    /**
     * This will write a CRLF to the stream.
     *
     * @throws IOException If there is an error writing the data to the stream.
     */
    public void writeCRLF() throws IOException
    {
        write(CRLF);
        // setOnNewLine(true);
    }

    /**
     * This will write an EOL to the stream.
     *
     * @throws IOException If there is an error writing to the stream
     */
    public void writeEOL() throws IOException
    {
        if (!isOnNewLine())
        {
            write(EOL);
            setOnNewLine(true);
        }
    }

    /**
     * This will write a Linefeed to the stream.
     *
     * @throws IOException If there is an error writing to the underlying stream.
     */
    public void writeLF() throws IOException
    {
        write(LF);
        // setOnNewLine(true);
    }
}