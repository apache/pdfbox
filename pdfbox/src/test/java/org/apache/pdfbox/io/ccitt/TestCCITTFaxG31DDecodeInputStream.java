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

/* $Id$ */

package org.apache.pdfbox.io.ccitt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.io.IOUtils;

/**
 * Tests the CCITT Fax G3 1D decoder.
 * @version $Revision$
 */
public class TestCCITTFaxG31DDecodeInputStream extends AbstractCCITTFaxTestCase
{

    private static final boolean DEBUG = false;

    private static final String EOL = "000000000001";
    private static final String RTC = EOL + EOL + EOL + EOL + EOL + EOL;

    /**
     * Tests the decoder with naked bits (no EOL, no alignment, nothing).
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderNaked() throws IOException
    {
        //Test data: 24x3 pixels encoded
        byte[] data = fromBinary("10011" + "000101" + "10011"
                    + "00110101" + "011" + "10011" + "0000111"
                    + "00110101" + "010" + "000111" + "010" + "0010111" + "000000");
        assertStandardDecodingResult(data);
    }

    /**
     * Tests the decoder with EOLs.
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderWithEOL() throws IOException
    {
        //Test data: 24x3 pixels encoded
        byte[] data = fromBinary("10011" + "000101" + "10011" + EOL
                    + "00110101" + "011" + "10011" + "0000111" + EOL
                    + "00110101" + "010" + "000111" + "010" + "0010111" + "000000" + EOL);
        assertStandardDecodingResult(data);
    }

    /**
     * Tests the decoder with RTC and byte alignment.
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderAlignedWithRTC() throws IOException
    {
        //Test data: 24x3 pixels encoded
        byte[] data = fromBinary("1001100010110011" + EOL
                               + "00110101011100110000111" + "0" + EOL
                               + "001101010100001110100010111000000" + "00000" + RTC);
        assertStandardDecodingResult(data);
    }

    /**
     * Tests the decoder with an initial EOL.
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderInitialEOL() throws IOException
    {
        //Test data: 24x3 pixels encoded
        byte[] data = fromBinary("000" + EOL + "1001100010110011" + EOL
                               + "00110101011100110000111" + EOL
                               + "001101010100001110100010111000000");
        assertStandardDecodingResult(data);
    }

    private void assertStandardDecodingResult(byte[] data) throws IOException
    {
        int columns = 24;

        byte[] decoded = decode(data, columns);

        if (DEBUG)
        {
            dumpBitmap(decoded, columns);
            System.out.println(PackedBitArray.toBitString(decoded));
        }

        assertEquals(9, decoded.length);
        assertEquals("000000001111111100000000"
                   + "111100000000111111111111"
                   + "101000000000000000000000", toBitString(decoded));
    }

    /**
     * Tests the decoder with a restriction in the number of rows.
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderRowsRestriction() throws IOException
    {
        //Test data: 24x3 pixels encoded
        byte[] data = fromBinary("10011" + "000101" + "10011"
                    + "00110101" + "011" + "10011" + "0000111"
                    + "00110101" + "010" + "000111" + "010" + "0010111" + "000000");
        int columns = 24;
        int rows = 2; //We actually have data for three rows. Just checking the restriction.

        CCITTFaxG31DDecodeInputStream decoder = new CCITTFaxG31DDecodeInputStream(
                new ByteArrayInputStream(data), columns, rows);
        byte[] decoded = IOUtils.toByteArray(decoder);
        decoder.close();

        if (DEBUG)
        {
            dumpBitmap(decoded, columns);
            System.out.println(PackedBitArray.toBitString(decoded));
        }

        assertEquals(6, decoded.length);
        assertEquals("000000001111111100000000"
                   + "111100000000111111111111", toBitString(decoded));
    }

    /**
     * Tests the decoder with white lines.
     * @throws IOException if an I/O error occurs
     */
    public void testDecoderWhiteLines() throws IOException
    {
        //Test data: 1728x3 pixels encoded (all white)
        byte[] data = fromBinary(EOL + "010011011" + "00110101" //EOL + w1728 (make-up) + w0
                               + EOL + "010011011" + "00110101"
                               + EOL + "010011011" + "00110101" + RTC);
        int columns = 1728;

        byte[] decoded = decode(data, columns);

        if (DEBUG)
        {
            dumpBitmap(decoded, columns);
        }

        assertEquals(columns * 3 / 8, decoded.length);
    }

    /**
     * Decodes a byte buffer.
     * @param data the data
     * @param columns the number of columns
     * @return the decoded bits/pixels
     * @throws IOException if an I/O error occurs
     */
    public static byte[] decode(byte[] data, int columns) throws IOException
    {
        CCITTFaxG31DDecodeInputStream decoder = new CCITTFaxG31DDecodeInputStream(
                new ByteArrayInputStream(data), columns);
        byte[] decoded = IOUtils.toByteArray(decoder);
        decoder.close();
        return decoded;
    }

    private byte[] fromBinary(String binary)
    {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        int pos = 0;
        while (pos < binary.length() - 8)
        {
            int v = Integer.parseInt(binary.substring(pos, pos + 8), 2);
            baout.write(v & 0xFF);
            pos += 8;
        }
        int rest = binary.length() - pos;
        if (rest > 0)
        {
            String f = binary.substring(pos) + "00000000".substring(rest);
            baout.write(Integer.parseInt(f, 2));
        }
        return baout.toByteArray();
    }

}
