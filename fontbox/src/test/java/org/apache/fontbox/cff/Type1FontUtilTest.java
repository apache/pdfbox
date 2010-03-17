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

import static org.junit.Assert.assertArrayEquals;

import java.util.Random;
import org.junit.Test;

/**
 * This class includes some tests for the Type1FontUtil class.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class Type1FontUtilTest
{

    /**
     * Tests the hex encoding/decoding.
     */
    @Test
    public void hexEncoding()
    {
        byte[] bytes = randomBytes(128);

        String encodedBytes = Type1FontUtil.hexEncode(bytes);
        byte[] decodedBytes = Type1FontUtil.hexDecode(encodedBytes);

        assertArrayEquals(bytes, decodedBytes);
    }

    /**
     * Tests the eexec encryption/decryption.
     */
    @Test
    public void eexecEncryption()
    {
        byte[] bytes = randomBytes(128);

        byte[] encryptedBytes = Type1FontUtil.eexecEncrypt(bytes);
        byte[] decryptedBytes = Type1FontUtil.eexecDecrypt(encryptedBytes);

        assertArrayEquals(bytes, decryptedBytes);
    }

    /**
     * Tests the charstring encryption/decryption.
     */
    @Test
    public void charstringEncryption()
    {
        byte[] bytes = randomBytes(128);

        byte[] encryptedBytes = Type1FontUtil.charstringEncrypt(bytes, 4);
        byte[] decryptedBytes = Type1FontUtil.charstringDecrypt(encryptedBytes,
                4);

        assertArrayEquals(bytes, decryptedBytes);
    }

    private static byte[] randomBytes(int length)
    {
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++)
        {
            bytes[i] = (byte) RANDOM.nextInt(256);
        }

        return bytes;
    }

    private static final Random RANDOM = new Random();
}