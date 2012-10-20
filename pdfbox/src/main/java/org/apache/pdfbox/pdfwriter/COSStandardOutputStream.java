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
package org.apache.pdfbox.pdfwriter;



import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import org.apache.pdfbox.util.StringUtil;

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
    public static final byte[] CRLF = StringUtil.getBytes("\r\n");

    /**
     * Line feed character.
     */
    public static final byte[] LF = StringUtil.getBytes("\n");

    /**
     * standard line separator.
     */
    public static final byte[] EOL = StringUtil.getBytes("\n");

    // current byte pos in the output stream
    private long pos = 0;
    // flag to prevent generating two newlines in sequence
    private boolean onNewLine = false;
    private FileChannel fileChannel = null;
    private FileDescriptor fileDescriptor = null;
    private long mark = -1;
    
    /**
     * COSOutputStream constructor comment.
     *
     * @param out The underlying stream to write to.
     */
    public COSStandardOutputStream(OutputStream out)
    {
        super(out);
        if(out instanceof FileOutputStream) {
            try {
                fileChannel = ((FileOutputStream)out).getChannel();
                fileDescriptor = ((FileOutputStream)out).getFD();
                pos = fileChannel.position();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     * This will get the current position in the stream.
     *
     * @return The current position in the stream.
     * @throws IOException 
     */
    public void setPos(long pos) throws IOException
    {
        if(fileChannel!=null) {
            checkPos();
            this.pos=pos;
            fileChannel.position(pos);
        }
    }
    
    /**
     * This will tell if we are on a newline.
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
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkPos();
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
    @Override
    public void write(int b) throws IOException
    {
        checkPos();
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
    }
    
    public void mark() throws IOException 
    {
        checkPos();
        mark = getPos();
    }
    
    public void reset() throws IOException 
    {
        if(mark<0)
            return;
        setPos(mark);
    }
    
    private void checkPos() throws IOException 
    {
        if(fileChannel!=null && fileChannel.position() != getPos())
            throw new IOException("OutputStream has an invalid position");
    }

    public byte[] getFileInBytes(int[] byteRange) throws IOException 
    {
        return null;
    }
    
    public InputStream getFilterInputStream(int[] byteRange) 
    {
        return new COSFilterInputStream(new FileInputStream(fileDescriptor), byteRange);
    }
}
