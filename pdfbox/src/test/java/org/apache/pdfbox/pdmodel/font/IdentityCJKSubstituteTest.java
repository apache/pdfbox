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

import static org.apache.pdfbox.pdmodel.font.IdentityCJKFixture.ZHONG;
import static org.apache.pdfbox.pdmodel.font.IdentityCJKFixture.ZHONG_GID;
import static org.apache.pdfbox.pdmodel.font.IdentityCJKFixture.type0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.GeneralPath;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Non-embedded Adobe-GB1 CID fonts substituted by an Adobe-Identity-0 CID-keyed font (Noto CJK,
 * Source Han). The PDF's CIDs are not glyph indices in such a substitute, so glyphs must be
 * resolved via Unicode: PDFBOX-6249 for CIDFontType0, PDFBOX-6293 for CIDFontType2.
 */
@Isolated
class IdentityCJKSubstituteTest
{
    private IdentityCJKFixture fixture;

    @BeforeEach
    void installSubstitute() throws IOException
    {
        fixture = new IdentityCJKFixture();
    }

    @AfterEach
    void restore() throws IOException
    {
        fixture.close();
    }

    @Test
    void testCIDFontType2ResolvesViaUnicode() throws IOException
    {
        PDType0Font font = new PDType0Font(type0("SimSun", COSName.CID_FONT_TYPE2, "UniGB-UCS2-H"),
                null);
        PDCIDFontType2 cidFont = (PDCIDFontType2) font.getDescendantFont();

        assertEquals(ZHONG_GID, cidFont.codeToGID(ZHONG, font),
                "GID must be resolved via Unicode, not used as a CID");
        assertSameGlyph(cidFont.getPath(ZHONG, font));
    }

    @Test
    void testCIDFontType0ResolvesViaUnicode() throws IOException
    {
        PDType0Font font = new PDType0Font(
                type0("AdobeSongStd-Light", COSName.CID_FONT_TYPE0, "UniGB-UCS2-H"), null);
        PDCIDFontType0 cidFont = (PDCIDFontType0) font.getDescendantFont();

        assertEquals(ZHONG_GID, cidFont.codeToGID(ZHONG, font),
                "GID must be resolved via Unicode, not used as a CID");
        assertSameGlyph(cidFont.getPath(ZHONG, font));
    }

    private void assertSameGlyph(GeneralPath path) throws IOException
    {
        // with the CID used as a GID, the fixture yields an empty glyph
        assertFalse(path.getPathIterator(null).isDone(), "glyph for U+4E2D has an empty path");
        GeneralPath expected = fixture.getSubstitute().getCFF().getFont()
                .getType2CharString(ZHONG_GID).getPath();
        assertEquals(expected.getBounds2D(), path.getBounds2D());
        assertTrue(path.getBounds2D().getWidth() > 0);
    }
}
