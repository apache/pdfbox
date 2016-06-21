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
    private static final String HEXES_STRING = "0123456789ABCDEF";

    private static final byte[] HEXES = HEXES_STRING.getBytes(Charsets.US_ASCII);

    private Hex() {}

    /**
     * Returns a hex string of the given byte.
     */
    public static String getString(byte b)
    {
        char[] chars = new char[]{HEXES_STRING.charAt(getHighNibble(b)), HEXES_STRING.charAt(getLowNibble(b))};
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
            string.append(HEXES_STRING.charAt(getHighNibble(b))).append(HEXES_STRING.charAt(getLowNibble(b)));
        }
        return string.toString();
    }

    /**
     * Returns the bytes corresponding to the ASCII hex encoding of the given byte.
     */
    public static byte[] getBytes(byte b)
    {
        return new byte[]{HEXES[getHighNibble(b)], HEXES[getLowNibble(b)]};
    }
    
    /**
     * Returns the bytes corresponding to the ASCII hex encoding of the given bytes.
     */
    public static byte[] getBytes(byte[] bytes)
    {
        byte[] asciiBytes = new byte[bytes.length*2];
        for(int i=0; i< bytes.length; i++)
        {
            asciiBytes[i*2] = HEXES[getHighNibble(bytes[i])];
            asciiBytes[i*2+1] = HEXES[getLowNibble(bytes[i])];
        }
        return asciiBytes;
    }

    /** 
     * Writes the given byte as hex value to the given output stream.
     * @param b the byte to be written
     * @param output the output stream to be written to
     * @throws IOException exception if anything went wrong
     */
    public static void writeHexByte(byte b, OutputStream output) throws IOException
    {
        output.write(HEXES[getHighNibble(b)]);
        output.write(HEXES[getLowNibble(b)]);
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
