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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fontbox.ttf.advanced.util.CharAssociation;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;

// CSOFF: LineLengthCheck

/**
 * <p>The <code>DevanagariScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Devanagari script.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class DevanagariScriptProcessor extends IndicScriptProcessor {

    /** logging instance */
    private static final Log log = LogFactory.getLog(DevanagariScriptProcessor.class);

    DevanagariScriptProcessor(String script) {
        super(script);
    }

    @Override
    protected Class<? extends DevanagariSyllabizer> getSyllabizerClass() {
        return DevanagariSyllabizer.class;
    }

    @Override
    // find rightmost pre-base matra
    protected int findPreBaseMatra(GlyphSequence gs) {
        int   ng = gs.getGlyphCount();
        int   lk = -1;
        for (int i = ng; i > 0; i--) {
            int k = i - 1;
            if (containsPreBaseMatra(gs, k)) {
                lk = k;
                break;
            }
        }
        return lk;
    }

    @Override
    // find leftmost pre-base matra target, starting from source
    protected int findPreBaseMatraTarget(GlyphSequence gs, int source) {
        int   ng = gs.getGlyphCount();
        int   lk = -1;
        for (int i = (source < ng) ? source : ng; i > 0; i--) {
            int k = i - 1;
            if (containsConsonant(gs, k)) {
                if (containsHalfConsonant(gs, k)) {
                    lk = k;
                } else if (lk == -1) {
                    lk = k;
                } else {
                    break;
                }
            }
        }
        return lk;
    }

    private static boolean containsPreBaseMatra(GlyphSequence gs, int k) {
        CharAssociation a = gs.getAssociation(k);
        int[] ca = gs.getCharacterArray(false);
        for (int i = a.getStart(), e = a.getEnd(); i < e; i++) {
            if (isPreM(ca [ i ])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsConsonant(GlyphSequence gs, int k) {
        CharAssociation a = gs.getAssociation(k);
        int[] ca = gs.getCharacterArray(false);
        for (int i = a.getStart(), e = a.getEnd(); i < e; i++) {
            if (isC(ca [ i ])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsHalfConsonant(GlyphSequence gs, int k) {
        Boolean half = (Boolean) gs.getAssociation(k) .getPredication("half");
        return (half != null) ? half.booleanValue() : false;
    }

    @Override
    protected int findReph(GlyphSequence gs) {
        int   ng = gs.getGlyphCount();
        int   li = -1;
        for (int i = 0; i < ng; i++) {
            if (containsReph(gs, i)) {
                li = i;
                break;
            }
        }
        return li;
    }

    @Override
    protected int findRephTarget(GlyphSequence gs, int source) {
        int   ng = gs.getGlyphCount();
        int   c1 = -1;
        int   c2 = -1;
        // first candidate target is after first non-half consonant
        for (int i = 0; i < ng; i++) {
            if ((i != source) && containsConsonant(gs, i)) {
                if (!containsHalfConsonant(gs, i)) {
                    c1 = i + 1;
                    break;
                }
            }
        }
        // second candidate target is after last non-prebase matra after first candidate or before first syllable or vedic mark
        for (int i = (c1 >= 0) ? c1 : 0; i < ng; i++) {
            if (containsMatra(gs, i) && !containsPreBaseMatra(gs, i)) {
                c2 = i + 1;
            } else if (containsOtherMark(gs, i)) {
                c2 = i;
                break;
            }
        }
        if (c2 >= 0) {
            return c2;
        } else if (c1 >= 0) {
            return c1;
        } else {
            return source;
        }
    }

    private static boolean containsReph(GlyphSequence gs, int k) {
        Boolean rphf = (Boolean) gs.getAssociation(k) .getPredication("rphf");
        return (rphf != null) ? rphf.booleanValue() : false;
    }

    private static boolean containsMatra(GlyphSequence gs, int k) {
        CharAssociation a = gs.getAssociation(k);
        int[] ca = gs.getCharacterArray(false);
        for (int i = a.getStart(), e = a.getEnd(); i < e; i++) {
            if (isM(ca [ i ])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsOtherMark(GlyphSequence gs, int k) {
        CharAssociation a = gs.getAssociation(k);
        int[] ca = gs.getCharacterArray(false);
        for (int i = a.getStart(), e = a.getEnd(); i < e; i++) {
            switch (typeOf(ca [ i ])) {
            case C_T:   // tone (e.g., udatta, anudatta)
            case C_A:   // accent (e.g., acute, grave)
            case C_O:   // other (e.g., candrabindu, anusvara, visarga, etc)
                return true;
            default:
                break;
            }
        }
        return false;
    }

    private static class DevanagariSyllabizer extends DefaultSyllabizer {
        DevanagariSyllabizer(String script, String language) {
            super(script, language);
        }
        @Override
        // | C ...
        protected int findStartOfSyllable(int[] ca, int s, int e) {
            if ((s < 0) || (s >= e)) {
                return -1;
            } else {
                while (s < e) {
                    int c = ca [ s ];
                    if (isC(c)) {
                        break;
                    } else {
                        s++;
                    }
                }
                return s;
            }
        }
        @Override
        // D* L? | ...
        protected int findEndOfSyllable(int[] ca, int s, int e) {
            if ((s < 0) || (s >= e)) {
                return -1;
            } else {
                int nd = 0;
                int nl = 0;
                int i;
                // consume dead consonants
                while ((i = isDeadConsonant(ca, s, e)) > s) {
                    s = i;
                    nd++;
                }
                // consume zero or one live consonant
                if ((i = isLiveConsonant(ca, s, e)) > s) {
                    s = i;
                    nl++;
                }
                return ((nd > 0) || (nl > 0)) ? s : -1;
            }
        }
        // D := ( C N? H )?
        private int isDeadConsonant(int[] ca, int s, int e) {
            if (s < 0) {
                return -1;
            } else {
                int c;
                int i = 0;
                int nc = 0;
                int nh = 0;
                do {
                    // C
                    if ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isC(c)) {
                            i++;
                            nc++;
                        } else {
                            break;
                        }
                    }
                    // N?
                    if ((s + i) < e) {
                        c = ca [ s + 1 ];
                        if (isN(c)) {
                            i++;
                        }
                    }
                    // H
                    if ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isH(c)) {
                            i++;
                            nh++;
                        } else {
                            break;
                        }
                    }
                } while (false);
                return (nc > 0) && (nh > 0) ? s + i : -1;
            }
        }
        // L := ( (C|V) N? X* )?; where X = ( MATRA | ACCENT MARK | TONE MARK | OTHER MARK )
        private int isLiveConsonant(int[] ca, int s, int e) {
            if (s < 0) {
                return -1;
            } else {
                int c;
                int i = 0;
                int nc = 0;
                int nv = 0;
                int nx = 0;
                do {
                    // C
                    if ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isC(c)) {
                            i++;
                            nc++;
                        } else if (isV(c)) {
                            i++;
                            nv++;
                        } else {
                            break;
                        }
                    }
                    // N?
                    if ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isN(c)) {
                            i++;
                        }
                    }
                    // X*
                    while ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isX(c)) {
                            i++;
                            nx++;
                        } else {
                            break;
                        }
                    }
                } while (false);
                // if no X but has H, then ignore C|I
                if (nx == 0) {
                    if ((s + i) < e) {
                        c = ca [ s + i ];
                        if (isH(c)) {
                            if (nc > 0) {
                                nc--;
                            } else if (nv > 0) {
                                nv--;
                            }
                        }
                    }
                }
                return ((nc > 0) || (nv > 0)) ? s + i : -1;
            }
        }
    }

    // devanagari character types
    static final short C_U          = 0;            // unassigned
    static final short C_C          = 1;            // consonant
    static final short C_V          = 2;            // vowel
    static final short C_M          = 3;            // vowel sign (matra)
    static final short C_S          = 4;            // symbol or sign
    static final short C_T          = 5;            // tone mark
    static final short C_A          = 6;            // accent mark
    static final short C_P          = 7;            // punctuation
    static final short C_D          = 8;            // digit
    static final short C_H          = 9;            // halant (virama)
    static final short C_O          = 10;           // other signs
    static final short C_N          = 0x0100;       // nukta(ized)
    static final short C_R          = 0x0200;       // reph(ized)
    static final short C_PRE        = 0x0400;       // pre-base
    static final short C_M_TYPE     = 0x00FF;       // type mask
    static final short C_M_FLAGS    = 0x7F00;       // flag mask
    // devanagari block range
    static final int CCA_START       =  0x0900;      // first code point mapped by cca
    static final int CCA_END         =  0x0980;      // last code point + 1 mapped by cca
    // devanagari character type lookups
    static final short[] CCA = {
        C_O,                        // 0x0900       // INVERTED CANDRABINDU
        C_O,                        // 0x0901       // CANDRABINDU
        C_O,                        // 0x0902       // ANUSVARA
        C_O,                        // 0x0903       // VISARGA
        C_V,                        // 0x0904       // SHORT A
        C_V,                        // 0x0905       // A
        C_V,                        // 0x0906       // AA
        C_V,                        // 0x0907       // I
        C_V,                        // 0x0908       // II
        C_V,                        // 0x0909       // U
        C_V,                        // 0x090A       // UU
        C_V,                        // 0x090B       // VOCALIC R
        C_V,                        // 0x090C       // VOCALIC L
        C_V,                        // 0x090D       // CANDRA E
        C_V,                        // 0x090E       // SHORT E
        C_V,                        // 0x090F       // E
        C_V,                        // 0x0910       // AI
        C_V,                        // 0x0911       // CANDRA O
        C_V,                        // 0x0912       // SHORT O
        C_V,                        // 0x0913       // O
        C_V,                        // 0x0914       // AU
        C_C,                        // 0x0915       // KA
        C_C,                        // 0x0916       // KHA
        C_C,                        // 0x0917       // GA
        C_C,                        // 0x0918       // GHA
        C_C,                        // 0x0919       // NGA
        C_C,                        // 0x091A       // CA
        C_C,                        // 0x091B       // CHA
        C_C,                        // 0x091C       // JA
        C_C,                        // 0x091D       // JHA
        C_C,                        // 0x091E       // NYA
        C_C,                        // 0x091F       // TTA
        C_C,                        // 0x0920       // TTHA
        C_C,                        // 0x0921       // DDA
        C_C,                        // 0x0922       // DDHA
        C_C,                        // 0x0923       // NNA
        C_C,                        // 0x0924       // TA
        C_C,                        // 0x0925       // THA
        C_C,                        // 0x0926       // DA
        C_C,                        // 0x0927       // DHA
        C_C,                        // 0x0928       // NA
        C_C,                        // 0x0929       // NNNA
        C_C,                        // 0x092A       // PA
        C_C,                        // 0x092B       // PHA
        C_C,                        // 0x092C       // BA
        C_C,                        // 0x092D       // BHA
        C_C,                        // 0x092E       // MA
        C_C,                        // 0x092F       // YA
        C_C | C_R,                  // 0x0930       // RA
        C_C | C_R | C_N,            // 0x0931       // RRA          = 0930+093C
        C_C,                        // 0x0932       // LA
        C_C,                        // 0x0933       // LLA
        C_C,                        // 0x0934       // LLLA
        C_C,                        // 0x0935       // VA
        C_C,                        // 0x0936       // SHA
        C_C,                        // 0x0937       // SSA
        C_C,                        // 0x0938       // SA
        C_C,                        // 0x0939       // HA
        C_M,                        // 0x093A       // OE (KASHMIRI)
        C_M,                        // 0x093B       // OOE (KASHMIRI)
        C_N,                        // 0x093C       // NUKTA
        C_S,                        // 0x093D       // AVAGRAHA
        C_M,                        // 0x093E       // AA
        C_M | C_PRE,                // 0x093F       // I
        C_M,                        // 0x0940       // II
        C_M,                        // 0x0941       // U
        C_M,                        // 0x0942       // UU
        C_M,                        // 0x0943       // VOCALIC R
        C_M,                        // 0x0944       // VOCALIC RR
        C_M,                        // 0x0945       // CANDRA E
        C_M,                        // 0x0946       // SHORT E
        C_M,                        // 0x0947       // E
        C_M,                        // 0x0948       // AI
        C_M,                        // 0x0949       // CANDRA O
        C_M,                        // 0x094A       // SHORT O
        C_M,                        // 0x094B       // O
        C_M,                        // 0x094C       // AU
        C_H,                        // 0x094D       // VIRAMA (HALANT)
        C_M,                        // 0x094E       // PRISHTHAMATRA E
        C_M,                        // 0x094F       // AW
        C_S,                        // 0x0950       // OM
        C_T,                        // 0x0951       // UDATTA
        C_T,                        // 0x0952       // ANUDATTA
        C_A,                        // 0x0953       // GRAVE
        C_A,                        // 0x0954       // ACUTE
        C_M,                        // 0x0955       // CANDRA LONG E
        C_M,                        // 0x0956       // UE
        C_M,                        // 0x0957       // UUE
        C_C | C_N,                  // 0x0958       // QA
        C_C | C_N,                  // 0x0959       // KHHA
        C_C | C_N,                  // 0x095A       // GHHA
        C_C | C_N,                  // 0x095B       // ZA
        C_C | C_N,                  // 0x095C       // DDDHA
        C_C | C_N,                  // 0x095D       // RHA
        C_C | C_N,                  // 0x095E       // FA
        C_C | C_N,                  // 0x095F       // YYA
        C_V,                        // 0x0960       // VOCALIC RR
        C_V,                        // 0x0961       // VOCALIC LL
        C_M,                        // 0x0962       // VOCALIC RR
        C_M,                        // 0x0963       // VOCALIC LL
        C_P,                        // 0x0964       // DANDA
        C_P,                        // 0x0965       // DOUBLE DANDA
        C_D,                        // 0x0966       // ZERO
        C_D,                        // 0x0967       // ONE
        C_D,                        // 0x0968       // TWO
        C_D,                        // 0x0969       // THREE
        C_D,                        // 0x096A       // FOUR
        C_D,                        // 0x096B       // FIVE
        C_D,                        // 0x096C       // SIX
        C_D,                        // 0x096D       // SEVEN
        C_D,                        // 0x096E       // EIGHT
        C_D,                        // 0x096F       // NINE
        C_S,                        // 0x0970       // ABBREVIATION SIGN
        C_S,                        // 0x0971       // HIGH SPACING DOT
        C_V,                        // 0x0972       // CANDRA A (MARATHI)
        C_V,                        // 0x0973       // OE (KASHMIRI)
        C_V,                        // 0x0974       // OOE (KASHMIRI)
        C_V,                        // 0x0975       // AW (KASHMIRI)
        C_V,                        // 0x0976       // UE (KASHMIRI)
        C_V,                        // 0x0977       // UUE (KASHMIRI)
        C_U,                        // 0x0978       // UNASSIGNED
        C_C,                        // 0x0979       // ZHA
        C_C,                        // 0x097A       // HEAVY YA
        C_C,                        // 0x097B       // GGAA (SINDHI)
        C_C,                        // 0x097C       // JJA (SINDHI)
        C_C,                        // 0x097D       // GLOTTAL STOP (LIMBU)
        C_C,                        // 0x097E       // DDDA (SINDHI)
        C_C                         // 0x097F       // BBA (SINDHI)
    };
    static int typeOf(int c) {
        if ((c >= CCA_START) && (c < CCA_END)) {
            return CCA [ c - CCA_START ] & C_M_TYPE;
        } else {
            return C_U;
        }
    }
    static boolean isType(int c, int t) {
        return typeOf(c) == t;
    }
    static boolean hasFlag(int c, int f) {
        if ((c >= CCA_START) && (c < CCA_END)) {
            return (CCA [ c - CCA_START ] & f) == f;
        } else {
            return false;
        }
    }
    static boolean isC(int c) {
        return isType(c, C_C);
    }
    static boolean isR(int c) {
        return isType(c, C_C) && hasR(c);
    }
    static boolean isV(int c) {
        return isType(c, C_V);
    }
    static boolean isN(int c) {
        return c == 0x093C;
    }
    static boolean isH(int c) {
        return c == 0x094D;
    }
    static boolean isM(int c) {
        return isType(c, C_M);
    }
    static boolean isPreM(int c) {
        return isType(c, C_M) && hasFlag(c, C_PRE);
    }
    static boolean isX(int c) {
        switch (typeOf(c)) {
        case C_M: // matra (combining vowel)
        case C_A: // accent mark
        case C_T: // tone mark
        case C_O: // other (modifying) mark
            return true;
        default:
            return false;
        }
    }
    static boolean hasR(int c) {
        return hasFlag(c, C_R);
    }
    static boolean hasN(int c) {
        return hasFlag(c, C_N);
    }

}
