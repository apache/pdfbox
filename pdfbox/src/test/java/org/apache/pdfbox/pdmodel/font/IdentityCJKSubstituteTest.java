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

import java.awt.geom.GeneralPath;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Non-embedded Adobe-GB1 CID fonts substituted by an Adobe-Identity-0 CID-keyed font (Noto CJK,
 * Source Han). The PDF's CIDs are not glyph indices in such a substitute, so glyphs must be
 * resolved via Unicode: PDFBOX-6249 for CIDFontType0, PDFBOX-6293 for CIDFontType2.
 * <p>
 * <b>This test should not run in parallel, which is why it is "isolated" in later PDFBox versions!</b> 
 */
public class IdentityCJKSubstituteTest
{
    private IdentityCJKFixture fixture;

    @Before
    public void installSubstitute() throws IOException
    {
        fixture = new IdentityCJKFixture();
    }

    @After
    public void restore() throws IOException
    {
        fixture.close();
    }

    @Test
    public void testCIDFontType2ResolvesViaUnicode() throws IOException
    {
        PDType0Font font = new PDType0Font(type0("SimSun", COSName.CID_FONT_TYPE2, "UniGB-UCS2-H"));
        PDCIDFontType2 cidFont = (PDCIDFontType2) font.getDescendantFont();

        assertEquals(ZHONG_GID, cidFont.codeToGID(ZHONG));
        assertSameGlyph(cidFont.getPath(ZHONG));
    }

    @Test
    public void testCIDFontType0ResolvesViaUnicode() throws IOException
    {
        PDType0Font font = new PDType0Font(
                type0("AdobeSongStd-Light", COSName.CID_FONT_TYPE0, "UniGB-UCS2-H"));
        PDCIDFontType0 cidFont = (PDCIDFontType0) font.getDescendantFont();

        assertEquals(ZHONG_GID, cidFont.codeToGID(ZHONG));
        assertSameGlyph(cidFont.getPath(ZHONG));
    }

    private void assertSameGlyph(GeneralPath path) throws IOException
    {
        // with the CID used as a GID, the fixture yields an empty glyph
        assertFalse(path.getPathIterator(null).isDone());
        GeneralPath expected = fixture.getSubstitute().getCFF().getFont()
                .getType2CharString(ZHONG_GID).getPath();
        assertEquals(expected.getBounds2D(), path.getBounds2D());
        assertTrue(path.getBounds2D().getWidth() > 0);
    }
}
