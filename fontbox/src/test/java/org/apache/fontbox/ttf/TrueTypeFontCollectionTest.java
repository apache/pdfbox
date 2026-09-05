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
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrueTypeFontCollectionTest
{
    @Test
    void testMissingTtcHeader()
    {
        IOException ex = assertThrows(IOException.class,
                () -> new TrueTypeCollection(new ByteArrayInputStream(new byte[4])),
                "Missing ttc header not detected!");
        assertEquals("Missing TTC header", ex.getMessage());
    }

    @Test
    void testNumberOfFonts()
    {
        byte[] payload = { 0x74, 0x74, 0x63, 0x66, 0x00, 0x00, 0x00, 0x00, 0x7F, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        IOException ex = assertThrows(IOException.class,
                () -> new TrueTypeCollection(new ByteArrayInputStream(payload)),
                "Invalid number of fonts not detected!");
        assertEquals("Invalid number of fonts 2147483647", ex.getMessage());
    }

    @ParameterizedTest
    @EnabledOnOs(OS.WINDOWS)
    @CsvSource(value = {
        "c:/windows/fonts/mingliu.ttc;[MingLiU, PMingLiU, Ming-Lt-HKSCS-UNI-H]",
        "c:/windows/fonts/msmincho.ttc;[MS-Mincho, MS-PMincho]",
        "c:/windows/fonts/simsun.ttc;[SimSun, NSimSun]"},
            delimiter = ';')
    void testOnWindows(String filename, String listText) throws IOException
    {
        checkTrueTypeCollection(filename, listText);
    }

    @ParameterizedTest
    @EnabledOnOs(OS.MAC)
    @CsvSource(value = {
        "/System/Library/Fonts/LucidaGrande.ttc;[LucidaGrande, LucidaGrande-Bold, .LucidaGrandeUI, .LucidaGrandeUI-Bold]",
        "/System/Library/Fonts/Avenir.ttc;[Avenir-Book, Avenir-BookOblique, Avenir-Black, Avenir-BlackOblique, "
                        + "Avenir-Heavy, Avenir-HeavyOblique, Avenir-Light, Avenir-LightOblique, "
                        + "Avenir-Medium, Avenir-MediumOblique, Avenir-Oblique, Avenir-Roman]"},
            delimiter = ';')
    void testOnMac(String filename, String listText) throws IOException
    {
        checkTrueTypeCollection(filename, listText);
    }

    // test with https://raw.githubusercontent.com/notofonts/noto-cjk/main/Sans/OTC/NotoSansCJK-Regular.ttc
    // could be possible, but that one is 19MB

    private void checkTrueTypeCollection(String filename, String expected) throws IOException
    {
        File file = new File(filename);
        assumeTrue(file.exists());
        try (TrueTypeCollection ttc = new TrueTypeCollection(file))
        {
            List<String> list = new ArrayList();
            ttc.processAllFonts((TrueTypeFont ttf) ->
            {
                list.add(ttf.getName());
                try (TrueTypeFont ttfByName = ttc.getFontByName(ttf.getName()))
                {
                    assertEquals(ttf.getName(), ttfByName.getName());
                }
            });
            assertEquals(expected, list.toString());
        }
        List<String> list = new ArrayList();
        TrueTypeCollection.processAllFontHeaders(file, fontHeaders -> list.add(fontHeaders.getName()));
        assertEquals(expected, list.toString());
    }
}