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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parser for a pfb-file.
 *
 * @author Ben Litchfield
 * @author Michael Niedermair
 */
public class PfbParser 
{
    private static final Logger LOG = LogManager.getLogger(PfbParser.class);
    
    /**
     * the pfb header length.
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
     * the EOF marker.
     */
    private static final int EOF_MARKER = 0x03;

    /**
     * the parsed pfb-data.
     */
    private byte[] pfbdata;

    /**
     * the lengths of the records (ASCII, BINARY, ASCII)
     */
    private final int[] lengths = new int[3];

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
        this(Files.readAllBytes(Paths.get(filename)));
    }

    /**
     * Create a new object.
     * @param in   The input.
     * @throws IOException if an IO-error occurs.
     */
    public PfbParser(final InputStream in) throws IOException 
    {
        byte[] pfb = in.readAllBytes();
        parsePfb(pfb);
    }

    /**
     * Create a new object.
     * @param bytes   The input.
     * @throws IOException if an IO-error occurs.
     */
    public PfbParser(final byte[] bytes) throws IOException
    {
        parsePfb(bytes);
    }

    /**
     * Parse the pfb-array.
     * @param pfb   The pfb-Array
     * @throws IOException in an IO-error occurs.
     */
    private void parsePfb(final byte[] pfb) throws IOException 
    {
        if (pfb.length < PFB_HEADER_LENGTH)
        {
            throw new IOException("PFB header missing");
        }
        // read into segments and keep them
        List<Integer> typeList = new ArrayList<>(3);
        List<byte[]> barrList = new ArrayList<>(3);
        ByteArrayInputStream in = new ByteArrayInputStream(pfb);
        int total = 0;
        do
        {
            int r = in.read();
            if (r == -1 && total > 0)
            {
                break; // EOF
            }
            if (r != START_MARKER) 
            {
                throw new IOException("Start marker missing");
            }
            int recordType = in.read();
            if (recordType == EOF_MARKER)
            {
                break;
            }
            if (recordType != ASCII_MARKER && recordType != BINARY_MARKER)
            {
                throw new IOException("Incorrect record type: " + recordType);
            }

            int size = in.read();
            size += in.read() << 8;
            size += in.read() << 16;
            size += in.read() << 24;
            LOG.debug("record type: {}, segment size: {}", recordType, size);
            byte[] ar = new byte[size];
            int got = in.read(ar);
            if (got != size)
            {
                throw new EOFException("EOF while reading PFB font");
            }
            total += size;
            typeList.add(recordType);
            barrList.add(ar);
        }
        while (true);
        
        // We now have ASCII and binary segments. Lets arrange these so that the ASCII segments
        // come first, then the binary segments, then the last ASCII segment if it is
        // 0000... cleartomark
        
        pfbdata = new byte[total];
        byte[] cleartomarkSegment = null;
        int dstPos = 0;
        
        // copy the ASCII segments
        for (int i = 0; i < typeList.size(); ++i)
        {
            if (typeList.get(i) != ASCII_MARKER)
            {
                continue;
            }
            byte[] ar = barrList.get(i);
            if (i == typeList.size() - 1 && ar.length < 600 && new String(ar).contains("cleartomark"))
            {
                cleartomarkSegment = ar;
                continue;
            }
            System.arraycopy(ar, 0, pfbdata, dstPos, ar.length);
            dstPos += ar.length;
        }
        lengths[0] = dstPos;

        // copy the binary segments
        for (int i = 0; i < typeList.size(); ++i)
        {
            if (typeList.get(i) != BINARY_MARKER)
            {
                continue;
            }
            byte[] ar = barrList.get(i);
            System.arraycopy(ar, 0, pfbdata, dstPos, ar.length);
            dstPos += ar.length;
        }
        lengths[1] = dstPos - lengths[0];
        
        if (cleartomarkSegment != null)
        {
            System.arraycopy(cleartomarkSegment, 0, pfbdata, dstPos, cleartomarkSegment.length);
            lengths[2] = cleartomarkSegment.length;
        }
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

    /**
     * Returns the first segment
     * @return first segment bytes
     */
    public byte[] getSegment1()
    {
        return Arrays.copyOfRange(pfbdata, 0, lengths[0]);
    }

    /**
     * Returns the second segment
     * @return second segment bytes
     */
    public byte[] getSegment2()
    {
        return Arrays.copyOfRange(pfbdata, lengths[0], lengths[0] + lengths[1]);
    }
}
