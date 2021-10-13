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

package org.apache.fontbox.ttf.advanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEMappingRange;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SubtableEntry;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>Base class implementation of glyph mapping table. This base
 * class maps glyph indices to arbitrary integers (mappping indices), and
 * is used to implement both glyph coverage and glyph class maps.</p>
 *
 * @author Glenn Adams
 */
public class GlyphMappingTable {

    /** empty mapping table */
    public static final int GLYPH_MAPPING_TYPE_EMPTY = 0;

    /** mapped mapping table */
    public static final int GLYPH_MAPPING_TYPE_MAPPED = 1;

    /** range based mapping table */
    public static final int GLYPH_MAPPING_TYPE_RANGE = 2;

    /**
     * Obtain mapping type.
     * @return mapping format type
     */
    public int getType() {
        return -1;
    }

    /**
     * Obtain mapping entries.
     * @return list of mapping entries
     */
    public List<SubtableEntry> getEntries() {
        return null;
    }

    /**
     * Obtain size of mapping table, i.e., ciMax + 1, where ciMax is the maximum
     * mapping index.
     * @return size of mapping table
     */
    public int getMappingSize() {
        return 0;
    }

    /**
     * Map glyph identifier (code) to coverge index. Returns -1 if glyph identifier is not in the domain of
     * the mapping table.
     * @param gid glyph identifier (code)
     * @return non-negative glyph mapping index or -1 if glyph identifiers is not mapped by table
     */
    public int getMappedIndex(int gid) {
        return -1;
    }

    /** empty mapping table base class */
    protected static class EmptyMappingTable extends GlyphMappingTable {
        /**
         * Construct empty mapping table.
         */
        public EmptyMappingTable() {
            this(null);
        }

        /**
         * Construct empty mapping table with entries (ignored).
         * @param entries list of entries (ignored)
         */
        public EmptyMappingTable(List<SubtableEntry> entries) {
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GLYPH_MAPPING_TYPE_EMPTY;
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return new java.util.ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public int getMappingSize() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid) {
            return -1;
        }
    }

    /** mapped mapping table base class */
    protected static class MappedMappingTable extends GlyphMappingTable {
        /**
         * Construct mapped mapping table.
         */
        public MappedMappingTable() {
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GLYPH_MAPPING_TYPE_MAPPED;
        }
    }

    /** range mapping table base class */
    protected abstract static class RangeMappingTable extends GlyphMappingTable {
        private int[] sa;                                                // array of range (inclusive) starts
        private int[] ea;                                                // array of range (inclusive) ends
        private int[] ma;                                                // array of range mapped values
        private int miMax = -1;
        /**
         * Construct range mapping table.
         * @param entries of mapping ranges
         */
        public RangeMappingTable(List<SubtableEntry> entries) {
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GLYPH_MAPPING_TYPE_RANGE;
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (sa != null) {
                return rangeMap(sa.length, (i) -> new SEMappingRange(new MappingRange(sa [ i ], ea [ i ], ma [ i ])));
            }
            return new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public int getMappingSize() {
            return miMax + 1;
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid) {
            int i;
            int mi;
            if ((i = Arrays.binarySearch(sa, gid)) >= 0) {
                mi = getMappedIndex(gid, sa [ i ], ma [ i ]);                // matches start of (some) range
            } else if ((i = -(i + 1)) == 0) {
                mi = -1;                                                        // precedes first range
            } else if (gid > ea [ --i ]) {
                mi = -1;                                                        // follows preceding (or last) range
            } else {
                mi = getMappedIndex(gid, sa [ i ], ma [ i ]);                // intersects (some) range
            }
            return mi;
        }

        /**
         * Map glyph identifier (code) to coverge index. Returns -1 if glyph identifier is not in the domain of
         * the mapping table.
         * @param gid glyph identifier (code)
         * @param s start of range
         * @param m mapping value
         * @return non-negative glyph mapping index or -1 if glyph identifiers is not mapped by table
         */
        public abstract int getMappedIndex(int gid, int s, int m);

        private void populate(List<SubtableEntry> entries) {
            int n = entries.size();
            int gidMax = -1;
            int miMax = -1;
            int[] sa = new int [ n ];
            int[] ea = new int [ n ];
            int[] ma = new int [ n ];

            for (int idx = 0; idx < n; idx++) {
                MappingRange r = checkGet(entries, idx, SEMappingRange.class).get();

                int gs = r.getStart();
                int ge = r.getEnd();
                int mi = r.getIndex();

                checkGidRange(gs, () -> "illegal glyph range: [" + gs + "," + ge + "]: bad start index");
                checkGidRange(ge, () -> "illegal glyph range: [" + gs + "," + ge + "]: bad end index");
                checkCondition(gs, notGt(ge), () -> "illegal glyph range: [" + gs + "," + ge + "]: start index exceeds end index");
                checkCondition(gs, notLt(gidMax), () -> "out of order glyph range: [" + gs + "," + ge + "]");
                checkCondition(mi, notLt(0), () -> "illegal mapping index: " + mi);

                int miLast;
                sa [ idx ] = gs;
                ea [ idx ] = gidMax = ge;
                ma [ idx ] = mi;

                if ((miLast = mi + (ge - gs)) > miMax) {
                    miMax = miLast;
                }
            }

            assert this.sa == null;
            assert this.ea == null;
            assert this.ma == null;
            this.sa = sa;
            this.ea = ea;
            this.ma = ma;
            this.miMax = miMax;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return IntStream.range(0, sa.length)
              .mapToObj(i -> '[' + sa [ i ] + ea [ i ] + "]:" + ma [ i ])
              .collect(Collectors.joining(",", "{", "}"));
        }
    }

    /**
     * The <code>MappingRange</code> class encapsulates a glyph [start,end] range and
     * a mapping index.
     */
    public static class MappingRange {

        private final int gidStart;                     // first glyph in range (inclusive)
        private final int gidEnd;                       // last glyph in range (inclusive)
        private final int index;                        // mapping index;

        /**
         * Instantiate a mapping range.
         */
        public MappingRange() {
            this (0, 0, 0);
        }

        /**
         * Instantiate a specific mapping range.
         * @param gidStart start of range
         * @param gidEnd end of range
         * @param index mapping index
         */
        public MappingRange(int gidStart, int gidEnd, int index) {
            if ((gidStart < 0) || (gidEnd < 0) || (index < 0)) {
                throw new AdvancedTypographicTableFormatException();
            } else if (gidStart > gidEnd) {
                throw new AdvancedTypographicTableFormatException();
            } else {
                this.gidStart = gidStart;
                this.gidEnd = gidEnd;
                this.index = index;
            }
        }

        /** @return start of range */
        public int getStart() {
            return gidStart;
        }

        /** @return end of range */
        public int getEnd() {
            return gidEnd;
        }

        /** @return mapping index */
        public int getIndex() {
            return index;
        }

        /** @return interval as a pair of integers */
        public int[] getInterval() {
            return new int[] { gidStart, gidEnd };
        }

        /**
         * Obtain interval, filled into first two elements of specified array.
         * @param interval an array of length two
         * @return interval as a pair of integers, filled into specified array
         */
        public int[] getInterval(int[] interval) {
            if ((interval == null) || (interval.length != 2)) {
                throw new IllegalArgumentException();
            } else {
                interval[0] = gidStart;
                interval[1] = gidEnd;
            }
            return interval;
        }

        /** @return length of interval */
        public int getLength() {
            return gidStart - gidEnd;
        }

    }

}
