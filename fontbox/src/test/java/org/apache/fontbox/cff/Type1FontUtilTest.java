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
import junit.framework.TestCase;
import static org.junit.Assert.assertArrayEquals;
import org.junit.internal.ArrayComparisonFailure;

/**
 * This class includes some tests for the Type1FontUtil class.
 *
 * @author Villu Ruusmann
 */
public class Type1FontUtilTest extends TestCase
{
    static final long DEFAULTSEED = 12345;
    static final long LOOPS = 1000;

    /**
     * Tests the hex encoding/decoding.
     */
    public void testHexEncoding()
    {
        long seed = DEFAULTSEED;
        tryHexEncoding(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryHexEncoding(System.currentTimeMillis());
        }
    }

    private void tryHexEncoding(long seed) throws ArrayComparisonFailure
    {
        byte[] bytes = createRandomByteArray(128, seed);

        String encodedBytes = Type1FontUtil.hexEncode(bytes);
        byte[] decodedBytes = Type1FontUtil.hexDecode(encodedBytes);
        
        assertArrayEquals("Seed: " + seed, bytes, decodedBytes);
    }

    /**
     * Tests the eexec encryption/decryption.
     */
    public void testEexecEncryption()
    {
        long seed = DEFAULTSEED;
        tryEexecEncryption(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryEexecEncryption(System.currentTimeMillis());
        }
    }

    private void tryEexecEncryption(long seed) throws ArrayComparisonFailure
    {
        byte[] bytes = createRandomByteArray(128, seed);

        byte[] encryptedBytes = Type1FontUtil.eexecEncrypt(bytes);
        byte[] decryptedBytes = Type1FontUtil.eexecDecrypt(encryptedBytes);

        assertArrayEquals("Seed: " + seed, bytes, decryptedBytes);
    }

    /**
     * Tests the charstring encryption/decryption.
     */
    public void testCharstringEncryption()
    {
        long seed = DEFAULTSEED;
        tryCharstringEncryption(seed);
        for (int i = 0; i < LOOPS; ++i)
        {
            tryCharstringEncryption(System.currentTimeMillis());
        }
    }

    private void tryCharstringEncryption(long seed) throws ArrayComparisonFailure
    {
        byte[] bytes = createRandomByteArray(128, seed);

        byte[] encryptedBytes = Type1FontUtil.charstringEncrypt(bytes, 4);
        byte[] decryptedBytes = Type1FontUtil.charstringDecrypt(encryptedBytes, 4);

        assertArrayEquals("Seed: " + seed, bytes, decryptedBytes);
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
