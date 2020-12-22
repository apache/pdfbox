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
package org.apache.pdfbox.pdmodel.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of the RC4 stream cipher.
 *
 * @author Ben Litchfield
 */
class RC4Cipher
{
    private final int[] salt;
    private int b;
    private int c;

    /**
     * Constructor.
     */
    RC4Cipher()
    {
        salt = new int[256];
    }

    /**
     * This will reset the key to be used.
     *
     * @param key The RC4 key used during encryption.
     */
    public void setKey(final byte[] key )
    {
        b = 0;
        c = 0;

        if(key.length < 1 || key.length > 32)
        {
            throw new IllegalArgumentException("number of bytes must be between 1 and 32");
        }
        for(int i = 0; i < salt.length; i++)
        {
            salt[i] = i;
        }

        int keyIndex = 0;
        int saltIndex = 0;
        for( int i = 0; i < salt.length; i++)
        {
            saltIndex = (fixByte(key[keyIndex]) + salt[i] + saltIndex) % 256;
            swap( salt, i, saltIndex );
            keyIndex = (keyIndex + 1) % key.length;
        }

    }

    /**
     * This will ensure that the value for a byte &gt;=0.
     *
     * @param aByte The byte to test against.
     *
     * @return A value &gt;=0 and &lt; 256
     */
    private static int fixByte(final byte aByte )
    {
        return aByte < 0 ? 256 + aByte : aByte;
    }

    /**
     * This will swap two values in an array.
     *
     * @param data The array to swap from.
     * @param firstIndex The index of the first element to swap.
     * @param secondIndex The index of the second element to swap.
     */
    private static void swap(final int[] data, final int firstIndex, final int secondIndex )
    {
        final int tmp = data[ firstIndex ];
        data[ firstIndex ] = data[ secondIndex ];
        data[ secondIndex ] = tmp;
    }

    /**
     * This will encrypt and write the next byte.
     *
     * @param aByte The byte to encrypt.
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing to the output stream.
     */
    public void write(final byte aByte, final OutputStream output ) throws IOException
    {
        b = (b + 1) % 256;
        c = (salt[b] + c) % 256;
        swap( salt, b, c );
        final int saltIndex = (salt[b] + salt[c]) % 256;
        output.write(aByte ^ (byte)salt[saltIndex]);
    }

    /**
     * This will encrypt and write the data.
     *
     * @param data The data to encrypt.
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing to the output stream.
     */
    public void write(final byte[] data, final OutputStream output ) throws IOException
    {
        for (final byte aData : data)
        {
            write(aData, output);
        }
    }

    /**
     * This will encrypt and write the data.
     *
     * @param data The data to encrypt.
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing to the output stream.
     */
    public void write(final InputStream data, final OutputStream output ) throws IOException
    {
        final byte[] buffer = new byte[1024];
        int amountRead;
        while( (amountRead = data.read( buffer )) != -1 )
        {
            write( buffer, 0, amountRead, output );
        }
    }

    /**
     * This will encrypt and write the data.
     *
     * @param data The data to encrypt.
     * @param offset The offset into the array to start reading data from.
     * @param len The number of bytes to attempt to read.
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing to the output stream.
     */
    public void write(final byte[] data, final int offset, final int len, final OutputStream output) throws IOException
    {
        for( int i = offset; i < offset + len; i++ )
        {
            write( data[i], output );
        }
    }
}
