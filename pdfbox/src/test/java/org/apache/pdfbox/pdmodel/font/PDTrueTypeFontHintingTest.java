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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Verifies the render-path hinting wiring at the font level: an embedded TrueType font returns a
 * grid-fitted normalized path that differs from the unhinted one, is in the same 1000/em space, and
 * respects the gasp gate.
 */
@Isolated // TrueTypeFont hinting is a global switch; other classes must not render while it is on
class PDTrueTypeFontHintingTest
{
    // hinting is off by default, so these tests have to turn the feature on first
    @BeforeEach
    void enableHinting()
    {
        TrueTypeFont.setHintingEnabled(true);
    }

    @AfterEach
    void restoreHinting()
    {
        TrueTypeFont.setHintingEnabled(false);
    }

    private static final File FONT =
            new File("src/test/resources/org/apache/pdfbox/ttf/LiberationSans-Regular.ttf");

    @Test
    void testHintedNormalizedPathDiffersFromUnhinted() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDTrueTypeFont font = PDTrueTypeFont.load(doc, FONT, WinAnsiEncoding.INSTANCE);
            int code = 'H';

            GeneralPath hinted = font.getHintedNormalizedPath(code, 16);
            assertNotNull(hinted, "expected a hinted path for 'H' at 16ppem");
            GeneralPath unhinted = font.getNormalizedPath(code);

            // hinting must change the outline
            assertFalse(Arrays.equals(flatten(hinted), flatten(unhinted)),
                    "hinted path should differ from the unhinted path");

            // and it must still be in the 1000-unit em square (an 'H' cap height is several hundred)
            Rectangle2D bounds = hinted.getBounds2D();
            assertTrue(bounds.getMaxY() > 300 && bounds.getMaxY() < 1000,
                    "hinted path should be normalized to 1000/em, was maxY=" + bounds.getMaxY());
        }
    }

    @Test
    void testGaspGateReturnsNullAtSmallPpem() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDTrueTypeFont font = PDTrueTypeFont.load(doc, FONT, WinAnsiEncoding.INSTANCE);
            // LiberationSans gasp disables grid-fitting at <= 8 ppem
            assertNull(font.getHintedNormalizedPath('H', 8));
            assertNotNull(font.getHintedNormalizedPath('H', 16));
        }
    }

    /** Only an embedded outline carries bytecode we can execute; a substituted font must not hint. */
    @Test
    void testNonEmbeddedFontDoesNotHint() throws IOException
    {
        COSDictionary dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.FONT);
        dict.setItem(COSName.SUBTYPE, COSName.TRUE_TYPE);
        dict.setName(COSName.BASE_FONT, "Helvetica");

        PDTrueTypeFont font = new PDTrueTypeFont(dict, null);
        assertFalse(font.isEmbedded(), "font should not be embedded");
        // the substituted font still draws, so a null hinted path is the embedded check talking
        // rather than a font that cannot produce an outline at all
        assertFalse(font.getNormalizedPath('H').getPathIterator(null).isDone(),
                "expected the substitute font to produce an outline");
        assertNull(font.getHintedNormalizedPath('H', 16));
    }

    private static double[] flatten(GeneralPath path)
    {
        double[] coords = new double[6];
        java.util.List<Double> out = new java.util.ArrayList<>();
        for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next())
        {
            out.add((double) it.currentSegment(coords));
            for (double c : coords)
            {
                out.add(c);
            }
        }
        double[] array = new double[out.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = out.get(i);
        }
        return array;
    }
}
