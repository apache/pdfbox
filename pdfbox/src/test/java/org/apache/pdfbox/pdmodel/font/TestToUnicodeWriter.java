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

package org.apache.pdfbox.pdmodel.font;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author ryanjackson-wf
 */
class TestToUnicodeWriter
{
    @Test
    void testCMapLigatures() throws IOException
    {
        ToUnicodeWriter toUnicodeWriter = new ToUnicodeWriter();

        toUnicodeWriter.add(0x400, "a");
        toUnicodeWriter.add(0x401, "b");
        toUnicodeWriter.add(0x402, "ff");
        toUnicodeWriter.add(0x403, "fi");
        toUnicodeWriter.add(0x404, "ffl");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toUnicodeWriter.writeTo(baos);
        String output = baos.toString("ISO-8859-1");
        assertTrue(output.contains("4 beginbfrange"));
        assertTrue(output.contains("<0402> <0402> <00660066>"));
        assertTrue(output.contains("<0403> <0403> <00660069>"));
        assertTrue(output.contains("<0404> <0404> <00660066006C>"));
    }

    @Test
    void testCMapCIDOverflow() throws IOException
    {
        ToUnicodeWriter toUnicodeWriter = new ToUnicodeWriter();

        toUnicodeWriter.add(0x3ff, "6");
        toUnicodeWriter.add(0x400, "7");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toUnicodeWriter.writeTo(baos);
        String output = baos.toString("ISO-8859-1");

        assertTrue(output.contains("2 beginbfrange"));
        assertTrue(output.contains("<03FF> <03FF> <0036>"));
        assertTrue(output.contains("<0400> <0400> <0037>"));
    }

    @Test
    void testCMapStringOverflow() throws IOException
    {
        ToUnicodeWriter toUnicodeWriter = new ToUnicodeWriter();

        StringBuilder string1 = new StringBuilder();
        string1.appendCodePoint(0x04FF);
        StringBuilder string2 = new StringBuilder();
        string2.appendCodePoint(0x0500);
        toUnicodeWriter.add(0x3ff, string1.toString());
        toUnicodeWriter.add(0x400, string2.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toUnicodeWriter.writeTo(baos);
        String output = baos.toString("ISO-8859-1");

        assertTrue(output.contains("2 beginbfrange"));
        assertTrue(output.contains("<03FF> <03FF> <04FF>"));
        assertTrue(output.contains("<0400> <0400> <0500>"));
    }

    @Test
    void testCMapSurrogates() throws IOException
    {
        ToUnicodeWriter toUnicodeWriter = new ToUnicodeWriter();

        toUnicodeWriter.add(0x300, new String(new int[] { 0x2F874 }, 0, 1));
        toUnicodeWriter.add(0x301, new String(new int[] { 0x2F876 }, 0, 1));
        toUnicodeWriter.add(0x304, new String(new int[] { 0x2F884 }, 0, 1));
        toUnicodeWriter.add(0x305, new String(new int[] { 0x2F885 }, 0, 1));
        toUnicodeWriter.add(0x306, new String(new int[] { 0x2F886 }, 0, 1));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toUnicodeWriter.writeTo(baos);
        String output = baos.toString("ISO-8859-1");

        assertTrue(output.contains("3 beginbfrange"));
        assertTrue(output.contains("<0300> <0300> <D87EDC74>"));
        assertTrue(output.contains("<0301> <0301> <D87EDC76>"));
        assertTrue(output.contains("<0304> <0306> <D87EDC84>"));
    }

    @Test
    void testAllowCIDToUnicodeRange()
    {
        Map.Entry<Integer, String> six = new AbstractMap.SimpleEntry<>(0x03FF, "6");
        Map.Entry<Integer, String> seven = new AbstractMap.SimpleEntry<>(0x0400,
                "7");
        Map.Entry<Integer, String> eight = new AbstractMap.SimpleEntry<>(0x0401,
                "8");

        assertFalse(ToUnicodeWriter.allowCIDToUnicodeRange(null, seven));
        assertFalse(ToUnicodeWriter.allowCIDToUnicodeRange(six, null));
        assertFalse(ToUnicodeWriter.allowCIDToUnicodeRange(six, seven));
        assertTrue(ToUnicodeWriter.allowCIDToUnicodeRange(seven, eight));
    }

    @Test
    void testAllowCodeRange()
    {
        // Denied progressions (negative)
        assertFalse(ToUnicodeWriter.allowCodeRange(0x000F, 0x0007));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x00FF, 0x0000));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x03FF, 0x0300));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0401, 0x0400));
        assertFalse(ToUnicodeWriter.allowCodeRange(0xFFFF, 0x0000));

        // Denied progressions (non sequential)
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0000, 0x0000));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0000, 0x000F));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0000, 0x007F));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0000, 0x00FF));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0007, 0x000F));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x007F, 0x00FF));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x00FF, 0x00FF));

        // Denied progressions (overflow)
        assertFalse(ToUnicodeWriter.allowCodeRange(0x00FF, 0x0100));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x01FF, 0x0200));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x03FF, 0x0400));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x07FF, 0x0800));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x0FFF, 0x1000));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x1FFF, 0x2000));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x3FFF, 0x4000));
        assertFalse(ToUnicodeWriter.allowCodeRange(0x7FFF, 0x8000));

        // Allowed progressions (positive, sequential, and w/o overflow)
        assertTrue(ToUnicodeWriter.allowCodeRange(0x00, 0x01));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x01, 0x02));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x03, 0x04));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x07, 0x08));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x0E, 0x0F));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x1F, 0x20));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x3F, 0x40));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x7F, 0x80));
        assertTrue(ToUnicodeWriter.allowCodeRange(0xFE, 0xFF));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x03FE, 0x03FF));
        assertTrue(ToUnicodeWriter.allowCodeRange(0x0400, 0x0401));
        assertTrue(ToUnicodeWriter.allowCodeRange(0xFFFE, 0xFFFF));
    }

    @Test
    void testAllowDestinationRange()
    {
        // Denied (bogus)
        assertFalse(ToUnicodeWriter.allowDestinationRange("", ""));
        assertFalse(ToUnicodeWriter.allowDestinationRange("0", ""));
        assertFalse(ToUnicodeWriter.allowDestinationRange("", "0"));

        // Denied (non sequential)
        assertFalse(ToUnicodeWriter.allowDestinationRange("0", "A"));
        assertFalse(ToUnicodeWriter.allowDestinationRange("A", "a"));

        // Denied (overflow)
        assertFalse(ToUnicodeWriter.allowDestinationRange("ÿ", "Ā"));

        // Allowed (sequential w/o surrogate)
        assertTrue(ToUnicodeWriter.allowDestinationRange(" ", "!"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("(", ")"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("0", "1"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("a", "b"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("A", "B"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("À", "Á"));
        assertTrue(ToUnicodeWriter.allowDestinationRange("þ", "ÿ"));

        // Denied (ligatures)
        assertFalse(ToUnicodeWriter.allowDestinationRange("ff", "fi"));
    }

    @Test
    void testAllowDestinationRangeSurrogates()
    {
        // Check surrogates
        StringBuilder endOfBMP = new StringBuilder();
        endOfBMP.appendCodePoint(0xFFFF);

        StringBuilder beyondBMP = new StringBuilder();
        beyondBMP.appendCodePoint(0x10000);

        StringBuilder cjk1 = new StringBuilder();
        cjk1.appendCodePoint(0x2F884);

        StringBuilder cjk2 = new StringBuilder();
        cjk2.appendCodePoint(0x2F885);

        StringBuilder cjk3 = new StringBuilder();
        cjk3.appendCodePoint(0x2F886);

        // Denied (overflow)
        assertFalse(
                ToUnicodeWriter.allowDestinationRange(endOfBMP.toString(), beyondBMP.toString()));
        // Allowed (sequential surrogates)
        assertTrue(ToUnicodeWriter.allowDestinationRange(cjk1.toString(), cjk2.toString()));
        assertTrue(ToUnicodeWriter.allowDestinationRange(cjk2.toString(), cjk3.toString()));
        // Denied (non sequential surrogates)
        assertFalse(ToUnicodeWriter.allowDestinationRange(cjk1.toString(), cjk3.toString()));
    }

}