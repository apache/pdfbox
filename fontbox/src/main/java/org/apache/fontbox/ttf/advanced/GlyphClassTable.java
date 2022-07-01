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


import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEGlyphCoverageTable;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEInteger;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SEMappingRange;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SubtableEntry;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>Base class implementation of glyph class table.</p>
 *
 * @author Glenn Adams
 */
public final class GlyphClassTable extends GlyphMappingTable implements GlyphClassMapping {

    /** empty mapping table */
    public static final int GLYPH_CLASS_TYPE_EMPTY = GLYPH_MAPPING_TYPE_EMPTY;

    /** mapped mapping table */
    public static final int GLYPH_CLASS_TYPE_MAPPED = GLYPH_MAPPING_TYPE_MAPPED;

    /** range based mapping table */
    public static final int GLYPH_CLASS_TYPE_RANGE = GLYPH_MAPPING_TYPE_RANGE;

    /** empty mapping table */
    public static final int GLYPH_CLASS_TYPE_COVERAGE_SET = 3;

    private GlyphClassMapping cm;

    private GlyphClassTable(GlyphClassMapping cm) {
        assert cm != null;
        assert cm instanceof GlyphMappingTable;
        this.cm = cm;
    }

    /** {@inheritDoc} */
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
    public int getClassSize(int set) {
        return cm.getClassSize(set);
    }

    /** {@inheritDoc} */
    @Override
    public int getClassIndex(int gid, int set) {
        return cm.getClassIndex(gid, set);
    }

    /**
     * Create glyph class table.
     * @param entries list of mapped or ranged class entries, or null or empty list
     * @return a new covera table instance
     */
    public static GlyphClassTable createClassTable(List<SubtableEntry> entries) {
        GlyphClassMapping cm;
        if ((entries == null) || (entries.size() == 0)) {
            cm = new EmptyClassTable(entries);
        } else if (isMappedClass(entries)) {
            cm = new MappedClassTable(entries);
        } else if (isRangeClass(entries)) {
            cm = new RangeClassTable(entries);
        } else if (isCoverageSetClass(entries)) {
            cm = new CoverageSetClassTable(entries);
        } else {
            cm = null;
        }
        assert cm != null : "unknown class type";
        return new GlyphClassTable(cm);
    }

    private static boolean isMappedClass(List<SubtableEntry> entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            return allOfType(entries, SEInteger.class);
        }
    }

    private static boolean isRangeClass(List<SubtableEntry> entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            return allOfType(entries, SEMappingRange.class);
        }
    }

    private static boolean isCoverageSetClass(List<SubtableEntry> entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            return allOfType(entries, SEGlyphCoverageTable.class);
        }
    }

    private static class EmptyClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
        public EmptyClassTable(List<SubtableEntry> entries) {
            super(entries);
        }

        /** {@inheritDoc} */
        @Override
        public int getClassSize(int set) {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public int getClassIndex(int gid, int set) {
            return -1;
        }
    }

    private static class MappedClassTable extends GlyphMappingTable.MappedMappingTable implements GlyphClassMapping {
        private int firstGlyph;
        private int[] gca;
        private int gcMax = -1;
        public MappedClassTable(List<SubtableEntry> entries) {
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            List<SubtableEntry> entries = new java.util.ArrayList<>();
            entries.add(SEInteger.valueOf(firstGlyph));
            if (gca != null) {
                for (int i = 0, n = gca.length; i < n; i++) {
                    entries.add(SEInteger.valueOf(gca [ i ]));
                }
            }
            return entries;
        }

        /** {@inheritDoc} */
        @Override
        public int getMappingSize() {
            return gcMax + 1;
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid) {
            int i = gid - firstGlyph;
            if ((i >= 0) && (i < gca.length)) {
                return gca [ i ];
            } else {
                return -1;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getClassSize(int set) {
            return getMappingSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getClassIndex(int gid, int set) {
            return getMappedIndex(gid);
        }

        private void populate(List<SubtableEntry> entries) {
            if (entries == null || entries.isEmpty()) {
                throw new AdvancedTypographicTableFormatException("Mapped class table must contain at least one glyph");
            }

            int i = 0;
            int n = entries.size() - 1;
            int gcMax = -1;
            int[] gca = new int [ n ];
            int firstGlyph = 0;

            for (int idx = 0; idx < entries.size(); idx++) {
                if (idx == 0) {
                    firstGlyph = checkGet(entries, 0, SEInteger.class).get();
                } else {
                    // extract glyph class array
                    int gc = checkGet(entries, idx, SEInteger.class).get();
                    gca [ i++ ] = gc;
                    if (gc > gcMax) {
                        gcMax = gc;
                    }
                }
            }

            assert i == n;
            assert this.gca == null;
            this.firstGlyph = firstGlyph;
            this.gca = gca;
            this.gcMax = gcMax;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            String prefix = "{ firstGlyph = " + firstGlyph + ", classes = {";
            return Arrays.stream(gca)
              .mapToObj(Integer::toString)
              .collect(Collectors.joining(",", prefix, "} }"));
        }
    }

    private static class RangeClassTable extends GlyphMappingTable.RangeMappingTable implements GlyphClassMapping {
        public RangeClassTable(List<SubtableEntry> entries) {
            super(entries);
        }

        /** {@inheritDoc} */
        @Override
        public int getMappedIndex(int gid, int s, int m) {
            return m;
        }

        /** {@inheritDoc} */
        @Override
        public int getClassSize(int set) {
            return getMappingSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getClassIndex(int gid, int set) {
            return getMappedIndex(gid);
        }
    }

    private static class CoverageSetClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
        private static final Log LOG = LogFactory.getLog(CoverageSetClassTable.class);
        public CoverageSetClassTable(List<SubtableEntry> entries) {
            // See FOP-2704
            // throw new UnsupportedOperationException("coverage set class table not yet supported");
            LOG.warn("coverage set class table not yet supported");
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GLYPH_CLASS_TYPE_COVERAGE_SET;
        }

        /** {@inheritDoc} */
        @Override
        public int getClassSize(int set) {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public int getClassIndex(int gid, int set) {
            return -1;
        }
    }

}
