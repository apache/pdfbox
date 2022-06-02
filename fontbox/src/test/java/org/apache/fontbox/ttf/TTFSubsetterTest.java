/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import org.apache.fontbox.util.autodetect.FontFileFinder;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TTFSubsetterTest
{

    /**
     * Test of PDFBOX-2854: empty subset with all tables.
     * 
     * @throws java.io.IOException
     */
    @Test
    void testEmptySubset() throws IOException
    {
        TrueTypeFont x = new TTFParser().parse(new RandomAccessReadBufferedFile(
                "src/test/resources/ttf/LiberationSans-Regular.ttf"));
        TTFSubsetter ttfSubsetter = new TTFSubsetter(x);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser(true)
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(1, subset.getNumberOfGlyphs());
            assertEquals(0, subset.nameToGID(".notdef"));
            assertNotNull(subset.getGlyph().getGlyph(0));
        }
    }

    /**
     * Test of PDFBOX-2854: empty subset with selected tables.
     * 
     * @throws java.io.IOException
     */
    @Test
    void testEmptySubset2() throws IOException
    {
        TrueTypeFont x = new TTFParser().parse(new RandomAccessReadBufferedFile(
                "src/test/resources/ttf/LiberationSans-Regular.ttf"));
        // List copied from TrueTypeEmbedder.java
        List<String> tables = new ArrayList<>();
        tables.add("head");
        tables.add("hhea");
        tables.add("loca");
        tables.add("maxp");
        tables.add("cvt ");
        tables.add("prep");
        tables.add("glyf");
        tables.add("hmtx");
        tables.add("fpgm");
        tables.add("gasp");
        TTFSubsetter ttfSubsetter = new TTFSubsetter(x, tables);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser(true)
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(1, subset.getNumberOfGlyphs());
            assertEquals(0, subset.nameToGID(".notdef"));
            assertNotNull(subset.getGlyph().getGlyph(0));
        }
    }

    /**
     * Test of PDFBOX-2854: subset with one glyph.
     * 
     * @throws java.io.IOException
     */
    @Test
    void testNonEmptySubset() throws IOException
    {
        TrueTypeFont full = new TTFParser().parse(new RandomAccessReadBufferedFile(
                "src/test/resources/ttf/LiberationSans-Regular.ttf"));
        TTFSubsetter ttfSubsetter = new TTFSubsetter(full);
        ttfSubsetter.add('a');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser(true)
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(2, subset.getNumberOfGlyphs());
            assertEquals(0, subset.nameToGID(".notdef"));
            assertEquals(1, subset.nameToGID("a"));
            assertNotNull(subset.getGlyph().getGlyph(0));
            assertNotNull(subset.getGlyph().getGlyph(1));
            assertNull(subset.getGlyph().getGlyph(2));
            assertEquals(full.getAdvanceWidth(full.nameToGID("a")),
                    subset.getAdvanceWidth(subset.nameToGID("a")));
            assertEquals(full.getHorizontalMetrics().getLeftSideBearing(full.nameToGID("a")),
                    subset.getHorizontalMetrics().getLeftSideBearing(subset.nameToGID("a")));
        }
    }

    /**
     * Test of PDFBOX-3319: check that widths and left side bearings in partially monospaced font
     * are kept.
     *
     * @throws java.io.IOException
     */
    @Test
    void testPDFBox3319() throws IOException
    {
        System.out.println("Searching for SimHei font...");
        FontFileFinder fontFileFinder = new FontFileFinder();
        List<URI> files = fontFileFinder.find();
        File simhei = null;
        for (URI uri : files)
        {
            String path = uri.getPath();
            if (path != null && path.toLowerCase(Locale.US).endsWith("simhei.ttf"))
            {
                simhei = new File(uri);
                break;
            }
        }
        Assumptions.assumeTrue(simhei != null, "SimHei font not available on this machine, test skipped");
        System.out.println("SimHei font found!");
        TrueTypeFont full = new TTFParser().parse(new RandomAccessReadBufferedFile(simhei));

        // List copied from TrueTypeEmbedder.java
        // Without it, the test would fail because of missing post table in source font
        List<String> tables = new ArrayList<>();
        tables.add("head");
        tables.add("hhea");
        tables.add("loca");
        tables.add("maxp");
        tables.add("cvt ");
        tables.add("prep");
        tables.add("glyf");
        tables.add("hmtx");
        tables.add("fpgm");
        tables.add("gasp");
        
        TTFSubsetter ttfSubsetter = new TTFSubsetter(full, tables);
        
        String chinese = "中国你好!";
        for (int offset = 0; offset < chinese.length();)
        {
            int codePoint = chinese.codePointAt(offset);
            ttfSubsetter.add(codePoint);
            offset += Character.charCount(codePoint);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser(true)
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(6, subset.getNumberOfGlyphs());

            for (Entry<Integer, Integer> entry : ttfSubsetter.getGIDMap().entrySet())
            {
                Integer newGID = entry.getKey();
                Integer oldGID = entry.getValue();
                assertEquals(full.getAdvanceWidth(oldGID), subset.getAdvanceWidth(newGID));
                assertEquals(full.getHorizontalMetrics().getLeftSideBearing(oldGID),
                        subset.getHorizontalMetrics().getLeftSideBearing(newGID));
            }
        }
    }

    /**
     * Test of PDFBOX-3379: check that left side bearings in partially monospaced font are kept.
     * 
     * @throws java.io.IOException
     */
    @Test
    void testPDFBox3379() throws IOException
    {
        TrueTypeFont full = new TTFParser()
                .parse(new RandomAccessReadBufferedFile("target/pdfs/DejaVuSansMono.ttf"));
        TTFSubsetter ttfSubsetter = new TTFSubsetter(full);
        ttfSubsetter.add('A');
        ttfSubsetter.add(' ');
        ttfSubsetter.add('B');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser()
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(4, subset.getNumberOfGlyphs());
            assertEquals(0, subset.nameToGID(".notdef"));
            assertEquals(1, subset.nameToGID("space"));
            assertEquals(2, subset.nameToGID("A"));
            assertEquals(3, subset.nameToGID("B"));
            String [] names = new String[]{"A","B","space"};
            for (String name : names)
            {
                assertEquals(full.getAdvanceWidth(full.nameToGID(name)),
                        subset.getAdvanceWidth(subset.nameToGID(name)));
                assertEquals(full.getHorizontalMetrics().getLeftSideBearing(full.nameToGID(name)),
                        subset.getHorizontalMetrics().getLeftSideBearing(subset.nameToGID(name)));
            }
        }
    }
    
    /**
     * Test of PDFBOX-3757: check that PostScript names that are not part of WGL4Names don't get
     * shuffled in buildPostTable().
     *
     * @throws java.io.IOException
     */
    @Test
    void testPDFBox3757() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(testFile));
        TTFSubsetter ttfSubsetter = new TTFSubsetter(ttf);
        ttfSubsetter.add('Ö');
        ttfSubsetter.add('\u200A');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        try (TrueTypeFont subset = new TTFParser(true)
                .parse(new RandomAccessReadBuffer(baos.toByteArray())))
        {
            assertEquals(5, subset.getNumberOfGlyphs());
            
            assertEquals(0, subset.nameToGID(".notdef"));
            assertEquals(1, subset.nameToGID("O"));
            assertEquals(2, subset.nameToGID("Odieresis"));
            assertEquals(3, subset.nameToGID("uni200A"));
            assertEquals(4, subset.nameToGID("dieresis.uc"));
            
            PostScriptTable pst = subset.getPostScript();
            assertEquals(".notdef", pst.getName(0));
            assertEquals("O", pst.getName(1));
            assertEquals("Odieresis", pst.getName(2));
            assertEquals("uni200A", pst.getName(3));
            assertEquals("dieresis.uc", pst.getName(4));
            
            assertTrue(subset.getPath("uni200A").getBounds2D().isEmpty(),
                    "Hair space path should be empty");
            assertFalse(subset.getPath("dieresis.uc").getBounds2D().isEmpty(),
                    "UC dieresis path should not be empty");
        }
    }
}
