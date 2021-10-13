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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEInteger;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEMappingRange;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SubtableEntry;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>.Base class implementation of glyph coverage table.</p>
 *
 * @author Glenn Adams
 */
public final class GlyphCoverageTable extends GlyphMappingTable implements GlyphCoverageMapping {

    /* logging instance */
    private static final Log log = LogFactory.getLog(GlyphCoverageTable.class);

    /** empty mapping table */
    public static final int GLYPH_COVERAGE_TYPE_EMPTY = GLYPH_MAPPING_TYPE_EMPTY;

    /** mapped mapping table */
    public static final int GLYPH_COVERAGE_TYPE_MAPPED = GLYPH_MAPPING_TYPE_MAPPED;

    /** range based mapping table */
    public static final int GLYPH_COVERAGE_TYPE_RANGE = GLYPH_MAPPING_TYPE_RANGE;

    private GlyphCoverageMapping cm;

    private GlyphCoverageTable(GlyphCoverageMapping cm) {
        assert cm != null;
        assert cm instanceof GlyphMappingTable;
        this.cm = cm;
    }

    /** {@inheritDoc} */
    @Override
    public int getType() {
        return ((GlyphMappingTable) cm) .getType();
    }

    /** {@inheritDoc} */
    @Override
    public List<SubtableEntry> getEntries() {
        return ((GlyphMappingTable) cm) .getEntries();
    }

    /** {@inheritDoc} */
    @Override
    public int getCoverageSize() {
        return cm.getCoverageSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getCoverageIndex(int gid) {
        return cm.getCoverageIndex(gid);
    }

    /**
     * Create glyph coverage table.
     * @param entries list of mapped or ranged coverage entries, or null or empty list
     * @return a new covera table instance
     */
    public static GlyphCoverageTable createCoverageTable(List<SubtableEntry> entries) {
        GlyphCoverageMapping cm;
        if ((entries == null) || (entries.size() == 0)) {
            cm = new EmptyCoverageTable(entries);
        } else if (isMappedCoverage(entries)) {
            cm = new MappedCoverageTable(entries);
        } else if (isRangeCoverage(entries)) {
            cm = new RangeCoverageTable(entries);
        } else {
            cm = null;
        }
        assert cm != null : "unknown coverage type";
        return new GlyphCoverageTable(cm);
    }

    private static boolean isMappedCoverage(List<SubtableEntry> entries) {
        if ((entries == null) || (entries.isEmpty())) {
            return false;
        } else {
            return allOfType(entries, SEInteger.class);
        }
    }

    private static boolean isRangeCoverage(List<SubtableEntry> entries) {
        if ((entries == null) || (entries.isEmpty())) {
            return false;
        } else {
            return allOfType(entries, SEMappingRange.class);
        }
    }

    private static class EmptyCoverageTable extends GlyphMappingTable.EmptyMappingTable implements GlyphCoverageMapping {
        public EmptyCoverageTable(List<SubtableEntry> entries) {
            super(entries);
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageSize() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageIndex(int gid) {
            return -1;
        }
    }

    private static class MappedCoverageTable extends GlyphMappingTable.MappedMappingTable implements GlyphCoverageMapping {
        private int[] map;
        public MappedCoverageTable(List<SubtableEntry> entries) {
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return arrayMap(map, SEInteger::valueOf);
        }

        /** {@inheritDoc} */
        @Override
        public int getMappingSize() {
            return (map != null) ? map.length : 0;
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid) {
            int i;
            if ((i = Arrays.binarySearch(map, gid)) >= 0) {
                return i;
            } else {
                return -1;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageSize() {
            return getMappingSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageIndex(int gid) {
            return getMappedIndex(gid);
        }

        private void populate(List<SubtableEntry> entries) {
            int i = 0;
            int skipped = 0;
            int n = entries.size();
            int gidMax = -1;
            int[] map = new int [ n ];

            for (int idx = 0; idx < n; idx++) {
                int gid = checkGet(entries, idx, SEInteger.class).get();
                checkGidRange(gid, () -> "illegal glyph index: " + gid);

                if (gid > gidMax) {
                    map [ i++ ] = gidMax = gid;
                } else {
                    log.info("ignoring out of order or duplicate glyph index: " + gid);
                    skipped++;
                }
            }

            assert (i + skipped) == n;
            assert this.map == null;
            this.map = map;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return Arrays.stream(map)
              .mapToObj(Integer::toString)
              .collect(Collectors.joining(",", "{", "}"));
        }
    }

    private static class RangeCoverageTable extends GlyphMappingTable.RangeMappingTable implements GlyphCoverageMapping {
        public RangeCoverageTable(List<SubtableEntry> entries) {
            super(entries);
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid, int s, int m) {
            return m + gid - s;
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageSize() {
            return getMappingSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getCoverageIndex(int gid) {
            return getMappedIndex(gid);
        }
    }

}
