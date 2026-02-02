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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.Language;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A bunch of tests on {@link GlyphSubstitutionTable} for {@code LiberationSans-Regular} font
 *
 * @author Vladimir Plizga
 */
class GlyphSubstitutionTableLiberationFontTest
{

    private OpenTypeFont font;

    @BeforeEach
    void setUp() throws IOException
    {
        OTFParser otfParser = new OTFParser();
        String fontPath = "src/test/resources/ttf/LiberationSans-Regular.ttf";
        try (RandomAccessRead fontFile = new RandomAccessReadBufferedFile(fontPath))
        {
            font = otfParser.parse(fontFile);
        }
    }

    @AfterEach
    void tearDown() throws IOException
    {
        font.close();
    }

    @Test
    @DisplayName("getGsubData() with no args yields latn")
    void getGsubDataDefault() throws IOException
    {
        // given

        // when
        GsubData gsubData = font.getGsubData();

        // then
        assertEquals("latn", gsubData.getActiveScriptName());
    }

    @Test
    @DisplayName("getGsubData() for an unsupported script yields null")
    void getGsubDataForUnsupportedScriptTag() throws IOException
    {
        // given
        GlyphSubstitutionTable gsub = font.getGsub();

        // when
        GsubData gsubData = gsub.getGsubData("<some_non_existent_script_tag>");

        // then
        assertNull(gsubData);
    }

    @Test
    @DisplayName("getGsubData() for 'cyrl' tag yields GSUB features of Cyrillic script")
    void testGetGsubDataForCyrillic() throws IOException
    {
        // given
        GlyphSubstitutionTable gsub = font.getGsub();
        String cyrillicScriptTag = "cyrl";
        List<String> expectedFeatures = asList("subs", "sups");

        // when
        GsubData cyrillicGsubData = gsub.getGsubData(cyrillicScriptTag);

        // then
        assertNotNull(cyrillicGsubData);
        assertEquals(cyrillicScriptTag, cyrillicGsubData.getActiveScriptName());
        assertEquals(new HashSet<>(expectedFeatures), cyrillicGsubData.getSupportedFeatures());
    }

    @Test
    @DisplayName("All the script tags are loaded from GSUB as is")
    void getSupportedScriptTags() throws IOException
    {
        // given
        GlyphSubstitutionTable gsub = font.getGsub();
        List<String> expectedSet = asList("DFLT", "bopo", "copt", "cyrl", "grek", "hebr", "latn");

        // when
        Set<String> supportedScriptTags = gsub.getSupportedScriptTags();

        // then
        assertEquals(new HashSet<>(expectedSet), supportedScriptTags);
    }

    @DisplayName("GSUB data is loaded for all scripts supported by the font")
    @ParameterizedTest
    @ValueSource(strings = { "DFLT", "bopo", "copt", "cyrl", "grek", "hebr", "latn" })
    void checkGsubDataLoadingForAllSupportedScripts(String scriptTag) throws IOException
    {
        // given
        GlyphSubstitutionTable gsub = font.getGsub();

        // when
        GsubData gsubData = gsub.getGsubData(scriptTag);

        // then
        assertNotNull(gsubData);
        assertNotSame(GsubData.NO_DATA_FOUND, gsubData);

        assertEquals(Language.UNSPECIFIED, gsubData.getLanguage());
        assertEquals(scriptTag, gsubData.getActiveScriptName());
    }

}