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

import java.io.File;
import java.io.IOException;

import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/**
 * Installs an Adobe-Identity-0 CID-keyed font as the substitute for every non-embedded CID font,
 * standing in for an installed Noto CJK / Source Han font, and restores the previous mapper on
 * {@link #close()}. {@link FontMappers} is JVM-wide, so test classes using this must be
 * {@code @Isolated}.
 * <p>
 * The substitute is Noto Sans CJK SC (SIL OFL 1.1) cut down to space and U+4E2D with glyph IDs
 * kept, so that CIDs still equal GIDs as in the full font; every other glyph is empty.
 * Regenerate with fontTools:
 * <pre>
 * pyftsubset NotoSansCJK-Regular.ttc --font-number=2 --unicodes=U+0020,U+4E2D --retain-gids \
 *     --layout-features='' --no-hinting --desubroutinize --name-IDs='*' --notdef-outline \
 *     --output-file=NotoSansCJKsc-Regular-subset.otf
 * </pre>
 */
final class IdentityCJKFixture implements AutoCloseable
{
    private static final File FIXTURE = new File(
            "src/test/resources/org/apache/pdfbox/pdmodel/font/NotoSansCJKsc-Regular-subset.otf");

    // U+4E2D: GID/CID 9544 in the substitute, CID 4559 in Adobe-GB1
    static final int ZHONG = 0x4E2D;
    static final int ZHONG_GID = 9544;

    private final FontMapper original;
    private final OpenTypeFont substitute;

    IdentityCJKFixture() throws IOException
    {
        substitute = new OTFParser().parse(new RandomAccessReadBufferedFile(FIXTURE));
        CFFCIDFont cff = (CFFCIDFont) substitute.getCFF().getFont();
        if (!"Identity".equals(cff.getOrdering()))
        {
            throw new IllegalStateException("fixture is not Adobe-Identity-0");
        }
        original = FontMappers.instance();
        FontMappers.set(new FixtureMapper(original, substitute));
    }

    OpenTypeFont getSubstitute()
    {
        return substitute;
    }

    @Override
    public void close() throws IOException
    {
        FontMappers.set(original);
        substitute.close();
    }

    /**
     * A non-embedded Adobe-GB1 Type0 font dictionary.
     */
    static COSDictionary type0(String baseFont, COSName subtype, String encoding)
    {
        COSDictionary ros = new COSDictionary();
        ros.setItem(COSName.REGISTRY, new COSString("Adobe"));
        ros.setItem(COSName.ORDERING, new COSString("GB1"));
        ros.setInt(COSName.SUPPLEMENT, 2);

        COSDictionary fd = new COSDictionary();
        fd.setItem(COSName.TYPE, COSName.FONT_DESC);
        fd.setName(COSName.FONT_NAME, baseFont);
        fd.setInt(COSName.FLAGS, 6);
        COSArray bbox = new COSArray();
        bbox.add(COSInteger.get(0));
        bbox.add(COSInteger.get(-141));
        bbox.add(COSInteger.get(996));
        bbox.add(COSInteger.get(855));
        fd.setItem(COSName.FONT_BBOX, bbox);
        fd.setInt(COSName.ITALIC_ANGLE, 0);
        fd.setInt(COSName.ASCENT, 859);
        fd.setInt(COSName.DESCENT, -141);
        fd.setInt(COSName.CAP_HEIGHT, 859);
        fd.setInt(COSName.STEM_V, 80);

        COSDictionary cid = new COSDictionary();
        cid.setItem(COSName.TYPE, COSName.FONT);
        cid.setItem(COSName.SUBTYPE, subtype);
        cid.setName(COSName.BASE_FONT, baseFont);
        cid.setItem(COSName.CIDSYSTEMINFO, ros);
        cid.setItem(COSName.FONT_DESC, fd);
        cid.setInt(COSName.DW, 1000);

        COSArray descendants = new COSArray();
        descendants.add(cid);
        COSDictionary type0 = new COSDictionary();
        type0.setItem(COSName.TYPE, COSName.FONT);
        type0.setItem(COSName.SUBTYPE, COSName.TYPE0);
        type0.setName(COSName.BASE_FONT, baseFont);
        type0.setName(COSName.ENCODING, encoding);
        type0.setItem(COSName.DESCENDANT_FONTS, descendants);
        return type0;
    }

    static COSDictionary descendant(COSDictionary type0)
    {
        return (COSDictionary) ((COSArray) type0.getDictionaryObject(COSName.DESCENDANT_FONTS))
                .getObject(0);
    }

    private static final class FixtureMapper implements FontMapper
    {
        private final FontMapper delegate;
        private final OpenTypeFont substitute;

        FixtureMapper(FontMapper delegate, OpenTypeFont substitute)
        {
            this.delegate = delegate;
            this.substitute = substitute;
        }

        @Override
        public FontMapping<TrueTypeFont> getTrueTypeFont(String baseFont,
                PDFontDescriptor fontDescriptor)
        {
            return delegate.getTrueTypeFont(baseFont, fontDescriptor);
        }

        @Override
        public FontMapping<FontBoxFont> getFontBoxFont(String baseFont,
                PDFontDescriptor fontDescriptor)
        {
            return delegate.getFontBoxFont(baseFont, fontDescriptor);
        }

        @Override
        public CIDFontMapping getCIDFont(String baseFont, PDFontDescriptor fontDescriptor,
                PDCIDSystemInfo cidSystemInfo)
        {
            return new CIDFontMapping(substitute, null, true);
        }
    }
}
