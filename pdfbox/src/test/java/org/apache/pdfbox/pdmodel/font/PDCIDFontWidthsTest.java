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

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Widths of a CIDFont from its /W array (PDFBOX-6319): a c_first c_last w range is not expanded
 * per CID.
 */
class PDCIDFontWidthsTest
{
    private static PDType0Font type0Font(COSBase... w) throws IOException
    {
        COSDictionary systemInfo = new COSDictionary();
        systemInfo.setString(COSName.REGISTRY, "Adobe");
        systemInfo.setString(COSName.ORDERING, "Identity");
        systemInfo.setInt(COSName.SUPPLEMENT, 0);
        COSDictionary cidFont = new COSDictionary();
        cidFont.setItem(COSName.TYPE, COSName.FONT);
        cidFont.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE2);
        cidFont.setName(COSName.BASE_FONT, "Test");
        cidFont.setItem(COSName.CIDSYSTEMINFO, systemInfo);
        cidFont.setInt(COSName.DW, 1000);
        COSArray widths = new COSArray();
        for (COSBase item : w)
        {
            widths.add(item);
        }
        cidFont.setItem(COSName.W, widths);
        COSArray descendants = new COSArray();
        descendants.add(cidFont);
        COSDictionary font = new COSDictionary();
        font.setItem(COSName.TYPE, COSName.FONT);
        font.setItem(COSName.SUBTYPE, COSName.TYPE0);
        font.setName(COSName.BASE_FONT, "Test");
        font.setItem(COSName.ENCODING, COSName.IDENTITY_H);
        font.setItem(COSName.DESCENDANT_FONTS, descendants);
        return new PDType0Font(font);
    }

    @Test
    @Timeout(10)
    void testRangeUpToIntegerMaxValueIsNotExpanded() throws IOException
    {
        PDType0Font font = type0Font(COSInteger.ZERO, COSInteger.get(Integer.MAX_VALUE),
                COSInteger.get(500));
        assertEquals(500, font.getWidth(0x4E00));
        assertTrue(font.hasExplicitWidth(0xFFFF));
        assertEquals(500, font.getAverageFontWidth());
    }

    @Test
    void testReversedRangeIsIgnored() throws IOException
    {
        PDType0Font font = type0Font(COSInteger.get(5), COSInteger.ONE, new COSFloat(250f),
                new COSString("x"));
        assertEquals(1000, font.getWidth(3));
        assertFalse(font.hasExplicitWidth(3));
    }
}
