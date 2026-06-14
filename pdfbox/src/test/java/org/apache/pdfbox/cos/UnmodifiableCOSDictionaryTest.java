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
package org.apache.pdfbox.cos;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Calendar;

import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.junit.jupiter.api.Test;

class UnmodifiableCOSDictionaryTest
{
    @Test
    void testUnmodifiableCOSDictionary()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class, unmodifiableCOSDictionary::clear);

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.removeItem(COSName.A));

        COSDictionary cosDictionary = new COSDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.addAll(cosDictionary));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setFlag(COSName.A, 0, true));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setNeedToBeUpdated(true));
    }

    @Test
    void testSetItem()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setItem(COSName.A, COSName.A));

        Encoding standardEncoding = Encoding.getInstance(COSName.STANDARD_ENCODING);

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setItem(COSName.A, standardEncoding));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setItem("A", COSName.A));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setItem("A", standardEncoding));
    }

    @Test
    void testSetBoolean()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setBoolean(COSName.A, true));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setBoolean("A", true));
    }

    @Test
    void testSetName()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setName(COSName.A, "A"));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setName("A", "A"));
    }

    @Test
    void testSetDate()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        Calendar calendar = Calendar.getInstance();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setDate(COSName.A, calendar));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setDate("A", calendar));
    }

    @Test
    void testSetEmbeddedDate()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        Calendar calendar = Calendar.getInstance();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setEmbeddedDate(
                        COSName.PARAMS, COSName.A, calendar));
    }

    @Test
    void testSetString()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setString(COSName.A, "A"));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setString("A", "A"));
    }

    @Test
    void testSetEmbeddedString()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setEmbeddedString(
                        COSName.PARAMS, COSName.A, "A"));
    }

    @Test
    void testSetInt()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setInt(COSName.A, 0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setInt("A", 0));
    }

    @Test
    void testSetEmbeddedInt()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setEmbeddedInt(
                        COSName.PARAMS, COSName.A, 0));
    }

    @Test
    void testSetLong()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setLong(COSName.A, 0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setLong("A", 0));
    }

    @Test
    void testSetFloat()
    {
        COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setFloat(COSName.A, 0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableCOSDictionary.setFloat("A", 0));
    }
}