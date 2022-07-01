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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.TTFTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

import static org.apache.fontbox.ttf.advanced.util.AdvancedChecker.*;

/**
 * <p>Base class for all advanced typographic glyph tables.</p>
 *
 * <p>Adapted from the Apache FOP Project.</p>
 *
 * @author Glenn Adams
 */
public class AdvancedTypographicTable extends TTFTable {

    /** logging instance */
    private static final Log log = LogFactory.getLog(AdvancedTypographicTable.class);

    /** substitution glyph table type */
    public static final int GLYPH_TABLE_TYPE_SUBSTITUTION = 1;
    /** positioning glyph table type */
    public static final int GLYPH_TABLE_TYPE_POSITIONING = 2;
    /** justification glyph table type */
    public static final int GLYPH_TABLE_TYPE_JUSTIFICATION = 3;
    /** baseline glyph table type */
    public static final int GLYPH_TABLE_TYPE_BASELINE = 4;
    /** definition glyph table type */
    public static final int GLYPH_TABLE_TYPE_DEFINITION = 5;

    // (optional) glyph definition table in table types other than glyph definition table
    private AdvancedTypographicTable gdef;

    // map from lookup specs to lists of strings, each of which identifies a lookup table (consisting of one or more subtables)
    private Map<LookupSpec, List<String>> lookups;

    // map from lookup identifiers to lookup tables
    private Map<String, LookupTable> lookupTables;

    // cache for lookups matching
    private Map<LookupSpec, Map<LookupSpec, List<LookupTable>>> matchedLookups;

    // if true, then prevent further subtable addition
    private boolean frozen;

    /**
     * Instantiate glyph table with specified lookups.
     * @param gdef glyph definition table that applies
     * @param lookups map from lookup specs to lookup tables
     */
    public AdvancedTypographicTable(TrueTypeFont ttf, AdvancedTypographicTable gdef, Map<LookupSpec,List<String>> lookups) {
        super(ttf);
        if ((gdef != null) && !(gdef instanceof GlyphDefinitionTable)) {
            throw new AdvancedTypographicTableFormatException("bad glyph definition table");
        } else if (lookups == null) {
            throw new AdvancedTypographicTableFormatException("lookups must be non-null map");
        } else {
            this.gdef = gdef;
            this.lookups = lookups;
            this.lookupTables = new LinkedHashMap<String, LookupTable>();
            this.matchedLookups = new HashMap<LookupSpec, Map<LookupSpec, List<LookupTable>>>();
        }
    }

    protected void initialize(Map<LookupSpec, List<String>> lookups) {
        this.lookups = lookups;
    }

    /**
     * Obtain glyph definition table.
     * @return (possibly null) glyph definition table
     */
    public GlyphDefinitionTable getGlyphDefinitions() {
        return (GlyphDefinitionTable) gdef;
    }

    /**
     * Set glyph definition table
     * @param gdef
     */
    public void setGdef(GlyphDefinitionTable gdef) {
        this.gdef = gdef;
    }


    /**
     * Obtain list of all lookup specifications.
     * @return (possibly empty) list of all lookup specifications
     */
    public List<LookupSpec> getLookups() {
        return matchLookupSpecs("*", "*", "*");
    }

    /**
     * Obtain ordered list of all lookup tables, where order is by lookup identifier, which
     * lexicographic ordering follows the lookup list order.
     * @return (possibly empty) ordered list of all lookup tables
     */
    public List<LookupTable> getLookupTables() {
        TreeSet<String> lids = new TreeSet<>(lookupTables.keySet());
        List<LookupTable> ltl = new ArrayList<LookupTable>(lids.size());
        lids.forEach(lid -> ltl.add(lookupTables.get(lid)));
        return ltl;
    }

    /**
     * Obtain lookup table by lookup id. This method is used by test code, and provides
     * access to embedded lookups not normally accessed by {script, language, feature} lookup spec.
     * @param lid lookup id
     * @return table associated with lookup id or null if none
     */
    public LookupTable getLookupTable(String lid) {
        return lookupTables.get(lid);
    }

    /**
     * Add a subtable.
     * @param subtable a (non-null) glyph subtable
     */
    protected void addSubtable(GlyphSubtable subtable) {
        // ensure table is not frozen
        if (frozen) {
            throw new IllegalStateException("glyph table is frozen, subtable addition prohibited");
        }
        // set subtable's table reference to this table
        subtable.setTable(this);
        // add subtable to this table's subtable collection
        String lid = subtable.getLookupId();
        if (lookupTables.containsKey(lid)) {
            LookupTable lt = lookupTables.get(lid);
            lt.addSubtable(subtable);
        } else {
            LookupTable lt = new LookupTable(lid, subtable);
            lookupTables.put(lid, lt);
        }
    }

    /**
     * Freeze subtables, i.e., do not allow further subtable addition, and
     * create resulting cached state.
     */
    protected void freezeSubtables() {
        if (!frozen) {
            lookupTables.values().forEach(lt -> lt.freezeSubtables(lookupTables));
            frozen = true;
        }
    }

    /**
     * Match lookup specifications according to &lt;script,language,feature&gt; tuple, where
     * '*' is a wildcard for a tuple component.
     * @param script a script identifier
     * @param language a language identifier
     * @param feature a feature identifier
     * @return a (possibly empty) array of matching lookup specifications
     */
    public List<LookupSpec> matchLookupSpecs(String script, String language, String feature) {
        Set<LookupSpec> keys = lookups.keySet();
        List<LookupSpec> matches = new ArrayList<>();
        for (LookupSpec ls : keys) {
            if (!"*".equals(script)) {
                if (!ls.getScript().equals(script)) {
                    continue;
                }
            }
            if (!"*".equals(language)) {
                if (!ls.getLanguage().equals(language)) {
                    continue;
                }
            }
            if (!"*".equals(feature)) {
                if (!ls.getFeature().equals(feature)) {
                    continue;
                }
            }
            matches.add(ls);
        }
        return matches;
    }

    /**
     * Match lookup specifications according to &lt;script,language,feature&gt; tuple, where
     * '*' is a wildcard for a tuple component.
     * @param script a script identifier
     * @param language a language identifier
     * @param feature a feature identifier
     * @return a (possibly empty) map from matching lookup specifications to lists of corresponding lookup tables
     */
    public Map<LookupSpec, List<LookupTable>> matchLookups(String script, String language, String feature) {
        LookupSpec lsm = new LookupSpec(script, language, feature, true, true);
        Map<LookupSpec, List<LookupTable>> lm = matchedLookups.get(lsm);
        if (lm == null) {
            lm = new LinkedHashMap<>();
            List<LookupSpec> lsl = matchLookupSpecs(script, language, feature);
            for (LookupSpec ls : lsl) {
                lm.put(ls, findLookupTables(ls));
            }
            matchedLookups.put(lsm, lm);
        }
        if (lm.isEmpty() && !OTFScript.isDefault(script) && !OTFScript.isWildCard(script)) {
            return matchLookups(OTFScript.DEFAULT, OTFLanguage.DEFAULT, feature);
        } else {
            return lm;
        }
    }

    /**
     * Obtain ordered list of glyph lookup tables that match a specific lookup specification.
     * @param ls a (non-null) lookup specification
     * @return a (possibly empty) ordered list of lookup tables whose corresponding lookup specifications match the specified lookup spec
     */
    public List<LookupTable> findLookupTables(LookupSpec ls) {
        TreeSet<LookupTable> lts = new TreeSet<>();
        List<String> ids = lookups.get(ls);
        transformConsume(ids, lookupTables::get, lts::add);
        return new ArrayList<>(lts);
    }

    /**
     * Assemble ordered array of lookup table use specifications according to the specified features and candidate lookups,
     * where the order of the array is in accordance to the order of the applicable lookup list.
     * @param features array of feature identifiers to apply
     * @param lookups a mapping from lookup specifications to lists of look tables from which to select lookup tables according to the specified features
     * @return ordered array of assembled lookup table use specifications
     */
    public UseSpec[] assembleLookups(String[] features, Map<LookupSpec, List<LookupTable>> lookups) {
        TreeSet<UseSpec> uss = new TreeSet<>();
        for (int i = 0, n = features.length; i < n; i++) {
            String feature = features[i];
            for (Map.Entry<LookupSpec, List<LookupTable>> e : lookups.entrySet()) {
                LookupSpec ls = e.getKey();
                if (ls.getFeature().equals(feature)) {
                    List<LookupTable> ltl = e.getValue();
                    if (ltl != null) {
                        ltl.forEach(lt -> uss.add(new UseSpec(lt, feature)));
                    }
                }
            }
        }
        return uss.toArray(new UseSpec [ uss.size() ]);
    }

    /**
     * Determine if table supports specific feature, i.e., supports at least one lookup.
     *
     * @param script to qualify feature lookup
     * @param language to qualify feature lookup
     * @param feature to test
     * @return true if feature supported (has at least one lookup)
     */
    public boolean hasFeature(String script, String language, String feature) {
        UseSpec[] usa = assembleLookups(new String[] { feature }, matchLookups(script, language, feature));
        return usa.length > 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("{");
        sb.append("lookups={");
        sb.append(lookups.toString());
        sb.append("},lookupTables={");
        sb.append(lookupTables.toString());
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Obtain glyph table type from name.
     * @param name of table type to map to type value
     * @return glyph table type (as an integer constant) or -1
     */
    public static int getTableTypeFromName(String name) {
        int t;
        String s = name.toLowerCase(Locale.ROOT);
        if ("gsub".equals(s)) {
            t = GLYPH_TABLE_TYPE_SUBSTITUTION;
        } else if ("gpos".equals(s)) {
            t = GLYPH_TABLE_TYPE_POSITIONING;
        } else if ("jstf".equals(s)) {
            t = GLYPH_TABLE_TYPE_JUSTIFICATION;
        } else if ("base".equals(s)) {
            t = GLYPH_TABLE_TYPE_BASELINE;
        } else if ("gdef".equals(s)) {
            t = GLYPH_TABLE_TYPE_DEFINITION;
        } else {
            t = -1;
        }
        return t;
    }

    /**
     * Resolve references to lookup tables in a collection of rules sets.
     * @param rsa array of rule sets
     * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
     */
    public static void resolveLookupReferences(RuleSet[] rsa, Map<String, LookupTable> lookupTables) {
        if ((rsa != null) && (lookupTables != null)) {
            for (int i = 0, n = rsa.length; i < n; i++) {
                RuleSet rs = rsa [ i ];
                if (rs != null) {
                    rs.resolveLookupReferences(lookupTables);
                }
            }
        }
    }

    /**
     * A structure class encapsulating a lookup specification as a &lt;script,language,feature&gt; tuple.
     */
    public static class LookupSpec implements Comparable<LookupSpec>{

        private final String script;
        private final String language;
        private final String feature;

        /**
         * Instantiate lookup spec.
         * @param script a script identifier
         * @param language a language identifier
         * @param feature a feature identifier
         */
        public LookupSpec(String script, String language, String feature) {
            this (script, language, feature, false, false);
        }

        /**
         * Instantiate lookup spec.
         * @param script a script identifier
         * @param language a language identifier
         * @param feature a feature identifier
         * @param permitEmpty if true then permit empty script, language, or feature
         * @param permitWildcard if true then permit wildcard script, language, or feature
         */
        LookupSpec(String script, String language, String feature, boolean permitEmpty, boolean permitWildcard) {
            if ((script == null) || (!permitEmpty && (script.length() == 0))) {
                throw new AdvancedTypographicTableFormatException("script must be non-empty string");
            } else if ((language == null) || (!permitEmpty && (language.length() == 0))) {
                throw new AdvancedTypographicTableFormatException("language must be non-empty string");
            } else if ((feature == null) || (!permitEmpty && (feature.length() == 0))) {
                throw new AdvancedTypographicTableFormatException("feature must be non-empty string");
            } else if (!permitWildcard && script.equals("*")) {
                throw new AdvancedTypographicTableFormatException("script must not be wildcard");
            } else if (!permitWildcard && language.equals("*")) {
                throw new AdvancedTypographicTableFormatException("language must not be wildcard");
            } else if (!permitWildcard && feature.equals("*")) {
                throw new AdvancedTypographicTableFormatException("feature must not be wildcard");
            }
            this.script = script.trim();
            this.language = language.trim();
            this.feature = feature.trim();
        }

        /** @return script identifier */
        public String getScript() {
            return script;
        }

        /** @return language identifier */
        public String getLanguage() {
            return language;
        }

        /** @return feature identifier  */
        public String getFeature() {
            return feature;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(script, language, feature);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            if (o instanceof LookupSpec) {
                LookupSpec l = (LookupSpec) o;
                if (!l.script.equals(script)) {
                    return false;
                } else if (!l.language.equals(language)) {
                    return false;
                } else {
                    return l.feature.equals(feature);
                }
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(LookupSpec ls) {
            int d;
            if ((d = script.compareTo(ls.script)) == 0) {
                if ((d = language.compareTo(ls.language)) == 0) {
                    if ((d = feature.compareTo(ls.feature)) == 0) {
                        d = 0;
                    }
                }
            }
            return d;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append("{");
            sb.append("<'" + script + "'");
            sb.append(",'" + language + "'");
            sb.append(",'" + feature + "'");
            sb.append(">}");
            return sb.toString();
        }

    }

    /**
     * The <code>LookupTable</code> class comprising an identifier and an ordered list
     * of glyph subtables, each of which employ the same lookup identifier.
     */
    public static class LookupTable implements Comparable<LookupTable> {

        private final String id;                                // lookup identifier
        private final int idOrdinal;                            // parsed lookup identifier ordinal
        private final List<GlyphSubtable> subtables;            // list of subtables
        private boolean doesSub;                                // performs substitutions
        private boolean doesPos;                                // performs positioning
        private boolean frozen;                                 // if true, then don't permit further subtable additions
        // frozen state
        private GlyphSubtable[] subtablesArray;
        private static final GlyphSubtable[] EMPTY_SUBTABLES_ARRAY       = new GlyphSubtable[0];

        /**
         * Instantiate a LookupTable.
         * @param id the lookup table's identifier
         * @param subtable an initial subtable (or null)
         */
        public LookupTable(String id, GlyphSubtable subtable) {
            this (id, makeSingleton(subtable));
        }

        /**
         * Instantiate a LookupTable.
         * @param id the lookup table's identifier
         * @param subtables a pre-poplated list of subtables or null
         */
        public LookupTable(String id, List<GlyphSubtable> subtables) {
            assert id != null;
            assert id.length() != 0;
            assert id.startsWith("lu");
            this.id = id;
            this.idOrdinal = Integer.parseInt(id.substring(2));
            this.subtables = new ArrayList<GlyphSubtable>();
            if (subtables != null) {
                subtables.forEach(this::addSubtable);
            }
        }

        /** @return the subtables as an array */
        public GlyphSubtable[] getSubtables() {
            if (frozen) {
                return (subtablesArray != null) ? subtablesArray : EMPTY_SUBTABLES_ARRAY;
            } else {
                if (doesSub) {
                    return subtables.toArray(new GlyphSubstitutionSubtable [ subtables.size() ]);
                } else if (doesPos) {
                    return subtables.toArray(new GlyphPositioningSubtable [ subtables.size() ]);
                } else {
                    return null;
                }
            }
        }

        /**
         * Add a subtable into this lookup table's collecion of subtables according to its
         * natural order.
         * @param subtable to add
         * @return true if subtable was not already present, otherwise false
         */
        public boolean addSubtable(GlyphSubtable subtable) {
            boolean added;
            // ensure table is not frozen
            if (frozen) {
                throw new IllegalStateException("glyph table is frozen, subtable addition prohibited");
            }
            // validate subtable to ensure consistency with current subtables
            validateSubtable(subtable);

            // insert subtable into ordered list
            int insertIdx = -1;
            for (int i = 0; i < subtables.size(); i++) {
                GlyphSubtable st = subtables.get(i);
                int compareResult = subtable.compareTo(st);
                if (compareResult < 0) {
                    // insert before i
                    insertIdx = i;
                    break;
                } else if (compareResult == 0) {
                    // duplicate entry is ignored
                    insertIdx = -2;
                    break;
                }
            }

            if (insertIdx >= 0) {
                subtables.add(insertIdx, subtable);
                added = true;
            } else if (insertIdx == -1) {
                // append at end of list
                subtables.add(subtable);
                added = true;
            } else {
                // duplicate
                added = false;
            }

            return added;
        }

        private void validateSubtable(GlyphSubtable subtable) {
            if (subtable == null) {
                throw new AdvancedTypographicTableFormatException("subtable must be non-null");
            }
            if (subtable instanceof GlyphSubstitutionSubtable) {
                if (doesPos) {
                    throw new AdvancedTypographicTableFormatException("subtable must be positioning subtable, but is: " + subtable);
                } else {
                    doesSub = true;
                }
            }
            if (subtable instanceof GlyphPositioningSubtable) {
                if (doesSub) {
                    throw new AdvancedTypographicTableFormatException("subtable must be substitution subtable, but is: " + subtable);
                } else {
                    doesPos = true;
                }
            }
            if (!subtables.isEmpty()) {
                GlyphSubtable st = subtables.get(0);
                if (!st.isCompatible(subtable)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding " + st.getClass().getSimpleName() + " to existing: " + toClassString(subtables, "[", "]"));
                    }
                    // FIXME
                    //throw new AdvancedTypographicTableFormatException("subtable " + subtable + " is not compatible with subtable " + st);
                }
            }
        }

        /**
         * Freeze subtables, i.e., do not allow further subtable addition, and
         * create resulting cached state. In addition, resolve any references to
         * lookup tables that appear in this lookup table's subtables.
         * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
         */
        public void freezeSubtables(Map<String, LookupTable> lookupTables) {
            if (!frozen) {
                GlyphSubtable[] sta = getSubtables();
                resolveLookupReferences(sta, lookupTables);
                this.subtablesArray = sta;
                this.frozen = true;
            }
        }

        private void resolveLookupReferences(GlyphSubtable[] subtables, Map<String, LookupTable> lookupTables) {
            if (subtables != null) {
                for (int i = 0, n = subtables.length; i < n; i++) {
                    GlyphSubtable st = subtables [ i ];
                    if (st != null) {
                        st.resolveLookupReferences(lookupTables);
                    }
                }
            }
        }

        /**
         * Determine if this glyph table performs substitution.
         * @return true if it performs substitution
         */
        public boolean performsSubstitution() {
            return doesSub;
        }

        /**
         * Perform substitution processing using this lookup table's subtables.
         * @param gs an input glyph sequence
         * @param script a script identifier
         * @param language a language identifier
         * @param feature a feature identifier
         * @param sct a script specific context tester (or null)
         * @return the substituted (output) glyph sequence
         */
        public GlyphSequence substitute(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
            if (performsSubstitution()) {
                return GlyphSubstitutionSubtable.substitute(gs, script, language, feature, (GlyphSubstitutionSubtable[]) subtablesArray, sct);
            } else {
                return gs;
            }
        }

        /**
         * Perform substitution processing on an existing glyph substitution state object using this lookup table's subtables.
         * @param ss a glyph substitution state object
         * @param sequenceIndex if non negative, then apply subtables only at specified sequence index
         * @return the substituted (output) glyph sequence
         */
        public GlyphSequence substitute(GlyphSubstitutionState ss, int sequenceIndex) {
            if (performsSubstitution()) {
                return GlyphSubstitutionSubtable.substitute(ss, (GlyphSubstitutionSubtable[]) subtablesArray, sequenceIndex);
            } else {
                return ss.getInput();
            }
        }

        /**
         * Determine if this glyph table performs positioning.
         * @return true if it performs positioning
         */
        public boolean performsPositioning() {
            return doesPos;
        }

        /**
         * Perform positioning processing using this lookup table's subtables.
         * @param gs an input glyph sequence
         * @param script a script identifier
         * @param language a language identifier
         * @param feature a feature identifier
         * @param fontSize size in device units
         * @param widths array of default advancements for each glyph in font
         * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
         * with one 4-tuple for each element of glyph sequence
         * @param sct a script specific context tester (or null)
         * @return true if some adjustment is not zero; otherwise, false
         */
        public boolean position(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
            if (performsPositioning()) {
                return GlyphPositioningSubtable.position(gs, script, language, feature, fontSize, (GlyphPositioningSubtable[]) subtablesArray, widths, adjustments, sct);
            } else {
                return false;
            }
        }

        /**
         * Perform positioning processing on an existing glyph positioning state object using this lookup table's subtables.
         * @param ps a glyph positioning state object
         * @param sequenceIndex if non negative, then apply subtables only at specified sequence index
         * @return true if some adjustment is not zero; otherwise, false
         */
        public boolean position(GlyphPositioningState ps, int sequenceIndex) {
            if (performsPositioning()) {
                return GlyphPositioningSubtable.position(ps, (GlyphPositioningSubtable[]) subtablesArray, sequenceIndex);
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return idOrdinal;
        }

        /**
         * {@inheritDoc}
         * @return true if identifier of the specified lookup table is the same
         * as the identifier of this lookup table
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof LookupTable) {
                LookupTable lt = (LookupTable) o;
                return idOrdinal == lt.idOrdinal;
            } else {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         * @return the result of comparing the identifier of the specified lookup table with
         * the identifier of this lookup table; lookup table identifiers take the form
         * "lu(DIGIT)+", with comparison based on numerical ordering of numbers expressed by
         * (DIGIT)+.
         */
        @Override
        public int compareTo(LookupTable lt) {
            int i = idOrdinal;
            int j = lt.idOrdinal;
            if (i < j) {
                return -1;
            } else if (i > j) {
                return 1;
            } else {
                return 0;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("id = " + id);
            sb.append(", subtables = " + subtables);
            sb.append(" }");
            return sb.toString();
        }

        private static List<GlyphSubtable> makeSingleton(GlyphSubtable subtable) {
            if (subtable == null) {
                return null;
            } else {
                return mutableSingleton(subtable);
            }
        }

    }

    /**
     * The <code>UseSpec</code> class comprises a lookup table reference
     * and the feature that selected the lookup table.
     */
    public static class UseSpec implements Comparable<UseSpec> {

        /** lookup table to apply */
        private final LookupTable lookupTable;
        /** feature that caused selection of the lookup table */
        private final String feature;

        /**
         * Construct a glyph lookup table use specification.
         * @param lookupTable a glyph lookup table
         * @param feature a feature that caused lookup table selection
         */
        public UseSpec(LookupTable lookupTable, String feature) {
            this.lookupTable = lookupTable;
            this.feature = feature;
        }

        /** @return the lookup table */
        public LookupTable getLookupTable() {
            return lookupTable;
        }

        /** @return the feature that selected this lookup table */
        public String getFeature() {
            return feature;
        }

        /**
         * Perform substitution processing using this use specification's lookup table.
         * @param gs an input glyph sequence
         * @param script a script identifier
         * @param language a language identifier
         * @param sct a script specific context tester (or null)
         * @return the substituted (output) glyph sequence
         */
        public GlyphSequence substitute(GlyphSequence gs, String script, String language, ScriptContextTester sct) {
            return lookupTable.substitute(gs, script, language, feature, sct);
        }

        /**
         * Perform positioning processing using this use specification's lookup table.
         * @param gs an input glyph sequence
         * @param script a script identifier
         * @param language a language identifier
         * @param fontSize size in device units
         * @param widths array of default advancements for each glyph in font
         * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
         * with one 4-tuple for each element of glyph sequence
         * @param sct a script specific context tester (or null)
         * @return true if some adjustment is not zero; otherwise, false
         */
        public boolean position(GlyphSequence gs, String script, String language, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
            return lookupTable.position(gs, script, language, feature, fontSize, widths, adjustments, sct);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return lookupTable.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            if (o instanceof UseSpec) {
                UseSpec u = (UseSpec) o;
                return lookupTable.equals(u.lookupTable);
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(UseSpec u) {
            return lookupTable.compareTo(u.lookupTable);
        }

    }

    /**
     * The <code>RuleLookup</code> class implements a rule lookup record, comprising
     * a glyph sequence index and a lookup table index (in an applicable lookup list).
     */
    public static class RuleLookup {

        private final int sequenceIndex;                        // index into input glyph sequence
        private final int lookupIndex;                          // lookup list index
        private LookupTable lookup;                             // resolved lookup table

        /**
         * Instantiate a RuleLookup.
         * @param sequenceIndex the index into the input sequence
         * @param lookupIndex the lookup table index
         */
        public RuleLookup(int sequenceIndex, int lookupIndex) {
            this.sequenceIndex = sequenceIndex;
            this.lookupIndex = lookupIndex;
            this.lookup = null;
        }

        /** @return the sequence index */
        public int getSequenceIndex() {
            return sequenceIndex;
        }

        /** @return the lookup index */
        public int getLookupIndex() {
            return lookupIndex;
        }

        /** @return the lookup table */
        public LookupTable getLookup() {
            return lookup;
        }

        /**
         * Resolve references to lookup tables.
         * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
         */
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            if (lookupTables != null) {
                String lid = "lu" + Integer.toString(lookupIndex);
                LookupTable lt = lookupTables.get(lid);
                if (lt != null) {
                    this.lookup = lt;
                } else {
                    log.warn("unable to resolve glyph lookup table reference '" + lid + "' amongst lookup tables: " + lookupTables.values());
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "{ sequenceIndex = " + sequenceIndex + ", lookupIndex = " + lookupIndex + " }";
        }

    }

    /**
     * The <code>Rule</code> class implements an array of rule lookup records.
     */
    public abstract static class Rule {

        private final RuleLookup[] lookups;                     // rule lookups
        private final int inputSequenceLength;                  // input sequence length

        /**
         * Instantiate a Rule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength the number of glyphs in the input sequence for this rule
         */
        protected Rule(RuleLookup[] lookups, int inputSequenceLength) {
            assert lookups != null;
            this.lookups = lookups;
            this.inputSequenceLength = inputSequenceLength;
        }

        /** @return the lookups */
        public RuleLookup[] getLookups() {
            return lookups;
        }

        /** @return the input sequence length */
        public int getInputSequenceLength() {
            return inputSequenceLength;
        }

        /**
         * Resolve references to lookup tables, e.g., in RuleLookup, to the lookup tables themselves.
         * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
         */
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            if (lookups != null) {
                for (int i = 0; i < lookups.length; i++) {
                    if (lookups[i] != null) {
                        lookups[i].resolveLookupReferences(lookupTables);
                    }
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "{ lookups = " + Arrays.toString(lookups) + ", inputSequenceLength = " + inputSequenceLength + " }";
        }

    }

    /**
     * The <code>GlyphSequenceRule</code> class implements a subclass of <code>Rule</code>
     * that supports matching on a specific glyph sequence.
     */
    public static class GlyphSequenceRule extends Rule {

        private final int[] glyphs;                             // glyphs

        /**
         * Instantiate a GlyphSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param glyphs the rule's glyph sequence to match, starting with second glyph in sequence
         */
        public GlyphSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] glyphs) {
            super(lookups, inputSequenceLength);
            assert glyphs != null;
            this.glyphs = glyphs;
        }

        /**
         * Obtain glyphs. N.B. that this array starts with the second
         * glyph of the input sequence.
         * @return the glyphs
         */
        public int[] getGlyphs() {
            return glyphs;
        }

        /**
         * Obtain glyphs augmented by specified first glyph entry.
         * @param firstGlyph to fill in first glyph entry
         * @return the glyphs augmented by first glyph
         */
        public int[] getGlyphs(int firstGlyph) {
            int[] ga = new int [ glyphs.length + 1 ];
            ga [ 0 ] = firstGlyph;
            System.arraycopy(glyphs, 0, ga, 1, glyphs.length);
            return ga;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", glyphs = " + Arrays.toString(glyphs));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>ClassSequenceRule</code> class implements a subclass of <code>Rule</code>
     * that supports matching on a specific glyph class sequence.
     */
    public static class ClassSequenceRule extends Rule {

        private final int[] classes;                            // glyph classes

        /**
         * Instantiate a ClassSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param classes the rule's glyph class sequence to match, starting with second glyph in sequence
         */
        public ClassSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] classes) {
            super(lookups, inputSequenceLength);
            assert classes != null;
            this.classes = classes;
        }

        /**
         * Obtain glyph classes. N.B. that this array starts with the class of the second
         * glyph of the input sequence.
         * @return the classes
         */
        public int[] getClasses() {
            return classes;
        }

        /**
         * Obtain glyph classes augmented by specified first class entry.
         * @param firstClass to fill in first class entry
         * @return the classes augmented by first class
         */
        public int[] getClasses(int firstClass) {
            int[] ca = new int [ classes.length + 1 ];
            ca [ 0 ] = firstClass;
            System.arraycopy(classes, 0, ca, 1, classes.length);
            return ca;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", classes = " + Arrays.toString(classes));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>CoverageSequenceRule</code> class implements a subclass of <code>Rule</code>
     * that supports matching on a specific glyph coverage sequence.
     */
    public static class CoverageSequenceRule extends Rule {

        private final GlyphCoverageTable[] coverages;           // glyph coverages

        /**
         * Instantiate a ClassSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param coverages the rule's glyph coverage sequence to match, starting with first glyph in sequence
         */
        public CoverageSequenceRule(RuleLookup[] lookups, int inputSequenceLength, GlyphCoverageTable[] coverages) {
            super(lookups, inputSequenceLength);
            assert coverages != null;
            this.coverages = coverages;
        }

        /** @return the coverages */
        public GlyphCoverageTable[] getCoverages() {
            return coverages;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", coverages = " + Arrays.toString(coverages));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>ChainedGlyphSequenceRule</code> class implements a subclass of <code>GlyphSequenceRule</code>
     * that supports matching on a specific glyph sequence in a specific chained contextual.
     */
    public static class ChainedGlyphSequenceRule extends GlyphSequenceRule {

        private final int[] backtrackGlyphs;                    // backtrack glyphs
        private final int[] lookaheadGlyphs;                    // lookahead glyphs

        /**
         * Instantiate a ChainedGlyphSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param glyphs the rule's input glyph sequence to match, starting with second glyph in sequence
         * @param backtrackGlyphs the rule's backtrack glyph sequence to match, starting with first glyph in sequence
         * @param lookaheadGlyphs the rule's lookahead glyph sequence to match, starting with first glyph in sequence
         */
        public ChainedGlyphSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] glyphs, int[] backtrackGlyphs, int[] lookaheadGlyphs) {
            super(lookups, inputSequenceLength, glyphs);
            assert backtrackGlyphs != null;
            assert lookaheadGlyphs != null;
            this.backtrackGlyphs = backtrackGlyphs;
            this.lookaheadGlyphs = lookaheadGlyphs;
        }

        /** @return the backtrack glyphs */
        public int[] getBacktrackGlyphs() {
            return backtrackGlyphs;
        }

        /** @return the lookahead glyphs */
        public int[] getLookaheadGlyphs() {
            return lookaheadGlyphs;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", glyphs = " + Arrays.toString(getGlyphs()));
            sb.append(", backtrackGlyphs = " + Arrays.toString(backtrackGlyphs));
            sb.append(", lookaheadGlyphs = " + Arrays.toString(lookaheadGlyphs));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>ChainedClassSequenceRule</code> class implements a subclass of <code>ClassSequenceRule</code>
     * that supports matching on a specific glyph class sequence in a specific chained contextual.
     */
    public static class ChainedClassSequenceRule extends ClassSequenceRule {

        private final int[] backtrackClasses;                    // backtrack classes
        private final int[] lookaheadClasses;                    // lookahead classes

        /**
         * Instantiate a ChainedClassSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param classes the rule's input glyph class sequence to match, starting with second glyph in sequence
         * @param backtrackClasses the rule's backtrack glyph class sequence to match, starting with first glyph in sequence
         * @param lookaheadClasses the rule's lookahead glyph class sequence to match, starting with first glyph in sequence
         */
        public ChainedClassSequenceRule(RuleLookup[] lookups, int inputSequenceLength, int[] classes, int[] backtrackClasses, int[] lookaheadClasses) {
            super(lookups, inputSequenceLength, classes);
            assert backtrackClasses != null;
            assert lookaheadClasses != null;
            this.backtrackClasses = backtrackClasses;
            this.lookaheadClasses = lookaheadClasses;
        }

        /** @return the backtrack classes */
        public int[] getBacktrackClasses() {
            return backtrackClasses;
        }

        /** @return the lookahead classes */
        public int[] getLookaheadClasses() {
            return lookaheadClasses;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", classes = " + Arrays.toString(getClasses()));
            sb.append(", backtrackClasses = " + Arrays.toString(backtrackClasses));
            sb.append(", lookaheadClasses = " + Arrays.toString(lookaheadClasses));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>ChainedCoverageSequenceRule</code> class implements a subclass of <code>CoverageSequenceRule</code>
     * that supports matching on a specific glyph class sequence in a specific chained contextual.
     */
    public static class ChainedCoverageSequenceRule extends CoverageSequenceRule {

        private final GlyphCoverageTable[] backtrackCoverages;  // backtrack coverages
        private final GlyphCoverageTable[] lookaheadCoverages;  // lookahead coverages

        /**
         * Instantiate a ChainedCoverageSequenceRule.
         * @param lookups the rule's lookups
         * @param inputSequenceLength number of glyphs constituting input sequence (to be consumed)
         * @param coverages the rule's input glyph class sequence to match, starting with first glyph in sequence
         * @param backtrackCoverages the rule's backtrack glyph class sequence to match, starting with first glyph in sequence
         * @param lookaheadCoverages the rule's lookahead glyph class sequence to match, starting with first glyph in sequence
         */
        public ChainedCoverageSequenceRule(RuleLookup[] lookups, int inputSequenceLength, GlyphCoverageTable[] coverages, GlyphCoverageTable[] backtrackCoverages, GlyphCoverageTable[] lookaheadCoverages) {
            super(lookups, inputSequenceLength, coverages);
            assert backtrackCoverages != null;
            assert lookaheadCoverages != null;
            this.backtrackCoverages = backtrackCoverages;
            this.lookaheadCoverages = lookaheadCoverages;
        }

        /** @return the backtrack coverages */
        public GlyphCoverageTable[] getBacktrackCoverages() {
            return backtrackCoverages;
        }

        /** @return the lookahead coverages */
        public GlyphCoverageTable[] getLookaheadCoverages() {
            return lookaheadCoverages;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("lookups = " + Arrays.toString(getLookups()));
            sb.append(", coverages = " + Arrays.toString(getCoverages()));
            sb.append(", backtrackCoverages = " + Arrays.toString(backtrackCoverages));
            sb.append(", lookaheadCoverages = " + Arrays.toString(lookaheadCoverages));
            sb.append(" }");
            return sb.toString();
        }

    }

    /**
     * The <code>RuleSet</code> class implements a collection of rules, which
     * may or may not be the same rule type.
     */
    public static class RuleSet {

        private final Rule[] rules;                             // set of rules

        /**
         * Instantiate a Rule Set.
         * @param rules the rules
         * @throws AdvancedTypographicTableFormatException if rules or some element of rules is null
         */
        public RuleSet(Rule[] rules) throws AdvancedTypographicTableFormatException {
            // enforce rules array instance
            if (rules == null) {
                throw new AdvancedTypographicTableFormatException("rules[] is null");
            }
            this.rules = rules;
        }

        /** @return the rules */
        public Rule[] getRules() {
            return rules;
        }

        /**
         * Resolve references to lookup tables, e.g., in RuleLookup, to the lookup tables themselves.
         * @param lookupTables map from lookup table identifers, e.g. "lu4", to lookup tables
         */
        public void resolveLookupReferences(Map<String, LookupTable> lookupTables) {
            if (rules != null) {
                for (int i = 0; i < rules.length; i++) {
                    if (rules[i] != null) {
                        rules[i].resolveLookupReferences(lookupTables);
                    }
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "{ rules = " + Arrays.toString(rules) + " }";
        }

    }

    /**
     * The <code>HomogenousRuleSet</code> class implements a collection of rules, which
     * must be the same rule type (i.e., same concrete rule class) or null.
     */
    public static class HomogeneousRuleSet extends RuleSet {

        /**
         * Instantiate a Homogeneous Rule Set.
         * @param rules the rules
         * @throws AdvancedTypographicTableFormatException if some rule[i] is not an instance of rule[0]
         */
        public HomogeneousRuleSet(Rule[] rules) throws AdvancedTypographicTableFormatException {
            super(rules);
            // find first non-null rule
            Rule r0 = null;
            for (int i = 1, n = rules.length; (r0 == null) && (i < n); i++) {
                if (rules[i] != null) {
                    r0 = rules[i];
                }
            }
            // enforce rule instance homogeneity
            if (r0 != null) {
                Class<?> c = r0.getClass();
                for (int i = 1, n = rules.length; i < n; i++) {
                    Rule r = rules[i];
                    if ((r != null) && !c.isInstance(r)) {
                        throw new AdvancedTypographicTableFormatException("rules[" + i + "] is not an instance of " + c.getName());
                    }
                }
            }

        }

    }

}
