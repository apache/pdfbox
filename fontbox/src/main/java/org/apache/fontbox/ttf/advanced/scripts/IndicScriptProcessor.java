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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fontbox.ttf.advanced.AdvancedTypographicTable;
import org.apache.fontbox.ttf.advanced.util.CharAssociation;
import org.apache.fontbox.ttf.advanced.util.CharScript;
import org.apache.fontbox.ttf.advanced.util.GlyphContextTester;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

// CSOFF: LineLengthCheck

/**
 * <p>The <code>IndicScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Indic script.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class IndicScriptProcessor extends DefaultScriptProcessor {

    /** logging instance */
    private static final Log log = LogFactory.getLog(IndicScriptProcessor.class);

    /** required features to use for substitutions */
    private static final String[] GSUB_REQ_FEATURES =
    {
        "abvf",                                                 // above base forms
        "abvs",                                                 // above base substitutions
        "akhn",                                                 // akhand
        "blwf",                                                 // below base forms
        "blws",                                                 // below base substitutions
        "ccmp",                                                 // glyph composition/decomposition
        "cjct",                                                 // conjunct forms
        "clig",                                                 // contextual ligatures
        "half",                                                 // half forms
        "haln",                                                 // halant forms
        "locl",                                                 // localized forms
        "nukt",                                                 // nukta forms
        "pref",                                                 // pre-base forms
        "pres",                                                 // pre-base substitutions
        "pstf",                                                 // post-base forms
        "psts",                                                 // post-base substitutions
        "rkrf",                                                 // rakar forms
        "rphf",                                                 // reph form
        "vatu"                                                  // vattu variants
    };

    /** optional features to use for substitutions */
    private static final String[] GSUB_OPT_FEATURES =
    {
        "afrc",                                                 // alternative fractions
        "calt",                                                 // contextual alternatives
        "dlig"                                                  // discretionary ligatures
    };

    /** required features to use for positioning */
    private static final String[] GPOS_REQ_FEATURES =
    {
        "abvm",                                                 // above base marks
        "blwm",                                                 // below base marks
        "dist",                                                 // distance (adjustment)
        "kern"                                                  // kerning
    };

    /** required features to use for positioning */
    private static final String[] GPOS_OPT_FEATURES =
    {
    };

    private static class SubstitutionScriptContextTester implements ScriptContextTester {
        private static Map/*<String,GlyphContextTester>*/ testerMap = new HashMap/*<String,GlyphContextTester>*/();
        public GlyphContextTester getTester(String feature) {
            return (GlyphContextTester) testerMap.get(feature);
        }
    }

    private static class PositioningScriptContextTester implements ScriptContextTester {
        private static Map/*<String,GlyphContextTester>*/ testerMap = new HashMap/*<String,GlyphContextTester>*/();
        public GlyphContextTester getTester(String feature) {
            return (GlyphContextTester) testerMap.get(feature);
        }
    }

    /**
     * Make script specific flavor of Indic script processor.
     * @param script tag
     * @return script processor instance
     */
    public static ScriptProcessor makeProcessor(String script) {
        switch (CharScript.scriptCodeFromTag(script)) {
        case CharScript.SCRIPT_DEVANAGARI:
        case CharScript.SCRIPT_DEVANAGARI_2:
            return new DevanagariScriptProcessor(script);
        case CharScript.SCRIPT_GUJARATI:
        case CharScript.SCRIPT_GUJARATI_2:
            return new GujaratiScriptProcessor(script);
        case CharScript.SCRIPT_GURMUKHI:
        case CharScript.SCRIPT_GURMUKHI_2:
            return new GurmukhiScriptProcessor(script);
        case CharScript.SCRIPT_TAMIL:
        case CharScript.SCRIPT_TAMIL_2:
            return new TamilScriptProcessor(script);
        // [TBD] implement other script processors
        default:
            return new IndicScriptProcessor(script);
        }
    }

    private final ScriptContextTester subContextTester;
    private final ScriptContextTester posContextTester;

    IndicScriptProcessor(String script) {
        super(script);
        this.subContextTester = new SubstitutionScriptContextTester();
        this.posContextTester = new PositioningScriptContextTester();
    }

    /** {@inheritDoc} */
    public String[] getSubstitutionFeatures(Object[][] features) {
        return GSUB_REQ_FEATURES;
    }

    /** {@inheritDoc} */
    public String[] getOptionalSubstitutionFeatures() {
        return GSUB_OPT_FEATURES;
    }

    /** {@inheritDoc} */
    public ScriptContextTester getSubstitutionContextTester() {
        return subContextTester;
    }

    /** {@inheritDoc} */
    public String[] getPositioningFeatures(Object[][] features) {
        return GPOS_REQ_FEATURES;
    }

    /** {@inheritDoc} */
    public String[] getOptionalPositioningFeatures() {
        return GPOS_OPT_FEATURES;
    }

    /** {@inheritDoc} */
    public ScriptContextTester getPositioningContextTester() {
        return posContextTester;
    }

    /** {@inheritDoc} */
    @Override
    public GlyphSequence substitute(GlyphSequence gs, String script, String language, AdvancedTypographicTable.UseSpec[] usa, ScriptContextTester sct) {
        assert usa != null;
        // 1. syllabize
        GlyphSequence[] sa = syllabize(gs, script, language);
        // 2. process each syllable
        for (int i = 0, n = sa.length; i < n; i++) {
            GlyphSequence s = sa [ i ];
            // apply basic shaping subs
            for (int j = 0, m = usa.length; j < m; j++) {
                AdvancedTypographicTable.UseSpec us = usa [ j ];
                if (isBasicShapingUse(us)) {
                    s.setPredications(true);
                    s = us.substitute(s, script, language, sct);
                }
            }
            // reorder pre-base matra
            s = reorderPreBaseMatra(s);
            // reorder reph
            s = reorderReph(s);
            // apply presentation subs
            for (int j = 0, m = usa.length; j < m; j++) {
                AdvancedTypographicTable.UseSpec us = usa [ j ];
                if (isPresentationUse(us)) {
                    s.setPredications(true);
                    s = us.substitute(s, script, language, sct);
                }
            }
            // record result
            sa [ i ] = s;
        }
        // 3. return reassembled substituted syllables
        return unsyllabize(gs, sa);
    }

    /**
     * Get script specific syllabizer class.
     * @return a syllabizer class object or null
     */
    protected Class<? extends Syllabizer> getSyllabizerClass() {
        return null;
    }

    private GlyphSequence[] syllabize(GlyphSequence gs, String script, String language) {
        return Syllabizer.getSyllabizer(script, language, getSyllabizerClass()).syllabize(gs);
    }

    private GlyphSequence unsyllabize(GlyphSequence gs, GlyphSequence[] sa) {
        return GlyphSequence.join(gs, sa);
    }

    private static Set<String> basicShapingFeatures;
    private static final String[] BASIC_SHAPING_FEATURE_STRINGS = {
        "abvf",
        "akhn",
        "blwf",
        "cjct",
        "half",
        "locl",
        "nukt",
        "pref",
        "pstf",
        "rkrf",
        "rphf",
        "vatu",
    };
    static {
        basicShapingFeatures = new HashSet<String>();
        for (String s : BASIC_SHAPING_FEATURE_STRINGS) {
            basicShapingFeatures.add(s);
        }
    }
    private boolean isBasicShapingUse(AdvancedTypographicTable.UseSpec us) {
        assert us != null;
        if (basicShapingFeatures != null) {
            return basicShapingFeatures.contains(us.getFeature());
        } else {
            return false;
        }
    }

    private static  Set<String> presentationFeatures;
    private static final String[] PRESENTATION_FEATURE_STRINGS = {
        "abvs",
        "blws",
        "calt",
        "haln",
        "pres",
        "psts",
    };
    static {
        presentationFeatures = new HashSet<String>();
        for (String s : PRESENTATION_FEATURE_STRINGS) {
            presentationFeatures.add(s);
        }
    }
    private boolean isPresentationUse(AdvancedTypographicTable.UseSpec us) {
        assert us != null;
        if (presentationFeatures != null) {
            return presentationFeatures.contains(us.getFeature());
        } else {
            return false;
        }
    }

    private GlyphSequence reorderPreBaseMatra(GlyphSequence gs) {
        int source;
        if ((source = findPreBaseMatra(gs)) >= 0) {
            int target;
            if ((target = findPreBaseMatraTarget(gs, source)) >= 0) {
                if (target != source) {
                    gs = reorder(gs, source, target);
                }
            }
        }
        return gs;
    }

    /**
     * Find pre-base matra in sequence.
     * @param gs input sequence
     * @return index of pre-base matra or -1 if not found
     */
    protected int findPreBaseMatra(GlyphSequence gs) {
        return -1;
    }

    /**
     * Find pre-base matra target in sequence.
     * @param gs input sequence
     * @param source index of pre-base matra
     * @return index of pre-base matra target or -1
     */
    protected int findPreBaseMatraTarget(GlyphSequence gs, int source) {
        return -1;
    }

    private GlyphSequence reorderReph(GlyphSequence gs) {
        int source;
        if ((source = findReph(gs)) >= 0) {
            int target;
            if ((target = findRephTarget(gs, source)) >= 0) {
                if (target != source) {
                    gs = reorder(gs, source, target);
                }
            }
        }
        return gs;
    }

    /**
     * Find reph in sequence.
     * @param gs input sequence
     * @return index of reph or -1 if not found
     */
    protected int findReph(GlyphSequence gs) {
        return -1;
    }

    /**
     * Find reph target in sequence.
     * @param gs input sequence
     * @param source index of reph
     * @return index of reph target or -1
     */
    protected int findRephTarget(GlyphSequence gs, int source) {
        return -1;
    }

    private GlyphSequence reorder(GlyphSequence gs, int source, int target) {
        return GlyphSequence.reorder(gs, source, 1, target);
    }

    /** {@inheritDoc} */
    @Override
    public boolean position(GlyphSequence gs, String script, String language, int fontSize, AdvancedTypographicTable.UseSpec[] usa, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        boolean adjusted = super.position(gs, script, language, fontSize, usa, widths, adjustments, sct);
        return adjusted;
    }

    /** Abstract syllabizer. */
    protected abstract static class Syllabizer implements Comparable {
        private String script;
        private String language;
        Syllabizer(String script, String language) {
            this.script = script;
            this.language = language;
        }
        /**
         * Subdivide glyph sequence GS into syllabic segments each represented by a distinct
         * output glyph sequence.
         * @param gs input glyph sequence
         * @return segmented syllabic glyph sequences
         */
        abstract GlyphSequence[] syllabize(GlyphSequence gs);
        /** {@inheritDoc} */
        public int hashCode() {
            int hc = 0;
            hc =  7 * hc + (hc ^ script.hashCode());
            hc = 11 * hc + (hc ^ language.hashCode());
            return hc;
        }
        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (o instanceof Syllabizer) {
                Syllabizer s = (Syllabizer) o;
                if (!s.script.equals(script)) {
                    return false;
                } else {
                    return s.language.equals(language);
                }
            } else {
                return false;
            }
        }
        /** {@inheritDoc} */
        public int compareTo(Object o) {
            int d;
            if (o instanceof Syllabizer) {
                Syllabizer s = (Syllabizer) o;
                if ((d = script.compareTo(s.script)) == 0) {
                    d = language.compareTo(s.language);
                }
            } else {
                d = -1;
            }
            return d;
        }
        private static Map<String, Syllabizer> syllabizers = new HashMap<String, Syllabizer>();
        static Syllabizer getSyllabizer(String script, String language, Class<? extends Syllabizer> syllabizerClass) {
            String sid = makeSyllabizerId(script, language);
            Syllabizer s = syllabizers.get(sid);
            if (s == null) {
                if ((syllabizerClass == null) || ((s = makeSyllabizer(script, language, syllabizerClass)) == null)) {
                    log.warn("No syllabizer available for script '" + script + "', language '" + language + "', using default Indic syllabizer.");
                    s = new DefaultSyllabizer(script, language);
                }
                syllabizers.put(sid, s);
            }
            return s;
        }
        static String makeSyllabizerId(String script, String language) {
            return script + ":" + language;
        }
        static Syllabizer makeSyllabizer(String script, String language, Class<? extends Syllabizer> syllabizerClass) {
            Syllabizer s;
            try {
                Constructor<? extends Syllabizer> cf = syllabizerClass.getDeclaredConstructor(new Class[] { String.class, String.class });
                s = (Syllabizer) cf.newInstance(script, language);
            } catch (NoSuchMethodException e) {
                s = null;
            } catch (InstantiationException e) {
                s = null;
            } catch (IllegalAccessException e) {
                s = null;
            } catch (InvocationTargetException e) {
                s = null;
            }
            return s;
        }
    }

    /** Default syllabizer. */
    protected static class DefaultSyllabizer extends Syllabizer {
        DefaultSyllabizer(String script, String language) {
            super(script, language);
        }
        /** {@inheritDoc} */
        @Override
        GlyphSequence[] syllabize(GlyphSequence gs) {
            int[] ca = gs.getCharacterArray(false);
            int   nc = gs.getCharacterCount();
            if (nc == 0) {
                return new GlyphSequence[] { gs };
            } else {
                return segmentize(gs, segmentize(ca, nc));
            }
        }
        /**
         * Construct array of segements from original character array (associated with original glyph sequence)
         * @param ca input character sequence
         * @param nc number of characters in sequence
         * @return array of syllable segments
         */
        protected Segment[] segmentize(int[] ca, int nc) {
            Vector<Segment> sv = new Vector<Segment>(nc);
            for (int s = 0, e = nc; s < e; ) {
                int i;
                if ((i = findStartOfSyllable(ca, s, e)) < e) {
                    if (s < i) {
                        // from s to i is non-syllable segment
                        sv.add(new Segment(s, i, Segment.OTHER));
                    }
                    s = i; // move s to start of syllable
                } else {
                    if (s < e) {
                        // from s to e is non-syllable segment
                        sv.add(new Segment(s, e, Segment.OTHER));
                    }
                    s = e; // move s to end of input sequence
                }
                if ((i = findEndOfSyllable(ca, s, e)) > s) {
                    if (s < i) {
                        // from s to i is syllable segment
                        sv.add(new Segment(s, i, Segment.SYLLABLE));
                    }
                    s = i; // move s to end of syllable
                } else {
                    if (s < e) {
                        // from s to e is non-syllable segment
                        sv.add(new Segment(s, e, Segment.OTHER));
                    }
                    s = e; // move s to end of input sequence
                }
            }
            return sv.toArray(new Segment [ sv.size() ]);
        }
        /**
         * Construct array of glyph sequences from original glyph sequence and segment array.
         * @param gs original input glyph sequence
         * @param sa segment array
         * @return array of glyph sequences each belonging to an (ordered) segment in SA
         */
        protected GlyphSequence[] segmentize(GlyphSequence gs, Segment[] sa) {
            int   ng = gs.getGlyphCount();
            int[] ga = gs.getGlyphArray(false);
            CharAssociation[] aa = gs.getAssociations(0, -1);
            Vector<GlyphSequence> nsv = new Vector<GlyphSequence>();
            for (int i = 0, ns = sa.length; i < ns; i++) {
                Segment s = sa [ i ];
                Vector<Integer> ngv = new Vector<Integer>(ng);
                Vector<CharAssociation> nav = new Vector<CharAssociation>(ng);
                for (int j = 0; j < ng; j++) {
                    CharAssociation ca = aa [ j ];
                    if (ca.contained(s.getOffset(), s.getCount())) {
                        ngv.add(ga [ j ]);
                        nav.add(ca);
                    }
                }
                if (ngv.size() > 0) {
                    nsv.add(new GlyphSequence(gs, null, toIntArray(ngv), null, null, nav.toArray(new CharAssociation [ nav.size() ]), null));
                }
            }
            if (nsv.size() > 0) {
                return nsv.toArray(new GlyphSequence [ nsv.size() ]);
            } else {
                return new GlyphSequence[] { gs };
            }
        }
        /**
         * Find start of syllable in character array, starting at S, ending at E.
         * @param ca character array
         * @param s start index
         * @param e end index
         * @return index of start or E if no start found
         */
        protected int findStartOfSyllable(int[] ca, int s, int e) {
            return e;
        }
        /**
         * Find end of syllable in character array, starting at S, ending at E.
         * @param ca character array
         * @param s start index
         * @param e end index
         * @return index of start or S if no end found
         */
        protected int findEndOfSyllable(int[] ca, int s, int e) {
            return s;
        }
        private static int[] toIntArray(Vector<Integer> iv) {
            int ni = iv.size();
            int[] ia = new int [ iv.size() ];
            for (int i = 0, n = ni; i < n; i++) {
                ia [ i ] = (int) iv.get(i);
            }
            return ia;
        }
    }

    /** Syllabic segment. */
    protected static class Segment {

        static final int OTHER = 0;            // other (non-syllable) characters
        static final int SYLLABLE = 1;         // (orthographic) syllable

        private int start;
        private int end;
        private int type;

        Segment(int start, int end, int type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }

        int getOffset() {
            return start;
        }

        int getCount() {
            return end - start;
        }

        int getType() {
            return type;
        }
    }
}
