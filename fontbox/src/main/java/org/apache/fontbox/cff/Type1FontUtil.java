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
package org.apache.fontbox.cff;

import java.util.Locale;

/**
 * This class contains some helper methods handling Type1-Fonts.
 *
 * @author Villu Ruusmann
 */
public final class Type1FontUtil
{

    private Type1FontUtil()
    {
    }

    /**
     * Converts a byte-array into a string with the corresponding hex value. 
     * @param bytes the byte array
     * @return the string with the hex value
     */
    public static String hexEncode(final byte[] bytes)
    {
        final StringBuilder sb = new StringBuilder();
        for (final byte aByte : bytes)
        {
            final String string = Integer.toHexString(aByte & 0xff);
            if (string.length() == 1)
            {
                sb.append("0");
            }
            sb.append(string.toUpperCase(Locale.US));
        }
        return sb.toString();
    }

    /**
     * Converts a string representing a hex value into a byte array.
     * @param string the string representing the hex value
     * @return the hex value as byte array
     */
    public static byte[] hexDecode(final String string)
    {
        if (string.length() % 2 != 0)
        {
            throw new IllegalArgumentException();
        }
        final byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < string.length(); i += 2)
        {
            bytes[i / 2] = (byte) Integer.parseInt(string.substring(i, i + 2), 16);
        }
        return bytes;
    }

    /**
     * Encrypt eexec.
     * @param buffer the given data
     * @return the encrypted data
     */
    public static byte[] eexecEncrypt(final byte[] buffer)
    {
        return encrypt(buffer, 55665, 4);
    }

    /**
     * Encrypt charstring.
     * @param buffer the given data
     * @param n blocksize?
     * @return the encrypted data
     */
    public static byte[] charstringEncrypt(final byte[] buffer, final int n)
    {
        return encrypt(buffer, 4330, n);
    }

    private static byte[] encrypt(final byte[] plaintextBytes, int r, final int n)
    {
        final byte[] buffer = new byte[plaintextBytes.length + n];

        for (int i = 0; i < n; i++)
        {
            buffer[i] = 0;
        }

        System.arraycopy(plaintextBytes, 0, buffer, n, buffer.length - n);

        final int c1 = 52845;
        final int c2 = 22719;

        final byte[] ciphertextBytes = new byte[buffer.length];

        for (int i = 0; i < buffer.length; i++)
        {
            final int plain = buffer[i] & 0xff;
            final int cipher = plain ^ r >> 8;

            ciphertextBytes[i] = (byte) cipher;

            r = (cipher + r) * c1 + c2 & 0xffff;
        }

        return ciphertextBytes;
    }

    /**
     * Decrypt eexec.
     * @param buffer the given encrypted data
     * @return the decrypted data
     */
    public static byte[] eexecDecrypt(final byte[] buffer)
    {
        return decrypt(buffer, 55665, 4);
    }

    /**
     * Decrypt charstring.
     * @param buffer the given encrypted data
     * @param n blocksize?
     * @return the decrypted data
     */
    public static byte[] charstringDecrypt(final byte[] buffer, final int n)
    {
        return decrypt(buffer, 4330, n);
    }

    private static byte[] decrypt(final byte[] ciphertextBytes, int r, final int n)
    {
        final byte[] buffer = new byte[ciphertextBytes.length];

        final int c1 = 52845;
        final int c2 = 22719;

        for (int i = 0; i < ciphertextBytes.length; i++)
        {
            final int cipher = ciphertextBytes[i] & 0xff;
            final int plain = cipher ^ r >> 8;

            buffer[i] = (byte) plain;

            r = (cipher + r) * c1 + c2 & 0xffff;
        }

        final byte[] plaintextBytes = new byte[ciphertextBytes.length - n];
        System.arraycopy(buffer, n, plaintextBytes, 0, plaintextBytes.length);

        return plaintextBytes;
    }
}