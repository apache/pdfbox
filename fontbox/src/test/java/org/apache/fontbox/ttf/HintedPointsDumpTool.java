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

import java.io.File;
import java.io.PrintStream;

import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Developer tool (not a real test): dumps the grid-fitted F26Dot6 points of a range of characters at a
 * range of ppems, for comparison against FreeType's with {@code ft_points_dump.py} and
 * {@code compare_points.py} in {@code src/test/resources/ttf/hinting}. Skipped unless the
 * {@code dump.fonts} system property is set, e.g.
 *
 * <pre>
 *   mvn -pl fontbox test -Dtest=HintedPointsDumpTool \
 *       -Ddump.fonts=/usr/share/fonts/truetype/msttcorefonts/georgia.ttf,/path/to/other.ttf \
 *       -Ddump.out=/tmp/hint-compare
 * </pre>
 *
 * Properties: {@code dump.fonts} (comma-separated paths, required), {@code dump.out} (output
 * directory, required), {@code dump.suffix} (file suffix, default {@code fontbox}; use another one,
 * e.g. {@code baseline}, to keep a dump from a different build), {@code dump.ppems} and
 * {@code dump.chars} (defaults match {@code ft_points_dump.py}). Each font is written to
 * {@code <out>/<font name>.<suffix>}, one line per (ppem, glyph):
 * {@code <ppem> <gid> U+<code> X <x...> Y <y...>}, or {@code NULL} in place of the points where the
 * hinter declined (gasp, lowestRecPPEM, INSTCTRL or a failure).
 */
class HintedPointsDumpTool
{
    /** Printable ASCII plus common accented letters, which are composite glyphs in most fonts. */
    static final String DEFAULT_CHARS = asciiPrintable()
            + "áàâäãéèçñüÁÉÑÜ";

    static final String DEFAULT_PPEMS = "9,10,11,12,13,14,16,18,20,24,32";

    @Test
    void dumpPoints() throws Exception
    {
        String fonts = System.getProperty("dump.fonts");
        Assumptions.assumeTrue(fonts != null, "set -Ddump.fonts to dump hinted points");
        String outDir = System.getProperty("dump.out");
        Assumptions.assumeTrue(outDir != null, "set -Ddump.out to the output directory");
        String suffix = System.getProperty("dump.suffix", "fontbox");
        String[] ppems = System.getProperty("dump.ppems", DEFAULT_PPEMS).split(",");
        String chars = System.getProperty("dump.chars", DEFAULT_CHARS);

        new File(outDir).mkdirs();
        for (String path : fonts.split(","))
        {
            File fontFile = new File(path.trim());
            String name = fontFile.getName().replaceFirst("\\.[^.]*$", "");
            File outFile = new File(outDir, name + "." + suffix);
            try (PrintStream out = new PrintStream(outFile, "UTF-8"))
            {
                dumpFont(fontFile, ppems, chars, out);
            }
            System.out.println("wrote " + outFile);
        }
    }

    private static void dumpFont(File fontFile, String[] ppems, String chars, PrintStream out)
            throws Exception
    {
        // isEmbedded=true tolerates subset fonts that drop the otherwise-mandatory 'post' table
        TrueTypeFont font = new TTFParser(true).parse(new RandomAccessReadBufferedFile(fontFile));
        try
        {
            CmapLookup cmap = font.getUnicodeCmapLookup(false);
            GlyphHinter hinter = new GlyphHinter(font);
            for (String p : ppems)
            {
                int ppem = Integer.parseInt(p.trim());
                for (int i = 0; i < chars.length(); i++)
                {
                    char ch = chars.charAt(i);
                    int gid = cmap.getGlyphId(ch);
                    if (gid == 0)
                    {
                        continue;
                    }
                    StringBuilder line = new StringBuilder();
                    line.append(ppem).append(' ').append(gid)
                            .append(String.format(" U+%04X", (int) ch));
                    int[][] points = hinter.getHintedPointsF26Dot6(gid, ppem);
                    if (points == null)
                    {
                        line.append(" NULL");
                    }
                    else
                    {
                        line.append(" X");
                        for (int v : points[0])
                        {
                            line.append(' ').append(v);
                        }
                        line.append(" Y");
                        for (int v : points[1])
                        {
                            line.append(' ').append(v);
                        }
                    }
                    out.println(line);
                }
            }
        }
        finally
        {
            font.close();
        }
    }

    private static String asciiPrintable()
    {
        StringBuilder sb = new StringBuilder();
        for (char c = 33; c < 127; c++)
        {
            sb.append(c);
        }
        return sb.toString();
    }
}
