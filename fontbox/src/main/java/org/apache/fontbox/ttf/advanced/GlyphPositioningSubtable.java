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
 * <p>The <code>GlyphPositioningSubtable</code> implements an abstract base of a glyph subtable,
 * providing a default implementation of the <code>GlyphPositioning</code> interface.</p>
 *
 * @author Glenn Adams
 */
public abstract class GlyphPositioningSubtable extends GlyphSubtable implements GlyphPositioning {

    private static final GlyphPositioningState STATE = new GlyphPositioningState();

    /**
     * Instantiate a <code>GlyphPositioningSubtable</code>.
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param coverage subtable coverage table
     */
    protected GlyphPositioningSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage) {
        super(id, sequence, flags, format, coverage);
    }

    /** {@inheritDoc} */
    public int getTableType() {
        return AdvancedTypographicTable.GLYPH_TABLE_TYPE_POSITIONING;
    }

    /** {@inheritDoc} */
    public String getTypeName() {
        return GlyphPositioningTable.getLookupTypeName(getType());
    }

    /** {@inheritDoc} */
    public boolean isCompatible(GlyphSubtable subtable) {
        return subtable instanceof GlyphPositioningSubtable;
    }

    /** {@inheritDoc} */
    public boolean usesReverseScan() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean position(GlyphPositioningState ps) {
        return false;
    }

    /**
     * Apply positioning using specified state and subtable array. For each position in input sequence,
     * apply subtables in order until some subtable applies or none remain. If no subtable applied or no
     * input was consumed for a given position, then apply default action (no adjustments and advance).
     * If <code>sequenceIndex</code> is non-negative, then apply subtables only when current position
     * matches <code>sequenceIndex</code> in relation to the starting position. Furthermore, upon
     * successful application at <code>sequenceIndex</code>, then discontinue processing the remaining
     * @param ps positioning state
     * @param sta array of subtables to apply
     * @param sequenceIndex if non negative, then apply subtables only at specified sequence index
     * @return true if a non-zero adjustment occurred
     */
    public static final boolean position(GlyphPositioningState ps, GlyphPositioningSubtable[] sta, int sequenceIndex) {
        int sequenceStart = ps.getPosition();
        boolean appliedOneShot = false;
        while (ps.hasNext()) {
            boolean applied = false;
            if (!appliedOneShot && ps.maybeApplicable()) {
                for (int i = 0, n = sta.length; !applied && (i < n); i++) {
                    if (sequenceIndex < 0) {
                        applied = ps.apply(sta [ i ]);
                    } else if (ps.getPosition() == (sequenceStart + sequenceIndex)) {
                        applied = ps.apply(sta [ i ]);
                        if (applied) {
                            appliedOneShot = true;
                        }
                    }
                }
            }
            if (!applied || !ps.didConsume()) {
                ps.applyDefault();
            }
            ps.next();
        }
        return ps.getAdjusted();
    }

    /**
     * Apply positioning.
     * @param gs input glyph sequence
     * @param script tag
     * @param language tag
     * @param feature tag
     * @param fontSize the font size
     * @param sta subtable array
     * @param widths array
     * @param adjustments array (receives output adjustments)
     * @param sct script context tester
     * @return true if a non-zero adjustment occurred
     */
    public static final boolean position(GlyphSequence gs, String script, String language, String feature, int fontSize, GlyphPositioningSubtable[] sta, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        synchronized (STATE) {
            return position(STATE.reset(gs, script, language, feature, fontSize, widths, adjustments, sct), sta, -1);
        }
    }

}
