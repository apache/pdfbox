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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A 'kern' table in a true type font.
 * 
 * @author Glenn Adams
 */
public class KerningSubtable
{
    private static final Log LOG = LogFactory.getLog(KerningSubtable.class);

    // coverage field bit masks and values
    private static final int COVERAGE_HORIZONTAL = 0x0001;
    private static final int COVERAGE_MINIMUMS = 0x0002;
    private static final int COVERAGE_CROSS_STREAM = 0x0004;
    private static final int COVERAGE_FORMAT = 0xFF00;

    private static final int COVERAGE_HORIZONTAL_SHIFT = 0;
    private static final int COVERAGE_MINIMUMS_SHIFT = 1;
    private static final int COVERAGE_CROSS_STREAM_SHIFT = 2;
    private static final int COVERAGE_FORMAT_SHIFT = 8;

    // true if horizontal kerning
    private boolean horizontal;
    // true if minimum adjustment values (versus kerning values)
    private boolean minimums; 
    // true if cross-stream (block progression) kerning
    private boolean crossStream;
    // format specific pair data
    private PairData pairs;

    KerningSubtable()
    {
    }
    
    /**
     * This will read the required data from the stream.
     * 
     * @param data The stream to read the data from.
     * @param version The version of the table to be read
     * @throws IOException If there is an error reading the data.
     */
    void read(final TTFDataStream data, final int version) throws IOException
    {
        if (version == 0)
        {
            readSubtable0(data);
        }
        else if (version == 1)
        {
            readSubtable1(data);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    /**
     * Determine if subtable is designated for use in horizontal writing modes and
     * contains inline progression kerning pairs (not block progression "cross stream")
     * kerning pairs.
     *
     * @return true if subtable is for horizontal kerning
     */
    public boolean isHorizontalKerning()
    {
        return isHorizontalKerning(false);
    }

    /**
     * Determine if subtable is designated for use in horizontal writing modes, contains
     * kerning pairs (as opposed to minimum pairs), and, if CROSS is true, then return
     * cross stream designator; otherwise, if CROSS is false, return true if cross stream
     * designator is false.
     *
     * @param cross if true, then return cross stream designator in horizontal modes
     * @return true if subtable is for horizontal kerning in horizontal modes
     */
    public boolean isHorizontalKerning(final boolean cross)
    {
        if (!horizontal)
        {
            return false;
        }
        else if (minimums)
        {
            return false;
        }
        else if (cross)
        {
            return crossStream;
        }
        else
        {
            return !crossStream;
        }
    }

    /**
     * Obtain kerning adjustments for GLYPHS sequence, where the
     * Nth returned adjustment is associated with the Nth glyph
     * and the succeeding non-zero glyph in the GLYPHS sequence.
     *
     * Kerning adjustments are returned in font design coordinates.
     *
     * @param glyphs a (possibly empty) array of glyph identifiers
     * @return a (possibly empty) array of kerning adjustments
     */
    public int[] getKerning(final int[] glyphs)
    {
        int[] kerning = null;
        if (pairs != null)
        {
            final int ng = glyphs.length;
            kerning = new int[ng];
            for (int i = 0; i < ng; ++i)
            {
                final int l = glyphs[i];
                int r = -1;
                for (int k = i + 1; k < ng; ++k)
                {
                    final int g = glyphs[k];
                    if (g >= 0)
                    {
                        r = g;
                        break;
                    }
                }
                kerning[i] = getKerning(l, r);
            }
        }
        else
        {
            LOG.warn("No kerning subtable data available due to an unsupported kerning subtable version");
        }
        return kerning;
    }

    /**
     * Obtain kerning adjustment for glyph pair {L,R}.
     *
     * @param l left member of glyph pair
     * @param r right member of glyph pair
     * @return a (possibly zero) kerning adjustment
     */
    public int getKerning(final int l, final int r)
    {
        if (pairs == null)
        {
            LOG.warn("No kerning subtable data available due to an unsupported kerning subtable version");
            return 0;
        }
        return pairs.getKerning(l, r);
    }

    private void readSubtable0(final TTFDataStream data) throws IOException
    {
        final int version = data.readUnsignedShort();
        if (version != 0)
        {
            LOG.info("Unsupported kerning sub-table version: " + version);
            return;
        }
        final int length = data.readUnsignedShort();
        if (length < 6)
        {
            throw new IOException("Kerning sub-table too short, got " + length
                    + " bytes, expect 6 or more.");
        }
        final int coverage = data.readUnsignedShort();
        if (isBitsSet(coverage, COVERAGE_HORIZONTAL, COVERAGE_HORIZONTAL_SHIFT))
        {
            this.horizontal = true;
        }
        if (isBitsSet(coverage, COVERAGE_MINIMUMS, COVERAGE_MINIMUMS_SHIFT))
        {
            this.minimums = true;
        }
        if (isBitsSet(coverage, COVERAGE_CROSS_STREAM, COVERAGE_CROSS_STREAM_SHIFT))
        {
            this.crossStream = true;
        }
        final int format = getBits(coverage, COVERAGE_FORMAT, COVERAGE_FORMAT_SHIFT);
        if (format == 0)
        {
            readSubtable0Format0(data);
        }
        else if (format == 2)
        {
            readSubtable0Format2(data);
        }
        else
        {
            LOG.debug("Skipped kerning subtable due to an unsupported kerning subtable version: " + format);
        }
    }

    private void readSubtable0Format0(final TTFDataStream data) throws IOException
    {
        pairs = new PairData0Format0();
        pairs.read(data);
    }

    private void readSubtable0Format2(final TTFDataStream data)
    {
        LOG.info("Kerning subtable format 2 not yet supported.");
    }

    private void readSubtable1(final TTFDataStream data)
    {
        LOG.info("Kerning subtable format 1 not yet supported.");
    }

    private static boolean isBitsSet(final int bits, final int mask, final int shift)
    {
        return getBits(bits, mask, shift) != 0;
    }

    private static int getBits(final int bits, final int mask, final int shift)
    {
        return (bits & mask) >> shift;
    }

    private interface PairData
    {
        void read(TTFDataStream data) throws IOException;

        int getKerning(int l, int r);
    }

    private static class PairData0Format0 implements Comparator<int[]>, PairData
    {
        private int searchRange;
        private int[][] pairs;

        @Override
        public void read(final TTFDataStream data) throws IOException
        {
            final int numPairs = data.readUnsignedShort();
            searchRange = data.readUnsignedShort()/6;
            final int entrySelector = data.readUnsignedShort();
            final int rangeShift = data.readUnsignedShort();
            pairs = new int[numPairs][3];
            for (int i = 0; i < numPairs; ++i)
            {
                final int left = data.readUnsignedShort();
                final int right = data.readUnsignedShort();
                final int value = data.readSignedShort();
                pairs[i][0] = left;
                pairs[i][1] = right;
                pairs[i][2] = value;
            }
        }

        @Override
        public int getKerning(final int l, final int r)
        {
            final int[] key = new int[] { l, r, 0 };
            final int index = Arrays.binarySearch(pairs, key, this);
            if (index >= 0)
            {
                return pairs[index][2];
            }
            return 0;
        }

        @Override
        public int compare(final int[] p1, final int[] p2)
        {
            assert p1 != null;
            assert p1.length >= 2;
            assert p2 != null;
            assert p2.length >= 2;
            final int cmp1 = Integer.compare(p1[0], p2[0]);
            if (cmp1 != 0)
            {
                return cmp1;
            }
            return Integer.compare(p1[1], p2[1]);
        }
    }
}
