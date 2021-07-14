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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * <p>The <code>GlyphSubtable</code> implements an abstract glyph subtable that
 * encapsulates identification, type, format, and coverage information.</p>
 *
 * @author Glenn Adams
 */
@SuppressWarnings("unchecked") 
public abstract class GlyphSubtable implements Comparable {

    /** lookup flag - right to left */
    public static final int LF_RIGHT_TO_LEFT = 0x0001;
    /** lookup flag - ignore base glyphs */
    public static final int LF_IGNORE_BASE = 0x0002;
    /** lookup flag - ignore ligatures */
    public static final int LF_IGNORE_LIGATURE = 0x0004;
    /** lookup flag - ignore marks */
    public static final int LF_IGNORE_MARK = 0x0008;
    /** lookup flag - use mark filtering set */
    public static final int LF_USE_MARK_FILTERING_SET = 0x0010;
    /** lookup flag - reserved */
    public static final int LF_RESERVED = 0x0E00;
    /** lookup flag - mark attachment type */
    public static final int LF_MARK_ATTACHMENT_TYPE = 0xFF00;
    /** internal flag - use reverse scan */
    public static final int LF_INTERNAL_USE_REVERSE_SCAN = 0x10000;

    /** lookup identifier, having form of "lu%d" where %d is index of lookup in lookup list; shared by multiple subtables in a single lookup  */
    private String lookupId;
    /** subtable sequence (index) number in lookup, zero based */
    private int sequence;
    /** subtable flags */
    private int flags;
    /** subtable format */
    private int format;
    /** subtable mapping table */
    private GlyphMappingTable mapping;
    /** weak reference to parent (gsub or gpos) table */
    private WeakReference table;

    /**
     * Instantiate this glyph subtable.
     * @param lookupId lookup identifier, having form of "lu%d" where %d is index of lookup in lookup list
     * @param sequence subtable sequence (within lookup), starting with zero
     * @param flags subtable flags
     * @param format subtable format
     * @param mapping subtable mapping table
     */
    protected GlyphSubtable(String lookupId, int sequence, int flags, int format, GlyphMappingTable mapping)
    {
        if ((lookupId == null) || (lookupId.length() == 0)) {
            throw new AdvancedTypographicTableFormatException("invalid lookup identifier, must be non-empty string");
        } else if (mapping == null) {
            throw new AdvancedTypographicTableFormatException("invalid mapping table, must not be null");
        } else {
            this.lookupId = lookupId;
            this.sequence = sequence;
            this.flags = flags;
            this.format = format;
            this.mapping = mapping;
        }
    }

    /** @return this subtable's lookup identifer */
    public String getLookupId() {
        return lookupId;
    }

    /** @return this subtable's table type */
    public abstract int getTableType();

    /** @return this subtable's type */
    public abstract int getType();

    /** @return this subtable's type name */
    public abstract String getTypeName();

    /**
     * Determine if a glyph subtable is compatible with this glyph subtable. Two glyph subtables are
     * compatible if the both may appear in a single lookup table.
     * @param subtable a glyph subtable to determine compatibility
     * @return true if specified subtable is compatible with this glyph subtable, where by compatible
     * is meant that they share the same lookup type
     */
    public abstract boolean isCompatible(GlyphSubtable subtable);

    /** @return true if subtable uses reverse scanning of glyph sequence, meaning from the last glyph
     * in a glyph sequence to the first glyph
     */
    public abstract boolean usesReverseScan();

    /** @return this subtable's sequence (index) within lookup */
    public int getSequence() {
        return sequence;
    }

    /** @return this subtable's flags */
    public int getFlags() {
        return flags;
    }

    /** @return this subtable's format */
    public int getFormat() {
        return format;
    }

    /** @return this subtable's governing glyph definition table or null if none available */
    public GlyphDefinitionTable getGDEF() {
        AdvancedTypographicTable gt = getTable();
        if (gt != null) {
            return gt.getGlyphDefinitions();
        } else {
            return null;
        }
    }

    /** @return this subtable's coverage mapping or null if mapping is not a coverage mapping */
    public GlyphCoverageMapping getCoverage() {
        if (mapping instanceof GlyphCoverageMapping) {
            return (GlyphCoverageMapping) mapping;
        } else {
            return null;
        }
    }

    /** @return this subtable's class mapping or null if mapping is not a class mapping */
    public GlyphClassMapping getClasses() {
        if (mapping instanceof GlyphClassMapping) {
            return (GlyphClassMapping) mapping;
        } else {
            return null;
        }
    }

    /** @return this subtable's lookup entries */
    public abstract List getEntries();

    /** @return this subtable's parent table (or null if undefined) */
    public synchronized AdvancedTypographicTable getTable() {
        WeakReference r = this.table;
        return (r != null) ? (AdvancedTypographicTable) r.get() : null;
    }

    /**
     * Establish a weak reference from this subtable to its parent
     * table. If table parameter is specified as <code>null</code>, then
     * clear and remove weak reference.
     * @param table the table or null
     * @throws IllegalStateException if table is already set to non-null
     */
    public synchronized void setTable(AdvancedTypographicTable table) throws IllegalStateException {
        WeakReference r = this.table;
        if (table == null) {
            this.table = null;
            if (r != null) {
                r.clear();
            }
        } else if (r == null) {
            this.table = new WeakReference(table);
        } else {
            throw new IllegalStateException("table already set");
        }
    }

    /**
     * Resolve references to lookup tables, e.g., in RuleLookup, to the lookup tables themselves.
     * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
     */
    public void resolveLookupReferences(Map/*<String,AdvancedTypographicTable.LookupTable>*/ lookupTables) {
    }

    /**
     * Map glyph id to coverage index.
     * @param gid glyph id
     * @return the corresponding coverage index of the specified glyph id
     */
    public int getCoverageIndex(int gid) {
        if (mapping instanceof GlyphCoverageMapping) {
            return ((GlyphCoverageMapping) mapping) .getCoverageIndex(gid);
        } else {
            return -1;
        }
    }

    /**
     * Map glyph id to coverage index.
     * @return the corresponding coverage index of the specified glyph id
     */
    public int getCoverageSize() {
        if (mapping instanceof GlyphCoverageMapping) {
            return ((GlyphCoverageMapping) mapping) .getCoverageSize();
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int hc = sequence;
        hc = (hc * 3) + (lookupId.hashCode() ^ hc);
        return hc;
    }

    /**
     * {@inheritDoc}
     * @return true if the lookup identifier and the sequence number of the specified subtable is the same
     * as the lookup identifier and sequence number of this subtable
     */
    public boolean equals(Object o) {
        if (o instanceof GlyphSubtable) {
            GlyphSubtable st = (GlyphSubtable) o;
            return lookupId.equals(st.lookupId) && (sequence == st.sequence);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * @return the result of comparing the lookup identifier and the sequence number of the specified subtable with
     * the lookup identifier and sequence number of this subtable
     */
    public int compareTo(Object o) {
        int d;
        if (o instanceof GlyphSubtable) {
            GlyphSubtable st = (GlyphSubtable) o;
            if ((d = lookupId.compareTo(st.lookupId)) == 0) {
                if (sequence < st.sequence) {
                    d = -1;
                } else if (sequence > st.sequence) {
                    d = 1;
                }
            }
        } else {
            d = -1;
        }
        return d;
    }

    /**
     * Determine if any of the specified subtables uses reverse scanning.
     * @param subtables array of glyph subtables
     * @return true if any of the specified subtables uses reverse scanning.
     */
    public static boolean usesReverseScan(GlyphSubtable[] subtables) {
        if ((subtables == null) || (subtables.length == 0)) {
            return false;
        } else {
            for (int i = 0, n = subtables.length; i < n; i++) {
                if (subtables[i].usesReverseScan()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Determine consistent flags for a set of subtables.
     * @param subtables array of glyph subtables
     * @return consistent flags
     * @throws IllegalStateException if inconsistent flags
     */
    public static int getFlags(GlyphSubtable[] subtables) throws IllegalStateException {
        if ((subtables == null) || (subtables.length == 0)) {
            return 0;
        } else {
            int flags = 0;
            // obtain first non-zero value of flags in array of subtables
            for (int i = 0, n = subtables.length; i < n; i++) {
                int f = subtables[i].getFlags();
                if (flags == 0) {
                    flags = f;
                    break;
                }
            }
            // enforce flag consistency
            for (int i = 0, n = subtables.length; i < n; i++) {
                int f = subtables[i].getFlags();
                if (f != flags) {
                    throw new IllegalStateException("inconsistent lookup flags " + f + ", expected " + flags);
                }
            }
            return flags | (usesReverseScan(subtables) ? LF_INTERNAL_USE_REVERSE_SCAN : 0);
        }
    }

}
