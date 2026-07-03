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

import java.io.IOException;

public interface ContentStreamForGlyphLayoutInterface
{

    /**
     * Show the given glyphs at the specified positions
     *
     * @param glyphsAndPositions List of glyphs and positions
     * @throws IOException if an IO error occurs
     */
    void showGlyphsWithPositioning(GlyphsAndPositions glyphsAndPositions) throws IOException;

    /**
     * Shows the glyphs for the given glyph codes
     *
     * @param glyphCodes Array of glyph codes of the content font
     * @throws IOException if an I/O exception occurs
     */
    void showGlyphCodes(int[] glyphCodes) throws IOException;

    /**
     * Set the text rise value, i.e. move the baseline up or down. This is useful for drawing
     * superscripts or subscripts.
     *
     * @param rise Specifies the distance, in unscaled text space units, to move the baseline up or
     * down from its default location. 0 restores the default location.
     * @throws IOException If the content stream could not be written.
     */
    void setTextRise(float rise) throws IOException;
}
