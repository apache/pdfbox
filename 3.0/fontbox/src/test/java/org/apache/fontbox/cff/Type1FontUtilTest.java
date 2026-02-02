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

import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

/**
 * This class includes some tests for the Type1FontUtil class.
 *
 * @author Villu Ruusmann
 */
class Type1FontUtilTest
{
    static final long DEFAULTSEED = 12345;
    static final long LOOPS = 1000;

    /**
     * Tests the hex encoding/decoding.
     */
    @Test
    void testHexEncoding()
    {
        long seed = DEFAULTSEED;
        tryHexEncoding(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryHexEncoding(System.currentTimeMillis());
        }
    }

    private void tryHexEncoding(long seed)
    {
        byte[] bytes = createRandomByteArray(128, seed);

        String encodedBytes = Type1FontUtil.hexEncode(bytes);
        byte[] decodedBytes = Type1FontUtil.hexDecode(encodedBytes);
        
        assertArrayEquals(bytes, decodedBytes, "Seed: " + seed);
    }

    /**
     * Tests the eexec encryption/decryption.
     */
    @Test
    void testEexecEncryption()
    {
        long seed = DEFAULTSEED;
        tryEexecEncryption(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryEexecEncryption(System.currentTimeMillis());
        }
    }

    private void tryEexecEncryption(long seed)
    {
        byte[] bytes = createRandomByteArray(128, seed);

        byte[] encryptedBytes = Type1FontUtil.eexecEncrypt(bytes);
        byte[] decryptedBytes = Type1FontUtil.eexecDecrypt(encryptedBytes);

        assertArrayEquals(bytes, decryptedBytes, "Seed: " + seed);
    }

    /**
     * Tests the charstring encryption/decryption.
     */
    @Test
    void testCharstringEncryption()
    {
        long seed = DEFAULTSEED;
        tryCharstringEncryption(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryCharstringEncryption(System.currentTimeMillis());
        }
    }

    private void tryCharstringEncryption(long seed)
    {
        byte[] bytes = createRandomByteArray(128, seed);

        byte[] encryptedBytes = Type1FontUtil.charstringEncrypt(bytes, 4);
        byte[] decryptedBytes = Type1FontUtil.charstringDecrypt(encryptedBytes, 4);

        assertArrayEquals(bytes, decryptedBytes, "Seed: " + seed);
    }

    private static byte[] createRandomByteArray(int arrayLength, long seed)
    {
        byte[] bytes = new byte[arrayLength];
        Random ramdom = new Random(seed);

        for (int i = 0; i < arrayLength; i++)
        {
            bytes[i] = (byte) ramdom.nextInt(256);
        }
        return bytes;
    }
}
