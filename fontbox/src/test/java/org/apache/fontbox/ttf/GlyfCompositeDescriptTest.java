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
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Vladimir Plizga
 */
class GlyfCompositeDescriptTest
{

    @Test
    @DisplayName("getComponents() method returns read-only list of all glyph components")
    void getComponentsView() throws IOException
    {
        // given
        OTFParser otfParser = new OTFParser();
        String fontPath = "src/test/resources/ttf/LiberationSans-Regular.ttf";
        OpenTypeFont font;
        try (RandomAccessRead fontFile = new RandomAccessReadBufferedFile(fontPath))
        {
            font = otfParser.parse(fontFile);
        }
        GlyphTable glyphTable = font.getGlyph();
        // A acute
        GlyphData aacuteGlyph = glyphTable.getGlyph(131);

        GlyphDescription glyphDescription = aacuteGlyph.getDescription();
        // consists of glyphs 36 & 2335
        assertTrue(glyphDescription.isComposite());

        GlyfCompositeDescript compositeGlyphDescription = (GlyfCompositeDescript) glyphDescription;

        // check unmodifiable list
        List<GlyfCompositeComp> componentsView = compositeGlyphDescription.getComponents();
        assertEquals(2, componentsView.size());
        assertThrows(UnsupportedOperationException.class, () -> componentsView.remove(0));
    }
}
