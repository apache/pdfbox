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
package org.apache.fontbox.cmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CMapStringsTest
{

    @Test
    void getNonCachedMappings()
    {
        // arrays consisting of more than 2 bytes aren't cached.
        assertNull(CMapStrings.getMapping(new byte[] { 0, 0, 0 }));
        assertNull(CMapStrings.getMapping(new byte[] { 0, 0, 0, 0 }));
    }

    @Test
    void getMappingOneByte()
    {
        byte[] minValueOneByte = new byte[] { 0 };
        String minValueMapping = new String(minValueOneByte, StandardCharsets.ISO_8859_1);
        // the given values are equal
        assertEquals(CMapStrings.getMapping(minValueOneByte),
                CMapStrings.getMapping(minValueOneByte));
        // the given values are the same objects
        assertSame(
                CMapStrings.getMapping(minValueOneByte), CMapStrings.getMapping(minValueOneByte));
        // check the mapped string value
        assertEquals(minValueMapping, CMapStrings.getMapping(minValueOneByte));

        byte[] maxValueOneByte = new byte[] { (byte) 0xff };
        String maxValueMapping = new String(maxValueOneByte, StandardCharsets.ISO_8859_1);
        assertEquals(CMapStrings.getMapping(maxValueOneByte),
                CMapStrings.getMapping(maxValueOneByte));
        assertSame(
                CMapStrings.getMapping(maxValueOneByte), CMapStrings.getMapping(maxValueOneByte));
        assertEquals(maxValueMapping, CMapStrings.getMapping(maxValueOneByte));

        byte[] anyValueOneByte = new byte[] { 98 };
        String anyValueMapping = new String(anyValueOneByte, StandardCharsets.ISO_8859_1);
        assertEquals(CMapStrings.getMapping(anyValueOneByte),
                CMapStrings.getMapping(anyValueOneByte));
        assertSame(
                CMapStrings.getMapping(anyValueOneByte), CMapStrings.getMapping(anyValueOneByte));
        assertEquals(anyValueMapping, CMapStrings.getMapping(anyValueOneByte));
    }

    @Test
    void getMappingTwoByte()
    {
        byte[] minValueTwoByte = new byte[] { 0, 0 };
        String minValueMapping = new String(minValueTwoByte, StandardCharsets.UTF_16BE);
        // the given values are equal
        assertEquals(CMapStrings.getMapping(minValueTwoByte),
                CMapStrings.getMapping(minValueTwoByte));
        // the given values are the same objects
        assertSame(
                CMapStrings.getMapping(minValueTwoByte), CMapStrings.getMapping(minValueTwoByte));
        // check the mapped string value
        assertEquals(minValueMapping, CMapStrings.getMapping(minValueTwoByte));

        byte[] maxValueTwoByte = new byte[] { (byte) 0xff, (byte) 0xff };
        String maxValueMapping = new String(maxValueTwoByte, StandardCharsets.UTF_16BE);
        assertEquals(CMapStrings.getMapping(maxValueTwoByte),
                CMapStrings.getMapping(maxValueTwoByte));
        assertSame(
                CMapStrings.getMapping(maxValueTwoByte), CMapStrings.getMapping(maxValueTwoByte));
        assertEquals(maxValueMapping, CMapStrings.getMapping(maxValueTwoByte));

        byte[] anyValueTwoByte1 = new byte[] { 0x62, 0x43 };
        String anyValueMapping1 = new String(anyValueTwoByte1, StandardCharsets.UTF_16BE);
        assertEquals(CMapStrings.getMapping(anyValueTwoByte1),
                CMapStrings.getMapping(anyValueTwoByte1));
        assertSame(CMapStrings.getMapping(anyValueTwoByte1), CMapStrings
                .getMapping(anyValueTwoByte1));
        assertEquals(anyValueMapping1, CMapStrings.getMapping(anyValueTwoByte1));

        byte[] anyValueTwoByte2 = new byte[] { (byte) 0xff, 0x43 };
        String anyValueMapping2 = new String(anyValueTwoByte2, StandardCharsets.UTF_16BE);
        assertEquals(CMapStrings.getMapping(anyValueTwoByte2),
                CMapStrings.getMapping(anyValueTwoByte2));
        assertSame(CMapStrings.getMapping(anyValueTwoByte2), CMapStrings
                .getMapping(anyValueTwoByte2));
        assertEquals(anyValueMapping2, CMapStrings.getMapping(anyValueTwoByte2));

        byte[] anyValueTwoByte3 = new byte[] { 0x38, (byte) 0xff };
        String anyValueMapping3 = new String(anyValueTwoByte3, StandardCharsets.UTF_16BE);
        assertEquals(CMapStrings.getMapping(anyValueTwoByte3),
                CMapStrings.getMapping(anyValueTwoByte3));
        assertSame(CMapStrings.getMapping(anyValueTwoByte3), CMapStrings
                .getMapping(anyValueTwoByte3));
        assertEquals(anyValueMapping3, CMapStrings.getMapping(anyValueTwoByte3));
    }

    @Test
    void getByteValuesOneByte()
    {
        byte[] minValueOneByte = new byte[] { 0 };
        // the given values are equal
        assertEquals(CMapStrings.getByteValue(minValueOneByte),
                CMapStrings.getByteValue(minValueOneByte));
        // the given values are the same objects
        assertSame(CMapStrings.getByteValue(minValueOneByte), CMapStrings
                .getByteValue(minValueOneByte));
        // the cached value isn't the same object than the given one
        assertNotSame(minValueOneByte, CMapStrings.getByteValue(minValueOneByte));

        byte[] maxValueOneByte = new byte[] { (byte) 0xff };
        assertEquals(CMapStrings.getByteValue(maxValueOneByte),
                CMapStrings.getByteValue(maxValueOneByte));
        assertSame(CMapStrings.getByteValue(maxValueOneByte), CMapStrings
                .getByteValue(maxValueOneByte));
        assertNotSame(maxValueOneByte, CMapStrings.getByteValue(maxValueOneByte));

        byte[] anyValueOneByte = new byte[] { 98 };
        assertEquals(CMapStrings.getByteValue(anyValueOneByte),
                CMapStrings.getByteValue(anyValueOneByte));
        assertSame(CMapStrings.getByteValue(anyValueOneByte), CMapStrings
                .getByteValue(anyValueOneByte));
        assertNotSame(anyValueOneByte, CMapStrings.getByteValue(anyValueOneByte));
    }

    @Test
    void getByteValuesTwoByte()
    {
        byte[] minValueTwoByte = new byte[] { 0, 0 };
        // the given values are equal
        assertEquals(CMapStrings.getByteValue(minValueTwoByte),
                CMapStrings.getByteValue(minValueTwoByte));
        // the given values are the same objects
        assertSame(CMapStrings.getByteValue(minValueTwoByte), CMapStrings
                .getByteValue(minValueTwoByte));
        // the cached value isn't the same object than the given one
        assertNotSame(minValueTwoByte, CMapStrings.getByteValue(minValueTwoByte));

        byte[] maxValueTwoByte = new byte[] { (byte) 0xff, (byte) 0xff };
        assertEquals(CMapStrings.getByteValue(maxValueTwoByte),
                CMapStrings.getByteValue(maxValueTwoByte));
        assertSame(CMapStrings.getByteValue(maxValueTwoByte), CMapStrings
                .getByteValue(maxValueTwoByte));
        assertNotSame(maxValueTwoByte, CMapStrings.getByteValue(maxValueTwoByte));

        byte[] anyValueTwoByte1 = new byte[] { 0x62, 0x43 };
        assertEquals(CMapStrings.getByteValue(anyValueTwoByte1),
                CMapStrings.getByteValue(anyValueTwoByte1));
        assertSame(CMapStrings.getByteValue(anyValueTwoByte1), CMapStrings
                .getByteValue(anyValueTwoByte1));
        assertNotSame(anyValueTwoByte1, CMapStrings.getByteValue(anyValueTwoByte1));

        byte[] anyValueTwoByte2 = new byte[] { (byte) 0xff, 0x43 };
        assertEquals(CMapStrings.getByteValue(anyValueTwoByte2),
                CMapStrings.getByteValue(anyValueTwoByte2));
        assertSame(CMapStrings.getByteValue(anyValueTwoByte2), CMapStrings
                .getByteValue(anyValueTwoByte2));
        assertNotSame(anyValueTwoByte2, CMapStrings.getByteValue(anyValueTwoByte2));

        byte[] anyValueTwoByte3 = new byte[] { 0x38, (byte) 0xff };
        assertEquals(CMapStrings.getByteValue(anyValueTwoByte3),
                CMapStrings.getByteValue(anyValueTwoByte3));
        assertSame(CMapStrings.getByteValue(anyValueTwoByte3), CMapStrings
                .getByteValue(anyValueTwoByte3));
        assertNotSame(anyValueTwoByte3, CMapStrings.getByteValue(anyValueTwoByte3));
    }

    @Test
    void getNonCachedByteValues()
    {
        // arrays consisting of more than 2 bytes aren't cached.
        assertNull(CMapStrings.getByteValue(new byte[] { 0, 0, 0 }));
        assertNull(CMapStrings.getByteValue(new byte[] { 0, 0, 0, 0 }));
    }

    @Test
    void getIndexValuesOneByte()
    {
        byte[] minValueOneByte = new byte[] { 0 };
        // the given values are equal
        assertEquals(CMapStrings.getIndexValue(minValueOneByte),
                CMapStrings.getIndexValue(minValueOneByte));
        // the given values are the same objects
        assertSame(CMapStrings.getIndexValue(minValueOneByte), CMapStrings
                .getIndexValue(minValueOneByte));
        // check the int value
        assertEquals(0, CMapStrings.getIndexValue(minValueOneByte));

        byte[] maxValueOneByte = new byte[] { (byte) 0xff };
        assertEquals(CMapStrings.getIndexValue(maxValueOneByte),
                CMapStrings.getIndexValue(maxValueOneByte));
        assertSame(CMapStrings.getIndexValue(maxValueOneByte), CMapStrings
                .getIndexValue(maxValueOneByte));
        assertEquals(0xff, CMapStrings.getIndexValue(maxValueOneByte));

        byte[] anyValueOneByte = new byte[] { 98 };
        assertEquals(CMapStrings.getIndexValue(anyValueOneByte),
                CMapStrings.getIndexValue(anyValueOneByte));
        assertSame(CMapStrings.getIndexValue(anyValueOneByte), CMapStrings
                .getIndexValue(anyValueOneByte));
        assertEquals(98, CMapStrings.getIndexValue(anyValueOneByte));
    }

    @Test
    void getIndexValuesTwoByte()
    {
        byte[] minValueTwoByte = new byte[] { 0, 0 };
        // the given values are equal
        assertEquals(CMapStrings.getIndexValue(minValueTwoByte),
                CMapStrings.getIndexValue(minValueTwoByte));
        // the given values are the same objects
        assertSame(CMapStrings.getIndexValue(minValueTwoByte), CMapStrings
                .getIndexValue(minValueTwoByte));
        // check the int value
        assertEquals(0, CMapStrings.getIndexValue(minValueTwoByte));

        byte[] maxValueTwoByte = new byte[] { (byte) 0xff, (byte) 0xff };
        assertEquals(CMapStrings.getIndexValue(maxValueTwoByte),
                CMapStrings.getIndexValue(maxValueTwoByte));
        assertSame(CMapStrings.getIndexValue(maxValueTwoByte), CMapStrings
                .getIndexValue(maxValueTwoByte));
        assertEquals(0xffff, CMapStrings.getIndexValue(maxValueTwoByte));

        byte[] anyValueTwoByte1 = new byte[] { 0x62, 0x43 };
        assertEquals(CMapStrings.getIndexValue(anyValueTwoByte1),
                CMapStrings.getIndexValue(anyValueTwoByte1));
        assertSame(CMapStrings.getIndexValue(anyValueTwoByte1), CMapStrings
                .getIndexValue(anyValueTwoByte1));
        assertEquals(0x6243, CMapStrings.getIndexValue(anyValueTwoByte1));

        byte[] anyValueTwoByte2 = new byte[] { (byte) 0xff, 0x43 };
        assertEquals(CMapStrings.getIndexValue(anyValueTwoByte2),
                CMapStrings.getIndexValue(anyValueTwoByte2));
        assertSame(CMapStrings.getIndexValue(anyValueTwoByte2), CMapStrings
                .getIndexValue(anyValueTwoByte2));
        assertEquals(0xff43, CMapStrings.getIndexValue(anyValueTwoByte2));

        byte[] anyValueTwoByte3 = new byte[] { 0x38, (byte) 0xff };
        assertEquals(CMapStrings.getIndexValue(anyValueTwoByte3),
                CMapStrings.getIndexValue(anyValueTwoByte3));
        assertSame(CMapStrings.getIndexValue(anyValueTwoByte3), CMapStrings
                .getIndexValue(anyValueTwoByte3));
        assertEquals(0x38ff, CMapStrings.getIndexValue(anyValueTwoByte3));
    }

    @Test
    void getNonCachedIndexValues()
    {
        // arrays consisting of more than 2 bytes aren't cached.
        assertNull(CMapStrings.getIndexValue(new byte[] { 0, 0, 0 }));
        assertNull(CMapStrings.getIndexValue(new byte[] { 0, 0, 0, 0 }));
    }

}
