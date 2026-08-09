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
package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;

/**
 * Interface for glyph layout that is independent of a specific implementation so that more
 * implementations can be tried in the future.
 *
 * @author Volker Kunert
 */
public interface GlyphLayoutProcessorInterface
{

    /**
     * Checks if the font is supported
     *
     * @param font to be checked
     * @return true if glyph layout is supported for this font and this font is a PDType0Font
     */
    boolean supportsFont(PDFont font);

    /**
     * Compute the width for a text
     * @param font to be used
     * @param fontSize font size
     * @param text text
     * @return string width
     */
    float getStringWidth(PDType0Font font, float fontSize, String text) throws IOException;


    /**
     * Shows a text using glyph positioning (if needed)
     *
     * @param contentStream the content stream
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if glyphs are missing
     */
    void showText(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize, String text) throws IOException;
}
