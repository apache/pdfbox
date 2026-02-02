/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fontbox.cmap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Many CMaps are using the same values for the mapped strings. This class provides all common one- and two-byte
 * mappings to avoid duplicate strings.
 */
public class CMapStrings
{
    private static final List<String> twoByteMappings = new ArrayList<>(256 * 256);
    private static final List<String> oneByteMappings = new ArrayList<>(256);

    private static final List<Integer> indexValues = new ArrayList<>(256 * 256);
    private static final List<byte[]> oneByteValues = new ArrayList<>(256);
    private static final List<byte[]> twoByteValues = new ArrayList<>(256 * 256);

    static
    {
        // create all mappings when loading the class to avoid concurrency issues
        fillMappings();
    }

    private CMapStrings()
    {
    }

    private static void fillMappings()
    {
        for (int i = 0; i < 256; i++)
        {
            for (int j = 0; j < 256; j++)
            {
                byte[] bytes = { (byte) i, (byte) j };
                twoByteMappings.add(new String(bytes, StandardCharsets.UTF_16BE));
                twoByteValues.add(bytes);
                indexValues.add((i * 256) + j);
            }
        }
        for (int i = 0; i < 256; i++)
        {
            byte[] bytes = { (byte) i };
            oneByteMappings.add(new String(bytes, StandardCharsets.ISO_8859_1));
            oneByteValues.add(bytes);
        }
    }

    /**
     * Get the mapped string value for the given combination of bytes. The mapping is limited to one and two-byte
     * mappings. Any longer byte sequence produces null as return value.
     * 
     * @param bytes the given combination of bytes
     * @return the string representation for the given combination of bytes
     */
    public static String getMapping(byte[] bytes)
    {
        if (bytes.length > 2)
        {
            return null;
        }
        return bytes.length == 1 ? oneByteMappings.get(CMap.toInt(bytes))
                : twoByteMappings.get(CMap.toInt(bytes));
    }

    /**
     * Get an Integer instance of the given combination of bytes. Each value is a singleton to avoid multiple instances
     * for same value. The values are limited to one and two-byte sequences. Any longer byte sequence produces null as
     * return value.
     * 
     * @param bytes the given combination of bytes
     * @return the Integer representation for the given combination of bytes
     */
    public static Integer getIndexValue(byte[] bytes)
    {
        if (bytes.length > 2)
        {
            return null;
        }
        return indexValues.get(CMap.toInt(bytes));
    }

    /**
     * Get a singleton instance of the given combination of bytes to avoid multiple instances for same value. The values
     * are limited to one and two-byte sequences. Any longer byte sequence produces null as return value.
     * 
     * @param bytes the given combination of bytes
     * @return a singleton instance for the given combination of bytes
     */
    public static byte[] getByteValue(byte[] bytes)
    {
        if (bytes.length > 2)
        {
            return null;
        }
        return bytes.length == 1 ? oneByteValues.get(CMap.toInt(bytes))
                : twoByteValues.get(CMap.toInt(bytes));
    }
}
