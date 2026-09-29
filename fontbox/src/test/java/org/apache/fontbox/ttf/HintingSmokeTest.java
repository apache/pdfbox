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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Hints every glyph of each TrueType test font at a range of sizes and requires that none fails. A
 * failure (malformed-looking bytecode, an exhausted execution budget, an unimplemented opcode) falls
 * back to the raw outline silently, so without this a whole font can lose its hinting unnoticed: the
 * loop budget for {@code prep} was once sized for glyph programs, and Keyboard.ttf's prep, which
 * loops 240 times, disabled hinting for every glyph. Glyphs that are legitimately not hinted (no
 * instructions, excluded by gasp or lowestRecPPEM) are not failures. Correctness of the result is
 * {@link GoldenHintingTest}'s job; this only checks that the programs run.
 */
class HintingSmokeTest
{
    private static final int[] PPEMS = { 9, 12, 16, 24, 48 };

    @ParameterizedTest
    @ValueSource(strings = {
        "src/test/resources/ttf/LiberationSans-Regular.ttf",
        "src/test/resources/ttf/JosefinSans-Italic.ttf",
        "src/test/resources/ttf/Lohit-Bengali.ttf",
        "src/test/resources/ttf/Lohit-Devanagari.ttf",
        "src/test/resources/ttf/Lohit-Gujarati.ttf",
        "src/test/resources/ttf/Lohit-Tamil.ttf",
        // downloaded by the build (see pom.xml)
        "target/fonts/DejaVuSansMono.ttf",
        "target/fonts/Keyboard.ttf",
        "target/fonts/NotoEmoji-Regular.ttf",
        "target/fonts/NotoMono-Regular.ttf",
        "target/fonts/ipag00303/ipag.ttf" })
    void testEveryGlyphHintsWithoutFailure(String path) throws IOException
    {
        try (TrueTypeFont font = new TTFParser(true).parse(new RandomAccessReadBufferedFile(path)))
        {
            GlyphHinter hinter = new GlyphHinter(font);
            int glyphs = font.getNumberOfGlyphs();
            assertTrue(glyphs > 0, path);
            for (int ppem : PPEMS)
            {
                for (int gid = 0; gid < glyphs; gid++)
                {
                    hinter.getHintedPointsF26Dot6(gid, ppem);
                }
            }
            assertEquals(0, hinter.getFailureCount(),
                    path + ": " + hinter.getFailureCount() + " failures, first " + hinter.getFirstFailure());
        }
    }
}
