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

import java.io.InputStream;
import java.io.PrintStream;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Developer tool (not a real test): dumps the interpreter's per-instruction execution trace for one
 * glyph, to be diffed against a FreeType {@code ttinterp} trace by {@code trace_diff.py}. Skipped
 * unless the {@code trace.gid} system property is set, e.g.
 *
 * <pre>
 *   mvn -pl fontbox test -Dtest=GlyphTraceTool \
 *       -Dtrace.font=src/test/resources/ttf/LiberationSans-Regular.ttf \
 *       -Dtrace.gid=22 -Dtrace.ppem=11 -Dtrace.out=/tmp/our-trace.txt
 * </pre>
 */
class GlyphTraceTool
{
    @Test
    void dumpTrace() throws Exception
    {
        String gidProp = System.getProperty("trace.gid");
        Assumptions.assumeTrue(gidProp != null, "set -Dtrace.gid to dump a trace");

        int gid = Integer.parseInt(gidProp);
        int ppem = Integer.parseInt(System.getProperty("trace.ppem", "11"));
        int point = Integer.parseInt(System.getProperty("trace.point", "-1"));
        String fontPath = System.getProperty("trace.font",
                "src/test/resources/ttf/LiberationSans-Regular.ttf");
        String outPath = System.getProperty("trace.out");

        TrueTypeFont font;
        try (InputStream is = new java.io.FileInputStream(fontPath))
        {
            // isEmbedded=true tolerates subset fonts that drop the otherwise-mandatory 'post' table
            font = new TTFParser(true).parse(new RandomAccessReadBuffer(is));
        }

        PrintStream out = outPath != null ? new PrintStream(outPath, "UTF-8") : System.out;
        try
        {
            new GlyphHinter(font).traceGlyph(gid, ppem, out, point);
        }
        finally
        {
            if (outPath != null)
            {
                out.close();
            }
        }
    }
}
