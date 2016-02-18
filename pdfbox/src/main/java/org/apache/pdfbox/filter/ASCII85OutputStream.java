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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents an ASCII85 output stream.
 *
 * @author Ben Litchfield
 *
 */
final class ASCII85OutputStream extends FilterOutputStream
{

    private int lineBreak;
    private int count;

    private byte[] indata;
    private byte[] outdata;

    /**
     * Function produces five ASCII printing characters from
     * four bytes of binary data.
     */
    private int maxline;
    private boolean flushed;
    private char terminator;
    private static final char OFFSET = '!';
    private static final char NEWLINE = '\n';
    private static final char Z = 'z';

    /**
     * Constructor.
     *
     * @param out The output stream to write to.
     */
    ASCII85OutputStream(OutputStream out)
    {
        super(out);
        lineBreak = 36 * 2;
        maxline = 36 * 2;
        count = 0;
        indata = new byte[4];
        outdata = new byte[5];
        flushed = true;
        terminator = '~';
    }

    /**
     * This will set the terminating character.
     *
     * @param term The terminating character.
     */
    public void setTerminator(char term)
    {
        if (term < 118 || term > 126 || term == Z)
        {
            throw new IllegalArgumentException("Terminator must be 118-126 excluding z");
        }
        terminator = term;
    }

    /**
     * This will get the terminating character.
     *
     * @return The terminating character.
     */
    public char getTerminator()
    {
        return terminator;
    }

    /**
     * This will set the line length that will be used.
     *
     * @param l The length of the line to use.
     */
    public void setLineLength(int l)
    {
        if (lineBreak > l)
        {
            lineBreak = l;
        }
        maxline = l;
    }

    /**
     * This will get the length of the line.
     *
     * @return The line length attribute.
     */
    public int getLineLength()
    {
        return maxline;
    }

    /**
     * This will transform the next four ascii bytes.
     */
    private void transformASCII85()
    {
        long word = ((((indata[0] << 8) | (indata[1] & 0xFF)) << 16) | ((indata[2] & 0xFF) << 8) | (indata[3] & 0xFF)) & 0xFFFFFFFFL;

        if (word == 0)
        {
            outdata[0] = (byte) Z;
            outdata[1] = 0;
            return;
        }
        long x;
        x = word / (85L * 85L * 85L * 85L);
        outdata[0] = (byte) (x + OFFSET);
        word -= x * 85L * 85L * 85L * 85L;

        x = word / (85L * 85L * 85L);
        outdata[1] = (byte) (x + OFFSET);
        word -= x * 85L * 85L * 85L;

        x = word / (85L * 85L);
        outdata[2] = (byte) (x + OFFSET);
        word -= x * 85L * 85L;

        x = word / 85L;
        outdata[3] = (byte) (x + OFFSET);

        outdata[4] = (byte) ((word % 85L) + OFFSET);
    }

    /**
     * This will write a single byte.
     *
     * @param b The byte to write.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    public void write(int b) throws IOException
    {
        flushed = false;
        indata[count++] = (byte) b;
        if (count < 4)
        {
            return;
        }
        transformASCII85();
        for (int i = 0; i < 5; i++)
        {
            if (outdata[i] == 0)
            {
                break;
            }
            out.write(outdata[i]);
            if (--lineBreak == 0)
            {
                out.write(NEWLINE);
                lineBreak = maxline;
            }
        }
        count = 0;
    }

    /**
     * This will flush the data to the stream.
     *
     * @throws IOException If there is an error writing the data to the stream.
     */
    @Override
    public void flush() throws IOException
    {
        if (flushed)
        {
            return;
        }
        if (count > 0)
        {
            for (int i = count; i < 4; i++)
            {
                indata[i] = 0;
            }
            transformASCII85();
            if (outdata[0] == Z)
            {
                for (int i = 0; i < 5; i++) // expand 'z',
                {
                    outdata[i] = (byte) OFFSET;
                }
            }
            for (int i = 0; i < count + 1; i++)
            {
                out.write(outdata[i]);
                if (--lineBreak == 0)
                {
                    out.write(NEWLINE);
                    lineBreak = maxline;
                }
            }
        }
        if (--lineBreak == 0)
        {
            out.write(NEWLINE);
        }
        out.write(terminator);
        out.write('>');
        out.write(NEWLINE);
        count = 0;
        lineBreak = maxline;
        flushed = true;
        super.flush();
    }

    /**
     * This will close the stream.
     *
     * @throws IOException If there is an error closing the wrapped stream.
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            flush();
            super.close();
        }
        finally
        {
            indata = outdata = null;
        }
    }
}
