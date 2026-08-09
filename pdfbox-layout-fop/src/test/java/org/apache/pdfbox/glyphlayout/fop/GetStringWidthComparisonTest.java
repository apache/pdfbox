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

package org.apache.pdfbox.glyphlayout.fop;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that compare getStringWidth between the AWT and FOP glyph layout processors.
 */
class GetStringWidthComparisonTest extends TestBase
{
    private static final String DEJAVU_PATH = "/ttf/DejaVuSans.ttf";
    private static final String DEJAVU_STRING =  "AVATAR, effective, affiliation, float, film, affluent";

    @Test
    void testGetStringWidthAwtVsFop() throws IOException, FontFormatException, URISyntaxException
    {
        GlyphLayoutProcessorAwt awt = new GlyphLayoutProcessorAwt();
        GlyphLayoutProcessorFop fop = new GlyphLayoutProcessorFop();

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font awtFont = createPdType0Font(awt, doc, DEJAVU_PATH);
            PDType0Font fopFont = createPdType0Font(fop, doc, DEJAVU_PATH);

            float wA = awt.getStringWidth(awtFont, fontSize, DEJAVU_STRING);
            float wF = fop.getStringWidth(fopFont, fontSize, DEJAVU_STRING);

            assertEquals(wA, wF, 0.5f, "AWT and FOP getStringWidth should match within tolerance");
        }
    }
}
