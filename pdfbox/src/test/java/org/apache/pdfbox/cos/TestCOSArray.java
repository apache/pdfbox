/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.cos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unittests for {@link COSArray}
 */
class TestCOSArray
{
    @Test
    void testCreate()
    {
        COSArray cosArray = new COSArray();
        assertEquals(0, cosArray.size());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new COSArray(null),
                "Constructor should have thrown an exception");

        cosArray = new COSArray(Arrays.asList(COSName.A, COSName.B, COSName.C));
        assertEquals(3, cosArray.size());
        assertEquals(COSName.A, cosArray.get(0));
        assertEquals(COSName.B, cosArray.get(1));
        assertEquals(COSName.C, cosArray.get(2));
    }

    @Test
    void testConvertString2COSNameAndBack()
    {
        COSArray cosArray = COSArray.ofCOSNames(
                Arrays.asList(COSName.A.getName(), COSName.B.getName(), COSName.C.getName()));
        assertEquals(3, cosArray.size());
        assertEquals(COSName.A, cosArray.get(0));
        assertEquals(COSName.B, cosArray.get(1));
        assertEquals(COSName.C, cosArray.get(2));

        List<String> cosNameStringList = cosArray.toCOSNameStringList();
        assertEquals(3, cosNameStringList.size());
        assertEquals(COSName.A.getName(), cosNameStringList.get(0));
        assertEquals(COSName.B.getName(), cosNameStringList.get(1));
        assertEquals(COSName.C.getName(), cosNameStringList.get(2));
    }

    @Test
    void testConvertString2COSStringAndBack()
    {
        COSArray cosArray = COSArray
                .ofCOSStrings(Arrays.asList("A", "B", "C"));
        assertEquals(3, cosArray.size());
        assertEquals("A", cosArray.getString(0));
        assertEquals("B", cosArray.getString(1));
        assertEquals("C", cosArray.getString(2));

        List<String> cosStringStringList = cosArray.toCOSStringStringList();
        assertEquals(3, cosStringStringList.size());
        assertEquals("A", cosStringStringList.get(0));
        assertEquals("B", cosStringStringList.get(1));
        assertEquals("C", cosStringStringList.get(2));
    }

    @Test
    void testConvertInteger2COSStringAndBack()
    {
        COSArray cosArray = COSArray.ofCOSIntegers(Arrays.asList(1, 2, 3));
        assertEquals(3, cosArray.size());
        assertEquals(1, cosArray.getInt(0));
        assertEquals(2, cosArray.getInt(1));
        assertEquals(3, cosArray.getInt(2));

        List<Integer> cosNumberIntegerList = cosArray.toCOSNumberIntegerList();
        assertEquals(3, cosNumberIntegerList.size());
        assertEquals(1, (int) cosNumberIntegerList.get(0));
        assertEquals(2, (int) cosNumberIntegerList.get(1));
        assertEquals(3, (int) cosNumberIntegerList.get(2));

        // check arrays with null values
        cosArray = new COSArray(Arrays.asList(COSInteger.get(1), null, COSInteger.get(3)));
        assertEquals(3, cosArray.size());
        assertEquals(1, cosArray.getInt(0));
        assertNull(cosArray.get(1));
        assertEquals(3, cosArray.getInt(2));
        cosNumberIntegerList = cosArray.toCOSNumberIntegerList();
        assertEquals(3, cosNumberIntegerList.size());
        assertEquals(1, (int) cosNumberIntegerList.get(0));
        assertNull(cosNumberIntegerList.get(1));
        assertEquals(3, (int) cosNumberIntegerList.get(2));
    }

    @Test
    void testConvertFloat2COSStringAndBack()
    {
        float[] floatArrayStart = new float[] { 1.0f, 0.1f, 0.02f };
        COSArray cosArray = new COSArray();
        cosArray.setFloatArray(floatArrayStart);

        assertEquals(3, cosArray.size());
        assertEquals(COSFloat.ONE, cosArray.get(0));
        assertEquals(new COSFloat(0.1f), cosArray.get(1));
        assertEquals(new COSFloat(0.02f), cosArray.get(2));

        List<Float> cosNumberFloatList = cosArray.toCOSNumberFloatList();
        assertEquals(3, cosNumberFloatList.size());
        assertEquals(1.0f, (float) cosNumberFloatList.get(0), 0);
        assertEquals(0.1f, (float) cosNumberFloatList.get(1), 0);
        assertEquals(0.02f, (float) cosNumberFloatList.get(2), 0);

        float[] floatArrayEnd = cosArray.toFloatArray();
        assertEquals(1.0f, (float) cosNumberFloatList.get(0), 0);
        assertEquals(0.1f, (float) cosNumberFloatList.get(1), 0);
        assertEquals(0.02f, (float) cosNumberFloatList.get(2), 0);
        assertArrayEquals(floatArrayStart, floatArrayEnd, 0);

        // check arrays with null values
        cosArray = new COSArray(Arrays.asList(COSFloat.ONE, null, new COSFloat(0.02f)));
        assertEquals(3, cosArray.size());
        assertEquals(COSFloat.ONE, cosArray.get(0));
        assertNull(cosArray.get(1));
        assertEquals(new COSFloat(0.02f), cosArray.get(2));

        cosNumberFloatList = cosArray.toCOSNumberFloatList();
        assertEquals(3, cosNumberFloatList.size());
        assertEquals(1.0f, (float) cosNumberFloatList.get(0), 0);
        assertNull(cosNumberFloatList.get(1));
        assertEquals(0.02f, (float) cosNumberFloatList.get(2), 0);

        floatArrayEnd = cosArray.toFloatArray();
        // due to the null value the second value of the array is set to 0
        assertArrayEquals(new float[] { 1.0f, 0f, 0.02f }, floatArrayEnd, 0);

    }

    @Test
    void testGetSetName()
    {
        COSArray cosArray = new COSArray();
        cosArray.growToSize(3);
        cosArray.setName(0, "A");
        cosArray.setName(1, "B");
        cosArray.setName(2, "C");
        assertEquals(3, cosArray.size());
        assertEquals("A", cosArray.getName(0));
        assertEquals("B", cosArray.getName(1));
        assertEquals("C", cosArray.getName(2));
        assertEquals("NULL", cosArray.getName(3, "NULL"));
        assertEquals(0, cosArray.indexOf(COSName.A));
        assertEquals(1, cosArray.indexOf(COSName.B));
        assertEquals(2, cosArray.indexOf(COSName.C));
        assertEquals(-1, cosArray.indexOf(COSName.D));
        cosArray.setName(1, "D");
        assertEquals(3, cosArray.size());
        assertEquals("D", cosArray.getName(1));
    }

    @Test
    void testGetSetInt()
    {
        COSArray cosArray = new COSArray();
        cosArray.growToSize(3);
        cosArray.setInt(0, 0);
        cosArray.setInt(1, 1);
        cosArray.setInt(2, 2);
        assertEquals(3, cosArray.size());
        assertEquals(0, cosArray.getInt(0));
        assertEquals(1, cosArray.getInt(1));
        assertEquals(2, cosArray.getInt(2));
        assertEquals(0, cosArray.getInt(3, 0));
        assertEquals(0, cosArray.indexOf(COSInteger.get(0)));
        assertEquals(1, cosArray.indexOf(COSInteger.get(1)));
        assertEquals(2, cosArray.indexOf(COSInteger.get(2)));
        assertEquals(-1, cosArray.indexOf(COSInteger.get(3)));
        cosArray.setInt(1, 3);
        assertEquals(3, cosArray.size());
        assertEquals(3, cosArray.getInt(1));
    }

    @Test
    void testGetSetString()
    {
        COSArray cosArray = new COSArray();
        cosArray.growToSize(3);
        cosArray.setString(0, "Test1");
        cosArray.setString(1, "Test2");
        cosArray.setString(2, "Test3");
        assertEquals(3, cosArray.size());
        assertEquals("Test1", cosArray.getString(0));
        assertEquals("Test2", cosArray.getString(1));
        assertEquals("Test3", cosArray.getString(2));
        assertEquals("NULL", cosArray.getString(3, "NULL"));
        assertEquals(0, cosArray.indexOf(new COSString("Test1")));
        assertEquals(1, cosArray.indexOf(new COSString("Test2")));
        assertEquals(2, cosArray.indexOf(new COSString("Test3")));
        assertEquals(-1, cosArray.indexOf(new COSString("Test4")));
        cosArray.setString(1, "Test4");
        assertEquals(3, cosArray.size());
        assertEquals("Test4", cosArray.getString(1));
    }

    @Test
    void testRemove()
    {
        COSArray cosArray = COSArray
                .ofCOSIntegers(Arrays.asList(1, 2, 3, 4, 5, 6));
        cosArray.clear();
        assertEquals(0, cosArray.size());

        cosArray = COSArray.ofCOSIntegers(Arrays.asList(1, 2, 3, 4, 5, 6));
        assertEquals(COSInteger.get(3), cosArray.remove(2));
        // 1,2,4,5,6 should be left
        assertEquals(5, cosArray.size());
        assertEquals(1, cosArray.getInt(0));
        assertEquals(4, cosArray.getInt(2));

        // 1,2,4,6 should be left
        assertTrue(cosArray.removeObject(COSInteger.get(5)));
        assertEquals(4, cosArray.size());
        assertEquals(1, cosArray.getInt(0));
        assertEquals(4, cosArray.getInt(2));
        assertEquals(6, cosArray.getInt(3));

        cosArray = COSArray.ofCOSIntegers(Arrays.asList(1, 2, 3, 4, 5, 6));
        cosArray.removeAll(Arrays.asList(COSInteger.get(3), COSInteger.get(4)));
        // 1,2,5,6 should be left
        assertEquals(4, cosArray.size());
        assertEquals(2, cosArray.getInt(1));
        assertEquals(5, cosArray.getInt(2));

        cosArray = COSArray.ofCOSIntegers(Arrays.asList(1, 2, 3, 4, 5, 6));
        cosArray.retainAll(Arrays.asList(COSInteger.get(3), COSInteger.get(4)));
        // 3,4 should be left
        assertEquals(2, cosArray.size());
        assertEquals(3, cosArray.getInt(0));
        assertEquals(4, cosArray.getInt(1));

    }

    @Test
    void testGrowToSize()
    {
        COSArray cosArray = new COSArray();
        assertEquals(0, cosArray.size());
        cosArray.growToSize(2);
        // COSArray has 2 empty elements
        assertEquals(2, cosArray.size());
        // size is already 2 -> nothing happens
        cosArray.growToSize(2, COSInteger.get(0));
        assertEquals(2, cosArray.size());
        // increase size, fill the new elements with the given value
        cosArray.growToSize(4, COSInteger.get(1));
        assertEquals(4, cosArray.size());
        List<Integer> cosNumberIntegerList = cosArray.toCOSNumberIntegerList();
        assertEquals(4, cosNumberIntegerList.size());
        assertNull(cosNumberIntegerList.get(0));
        assertEquals(1, (int) cosNumberIntegerList.get(2));
        assertEquals(1, (int) cosNumberIntegerList.get(3));
    }

    @Test
    void testToList()
    {
        COSArray cosArray = COSArray
                .ofCOSIntegers(Arrays.asList(0, 1, 2, 3, 4, 5));
        List<? extends COSBase> list = cosArray.toList();
        assertEquals(6, list.size());
        assertEquals(COSInteger.get(0), list.get(0));
        assertEquals(COSInteger.get(5), list.get(5));
    }
}
