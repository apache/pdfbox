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

package org.apache.pdfbox.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility functions for hex encoding.
 *
 * @author John Hewson
 */
public final class Hex
{
    /**
     * for hex conversion.
     * 
     * https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal
     *
     */
    private static final byte[] HEX_BYTES = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private static final char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private Hex() {}

    /**
     * Returns a hex string of the given byte.
     */
    public static String getString(byte b)
    {
        char[] chars = new char[]{HEX_CHARS[getHighNibble(b)], HEX_CHARS[getLowNibble(b)]};
        return new String(chars);
    }

    /**
     * Returns a hex string of the given byte array.
     */
    public static String getString(byte[] bytes)
    {
        StringBuilder string = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            string.append(HEX_CHARS[getHighNibble(b)]).append(HEX_CHARS[getLowNibble(b)]);
        }
        return string.toString();
    }

    /**
     * Returns the bytes corresponding to the ASCII hex encoding of the given byte.
     */
    public static byte[] getBytes(byte b)
    {
        return new byte[]{HEX_BYTES[getHighNibble(b)], HEX_BYTES[getLowNibble(b)]};
    }
    
    /**
     * Returns the bytes corresponding to the ASCII hex encoding of the given bytes.
     */
    public static byte[] getBytes(byte[] bytes)
    {
        byte[] asciiBytes = new byte[bytes.length*2];
        for(int i=0; i< bytes.length; i++)
        {
            asciiBytes[i*2] = HEX_BYTES[getHighNibble(bytes[i])];
            asciiBytes[i*2+1] = HEX_BYTES[getLowNibble(bytes[i])];
        }
        return asciiBytes;
    }

    /** 
     * Returns the characters corresponding to the ASCII hex encoding of the given short.
     */
    public static char[] getChars(short num)
    {
        char[] hex = new char[4];
        hex[0] = HEX_CHARS[(num >> 12) & 0x0F];
        hex[1] = HEX_CHARS[(num >> 8) & 0x0F];
        hex[2] = HEX_CHARS[(num >> 4) & 0x0F];
        hex[3] = HEX_CHARS[num & 0x0F];
        return hex;
    }

    /**
     * Takes the characters in the given string, convert it to bytes in UTF16-BE format
     * and build a char array that corresponds to the ASCII hex encoding of the resulting
     * bytes.
     *
     * Example:
     * <pre>
     *   getCharsUTF16BE("ab") == new char[]{'0','0','6','1','0','0','6','2'}
     * </pre>
     *
     * @param text The string to convert
     * @return The string converted to hex
     */
    public static char[] getCharsUTF16BE(String text)
    {
        // Note that the internal representation of string in Java is already UTF-16. Therefore
        // we do not need to use an encoder to convert the string to its byte representation.
        char[] hex = new char[text.length()*4];

        for (int stringIdx = 0, charIdx = 0; stringIdx < text.length(); stringIdx++)
        {
            char c = text.charAt(stringIdx);
            hex[charIdx++] = HEX_CHARS[(c >> 12) & 0x0F];
            hex[charIdx++] = HEX_CHARS[(c >> 8) & 0x0F];
            hex[charIdx++] = HEX_CHARS[(c >> 4) & 0x0F];
            hex[charIdx++] = HEX_CHARS[c & 0x0F];
        }

        return hex;
    }

    /**
     * Writes the given byte as hex value to the given output stream.
     * @param b the byte to be written
     * @param output the output stream to be written to
     * @throws IOException exception if anything went wrong
     */
    public static void writeHexByte(byte b, OutputStream output) throws IOException
    {
        output.write(HEX_BYTES[getHighNibble(b)]);
        output.write(HEX_BYTES[getLowNibble(b)]);
    }

    /** 
     * Writes the given byte array as hex value to the given output stream.
     * @param bytes the byte array to be written
     * @param output the output stream to be written to
     * @throws IOException exception if anything went wrong
     */
    public static void writeHexBytes(byte[] bytes, OutputStream output) throws IOException
    {
        for (byte b : bytes)
        {
            writeHexByte(b, output);
        }
    }
    
    /**
     * Get the high nibble of the given byte.
     * 
     * @param b the given byte
     * @return the high nibble
     */
    private static int getHighNibble(byte b)
    {
        return (b & 0xF0) >> 4;
    }

    /**
     * Get the low nibble of the given byte.
     * 
     * @param b the given byte
     * @return the low nibble
     */
    private static int getLowNibble(byte b)
    {
        return b & 0x0F;
    }
}
