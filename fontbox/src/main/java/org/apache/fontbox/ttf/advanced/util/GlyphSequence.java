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

package org.apache.fontbox.ttf.advanced.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

// CSOFF: LineLengthCheck

/**
 * <p>A GlyphSequence encapsulates a sequence of character codes, a sequence of glyph codes,
 * and a sequence of character associations, where, for each glyph in the sequence of glyph
 * codes, there is a corresponding character association. Character associations server to
 * relate the glyph codes in a glyph sequence to the specific characters in an original
 * character code sequence with which the glyph codes are associated.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class GlyphSequence implements Cloneable {

    /** default character buffer capacity in case new character buffer is created */
    private static final int DEFAULT_CHARS_CAPACITY = 8;

    /** character buffer */
    private IntBuffer characters;
    /** glyph buffer */
    private IntBuffer glyphs;
    /** association list */
    private List<CharAssociation> associations;
    /** predications flag */
    private boolean predications;

    /**
     * Instantiate a glyph sequence, reusing (i.e., not copying) the referenced
     * character and glyph buffers and associations. If characters is null, then
     * an empty character buffer is created. If glyphs is null, then a glyph buffer
     * is created whose capacity is that of the character buffer. If associations is
     * null, then identity associations are created.
     * @param characters a (possibly null) buffer of associated (originating) characters
     * @param glyphs a (possibly null) buffer of glyphs
     * @param associations a (possibly null) array of glyph to character associations
     * @param predications true if predications are enabled
     */
    public GlyphSequence(IntBuffer characters, IntBuffer glyphs, List<CharAssociation> associations, boolean predications) {
        if (characters == null) {
            characters = IntBuffer.allocate(DEFAULT_CHARS_CAPACITY);
        }
        if (glyphs == null) {
            glyphs = IntBuffer.allocate(characters.capacity());
        }
        if (associations == null) {
            associations = makeIdentityAssociations(characters.limit(), glyphs.limit());
        }
        this.characters = characters;
        this.glyphs = glyphs;
        this.associations = associations;
        this.predications = predications;
    }

    /**
     * Instantiate a glyph sequence, reusing (i.e., not copying) the referenced
     * character and glyph buffers and associations. If characters is null, then
     * an empty character buffer is created. If glyphs is null, then a glyph buffer
     * is created whose capacity is that of the character buffer. If associations is
     * null, then identity associations are created.
     * @param characters a (possibly null) buffer of associated (originating) characters
     * @param glyphs a (possibly null) buffer of glyphs
     * @param associations a (possibly null) array of glyph to character associations
     */
    public GlyphSequence(IntBuffer characters, IntBuffer glyphs, List<CharAssociation> associations) {
        this (characters, glyphs, associations, false);
    }

    /**
     * Instantiate a glyph sequence using an existing glyph sequence, where the new glyph sequence shares
     * the character array of the existing sequence (but not the buffer object), and creates new copies
     * of glyphs buffer and association list.
     * @param gs an existing glyph sequence
     */
    public GlyphSequence(GlyphSequence gs) {
        this (gs.characters.duplicate(), copyBuffer(gs.glyphs), copyAssociations(gs.associations), gs.predications);
    }

    /**
     * Instantiate a glyph sequence using an existing glyph sequence, where the new glyph sequence shares
     * the character array of the existing sequence (but not the buffer object), but uses the specified
     * backtrack, input, and lookahead glyph arrays to populate the glyphs, and uses the specified
     * of glyphs buffer and association list.
     * backtrack, input, and lookahead association arrays to populate the associations.
     * @param gs an existing glyph sequence
     * @param bga backtrack glyph array
     * @param iga input glyph array
     * @param lga lookahead glyph array
     * @param bal backtrack association list
     * @param ial input association list
     * @param lal lookahead association list
     */
    public GlyphSequence(GlyphSequence gs, int[] bga, int[] iga, int[] lga, CharAssociation[] bal, CharAssociation[] ial, CharAssociation[] lal) {
        this (gs.characters.duplicate(), concatGlyphs(bga, iga, lga), concatAssociations(bal, ial, lal), gs.predications);
    }

    /**
     * Obtain reference to underlying character buffer.
     * @return character buffer reference
     */
    public IntBuffer getCharacters() {
        return characters;
    }

    /**
     * Obtain array of characters. If <code>copy</code> is true, then
     * a newly instantiated array is returned, otherwise a reference to
     * the underlying buffer's array is returned. N.B. in case a reference
     * to the undelying buffer's array is returned, the length
     * of the array is not necessarily the number of characters in array.
     * To determine the number of characters, use {@link #getCharacterCount}.
     * @param copy true if to return a newly instantiated array of characters
     * @return array of characters
     */
    public int[] getCharacterArray(boolean copy) {
        if (copy) {
            return toArray(characters);
        } else {
            return characters.array();
        }
    }

    /**
     * Obtain the number of characters in character array, where
     * each character constitutes a unicode scalar value.
     * @return number of characters available in character array
     */
    public int getCharacterCount() {
        return characters.limit();
    }

    /**
     * Obtain glyph id at specified index.
     * @param index to obtain glyph
     * @return the glyph identifier of glyph at specified index
     * @throws IndexOutOfBoundsException if index is less than zero
     * or exceeds last valid position
     */
    public int getGlyph(int index) throws IndexOutOfBoundsException {
        return glyphs.get(index);
    }

    /**
     * Set glyph id at specified index.
     * @param index to set glyph
     * @param gi glyph index
     * @throws IndexOutOfBoundsException if index is greater or equal to
     * the limit of the underlying glyph buffer
     */
    public void setGlyph(int index, int gi) throws IndexOutOfBoundsException {
        if (gi > 65535) {
            gi = 65535;
        }
        glyphs.put(index, gi);
    }

    /**
     * Obtain reference to underlying glyph buffer.
     * @return glyph buffer reference
     */
    public IntBuffer getGlyphs() {
        return glyphs;
    }

    /**
     * Obtain count glyphs starting at offset. If <code>count</code> is
     * negative, then it is treated as if the number of available glyphs
     * were specified.
     * @param offset into glyph sequence
     * @param count of glyphs to obtain starting at offset, or negative,
     * indicating all avaialble glyphs starting at offset
     * @return glyph array
     */
    public int[] getGlyphs(int offset, int count) {
        int ng = getGlyphCount();
        if (offset < 0) {
            offset = 0;
        } else if (offset > ng) {
            offset = ng;
        }
        if (count < 0) {
            count = ng - offset;
        }
        int[] ga = new int [ count ];
        for (int i = offset, n = offset + count, k = 0; i < n; i++) {
            if (k < ga.length) {
                ga [ k++ ] = glyphs.get(i);
            }
        }
        return ga;
    }

    /**
     * Obtain array of glyphs. If <code>copy</code> is true, then
     * a newly instantiated array is returned, otherwise a reference to
     * the underlying buffer's array is returned. N.B. in case a reference
     * to the undelying buffer's array is returned, the length
     * of the array is not necessarily the number of glyphs in array.
     * To determine the number of glyphs, use {@link #getGlyphCount}.
     * @param copy true if to return a newly instantiated array of glyphs
     * @return array of glyphs
     */
    public int[] getGlyphArray(boolean copy) {
        if (copy) {
            return toArray(glyphs);
        } else {
            return glyphs.array();
        }
    }

    /**
     * Obtain the number of glyphs in glyphs array, where
     * each glyph constitutes a font specific glyph index.
     * @return number of glyphs available in character array
     */
    public int getGlyphCount() {
        return glyphs.limit();
    }

    /**
     * Obtain association at specified index.
     * @param index into associations array
     * @return glyph to character associations at specified index
     * @throws IndexOutOfBoundsException if index is less than zero
     * or exceeds last valid position
     */
    public CharAssociation getAssociation(int index) throws IndexOutOfBoundsException {
        return (CharAssociation) associations.get(index);
    }

    /**
     * Obtain reference to underlying associations list.
     * @return associations list
     */
    public List<CharAssociation> getAssociations() {
        return associations;
    }

    /**
     * Obtain count associations starting at offset.
     * @param offset into glyph sequence
     * @param count of associations to obtain starting at offset, or negative,
     * indicating all avaialble associations starting at offset
     * @return associations
     */
    public CharAssociation[] getAssociations(int offset, int count) {
        int ng = getGlyphCount();
        if (offset < 0) {
            offset = 0;
        } else if (offset > ng) {
            offset = ng;
        }
        if (count < 0) {
            count = ng - offset;
        }
        CharAssociation[] aa = new CharAssociation [ count ];
        for (int i = offset, n = offset + count, k = 0; i < n; i++) {
            if (k < aa.length) {
                aa [ k++ ] = (CharAssociation) associations.get(i);
            }
        }
        return aa;
    }

    /**
     * Enable or disable predications.
     * @param enable true if predications are to be enabled; otherwise false to disable
     */
    public void setPredications(boolean enable) {
        this.predications = enable;
    }

    /**
     * Obtain predications state.
     * @return true if predications are enabled
     */
    public boolean getPredications() {
        return this.predications;
    }

    /**
     * Set predication &lt;KEY,VALUE&gt; at glyph sequence OFFSET.
     * @param offset offset (index) into glyph sequence
     * @param key predication key
     * @param value predication value
     */
    public void setPredication(int offset, String key, Object value) {
        if (predications) {
            CharAssociation[] aa = getAssociations(offset, 1);
            CharAssociation   ca = aa[0];
            ca.setPredication(key, value);
        }
    }

    /**
     * Get predication KEY at glyph sequence OFFSET.
     * @param offset offset (index) into glyph sequence
     * @param key predication key
     * @return predication KEY at OFFSET or null if none exists
     */
    public Object getPredication(int offset, String key) {
        if (predications) {
            CharAssociation[] aa = getAssociations(offset, 1);
            CharAssociation   ca = aa[0];
            return ca.getPredication(key);
        } else {
            return null;
        }
    }

    /**
     * Compare glyphs.
     * @param gb buffer containing glyph indices with which this glyph sequence's glyphs are to be compared
     * @return zero if glyphs are the same, otherwise returns 1 or -1 according to whether this glyph sequence's
     * glyphs are lexicographically greater or lesser than the glyphs in the specified string buffer
     */
    public int compareGlyphs(IntBuffer gb) {
        int ng = getGlyphCount();
        for (int i = 0, n = gb.limit(); i < n; i++) {
            if (i < ng) {
                int g1 = glyphs.get(i);
                int g2 = gb.get(i);
                if (g1 > g2) {
                    return 1;
                } else if (g1 < g2) {
                    return -1;
                }
            } else {
                return -1;              // this gb is a proper prefix of specified gb
            }
        }
        return 0;                       // same lengths with no difference
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        try {
            GlyphSequence gs = (GlyphSequence) super.clone();
            gs.characters = copyBuffer(characters);
            gs.glyphs = copyBuffer(glyphs);
            gs.associations = copyAssociations(associations);
            return gs;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("chars = [");
        sb.append(characters);
        sb.append("], glyphs = [");
        sb.append(glyphs);
        sb.append("], associations = [");
        sb.append(associations);
        sb.append("]");
        sb.append('}');
        return sb.toString();
    }

    /**
     * Determine if two arrays of glyphs are identical.
     * @param ga1 first glyph array
     * @param ga2 second glyph array
     * @return true if arrays are botth null or both non-null and have identical elements
     */
    public static boolean sameGlyphs(int[] ga1, int[] ga2) {
        if (ga1 == ga2) {
            return true;
        } else if ((ga1 == null) || (ga2 == null)) {
            return false;
        } else if (ga1.length != ga2.length) {
            return false;
        } else {
            for (int i = 0, n = ga1.length; i < n; i++) {
                if (ga1[i] != ga2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Concatenante glyph arrays.
     * @param bga backtrack glyph array
     * @param iga input glyph array
     * @param lga lookahead glyph array
     * @return new integer buffer containing concatenated glyphs
     */
    public static IntBuffer concatGlyphs(int[] bga, int[] iga, int[] lga) {
        int ng = 0;
        if (bga != null) {
            ng += bga.length;
        }
        if (iga != null) {
            ng += iga.length;
        }
        if (lga != null) {
            ng += lga.length;
        }
        IntBuffer gb = IntBuffer.allocate(ng);
        if (bga != null) {
            gb.put(bga);
        }
        if (iga != null) {
            gb.put(iga);
        }
        if (lga != null) {
            gb.put(lga);
        }
        gb.flip();
        return gb;
    }

    /**
     * Concatenante association arrays.
     * @param baa backtrack association array
     * @param iaa input association array
     * @param laa lookahead association array
     * @return new list containing concatenated associations
     */
    public static List<CharAssociation> concatAssociations(CharAssociation[] baa, CharAssociation[] iaa, CharAssociation[] laa) {
        int na = 0;
        if (baa != null) {
            na += baa.length;
        }
        if (iaa != null) {
            na += iaa.length;
        }
        if (laa != null) {
            na += laa.length;
        }
        if (na > 0) {
            List<CharAssociation> gl = new ArrayList<>(na);
            if (baa != null) {
                for (int i = 0; i < baa.length; i++) {
                    gl.add(baa[i]);
                }
            }
            if (iaa != null) {
                for (int i = 0; i < iaa.length; i++) {
                    gl.add(iaa[i]);
                }
            }
            if (laa != null) {
                for (int i = 0; i < laa.length; i++) {
                    gl.add(laa[i]);
                }
            }
            return gl;
        } else {
            return null;
        }
    }

    /**
     * Join (concatenate) glyph sequences.
     * @param gs original glyph sequence from which to reuse character array reference
     * @param sa array of glyph sequences, whose glyph arrays and association lists are to be concatenated
     * @return new glyph sequence referring to character array of GS and concatenated glyphs and associations of SA
     */
    public static GlyphSequence join(GlyphSequence gs, GlyphSequence[] sa) {
        assert sa != null;
        int tg = 0;
        int ta = 0;
        for (int i = 0, n = sa.length; i < n; i++) {
            GlyphSequence s = sa [ i ];
            IntBuffer ga = s.getGlyphs();
            assert ga != null;
            int ng = ga.limit();
            List<CharAssociation> al = s.getAssociations();
            assert al != null;
            int na = al.size();
            assert na == ng;
            tg += ng;
            ta += na;
        }
        IntBuffer uga = IntBuffer.allocate(tg);
        ArrayList<CharAssociation> ual = new ArrayList<>(ta);
        for (int i = 0, n = sa.length; i < n; i++) {
            GlyphSequence s = sa [ i ];
            uga.put(s.getGlyphs());
            ual.addAll(s.getAssociations());
        }
        return new GlyphSequence(gs.getCharacters(), uga, ual, gs.getPredications());
    }

    /**
     * Reorder sequence such that [SOURCE,SOURCE+COUNT) is moved just prior to TARGET.
     * @param gs input sequence
     * @param source index of sub-sequence to reorder
     * @param count length of sub-sequence to reorder
     * @param target index to which source sub-sequence is to be moved
     * @return reordered sequence (or original if no reordering performed)
     */
    public static GlyphSequence reorder(GlyphSequence gs, int source, int count, int target) {
        if (source != target) {
            int   ng  = gs.getGlyphCount();
            int[] ga  = gs.getGlyphArray(false);
            int[] nga = new int [ ng ];
            CharAssociation[] aa  = gs.getAssociations(0, ng);
            CharAssociation[] naa = new CharAssociation [ ng ];
            if (source < target) {
                int t = 0;
                for (int s = 0, e = source; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = source + count, e = target; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = source, e = source + count; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = target, e = ng; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
            } else {
                int t = 0;
                for (int s = 0, e = target; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = source, e = source + count; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = target, e = source; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
                for (int s = source + count, e = ng; s < e; s++, t++) {
                    nga[t] = ga[s];
                    naa[t] = aa[s];
                }
            }
            return new GlyphSequence(gs, null, nga, null, null, naa, null);
        } else {
            return gs;
        }
    }

    private static int[] toArray(IntBuffer ib) {
        if (ib != null) {
            int n = ib.limit();
            int[] ia = new int[n];
            ib.get(ia, 0, n);
            return ia;
        } else {
            return new int[0];
        }
    }

    private static List<CharAssociation> makeIdentityAssociations(int numChars, int numGlyphs) {
        int nc = numChars;
        int ng = numGlyphs;
        List<CharAssociation> av = new ArrayList<>(ng);
        for (int i = 0, n = ng; i < n; i++) {
            int k = (i > nc) ? nc : i;
            av.add(new CharAssociation(i, (k == nc) ? 0 : 1));
        }
        return av;
    }

    private static IntBuffer copyBuffer(IntBuffer ib) {
        if (ib != null) {
            int[] ia = new int [ ib.capacity() ];
            int   p  = ib.position();
            int   l  = ib.limit();
            System.arraycopy(ib.array(), 0, ia, 0, ia.length);
            return IntBuffer.wrap(ia, p, l - p);
        } else {
            return null;
        }
    }

    private static List<CharAssociation> copyAssociations(List<CharAssociation> ca) {
        if (ca != null) {
            return new ArrayList<>(ca);
        } else {
            return ca;
        }
    }

}
