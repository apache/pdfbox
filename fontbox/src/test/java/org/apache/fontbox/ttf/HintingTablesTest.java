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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

/**
 * Tests parsing of the TrueType hinting tables ('cvt ', 'fpgm', 'prep', 'gasp').
 *
 * LiberationSans-Regular was built as a hand-hinted, metric-compatible Arial replacement, so it
 * carries a full bytecode hinting program and all four tables. The expected values below were taken
 * from its on-disk table directory.
 */
class HintingTablesTest
{
    private static TrueTypeFont parse(String resource) throws IOException
    {
        try (InputStream is = HintingTablesTest.class.getResourceAsStream(resource))
        {
            assertNotNull(is, "missing test resource " + resource);
            return new TTFParser().parse(new RandomAccessReadBuffer(is));
        }
    }

    @Test
    void testControlValueTable() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        ControlValueTable cvt = font.getControlValues();
        assertNotNull(cvt);
        // 648-byte table / 2 bytes per FWord
        assertEquals(324, cvt.getValueCount());
        assertEquals(324, cvt.getValues().length);
        // CVT entries are signed FWords (raw font units, not yet scaled to ppem)
        assertEquals(1484, cvt.getValues()[0]);
    }

    @Test
    void testFontProgramTable() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        FontProgramTable fpgm = font.getFontProgram();
        assertNotNull(fpgm);
        assertEquals(1972, fpgm.getProgram().length);
    }

    @Test
    void testControlValueProgramTable() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        ControlValueProgramTable prep = font.getControlValueProgram();
        assertNotNull(prep);
        assertEquals(835, prep.getProgram().length);
    }

    @Test
    void testGaspTable() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        GaspTable gasp = font.getGasp();
        assertNotNull(gasp);
        assertEquals(0, gasp.getVersion());

        // three ranges: (<=8: DOGRAY), (<=17: GRIDFIT), (<=65535: GRIDFIT|DOGRAY)
        assertArrayEquals(new int[] { 8, 17, 65535 }, gasp.getRangeMaxPPEM());
        assertArrayEquals(new int[] { GaspTable.GASP_DOGRAY, GaspTable.GASP_GRIDFIT,
                GaspTable.GASP_GRIDFIT | GaspTable.GASP_DOGRAY }, gasp.getRangeFlags());

        // ppem -> flags lookup, including range boundaries
        assertEquals(GaspTable.GASP_DOGRAY, gasp.getFlags(8));
        assertEquals(GaspTable.GASP_GRIDFIT, gasp.getFlags(9));
        assertEquals(GaspTable.GASP_GRIDFIT, gasp.getFlags(17));
        assertEquals(GaspTable.GASP_GRIDFIT | GaspTable.GASP_DOGRAY, gasp.getFlags(18));
        assertEquals(GaspTable.GASP_GRIDFIT | GaspTable.GASP_DOGRAY, gasp.getFlags(2000));

        // grid-fitting is off at 8 ppem and below, on above it
        assertFalse(gasp.isGridFit(8));
        assertTrue(gasp.isGridFit(9));
        assertTrue(gasp.isGridFit(16));
    }

    @Test
    void testAbsentTablesReturnNull() throws IOException
    {
        // None of the bundled fonts lacks all four tables, but the absent-table path is covered:
        // JosefinSans-Italic has no 'cvt '/'fpgm' (but does have 'prep'/'gasp')...
        TrueTypeFont josefin = parse("/ttf/JosefinSans-Italic.ttf");
        assertNull(josefin.getControlValues());
        assertNull(josefin.getFontProgram());
        assertNotNull(josefin.getControlValueProgram());
        assertNotNull(josefin.getGasp());

        // ...and Lohit-Tamil has no 'gasp'. Absent accessors must return null, not throw.
        TrueTypeFont lohit = parse("/ttf/Lohit-Tamil.ttf");
        assertNull(lohit.getGasp());
        assertNotNull(lohit.getControlValues());
        assertNotNull(lohit.getFontProgram());
        assertNotNull(lohit.getControlValueProgram());
    }
}