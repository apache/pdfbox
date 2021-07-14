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
 * <p>The <code>TamilScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Tamil script.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class TamilScriptProcessor extends IndicScriptProcessor {

    /** logging instance */
    private static final Log log = LogFactory.getLog(TamilScriptProcessor.class);

    TamilScriptProcessor(String script) {
        super(script);
    }

    @Override
    protected Class<? extends TamilSyllabizer> getSyllabizerClass() {
        return TamilSyllabizer.class;
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
        Boolean half = (Boolean) gs.getAssociation(k).getPredication("half");
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
        Boolean rphf = (Boolean) gs.getAssociation(k).getPredication("rphf");
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

    private static class TamilSyllabizer extends DefaultSyllabizer {
        TamilSyllabizer(String script, String language) {
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

    // tamil character types
    static final short C_U          = 0;                        // unassigned
    static final short C_C          = 1;                        // consonant
    static final short C_V          = 2;                        // vowel
    static final short C_M          = 3;                        // vowel sign (matra)
    static final short C_S          = 4;                        // symbol or sign
    static final short C_T          = 5;                        // tone mark
    static final short C_A          = 6;                        // accent mark
    static final short C_P          = 7;                        // punctuation
    static final short C_D          = 8;                        // digit
    static final short C_H          = 9;                        // halant (virama)
    static final short C_O          = 10;                       // other signs
    static final short C_N          = 0x0100;                   // nukta(ized)
    static final short C_R          = 0x0200;                   // reph(ized)
    static final short C_PRE        = 0x0400;                   // pre-base
    static final short C_POST       = 0x1000;                   // post-base
    static final short C_WRAP       = C_PRE | C_POST;           // wrap (two part) vowel
    static final short C_M_TYPE     = 0x00FF;                   // type mask
    static final short C_M_FLAGS    = 0x7F00;                   // flag mask
    // tamil block range
    static final int CCA_START       =  0x0B80;                 // first code point mapped by cca
    static final int CCA_END         =  0x0C00;                 // last code point + 1 mapped by cca
    // tamil character type lookups
    static final short[] CCA = {
        C_U,                        // 0x0B80                   //
        C_U,                        // 0x0B81                   //
        C_O,                        // 0x0B82                   // ANUSVARA
        C_O,                        // 0x0B83                   // VISARGA
        C_U,                        // 0x0B84                   //
        C_V,                        // 0x0B85                   // A
        C_V,                        // 0x0B86                   // AA
        C_V,                        // 0x0B87                   // I
        C_V,                        // 0x0B88                   // II
        C_V,                        // 0x0B89                   // U
        C_V,                        // 0x0B8A                   // UU
        C_U,                        // 0x0B8B                   //
        C_U,                        // 0x0B8C                   //
        C_U,                        // 0x0B8D                   //
        C_V,                        // 0x0B8E                   // E
        C_V,                        // 0x0B8F                   // EE
        C_V,                        // 0x0B90                   // AI
        C_U,                        // 0x0B91                   //
        C_V,                        // 0x0B92                   // O
        C_V,                        // 0x0B93                   // OO
        C_V,                        // 0x0B94                   // AU
        C_C,                        // 0x0B95                   // KA
        C_U,                        // 0x0B96                   //
        C_U,                        // 0x0B97                   //
        C_U,                        // 0x0B98                   //
        C_C,                        // 0x0B99                   // NGA
        C_C,                        // 0x0B9A                   // CA
        C_U,                        // 0x0B9B                   //
        C_C,                        // 0x0B9C                   // JA
        C_U,                        // 0x0B9D                   //
        C_C,                        // 0x0B9E                   // NYA
        C_C,                        // 0x0B9F                   // TTA
        C_U,                        // 0x0BA0                   //
        C_U,                        // 0x0BA1                   //
        C_U,                        // 0x0BA2                   //
        C_C,                        // 0x0BA3                   // NNA
        C_C,                        // 0x0BA4                   // TA
        C_U,                        // 0x0BA5                   //
        C_U,                        // 0x0BA6                   //
        C_U,                        // 0x0BA7                   //
        C_C,                        // 0x0BA8                   // NA
        C_C,                        // 0x0BA9                   // NNNA
        C_C,                        // 0x0BAA                   // PA
        C_U,                        // 0x0BAB                   //
        C_U,                        // 0x0BAC                   //
        C_U,                        // 0x0BAD                   //
        C_C,                        // 0x0BAE                   // MA
        C_C,                        // 0x0BAF                   // YA
        C_C | C_R,                  // 0x0BB0                   // RA
        C_C | C_R,                  // 0x0BB1                   // RRA
        C_C,                        // 0x0BB2                   // LA
        C_C,                        // 0x0BB3                   // LLA
        C_C,                        // 0x0BB4                   // LLLA
        C_C,                        // 0x0BB5                   // VA
        C_C,                        // 0x0BB6                   // SHA
        C_C,                        // 0x0BB7                   // SSA
        C_C,                        // 0x0BB8                   // SA
        C_C,                        // 0x0BB9                   // HA
        C_U,                        // 0x0BBA                   //
        C_U,                        // 0x0BBB                   //
        C_U,                        // 0x0BBC                   //
        C_U,                        // 0x0BBD                   //
        C_M,                        // 0x0BBE                   // AA
        C_M,                        // 0x0BBF                   // I
        C_M,                        // 0x0BC0                   // II
        C_M,                        // 0x0BC1                   // U
        C_M,                        // 0x0BC2                   // UU
        C_U,                        // 0x0BC3                   //
        C_U,                        // 0x0BC4                   //
        C_U,                        // 0x0BC5                   //
        C_M | C_PRE,                // 0x0BC6                   // E
        C_M | C_PRE,                // 0x0BC7                   // EE
        C_M | C_PRE,                // 0x0BC8                   // AI
        C_U,                        // 0x0BC9                   //
        C_M | C_WRAP,               // 0x0BCA                   // O
        C_M | C_WRAP,               // 0x0BCB                   // OO
        C_M | C_WRAP,               // 0x0BCC                   // AU
        C_H,                        // 0x0BCD                   // VIRAMA (HALANT)
        C_U,                        // 0x0BCE                   //
        C_U,                        // 0x0BCF                   //
        C_S,                        // 0x0BD0                   // OM
        C_U,                        // 0x0BD1                   //
        C_U,                        // 0x0BD2                   //
        C_U,                        // 0x0BD3                   //
        C_U,                        // 0x0BD4                   //
        C_U,                        // 0x0BD5                   //
        C_U,                        // 0x0BD6                   //
        C_M,                        // 0x0BD7                   // AU LENGTH MARK
        C_U,                        // 0x0BD8                   //
        C_U,                        // 0x0BD9                   //
        C_U,                        // 0x0BDA                   //
        C_U,                        // 0x0BDB                   //
        C_U,                        // 0x0BDC                   //
        C_U,                        // 0x0BDD                   //
        C_U,                        // 0x0BDE                   //
        C_U,                        // 0x0BDF                   //
        C_U,                        // 0x0BE0                   //
        C_U,                        // 0x0BE1                   //
        C_U,                        // 0x0BE2                   //
        C_U,                        // 0x0BE3                   //
        C_U,                        // 0x0BE4                   //
        C_U,                        // 0x0BE5                   //
        C_D,                        // 0x0BE6                   // ZERO
        C_D,                        // 0x0BE7                   // ONE
        C_D,                        // 0x0BE8                   // TWO
        C_D,                        // 0x0BE9                   // THREE
        C_D,                        // 0x0BEA                   // FOUR
        C_D,                        // 0x0BEB                   // FIVE
        C_D,                        // 0x0BEC                   // SIX
        C_D,                        // 0x0BED                   // SEVEN
        C_D,                        // 0x0BEE                   // EIGHT
        C_D,                        // 0x0BEF                   // NINE
        C_S,                        // 0x0BF0                   // TEN
        C_S,                        // 0x0BF1                   // ONE HUNDRED
        C_S,                        // 0x0BF2                   // ONE THOUSAND
        C_S,                        // 0x0BF3                   // DAY SIGN (naal)
        C_S,                        // 0x0BF4                   // MONTH SIGN (maatham)
        C_S,                        // 0x0BF5                   // YEAR SIGN (varudam)
        C_S,                        // 0x0BF6                   // DEBIT SIGN (patru)
        C_S,                        // 0x0BF7                   // CREDIT SIGN (varavu)
        C_S,                        // 0x0BF8                   // AS ABOVE SIGN (merpadi)
        C_S,                        // 0x0BF9                   // RUPEE SIGN (rupai)
        C_S,                        // 0x0BFA                   // NUMBER SIGN (enn)
        C_U,                        // 0x0BFB                   //
        C_U,                        // 0x0BFC                   //
        C_U,                        // 0x0BFD                   //
        C_U,                        // 0x0BFE                   //
        C_U                         // 0x0BFF                   //
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
