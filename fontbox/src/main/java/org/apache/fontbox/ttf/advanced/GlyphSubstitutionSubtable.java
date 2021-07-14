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

import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

/**
 * <p>The <code>GlyphSubstitutionSubtable</code> implements an abstract base of a glyph substitution subtable,
 * providing a default implementation of the <code>GlyphSubstitution</code> interface.</p>
 *
 * @author Glenn Adams
 */
public abstract class GlyphSubstitutionSubtable extends GlyphSubtable implements GlyphSubstitution {

    private static final GlyphSubstitutionState STATE = new GlyphSubstitutionState();

    /**
     * Instantiate a <code>GlyphSubstitutionSubtable</code>.
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     */
    protected GlyphSubstitutionSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage) {
        super(id, sequence, flags, format, coverage);
    }

    /** {@inheritDoc} */
    public int getTableType() {
        return AdvancedTypographicTable.GLYPH_TABLE_TYPE_SUBSTITUTION;
    }

    /** {@inheritDoc} */
    public String getTypeName() {
        return GlyphSubstitutionTable.getLookupTypeName(getType());
    }

    /** {@inheritDoc} */
    public boolean isCompatible(GlyphSubtable subtable) {
        return subtable instanceof GlyphSubstitutionSubtable;
    }

    /** {@inheritDoc} */
    public boolean usesReverseScan() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean substitute(GlyphSubstitutionState ss) {
        return false;
    }

    /**
     * Apply substitutions using specified state and subtable array. For each position in input sequence,
     * apply subtables in order until some subtable applies or none remain. If no subtable applied or no
     * input was consumed for a given position, then apply default action (copy input glyph and advance).
     * If <code>sequenceIndex</code> is non-negative, then apply subtables only when current position
     * matches <code>sequenceIndex</code> in relation to the starting position. Furthermore, upon
     * successful application at <code>sequenceIndex</code>, then apply default action for all remaining
     * glyphs in input sequence.
     * @param ss substitution state
     * @param sta array of subtables to apply
     * @param sequenceIndex if non negative, then apply subtables only at specified sequence index
     * @return output glyph sequence
     */
    public static final GlyphSequence substitute(GlyphSubstitutionState ss, GlyphSubstitutionSubtable[] sta, int sequenceIndex) {
        int sequenceStart = ss.getPosition();
        boolean appliedOneShot = false;
        while (ss.hasNext()) {
            boolean applied = false;
            if (!appliedOneShot && ss.maybeApplicable()) {
                for (int i = 0, n = sta.length; !applied && (i < n); i++) {
                    if (sequenceIndex < 0) {
                        applied = ss.apply(sta [ i ]);
                    } else if (ss.getPosition() == (sequenceStart + sequenceIndex)) {
                        applied = ss.apply(sta [ i ]);
                        if (applied) {
                            appliedOneShot = true;
                        }
                    }
                }
            }
            if (!applied || !ss.didConsume()) {
                ss.applyDefault();
            }
            ss.next();
        }
        return ss.getOutput();
    }

    /**
     * Apply substitutions.
     * @param gs input glyph sequence
     * @param script tag
     * @param language tag
     * @param feature tag
     * @param sta subtable array
     * @param sct script context tester
     * @return output glyph sequence
     */
    public static final GlyphSequence substitute(GlyphSequence gs, String script, String language, String feature, GlyphSubstitutionSubtable[] sta, ScriptContextTester sct) {
        synchronized (STATE) {
            return substitute(STATE.reset(gs, script, language, feature, sct), sta, -1);
        }
    }

}
