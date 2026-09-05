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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fontbox.FontBoxFont;
import org.junit.jupiter.api.Test;

/**
 * CIDSystemInfo-based candidate filtering for CID font substitution.
 */
class CIDCharSetMatchTest
{
    private static final long CHINESE_TRADITIONAL = 1 << 20;
    private static final long CHINESE_SIMPLIFIED = 1 << 18;

    private static FontInfo info(final CIDSystemInfo ros, final int codePageRange1)
    {
        return new FontInfo()
        {
            @Override
            public String getPostScriptName()
            {
                return "TestFont";
            }

            @Override
            public FontFormat getFormat()
            {
                return FontFormat.OTF;
            }

            @Override
            public CIDSystemInfo getCIDSystemInfo()
            {
                return ros;
            }

            @Override
            public FontBoxFont getFont()
            {
                return null;
            }

            @Override
            public int getFamilyClass()
            {
                return 0;
            }

            @Override
            public int getWeightClass()
            {
                return 0;
            }

            @Override
            public int getCodePageRange1()
            {
                return codePageRange1;
            }

            @Override
            public int getCodePageRange2()
            {
                return 0;
            }

            @Override
            public int getMacStyle()
            {
                return 0;
            }

            @Override
            public PDPanoseClassification getPanose()
            {
                return null;
            }
        };
    }

    @Test
    void testCharSetMatch()
    {
        FontMapperImpl mapper = new FontMapperImpl();
        PDCIDSystemInfo cns1 = new PDCIDSystemInfo("Adobe", "CNS1", 0);

        // exact ROS match
        assertTrue(mapper.isCharSetMatch(cns1,
                info(new CIDSystemInfo("Adobe", "CNS1", 0), 0)));

        // a different legacy ROS never matches
        assertFalse(mapper.isCharSetMatch(cns1,
                info(new CIDSystemInfo("Adobe", "Japan1", 0), (int) CHINESE_TRADITIONAL)));

        // Adobe-Identity-0 (Noto CJK, Source Han) matches via its OS/2 code page bits
        assertTrue(mapper.isCharSetMatch(cns1,
                info(new CIDSystemInfo("Adobe", "Identity", 0), (int) CHINESE_TRADITIONAL)));
        assertFalse(mapper.isCharSetMatch(cns1,
                info(new CIDSystemInfo("Adobe", "Identity", 0), (int) CHINESE_SIMPLIFIED)));

        // ROS-less TrueType fonts keep matching via code page bits
        assertTrue(mapper.isCharSetMatch(cns1, info(null, (int) CHINESE_TRADITIONAL)));
        assertFalse(mapper.isCharSetMatch(cns1, info(null, 0)));
    }
}
