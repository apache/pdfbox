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

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Vladimir Plizga
 */
class GlyfCompositeDescriptTest {

    @Test
    @DisplayName("getComponentsView() method returns read-only list of all glyph components")
    void getComponentsView() throws IOException {
        // given
        OTFParser otfParser = new OTFParser();
        String fontPath = "src/test/resources/ttf/LiberationSans-Regular.ttf";
        OpenTypeFont font;
        try (RandomAccessRead fontFile = new RandomAccessReadBufferedFile(fontPath)) {
            font = otfParser.parse(fontFile);
        }
        GlyphTable glyphTable = font.getGlyph();
        GlyphData aacuteGlyph = glyphTable.getGlyph(131);       // A acute

        GlyphDescription glyphDescription = aacuteGlyph.getDescription();
        assertTrue(glyphDescription.isComposite());         // consists of glyphs 36 & 2335

        GlyfCompositeDescript compositeGlyphDescription = (GlyfCompositeDescript) glyphDescription;

        byte[] stubBytes = new byte[1024];
        Arrays.fill(stubBytes, (byte) 0);
        RandomAccessReadDataStream fakeInputStream = new RandomAccessReadDataStream(
                new ByteArrayInputStream(stubBytes));

        // when
        List<GlyfCompositeComp> componentsView = compositeGlyphDescription.getComponentsView();
        Executable viewModificationAttempt = () -> componentsView.add(new GlyfCompositeComp(fakeInputStream));

        //then
        assertEquals(2, componentsView.size());
        assertThrows(UnsupportedOperationException.class, viewModificationAttempt);
    }
}