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
 * <p>The <code>GujaratiScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Gujarati script.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class GujaratiScriptProcessor extends IndicScriptProcessor {

    /** logging instance */
    private static final Log log = LogFactory.getLog(GujaratiScriptProcessor.class);

    GujaratiScriptProcessor(String script) {
        super(script);
    }

    @Override
    protected Class<? extends GujaratiSyllabizer> getSyllabizerClass() {
        return GujaratiSyllabizer.class;
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

    private static class GujaratiSyllabizer extends DefaultSyllabizer {
        GujaratiSyllabizer(String script, String language) {
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

    // gujarati character types
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
    // gujarati block range
    static final int CCA_START       =  0x0A80;      // first code point mapped by cca
    static final int CCA_END         =  0x0B00;      // last code point + 1 mapped by cca
    // gujarati character type lookups
    static final short[] CCA = {
        C_U,                        // 0x0A80       // UNASSIGNED
        C_O,                        // 0x0A81       // CANDRABINDU
        C_O,                        // 0x0A82       // ANUSVARA
        C_O,                        // 0x0A83       // VISARGA
        C_U,                        // 0x0A84       // UNASSIGNED
        C_V,                        // 0x0A85       // A
        C_V,                        // 0x0A86       // AA
        C_V,                        // 0x0A87       // I
        C_V,                        // 0x0A88       // II
        C_V,                        // 0x0A89       // U
        C_V,                        // 0x0A8A       // UU
        C_V,                        // 0x0A8B       // VOCALIC R
        C_V,                        // 0x0A8C       // VOCALIC L
        C_V,                        // 0x0A8D       // CANDRA E
        C_U,                        // 0x0A8E       // UNASSIGNED
        C_V,                        // 0x0A8F       // E
        C_V,                        // 0x0A90       // AI
        C_V,                        // 0x0A91       // CANDRA O
        C_U,                        // 0x0A92       // UNASSIGNED
        C_V,                        // 0x0A93       // O
        C_V,                        // 0x0A94       // AU
        C_C,                        // 0x0A95       // KA
        C_C,                        // 0x0A96       // KHA
        C_C,                        // 0x0A97       // GA
        C_C,                        // 0x0A98       // GHA
        C_C,                        // 0x0A99       // NGA
        C_C,                        // 0x0A9A       // CA
        C_C,                        // 0x0A9B       // CHA
        C_C,                        // 0x0A9C       // JA
        C_C,                        // 0x0A9D       // JHA
        C_C,                        // 0x0A9E       // NYA
        C_C,                        // 0x0A9F       // TTA
        C_C,                        // 0x0AA0       // TTHA
        C_C,                        // 0x0AA1       // DDA
        C_C,                        // 0x0AA2       // DDHA
        C_C,                        // 0x0AA3       // NNA
        C_C,                        // 0x0AA4       // TA
        C_C,                        // 0x0AA5       // THA
        C_C,                        // 0x0AA6       // DA
        C_C,                        // 0x0AA7       // DHA
        C_C,                        // 0x0AA8       // NA
        C_U,                        // 0x0AA9       // UNASSIGNED
        C_C,                        // 0x0AAA       // PA
        C_C,                        // 0x0AAB       // PHA
        C_C,                        // 0x0AAC       // BA
        C_C,                        // 0x0AAD       // BHA
        C_C,                        // 0x0AAE       // MA
        C_C,                        // 0x0AAF       // YA
        C_C | C_R,                  // 0x0AB0       // RA
        C_U,                        // 0x0AB1       // UNASSIGNED
        C_C,                        // 0x0AB2       // LA
        C_C,                        // 0x0AB3       // LLA
        C_U,                        // 0x0AB4       // UNASSIGNED
        C_C,                        // 0x0AB5       // VA
        C_C,                        // 0x0AB6       // SHA
        C_C,                        // 0x0AB7       // SSA
        C_C,                        // 0x0AB8       // SA
        C_C,                        // 0x0AB9       // HA
        C_U,                        // 0x0ABA       // UNASSIGNED
        C_U,                        // 0x0ABB       // UNASSIGNED
        C_N,                        // 0x0ABC       // NUKTA
        C_S,                        // 0x0ABD       // AVAGRAHA
        C_M,                        // 0x0ABE       // AA
        C_M | C_PRE,                // 0x0ABF       // I
        C_M,                        // 0x0AC0       // II
        C_M,                        // 0x0AC1       // U
        C_M,                        // 0x0AC2       // UU
        C_M,                        // 0x0AC3       // VOCALIC R
        C_M,                        // 0x0AC4       // VOCALIC RR
        C_M,                        // 0x0AC5       // CANDRA E
        C_U,                        // 0x0AC6       // UNASSIGNED
        C_M,                        // 0x0AC7       // E
        C_M,                        // 0x0AC8       // AI
        C_M,                        // 0x0AC9       // CANDRA O
        C_U,                        // 0x0ACA       // UNASSIGNED
        C_M,                        // 0x0ACB       // O
        C_M,                        // 0x0ACC       // AU
        C_H,                        // 0x0ACD       // VIRAMA (HALANT)
        C_U,                        // 0x0ACE       // UNASSIGNED
        C_U,                        // 0x0ACF       // UNASSIGNED
        C_S,                        // 0x0AD0       // OM
        C_U,                        // 0x0AD1       // UNASSIGNED
        C_U,                        // 0x0AD2       // UNASSIGNED
        C_U,                        // 0x0AD3       // UNASSIGNED
        C_U,                        // 0x0AD4       // UNASSIGNED
        C_U,                        // 0x0AD5       // UNASSIGNED
        C_U,                        // 0x0AD6       // UNASSIGNED
        C_U,                        // 0x0AD7       // UNASSIGNED
        C_U,                        // 0x0AD8       // UNASSIGNED
        C_U,                        // 0x0AD9       // UNASSIGNED
        C_U,                        // 0x0ADA       // UNASSIGNED
        C_U,                        // 0x0ADB       // UNASSIGNED
        C_U,                        // 0x0ADC       // UNASSIGNED
        C_U,                        // 0x0ADD       // UNASSIGNED
        C_U,                        // 0x0ADE       // UNASSIGNED
        C_U,                        // 0x0ADF       // UNASSIGNED
        C_V,                        // 0x0AE0       // VOCALIC RR
        C_V,                        // 0x0AE1       // VOCALIC LL
        C_M,                        // 0x0AE2       // VOCALIC L
        C_M,                        // 0x0AE3       // VOCALIC LL
        C_U,                        // 0x0AE4       // UNASSIGNED
        C_U,                        // 0x0AE5       // UNASSIGNED
        C_D,                        // 0x0AE6       // ZERO
        C_D,                        // 0x0AE7       // ONE
        C_D,                        // 0x0AE8       // TWO
        C_D,                        // 0x0AE9       // THREE
        C_D,                        // 0x0AEA       // FOUR
        C_D,                        // 0x0AEB       // FIVE
        C_D,                        // 0x0AEC       // SIX
        C_D,                        // 0x0AED       // SEVEN
        C_D,                        // 0x0AEE       // EIGHT
        C_D,                        // 0x0AEF       // NINE
        C_U,                        // 0x0AF0       // UNASSIGNED
        C_S,                        // 0x0AF1       // RUPEE SIGN
        C_U,                        // 0x0AF2       // UNASSIGNED
        C_U,                        // 0x0AF3       // UNASSIGNED
        C_U,                        // 0x0AF4       // UNASSIGNED
        C_U,                        // 0x0AF5       // UNASSIGNED
        C_U,                        // 0x0AF6       // UNASSIGNED
        C_U,                        // 0x0AF7       // UNASSIGNED
        C_U,                        // 0x0AF8       // UNASSIGNED
        C_U,                        // 0x0AF9       // UNASSIGNED
        C_U,                        // 0x0AFA       // UNASSIGNED
        C_U,                        // 0x0AFB       // UNASSIGNED
        C_U,                        // 0x0AFC       // UNASSIGNED
        C_U,                        // 0x0AFD       // UNASSIGNED
        C_U,                        // 0x0AFE       // UNASSIGNED
        C_U                         // 0x0AFF       // UNASSIGNED
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
        return c == 0x0ABC;
    }
    static boolean isH(int c) {
        return c == 0x0ACD;
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
