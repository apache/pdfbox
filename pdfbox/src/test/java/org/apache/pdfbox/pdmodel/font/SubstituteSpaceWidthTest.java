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

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;

/**
 * PDFBOX-6292: The space width of a non-embedded Identity-H CIDFontType2 without ToUnicode must
 * come from the PDF, not from whichever substitute is installed.
 */
public class SubstituteSpaceWidthTest
{
    @Test
    public void testSpaceWidthIndependentOfSubstitute() throws IOException
    {
        PDType0Font font = new PDType0Font(simSunIdentityH());
        PDCIDFontType2 cidFont = (PDCIDFontType2) font.getDescendantFont();
        // any substitute works, down to the bundled last-resort font, as long as its space GID
        // lands in the /W range below
        int substituteSpaceGid = cidFont.getTrueTypeFont().getUnicodeCmapLookup().getGlyphId(' ');
        assumeTrue("substitute's space GID outside the test's /W range",
                substituteSpaceGid > 0 && substituteSpaceGid < 32);

        // an Identity CMap without ToUnicode says nothing about which code is a space; the answer
        // must not depend on which substitute happens to be installed
        assertEquals(font.getWidth(32), font.getSpaceWidth(), 0);
    }

    private static COSDictionary simSunIdentityH()
    {
        COSDictionary ros = new COSDictionary();
        ros.setItem(COSName.REGISTRY, new COSString("Adobe"));
        ros.setItem(COSName.ORDERING, new COSString("GB1"));
        ros.setInt(COSName.SUPPLEMENT, 2);

        COSDictionary fd = new COSDictionary();
        fd.setItem(COSName.TYPE, COSName.FONT_DESC);
        fd.setName(COSName.FONT_NAME, "SimSun");
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

        // widths for codes 0-31 only: the substitute's space GID lands here if it is wrongly
        // used as a code, while the PDF's code 32 falls back to /DW
        COSArray widths = new COSArray();
        for (int i = 0; i < 32; i++)
        {
            widths.add(COSInteger.get(500));
        }
        COSArray w = new COSArray();
        w.add(COSInteger.get(0));
        w.add(widths);

        COSDictionary cid = new COSDictionary();
        cid.setItem(COSName.TYPE, COSName.FONT);
        cid.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE2);
        cid.setName(COSName.BASE_FONT, "SimSun");
        cid.setItem(COSName.CIDSYSTEMINFO, ros);
        cid.setItem(COSName.FONT_DESC, fd);
        cid.setInt(COSName.DW, 1000);
        cid.setItem(COSName.W, w);

        COSArray descendants = new COSArray();
        descendants.add(cid);
        COSDictionary type0 = new COSDictionary();
        type0.setItem(COSName.TYPE, COSName.FONT);
        type0.setItem(COSName.SUBTYPE, COSName.TYPE0);
        type0.setName(COSName.BASE_FONT, "SimSun");
        type0.setName(COSName.ENCODING, "Identity-H");
        type0.setItem(COSName.DESCENDANT_FONTS, descendants);
        return type0;
    }
}
