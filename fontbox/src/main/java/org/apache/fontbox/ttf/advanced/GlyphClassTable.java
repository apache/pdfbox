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

import java.util.Iterator;
import java.util.List;

/**
 * <p>Base class implementation of glyph class table.</p>
 *
 * @author Glenn Adams
 */
@SuppressWarnings("unchecked") 
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
    public List getEntries() {
        return ((GlyphMappingTable) cm) .getEntries();
    }

    /** {@inheritDoc} */
    public int getClassSize(int set) {
        return cm.getClassSize(set);
    }

    /** {@inheritDoc} */
    public int getClassIndex(int gid, int set) {
        return cm.getClassIndex(gid, set);
    }

    /**
     * Create glyph class table.
     * @param entries list of mapped or ranged class entries, or null or empty list
     * @return a new covera table instance
     */
    public static GlyphClassTable createClassTable(List entries) {
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

    private static boolean isMappedClass(List entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            for (Iterator it = entries.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!(o instanceof Integer)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isRangeClass(List entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            for (Iterator it = entries.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!(o instanceof MappingRange)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isCoverageSetClass(List entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            for (Iterator it = entries.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!(o instanceof GlyphCoverageTable)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class EmptyClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
        public EmptyClassTable(List entries) {
            super(entries);
        }
        /** {@inheritDoc} */
        public int getClassSize(int set) {
            return 0;
        }
        /** {@inheritDoc} */
        public int getClassIndex(int gid, int set) {
            return -1;
        }
    }

    private static class MappedClassTable extends GlyphMappingTable.MappedMappingTable implements GlyphClassMapping {
        private int firstGlyph;
        private int[] gca;
        private int gcMax = -1;
        public MappedClassTable(List entries) {
            populate(entries);
        }
        /** {@inheritDoc} */
        public List getEntries() {
            List entries = new java.util.ArrayList();
            entries.add(Integer.valueOf(firstGlyph));
            if (gca != null) {
                for (int i = 0, n = gca.length; i < n; i++) {
                    entries.add(Integer.valueOf(gca [ i ]));
                }
            }
            return entries;
        }
        /** {@inheritDoc} */
        public int getMappingSize() {
            return gcMax + 1;
        }
        /** {@inheritDoc} */
        public int getMappedIndex(int gid) {
            int i = gid - firstGlyph;
            if ((i >= 0) && (i < gca.length)) {
                return gca [ i ];
            } else {
                return -1;
            }
        }
        /** {@inheritDoc} */
        public int getClassSize(int set) {
            return getMappingSize();
        }
        /** {@inheritDoc} */
        public int getClassIndex(int gid, int set) {
            return getMappedIndex(gid);
        }
        private void populate(List entries) {
            // obtain entries iterator
            Iterator it = entries.iterator();
            // extract first glyph
            int firstGlyph = 0;
            if (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Integer) {
                    firstGlyph = ((Integer) o) .intValue();
                } else {
                    throw new AdvancedTypographicTableFormatException("illegal entry, first entry must be Integer denoting first glyph value, but is: " + o);
                }
            }
            // extract glyph class array
            int i = 0;
            int n = entries.size() - 1;
            int gcMax = -1;
            int[] gca = new int [ n ];
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Integer) {
                    int gc = ((Integer) o) .intValue();
                    gca [ i++ ] = gc;
                    if (gc > gcMax) {
                        gcMax = gc;
                    }
                } else {
                    throw new AdvancedTypographicTableFormatException("illegal mapping entry, must be Integer: " + o);
                }
            }
            assert i == n;
            assert this.gca == null;
            this.firstGlyph = firstGlyph;
            this.gca = gca;
            this.gcMax = gcMax;
        }
        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{ firstGlyph = " + firstGlyph + ", classes = {");
            for (int i = 0, n = gca.length; i < n; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(Integer.toString(gca [ i ]));
            }
            sb.append("} }");
            return sb.toString();
        }
    }

    private static class RangeClassTable extends GlyphMappingTable.RangeMappingTable implements GlyphClassMapping {
        public RangeClassTable(List entries) {
            super(entries);
        }
        /** {@inheritDoc} */
        public int getMappedIndex(int gid, int s, int m) {
            return m;
        }
        /** {@inheritDoc} */
        public int getClassSize(int set) {
            return getMappingSize();
        }
        /** {@inheritDoc} */
        public int getClassIndex(int gid, int set) {
            return getMappedIndex(gid);
        }
    }

    private static class CoverageSetClassTable extends GlyphMappingTable.EmptyMappingTable implements GlyphClassMapping {
        public CoverageSetClassTable(List entries) {
            throw new UnsupportedOperationException("coverage set class table not yet supported");
        }
        /** {@inheritDoc} */
        public int getType() {
            return GLYPH_CLASS_TYPE_COVERAGE_SET;
        }
        /** {@inheritDoc} */
        public int getClassSize(int set) {
            return 0;
        }
        /** {@inheritDoc} */
        public int getClassIndex(int gid, int set) {
            return -1;
        }
    }

}
