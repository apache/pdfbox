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
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * A font-like object.
 *
 * @author John Hewson
 */
public interface PDFontLike
{
    /**
     * Returns the name of this font, either the PostScript "BaseName" or the Type 3 "Name".
     */
    String getName();

    /**
     * Returns the font descriptor, may be null.
     */
    PDFontDescriptor getFontDescriptor();

    /**
     * Returns the font matrix, which represents the transformation from glyph space to text space.
     */
    Matrix getFontMatrix();

    /**
     * Returns the font's bounding box.
     */
    BoundingBox getBoundingBox() throws IOException;

    /**
     * Returns the position vector (v), in text space, for the given character.
     * This represents the position of vertical origin relative to horizontal origin, for
     * horizontal writing it will always be (0, 0). For vertical writing both x and y are set.
     *
     * @param code character code
     * @return position vector
     */
    Vector getPositionVector(int code);

    /**
     * Returns the height of the given character, in glyph space. This can be expensive to
     * calculate. Results are only approximate.<p>
     * 
     * Warning: This method is deprecated in PDFBox 2.0 because there is no meaningful value
     * which it can return. The {@link #getWidth(int)} method returns the advance width of a glyph,
     * but there is no corresponding advance height. The logical height of a character is the same
     * for every character in a font, so if you want that, retrieve the font bbox's height. 
     * Otherwise if you want the visual bounds of the glyph then call getPath(..) on the appropriate
     * PDFont subclass to retrieve the glyph outline as a GeneralPath.
     * 
     * @param code character code
     * @deprecated Use
     * {@link #getBoundingBox() getBoundingBox()}.{@link BoundingBox#getHeight() getHeight()}
     * instead.
     */
    @Deprecated
    float getHeight(int code) throws IOException;

    /**
     * Returns the advance width of the given character, in glyph space.<p>
     * 
     * If you want the visual bounds of the glyph then call getPath(..) on the appropriate
     * PDFont subclass to retrieve the glyph outline as a GeneralPath instead.
     *
     * @param code character code
     */
    float getWidth(int code) throws IOException;

    /**
     * Returns the width of a glyph in the embedded font file.
     *
     * @param code character code
     * @return width in glyph space
     * @throws IOException if the font could not be read
     */
    float getWidthFromFont(int code) throws IOException;

    /**
     * Returns true if the font file is embedded in the PDF.
     */
    boolean isEmbedded();

    /**
     * Returns true if the embedded font file is damaged.
     */
    boolean isDamaged();

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     */
    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
    float getAverageFontWidth();
}
