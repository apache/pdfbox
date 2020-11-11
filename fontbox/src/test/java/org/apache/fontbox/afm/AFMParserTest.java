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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.fontbox.util.BoundingBox;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class AFMParserTest
{
    @Test
    public void testStartFontMetrics() throws IOException
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
    public void testEndFontMetrics() throws IOException
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
    public void testMalformedFloat() throws IOException
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
    public void testMalformedInteger() throws IOException
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
    public void testAFMParser() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse();

        assertEquals(4.1f, fontMetrics.getAFMVersion(), 0f);
        assertEquals("Helvetica", fontMetrics.getFontName());
        assertEquals("Helvetica", fontMetrics.getFullName());
        assertEquals("Helvetica", fontMetrics.getFamilyName());
        assertEquals("Medium", fontMetrics.getWeight());
        BoundingBox fontBBox = fontMetrics.getFontBBox();
        assertNotNull(fontBBox);
        assertEquals(-166f, fontBBox.getLowerLeftX(),0f);
        assertEquals(-225f, fontBBox.getLowerLeftY(), 0f);
        assertEquals(1000f, fontBBox.getUpperRightX(), 0f);
        assertEquals(931f, fontBBox.getUpperRightY(), 0f);
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
        // char metrics
        List<CharMetric> charMetrics = fontMetrics.getCharMetrics();
        assertEquals(315, charMetrics.size());
        // check "space" metrics
        Optional<CharMetric> space = charMetrics.stream()//
                .filter(c -> "space".equals(c.getName())).findFirst();
        assertTrue(space.isPresent());
        CharMetric spaceCharMetric = space.get();
        assertEquals(278f, spaceCharMetric.getWx(), 0f);
        assertEquals(32, spaceCharMetric.getCharacterCode());
        BoundingBox spaceBBox = spaceCharMetric.getBoundingBox();
        assertNotNull(spaceBBox);
        assertEquals(0, spaceBBox.getLowerLeftX(), 0f);
        assertEquals(0, spaceBBox.getLowerLeftY(), 0f);
        assertEquals(0, spaceBBox.getUpperRightX(), 0f);
        assertEquals(0, spaceBBox.getUpperRightY(), 0f);
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
        BoundingBox ringBBox = ringCharMetric.getBoundingBox();
        assertNotNull(ringBBox);
        assertEquals(75, ringBBox.getLowerLeftX(), 0f);
        assertEquals(572, ringBBox.getLowerLeftY(), 0f);
        assertEquals(259, ringBBox.getUpperRightX(), 0f);
        assertEquals(756, ringBBox.getUpperRightY(), 0f);
        assertTrue(ringCharMetric.getLigatures().isEmpty());
        assertNull(ringCharMetric.getW());
        assertNull(ringCharMetric.getW0());
        assertNull(ringCharMetric.getW1());
        assertNull(ringCharMetric.getVv());
        // KernPairs
        List<KernPair> kernPairs = fontMetrics.getKernPairs();
        assertEquals(2705, kernPairs.size());
        // check "KPX A Ucircumflex -50"
        Optional<KernPair> A_Ucircumflex = kernPairs.stream() //
                .filter(k -> "A".equals(k.getFirstKernCharacter())) //
                .filter(k -> "Ucircumflex".equals(k.getSecondKernCharacter())) //
                .findFirst();
        assertTrue(A_Ucircumflex.isPresent());
        assertEquals(-50f, A_Ucircumflex.get().getX(), 0f);
        assertEquals(0f, A_Ucircumflex.get().getY(), 0f);
        // check "KPX W agrave -40"
        Optional<KernPair> W_agrave = kernPairs.stream() //
                .filter(k -> "W".equals(k.getFirstKernCharacter())) //
                .filter(k -> "agrave".equals(k.getSecondKernCharacter())) //
                .findFirst();
        assertTrue(W_agrave.isPresent());
        assertEquals(-40f, W_agrave.get().getX(), 0f);
        assertEquals(0f, W_agrave.get().getY(), 0f);
        // KernPairs0
        List<KernPair> kernPairs0 = fontMetrics.getKernPairs0();
        assertTrue(kernPairs0.isEmpty());
        // KernPairs1
        List<KernPair> kernPairs1 = fontMetrics.getKernPairs1();
        assertTrue(kernPairs1.isEmpty());
        // composite data
        List<Composite> composites = fontMetrics.getComposites();
        assertTrue(composites.isEmpty());
    }

    @Test
    public void testAFMParserReducedDataset() throws IOException
    {
        AFMParser parser = new AFMParser(
                new FileInputStream("src/test/resources/afm/Helvetica.afm"));
        FontMetrics fontMetrics = parser.parse(true);

        assertEquals(4.1f, fontMetrics.getAFMVersion(), 0f);
        assertEquals("Helvetica", fontMetrics.getFontName());
        assertEquals("Helvetica", fontMetrics.getFullName());
        assertEquals("Helvetica", fontMetrics.getFamilyName());
        assertEquals("Medium", fontMetrics.getWeight());
        BoundingBox fontBBox = fontMetrics.getFontBBox();
        assertNotNull(fontBBox);
        assertEquals(-166f, fontBBox.getLowerLeftX(), 0f);
        assertEquals(-225f, fontBBox.getLowerLeftY(), 0f);
        assertEquals(1000f, fontBBox.getUpperRightX(), 0f);
        assertEquals(931f, fontBBox.getUpperRightY(), 0f);
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
        // char metrics
        List<CharMetric> charMetrics = fontMetrics.getCharMetrics();
        assertEquals(315, charMetrics.size());
        // check "space" metrics
        Optional<CharMetric> space = charMetrics.stream()//
                .filter(c -> "space".equals(c.getName())).findFirst();
        assertTrue(space.isPresent());
        CharMetric spaceCharMetric = space.get();
        assertEquals(278f, spaceCharMetric.getWx(), 0f);
        assertEquals(32, spaceCharMetric.getCharacterCode());
        BoundingBox spaceBBox = spaceCharMetric.getBoundingBox();
        assertNotNull(spaceBBox);
        assertEquals(0, spaceBBox.getLowerLeftX(), 0f);
        assertEquals(0, spaceBBox.getLowerLeftY(), 0f);
        assertEquals(0, spaceBBox.getUpperRightX(), 0f);
        assertEquals(0, spaceBBox.getUpperRightY(), 0f);
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
        BoundingBox ringBBox = ringCharMetric.getBoundingBox();
        assertNotNull(ringBBox);
        assertEquals(75, ringBBox.getLowerLeftX(), 0f);
        assertEquals(572, ringBBox.getLowerLeftY(), 0f);
        assertEquals(259, ringBBox.getUpperRightX(), 0f);
        assertEquals(756, ringBBox.getUpperRightY(), 0f);
        assertTrue(ringCharMetric.getLigatures().isEmpty());
        assertNull(ringCharMetric.getW());
        assertNull(ringCharMetric.getW0());
        assertNull(ringCharMetric.getW1());
        assertNull(ringCharMetric.getVv());
        // KernPairs, empty due to reducedDataset == true
        List<KernPair> kernPairs = fontMetrics.getKernPairs();
        assertTrue(kernPairs.isEmpty());
        // KernPairs0
        List<KernPair> kernPairs0 = fontMetrics.getKernPairs0();
        assertTrue(kernPairs0.isEmpty());
        // KernPairs1
        List<KernPair> kernPairs1 = fontMetrics.getKernPairs1();
        assertTrue(kernPairs1.isEmpty());
        // composite data
        List<Composite> composites = fontMetrics.getComposites();
        assertTrue(composites.isEmpty());
    }
}
