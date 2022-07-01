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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TTFDataStream;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.*;
import org.apache.fontbox.ttf.advanced.api.AdvancedOpenTypeFont;
import org.apache.fontbox.ttf.advanced.scripts.ScriptProcessor;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.GlyphTester;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>The <code>GlyphPositioningTable</code> class is a glyph table that implements
 * <code>GlyphPositioning</code> functionality.</p>
 *
 * <p>Adapted from the Apache FOP Project.</p>
 *
 * @author Glenn Adams
 */
public class GlyphPositioningTable extends AdvancedTypographicTable {

    /** logging instance */
    private static final Log log = LogFactory.getLog(GlyphPositioningTable.class);

    /** tag that identifies this table type */
    public static final String TAG = "GPOS";

    /** single positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_SINGLE = 1;
    /** multiple positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_PAIR = 2;
    /** cursive positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CURSIVE = 3;
    /** mark to base positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_BASE = 4;
    /** mark to ligature positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE = 5;
    /** mark to mark positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_MARK_TO_MARK = 6;
    /** contextual positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CONTEXTUAL = 7;
    /** chained contextual positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL = 8;
    /** extension positioning subtable type */
    public static final int GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING = 9;

    public GlyphPositioningTable(OpenTypeFont otf) {
        super(otf, null, new java.util.HashMap<>(0));
    }

    /**
     * Initialize this <code>GlyphPositioningTable</code> object using the specified lookups
     * and subtables.
     * @param gdef glyph definition table that applies
     * @param lookups a map of lookup specifications to subtable identifier strings
     * @param subtables a list of identified subtables
     */
    public GlyphPositioningTable initialize(GlyphDefinitionTable gdef, Map<LookupSpec, List<String>> lookups, List<GlyphSubtable> subtables) {
        setGdef(gdef);
        initialize(lookups);
        if ((subtables == null) || (subtables.size() == 0)) {
            throw new AdvancedTypographicTableFormatException("subtables must be non-empty");
        } else {
            for (GlyphSubtable o : subtables) {
                if (o instanceof GlyphPositioningSubtable) {
                    addSubtable(o);
                } else {
                    throw new AdvancedTypographicTableFormatException("subtable must be a glyph positioning subtable");
                }
            }
            freezeSubtables();
            return this;
        }
    }

    @Override
    protected void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        if (ttf instanceof AdvancedOpenTypeFont) {
            new AdvancedTypographicTableReader((AdvancedOpenTypeFont) ttf, this, data).read();
            this.initialized = true;
        }
    }

    /**
     * Map a lookup type name to its constant (integer) value.
     * @param name lookup type name
     * @return lookup type
     */
    public static int getLookupTypeFromName(String name) {
        int t;
        String s = name.toLowerCase();
        if ("single".equals(s)) {
            t = GPOS_LOOKUP_TYPE_SINGLE;
        } else if ("pair".equals(s)) {
            t = GPOS_LOOKUP_TYPE_PAIR;
        } else if ("cursive".equals(s)) {
            t = GPOS_LOOKUP_TYPE_CURSIVE;
        } else if ("marktobase".equals(s)) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_BASE;
        } else if ("marktoligature".equals(s)) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE;
        } else if ("marktomark".equals(s)) {
            t = GPOS_LOOKUP_TYPE_MARK_TO_MARK;
        } else if ("contextual".equals(s)) {
            t = GPOS_LOOKUP_TYPE_CONTEXTUAL;
        } else if ("chainedcontextual".equals(s)) {
            t = GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
        } else if ("extensionpositioning".equals(s)) {
            t = GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING;
        } else {
            t = -1;
        }
        return t;
    }

    /**
     * Map a lookup type constant (integer) value to its name.
     * @param type lookup type
     * @return lookup type name
     */
    public static String getLookupTypeName(int type) {
        String tn;
        switch (type) {
        case GPOS_LOOKUP_TYPE_SINGLE:
            tn = "single";
            break;
        case GPOS_LOOKUP_TYPE_PAIR:
            tn = "pair";
            break;
        case GPOS_LOOKUP_TYPE_CURSIVE:
            tn = "cursive";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_BASE:
            tn = "marktobase";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE:
            tn = "marktoligature";
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_MARK:
            tn = "marktomark";
            break;
        case GPOS_LOOKUP_TYPE_CONTEXTUAL:
            tn = "contextual";
            break;
        case GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL:
            tn = "chainedcontextual";
            break;
        case GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING:
            tn = "extensionpositioning";
            break;
        default:
            tn = "unknown";
            break;
        }
        return tn;
    }

    /**
     * Create a positioning subtable according to the specified arguments.
     * @param type subtable type
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     * @param entries subtable entries
     * @return a glyph subtable instance
     */
    public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
        GlyphSubtable st = null;
        switch (type) {
        case GPOS_LOOKUP_TYPE_SINGLE:
            st = SingleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_PAIR:
            st = PairSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_CURSIVE:
            st = CursiveSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_BASE:
            st = MarkToBaseSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE:
            st = MarkToLigatureSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_MARK_TO_MARK:
            st = MarkToMarkSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_CONTEXTUAL:
            st = ContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL:
            st = ChainedContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        default:
            break;
        }
        return st;
    }

    /**
     * Create a positioning subtable according to the specified arguments.
     * @param type subtable type
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage list of coverage table entries
     * @param entries subtable entries
     * @return a glyph subtable instance
     */
    public static GlyphSubtable createSubtable(int type, String id, int sequence, int flags, int format, List<SubtableEntry> coverage, List<SubtableEntry> entries) {
        return createSubtable(type, id, sequence, flags, format, GlyphCoverageTable.createCoverageTable(coverage), entries);
    }

    /**
     * Perform positioning processing using all matching lookups.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param fontSize size in device units
     * @param widths array of default advancements for each glyph
     * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
     * with one 4-tuple for each element of glyph sequence
     * @return true if some adjustment is not zero; otherwise, false
     */
    public boolean position(GlyphSequence gs, String script, String language, Object[][] features, int fontSize, int[] widths, int[][] adjustments) {
        Map<LookupSpec, List<LookupTable>> lookups = matchLookups(script, language, "*");
        if ((lookups != null) && (lookups.size() > 0)) {
            ScriptProcessor sp = ScriptProcessor.getInstance(script);
            return sp.position(this, gs, script, language, features, fontSize, lookups, widths, adjustments);
        } else {
            return false;
        }
    }

    private abstract static class SingleSubtable extends GlyphPositioningSubtable {
        SingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_SINGLE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof SingleSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            int gi = ps.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                Value v = getValue(ci, gi);
                if (v != null) {
                    if (ps.adjust(v)) {
                        ps.setAdjusted(true);
                    }
                    ps.consume(1);
                }
                return true;
            }
        }

        /**
         * Obtain positioning value for coverage index.
         * @param ci coverage index
         * @param gi input glyph index
         * @return positioning value or null if none applies
         */
        public abstract Value getValue(int ci, int gi);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new SingleSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else if (format == 2) {
                return new SingleSubtableFormat2(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class SingleSubtableFormat1 extends SingleSubtable {
        private Value value;
        private int ciMax;

        SingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (value != null) {
                List<SubtableEntry> entries = new ArrayList<>(1);
                entries.add(new SEValue(value));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public Value getValue(int ci, int gi) {
            if ((value != null) && (ci <= ciMax)) {
                return value;
            } else {
                return null;
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            this.value = checkGet(entries, 0, SEValue.class).get();
            this.ciMax = getCoverageSize() - 1;
        }
    }

    private static class SingleSubtableFormat2 extends SingleSubtable {
        private Value[] values;
        SingleSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (values != null) {
                return arrayMap(values, val -> new SEValue(val));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public Value getValue(int ci, int gi) {
            if ((values != null) && (ci < values.length)) {
                return values [ ci ];
            } else {
                return null;
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            Value[] va = checkGet(entries, 0, SEValueList.class).get();
            if (va.length != getCoverageSize()) {
                throw new AdvancedTypographicTableFormatException("illegal values array, " + entries.size() + " values present, but requires " + getCoverageSize() + " values");
            } else {
                assert this.values == null;
                this.values = va;
            }
        }
    }

    private abstract static class PairSubtable extends GlyphPositioningSubtable {
        PairSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_PAIR;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof PairSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int gi = ps.getGlyph(0);
            int ci;
            if ((ci = getCoverageIndex(gi)) >= 0) {
                int[] counts = ps.getGlyphsAvailable(0);
                int nga = counts[0];
                if (nga > 1) {
                    int[] iga = ps.getGlyphs(0, 2, null, counts);
                    if ((iga != null) && (iga.length == 2)) {
                        PairValues pv = getPairValues(ci, iga[0], iga[1]);
                        if (pv != null) {
                            int offset = 0;
                            int offsetLast = counts[0] + counts[1];
                            // skip any ignored glyphs prior to first non-ignored glyph
                            for ( ; offset < offsetLast; ++offset) {
                                if (!ps.isIgnoredGlyph(offset)) {
                                    break;
                                } else {
                                    ps.consume(1);
                                }
                            }
                            // adjust first non-ignored glyph if first value isn't null
                            Value v1 = pv.getValue1();
                            if (v1 != null) {
                                if (ps.adjust(v1, offset)) {
                                    ps.setAdjusted(true);
                                }
                                ps.consume(1);          // consume first non-ignored glyph
                                ++offset;
                            }
                            // skip any ignored glyphs prior to second non-ignored glyph
                            for ( ; offset < offsetLast; ++offset) {
                                if (!ps.isIgnoredGlyph(offset)) {
                                    break;
                                } else {
                                    ps.consume(1);
                                }
                            }
                            // adjust second non-ignored glyph if second value isn't null
                            Value v2 = pv.getValue2();
                            if (v2 != null) {
                                if (ps.adjust(v2, offset)) {
                                    ps.setAdjusted(true);
                                }
                                ps.consume(1);          // consume second non-ignored glyph
                                ++offset;
                            }
                            applied = true;
                        }
                    }
                }
            }
            return applied;
        }

        /**
         * Obtain associated pair values.
         * @param ci coverage index
         * @param gi1 first input glyph index
         * @param gi2 second input glyph index
         * @return pair values or null if none applies
         */
        public abstract PairValues getPairValues(int ci, int gi1, int gi2);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new PairSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else if (format == 2) {
                return new PairSubtableFormat2(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class PairSubtableFormat1 extends PairSubtable {
        private PairValues[][] pvm;                     // pair values matrix
        PairSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (pvm != null) {
                return mutableSingleton(new SEPairValueMatrix(pvm));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public PairValues getPairValues(int ci, int gi1, int gi2) {
            if ((pvm != null) && (ci < pvm.length)) {
                PairValues[] pvt = pvm [ ci ];
                for (int i = 0, n = pvt.length; i < n; i++) {
                    PairValues pv = pvt [ i ];
                    if (pv != null) {
                        int g = pv.getGlyph();
                        if (g < gi2) {
                            continue;
                        } else if (g == gi2) {
                            return pv;
                        } else {
                            break;
                        }
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            pvm = checkGet(entries, 0, SEPairValueMatrix.class).get();
        }
    }

    private static class PairSubtableFormat2 extends PairSubtable {
        private GlyphClassTable cdt1;                   // class def table 1
        private GlyphClassTable cdt2;                   // class def table 2
        private int nc1;                                // class 1 count
        private int nc2;                                // class 2 count
        private PairValues[][] pvm;                     // pair values matrix
        PairSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (pvm != null) {
                List<SubtableEntry> entries = new ArrayList<>(5);
                entries.add(new SEGlyphClassTable(cdt1));
                entries.add(new SEGlyphClassTable(cdt2));
                entries.add(SEInteger.valueOf(nc1));
                entries.add(SEInteger.valueOf(nc2));
                entries.add(new SEPairValueMatrix(pvm));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public PairValues getPairValues(int ci, int gi1, int gi2) {
            if (pvm != null) {
                int c1 = cdt1.getClassIndex(gi1, 0);
                if ((c1 >= 0) && (c1 < nc1) && (c1 < pvm.length)) {
                    PairValues[] pvt = pvm [ c1 ];
                    if (pvt != null) {
                        int c2 = cdt2.getClassIndex(gi2, 0);
                        if ((c2 >= 0) && (c2 < nc2) && (c2 < pvt.length)) {
                            return pvt [ c2 ];
                        }
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 5);
            cdt1 = checkGet(entries, 0, SEGlyphClassTable.class).get();
            cdt2 = checkGet(entries, 1, SEGlyphClassTable.class).get();
            nc1 = checkGet(entries, 2, SEInteger.class).get();
            nc2 = checkGet(entries, 3, SEInteger.class).get();
            pvm = checkGet(entries, 4, SEPairValueMatrix.class).get();
        }
    }

    private abstract static class CursiveSubtable extends GlyphPositioningSubtable {
        CursiveSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_CURSIVE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof CursiveSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int gi = ps.getGlyph(0);
            int ci;
            if ((ci = getCoverageIndex(gi)) >= 0) {
                int[] counts = ps.getGlyphsAvailable(0);
                int nga = counts[0];
                if (nga > 1) {
                    int[] iga = ps.getGlyphs(0, 2, null, counts);
                    if ((iga != null) && (iga.length == 2)) {
                        // int gi1 = gi;
                        int ci1 = ci;
                        int gi2 = iga [ 1 ];
                        int ci2 = getCoverageIndex(gi2);
                        Anchor[] aa = getExitEntryAnchors(ci1, ci2);
                        if (aa != null) {
                            Anchor exa = aa [ 0 ];
                            Anchor ena = aa [ 1 ];
                            // int exw = ps.getWidth ( gi1 );
                            int enw = ps.getWidth(gi2);
                            if ((exa != null) && (ena != null)) {
                                Value v = ena.getAlignmentAdjustment(exa);
                                v.adjust(-enw, 0, 0, 0);
                                if (ps.adjust(v)) {
                                    ps.setAdjusted(true);
                                }
                            }
                            // consume only first glyph of exit/entry glyph pair
                            ps.consume(1);
                            applied = true;
                        }
                    }
                }
            }
            return applied;
        }

        /**
         * Obtain exit anchor for first glyph with coverage index <code>ci1</code> and entry anchor for second
         * glyph with coverage index <code>ci2</code>.
         * @param ci1 coverage index of first glyph (may be negative)
         * @param ci2 coverage index of second glyph (may be negative)
         * @return array of two anchors or null if either coverage index is negative or corresponding anchor is
         * missing, where the first entry is the exit anchor of the first glyph and the second entry is the
         * entry anchor of the second glyph
         */
        public abstract Anchor[] getExitEntryAnchors(int ci1, int ci2);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new CursiveSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class CursiveSubtableFormat1 extends CursiveSubtable {
        private Anchor[] aa;                            // anchor array, where even entries are entry anchors, and odd entries are exit anchors
        CursiveSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (aa != null) {
                return mutableSingleton(new SEAnchorList(aa));
            } else {
                return null;
            }
        }
        /** {@inheritDoc} */
        @Override
        public Anchor[] getExitEntryAnchors(int ci1, int ci2) {
            if ((ci1 >= 0) && (ci2 >= 0)) {
                int ai1 = (ci1 * 2) + 1; // ci1 denotes glyph with exit anchor
                int ai2 = (ci2 * 2) + 0; // ci2 denotes glyph with entry anchor
                if ((aa != null) && (ai1 < aa.length) && (ai2 < aa.length)) {
                    Anchor exa = aa [ ai1 ];
                    Anchor ena = aa [ ai2 ];
                    if ((exa != null) && (ena != null)) {
                        return new Anchor[] { exa, ena };
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            Anchor[] o = checkGet(entries, 0, SEAnchorList.class).get();
            
            if ((o.length % 2) != 0) {
                throw new AdvancedTypographicTableFormatException("illegal entries, Anchor[] array must have an even number of entries, but has: " + o.length);
            } else {
                aa = o;
            }
        }
    }

    private abstract static class MarkToBaseSubtable extends GlyphPositioningSubtable {
        MarkToBaseSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_MARK_TO_BASE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof MarkToBaseSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int giMark = ps.getGlyph();
            int ciMark;
            if ((ciMark = getCoverageIndex(giMark)) >= 0) {
                MarkAnchor ma = getMarkAnchor(ciMark, giMark);
                if (ma != null) {
                    for (int i = 0, n = ps.getPosition(); i < n; i++) {
                        int gi = ps.getGlyph(-(i + 1));
                        if (ps.isMark(gi)) {
                            continue;
                        } else {
                            Anchor a = getBaseAnchor(gi, ma.getMarkClass());
                            if (a != null) {
                                Value v = a.getAlignmentAdjustment(ma);
                                // start experimental fix for END OF AYAH in Lateef/Scheherazade
                                int[] aa = ps.getAdjustment();
                                if (aa[2] == 0) {
                                    v.adjust(0, 0, -ps.getWidth(giMark), 0);
                                }
                                // end experimental fix for END OF AYAH in Lateef/Scheherazade
                                if (ps.adjust(v)) {
                                    ps.setAdjusted(true);
                                }
                            }
                            ps.consume(1);
                            applied = true;
                            break;
                        }
                    }
                }
            }
            return applied;
        }

        /**
         * Obtain mark anchor associated with mark coverage index.
         * @param ciMark coverage index
         * @param giMark input glyph index of mark glyph
         * @return mark anchor or null if none applies
         */
        public abstract MarkAnchor getMarkAnchor(int ciMark, int giMark);

        /**
         * Obtain anchor associated with base glyph index and mark class.
         * @param giBase input glyph index of base glyph
         * @param markClass class number of mark glyph
         * @return anchor or null if none applies
         */
        public abstract Anchor getBaseAnchor(int giBase, int markClass);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new MarkToBaseSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class MarkToBaseSubtableFormat1 extends MarkToBaseSubtable {
        private GlyphCoverageTable bct;                 // base coverage table
        private int nmc;                                // mark class count
        private MarkAnchor[] maa;                       // mark anchor array, ordered by mark coverage index
        private Anchor[][] bam;                         // base anchor matrix, ordered by base coverage index, then by mark class
        MarkToBaseSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if ((bct != null) && (maa != null) && (nmc > 0) && (bam != null)) {
                return new ArrayList<>(Arrays.asList(
                    new SEGlyphCoverageTable(bct),
                    SEInteger.valueOf(nmc),
                    new SEMarkAnchorList(maa),
                    new SEAnchorMatrix(bam)));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public MarkAnchor getMarkAnchor(int ciMark, int giMark) {
            if ((maa != null) && (ciMark < maa.length)) {
                return maa [ ciMark ];
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public Anchor getBaseAnchor(int giBase, int markClass) {
            int ciBase;
            if ((bct != null) && ((ciBase = bct.getCoverageIndex(giBase)) >= 0)) {
                if ((bam != null) && (ciBase < bam.length)) {
                    Anchor[] ba = bam [ ciBase ];
                    if ((ba != null) && (markClass < ba.length)) {
                        return ba [ markClass ];
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 4);
            bct = checkGet(entries, 0, SEGlyphCoverageTable.class).get();
            nmc = checkGet(entries, 1, SEInteger.class).get();
            maa = checkGet(entries, 2, SEMarkAnchorList.class).get();
            bam = checkGet(entries, 3, SEAnchorMatrix.class).get();
        }
    }

    private abstract static class MarkToLigatureSubtable extends GlyphPositioningSubtable {
        MarkToLigatureSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof MarkToLigatureSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int giMark = ps.getGlyph();
            int ciMark;
            if ((ciMark = getCoverageIndex(giMark)) >= 0) {
                MarkAnchor ma = getMarkAnchor(ciMark, giMark);
                int mxc = getMaxComponentCount();
                if (ma != null) {
                    for (int i = 0, n = ps.getPosition(); i < n; i++) {
                        int gi = ps.getGlyph(-(i + 1));
                        if (ps.isMark(gi)) {
                            continue;
                        } else {
                            Anchor a = getLigatureAnchor(gi, mxc, i, ma.getMarkClass());
                            if (a != null) {
                                if (ps.adjust(a.getAlignmentAdjustment(ma))) {
                                    ps.setAdjusted(true);
                                }
                            }
                            ps.consume(1);
                            applied = true;
                            break;
                        }
                    }
                }
            }
            return applied;
        }

        /**
         * Obtain mark anchor associated with mark coverage index.
         * @param ciMark coverage index
         * @param giMark input glyph index of mark glyph
         * @return mark anchor or null if none applies
         */
        public abstract MarkAnchor getMarkAnchor(int ciMark, int giMark);

        /**
         * Obtain maximum component count.
         * @return maximum component count (>=0)
         */
        public abstract int getMaxComponentCount();

        /**
         * Obtain anchor associated with ligature glyph index and mark class.
         * @param giLig input glyph index of ligature glyph
         * @param maxComponents maximum component count
         * @param component component number (0...maxComponents-1)
         * @param markClass class number of mark glyph
         * @return anchor or null if none applies
         */
        public abstract Anchor getLigatureAnchor(int giLig, int maxComponents, int component, int markClass);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new MarkToLigatureSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class MarkToLigatureSubtableFormat1 extends MarkToLigatureSubtable {
        private GlyphCoverageTable lct;                 // ligature coverage table
        private int nmc;                                // mark class count
        private int mxc;                                // maximum ligature component count
        private MarkAnchor[] maa;                       // mark anchor array, ordered by mark coverage index
        private Anchor[][][] lam;                       // ligature anchor matrix, ordered by ligature coverage index, then ligature component, then mark class
        MarkToLigatureSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (lam != null) {
                List<SubtableEntry> entries = new ArrayList<>(5);
                entries.add(new SEGlyphCoverageTable(lct));
                entries.add(SEInteger.valueOf(nmc));
                entries.add(SEInteger.valueOf(mxc));
                entries.add(new SEMarkAnchorList(maa));
                entries.add(new SEAnchorMultiMatrix(lam));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public MarkAnchor getMarkAnchor(int ciMark, int giMark) {
            if ((maa != null) && (ciMark < maa.length)) {
                return maa [ ciMark ];
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getMaxComponentCount() {
            return mxc;
        }

        /** {@inheritDoc} */
        @Override
        public Anchor getLigatureAnchor(int giLig, int maxComponents, int component, int markClass) {
            int ciLig;
            if ((lct != null) && ((ciLig = lct.getCoverageIndex(giLig)) >= 0)) {
                if ((lam != null) && (ciLig < lam.length)) {
                    Anchor[][] lcm = lam [ ciLig ];
                    if (component < maxComponents) {
                        Anchor[] la = lcm [ component ];
                        if ((la != null) && (markClass < la.length)) {
                            return la [ markClass ];
                        }
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 5);
            lct = checkGet(entries, 0, SEGlyphCoverageTable.class).get();
            nmc = checkGet(entries, 1, SEInteger.class).get();
            mxc = checkGet(entries, 2, SEInteger.class).get();
            maa = checkGet(entries, 3, SEMarkAnchorList.class).get();
            lam = checkGet(entries, 4, SEAnchorMultiMatrix.class).get();
        }
    }

    private abstract static class MarkToMarkSubtable extends GlyphPositioningSubtable {
        MarkToMarkSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_MARK_TO_MARK;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof MarkToMarkSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int giMark1 = ps.getGlyph();
            int ciMark1;
            if ((ciMark1 = getCoverageIndex(giMark1)) >= 0) {
                MarkAnchor ma = getMark1Anchor(ciMark1, giMark1);
                if (ma != null) {
                    if (ps.hasPrev()) {
                        Anchor a = getMark2Anchor(ps.getGlyph(-1), ma.getMarkClass());
                        if (a != null) {
                            if (ps.adjust(a.getAlignmentAdjustment(ma))) {
                                ps.setAdjusted(true);
                            }
                        }
                        ps.consume(1);
                        applied = true;
                    }
                }
            }
            return applied;
        }

        /**
         * Obtain mark 1 anchor associated with mark 1 coverage index.
         * @param ciMark1 mark 1 coverage index
         * @param giMark1 input glyph index of mark 1 glyph
         * @return mark 1 anchor or null if none applies
         */
        public abstract MarkAnchor getMark1Anchor(int ciMark1, int giMark1);

        /**
         * Obtain anchor associated with mark 2 glyph index and mark 1 class.
         * @param giMark2 input glyph index of mark 2 glyph
         * @param markClass class number of mark 1 glyph
         * @return anchor or null if none applies
         */
        public abstract Anchor getMark2Anchor(int giBase, int markClass);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new MarkToMarkSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class MarkToMarkSubtableFormat1 extends MarkToMarkSubtable {
        private GlyphCoverageTable mct2;                // mark 2 coverage table
        private int nmc;                                // mark class count
        private MarkAnchor[] maa;                       // mark1 anchor array, ordered by mark1 coverage index
        private Anchor[][] mam;                         // mark2 anchor matrix, ordered by mark2 coverage index, then by mark1 class
        MarkToMarkSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if ((mct2 != null) && (maa != null) && (nmc > 0) && (mam != null)) {
                List<SubtableEntry> entries = new ArrayList<>(4);
                entries.add(new SEGlyphCoverageTable(mct2));
                entries.add(SEInteger.valueOf(nmc));
                entries.add(new SEMarkAnchorList(maa));
                entries.add(new SEAnchorMatrix(mam));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public MarkAnchor getMark1Anchor(int ciMark1, int giMark1) {
            if ((maa != null) && (ciMark1 < maa.length)) {
                return maa [ ciMark1 ];
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public Anchor getMark2Anchor(int giMark2, int markClass) {
            int ciMark2;
            if ((mct2 != null) && ((ciMark2 = mct2.getCoverageIndex(giMark2)) >= 0)) {
                if ((mam != null) && (ciMark2 < mam.length)) {
                    Anchor[] ma = mam [ ciMark2 ];
                    if ((ma != null) && (markClass < ma.length)) {
                        return ma [ markClass ];
                    }
                }
            }
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 4);
            mct2 = checkGet(entries, 0, SEGlyphCoverageTable.class).get();
            nmc = checkGet(entries, 1, SEInteger.class).get();
            maa = checkGet(entries, 2, SEMarkAnchorList.class).get();
            mam = checkGet(entries, 3, SEAnchorMatrix.class).get();
        }
    }

    private abstract static class ContextualSubtable extends GlyphPositioningSubtable {
        ContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_CONTEXTUAL;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof ContextualSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int gi = ps.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) >= 0) {
                int[] rv = new int[1];
                RuleLookup[] la = getLookups(ci, gi, ps, rv);
                if (la != null) {
                    ps.apply(la, rv[0]);
                    applied = true;
                }
            }
            return applied;
        }

        /**
         * Obtain rule lookups set associated current input glyph context.
         * @param ci coverage index of glyph at current position
         * @param gi glyph index of glyph at current position
         * @param ps glyph positioning state
         * @param rv array of ints used to receive multiple return values, must be of length 1 or greater,
         * where the first entry is used to return the input sequence length of the matched rule
         * @return array of rule lookups or null if none applies
         */
        public abstract RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new ContextualSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else if (format == 2) {
                return new ContextualSubtableFormat2(id, sequence, flags, format, coverage, entries);
            } else if (format == 3) {
                return new ContextualSubtableFormat3(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class ContextualSubtableFormat1 extends ContextualSubtable {
        private RuleSet[] rsa;                          // rule set array, ordered by glyph coverage index
        ContextualSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                return mutableSingleton(new SERuleSetList(rsa));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedGlyphSequenceRule)) {
                            ChainedGlyphSequenceRule cr = (ChainedGlyphSequenceRule) r;
                            int[] iga = cr.getGlyphs(gi);
                            if (matches(ps, iga, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphPositioningState ps, int[] glyphs, int offset, int[] rv) {
            if ((glyphs == null) || (glyphs.length == 0)) {
                return true;                            // match null or empty glyph sequence
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ps.getIgnoreDefault();
                int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = glyphs.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, null, counts);
                    for (int k = 0; k < ngm; k++) {
                        if (ga [ k ] != glyphs [ k ]) {
                            return false;               // match fails at ga [ k ]
                        }
                    }
                    if (rv != null) {
                        rv[0] = counts[0] + counts[1];
                    }
                    return true;                        // all glyphs match
                }
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            rsa = checkGet(entries, 0, SERuleSetList.class).get();
        }
    }

    private static class ContextualSubtableFormat2 extends ContextualSubtable {
        private GlyphClassTable cdt;                    // class def table
        private int ngc;                                // class set count
        private RuleSet[] rsa;                          // rule set array, ordered by class number [0...ngc - 1]
        ContextualSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                List<SubtableEntry> entries = new ArrayList<>(3);
                entries.add(new SEGlyphClassTable(cdt));
                entries.add(SEInteger.valueOf(ngc));
                entries.add(new SERuleSetList(rsa));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String,LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedClassSequenceRule)) {
                            ChainedClassSequenceRule cr = (ChainedClassSequenceRule) r;
                            int[] ca = cr.getClasses(cdt.getClassIndex(gi, ps.getClassMatchSet(gi)));
                            if (matches(ps, cdt, ca, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphPositioningState ps, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
            if ((cdt == null) || (classes == null) || (classes.length == 0)) {
                return true;                            // match null class definitions, null or empty class sequence
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ps.getIgnoreDefault();
                int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = classes.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, null, counts);
                    for (int k = 0; k < ngm; k++) {
                        int gi = ga [ k ];
                        int ms = ps.getClassMatchSet(gi);
                        int gc = cdt.getClassIndex(gi, ms);
                        if ((gc < 0) || (gc >= cdt.getClassSize(ms))) {
                            return false;               // none or invalid class fails mat ch
                        } else if (gc != classes [ k ]) {
                            return false;               // match fails at ga [ k ]
                        }
                    }
                    if (rv != null) {
                        rv[0] = counts[0] + counts[1];
                    }
                    return true;                        // all glyphs match
                }
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 3);
            cdt = checkGet(entries, 0, SEGlyphClassTable.class).get();
            ngc = checkGet(entries, 1, SEInteger.class).get();
            rsa = checkGet(entries, 2, SERuleSetList.class).get();
        }
    }

    private static class ContextualSubtableFormat3 extends ContextualSubtable {
        private RuleSet[] rsa;                          // rule set array, containing a single rule set
        ContextualSubtableFormat3(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                return mutableSingleton(new SERuleSetList(rsa));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedCoverageSequenceRule)) {
                            ChainedCoverageSequenceRule cr = (ChainedCoverageSequenceRule) r;
                            GlyphCoverageTable[] gca = cr.getCoverages();
                            if (matches(ps, gca, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphPositioningState ps, GlyphCoverageTable[] gca, int offset, int[] rv) {
            if ((gca == null) || (gca.length == 0)) {
                return true;                            // match null or empty coverage array
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ps.getIgnoreDefault();
                int[] counts = ps.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = gca.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ps.getGlyphs(offset, ngm, reverse, ignores, null, counts);
                    for (int k = 0; k < ngm; k++) {
                        GlyphCoverageTable ct = gca [ k ];
                        if (ct != null) {
                            if (ct.getCoverageIndex(ga [ k ]) < 0) {
                                return false;           // match fails at ga [ k ]
                            }
                        }
                    }
                    if (rv != null) {
                        rv[0] = counts[0] + counts[1];
                    }
                    return true;                        // all glyphs match
                }
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            rsa = checkGet(entries, 0, SERuleSetList.class).get();
        }
    }

    private abstract static class ChainedContextualSubtable extends GlyphPositioningSubtable {
        ChainedContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof ChainedContextualSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean position(GlyphPositioningState ps) {
            boolean applied = false;
            int gi = ps.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) >= 0) {
                int[] rv = new int[1];
                RuleLookup[] la = getLookups(ci, gi, ps, rv);
                if (la != null) {
                    ps.apply(la, rv[0]);
                    applied = true;
                }
            }
            return applied;
        }

        /**
         * Obtain rule lookups set associated current input glyph context.
         * @param ci coverage index of glyph at current position
         * @param gi glyph index of glyph at current position
         * @param ps glyph positioning state
         * @param rv array of ints used to receive multiple return values, must be of length 1 or greater,
         * where the first entry is used to return the input sequence length of the matched rule
         * @return array of rule lookups or null if none applies
         */
        public abstract RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv);

        static GlyphPositioningSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new ChainedContextualSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else if (format == 2) {
                return new ChainedContextualSubtableFormat2(id, sequence, flags, format, coverage, entries);
            } else if (format == 3) {
                return new ChainedContextualSubtableFormat3(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class ChainedContextualSubtableFormat1 extends ChainedContextualSubtable {
        private RuleSet[] rsa;                          // rule set array, ordered by glyph coverage index
        ChainedContextualSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                return mutableSingleton(new SERuleSetList(rsa));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedGlyphSequenceRule)) {
                            ChainedGlyphSequenceRule cr = (ChainedGlyphSequenceRule) r;
                            int[] iga = cr.getGlyphs(gi);
                            if (matches(ps, iga, 0, rv)) {
                                int[] bga = cr.getBacktrackGlyphs();
                                if (matches(ps, bga, -1, null)) {
                                    int[] lga = cr.getLookaheadGlyphs();
                                    if (matches(ps, lga, rv[0], null)) {
                                        return r.getLookups();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean matches(GlyphPositioningState ps, int[] glyphs, int offset, int[] rv) {
            return ContextualSubtableFormat1.matches(ps, glyphs, offset, rv);
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            rsa = checkGet(entries, 0, SERuleSetList.class).get();
        }
    }

    private static class ChainedContextualSubtableFormat2 extends ChainedContextualSubtable {
        private GlyphClassTable icdt;                   // input class def table
        private GlyphClassTable bcdt;                   // backtrack class def table
        private GlyphClassTable lcdt;                   // lookahead class def table
        private int ngc;                                // class set count
        private RuleSet[] rsa;                          // rule set array, ordered by class number [0...ngc - 1]
        ChainedContextualSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                List<SubtableEntry> entries = new ArrayList<>(5);
                entries.add(new SEGlyphClassTable(icdt));
                entries.add(new SEGlyphClassTable(bcdt));
                entries.add(new SEGlyphClassTable(lcdt));
                entries.add(SEInteger.valueOf(ngc));
                entries.add(new SERuleSetList(rsa));
                return entries;
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String,LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedClassSequenceRule)) {
                            ChainedClassSequenceRule cr = (ChainedClassSequenceRule) r;
                            int[] ica = cr.getClasses(icdt.getClassIndex(gi, ps.getClassMatchSet(gi)));
                            if (matches(ps, icdt, ica, 0, rv)) {
                                int[] bca = cr.getBacktrackClasses();
                                if (matches(ps, bcdt, bca, -1, null)) {
                                    int[] lca = cr.getLookaheadClasses();
                                    if (matches(ps, lcdt, lca, rv[0], null)) {
                                        return r.getLookups();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean matches(GlyphPositioningState ps, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
            return ContextualSubtableFormat2.matches(ps, cdt, classes, offset, rv);
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 5);
            icdt = checkGet(entries, 0, SEGlyphClassTable.class).get();
            bcdt = checkGet(entries, 1, SEGlyphClassTable.class).get();
            lcdt = checkGet(entries, 2, SEGlyphClassTable.class).get();
            ngc = checkGet(entries, 3, SEInteger.class).get();
            rsa = checkGet(entries, 4, SERuleSetList.class).get();
            if (rsa.length != ngc) {
                throw new AdvancedTypographicTableFormatException("illegal entries, RuleSet[] length is " + rsa.length + ", but expected " + ngc + " glyph classes");
            }
        }
    }

    private static class ChainedContextualSubtableFormat3 extends ChainedContextualSubtable {
        private RuleSet[] rsa;                          // rule set array, containing a single rule set
        ChainedContextualSubtableFormat3(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (rsa != null) {
                return mutableSingleton(new SERuleSetList(rsa));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String,LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphPositioningState ps, int[] rv) {
            assert ps != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                RuleSet rs = rsa [ 0 ];
                if (rs != null) {
                    Rule[] ra = rs.getRules();
                    for (int i = 0, n = ra.length; i < n; i++) {
                        Rule r = ra [ i ];
                        if ((r != null) && (r instanceof ChainedCoverageSequenceRule)) {
                            ChainedCoverageSequenceRule cr = (ChainedCoverageSequenceRule) r;
                            GlyphCoverageTable[] igca = cr.getCoverages();
                            if (matches(ps, igca, 0, rv)) {
                                GlyphCoverageTable[] bgca = cr.getBacktrackCoverages();
                                if (matches(ps, bgca, -1, null)) {
                                    GlyphCoverageTable[] lgca = cr.getLookaheadCoverages();
                                    if (matches(ps, lgca, rv[0], null)) {
                                        return r.getLookups();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean matches(GlyphPositioningState ps, GlyphCoverageTable[] gca, int offset, int[] rv) {
            return ContextualSubtableFormat3.matches(ps, gca, offset, rv);
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            rsa = checkGet(entries, 0, SERuleSetList.class).get();
        }
    }

    /**
     * The <code>DeviceTable</code> class implements a positioning device table record, comprising
     * adjustments to be made to scaled design units according to the scaled size.
     */
    public static class DeviceTable {

        private final int startSize;
        private final int endSize;
        private final int[] deltas;

        /**
         * Instantiate a DeviceTable.
         * @param startSize the
         * @param endSize the ending (scaled) size
         * @param deltas adjustments for each scaled size
         */
        public DeviceTable(int startSize, int endSize, int[] deltas) {
            assert startSize >= 0;
            assert startSize <= endSize;
            assert deltas != null;
            assert deltas.length == (endSize - startSize) + 1;
            this.startSize = startSize;
            this.endSize = endSize;
            this.deltas = deltas;
        }

        /** @return the start size */
        public int getStartSize() {
            return startSize;
        }

        /** @return the end size */
        public int getEndSize() {
            return endSize;
        }

        /** @return the deltas */
        public int[] getDeltas() {
            return deltas;
        }

        /**
         * Find device adjustment.
         * @param fontSize the font size to search for
         * @return an adjustment if font size matches an entry
         */
        public int findAdjustment(int fontSize) {
            // [TODO] at present, assumes that 1 device unit equals one point
            int fs = fontSize / 1000;
            if (fs < startSize) {
                return 0;
            } else if (fs <= endSize) {
                return deltas [ fs - startSize ] * 1000;
            } else {
                return 0;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "{ start = " + startSize + ", end = " + endSize + ", deltas = " + Arrays.toString(deltas) + "}";
        }

    }

    /**
     * The <code>Value</code> class implements a positioning value record, comprising placement
     * and advancement information in X and Y axes, and optionally including device data used to
     * perform device (grid-fitted) specific fine grain adjustments.
     */
    public static class Value {

        /** X_PLACEMENT value format flag */
        public static final int X_PLACEMENT             = 0x0001;
        /** Y_PLACEMENT value format flag */
        public static final int Y_PLACEMENT             = 0x0002;
        /** X_ADVANCE value format flag */
        public static final int X_ADVANCE               = 0x0004;
        /** Y_ADVANCE value format flag */
        public static final int Y_ADVANCE               = 0x0008;
        /** X_PLACEMENT_DEVICE value format flag */
        public static final int X_PLACEMENT_DEVICE      = 0x0010;
        /** Y_PLACEMENT_DEVICE value format flag */
        public static final int Y_PLACEMENT_DEVICE      = 0x0020;
        /** X_ADVANCE_DEVICE value format flag */
        public static final int X_ADVANCE_DEVICE        = 0x0040;
        /** Y_ADVANCE_DEVICE value format flag */
        public static final int Y_ADVANCE_DEVICE        = 0x0080;

        /** X_PLACEMENT value index (within adjustments arrays) */
        public static final int IDX_X_PLACEMENT         = 0;
        /** Y_PLACEMENT value index (within adjustments arrays) */
        public static final int IDX_Y_PLACEMENT         = 1;
        /** X_ADVANCE value index (within adjustments arrays) */
        public static final int IDX_X_ADVANCE           = 2;
        /** Y_ADVANCE value index (within adjustments arrays) */
        public static final int IDX_Y_ADVANCE           = 3;

        private int xPlacement;                         // x placement
        private int yPlacement;                         // y placement
        private int xAdvance;                           // x advance
        private int yAdvance;                           // y advance
        private final DeviceTable xPlaDevice;           // x placement device table
        private final DeviceTable yPlaDevice;           // y placement device table
        private final DeviceTable xAdvDevice;           // x advance device table
        private final DeviceTable yAdvDevice;           // x advance device table

        /**
         * Instantiate a Value.
         * @param xPlacement the x placement or zero
         * @param yPlacement the y placement or zero
         * @param xAdvance the x advance or zero
         * @param yAdvance the y advance or zero
         * @param xPlaDevice the x placement device table or null
         * @param yPlaDevice the y placement device table or null
         * @param xAdvDevice the x advance device table or null
         * @param yAdvDevice the y advance device table or null
         */
        public Value(int xPlacement, int yPlacement, int xAdvance, int yAdvance, DeviceTable xPlaDevice, DeviceTable yPlaDevice, DeviceTable xAdvDevice, DeviceTable yAdvDevice) {
            this.xPlacement = xPlacement;
            this.yPlacement = yPlacement;
            this.xAdvance = xAdvance;
            this.yAdvance = yAdvance;
            this.xPlaDevice = xPlaDevice;
            this.yPlaDevice = yPlaDevice;
            this.xAdvDevice = xAdvDevice;
            this.yAdvDevice = yAdvDevice;
        }

        /** @return the x placement */
        public int getXPlacement() {
            return xPlacement;
        }

        /** @return the y placement */
        public int getYPlacement() {
            return yPlacement;
        }

        /** @return the x advance */
        public int getXAdvance() {
            return xAdvance;
        }

        /** @return the y advance */
        public int getYAdvance() {
            return yAdvance;
        }

        /** @return the x placement device table */
        public DeviceTable getXPlaDevice() {
            return xPlaDevice;
        }

        /** @return the y placement device table */
        public DeviceTable getYPlaDevice() {
            return yPlaDevice;
        }

        /** @return the x advance device table */
        public DeviceTable getXAdvDevice() {
            return xAdvDevice;
        }

        /** @return the y advance device table */
        public DeviceTable getYAdvDevice() {
            return yAdvDevice;
        }

        /**
         * Apply value to specific adjustments to without use of device table adjustments.
         * @param xPlacement the x placement or zero
         * @param yPlacement the y placement or zero
         * @param xAdvance the x advance or zero
         * @param yAdvance the y advance or zero
         */
        public void adjust(int xPlacement, int yPlacement, int xAdvance, int yAdvance) {
            this.xPlacement += xPlacement;
            this.yPlacement += yPlacement;
            this.xAdvance += xAdvance;
            this.yAdvance += yAdvance;
        }

        /**
         * Apply value to adjustments using font size for device table adjustments.
         * @param adjustments array of four integers containing X,Y placement and X,Y advance adjustments
         * @param fontSize font size for device table adjustments
         * @return true if some adjustment was made
         */
        public boolean adjust(int[] adjustments, int fontSize) {
            boolean adjust = false;
            int dv;
            if ((dv = xPlacement) != 0) {
                adjustments [ IDX_X_PLACEMENT ] += dv;
                adjust = true;
            }
            if ((dv = yPlacement) != 0) {
                adjustments [ IDX_Y_PLACEMENT ] += dv;
                adjust = true;
            }
            if ((dv = xAdvance) != 0) {
                adjustments [ IDX_X_ADVANCE ] += dv;
                adjust = true;
            }
            if ((dv = yAdvance) != 0) {
                adjustments [ IDX_Y_ADVANCE ] += dv;
                adjust = true;
            }
            if (fontSize != 0) {
                DeviceTable dt;
                if ((dt = xPlaDevice) != null) {
                    if ((dv = dt.findAdjustment(fontSize)) != 0) {
                        adjustments [ IDX_X_PLACEMENT ] += dv;
                        adjust = true;
                    }
                }
                if ((dt = yPlaDevice) != null) {
                    if ((dv = dt.findAdjustment(fontSize)) != 0) {
                        adjustments [ IDX_Y_PLACEMENT ] += dv;
                        adjust = true;
                    }
                }
                if ((dt = xAdvDevice) != null) {
                    if ((dv = dt.findAdjustment(fontSize)) != 0) {
                        adjustments [ IDX_X_ADVANCE ] += dv;
                        adjust = true;
                    }
                }
                if ((dt = yAdvDevice) != null) {
                    if ((dv = dt.findAdjustment(fontSize)) != 0) {
                        adjustments [ IDX_Y_ADVANCE ] += dv;
                        adjust = true;
                    }
                }
            }
            return adjust;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            sb.append("{ ");
            if (xPlacement != 0) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xPlacement = " + xPlacement);
            }
            if (yPlacement != 0) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("yPlacement = " + yPlacement);
            }
            if (xAdvance != 0) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xAdvance = " + xAdvance);
            }
            if (yAdvance != 0) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("yAdvance = " + yAdvance);
            }
            if (xPlaDevice != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xPlaDevice = " + xPlaDevice);
            }
            if (yPlaDevice != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xPlaDevice = " + yPlaDevice);
            }
            if (xAdvDevice != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xAdvDevice = " + xAdvDevice);
            }
            if (yAdvDevice != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("xAdvDevice = " + yAdvDevice);
            }
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>PairValues</code> class implements a pair value record, comprising a glyph id (or zero)
     * and two optional positioning values.
     */
    public static class PairValues {

        private final int glyph;                        // glyph id (or 0)
        private final Value value1;                     // value for first glyph in pair (or null)
        private final Value value2;                     // value for second glyph in pair (or null)

        /**
         * Instantiate a PairValues.
         * @param glyph the glyph id (or zero)
         * @param value1 the value of the first glyph in pair (or null)
         * @param value2 the value of the second glyph in pair (or null)
         */
        public PairValues(int glyph, Value value1, Value value2) {
            assert glyph >= 0;
            this.glyph = glyph;
            this.value1 = value1;
            this.value2 = value2;
        }

        /** @return the glyph id */
        public int getGlyph() {
            return glyph;
        }

        /** @return the first value */
        public Value getValue1() {
            return value1;
        }

        /** @return the second value */
        public Value getValue2() {
            return value2;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            sb.append("{ ");
            if (glyph != 0) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("glyph = " + glyph);
            }
            if (value1 != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("value1 = " + value1);
            }
            if (value2 != null) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append("value2 = " + value2);
            }
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>Anchor</code> class implements a anchor record, comprising an X,Y coordinate pair,
     * an optional anchor point index (or -1), and optional X or Y device tables (or null if absent).
     */
    public static class Anchor {

        private final int x;                            // xCoordinate (in design units)
        private final int y;                            // yCoordinate (in design units)
        private final int anchorPoint;                  // anchor point index (or -1)
        private final DeviceTable xDevice;              // x device table
        private final DeviceTable yDevice;              // y device table

        /**
         * Instantiate an Anchor (format 1).
         * @param x the x coordinate
         * @param y the y coordinate
         */
        public Anchor(int x, int y) {
            this (x, y, -1, null, null);
        }

        /**
         * Instantiate an Anchor (format 2).
         * @param x the x coordinate
         * @param y the y coordinate
         * @param anchorPoint anchor index (or -1)
         */
        public Anchor(int x, int y, int anchorPoint) {
            this (x, y, anchorPoint, null, null);
        }

        /**
         * Instantiate an Anchor (format 3).
         * @param x the x coordinate
         * @param y the y coordinate
         * @param xDevice the x device table (or null if not present)
         * @param yDevice the y device table (or null if not present)
         */
        public Anchor(int x, int y, DeviceTable xDevice, DeviceTable yDevice) {
            this (x, y, -1, xDevice, yDevice);
        }

        /**
         * Instantiate an Anchor based on an existing anchor.
         * @param a the existing anchor
         */
        protected Anchor(Anchor a) {
            this (a.x, a.y, a.anchorPoint, a.xDevice, a.yDevice);
        }

        private Anchor(int x, int  y, int anchorPoint, DeviceTable xDevice, DeviceTable yDevice) {
            assert (anchorPoint >= 0) || (anchorPoint == -1);
            this.x = x;
            this.y = y;
            this.anchorPoint = anchorPoint;
            this.xDevice = xDevice;
            this.yDevice = yDevice;
        }

        /** @return the x coordinate */
        public int getX() {
            return x;
        }

        /** @return the y coordinate */
        public int getY() {
            return y;
        }

        /** @return the anchor point index (or -1 if not specified) */
        public int getAnchorPoint() {
            return anchorPoint;
        }

        /** @return the x device table (or null if not specified) */
        public DeviceTable getXDevice() {
            return xDevice;
        }

        /** @return the y device table (or null if not specified) */
        public DeviceTable getYDevice() {
            return yDevice;
        }

        /**
         * Obtain adjustment value required to align the specified anchor
         * with this anchor.
         * @param a the anchor to align
         * @return the adjustment value needed to effect alignment
         */
        public Value getAlignmentAdjustment(Anchor a) {
            assert a != null;
            // TODO - handle anchor point
            // TODO - handle device tables
            return new Value(x - a.x, y - a.y, 0, 0, null, null, null, null);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ [" + x + "," + y + "]");
            if (anchorPoint != -1) {
                sb.append(", anchorPoint = " + anchorPoint);
            }
            if (xDevice != null) {
                sb.append(", xDevice = " + xDevice);
            }
            if (yDevice != null) {
                sb.append(", yDevice = " + yDevice);
            }
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>MarkAnchor</code> class is a subclass of the <code>Anchor</code> class, adding a mark
     * class designation.
     */
    public static class MarkAnchor extends Anchor {

        private final int markClass;                            // mark class

        /**
         * Instantiate a MarkAnchor
         * @param markClass the mark class
         * @param a the underlying anchor (whose fields are copied)
         */
        public MarkAnchor(int markClass, Anchor a) {
            super(a);
            this.markClass = markClass;
        }

        /** @return the mark class */
        public int getMarkClass() {
            return markClass;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "{ markClass = " + markClass + ", anchor = " + super.toString() + " }";
        }

    }

}
