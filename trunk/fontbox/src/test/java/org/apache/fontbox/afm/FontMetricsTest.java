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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.fontbox.util.BoundingBox;
import org.junit.jupiter.api.Test;

class FontMetricsTest
{
    @Test
    void testFontMetricsNames()
    {
        FontMetrics fontMetrics = new FontMetrics();
        fontMetrics.setFontName("fontName");
        fontMetrics.setFamilyName("familyName");
        fontMetrics.setFullName("fullName");
        fontMetrics.setFontVersion("fontVersion");
        fontMetrics.setNotice("notice");
        assertEquals("fontName", fontMetrics.getFontName());
        assertEquals("familyName", fontMetrics.getFamilyName());
        assertEquals("fullName", fontMetrics.getFullName());
        assertEquals("fontVersion", fontMetrics.getFontVersion());
        assertEquals("notice", fontMetrics.getNotice());

        assertEquals(0, fontMetrics.getComments().size());
        fontMetrics.addComment("comment");
        List<String> comments = fontMetrics.getComments();
        assertEquals(1, comments.size());
        try
        {
            comments.add("comment");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    void testFontMetricsSimpleValues()
    {
        FontMetrics fontMetrics = new FontMetrics();
        fontMetrics.setAFMVersion(4.3f);
        fontMetrics.setWeight("weight");
        fontMetrics.setEncodingScheme("encodingScheme");
        fontMetrics.setMappingScheme(0);
        fontMetrics.setEscChar(0);
        fontMetrics.setCharacterSet("characterSet");
        fontMetrics.setCharacters(10);
        fontMetrics.setIsBaseFont(true);
        fontMetrics.setIsFixedV(true);
        fontMetrics.setCapHeight(10f);
        fontMetrics.setXHeight(20f);
        fontMetrics.setAscender(30f);
        fontMetrics.setDescender(40f);
        fontMetrics.setStandardHorizontalWidth(50f);
        fontMetrics.setStandardVerticalWidth(60f);
        fontMetrics.setUnderlinePosition(70f);
        fontMetrics.setUnderlineThickness(80f);
        fontMetrics.setItalicAngle(90f);
        fontMetrics.setFixedPitch(true);

        assertEquals(4.3f, fontMetrics.getAFMVersion(), 0f);
        assertEquals("weight", fontMetrics.getWeight());
        assertEquals("encodingScheme", fontMetrics.getEncodingScheme());
        assertEquals(0, fontMetrics.getMappingScheme());
        assertEquals(0, fontMetrics.getEscChar());
        assertEquals("characterSet", fontMetrics.getCharacterSet());
        assertEquals(10, fontMetrics.getCharacters());
        assertTrue(fontMetrics.getIsBaseFont());
        assertTrue(fontMetrics.getIsFixedV());
        assertEquals(10f, fontMetrics.getCapHeight(), 0f);
        assertEquals(20f, fontMetrics.getXHeight(), 0f);
        assertEquals(30f, fontMetrics.getAscender(), 0f);
        assertEquals(40f, fontMetrics.getDescender(), 0f);
        assertEquals(50f, fontMetrics.getStandardHorizontalWidth(), 0f);
        assertEquals(60f, fontMetrics.getStandardVerticalWidth(), 0f);
        assertEquals(70f, fontMetrics.getUnderlinePosition(), 0f);
        assertEquals(80f, fontMetrics.getUnderlineThickness(), 0f);
        assertEquals(90f, fontMetrics.getItalicAngle(), 0f);
        assertTrue(fontMetrics.getIsFixedPitch());
    }

    @Test
    void testFontMetricsComplexValues()
    {
        FontMetrics fontMetrics = new FontMetrics();
        fontMetrics.setFontBBox(new BoundingBox(10, 20, 30, 40));
        fontMetrics.setVVector(new float[] { 10, 20 });
        fontMetrics.setCharWidth(new float[] { 30, 40 });
        assertEquals(10, fontMetrics.getFontBBox().getLowerLeftX(), 0);
        assertEquals(20, fontMetrics.getFontBBox().getLowerLeftY(), 0);
        assertEquals(30, fontMetrics.getFontBBox().getUpperRightX(), 0);
        assertEquals(40, fontMetrics.getFontBBox().getUpperRightY(), 0);
        assertEquals(10, fontMetrics.getVVector()[0], 0);
        assertEquals(20, fontMetrics.getVVector()[1], 0);
        assertEquals(30, fontMetrics.getCharWidth()[0], 0);
        assertEquals(40, fontMetrics.getCharWidth()[1], 0);
    }

    @Test
    void testMetricSets()
    {
        FontMetrics fontMetrics = new FontMetrics();
        fontMetrics.setMetricSets(1);
        assertEquals(1, fontMetrics.getMetricSets());
        try
        {
            // any value < 0 should thrown an IllegalArgumentException
            fontMetrics.setMetricSets(-1);
            fail("An IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException excpetion)
        {
            // do nothing
        }

        try
        {
            // any value > 2 should thrown an IllegalArgumentException
            fontMetrics.setMetricSets(3);
            fail("An IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException excpetion)
        {
            // do nothing
        }
    }

    @Test
    void testCharMetrics()
    {
        FontMetrics fontMetrics = new FontMetrics();
        assertEquals(0, fontMetrics.getCharMetrics().size());
        CharMetric charMetric = new CharMetric();
        fontMetrics.addCharMetric(charMetric);
        List<CharMetric> charMetrics = fontMetrics.getCharMetrics();
        assertEquals(1, charMetrics.size());
        try
        {
            charMetrics.add(charMetric);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    void testComposites()
    {
        FontMetrics fontMetrics = new FontMetrics();
        assertEquals(0, fontMetrics.getComposites().size());
        Composite composite = new Composite("name");
        fontMetrics.addComposite(composite);
        List<Composite> composites = fontMetrics.getComposites();
        assertEquals(1, composites.size());
        try
        {
            composites.add(composite);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    void testKernData()
    {
        FontMetrics fontMetrics = new FontMetrics();
        // KernPairs
        assertEquals(0, fontMetrics.getKernPairs().size());
        KernPair kernPair = new KernPair("first", "second", 10, 20);
        fontMetrics.addKernPair(kernPair);
        List<KernPair> kernPairs = fontMetrics.getKernPairs();
        assertEquals(1, kernPairs.size());
        try
        {
            kernPairs.add(kernPair);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // KernPairs0
        assertEquals(0, fontMetrics.getKernPairs0().size());
        fontMetrics.addKernPair0(kernPair);
        List<KernPair> kernPairs0 = fontMetrics.getKernPairs0();
        assertEquals(1, kernPairs0.size());
        try
        {
            kernPairs0.add(kernPair);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // KernPairs1
        assertEquals(0, fontMetrics.getKernPairs1().size());
        fontMetrics.addKernPair1(kernPair);
        List<KernPair> kernPairs1 = fontMetrics.getKernPairs1();
        assertEquals(1, kernPairs1.size());
        try
        {
            kernPairs1.add(kernPair);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // TrackKern
        assertEquals(0, fontMetrics.getTrackKern().size());
        TrackKern trackKern = new TrackKern(0, 1, 1, 10, 10);
        fontMetrics.addTrackKern(trackKern);
        List<TrackKern> trackKerns = fontMetrics.getTrackKern();
        assertEquals(1, trackKerns.size());
        try
        {
            trackKerns.add(trackKern);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    void testCharMetricDimensions()
    {
        FontMetrics fontMetrics = new FontMetrics();
        assertEquals(0, fontMetrics.getAverageCharacterWidth(), 0f);

        CharMetric charMetric10 = new CharMetric();
        charMetric10.setName("ten");
        charMetric10.setWx(10f);
        charMetric10.setWy(20f);
        fontMetrics.addCharMetric(charMetric10);
        CharMetric charMetric20 = new CharMetric();
        charMetric20.setName("twenty");
        charMetric20.setWx(20f);
        charMetric20.setWy(40f);
        fontMetrics.addCharMetric(charMetric20);
        CharMetric charMetric30 = new CharMetric();
        charMetric30.setName("thirty");
        charMetric30.setWx(30f);
        charMetric30.setWy(60f);
        fontMetrics.addCharMetric(charMetric30);
        CharMetric charMetric40 = new CharMetric();
        charMetric40.setName("forty");
        charMetric40.setWx(40f);
        charMetric40.setWy(80f);
        fontMetrics.addCharMetric(charMetric40);

        assertEquals(10f, fontMetrics.getCharacterWidth("ten"), 0f);
        assertEquals(30f, fontMetrics.getCharacterWidth("thirty"), 0f);
        assertEquals(0f, fontMetrics.getCharacterWidth("unknown"), 0f);

        assertEquals(40f, fontMetrics.getCharacterHeight("twenty"), 0f);
        assertEquals(80f, fontMetrics.getCharacterHeight("forty"), 0f);
        assertEquals(0f, fontMetrics.getCharacterHeight("unknown"), 0f);

        assertEquals(25, fontMetrics.getAverageCharacterWidth(), 0f);
    }

}
