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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

/**
 * The CID half of the render-path hinting wiring. {@link PDCIDFontType2#getHintedNormalizedPath} is a
 * near-copy of the {@link PDTrueTypeFont} one but had no test of its own, and {@link PDType0Font}
 * forwards to it only when the descendant really is a CIDFontType2.
 */
class PDType0FontHintingTest
{
    /**
     * Embeds the font whole (no subsetting) so the encoding is Identity and a character code is its
     * own glyph id, which keeps the test about hinting rather than about CID mapping.
     */
    private static PDType0Font load(PDDocument doc, int[] gidOut) throws IOException, URISyntaxException
    {
        URL url = TrueTypeFont.class.getResource(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
        File fontFile = new File(url.toURI());

        TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(fontFile));
        gidOut[0] = ttf.getUnicodeCmapLookup().getGlyphId('H');
        assertTrue(gidOut[0] > 0, "no glyph for 'H'");
        return PDType0Font.load(doc, ttf, false);
    }

    @Test
    void testHintedNormalizedPathDiffersFromUnhinted() throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            int[] gid = new int[1];
            PDType0Font font = load(doc, gid);

            GeneralPath hinted = font.getHintedNormalizedPath(gid[0], 16);
            assertNotNull(hinted, "expected a hinted path for 'H' at 16ppem");
            GeneralPath unhinted = font.getNormalizedPath(gid[0]);

            assertFalse(Arrays.equals(flatten(hinted), flatten(unhinted)),
                    "hinted path should differ from the unhinted path");

            // and it must still be in the 1000-unit em square (an 'H' cap height is several hundred)
            Rectangle2D bounds = hinted.getBounds2D();
            assertTrue(bounds.getMaxY() > 300 && bounds.getMaxY() < 1000,
                    "hinted path should be normalized to 1000/em, was maxY=" + bounds.getMaxY());
        }
    }

    @Test
    void testGaspGateReturnsNullAtSmallPpem() throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            int[] gid = new int[1];
            PDType0Font font = load(doc, gid);
            // LiberationSans gasp disables grid-fitting at <= 8 ppem
            assertNull(font.getHintedNormalizedPath(gid[0], 8));
            assertNotNull(font.getHintedNormalizedPath(gid[0], 16));
        }
    }

    /** Only an embedded outline carries bytecode we can execute; a substituted font must not hint. */
    @Test
    void testNonEmbeddedFontDoesNotHint() throws IOException
    {
        PDType0Font font = nonEmbedded();
        assertFalse(font.getDescendantFont().isEmbedded(), "font should not be embedded");
        // the substituted font still draws, so a null hinted path is the embedded check talking
        // rather than a font that cannot produce an outline at all
        int gid = font.getDescendantFont().codeToGID('H', font);
        assertFalse(font.getNormalizedPath(gid).getPathIterator(null).isDone(),
                "expected the substitute font to produce an outline");
        assertNull(font.getHintedNormalizedPath(gid, 16));
    }

    /** Builds the Type0/CIDFontType2 dictionary pair a PDF uses when it does not embed the font. */
    private static PDType0Font nonEmbedded() throws IOException
    {
        COSDictionary cid = new COSDictionary();
        cid.setItem(COSName.TYPE, COSName.FONT);
        cid.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE2);
        cid.setName(COSName.BASE_FONT, "Helvetica");

        COSArray descendants = new COSArray();
        descendants.add(cid);

        COSDictionary type0 = new COSDictionary();
        type0.setItem(COSName.TYPE, COSName.FONT);
        type0.setItem(COSName.SUBTYPE, COSName.TYPE0);
        type0.setName(COSName.BASE_FONT, "Helvetica");
        type0.setItem(COSName.ENCODING, COSName.IDENTITY_H);
        type0.setItem(COSName.DESCENDANT_FONTS, descendants);
        return new PDType0Font(type0, null);
    }

    private static double[] flatten(GeneralPath path)
    {
        double[] coords = new double[6];
        List<Double> out = new ArrayList<>();
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
