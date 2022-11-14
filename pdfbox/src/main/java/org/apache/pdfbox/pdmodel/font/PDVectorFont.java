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

import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * A vector outline font, e.g. not Type 3.
 *
 * @author John Hewson
 */
public interface PDVectorFont
{
    /**
     * Returns the glyph path for the given character code.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @return the glyph path for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    GeneralPath getPath(int code) throws IOException;

    /**
     * Returns the normalized glyph path for the given character code in a PDF. The resulting path is normalized to the
     * PostScript 1000 unit square, and fallback glyphs are returned where appropriate, e.g. for missing glyphs.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @return the normalized glyph path for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    GeneralPath getNormalizedPath(int code) throws IOException;

    /**
     * Returns true if this font contains a glyph for the given character code in a PDF.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @return true if this font contains a glyph for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    boolean hasGlyph(int code) throws IOException;
}
