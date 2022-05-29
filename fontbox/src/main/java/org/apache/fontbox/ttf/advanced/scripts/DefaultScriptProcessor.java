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

import java.util.List;

import org.apache.fontbox.ttf.advanced.GlyphDefinitionTable;
import org.apache.fontbox.ttf.advanced.util.CharAssociation;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

// CSOFF: LineLengthCheck

/**
 * <p>Default script processor, which enables default glyph composition/decomposition, common ligatures, localized forms
 * and kerning.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class DefaultScriptProcessor extends ScriptProcessor {

    private static final String ccmpFeatureName                 = "ccmp";
    private static final String kernFeatureName                 = "kern";
    private static final String ligaFeatureName                 = "liga";
    private static final String loclFeatureName                 = "locl";
    private static final String markFeatureName                 = "mark";
    private static final String mkmkFeatureName                 = "mkmk";

    /** features to use for substitutions */
    private static final String[] GSUB_FEATURES =
    {
        ccmpFeatureName,                                        // glyph composition/decomposition
        ligaFeatureName,                                        // common ligatures
        loclFeatureName                                         // localized forms
    };

    /** features to use for positioning */
    private static final String[] GPOS_FEATURES =
    {
        kernFeatureName,                                        // kerning
        markFeatureName,                                        // mark to base or ligature positioning
        mkmkFeatureName                                         // mark to mark positioning
    };

    DefaultScriptProcessor(String script) {
        super(script);
    }

    @Override
    /** {@inheritDoc} */
    public String[] getSubstitutionFeatures(Object[][] features) {
        if ((features == null) || (features.length == 0))
            return GSUB_FEATURES;
        else
            return augmentFeatures(GSUB_FEATURES, features);
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getSubstitutionContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public String[] getPositioningFeatures(Object[][] features) {
        if ((features == null) || (features.length == 0))
            return GPOS_FEATURES;
        else
            return augmentFeatures(GPOS_FEATURES, features);
    }

    private String[] augmentFeatures(String[] features, Object[][] moreFeatures) {
        assert features != null;
        assert moreFeatures != null;
        List<String> augmentedFeatures = new java.util.ArrayList<String>();
        for (String f : features)
            augmentedFeatures.add(f);
        for (int i = 0, n = moreFeatures.length; i < n; ++i) {
            Object[] mf = moreFeatures[i];
            if (mf != null) {
                assert mf.length > 0;
                if (mf[0] instanceof String) {
                    String mfn = (String) mf[0];
                    if (mfn.equals(kernFeatureName)) {
                        if (mf.length > 1) {
                            if (mf[1] instanceof Boolean) {
                                boolean kerningEnabled = (Boolean) mf[1];
                                if (augmentedFeatures.contains(kernFeatureName)) {
                                    if (!kerningEnabled)
                                        augmentedFeatures.remove(kernFeatureName);
                                } else {
                                    if (kerningEnabled)
                                        augmentedFeatures.add(kernFeatureName);
                                }
                            }
                        }
                    } else if (!augmentedFeatures.contains(mfn)) {
                        augmentedFeatures.add(mfn);
                    }
                }
            }
        }
        return augmentedFeatures.toArray(new String[augmentedFeatures.size()]);
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getPositioningContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public GlyphSequence
        reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language, Object[][] features) {
        int   ng  = gs.getGlyphCount();
        int[] ga  = gs.getGlyphArray(false);
        int   nm  = 0;
        // count combining marks
        for (int i = 0; i < ng; i++) {
            int gid = ga [ i ];
            int gw = unscaledWidths [ i ];
            if (isReorderedMark(gdef, ga, unscaledWidths, i)) {
                nm++;
            }
        }
        // only reorder if there is at least one mark and at least one non-mark glyph
        if ((nm > 0) && ((ng - nm) > 0)) {
            CharAssociation[] aa = gs.getAssociations(0, -1);
            int[] nga = new int [ ng ];
            int[][] npa = (gpa != null) ? new int [ ng ][] : null;
            CharAssociation[] naa = new CharAssociation [ ng ];
            int k = 0;
            CharAssociation ba = null;
            int bg = -1;
            int[] bpa = null;
            for (int i = 0; i < ng; i++) {
                int gid = ga [ i ];
                int[] pa = (gpa != null) ? gpa [ i ] : null;
                CharAssociation ca = aa [ i ];
                if (isReorderedMark(gdef, ga, unscaledWidths, i)) {
                    nga [ k ] = gid;
                    naa [ k ] = ca;
                    if (npa != null) {
                        npa [ k ] = pa;
                    }
                    k++;
                } else {
                    if (bg != -1) {
                        nga [ k ] = bg;
                        naa [ k ] = ba;
                        if (npa != null) {
                            npa [ k ] = bpa;
                        }
                        k++;
                        bg = -1;
                        ba = null;
                        bpa = null;
                    }
                    if (bg == -1) {
                        bg = gid;
                        ba = ca;
                        bpa = pa;
                    }
                }
            }
            if (bg != -1) {
                nga [ k ] = bg;
                naa [ k ] = ba;
                if (npa != null) {
                    npa [ k ] = bpa;
                }
                k++;
            }
            assert k == ng;
            if (npa != null) {
                System.arraycopy(npa, 0, gpa, 0, ng);
            }
            return new GlyphSequence(gs, null, nga, null, null, naa, null);
        } else {
            return gs;
        }
    }

    protected boolean isReorderedMark(GlyphDefinitionTable gdef, int[] glyphs, int[] unscaledWidths, int index) {
        return gdef.isGlyphClass(glyphs[index], GlyphDefinitionTable.GLYPH_CLASS_MARK) && (unscaledWidths[index] == 0);
    }

}
