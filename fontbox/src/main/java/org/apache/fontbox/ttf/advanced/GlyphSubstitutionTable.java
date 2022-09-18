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
import org.apache.fontbox.ttf.advanced.util.CharAssociation;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.GlyphTester;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>The <code>GlyphSubstitutionTable</code> class is a glyph table that implements
 * <code>GlyphSubstitution</code> functionality.</p>
 *
 * <p>Adapted from the Apache FOP Project.</p>
 *
 * @author Glenn Adams
 */
public class GlyphSubstitutionTable extends AdvancedTypographicTable {

    /** logging instance */
    private static final Log log = LogFactory.getLog(GlyphSubstitutionTable.class);

    /** tag that identifies this table type */
    public static final String TAG = "GSUB";

    /** single substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_SINGLE = 1;
    /** multiple substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_MULTIPLE = 2;
    /** alternate substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_ALTERNATE = 3;
    /** ligature substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_LIGATURE = 4;
    /** contextual substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_CONTEXTUAL = 5;
    /** chained contextual substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL = 6;
    /** extension substitution substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION = 7;
    /** reverse chained contextual single substitution subtable type */
    public static final int GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE = 8;

    public GlyphSubstitutionTable(OpenTypeFont otf) {
        super(otf, null, new java.util.HashMap<>(0));
    }

    /**
     * Initialize this <code>GlyphSubstitutionTable</code> object using the specified lookups
     * and subtables.
     * @param gdef glyph definition table that applies
     * @param lookups a map of lookup specifications to subtable identifier strings
     * @param subtables a list of identified subtables
     */
    public GlyphSubstitutionTable initialize(GlyphDefinitionTable gdef, Map<LookupSpec, List<String>> lookups, List<GlyphSubtable> subtables) {
        initialize(lookups);
        if ((subtables == null) || (subtables.isEmpty())) {
            throw new AdvancedTypographicTableFormatException("subtables must be non-empty");
        } else {
            for (GlyphSubtable o : subtables) {
                if (o instanceof GlyphSubstitutionSubtable) {
                    addSubtable(o);
                } else {
                    throw new AdvancedTypographicTableFormatException("subtable must be a glyph substitution subtable");
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
     * Perform substitution processing using all matching lookups.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param features parameterized features
     * @return the substituted (output) glyph sequence
     */
    public GlyphSequence substitute(GlyphSequence gs, String script, String language, Object[][] features) {
        GlyphSequence ogs;
        Map<LookupSpec, List<LookupTable>> lookups = matchLookups(script, language, "*");
        if ((lookups != null) && (lookups.size() > 0)) {
            ScriptProcessor sp = ScriptProcessor.getInstance(script);
            ogs = sp.substitute(this, gs, script, language, features, lookups);
        } else {
            ogs = gs;
        }
        return ogs;
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
            t = GSUB_LOOKUP_TYPE_SINGLE;
        } else if ("multiple".equals(s)) {
            t = GSUB_LOOKUP_TYPE_MULTIPLE;
        } else if ("alternate".equals(s)) {
            t = GSUB_LOOKUP_TYPE_ALTERNATE;
        } else if ("ligature".equals(s)) {
            t = GSUB_LOOKUP_TYPE_LIGATURE;
        } else if ("contextual".equals(s)) {
            t = GSUB_LOOKUP_TYPE_CONTEXTUAL;
        } else if ("chainedcontextual".equals(s)) {
            t = GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
        } else if ("extensionsubstitution".equals(s)) {
            t = GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION;
        } else if ("reversechainiingcontextualsingle".equals(s)) {
            t = GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE;
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
        String tn = null;
        switch (type) {
        case GSUB_LOOKUP_TYPE_SINGLE:
            tn = "single";
            break;
        case GSUB_LOOKUP_TYPE_MULTIPLE:
            tn = "multiple";
            break;
        case GSUB_LOOKUP_TYPE_ALTERNATE:
            tn = "alternate";
            break;
        case GSUB_LOOKUP_TYPE_LIGATURE:
            tn = "ligature";
            break;
        case GSUB_LOOKUP_TYPE_CONTEXTUAL:
            tn = "contextual";
            break;
        case GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL:
            tn = "chainedcontextual";
            break;
        case GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION:
            tn = "extensionsubstitution";
            break;
        case GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE:
            tn = "reversechainiingcontextualsingle";
            break;
        default:
            tn = "unknown";
            break;
        }
        return tn;
    }

    /**
     * Create a substitution subtable according to the specified arguments.
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
        case GSUB_LOOKUP_TYPE_SINGLE:
            st = SingleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_MULTIPLE:
            st = MultipleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_ALTERNATE:
            st = AlternateSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_LIGATURE:
            st = LigatureSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_CONTEXTUAL:
            st = ContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL:
            st = ChainedContextualSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        case GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE:
            st = ReverseChainedSingleSubtable.create(id, sequence, flags, format, coverage, entries);
            break;
        default:
            break;
        }
        return st;
    }

    /**
     * Create a substitution subtable according to the specified arguments.
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

    private abstract static class SingleSubtable extends GlyphSubstitutionSubtable {
        SingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_SINGLE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof SingleSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                int go = getGlyphForCoverageIndex(ci, gi);
                if ((go < 0) || (go > 65535)) {
                    go = 65535;
                }
                ss.putGlyph(go, ss.getAssociation(), Boolean.TRUE);
                ss.consume(1);
                return true;
            }
        }

        /**
         * Obtain glyph for coverage index.
         * @param ci coverage index
         * @param gi original glyph index
         * @return substituted glyph value
         * @throws IllegalArgumentException if coverage index is not valid
         */
        public abstract int getGlyphForCoverageIndex(int ci, int gi) throws IllegalArgumentException;

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
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
        private int delta;
        private int ciMax;
        SingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return mutableSingleton(SEInteger.valueOf(delta));
        }

        /** {@inheritDoc} */
        @Override
        public int getGlyphForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
            if (ci <= ciMax) {
                return gi + delta;
            } else {
                throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + ciMax);
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            this.delta = checkGet(entries, 0, SEInteger.class).get();
            this.ciMax = getCoverageSize() - 1;
        }
    }

    private static class SingleSubtableFormat2 extends SingleSubtable {
        private int[] glyphs;
        SingleSubtableFormat2(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return arrayMap(glyphs, SEInteger::valueOf);
        }

        /** {@inheritDoc} */
        @Override
        public int getGlyphForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
            if (glyphs == null) {
                return -1;
            } else if (ci >= glyphs.length) {
                throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + glyphs.length);
            } else {
                return glyphs [ ci ];
            }
        }

        private void populate(List<SubtableEntry> entries) {
            int i = 0;
            int n = entries.size();
            int[] glyphs = new int [ n ];

            for (int idx = 0; idx < n; idx++) {
                int gid = checkGet(entries, idx, SEInteger.class).get();
                checkGidRange(gid, () -> "illegal glyph index: " + gid);
                glyphs [ i++ ] = gid;
            }

            assert i == n;
            assert this.glyphs == null;
            this.glyphs = glyphs;
        }
    }

    private abstract static class MultipleSubtable extends GlyphSubstitutionSubtable {
        public MultipleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_MULTIPLE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof MultipleSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                int[] ga = getGlyphsForCoverageIndex(ci, gi);
                if (ga != null) {
                    ss.putGlyphs(ga, CharAssociation.replicate(ss.getAssociation(), ga.length), Boolean.TRUE);
                    ss.consume(1);
                }
                return true;
            }
        }

        /**
         * Obtain glyph sequence for coverage index.
         * @param ci coverage index
         * @param gi original glyph index
         * @return sequence of glyphs to substitute for input glyph
         * @throws IllegalArgumentException if coverage index is not valid
         */
        public abstract int[] getGlyphsForCoverageIndex(int ci, int gi) throws IllegalArgumentException;

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new MultipleSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class MultipleSubtableFormat1 extends MultipleSubtable {
        private int[][] gsa;                            // glyph sequence array, ordered by coverage index
        MultipleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            if (gsa != null) {
                return mutableSingleton(new SESequenceList(gsa));
            } else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int[] getGlyphsForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
            if (gsa == null) {
                return null;
            } else if (ci >= gsa.length) {
                throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + gsa.length);
            } else {
                return gsa [ ci ];
            }
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            gsa = checkGet(entries, 0, SESequenceList.class).get();
        }
    }

    private abstract static class AlternateSubtable extends GlyphSubstitutionSubtable {
        public AlternateSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_ALTERNATE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof AlternateSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                int[] ga = getAlternatesForCoverageIndex(ci, gi);
                int ai = ss.getAlternatesIndex(ci);
                int go;
                if ((ai < 0) || (ai >= ga.length)) {
                    go = gi;
                } else {
                    go = ga [ ai ];
                }
                if ((go < 0) || (go > 65535)) {
                    go = 65535;
                }
                ss.putGlyph(go, ss.getAssociation(), Boolean.TRUE);
                ss.consume(1);
                return true;
            }
        }

        /**
         * Obtain glyph alternates for coverage index.
         * @param ci coverage index
         * @param gi original glyph index
         * @return sequence of glyphs to substitute for input glyph
         * @throws IllegalArgumentException if coverage index is not valid
         */
        public abstract int[] getAlternatesForCoverageIndex(int ci, int gi) throws IllegalArgumentException;

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new AlternateSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class AlternateSubtableFormat1 extends AlternateSubtable {
        private int[][] gaa;                            // glyph alternates array, ordered by coverage index
        AlternateSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return arrayMap(gaa, SEIntList::new);
        }

        /** {@inheritDoc} */
        @Override
        public int[] getAlternatesForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
            if (gaa == null) {
                return null;
            } else if (ci >= gaa.length) {
                throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + gaa.length);
            } else {
                return gaa [ ci ];
            }
        }
        private void populate(List<SubtableEntry> entries) {
            int i = 0;
            int n = entries.size();
            int[][] gaa = new int [ n ][];
            for (int idx = 0; idx < n; idx++) {
                gaa[i++] = checkGet(entries, idx, SEIntList.class).get();
            }
            assert i == n;
            assert this.gaa == null;
            this.gaa = gaa;
        }
    }

    private abstract static class LigatureSubtable extends GlyphSubstitutionSubtable {
        public LigatureSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_LIGATURE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof LigatureSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                LigatureSet ls = getLigatureSetForCoverageIndex(ci, gi);
                if (ls != null) {
                    boolean reverse = false;
                    GlyphTester ignores = ss.getIgnoreDefault();
                    int[] counts = ss.getGlyphsAvailable(0, reverse, ignores);
                    int nga = counts[0];
                    int ngi;
                    if (nga > 1) {
                        int[] iga = ss.getGlyphs(0, nga, reverse, ignores, null, counts);
                        Ligature l = findLigature(ls, iga);
                        if (l != null) {
                            int go = l.getLigature();
                            if ((go < 0) || (go > 65535)) {
                                go = 65535;
                            }
                            int nmg = 1 + l.getNumComponents();
                            // fetch matched number of component glyphs to determine matched and ignored count
                            ss.getGlyphs(0, nmg, reverse, ignores, null, counts);
                            nga = counts[0];
                            ngi = counts[1];
                            // fetch associations of matched component glyphs
                            CharAssociation[] laa = ss.getAssociations(0, nga);
                            // output ligature glyph and its association
                            ss.putGlyph(go, CharAssociation.join(laa), Boolean.TRUE);
                            // fetch and output ignored glyphs (if necessary)
                            if (ngi > 0) {
                                ss.putGlyphs(ss.getIgnoredGlyphs(0, ngi), ss.getIgnoredAssociations(0, ngi), null);
                            }
                            ss.consume(nga + ngi);
                        }
                    }
                }
                return true;
            }
        }

        private Ligature findLigature(LigatureSet ls, int[] glyphs) {
            Ligature[] la = ls.getLigatures();
            int k = -1;
            int maxComponents = -1;
            for (int i = 0, n = la.length; i < n; i++) {
                Ligature l = la [ i ];
                if (l.matchesComponents(glyphs)) {
                    int nc = l.getNumComponents();
                    if (nc > maxComponents) {
                        maxComponents = nc;
                        k = i;
                    }
                }
            }
            if (k >= 0) {
                return la [ k ];
            } else {
                return null;
            }
        }

        /**
         * Obtain ligature set for coverage index.
         * @param ci coverage index
         * @param gi original glyph index
         * @return ligature set (or null if none defined)
         * @throws IllegalArgumentException if coverage index is not valid
         */
        public abstract LigatureSet getLigatureSetForCoverageIndex(int ci, int gi) throws IllegalArgumentException;

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new LigatureSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class LigatureSubtableFormat1 extends LigatureSubtable {
        private LigatureSet[] ligatureSets;
        public LigatureSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return arrayMap(ligatureSets, SELigatureSet::new);
        }

        /** {@inheritDoc} */
        @Override
        public LigatureSet getLigatureSetForCoverageIndex(int ci, int gi) throws IllegalArgumentException {
            if (ligatureSets == null) {
                return null;
            } else if (ci >= ligatureSets.length) {
                throw new IllegalArgumentException("coverage index " + ci + " out of range, maximum coverage index is " + ligatureSets.length);
            } else {
                return ligatureSets [ ci ];
            }
        }

        private void populate(List<SubtableEntry> entries) {
            int i = 0;
            int n = entries.size();
            LigatureSet[] ligatureSets = new LigatureSet [ n ];
            for (int idx = 0; idx < n; idx++) {
                ligatureSets[i++] = checkGet(entries, idx, SELigatureSet.class).get();
            }
            assert i == n;
            assert this.ligatureSets == null;
            this.ligatureSets = ligatureSets;
        }
    }

    private abstract static class ContextualSubtable extends GlyphSubstitutionSubtable {
        public ContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_CONTEXTUAL;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof ContextualSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                int[] rv = new int[1];
                RuleLookup[] la = getLookups(ci, gi, ss, rv);
                if (la != null) {
                    ss.apply(la, rv[0]);
                }
                return true;
            }
        }

        /**
         * Obtain rule lookups set associated current input glyph context.
         * @param ci coverage index of glyph at current position
         * @param gi glyph index of glyph at current position
         * @param ss glyph substitution state
         * @param rv array of ints used to receive multiple return values, must be of length 1 or greater,
         * where the first entry is used to return the input sequence length of the matched rule
         * @return array of rule lookups or null if none applies
         */
        public abstract RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv);

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
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
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
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
                            if (matches(ss, iga, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphSubstitutionState ss, int[] glyphs, int offset, int[] rv) {
            if ((glyphs == null) || (glyphs.length == 0)) {
                return true;                            // match null or empty glyph sequence
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ss.getIgnoreDefault();
                int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = glyphs.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, null, counts);
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
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
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
                            int[] ca = cr.getClasses(cdt.getClassIndex(gi, ss.getClassMatchSet(gi)));
                            if (matches(ss, cdt, ca, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphSubstitutionState ss, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
            if ((cdt == null) || (classes == null) || (classes.length == 0)) {
                return true;                            // match null class definitions, null or empty class sequence
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ss.getIgnoreDefault();
                int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = classes.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, null, counts);
                    for (int k = 0; k < ngm; k++) {
                        int gi = ga [ k ];
                        int ms = ss.getClassMatchSet(gi);
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
            if (rsa.length != ngc) {
                throw new AdvancedTypographicTableFormatException("illegal entries, RuleSet[] length is " + rsa.length + ", but expected " + ngc + " glyph classes");
            }
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
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
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
                            if (matches(ss, gca, 0, rv)) {
                                return r.getLookups();
                            }
                        }
                    }
                }
            }
            return null;
        }

        static boolean matches(GlyphSubstitutionState ss, GlyphCoverageTable[] gca, int offset, int[] rv) {
            if ((gca == null) || (gca.length == 0)) {
                return true;                            // match null or empty coverage array
            } else {
                boolean reverse = offset < 0;
                GlyphTester ignores = ss.getIgnoreDefault();
                int[] counts = ss.getGlyphsAvailable(offset, reverse, ignores);
                int nga = counts[0];
                int ngm = gca.length;
                if (nga < ngm) {
                    return false;                       // insufficient glyphs available to match
                } else {
                    int[] ga = ss.getGlyphs(offset, ngm, reverse, ignores, null, counts);
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

    private abstract static class ChainedContextualSubtable extends GlyphSubstitutionSubtable {
        public ChainedContextualSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof ChainedContextualSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean substitute(GlyphSubstitutionState ss) {
            int gi = ss.getGlyph();
            int ci;
            if ((ci = getCoverageIndex(gi)) < 0) {
                return false;
            } else {
                int[] rv = new int[1];
                RuleLookup[] la = getLookups(ci, gi, ss, rv);
                if (la != null) {
                    ss.apply(la, rv[0]);
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * Obtain rule lookups set associated current input glyph context.
         * @param ci coverage index of glyph at current position
         * @param gi glyph index of glyph at current position
         * @param ss glyph substitution state
         * @param rv array of ints used to receive multiple return values, must be of length 1 or greater
         * @return array of rule lookups or null if none applies
         */
        public abstract RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv);

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
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
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
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
                            if (matches(ss, iga, 0, rv)) {
                                int[] bga = cr.getBacktrackGlyphs();
                                if (matches(ss, bga, -1, null)) {
                                    int[] lga = cr.getLookaheadGlyphs();
                                    if (matches(ss, lga, rv[0], null)) {
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

        private boolean matches(GlyphSubstitutionState ss, int[] glyphs, int offset, int[] rv) {
            return ContextualSubtableFormat1.matches(ss, glyphs, offset, rv);
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
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
            assert (rv != null) && (rv.length > 0);
            assert rsa != null;
            if (rsa.length > 0) {
                for (RuleSet rs : rsa) {
                    if (rs != null) {
                        Rule[] ra = rs.getRules();
                        for (int i = 0, n = ra.length; i < n; i++) {
                            Rule r = ra[i];
                            if ((r != null) && (r instanceof ChainedClassSequenceRule)) {
                                ChainedClassSequenceRule cr = (ChainedClassSequenceRule) r;
                                int[] ica = cr.getClasses(icdt.getClassIndex(gi, ss.getClassMatchSet(gi)));
                                if (matches(ss, icdt, ica, 0, rv)) {
                                    int[] bca = cr.getBacktrackClasses();
                                    if (matches(ss, bcdt, bca, -1, null)) {
                                        int[] lca = cr.getLookaheadClasses();
                                        if (matches(ss, lcdt, lca, rv[0], null)) {
                                            return r.getLookups();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private boolean matches(GlyphSubstitutionState ss, GlyphClassTable cdt, int[] classes, int offset, int[] rv) {
            return ContextualSubtableFormat2.matches(ss, cdt, classes, offset, rv);
        }

        /** {@inheritDoc} */
        @Override
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
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
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            AdvancedTypographicTable.resolveLookupReferences(rsa, lookupTables);
        }

        /** {@inheritDoc} */
        @Override
        public RuleLookup[] getLookups(int ci, int gi, GlyphSubstitutionState ss, int[] rv) {
            assert ss != null;
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
                            if (matches(ss, igca, 0, rv)) {
                                GlyphCoverageTable[] bgca = cr.getBacktrackCoverages();
                                if (matches(ss, bgca, -1, null)) {
                                    GlyphCoverageTable[] lgca = cr.getLookaheadCoverages();
                                    if (matches(ss, lgca, rv[0], null)) {
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

        private boolean matches(GlyphSubstitutionState ss, GlyphCoverageTable[] gca, int offset, int[] rv) {
            return ContextualSubtableFormat3.matches(ss, gca, offset, rv);
        }

        private void populate(List<SubtableEntry> entries) {
            checkSize(entries, 1);
            rsa = checkGet(entries, 0, SERuleSetList.class).get();
        }
    }

    private abstract static class ReverseChainedSingleSubtable extends GlyphSubstitutionSubtable {
        public ReverseChainedSingleSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage);
        }

        /** {@inheritDoc} */
        @Override
        public int getType() {
            return GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCompatible(GlyphSubtable subtable) {
            return subtable instanceof ReverseChainedSingleSubtable;
        }

        /** {@inheritDoc} */
        @Override
        public boolean usesReverseScan() {
            return true;
        }

        static GlyphSubstitutionSubtable create(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            if (format == 1) {
                return new ReverseChainedSingleSubtableFormat1(id, sequence, flags, format, coverage, entries);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class ReverseChainedSingleSubtableFormat1 extends ReverseChainedSingleSubtable {
        ReverseChainedSingleSubtableFormat1(String id, int sequence, int flags, int format, GlyphCoverageTable coverage, List<SubtableEntry> entries) {
            super(id, sequence, flags, format, coverage, entries);
            populate(entries);
        }

        /** {@inheritDoc} */
        @Override
        public List<SubtableEntry> getEntries() {
            return null;
        }

        private void populate(List<SubtableEntry> entries) {
        }
    }

    /**
     * The <code>Ligature</code> class implements a ligature lookup result in terms of
     * a ligature glyph (code) and the <i>N+1...</i> components that comprise the ligature,
     * where the <i>Nth</i> component was consumed in the coverage table lookup mapping to
     * this ligature instance.
     */
    public static class Ligature {

        private final int ligature;                     // (resulting) ligature glyph
        private final int[] components;                 // component glyph codes (note that first component is implied)

        /**
         * Instantiate a ligature.
         * @param ligature glyph id
         * @param components sequence of <i>N+1...</i> component glyph (or character) identifiers
         */
        public Ligature(int ligature, int[] components) {
            if ((ligature < 0) || (ligature > 65535)) {
                throw new AdvancedTypographicTableFormatException("invalid ligature glyph index: " + ligature);
            } else if (components == null) {
                throw new AdvancedTypographicTableFormatException("invalid ligature components, must be non-null array");
            } else {
                for (int i = 0, n = components.length; i < n; i++) {
                    int gc = components [ i ];
                    if ((gc < 0) || (gc > 65535)) {
                        throw new AdvancedTypographicTableFormatException("invalid component glyph index: " + gc);
                    }
                }
                this.ligature = ligature;
                this.components = components;
            }
        }

        /** @return ligature glyph id */
        public int getLigature() {
            return ligature;
        }

        /** @return array of <i>N+1...</i> components */
        public int[] getComponents() {
            return components;
        }

        /** @return components count */
        public int getNumComponents() {
            return components.length;
        }

        /**
         * Determine if input sequence at offset matches ligature's components.
         * @param glyphs array of glyph components to match (including first, implied glyph)
         * @return true if matches
         */
        public boolean matchesComponents(int[] glyphs) {
            if (glyphs.length < (components.length + 1)) {
                return false;
            } else {
                for (int i = 0, n = components.length; i < n; i++) {
                    if (glyphs [ i + 1 ] != components [ i ]) {
                        return false;
                    }
                }
                return true;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{components={");
            for (int i = 0, n = components.length; i < n; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(Integer.toString(components[i]));
            }
            sb.append("},ligature=");
            sb.append(Integer.toString(ligature));
            sb.append("}");
            return sb.toString();
        }

    }

    /**
     * The <code>LigatureSet</code> class implements a set of  ligatures.
     */
    public static class LigatureSet {

        private final Ligature[] ligatures;                     // set of ligatures all of which share the first (implied) component
        private final int maxComponents;                        // maximum number of components (including first)

        /**
         * Instantiate a set of ligatures.
         * @param ligatures collection of ligatures
         */
        public LigatureSet(List<Ligature> ligatures) {
            this(ligatures.toArray(new Ligature [ ligatures.size() ]));
        }

        /**
         * Instantiate a set of ligatures.
         * @param ligatures array of ligatures
         */
        public LigatureSet(Ligature[] ligatures) {
            if (ligatures == null) {
                throw new AdvancedTypographicTableFormatException("invalid ligatures, must be non-null array");
            } else {
                this.ligatures = ligatures;
                int ncMax = -1;
                for (int i = 0, n = ligatures.length; i < n; i++) {
                    Ligature l = ligatures [ i ];
                    int nc = l.getNumComponents() + 1;
                    if (nc > ncMax) {
                        ncMax = nc;
                    }
                }
                maxComponents = ncMax;
            }
        }

        /** @return array of ligatures in this ligature set */
        public Ligature[] getLigatures() {
            return ligatures;
        }

        /** @return count of ligatures in this ligature set */
        public int getNumLigatures() {
            return ligatures.length;
        }

        /** @return maximum number of components in one ligature (including first component) */
        public int getMaxComponents() {
            return maxComponents;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ligs={");
            for (int i = 0, n = ligatures.length; i < n; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(ligatures[i]);
            }
            sb.append("}}");
            return sb.toString();
        }

    }

}

