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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import org.junit.jupiter.api.Test;

/**
 * A non-embedded CIDFontType2 whose declared /Encoding is a predefined "Uni...-UCS2/UTF16" CMap
 * must write a code that is valid under that encoding - the raw Unicode value - when generating
 * new appearance content, not the substituted font's own glyph index. Before the fix, the
 * substitute font's glyph index was written regardless of the declared encoding; since that
 * index is unrelated to the Unicode-based codespace of a "Uni...-UTF16-H" CMap, a conforming
 * reader (including PDFBox itself) decoded it back into a wrong, essentially arbitrary
 * character. PDFBOX-5953.
 *
 * <p>The fixture is the AcroForm/DR "SimSun" font (Type0/CIDFontType2, /Encoding
 * UniGB-UTF16-H, not embedded) of a real-world Chinese bank receipt form attached to the JIRA
 * issue, whose filled-in field values (account numbers, amounts, company names) render as
 * garbled or blank text because of this bug.
 */
class PDCIDFontType2SubstituteTest
{
    @Test
    void testEncodeNonEmbeddedPredefinedUnicodeEncoding() throws IOException
    {
        File file = new File("target/pdfs", "PDFBOX-5953-test.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDType0Font font = (PDType0Font) acroForm.getDefaultResources()
                    .getFont(COSName.getPDFName("SimSun"));
            PDCIDFontType2 cidFont = (PDCIDFontType2) font.getDescendantFont();
            assumeTrue(!cidFont.isEmbedded(),
                    "font is embedded in this fixture, can't test substitution");
            assumeTrue(font.getCMap().getName().startsWith("Uni"),
                    "fixture font's encoding changed, can't test");

            CmapLookup substituteCmap = cidFont.getTrueTypeFont().getUnicodeCmapLookup(false);
            // '百' (as in 百色, the city in the receipts): a common GB1 Hanzi, guaranteed to be
            // resolvable through the Adobe-GB1 <-> Unicode predefined CMaps
            int unicode = 0x767E;
            int expectedGid = substituteCmap.getGlyphId(unicode);
            assumeTrue(expectedGid != 0,
                    "no CJK glyph for U+767E in the substituted font, can't test");

            byte[] bytes = cidFont.encode(unicode, font);
            assertEquals(2, bytes.length);
            int code = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);

            // before the fix, 'code' was the substitute font's own glyph index; decoding it
            // again (as any reader must, since the font's /Encoding is UniGB-UTF16-H, not
            // Identity-H) produced a wrong, essentially arbitrary glyph instead of round
            // tripping back to the glyph for U+767E
            assertEquals(expectedGid, cidFont.codeToGID(code, font),
                    "encoded code must decode back to the substitute font's glyph for U+767E");
        }
    }
}
