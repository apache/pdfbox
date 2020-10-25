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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.fontbox.util.BoundingBox;
import org.junit.Test;

public class FontMetricsTest
{
    @Test
    public void testFontMetricsNames()
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
        assertEquals(1, fontMetrics.getComments().size());
        try
        {
            fontMetrics.getComments().add("comment");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    public void testFontMetricsSimpleValues()
    {
        FontMetrics fontMetrics = new FontMetrics();
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

        assertEquals("weight", fontMetrics.getWeight());
        assertEquals("encodingScheme", fontMetrics.getEncodingScheme());
        assertEquals(0, fontMetrics.getMappingScheme());
        assertEquals(0, fontMetrics.getEscChar());
        assertEquals("characterSet", fontMetrics.getCharacterSet());
        assertEquals(10, fontMetrics.getCharacters());
        assertTrue(fontMetrics.isBaseFont());
        assertTrue(fontMetrics.isFixedV());
        assertEquals(10f, fontMetrics.getCapHeight(), 0f);
        assertEquals(20f, fontMetrics.getXHeight(), 0f);
        assertEquals(30f, fontMetrics.getAscender(), 0f);
        assertEquals(40f, fontMetrics.getDescender(), 0f);
        assertEquals(50f, fontMetrics.getStandardHorizontalWidth(), 0f);
        assertEquals(60f, fontMetrics.getStandardVerticalWidth(), 0f);
        assertEquals(70f, fontMetrics.getUnderlinePosition(), 0f);
        assertEquals(80f, fontMetrics.getUnderlineThickness(), 0f);
        assertEquals(90f, fontMetrics.getItalicAngle(), 0f);
        assertTrue(fontMetrics.isFixedPitch());
    }

    @Test
    public void testFontMetricsComplexValues()
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
    public void testCharMetrics()
    {
        FontMetrics fontMetrics = new FontMetrics();
        assertEquals(0, fontMetrics.getCharMetrics().size());
        fontMetrics.addCharMetric(new CharMetric());
        assertEquals(1, fontMetrics.getCharMetrics().size());
        try
        {
            fontMetrics.getCharMetrics().add(new CharMetric());
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    public void testComposites()
    {
        FontMetrics fontMetrics = new FontMetrics();
        assertEquals(0, fontMetrics.getComposites().size());
        fontMetrics.addComposite(new Composite("name"));
        assertEquals(1, fontMetrics.getComposites().size());
        try
        {
            fontMetrics.getComposites().add(new Composite("name"));
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }

    @Test
    public void testKernData()
    {
        FontMetrics fontMetrics = new FontMetrics();
        // KernPairs
        assertEquals(0, fontMetrics.getKernPairs().size());
        fontMetrics.addKernPair(new KernPair("first", "second", 10, 20));
        assertEquals(1, fontMetrics.getKernPairs().size());
        try
        {
            fontMetrics.getKernPairs().add(new KernPair("first", "second", 10, 20));
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // KernPairs0
        assertEquals(0, fontMetrics.getKernPairs0().size());
        fontMetrics.addKernPair0(new KernPair("first", "second", 10, 20));
        assertEquals(1, fontMetrics.getKernPairs0().size());
        try
        {
            fontMetrics.getKernPairs0().add(new KernPair("first", "second", 10, 20));
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // KernPairs1
        assertEquals(0, fontMetrics.getKernPairs1().size());
        fontMetrics.addKernPair1(new KernPair("first", "second", 10, 20));
        assertEquals(1, fontMetrics.getKernPairs1().size());
        try
        {
            fontMetrics.getKernPairs1().add(new KernPair("first", "second", 10, 20));
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
        // TrackKern
        assertEquals(0, fontMetrics.getTrackKern().size());
        fontMetrics.addTrackKern(new TrackKern(0, 1, 1, 10, 10));
        assertEquals(1, fontMetrics.getTrackKern().size());
        try
        {
            fontMetrics.getTrackKern().add(new TrackKern(0, 1, 1, 10, 10));
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }
}
