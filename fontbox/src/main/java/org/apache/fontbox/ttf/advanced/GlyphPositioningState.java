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
 * <p>The <code>GlyphPositioningState</code> implements an state object used during glyph positioning
 * processing.</p>
 *
 * @author Glenn Adams
 */

public class GlyphPositioningState extends GlyphProcessingState {

    /** font size */
    private int fontSize;
    /** default advancements */
    private int[] widths;
    /** current adjustments */
    private int[][] adjustments;
    /** if true, then some adjustment was applied */
    private boolean adjusted;

    /**
     * Construct default (reset) glyph positioning state.
     */
    public GlyphPositioningState() {
    }

    /**
     * Construct glyph positioning state.
     * @param gs input glyph sequence
     * @param script script identifier
     * @param language language identifier
     * @param feature feature identifier
     * @param fontSize font size (in micropoints)
     * @param widths array of design advancements (in glyph index order)
     * @param adjustments positioning adjustments to which positioning is applied
     * @param sct script context tester (or null)
     */
    public GlyphPositioningState(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        super(gs, script, language, feature, sct);
        this.fontSize = fontSize;
        this.widths = widths;
        this.adjustments = adjustments;
    }

    /**
     * Construct glyph positioning state using an existing state object using shallow copy
     * except as follows: input glyph sequence is copied deep except for its characters array.
     * @param ps existing positioning state to copy from
     */
    public GlyphPositioningState(GlyphPositioningState ps) {
        super(ps);
        this.fontSize = ps.fontSize;
        this.widths = ps.widths;
        this.adjustments = ps.adjustments;
    }

    /**
     * Reset glyph positioning state.
     * @param gs input glyph sequence
     * @param script script identifier
     * @param language language identifier
     * @param feature feature identifier
     * @param fontSize font size (in micropoints)
     * @param widths array of design advancements (in glyph index order)
     * @param adjustments positioning adjustments to which positioning is applied
     * @param sct script context tester (or null)
     */
    public GlyphPositioningState reset(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        super.reset(gs, script, language, feature, sct);
        this.fontSize = fontSize;
        this.widths = widths;
        this.adjustments = adjustments;
        this.adjusted = false;
        return this;
    }

    /**
     * Obtain design advancement (width) of glyph at specified index.
     * @param gi glyph index
     * @return design advancement, or zero if glyph index is not present
     */
    public int getWidth(int gi) {
        if ((widths != null) && (gi < widths.length)) {
            return widths [ gi ];
        } else {
            return 0;
        }
    }

    /**
     * Perform adjustments at current position index.
     * @param v value containing adjustments
     * @return true if a non-zero adjustment was made
     */
    public boolean adjust(GlyphPositioningTable.Value v) {
        return adjust(v, 0);
    }

    /**
     * Perform adjustments at specified offset from current position index.
     * @param v value containing adjustments
     * @param offset from current position index
     * @return true if a non-zero adjustment was made
     */
    public boolean adjust(GlyphPositioningTable.Value v, int offset) {
        assert v != null;
        if ((index + offset) < indexLast) {
            return v.adjust(adjustments [ index + offset ], fontSize);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Obtain current adjustments at current position index.
     * @return array of adjustments (int[4]) at current position
     */
    public int[] getAdjustment() {
        return getAdjustment(0);
    }

    /**
     * Obtain current adjustments at specified offset from current position index.
     * @param offset from current position index
     * @return array of adjustments (int[4]) at specified offset
     * @throws IndexOutOfBoundsException if offset is invalid
     */
    public int[] getAdjustment(int offset) throws IndexOutOfBoundsException {
        if ((index + offset) < indexLast) {
            return adjustments [ index + offset ];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Apply positioning subtable to current state at current position (only),
     * resulting in the consumption of zero or more input glyphs.
     * @param st the glyph positioning subtable to apply
     * @return true if subtable applied, or false if it did not (e.g., its
     * input coverage table did not match current input context)
     */
    public boolean apply(GlyphPositioningSubtable st) {
        assert st != null;
        updateSubtableState(st);
        boolean applied = st.position(this);
        return applied;
    }

    /**
     * Apply a sequence of matched rule lookups to the <code>nig</code> input glyphs
     * starting at the current position. If lookups are non-null and non-empty, then
     * all input glyphs specified by <code>nig</code> are consumed irregardless of
     * whether any specified lookup applied.
     * @param lookups array of matched lookups (or null)
     * @param nig number of glyphs in input sequence, starting at current position, to which
     * the lookups are to apply, and to be consumed once the application has finished
     * @return true if lookups are non-null and non-empty; otherwise, false
     */
    public boolean apply(AdvancedTypographicTable.RuleLookup[] lookups, int nig) {
        if ((lookups != null) && (lookups.length > 0)) {
            // apply each rule lookup to extracted input glyph array
            for (int i = 0, n = lookups.length; i < n; i++) {
                AdvancedTypographicTable.RuleLookup l = lookups [ i ];
                if (l != null) {
                    AdvancedTypographicTable.LookupTable lt = l.getLookup();
                    if (lt != null) {
                        // perform positioning on a copy of previous state
                        GlyphPositioningState ps = new GlyphPositioningState(this);
                        // apply lookup table positioning
                        if (lt.position(ps, l.getSequenceIndex())) {
                            setAdjusted(true);
                        }
                    }
                }
            }
            consume(nig);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Apply default application semantices; namely, consume one input glyph.
     */
    public void applyDefault() {
        super.applyDefault();
    }

    /**
     * Set adjusted state, used to record effect of non-zero adjustment.
     * @param adjusted true if to set adjusted state, otherwise false to
     * clear adjusted state
     */
    public void setAdjusted(boolean adjusted) {
        this.adjusted = adjusted;
    }

    /**
     * Get adjusted state.
     * @return adjusted true if some non-zero adjustment occurred and
     * was recorded by {@link #setAdjusted}; otherwise, false.
     */
    public boolean getAdjusted() {
        return adjusted;
    }

}
