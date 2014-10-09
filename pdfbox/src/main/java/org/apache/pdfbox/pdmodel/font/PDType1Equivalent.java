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
import org.apache.fontbox.ttf.Type1Equivalent;

/**
 * A Type 1-equivalent font in a PDF, i.e. a font which can access glyphs by their PostScript name.
 * May be a PFB, CFF, or TTF.
 *
 * @author John Hewson
 */
public interface PDType1Equivalent extends PDFontLike
{
    /**
     * Returns the name of this font.
     */
    public String getName();

    /**
     * Returns the glyph name for the given character code.
     *
     * @param code character code
     * @return PostScript glyph name
     */
    public String codeToName(int code) throws IOException;

    /**
     * Returns the glyph path for the given character code.
     * @param name PostScript glyph name
     * @throws java.io.IOException if the font could not be read
     */
    public GeneralPath getPath(String name) throws IOException;

    /**
     * Returns the embedded or system font for rendering. This font is a Type 1-equivalent, but
     * may not be a Type 1 font, it could be a CFF font or TTF font. If there is no suitable font
     * then the fallback font will be returned: this method never returns null.
     */
    public Type1Equivalent getType1Equivalent();
}
