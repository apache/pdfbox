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

/* $Id$ */

package org.apache.fontbox.ttf.advanced.scripts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.fontbox.ttf.advanced.AdvancedTypographicTable;
import org.apache.fontbox.ttf.advanced.GlyphDefinitionTable;
import org.apache.fontbox.ttf.advanced.GlyphPositioningTable;
import org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable;
import org.apache.fontbox.ttf.advanced.util.CharScript;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

// CSOFF: LineLengthCheck

/**
 * <p>Abstract script processor base class for which an implementation of the substitution and positioning methods
 * must be supplied.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
@SuppressWarnings("unchecked") 
public abstract class ScriptProcessor {

    private final String script;

    private final Map/*<AssembledLookupsKey,AdvancedTypographicTable.UseSpec[]>*/ assembledLookups;

    private static Map<String, ScriptProcessor> processors = new HashMap<String, ScriptProcessor>();

    /**
     * Instantiate a script processor.
     * @param script a script identifier
     */
    protected ScriptProcessor(String script) {
        if ((script == null) || (script.length() == 0)) {
            throw new IllegalArgumentException("script must be non-empty string");
        } else {
            this.script = script;
            this.assembledLookups = new HashMap/*<AssembledLookupsKey,AdvancedTypographicTable.UseSpec[]>*/();
        }
    }

    /** @return script identifier */
    public final String getScript() {
        return script;
    }

    /**
     * Obtain script specific required substitution features.
     * @return array of suppported substitution features or null
     */
    public abstract String[] getSubstitutionFeatures(Object[][] features);

    /**
     * Obtain script specific optional substitution features.
     * @return array of suppported substitution features or null
     */
    public String[] getOptionalSubstitutionFeatures() {
        return new String[0];
    }

    /**
     * Obtain script specific substitution context tester.
     * @return substitution context tester or null
     */
    public abstract ScriptContextTester getSubstitutionContextTester();

    /**
     * Perform substitution processing using a specific set of lookup tables.
     * @param gsub the glyph substitution table that applies
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param lookups a mapping from lookup specifications to glyph subtables to use for substitution processing
     * @return the substituted (output) glyph sequence
     */
    public final GlyphSequence
        substitute(GlyphSubstitutionTable gsub, GlyphSequence gs, String script, String language, Object[][] features, Map/*<LookupSpec,List<LookupTable>>>*/ lookups) {
        return substitute(gs, script, language, assembleLookups(gsub, getSubstitutionFeatures(features), lookups), getSubstitutionContextTester());
    }

    /**
     * Perform substitution processing using a specific set of ordered glyph table use specifications.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param usa an ordered array of glyph table use specs
     * @param sct a script specific context tester (or null)
     * @return the substituted (output) glyph sequence
     */
    public GlyphSequence substitute(GlyphSequence gs, String script, String language, AdvancedTypographicTable.UseSpec[] usa, ScriptContextTester sct) {
        assert usa != null;
        for (int i = 0, n = usa.length; i < n; i++) {
            AdvancedTypographicTable.UseSpec us = usa [ i ];
            gs = us.substitute(gs, script, language, sct);
        }
        return gs;
    }

    /**
     * Reorder combining marks in glyph sequence so that they precede (within the sequence) the base
     * character to which they are applied. N.B. In the case of RTL segments, marks are not reordered by this,
     * method since when the segment is reversed by BIDI processing, marks are automatically reordered to precede
     * their base glyph.
     * @param gdef the glyph definition table that applies
     * @param gs an input glyph sequence
     * @param unscaledWidths associated unscaled advance widths (also reordered)
     * @param gpa associated glyph position adjustments (also reordered)
     * @param script a script identifier
     * @param language a language identifier
     * @return the reordered (output) glyph sequence
     */
    public GlyphSequence
        reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language, Object[][] features) {
        return gs;
    }

    /**
     * Obtain script specific required positioning features.
     * @param features
     * @return array of suppported positioning features or null
     */
    public abstract String[] getPositioningFeatures(Object[][] features);

    /**
     * Obtain script specific optional positioning features.
     * @return array of suppported positioning features or null
     */
    public String[] getOptionalPositioningFeatures() {
        return new String[0];
    }

    /**
     * Obtain script specific positioning context tester.
     * @return positioning context tester or null
     */
    public abstract ScriptContextTester getPositioningContextTester();

    /**
     * Perform positioning processing using a specific set of lookup tables.
     * @param gpos the glyph positioning table that applies
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param fontSize size in device units
     * @param lookups a mapping from lookup specifications to glyph subtables to use for positioning processing
     * @param widths array of default advancements for each glyph
     * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
     * with one 4-tuple for each element of glyph sequence
     * @return true if some adjustment is not zero; otherwise, false
     */
    public final boolean position(GlyphPositioningTable gpos, GlyphSequence gs, String script, String language, Object[][] features, int fontSize, Map/*<LookupSpec,List<LookupTable>>*/ lookups, int[] widths, int[][] adjustments) {
        return position(gs, script, language, fontSize, assembleLookups(gpos, getPositioningFeatures(features), lookups), widths, adjustments, getPositioningContextTester());
    }

    /**
     * Perform positioning processing using a specific set of ordered glyph table use specifications.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param fontSize size in device units
     * @param usa an ordered array of glyph table use specs
     * @param widths array of default advancements for each glyph in font
     * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
     * with one 4-tuple for each element of glyph sequence
     * @param sct a script specific context tester (or null)
     * @return true if some adjustment is not zero; otherwise, false
     */
    public boolean position(GlyphSequence gs, String script, String language, int fontSize, AdvancedTypographicTable.UseSpec[] usa, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        assert usa != null;
        boolean adjusted = false;
        for (int i = 0, n = usa.length; i < n; i++) {
            AdvancedTypographicTable.UseSpec us = usa [ i ];
            if (us.position(gs, script, language, fontSize, widths, adjustments, sct)) {
                adjusted = true;
            }
        }
        return adjusted;
    }

    /**
     * Assemble ordered array of lookup table use specifications according to the specified features and candidate lookups,
     * where the order of the array is in accordance to the order of the applicable lookup list.
     * @param table the governing glyph table
     * @param features array of feature identifiers to apply
     * @param lookups a mapping from lookup specifications to lists of look tables from which to select lookup tables according to the specified features
     * @return ordered array of assembled lookup table use specifications
     */
    public final AdvancedTypographicTable.UseSpec[] assembleLookups(AdvancedTypographicTable table, String[] features, Map/*<LookupSpec,List<LookupTable>>*/ lookups) {
        AssembledLookupsKey key = new AssembledLookupsKey(table, features, lookups);
        AdvancedTypographicTable.UseSpec[] usa;
        if ((usa = assembledLookupsGet(key)) != null) {
            return usa;
        } else {
            return assembledLookupsPut(key, table.assembleLookups(features, lookups));
        }
    }

    private AdvancedTypographicTable.UseSpec[] assembledLookupsGet(AssembledLookupsKey key) {
        return (AdvancedTypographicTable.UseSpec[]) assembledLookups.get(key);
    }

    private AdvancedTypographicTable.UseSpec[]  assembledLookupsPut(AssembledLookupsKey key, AdvancedTypographicTable.UseSpec[] usa) {
        assembledLookups.put(key, usa);
        return usa;
    }

    /**
     * Obtain script processor instance associated with specified script.
     * @param script a script identifier
     * @return a script processor instance or null if none found
     */
    public static synchronized ScriptProcessor getInstance(String script) {
        ScriptProcessor sp = null;
        assert processors != null;
        if ((sp = processors.get(script)) == null) {
            processors.put(script, sp = createProcessor(script));
        }
        return sp;
    }

    // [TBD] - rework to provide more configurable binding between script name and script processor constructor
    private static ScriptProcessor createProcessor(String script) {
        ScriptProcessor sp = null;
        int sc = CharScript.scriptCodeFromTag(script);
        if (sc == CharScript.SCRIPT_ARABIC) {
            sp = new ArabicScriptProcessor(script);
        } else if (CharScript.isIndicScript(sc)) {
            sp = IndicScriptProcessor.makeProcessor(script);
        } else {
            sp = new DefaultScriptProcessor(script);
        }
        return sp;
    }

    private static class AssembledLookupsKey {

        private final AdvancedTypographicTable table;
        private final String[] features;
        private final Map/*<LookupSpec,List<LookupTable>>*/ lookups;

        AssembledLookupsKey(AdvancedTypographicTable table, String[] features, Map/*<LookupSpec,List<LookupTable>>*/ lookups) {
            this.table = table;
            this.features = features;
            this.lookups = lookups;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            int hc = 0;
            hc =  7 * hc + (hc ^ table.hashCode());
            hc = 11 * hc + (hc ^ Arrays.hashCode(features));
            hc = 17 * hc + (hc ^ lookups.hashCode());
            return hc;
        }

        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (o instanceof AssembledLookupsKey) {
                AssembledLookupsKey k = (AssembledLookupsKey) o;
                if (!table.equals(k.table)) {
                    return false;
                } else if (!Arrays.equals(features, k.features)) {
                    return false;
                } else {
                    return lookups.equals(k.lookups);
                }
            } else {
                return false;
            }
        }

    }

}
