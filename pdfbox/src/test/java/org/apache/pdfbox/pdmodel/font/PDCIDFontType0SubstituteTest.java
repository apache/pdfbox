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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;

import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

/**
 * A non-embedded Adobe-CNS1 CIDFontType0 must render real glyphs when a CID-keyed substitute is
 * available, even when the substitute uses a different character collection (modern CJK fonts
 * such as Noto CJK are Adobe-Identity-0). PDFBOX-6249.
 */
class PDCIDFontType0SubstituteTest
{
    @Test
    void testLatinViaCns1NonEmbedded() throws IOException
    {
        File file = new File("src/test/resources/org/apache/pdfbox/pdmodel/font",
                "PDFBOX-6249-cns1-nonembedded-latin.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDPage page = doc.getPage(0);
            PDType0Font font = (PDType0Font) page.getResources().getFont(COSName.getPDFName("F1"));
            PDCIDFontType0 cidFont = (PDCIDFontType0) font.getDescendantFont();

            CIDFontMapping mapping = FontMappers.instance().getCIDFont(cidFont.getBaseFont(),
                    cidFont.getFontDescriptor(), cidFont.getCIDSystemInfo());
            assumeTrue(mapping.isCIDFont(),
                    "no CID-keyed substitute for Adobe-CNS1 installed, can't test");

            // 0x48 is "H": the CMap maps it to CID 41, which is not a valid glyph index in an
            // Adobe-Identity-0 substitute; it must be resolved via Unicode
            assertTrue(cidFont.hasGlyph(0x48, font), "glyph for 'H' not found in substitute");
            GeneralPath path = cidFont.getPath(0x48, font);
            assertFalse(path.getPathIterator(null).isDone(), "glyph for 'H' has an empty path");

            CFFFont cff = mapping.getFont().getCFF().getFont();
            if (cff instanceof CFFCIDFont && "Identity".equals(((CFFCIDFont) cff).getOrdering()))
            {
                // ASCII GIDs happen to line up with Adobe CIDs in Noto/Source Han, so assert on
                // an ideograph, where using the CID as a GID yields the wrong glyph
                int expected = mapping.getFont().getUnicodeCmapLookup().getGlyphId(0x4E2D);
                assertTrue(expected > 0);
                assertEquals(expected, cidFont.codeToGID(0x4E2D, font),
                        "GID must be resolved via Unicode, not used as a CID");
            }
        }
    }
}
