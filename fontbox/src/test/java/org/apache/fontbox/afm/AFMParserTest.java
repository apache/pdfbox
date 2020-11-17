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

package org.apache.fontbox.afm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.fontbox.util.BoundingBox;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class AFMParserTest
{
    @Test
    void testStartFontMetrics() throws IOException
    {
        try
        {
            new AFMParser(new ByteArrayInputStream("huhu".getBytes(StandardCharsets.US_ASCII)))
                    .parse();
            fail("The AFMParser should have thrown an IOException because of a missing "
                    + AFMParser.START_FONT_METRICS);
        }
        catch (IOException e)
        {
            // expected exception
        }
    }

    @Test
    void testEndFontMetrics() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/NoEndFontMetrics.afm"));
        try
        {
            parser.parse();
            fail("The AFMParser should have thrown an IOException because of a missing "
                    + AFMParser.END_FONT_METRICS);
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("Unknown AFM key"));
        }
    }

    @Test
    void testMalformedFloat() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/MalformedFloat.afm"));
        try
        {
            parser.parse();
            fail("The AFMParser should have thrown an IOException because of a malformed float value");
        }
        catch (IOException e)
        {
            assertTrue(e.getCause() instanceof NumberFormatException);
            assertTrue(e.getMessage().contains("4,1ab"));
        }
    }

    @Test
    void testMalformedInteger() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/MalformedInteger.afm"));
        try
        {
            parser.parse();
            fail("The AFMParser should have thrown an IOException because of a malformed int value");
        }
        catch (IOException e)
        {
            assertTrue(e.getCause() instanceof NumberFormatException);
            assertTrue(e.getMessage().contains("3.4"));
        }
    }

    @Test
    void testHelveticaFontMetrics() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        checkHelveticaFontMetrics(parser.parse());
    }

    @Test
    void testHelveticaCharMetrics() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse();

        // char metrics
        checkHelveticaCharMetrics(fontMetrics.getCharMetrics());
    }

    @Test
    void testHelveticaKernPairs() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse();

        // KernPairs
        List<KernPair> kernPairs = fontMetrics.getKernPairs();
        assertEquals(2705, kernPairs.size());
        // check "KPX A Ucircumflex -50"
        checkKernPair(kernPairs, "A", "Ucircumflex", -50, 0);
        // check "KPX W agrave -40"
        checkKernPair(kernPairs, "W", "agrave", -40, 0);
        // KernPairs0
        assertTrue(fontMetrics.getKernPairs0().isEmpty());
        // KernPairs1
        assertTrue(fontMetrics.getKernPairs1().isEmpty());
        // composite data
        assertTrue(fontMetrics.getComposites().isEmpty());
    }

    @Test
    void testHelveticaFontMetricsReducedDataset() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        checkHelveticaFontMetrics(parser.parse(true));
    }

    @Test
    void testHelveticaCharMetricsReducedDataset() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse(true);

        // char metrics
        checkHelveticaCharMetrics(fontMetrics.getCharMetrics());
    }

    @Test
    void testHelveticaKernPairsReducedDataset() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse(true);

        // KernPairs, empty due to reducedDataset == true
        assertTrue(fontMetrics.getKernPairs().isEmpty());
        // KernPairs0
        assertTrue(fontMetrics.getKernPairs0().isEmpty());
        // KernPairs1
        assertTrue(fontMetrics.getKernPairs1().isEmpty());
        // composite data
        assertTrue(fontMetrics.getComposites().isEmpty());
    }

    private void checkHelveticaCharMetrics(List<CharMetric> charMetrics)
    {
        assertEquals(315, charMetrics.size());
        // check "space" metrics
        Optional<CharMetric> space = charMetrics.stream()//
                .filter(c -> "space".equals(c.getName())).findFirst();
        assertTrue(space.isPresent());
        CharMetric spaceCharMetric = space.get();
        assertEquals(278f, spaceCharMetric.getWx(), 0f);
        assertEquals(32, spaceCharMetric.getCharacterCode());
        checkBBox(spaceCharMetric.getBoundingBox(), 0, 0, 0, 0);
        assertTrue(spaceCharMetric.getLigatures().isEmpty());
        assertNull(spaceCharMetric.getW());
        assertNull(spaceCharMetric.getW0());
        assertNull(spaceCharMetric.getW1());
        assertNull(spaceCharMetric.getVv());
        // check "ring" metrics
        Optional<CharMetric> ring = charMetrics.stream()//
                .filter(c -> "ring".equals(c.getName())).findFirst();
        assertTrue(ring.isPresent());
        CharMetric ringCharMetric = ring.get();
        assertEquals(333f, ringCharMetric.getWx(), 0f);
        assertEquals(202, ringCharMetric.getCharacterCode());
        checkBBox(ringCharMetric.getBoundingBox(), 75, 572, 259, 756);
        assertTrue(ringCharMetric.getLigatures().isEmpty());
        assertNull(ringCharMetric.getW());
        assertNull(ringCharMetric.getW0());
        assertNull(ringCharMetric.getW1());
        assertNull(ringCharMetric.getVv());
    }

    private void checkHelveticaFontMetrics(FontMetrics fontMetrics)
    {
        assertEquals(4.1f, fontMetrics.getAFMVersion(), 0f);
        assertEquals("Helvetica", fontMetrics.getFontName());
        assertEquals("Helvetica", fontMetrics.getFullName());
        assertEquals("Helvetica", fontMetrics.getFamilyName());
        assertEquals("Medium", fontMetrics.getWeight());
        checkBBox(fontMetrics.getFontBBox(), -166f, -225f, 1000f, 931f);
        assertEquals("002.000", fontMetrics.getFontVersion());
        assertEquals(
                "Copyright (c) 1985, 1987, 1989, 1990, 1997 Adobe Systems Incorporated.  All Rights Reserved.Helvetica is a trademark of Linotype-Hell AG and/or its subsidiaries.",
                fontMetrics.getNotice());
        assertEquals("AdobeStandardEncoding", fontMetrics.getEncodingScheme());
        assertEquals(0, fontMetrics.getMappingScheme());
        assertEquals(0, fontMetrics.getEscChar());
        assertEquals("ExtendedRoman", fontMetrics.getCharacterSet());
        assertEquals(0, fontMetrics.getCharacters());
        assertTrue(fontMetrics.getIsBaseFont());
        assertNull(fontMetrics.getVVector());
        assertFalse(fontMetrics.getIsFixedV());
        assertEquals(718f, fontMetrics.getCapHeight(), 0f);
        assertEquals(523f, fontMetrics.getXHeight(), 0f);
        assertEquals(718f, fontMetrics.getAscender(), 0f);
        assertEquals(-207f, fontMetrics.getDescender(), 0f);
        assertEquals(76f, fontMetrics.getStandardHorizontalWidth(), 0f);
        assertEquals(88f, fontMetrics.getStandardVerticalWidth(), 0f);
        List<String> comments = fontMetrics.getComments();
        assertEquals(4, comments.size());
        assertEquals(
                "Copyright (c) 1985, 1987, 1989, 1990, 1997 Adobe Systems Incorporated.  All Rights Reserved.",
                comments.get(0));
        assertEquals("UniqueID 43054", comments.get(2));
        assertEquals(-100f, fontMetrics.getUnderlinePosition(), 0f);
        assertEquals(50f, fontMetrics.getUnderlineThickness(), 0f);
        assertEquals(0f, fontMetrics.getItalicAngle(), 0f);
        assertNull(fontMetrics.getCharWidth());
        assertFalse(fontMetrics.getIsFixedPitch());
    }

    private void checkBBox(BoundingBox bBox, float lowerX, float lowerY, float upperX, float upperY)
    {
        assertNotNull(bBox);
        assertEquals(lowerX, bBox.getLowerLeftX(), 0f);
        assertEquals(lowerY, bBox.getLowerLeftY(), 0f);
        assertEquals(upperX, bBox.getUpperRightX(), 0f);
        assertEquals(upperY, bBox.getUpperRightY(), 0f);
    }

    private void checkKernPair(List<KernPair> kernPairs, String firstKernChar,
            String secondKernChar, float x, float y)
    {
        Optional<KernPair> kernPair = kernPairs.stream() //
                .filter(k -> firstKernChar.equals(k.getFirstKernCharacter())) //
                .filter(k -> secondKernChar.equals(k.getSecondKernCharacter())) //
                .findFirst();
        assertTrue(kernPair.isPresent());
        assertEquals(x, kernPair.get().getX(), 0f);
        assertEquals(y, kernPair.get().getY(), 0f);

    }
}
