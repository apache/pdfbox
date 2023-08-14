/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.pdfbox.debugger.flagbitspane;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

/**
 * @author Khyrul Bashar
 *
 * A class that provides Font flag bits.
 */
public class FontFlag extends Flag
{
    private final COSDictionary fontDescriptor;

    /**
     * Constructor
     * @param fontDescDictionary COSDictionary instance.
     */
    FontFlag(COSDictionary fontDescDictionary)
    {
        fontDescriptor = fontDescDictionary;
    }

    @Override
    String getFlagType()
    {
        return "Font flag";
    }

    @Override
    String getFlagValue()
    {
        return "Flag value:" + fontDescriptor.getInt(COSName.FLAGS);
    }

    @Override
    Object[][] getFlagBits()
    {
        PDFontDescriptor fontDesc = new PDFontDescriptor(fontDescriptor);
        return new Object[][]{
                new Object[]{1, "FixedPitch", fontDesc.isFixedPitch()},
                new Object[]{2, "Serif", fontDesc.isSerif()},
                new Object[]{3, "Symbolic", fontDesc.isSymbolic()},
                new Object[]{4, "Script", fontDesc.isScript()},
                new Object[]{6, "NonSymbolic", fontDesc.isNonSymbolic()},
                new Object[]{7, "Italic", fontDesc.isItalic()},
                new Object[]{17, "AllCap", fontDesc.isAllCap()},
                new Object[]{18, "SmallCap", fontDesc.isSmallCap()},
                new Object[]{19, "ForceBold", fontDesc.isForceBold()}
        };
    }
}
