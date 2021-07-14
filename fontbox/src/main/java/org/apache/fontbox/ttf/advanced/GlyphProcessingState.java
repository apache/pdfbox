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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.fontbox.ttf.advanced.util.CharAssociation;
import org.apache.fontbox.ttf.advanced.util.GlyphContextTester;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;
import org.apache.fontbox.ttf.advanced.util.GlyphTester;
import org.apache.fontbox.ttf.advanced.util.ScriptContextTester;

/**
 * <p>The <code>GlyphProcessingState</code> implements a common, base state object used during glyph substitution
 * and positioning processing.</p>
 *
 * @author Glenn Adams
 */

@SuppressWarnings("unchecked") 
public class GlyphProcessingState {

    /** governing glyph definition table */
    protected GlyphDefinitionTable gdef;
    /** governing script */
    protected String script;
    /** governing language */
    protected String language;
    /** governing feature */
    protected String feature;
    /** current input glyph sequence */
    protected GlyphSequence igs;
    /** current index in input sequence */
    protected int index;
    /** last (maximum) index of input sequence (exclusive) */
    protected int indexLast;
    /** consumed, updated after each successful subtable application */
    protected int consumed;
    /** lookup flags */
    protected int lookupFlags;
    /** class match set */
    protected int classMatchSet;
    /** script specific context tester or null */
    protected ScriptContextTester sct;
    /** glyph context tester or null */
    protected GlyphContextTester gct;
    /** ignore base glyph tester */
    protected GlyphTester ignoreBase;
    /** ignore ligature glyph tester */
    protected GlyphTester ignoreLigature;
    /** ignore mark glyph tester */
    protected GlyphTester ignoreMark;
    /** default ignore glyph tester */
    protected GlyphTester ignoreDefault;
    /** current subtable */
    private GlyphSubtable subtable;

    /**
     * Construct default (reset) glyph processing state.
     */
    public GlyphProcessingState() {
    }

    /**
     * Construct glyph processing state.
     * @param gs input glyph sequence
     * @param script script identifier
     * @param language language identifier
     * @param feature feature identifier
     * @param sct script context tester (or null)
     */
    protected GlyphProcessingState(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
        this.script = script;
        this.language = language;
        this.feature = feature;
        this.igs = gs;
        this.indexLast = gs.getGlyphCount();
        this.sct = sct;
        this.gct = (sct != null) ? sct.getTester(feature) : null;
        this.ignoreBase = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredBase(gi, flags); } };
        this.ignoreLigature = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredLigature(gi, flags); } };
        this.ignoreMark = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredMark(gi, flags); } };
    }

    /**
     * Construct glyph processing state using an existing state object using shallow copy
     * except as follows: input glyph sequence is copied deep except for its characters array.
     * @param s existing processing state to copy from
     */
    protected GlyphProcessingState(GlyphProcessingState s) {
        this (new GlyphSequence(s.igs), s.script, s.language, s.feature, s.sct);
        setPosition(s.index);
    }

    /**
     * Reset glyph processing state.
     * @param gs input glyph sequence
     * @param script script identifier
     * @param language language identifier
     * @param feature feature identifier
     * @param sct script context tester (or null)
     */
    protected GlyphProcessingState reset(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
        this.gdef = null;
        this.script = script;
        this.language = language;
        this.feature = feature;
        this.igs = gs;
        this.index = 0;
        this.indexLast = gs.getGlyphCount();
        this.consumed = 0;
        this.lookupFlags = 0;
        this.classMatchSet = 0;
        this.sct = sct;
        this.gct = (sct != null) ? sct.getTester(feature) : null;
        this.ignoreBase = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredBase(gi, flags); } };
        this.ignoreLigature = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredLigature(gi, flags); } };
        this.ignoreMark = new GlyphTester() { public boolean test(int gi, int flags) { return isIgnoredMark(gi, flags); } };
        this.ignoreDefault = null;
        this.subtable = null;
        return this;
    }

    /**
     * Set governing glyph definition table.
     * @param gdef glyph definition table (or null, to unset)
     */
    public void setGDEF(GlyphDefinitionTable gdef) {
        if (this.gdef == null) {
            this.gdef = gdef;
        } else if (gdef == null) {
            this.gdef = null;
        }
    }

    /**
     * Obtain governing glyph definition table.
     * @return glyph definition table (or null, to not set)
     */
    public GlyphDefinitionTable getGDEF() {
        return gdef;
    }

    /**
     * Set governing lookup flags
     * @param flags lookup flags (or zero, to unset)
     */
    public void setLookupFlags(int flags) {
        if (this.lookupFlags == 0) {
            this.lookupFlags = flags;
        } else if (flags == 0) {
            this.lookupFlags = 0;
        }
    }

    /**
     * Obtain governing lookup  flags.
     * @return lookup flags (zero may indicate unset or no flags)
     */
    public int getLookupFlags() {
        return lookupFlags;
    }

    /**
     * Obtain governing class match set.
     * @param gi glyph index that may be used to determine which match set applies
     * @return class match set (zero may indicate unset or no set)
     */
    public int getClassMatchSet(int gi) {
        return 0;
    }

    /**
     * Set default ignore tester.
     * @param ignoreDefault glyph tester (or null, to unset)
     */
    public void setIgnoreDefault(GlyphTester ignoreDefault) {
        if (this.ignoreDefault == null) {
            this.ignoreDefault = ignoreDefault;
        } else if (ignoreDefault == null) {
            this.ignoreDefault = null;
        }
    }

    /**
     * Obtain governing default ignores tester.
     * @return default ignores tester
     */
    public GlyphTester getIgnoreDefault() {
        return ignoreDefault;
    }

    /**
     * Update glyph subtable specific state. Each time a
     * different glyph subtable is to be applied, it is used
     * to update this state prior to application, after which
     * this state is to be reset.
     * @param st glyph subtable to use for update
     */
    public void updateSubtableState(GlyphSubtable st) {
        if (this.subtable != st) {
            setGDEF(st.getGDEF());
            setLookupFlags(st.getFlags());
            setIgnoreDefault(getIgnoreTester(getLookupFlags()));
            this.subtable = st;
        }
    }

    /**
     * Obtain current position index in input glyph sequence.
     * @return current index
     */
    public int getPosition() {
        return index;
    }

    /**
     * Set (seek to) position index in input glyph sequence.
     * @param index to seek to
     * @throws IndexOutOfBoundsException if index is less than zero
     * or exceeds last valid position
     */
    public void setPosition(int index) throws IndexOutOfBoundsException {
        if ((index >= 0) && (index <= indexLast)) {
            this.index =  index;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Obtain last valid position index in input glyph sequence.
     * @return current last index
     */
    public int getLastPosition() {
        return indexLast;
    }

    /**
     * Determine if at least one glyph remains in
     * input sequence.
     * @return true if one or more glyph remains
     */
    public boolean hasNext() {
        return hasNext(1);
    }

    /**
     * Determine if at least <code>count</code> glyphs remain in
     * input sequence.
     * @param count of glyphs to test
     * @return true if at least <code>count</code> glyphs are available
     */
    public boolean hasNext(int count) {
        return (index + count) <= indexLast;
    }

    /**
     * Update the current position index based upon previously consumed
     * glyphs, i.e., add the consuemd count to the current position index.
     * If no glyphs were previously consumed, then forces exactly one
     * glyph to be consumed.
     * @return the new (updated) position index
     */
    public int next() {
        if (index < indexLast) {
            // force consumption of at least one input glyph
            if (consumed == 0) {
                consumed = 1;
            }
            index += consumed;
            consumed = 0;
            if (index > indexLast) {
                index = indexLast;
            }
        }
        return index;
    }

    /**
     * Determine if at least one backtrack (previous) glyph is present
     * in input sequence.
     * @return true if one or more glyph remains
     */
    public boolean hasPrev() {
        return hasPrev(1);
    }

    /**
     * Determine if at least <code>count</code> backtrack (previous) glyphs
     * are present in input sequence.
     * @param count of glyphs to test
     * @return true if at least <code>count</code> glyphs are available
     */
    public boolean hasPrev(int count) {
        return (index - count) >= 0;
    }

    /**
     * Update the current position index based upon previously consumed
     * glyphs, i.e., subtract the consuemd count from the current position index.
     * If no glyphs were previously consumed, then forces exactly one
     * glyph to be consumed. This method is used to traverse an input
     * glyph sequence in reverse order.
     * @return the new (updated) position index
     */
    public int prev() {
        if (index > 0) {
            // force consumption of at least one input glyph
            if (consumed == 0) {
                consumed = 1;
            }
            index -= consumed;
            consumed = 0;
            if (index < 0) {
                index = 0;
            }
        }
        return index;
    }

    /**
     * Record the consumption of <code>count</code> glyphs such that
     * this consumption never exceeds the number of glyphs in the input glyph
     * sequence.
     * @param count of glyphs to consume
     * @return newly adjusted consumption count
     * @throws IndexOutOfBoundsException if count would cause consumption
     * to exceed count of glyphs in input glyph sequence
     */
    public int consume(int count) throws IndexOutOfBoundsException {
        if ((consumed + count) <= indexLast) {
            consumed += count;
            return consumed;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Determine if any consumption has occurred.
     * @return true if consumption count is greater than zero
     */
    public boolean didConsume() {
        return consumed > 0;
    }

    /**
     * Obtain reference to input glyph sequence, which must not be modified.
     * @return input glyph sequence
     */
    public GlyphSequence getInput() {
        return igs;
    }

    /**
     * Obtain glyph at specified offset from current position.
     * @param offset from current position
     * @return glyph at specified offset from current position
     * @throws IndexOutOfBoundsException if no glyph available at offset
     */
    public int getGlyph(int offset) throws IndexOutOfBoundsException {
        int i = index + offset;
        if ((i >= 0) && (i < indexLast)) {
            return igs.getGlyph(i);
        } else {
            throw new IndexOutOfBoundsException("attempting index at " + i);
        }
    }

    /**
     * Obtain glyph at current position.
     * @return glyph at current position
     * @throws IndexOutOfBoundsException if no glyph available
     */
    public int getGlyph() throws IndexOutOfBoundsException {
        return getGlyph(0);
    }

    /**
     * Set (replace) glyph at specified offset from current position.
     * @param offset from current position
     * @param glyph to set at specified offset from current position
     * @throws IndexOutOfBoundsException if specified offset is not valid position
     */
    public void setGlyph(int offset, int glyph) throws IndexOutOfBoundsException {
        int i = index + offset;
        if ((i >= 0) && (i < indexLast)) {
            igs.setGlyph(i, glyph);
        } else {
            throw new IndexOutOfBoundsException("attempting index at " + i);
        }
    }

    /**
     * Obtain character association of glyph at specified offset from current position.
     * @param offset from current position
     * @return character association of glyph at current position
     * @throws IndexOutOfBoundsException if offset results in an invalid index into input glyph sequence
     */
    public CharAssociation getAssociation(int offset) throws IndexOutOfBoundsException {
        int i = index + offset;
        if ((i >= 0) && (i < indexLast)) {
            return igs.getAssociation(i);
        } else {
            throw new IndexOutOfBoundsException("attempting index at " + i);
        }
    }

    /**
     * Obtain character association of glyph at current position.
     * @return character association of glyph at current position
     * @throws IndexOutOfBoundsException if no glyph available
     */
    public CharAssociation getAssociation() throws IndexOutOfBoundsException {
        return getAssociation(0);
    }

    /**
     * Obtain <code>count</code> glyphs starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then glyphs are returned in reverse order starting at specified offset
     * and going in reverse towards beginning of input glyph sequence.
     * @param offset from current position
     * @param count number of glyphs to obtain
     * @param reverseOrder true if to obtain in reverse order
     * @param ignoreTester glyph tester to use to determine which glyphs are ignored (or null, in which case none are ignored)
     * @param glyphs array to use to fetch glyphs
     * @param counts int[2] array to receive fetched glyph counts, where counts[0] will
     * receive the number of glyphs obtained, and counts[1] will receive the number of glyphs
     * ignored
     * @return array of glyphs
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getGlyphs(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
        if (count < 0) {
            count = getGlyphsAvailable(offset, reverseOrder, ignoreTester) [ 0 ];
        }
        int start = index + offset;
        if (start < 0) {
            throw new IndexOutOfBoundsException("will attempt index at " + start);
        } else if (!reverseOrder && ((start + count) > indexLast)) {
            throw new IndexOutOfBoundsException("will attempt index at " + (start + count));
        } else if (reverseOrder && ((start + 1) < count)) {
            throw new IndexOutOfBoundsException("will attempt index at " + (start - count));
        }
        if (glyphs == null) {
            glyphs = new int [ count ];
        } else if (glyphs.length != count) {
            throw new IllegalArgumentException("glyphs array is non-null, but its length (" + glyphs.length + "), is not equal to count (" + count + ")");
        }
        if (!reverseOrder) {
            return getGlyphsForward(start, count, ignoreTester, glyphs, counts);
        } else {
            return getGlyphsReverse(start, count, ignoreTester, glyphs, counts);
        }
    }

    private int[] getGlyphsForward(int start, int count, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        for (int i = start, n = indexLast; (i < n) && (counted < count); i++) {
            int gi = getGlyph(i - index);
            if (gi == 65535) {
                ignored++;
            } else {
                if ((ignoreTester == null) || !ignoreTester.test(gi, getLookupFlags())) {
                    glyphs [ counted++ ] = gi;
                } else {
                    ignored++;
                }
            }
        }
        if ((counts != null) && (counts.length > 1)) {
            counts[0] = counted;
            counts[1] = ignored;
        }
        return glyphs;
    }

    private int[] getGlyphsReverse(int start, int count, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        for (int i = start; (i >= 0) && (counted < count); i--) {
            int gi = getGlyph(i - index);
            if (gi == 65535) {
                ignored++;
            } else {
                if ((ignoreTester == null) || !ignoreTester.test(gi, getLookupFlags())) {
                    glyphs [ counted++ ] = gi;
                } else {
                    ignored++;
                }
            }
        }
        if ((counts != null) && (counts.length > 1)) {
            counts[0] = counted;
            counts[1] = ignored;
        }
        return glyphs;
    }

    /**
     * Obtain <code>count</code> glyphs starting at specified offset from current position. If
     * offset is negative, then glyphs are returned in reverse order starting at specified offset
     * and going in reverse towards beginning of input glyph sequence.
     * @param offset from current position
     * @param count number of glyphs to obtain
     * @param glyphs array to use to fetch glyphs
     * @param counts int[2] array to receive fetched glyph counts, where counts[0] will
     * receive the number of glyphs obtained, and counts[1] will receive the number of glyphs
     * ignored
     * @return array of glyphs
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getGlyphs(int offset, int count, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
        return getGlyphs(offset, count, offset < 0, ignoreDefault, glyphs, counts);
    }

    /**
     * Obtain all glyphs starting from current position to end of input glyph sequence.
     * @return array of available glyphs
     * @throws IndexOutOfBoundsException if no glyph available
     */
    public int[] getGlyphs() throws IndexOutOfBoundsException {
        return getGlyphs(0, indexLast - index, false, null, null, null);
    }

    /**
     * Obtain <code>count</code> ignored glyphs starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then glyphs are returned in reverse order starting at specified offset
     * and going in reverse towards beginning of input glyph sequence.
     * @param offset from current position
     * @param count number of glyphs to obtain
     * @param reverseOrder true if to obtain in reverse order
     * @param ignoreTester glyph tester to use to determine which glyphs are ignored (or null, in which case none are ignored)
     * @param glyphs array to use to fetch glyphs
     * @param counts int[2] array to receive fetched glyph counts, where counts[0] will
     * receive the number of glyphs obtained, and counts[1] will receive the number of glyphs
     * ignored
     * @return array of glyphs
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getIgnoredGlyphs(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
        return getGlyphs(offset, count, reverseOrder, new NotGlyphTester(ignoreTester), glyphs, counts);
    }

    /**
     * Obtain <code>count</code> ignored glyphs starting at specified offset from current position. If <code>offset</code> is
     * negative, then fetch in reverse order.
     * @param offset from current position
     * @param count number of glyphs to obtain
     * @return array of glyphs
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getIgnoredGlyphs(int offset, int count) throws IndexOutOfBoundsException {
        return getIgnoredGlyphs(offset, count, offset < 0, ignoreDefault, null, null);
    }

    /**
     * Determine if glyph at specified offset from current position is ignored. If <code>offset</code> is
     * negative, then test in reverse order.
     * @param offset from current position
     * @param ignoreTester glyph tester to use to determine which glyphs are ignored (or null, in which case none are ignored)
     * @return true if glyph is ignored
     * @throws IndexOutOfBoundsException if offset results in an
     * invalid index into input glyph sequence
     */
    public boolean isIgnoredGlyph(int offset, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
        return (ignoreTester != null) && ignoreTester.test(getGlyph(offset), getLookupFlags());
    }

    /**
     * Determine if glyph at specified offset from current position is ignored. If <code>offset</code> is
     * negative, then test in reverse order.
     * @param offset from current position
     * @return true if glyph is ignored
     * @throws IndexOutOfBoundsException if offset results in an
     * invalid index into input glyph sequence
     */
    public boolean isIgnoredGlyph(int offset) throws IndexOutOfBoundsException {
        return isIgnoredGlyph(offset, ignoreDefault);
    }

    /**
     * Determine if glyph at current position is ignored.
     * @return true if glyph is ignored
     * @throws IndexOutOfBoundsException if offset results in an
     * invalid index into input glyph sequence
     */
    public boolean isIgnoredGlyph() throws IndexOutOfBoundsException {
        return isIgnoredGlyph(getPosition());
    }

    /**
     * Determine number of glyphs available starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then search backwards in input glyph sequence.
     * @param offset from current position
     * @param reverseOrder true if to obtain in reverse order
     * @param ignoreTester glyph tester to use to determine which glyphs to count (or null, in which case none are ignored)
     * @return an int[2] array where counts[0] is the number of glyphs available, and counts[1] is the number of glyphs ignored
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getGlyphsAvailable(int offset, boolean reverseOrder, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
        int start = index + offset;
        if ((start < 0) || (start > indexLast)) {
            return new int[] { 0, 0 };
        } else if (!reverseOrder) {
            return getGlyphsAvailableForward(start, ignoreTester);
        } else {
            return getGlyphsAvailableReverse(start, ignoreTester);
        }
    }

    private int[] getGlyphsAvailableForward(int start, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        if (ignoreTester == null) {
            counted = indexLast - start;
        } else {
            for (int i = start, n = indexLast; i < n; i++) {
                int gi = getGlyph(i - index);
                if (gi == 65535) {
                    ignored++;
                } else {
                    if (ignoreTester.test(gi, getLookupFlags())) {
                        ignored++;
                    } else {
                        counted++;
                    }
                }
            }
        }
        return new int[] { counted, ignored };
    }

    private int[] getGlyphsAvailableReverse(int start, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        if (ignoreTester == null) {
            counted = start + 1;
        } else {
            for (int i = start; i >= 0; i--) {
                int gi = getGlyph(i - index);
                if (gi == 65535) {
                    ignored++;
                } else {
                    if (ignoreTester.test(gi, getLookupFlags())) {
                        ignored++;
                    } else {
                        counted++;
                    }
                }
            }
        }
        return new int[] { counted, ignored };
    }

    /**
     * Determine number of glyphs available starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then search backwards in input glyph sequence. Uses the
     * default ignores tester.
     * @param offset from current position
     * @param reverseOrder true if to obtain in reverse order
     * @return an int[2] array where counts[0] is the number of glyphs available, and counts[1] is the number of glyphs ignored
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getGlyphsAvailable(int offset, boolean reverseOrder) throws IndexOutOfBoundsException {
        return getGlyphsAvailable(offset, reverseOrder, ignoreDefault);
    }

    /**
     * Determine number of glyphs available starting at specified offset from current position. If
     * offset is negative, then search backwards in input glyph sequence. Uses the
     * default ignores tester.
     * @param offset from current position
     * @return an int[2] array where counts[0] is the number of glyphs available, and counts[1] is the number of glyphs ignored
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int[] getGlyphsAvailable(int offset) throws IndexOutOfBoundsException {
        return getGlyphsAvailable(offset, offset < 0);
    }

    /**
     * Obtain <code>count</code> character associations of glyphs starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then associations are returned in reverse order starting at specified offset
     * and going in reverse towards beginning of input glyph sequence.
     * @param offset from current position
     * @param count number of associations to obtain
     * @param reverseOrder true if to obtain in reverse order
     * @param ignoreTester glyph tester to use to determine which glyphs are ignored (or null, in which case none are ignored)
     * @param associations array to use to fetch associations
     * @param counts int[2] array to receive fetched association counts, where counts[0] will
     * receive the number of associations obtained, and counts[1] will receive the number of glyphs whose
     * associations were ignored
     * @return array of associations
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public CharAssociation[] getAssociations(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts)
        throws IndexOutOfBoundsException {
        if (count < 0) {
            count = getGlyphsAvailable(offset, reverseOrder, ignoreTester) [ 0 ];
        }
        int start = index + offset;
        if (start < 0) {
            throw new IndexOutOfBoundsException("will attempt index at " + start);
        } else if (!reverseOrder && ((start + count) > indexLast)) {
            throw new IndexOutOfBoundsException("will attempt index at " + (start + count));
        } else if (reverseOrder && ((start + 1) < count)) {
            throw new IndexOutOfBoundsException("will attempt index at " + (start - count));
        }
        if (associations == null) {
            associations = new CharAssociation [ count ];
        } else if (associations.length != count) {
            throw new IllegalArgumentException("associations array is non-null, but its length (" + associations.length + "), is not equal to count (" + count + ")");
        }
        if (!reverseOrder) {
            return getAssociationsForward(start, count, ignoreTester, associations, counts);
        } else {
            return getAssociationsReverse(start, count, ignoreTester, associations, counts);
        }
    }

    private CharAssociation[] getAssociationsForward(int start, int count, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts)
        throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        for (int i = start, n = indexLast, k = 0; i < n; i++) {
            int gi = getGlyph(i - index);
            if (gi == 65535) {
                ignored++;
            } else {
                if ((ignoreTester == null) || !ignoreTester.test(gi, getLookupFlags())) {
                    if (k < count) {
                        associations [ k++ ] = getAssociation(i - index);
                        counted++;
                    } else {
                        break;
                    }
                } else {
                    ignored++;
                }
            }
        }
        if ((counts != null) && (counts.length > 1)) {
            counts[0] = counted;
            counts[1] = ignored;
        }
        return associations;
    }

    private CharAssociation[] getAssociationsReverse(int start, int count, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts)
        throws IndexOutOfBoundsException {
        int counted = 0;
        int ignored = 0;
        for (int i = start, k = 0; i >= 0; i--) {
            int gi = getGlyph(i - index);
            if (gi == 65535) {
                ignored++;
            } else {
                if ((ignoreTester == null) || !ignoreTester.test(gi, getLookupFlags())) {
                    if (k < count) {
                        associations [ k++ ] = getAssociation(i - index);
                        counted++;
                    } else {
                        break;
                    }
                } else {
                    ignored++;
                }
            }
        }
        if ((counts != null) && (counts.length > 1)) {
            counts[0] = counted;
            counts[1] = ignored;
        }
        return associations;
    }

    /**
     * Obtain <code>count</code> character associations of glyphs starting at specified offset from current position. If
     * offset is negative, then search backwards in input glyph sequence. Uses the
     * default ignores tester.
     * @param offset from current position
     * @param count number of associations to obtain
     * @return array of associations
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public CharAssociation[] getAssociations(int offset, int count) throws IndexOutOfBoundsException {
        return getAssociations(offset, count, offset < 0, ignoreDefault, null, null);
    }

    /**
     * Obtain <code>count</code> character associations of ignored glyphs starting at specified offset from current position. If
     * <code>reverseOrder</code> is true, then glyphs are returned in reverse order starting at specified offset
     * and going in reverse towards beginning of input glyph sequence.
     * @param offset from current position
     * @param count number of character associations to obtain
     * @param reverseOrder true if to obtain in reverse order
     * @param ignoreTester glyph tester to use to determine which glyphs are ignored (or null, in which case none are ignored)
     * @param associations array to use to fetch associations
     * @param counts int[2] array to receive fetched association counts, where counts[0] will
     * receive the number of associations obtained, and counts[1] will receive the number of glyphs whose
     * associations were ignored
     * @return array of associations
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public CharAssociation[] getIgnoredAssociations(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts)
        throws IndexOutOfBoundsException {
        return getAssociations(offset, count, reverseOrder, new NotGlyphTester(ignoreTester), associations, counts);
    }

    /**
     * Obtain <code>count</code> character associations of ignored glyphs starting at specified offset from current position. If
     * offset is negative, then search backwards in input glyph sequence. Uses the
     * default ignores tester.
     * @param offset from current position
     * @param count number of character associations to obtain
     * @return array of associations
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public CharAssociation[] getIgnoredAssociations(int offset, int count) throws IndexOutOfBoundsException {
        return getIgnoredAssociations(offset, count, offset < 0, ignoreDefault, null, null);
    }

    /**
     * Replace subsequence of input glyph sequence starting at specified offset from current position and of
     * length <code>count</code> glyphs with a subsequence of the sequence <code>gs</code> starting from the specified
     * offset <code>gsOffset</code> of length <code>gsCount</code> glyphs.
     * @param offset from current position
     * @param count number of glyphs to replace, which, if negative means all glyphs from offset to end of input sequence
     * @param gs glyph sequence from which to obtain replacement glyphs
     * @param gsOffset offset of first glyph in replacement sequence
     * @param gsCount count of glyphs in replacement sequence starting at <code>gsOffset</code>
     * @return true if replacement occurred, or false if replacement would result in no change to input glyph sequence
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public boolean replaceInput(int offset, int count, GlyphSequence gs, int gsOffset, int gsCount) throws IndexOutOfBoundsException {
        int nig = (igs != null) ? igs.getGlyphCount() : 0;
        int position = getPosition() + offset;
        if (position < 0) {
            position = 0;
        } else if (position > nig) {
            position = nig;
        }
        if ((count < 0) || ((position + count) > nig)) {
            count = nig - position;
        }
        int nrg = (gs != null) ? gs.getGlyphCount() : 0;
        if (gsOffset < 0) {
            gsOffset = 0;
        } else if (gsOffset > nrg) {
            gsOffset = nrg;
        }
        if ((gsCount < 0) || ((gsOffset + gsCount) > nrg)) {
            gsCount = nrg - gsOffset;
        }
        int ng = nig + gsCount - count;
        IntBuffer gb = IntBuffer.allocate(ng);
        List al = new ArrayList(ng);
        for (int i = 0, n = position; i < n; i++) {
            gb.put(igs.getGlyph(i));
            al.add(igs.getAssociation(i));
        }
        for (int i = gsOffset, n = gsOffset + gsCount; i < n; i++) {
            gb.put(gs.getGlyph(i));
            al.add(gs.getAssociation(i));
        }
        for (int i = position + count, n = nig; i < n; i++) {
            gb.put(igs.getGlyph(i));
            al.add(igs.getAssociation(i));
        }
        gb.flip();
        if (igs.compareGlyphs(gb) != 0) {
            this.igs = new GlyphSequence(igs.getCharacters(), gb, al);
            this.indexLast = gb.limit();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Replace subsequence of input glyph sequence starting at specified offset from current position and of
     * length <code>count</code> glyphs with all glyphs in the replacement sequence <code>gs</code>.
     * @param offset from current position
     * @param count number of glyphs to replace, which, if negative means all glyphs from offset to end of input sequence
     * @param gs glyph sequence from which to obtain replacement glyphs
     * @return true if replacement occurred, or false if replacement would result in no change to input glyph sequence
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public boolean replaceInput(int offset, int count, GlyphSequence gs) throws IndexOutOfBoundsException {
        return replaceInput(offset, count, gs, 0, gs.getGlyphCount());
    }

    /**
     * Erase glyphs in input glyph sequence starting at specified offset from current position, where each glyph
     * in the specified <code>glyphs</code> array is matched, one at a time, and when a (forward searching) match is found
     * in the input glyph sequence, the matching glyph is replaced with the glyph index 65535.
     * @param offset from current position
     * @param glyphs array of glyphs to erase
     * @return the number of glyphs erased, which may be less than the number of specified glyphs
     * @throws IndexOutOfBoundsException if offset or count results in an
     * invalid index into input glyph sequence
     */
    public int erase(int offset, int[] glyphs) throws IndexOutOfBoundsException {
        int start = index + offset;
        if ((start < 0) || (start > indexLast)) {
            throw new IndexOutOfBoundsException("will attempt index at " + start);
        } else {
            int erased = 0;
            for (int i = start - index, n = indexLast - start; i < n; i++) {
                int gi = getGlyph(i);
                if (gi == glyphs [ erased ]) {
                    setGlyph(i, 65535);
                    erased++;
                }
            }
            return erased;
        }
    }

    /**
     * Determine if is possible that the current input sequence satisfies a script specific
     * context testing predicate. If no predicate applies, then application is always possible.
     * @return true if no script specific context tester applies or if a specified tester returns
     * true for the current input sequence context
     */
    public boolean maybeApplicable() {
        if (gct == null) {
            return true;
        } else {
            return gct.test(script, language, feature, igs, index, getLookupFlags());
        }
    }

    /**
     * Apply default application semantices; namely, consume one glyph.
     */
    public void applyDefault() {
        consumed += 1;
    }

    /**
     * Determine if specified glyph is a base glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @return true if glyph definition table records glyph as a base glyph; otherwise, false
     */
    public boolean isBase(int gi) {
        if (gdef != null) {
            return gdef.isGlyphClass(gi, GlyphDefinitionTable.GLYPH_CLASS_BASE);
        } else {
            return false;
        }
    }

    /**
     * Determine if specified glyph is an ignored base glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @param flags that apply to lookup in scope
     * @return true if glyph definition table records glyph as a base glyph; otherwise, false
     */
    public boolean isIgnoredBase(int gi, int flags) {
        return ((flags & GlyphSubtable.LF_IGNORE_BASE) != 0) && isBase(gi);
    }

    /**
     * Determine if specified glyph is an ligature glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @return true if glyph definition table records glyph as a ligature glyph; otherwise, false
     */
    public boolean isLigature(int gi) {
        if (gdef != null) {
            return gdef.isGlyphClass(gi, GlyphDefinitionTable.GLYPH_CLASS_LIGATURE);
        } else {
            return false;
        }
    }

    /**
     * Determine if specified glyph is an ignored ligature glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @param flags that apply to lookup in scope
     * @return true if glyph definition table records glyph as a ligature glyph; otherwise, false
     */
    public boolean isIgnoredLigature(int gi, int flags) {
        return ((flags & GlyphSubtable.LF_IGNORE_LIGATURE) != 0) && isLigature(gi);
    }

    /**
     * Determine if specified glyph is a mark glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @return true if glyph definition table records glyph as a mark glyph; otherwise, false
     */
    public boolean isMark(int gi) {
        if (gdef != null) {
            return gdef.isGlyphClass(gi, GlyphDefinitionTable.GLYPH_CLASS_MARK);
        } else {
            return false;
        }
    }

    /**
     * Determine if specified glyph is an ignored ligature glyph according to the governing
     * glyph definition table.
     * @param gi glyph index to test
     * @param flags that apply to lookup in scope
     * @return true if glyph definition table records glyph as a ligature glyph; otherwise, false
     */
    public boolean isIgnoredMark(int gi, int flags) {
        if ((flags & GlyphSubtable.LF_IGNORE_MARK) != 0) {
            return isMark(gi);
        } else if ((flags & GlyphSubtable.LF_MARK_ATTACHMENT_TYPE) != 0) {
            int lac = (flags & GlyphSubtable.LF_MARK_ATTACHMENT_TYPE) >> 8;
            int gac = gdef.getMarkAttachClass(gi);
            return (gac != lac);
        } else {
            return false;
        }
    }

    /**
     * Obtain an ignored glyph tester that corresponds to the specified lookup flags.
     * @param flags lookup flags
     * @return a glyph tester
     */
    public GlyphTester getIgnoreTester(int flags) {
        if ((flags & GlyphSubtable.LF_IGNORE_BASE) != 0) {
            if ((flags & (GlyphSubtable.LF_IGNORE_LIGATURE | GlyphSubtable.LF_IGNORE_MARK)) == 0) {
                return ignoreBase;
            } else {
                return getCombinedIgnoreTester(flags);
            }
        }
        if ((flags & GlyphSubtable.LF_IGNORE_LIGATURE) != 0) {
            if ((flags & (GlyphSubtable.LF_IGNORE_BASE | GlyphSubtable.LF_IGNORE_MARK)) == 0) {
                return ignoreLigature;
            } else {
                return getCombinedIgnoreTester(flags);
            }
        }
        if ((flags & GlyphSubtable.LF_IGNORE_MARK) != 0) {
            if ((flags & (GlyphSubtable.LF_IGNORE_BASE | GlyphSubtable.LF_IGNORE_LIGATURE)) == 0) {
                return ignoreMark;
            } else {
                return getCombinedIgnoreTester(flags);
            }
        }
        return null;
    }

    /**
     * Obtain an ignored glyph tester that corresponds to the specified multiple (combined) lookup flags.
     * @param flags lookup flags
     * @return a glyph tester
     */
    public GlyphTester getCombinedIgnoreTester(int flags) {
        GlyphTester[] gta = new GlyphTester [ 3 ];
        int ngt = 0;
        if ((flags & GlyphSubtable.LF_IGNORE_BASE) != 0) {
            gta [ ngt++ ] = ignoreBase;
        }
        if ((flags & GlyphSubtable.LF_IGNORE_LIGATURE) != 0) {
            gta [ ngt++ ] = ignoreLigature;
        }
        if ((flags & GlyphSubtable.LF_IGNORE_MARK) != 0) {
            gta [ ngt++ ] = ignoreMark;
        }
        return getCombinedOrTester(gta, ngt);
    }

    /**
     * Obtain an combined OR glyph tester.
     * @param gta an array of glyph testers
     * @param ngt number of glyph testers present in specified array
     * @return a combined OR glyph tester
     */
    public GlyphTester getCombinedOrTester(GlyphTester[] gta, int ngt) {
        if (ngt > 0) {
            return new CombinedOrGlyphTester(gta, ngt);
        } else {
            return null;
        }
    }

    /**
     * Obtain an combined AND glyph tester.
     * @param gta an array of glyph testers
     * @param ngt number of glyph testers present in specified array
     * @return a combined AND glyph tester
     */
    public GlyphTester getCombinedAndTester(GlyphTester[] gta, int ngt) {
        if (ngt > 0) {
            return new CombinedAndGlyphTester(gta, ngt);
        } else {
            return null;
        }
    }

    /** combined OR glyph tester */
    private static class CombinedOrGlyphTester implements GlyphTester {
        private GlyphTester[] gta;
        private int ngt;
        CombinedOrGlyphTester(GlyphTester[] gta, int ngt) {
            this.gta = gta;
            this.ngt = ngt;
        }
        /** {@inheritDoc} */
        public boolean test(int gi, int flags) {
            for (int i = 0, n = ngt; i < n; i++) {
                GlyphTester gt = gta [ i ];
                if (gt != null) {
                    if (gt.test(gi, flags)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /** combined AND glyph tester */
    private static class CombinedAndGlyphTester implements GlyphTester {
        private GlyphTester[] gta;
        private int ngt;
        CombinedAndGlyphTester(GlyphTester[] gta, int ngt) {
            this.gta = gta;
            this.ngt = ngt;
        }
        /** {@inheritDoc} */
        public boolean test(int gi, int flags) {
            for (int i = 0, n = ngt; i < n; i++) {
                GlyphTester gt = gta [ i ];
                if (gt != null) {
                    if (!gt.test(gi, flags)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /** NOT glyph tester */
    private static class NotGlyphTester implements GlyphTester {
        private GlyphTester gt;
        NotGlyphTester(GlyphTester gt) {
            this.gt = gt;
        }
        /** {@inheritDoc} */
        public boolean test(int gi, int flags) {
            if (gt != null) {
                if (gt.test(gi, flags)) {
                    return false;
                }
            }
            return true;
        }
    }

}
