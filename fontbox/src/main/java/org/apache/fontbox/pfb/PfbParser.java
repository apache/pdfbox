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
package org.apache.fontbox.pfb;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for a pfb-file.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @author <a href="mailto:m.g.n@gmx.de">Michael Niedermair</a>
 * @version $Revision: 1.1 $
 */
public class PfbParser 
{
    /**
     * the pdf header length.
     * (start-marker (1 byte), ascii-/binary-marker (1 byte), size (4 byte))
     * 3*6 == 18
     */
    private static final int PFB_HEADER_LENGTH = 18;

    /**
     * the start marker.
     */
    private static final int START_MARKER = 0x80;

    /**
     * the ascii marker.
     */
    private static final int ASCII_MARKER = 0x01;

    /**
     * the binary marker.
     */
    private static final int BINARY_MARKER = 0x02;

    /**
     * The record types in the pfb-file.
     */
    private static final int[] PFB_RECORDS = {ASCII_MARKER, BINARY_MARKER,
            ASCII_MARKER};
    
    /**
     * buffersize.
     */
    private static final int BUFFER_SIZE = 0xffff;

    /**
     * the parsed pfb-data.
     */
    private byte[] pfbdata;

    /**
     * the lengths of the records.
     */
    private int[] lengths;

    // sample (pfb-file)
    // 00000000 80 01 8b 15  00 00 25 21  50 53 2d 41  64 6f 62 65  
    //          ......%!PS-Adobe
    
    
    /**
     * Create a new object.
     * @param filename  the file name
     * @throws IOException if an IO-error occurs.
     */
    public PfbParser(final String filename) throws IOException 
    {
        this( new BufferedInputStream(new FileInputStream(filename),BUFFER_SIZE) );
    }

    /**
     * Create a new object.
     * @param in   The input.
     * @throws IOException if an IO-error occurs.
     */
    public PfbParser(final InputStream in) throws IOException 
    {
        byte[] pfb = readPfbInput(in);
        parsePfb(pfb);
    }

    /**
     * Parse the pfb-array.
     * @param pfb   The pfb-Array
     * @throws IOException in an IO-error occurs.
     */
    private void parsePfb(final byte[] pfb) throws IOException 
    {

        ByteArrayInputStream in = new ByteArrayInputStream(pfb);
        pfbdata = new byte[pfb.length - PFB_HEADER_LENGTH];
        lengths = new int[PFB_RECORDS.length];
        int pointer = 0;
        for (int records = 0; records < PFB_RECORDS.length; records++) 
        {
            if (in.read() != START_MARKER) 
            {
                throw new IOException("Start marker missing");
            }

            if (in.read() != PFB_RECORDS[records]) 
            {
                throw new IOException("Incorrect record type");
            }

            int size = in.read();
            size += in.read() << 8;
            size += in.read() << 16;
            size += in.read() << 24;
            lengths[records] = size;
            int got = in.read(pfbdata, pointer, size);
            if (got < 0) 
            {
                throw new EOFException();
            }
            pointer += got;
        }
    }

    /**
     * Read the pdf input.
     * @param in    The input.
     * @return Returns the pdf-array.
     * @throws IOException if an IO-error occurs.
     */
    private byte[] readPfbInput(final InputStream in) throws IOException 
    {
        // copy into an array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmpbuf = new byte[BUFFER_SIZE];
        int amountRead = -1;
        while ((amountRead = in.read(tmpbuf)) != -1) 
        {
            out.write(tmpbuf, 0, amountRead);
        }
        return out.toByteArray();
    }

    /**
     * Returns the lengths.
     * @return Returns the lengths.
     */
    public int[] getLengths() 
    {
        return lengths;
    }

    /**
     * Returns the pfbdata.
     * @return Returns the pfbdata.
     */
    public byte[] getPfbdata() 
    {
        return pfbdata;
    }

    /**
     * Returns the pfb data as stream.
     * @return Returns the pfb data as stream.
     */
    public InputStream getInputStream() 
    {
        return new ByteArrayInputStream(pfbdata);
    }

    /**
     * Returns the size of the pfb-data.
     * @return Returns the size of the pfb-data.
     */
    public int size() 
    {
        return pfbdata.length;
    }
}
