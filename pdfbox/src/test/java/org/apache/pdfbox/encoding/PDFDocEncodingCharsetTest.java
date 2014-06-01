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
package org.apache.pdfbox.encoding;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import junit.framework.TestCase;

/**
 * This class tests {@link PDFDocEncodingCharset} and indirectly {@link SingleByteCharset}.
 * @version $Revision$
 */
public class PDFDocEncodingCharsetTest extends TestCase
{

    /**
     * Tests {@link PDFDocEncodingCharset} and indirectly {@link SingleByteCharset}.
     * @throws UnsupportedEncodingException if an encoding cannot be found
     */
    public void testEncoding() throws UnsupportedEncodingException
    {
        //TODO Use when switching to JavaSE-1.6
        //Charset charset = PDFDocEncodingCharset.INSTANCE;

        //Check basic round-trip
        String text = "Test \u20AC$£ ;-) Gr\u00FCezi\u2026";
        byte[] encoded = text.getBytes(PDFDocEncodingCharset.NAME);
        int[] expected = new int[] {
                0x54, 0x65, 0x73, 0x74, 0x20, //Test
                0xA0, 0x24, 0xA3, 0x20, //Currency
                0x3B, 0x2D, 0x29, 0x20, //Smiley
                0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, //Hello in de_CH
                0x83 //ellipsis
        };
        compareEncoded(encoded, expected);
        String decoded = new String(encoded, PDFDocEncodingCharset.NAME);
        assertEquals(text, decoded);

        text = "Bad\u03C0\u2023char";
        expected = new int[] {
                0x42, 0x61, 0x64, 0x3F, 0x3F, 0x63, 0x68, 0x61, 0x72 //unencodable characters as '?'
        };
        encoded = text.getBytes(PDFDocEncodingCharset.NAME);
        compareEncoded(encoded, expected);
        decoded = new String(encoded, PDFDocEncodingCharset.NAME);
        assertEquals("Bad??char", decoded);
    }

    /**
     * Checking for behaviour with undefined character at the end of the buffer.
     * This used to cause an IllegalArgumentException.
     */
    public void testUnencodedAtEnd()
    {
        byte[] encoded = new byte[] {0x00, 0x01, 0x02, 0x7F}; //0x7F is undefined
        String decoded = toString(encoded, 0, encoded.length, PDFDocEncodingCharset.INSTANCE);
        assertEquals("\u0000\u0001\u0002\uFFFD", decoded);
    }
    
    private static String toString(byte[] data, int offset, int length, Charset charset)
    {
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(data, offset, length));
        return charBuffer.toString();
    }
    
    private void compareEncoded(byte[] encoded, int[] expected)
    {
        assertEquals(expected.length, encoded.length);
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals("Bad character at pos " + i, (byte)(expected[i] & 0xFF), encoded[i]);
        }
    }

}
