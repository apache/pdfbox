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

package org.apache.fontbox_ai2;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;
import org.apache.fontbox_ai2.util.BoundingBox;

/**
 * Common interface for all FontBox fonts.
 *
 * @author John Hewson
 */
public interface FontBoxFont
{
    /**
     * The PostScript name of the font.
     */
    String getName() throws IOException;
    
    /**
     * Returns the font's bounding box in PostScript units.
     */
    BoundingBox getFontBBox() throws IOException;

    /**
     * Returns the FontMatrix in PostScript units.
     */
    List<Number> getFontMatrix() throws IOException;

    /**
     * Returns the path for the character with the given name.
     *
     * @return glyph path
     * @throws IOException if the path could not be read
     */
    GeneralPath getPath(String name) throws IOException;

    /**
     * Returns the advance width for the character with the given name.
     *
     * @return glyph advance width
     * @throws IOException if the path could not be read
     */
    float getWidth(String name) throws IOException;

    /**
     * Returns true if the font contains the given glyph.
     * 
     * @param name PostScript glyph name
     */
    boolean hasGlyph(String name) throws IOException;
}
