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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.fontbox.util.autodetect.FontFileFinder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class TTFSubsetterTest
{

    /**
     * Test of PDFBOX-2854: empty subset with all tables.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testEmptySubset() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont x = new TTFParser().parse(testFile);
        TTFSubsetter ttfSubsetter = new TTFSubsetter(x);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        TrueTypeFont subset = new TTFParser(true).parse(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(1, subset.getNumberOfGlyphs());
        assertEquals(0, subset.nameToGID(".notdef"));
        assertNotNull(subset.getGlyph().getGlyph(0));
        subset.close();
    }

    /**
     * Test of PDFBOX-2854: empty subset with selected tables.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testEmptySubset2() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont x = new TTFParser().parse(testFile);
        // List copied from TrueTypeEmbedder.java
        List<String> tables = new ArrayList<String>();
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
        TrueTypeFont subset = new TTFParser(true).parse(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(1, subset.getNumberOfGlyphs());
        assertEquals(0, subset.nameToGID(".notdef"));
        assertNotNull(subset.getGlyph().getGlyph(0));
        subset.close();
    }

    /**
     * Test of PDFBOX-2854: subset with one glyph.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testNonEmptySubset() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont full = new TTFParser().parse(testFile);
        TTFSubsetter ttfSubsetter = new TTFSubsetter(full);
        ttfSubsetter.add('a');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        TrueTypeFont subset = new TTFParser(true).parse(new ByteArrayInputStream(baos.toByteArray()));
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
        subset.close();
    }

    /**
     * Test of PDFBOX-3319: check that widths and left side bearings in partially monospaced font
     * are kept.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testPDFBox3319() throws IOException
    {
        System.out.println("Searching for SimHei font...");
        FontFileFinder fontFileFinder = new FontFileFinder();
        List<URI> files = fontFileFinder.find();
        File simhei = null;
        for (URI uri : files)
        {
            if (uri.getPath() != null && uri.getPath().toLowerCase().endsWith("simhei.ttf"))
            {
                simhei = new File(uri);
            }
        }
        if (simhei == null)
        {
            System.err.println("SimHei font not available on this machine, test skipped");
            return;
        }
        System.out.println("SimHei font found!");
        TrueTypeFont full = new TTFParser().parse(simhei);

        // List copied from TrueTypeEmbedder.java
        // Without it, the test would fail because of missing post table in source font
        List<String> tables = new ArrayList<String>();
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
        TrueTypeFont subset = new TTFParser(true).parse(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(6, subset.getNumberOfGlyphs());

        for (Entry<Integer, Integer> entry : ttfSubsetter.getGIDMap().entrySet())
        {
            Integer newGID = entry.getKey();
            Integer oldGID = entry.getValue();
            assertEquals(full.getAdvanceWidth(oldGID), subset.getAdvanceWidth(newGID));
            assertEquals(full.getHorizontalMetrics().getLeftSideBearing(oldGID), 
                       subset.getHorizontalMetrics().getLeftSideBearing(newGID));
        }
        subset.close();
    }

    /**
     * Test of PDFBOX-3379: check that left side bearings in partially monospaced font are kept.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testPDFBox3379() throws IOException
    {
        InputStream is;
        try
        {
            // don't want to include this font into source download (300KB)
            System.out.println("Downloading DejaVuSansMono font...");
            is = new URL("https://issues.apache.org/jira/secure/attachment/12809395/DejaVuSansMono.ttf").openStream();
            System.out.println("Download finished!");
        }
        catch (IOException ex)
        {
            System.err.println("DejaVuSansMono font could not be downloaded, test skipped");
            return;
        }
        TrueTypeFont full = new TTFParser().parse(is);
        TTFSubsetter ttfSubsetter = new TTFSubsetter(full);
        ttfSubsetter.add('A');
        ttfSubsetter.add(' ');
        ttfSubsetter.add('B');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ttfSubsetter.writeToStream(baos);
        TrueTypeFont subset = new TTFParser().parse(new ByteArrayInputStream(baos.toByteArray()));
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
        subset.close();
    }
}
