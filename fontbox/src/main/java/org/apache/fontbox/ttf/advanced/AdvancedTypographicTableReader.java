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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TTFDataStream;
import org.apache.fontbox.ttf.TTFTable;

/**
 * <p>OpenType Font (OTF) advanced typographic table reader.</p>
 *
 * @author Glenn Adams
 */
@SuppressWarnings("unchecked") 
public final class AdvancedTypographicTableReader {

    // logging state
    private static Log log = LogFactory.getLog(AdvancedTypographicTableReader.class);
    // instance state
    private OpenTypeFont otf;                                   // enclosing font instance
    private TTFTable table;                                     // table being constructed
    private TTFDataStream data;                                 // data stream
    // transient parsing state
    private transient Map/*<String,Object[3]>*/ seScripts;      // script-tag         => Object[3] : { default-language-tag, List(language-tag), seLanguages }
    private transient Map/*<String,Object[2]>*/ seLanguages;    // language-tag       => Object[2] : { "f<required-feature-index>", List("f<feature-index>")
    private transient Map/*<String,List<String>>*/ seFeatures;  // "f<feature-index>" => Object[2] : { feature-tag, List("lu<lookup-index>") }
    private transient GlyphMappingTable seMapping;              // subtable entry mappings
    private transient List seEntries;                           // subtable entry entries
    private transient List seSubtables;                         // subtable entry subtables

    /**
     * Construct an <code>AdvancedTypographicTableReader</code> instance.
     * @param otf enclosing font file (must be non-null)
     * @param table table instance being constructed, will be one of Glyph{Definition,Substitution,Positioning}Table
     * @param data font file reader (must be non-null)
     */
    public AdvancedTypographicTableReader(OpenTypeFont otf, TTFTable table, TTFDataStream data) {
        assert otf != null;
        assert table != null;
        assert data != null;
        this.otf = otf;
        this.table = table;
        this.data = data;
    }

    /**
     * Read advanced typographic table.
     * @throws AdvancedTypographicTableFormatException if ATT table has invalid format
     */
    public void read() throws AdvancedTypographicTableFormatException {
        try {
            if (table instanceof GlyphDefinitionTable)
                readGDEF();
            else if (table instanceof GlyphSubstitutionTable)
                readGSUB();
            else if (table instanceof GlyphPositioningTable)
                readGPOS();
        } catch (AdvancedTypographicTableFormatException e) {
            resetATStateAll();
            throw e;
        } catch (IOException e) {
            resetATStateAll();
            throw new AdvancedTypographicTableFormatException(e.getMessage(), e);
        } finally {
            resetATState();
        }
    }

    private void readLangSysTable(String tableTag, long langSysTable, String langSysTag)
            throws IOException {
        data.seek(langSysTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lang sys table: " + langSysTag);
        }
        // read lookup order (reorder) table offset
        int lo = data.readUnsignedShort();
        // read required feature index
        int rf = data.readUnsignedShort();
        String rfi;
        if (rf != 65535) {
            rfi = "f" + rf;
        } else {
            rfi = null;
        }
        // read (non-required) feature count
        int nf = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lang sys table reorder table: " + lo);
            log.debug(tableTag + " lang sys table required feature index: " + rf);
            log.debug(tableTag + " lang sys table non-required feature count: " + nf);
        }
        // read (non-required) feature indices
        int[] fia = new int[nf];
        List fl = new java.util.ArrayList();
        for (int i = 0; i < nf; i++) {
            int fi = data.readUnsignedShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lang sys table non-required feature index: " + fi);
            }
            fia[i] = fi;
            fl.add("f" + fi);
        }
        if (seLanguages == null) {
            seLanguages = new java.util.LinkedHashMap();
        }
        seLanguages.put(langSysTag, new Object[] { rfi, fl });
    }

    private static String defaultTag = "dflt";

    private void readScriptTable(String tableTag, long scriptTable, String scriptTag) throws IOException {
        data.seek(scriptTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script table: " + scriptTag);
        }
        // read default language system table offset
        int dl = data.readUnsignedShort();
        String dt = defaultTag;
        if (dl > 0) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " default lang sys tag: " + dt);
                log.debug(tableTag + " default lang sys table offset: " + dl);
            }
        }
        // read language system record count
        int nl = data.readUnsignedShort();
        List ll = new java.util.ArrayList();
        if (nl > 0) {
            String[] lta = new String[nl];
            int[] loa = new int[nl];
            // read language system records
            for (int i = 0, n = nl; i < n; i++) {
                String lt = data.readTag();
                int lo = data.readUnsignedShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lang sys tag: " + lt);
                    log.debug(tableTag + " lang sys table offset: " + lo);
                }
                lta[i] = lt;
                loa[i] = lo;
                if (dl == lo) {
                    dl = 0;
                    dt = lt;
                }
                ll.add(lt);
            }
            // read non-default language system tables
            for (int i = 0, n = nl; i < n; i++) {
                readLangSysTable(tableTag, scriptTable + loa [ i ], lta [ i ]);
            }
        }
        // read default language system table (if specified)
        if (dl > 0) {
            readLangSysTable(tableTag, scriptTable + dl, dt);
        } else if (dt != null) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lang sys default: " + dt);
            }
        }
        seScripts.put(scriptTag, new Object[] { dt, ll, seLanguages });
        seLanguages = null;
    }

    private void readScriptList(String tableTag, long scriptList) throws IOException {
        data.seek(scriptList);
        // read script record count
        int ns = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list record count: " + ns);
        }
        if (ns > 0) {
            String[] sta = new String[ns];
            int[] soa = new int[ns];
            // read script records
            for (int i = 0, n = ns; i < n; i++) {
                String st = data.readTag();
                int so = data.readUnsignedShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " script tag: " + st);
                    log.debug(tableTag + " script table offset: " + so);
                }
                sta[i] = st;
                soa[i] = so;
            }
            // read script tables
            for (int i = 0, n = ns; i < n; i++) {
                seLanguages = null;
                readScriptTable(tableTag, scriptList + soa [ i ], sta [ i ]);
            }
        }
    }

    private void readFeatureTable(String tableTag, long featureTable, String featureTag, int featureIndex) throws IOException {
        data.seek(featureTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature table: " + featureTag);
        }
        // read feature params offset
        int po = data.readUnsignedShort();
        // read lookup list indices count
        int nl = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature table parameters offset: " + po);
            log.debug(tableTag + " feature table lookup list index count: " + nl);
        }
        // read lookup table indices
        int[] lia = new int[nl];
        List lul = new java.util.ArrayList();
        for (int i = 0; i < nl; i++) {
            int li = data.readUnsignedShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " feature table lookup index: " + li);
            }
            lia[i] = li;
            lul.add("lu" + li);
        }
        seFeatures.put("f" + featureIndex, new Object[] { featureTag, lul });
    }

    private void readFeatureList(String tableTag, long featureList) throws IOException {
        data.seek(featureList);
        // read feature record count
        int nf = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature list record count: " + nf);
        }
        if (nf > 0) {
            String[] fta = new String[nf];
            int[] foa = new int[nf];
            // read feature records
            for (int i = 0, n = nf; i < n; i++) {
                String ft = data.readTag();
                int fo = data.readUnsignedShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " feature tag: " + ft);
                    log.debug(tableTag + " feature table offset: " + fo);
                }
                fta[i] = ft;
                foa[i] = fo;
            }
            // read feature tables
            for (int i = 0, n = nf; i < n; i++) {
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " feature index: " + i);
                }
                readFeatureTable(tableTag, featureList + foa [ i ], fta [ i ], i);
            }
        }
    }

    static final class GDEFLookupType {
        static final int GLYPH_CLASS                    = 1;
        static final int ATTACHMENT_POINT               = 2;
        static final int LIGATURE_CARET                 = 3;
        static final int MARK_ATTACHMENT                = 4;
        private GDEFLookupType() {
        }
        public static int getSubtableType(int lt) {
            int st;
            switch (lt) {
            case GDEFLookupType.GLYPH_CLASS:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_GLYPH_CLASS;
                break;
            case GDEFLookupType.ATTACHMENT_POINT:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_ATTACHMENT_POINT;
                break;
            case GDEFLookupType.LIGATURE_CARET:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_LIGATURE_CARET;
                break;
            case GDEFLookupType.MARK_ATTACHMENT:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_MARK_ATTACHMENT;
                break;
            default:
                st = -1;
                break;
            }
            return st;
        }
        public static String toString(int type) {
            String s;
            switch (type) {
            case GLYPH_CLASS:
                s = "GlyphClass";
                break;
            case ATTACHMENT_POINT:
                s = "AttachmentPoint";
                break;
            case LIGATURE_CARET:
                s = "LigatureCaret";
                break;
            case MARK_ATTACHMENT:
                s = "MarkAttachment";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class GSUBLookupType {
        static final int SINGLE                         = 1;
        static final int MULTIPLE                       = 2;
        static final int ALTERNATE                      = 3;
        static final int LIGATURE                       = 4;
        static final int CONTEXTUAL                     = 5;
        static final int CHAINED_CONTEXTUAL             = 6;
        static final int EXTENSION                      = 7;
        static final int REVERSE_CHAINED_SINGLE         = 8;
        private GSUBLookupType() {
        }
        public static int getSubtableType(int lt) {
            int st;
            switch (lt) {
            case GSUBLookupType.SINGLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_SINGLE;
                break;
            case GSUBLookupType.MULTIPLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_MULTIPLE;
                break;
            case GSUBLookupType.ALTERNATE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_ALTERNATE;
                break;
            case GSUBLookupType.LIGATURE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_LIGATURE;
                break;
            case GSUBLookupType.CONTEXTUAL:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CONTEXTUAL;
                break;
            case GSUBLookupType.CHAINED_CONTEXTUAL:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
                break;
            case GSUBLookupType.EXTENSION:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION;
                break;
            case GSUBLookupType.REVERSE_CHAINED_SINGLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE;
                break;
            default:
                st = -1;
                break;
            }
            return st;
        }
        public static String toString(int type) {
            String s;
            switch (type) {
            case SINGLE:
                s = "Single";
                break;
            case MULTIPLE:
                s = "Multiple";
                break;
            case ALTERNATE:
                s = "Alternate";
                break;
            case LIGATURE:
                s = "Ligature";
                break;
            case CONTEXTUAL:
                s = "Contextual";
                break;
            case CHAINED_CONTEXTUAL:
                s = "ChainedContextual";
                break;
            case EXTENSION:
                s = "Extension";
                break;
            case REVERSE_CHAINED_SINGLE:
                s = "ReverseChainedSingle";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class GPOSLookupType {
        static final int SINGLE                         = 1;
        static final int PAIR                           = 2;
        static final int CURSIVE                        = 3;
        static final int MARK_TO_BASE                   = 4;
        static final int MARK_TO_LIGATURE               = 5;
        static final int MARK_TO_MARK                   = 6;
        static final int CONTEXTUAL                     = 7;
        static final int CHAINED_CONTEXTUAL             = 8;
        static final int EXTENSION                      = 9;
        private GPOSLookupType() {
        }
        public static String toString(int type) {
            String s;
            switch (type) {
            case SINGLE:
                s = "Single";
                break;
            case PAIR:
                s = "Pair";
                break;
            case CURSIVE:
                s = "Cursive";
                break;
            case MARK_TO_BASE:
                s = "MarkToBase";
                break;
            case MARK_TO_LIGATURE:
                s = "MarkToLigature";
                break;
            case MARK_TO_MARK:
                s = "MarkToMark";
                break;
            case CONTEXTUAL:
                s = "Contextual";
                break;
            case CHAINED_CONTEXTUAL:
                s = "ChainedContextual";
                break;
            case EXTENSION:
                s = "Extension";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class LookupFlag {
        static final int RIGHT_TO_LEFT                  = 0x0001;
        static final int IGNORE_BASE_GLYPHS             = 0x0002;
        static final int IGNORE_LIGATURE                = 0x0004;
        static final int IGNORE_MARKS                   = 0x0008;
        static final int USE_MARK_FILTERING_SET         = 0x0010;
        static final int MARK_ATTACHMENT_TYPE           = 0xFF00;
        private LookupFlag() {
        }
        public static String toString(int flags) {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            if ((flags & RIGHT_TO_LEFT) != 0) {
                if (first) {
                    first = false;
                } else {
                    sb.append('|');
                }
                sb.append("RightToLeft");
            }
            if ((flags & IGNORE_BASE_GLYPHS) != 0) {
                if (first) {
                    first = false;
                } else {
                    sb.append('|');
                }
                sb.append("IgnoreBaseGlyphs");
            }
            if ((flags & IGNORE_LIGATURE) != 0) {
                if (first) {
                    first = false;
                } else {
                    sb.append('|');
                }
                sb.append("IgnoreLigature");
            }
            if ((flags & IGNORE_MARKS) != 0) {
                if (first) {
                    first = false;
                } else {
                    sb.append('|');
                }
                sb.append("IgnoreMarks");
            }
            if ((flags & USE_MARK_FILTERING_SET) != 0) {
                if (first) {
                    first = false;
                } else {
                    sb.append('|');
                }
                sb.append("UseMarkFilteringSet");
            }
            if (sb.length() == 0) {
                sb.append('-');
            }
            return sb.toString();
        }
    }

    private GlyphCoverageTable readCoverageTableFormat1(String label, long tableOffset, int coverageFormat) throws IOException {
        List entries = new java.util.ArrayList();
        data.seek(tableOffset);
        // skip over format (already known)
        data.skip(2);
        // read glyph count
        int ng = data.readUnsignedShort();
        int[] ga = new int[ng];
        for (int i = 0, n = ng; i < n; i++) {
            int g = data.readUnsignedShort();
            ga[i] = g;
            entries.add(Integer.valueOf(g));
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(label + " glyphs: " + toString(ga));
        }
        return GlyphCoverageTable.createCoverageTable(entries);
    }

    private GlyphCoverageTable readCoverageTableFormat2(String label, long tableOffset, int coverageFormat) throws IOException {
        List entries = new java.util.ArrayList();
        data.seek(tableOffset);
        // skip over format (already known)
        data.skip(2);
        // read range record count
        int nr = data.readUnsignedShort();
        for (int i = 0, n = nr; i < n; i++) {
            // read range start
            int s = data.readUnsignedShort();
            // read range end
            int e = data.readUnsignedShort();
            // read range coverage (mapping) index
            int m = data.readUnsignedShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m);
            }
            entries.add(new GlyphCoverageTable.MappingRange(s, e, m));
        }
        return GlyphCoverageTable.createCoverageTable(entries);
    }

    private GlyphCoverageTable readCoverageTable(String label, long tableOffset) throws IOException {
        GlyphCoverageTable gct;
        long cp = data.getCurrentPosition();
        data.seek(tableOffset);
        // read coverage table format
        int cf = data.readUnsignedShort();
        if (cf == 1) {
            gct = readCoverageTableFormat1(label, tableOffset, cf);
        } else if (cf == 2) {
            gct = readCoverageTableFormat2(label, tableOffset, cf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported coverage table format: " + cf);
        }
        data.seek(cp);
        return gct;
    }

    private GlyphClassTable readClassDefTableFormat1(String label, long tableOffset, int classFormat) throws IOException {
        List entries = new java.util.ArrayList();
        data.seek(tableOffset);
        // skip over format (already known)
        data.skip(2);
        // read start glyph
        int sg = data.readUnsignedShort();
        entries.add(Integer.valueOf(sg));
        // read glyph count
        int ng = data.readUnsignedShort();
        // read glyph classes
        int[] ca = new int[ng];
        for (int i = 0, n = ng; i < n; i++) {
            int gc = data.readUnsignedShort();
            ca[i] = gc;
            entries.add(Integer.valueOf(gc));
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(label + " glyph classes: " + toString(ca));
        }
        return GlyphClassTable.createClassTable(entries);
    }

    private GlyphClassTable readClassDefTableFormat2(String label, long tableOffset, int classFormat) throws IOException {
        List entries = new java.util.ArrayList();
        data.seek(tableOffset);
        // skip over format (already known)
        data.skip(2);
        // read range record count
        int nr = data.readUnsignedShort();
        for (int i = 0, n = nr; i < n; i++) {
            // read range start
            int s = data.readUnsignedShort();
            // read range end
            int e = data.readUnsignedShort();
            // read range glyph class (mapping) index
            int m = data.readUnsignedShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m);
            }
            entries.add(new GlyphClassTable.MappingRange(s, e, m));
        }
        return GlyphClassTable.createClassTable(entries);
    }

    private GlyphClassTable readClassDefTable(String label, long tableOffset) throws IOException {
        GlyphClassTable gct;
        long cp = data.getCurrentPosition();
        data.seek(tableOffset);
        // read class table format
        int cf = data.readUnsignedShort();
        if (cf == 1) {
            gct = readClassDefTableFormat1(label, tableOffset, cf);
        } else if (cf == 2) {
            gct = readClassDefTableFormat2(label, tableOffset, cf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported class definition table format: " + cf);
        }
        data.seek(cp);
        return gct;
    }

    private void readSingleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read delta glyph
        int dg = data.readSignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (delta)");
            log.debug(tableTag + " single substitution coverage table offset: " + co);
            log.debug(tableTag + " single substitution delta: " + dg);
        }
        // read coverage table
        seMapping = readCoverageTable(tableTag + " single substitution coverage", subtableOffset + co);
        seEntries.add(Integer.valueOf(dg));
    }

    private void readSingleSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read glyph count
        int ng = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (mapped)");
            log.debug(tableTag + " single substitution coverage table offset: " + co);
            log.debug(tableTag + " single substitution glyph count: " + ng);
        }
        // read coverage table
        seMapping = readCoverageTable(tableTag + " single substitution coverage", subtableOffset + co);
        // read glyph substitutions
        int[] gsa = new int[ng];
        for (int i = 0, n = ng; i < n; i++) {
            int gs = data.readUnsignedShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " single substitution glyph[" + i + "]: " + gs);
            }
            gsa[i] = gs;
            seEntries.add(Integer.valueOf(gs));
        }
    }

    private int readSingleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readSingleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readSingleSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported single substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readMultipleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read sequence count
        int ns = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " multiple substitution subtable format: " + subtableFormat + " (mapped)");
            log.debug(tableTag + " multiple substitution coverage table offset: " + co);
            log.debug(tableTag + " multiple substitution sequence count: " + ns);
        }
        // read coverage table
        seMapping = readCoverageTable(tableTag + " multiple substitution coverage", subtableOffset + co);
        // read sequence table offsets
        int[] soa = new int[ns];
        for (int i = 0, n = ns; i < n; i++) {
            soa[i] = data.readUnsignedShort();
        }
        // read sequence tables
        int[][] gsa = new int [ ns ] [];
        for (int i = 0, n = ns; i < n; i++) {
            int so = soa[i];
            int[] ga;
            if (so > 0) {
                data.seek(subtableOffset + so);
                // read glyph count
                int ng = data.readUnsignedShort();
                ga = new int[ng];
                for (int j = 0; j < ng; j++) {
                    ga[j] = data.readUnsignedShort();
                }
            } else {
                ga = null;
            }
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " multiple substitution sequence[" + i + "]: " + toString(ga));
            }
            gsa [ i ] = ga;
        }
        seEntries.add(gsa);
    }

    private int readMultipleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readMultipleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported multiple substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readAlternateSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read alternate set count
        int ns = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " alternate substitution subtable format: " + subtableFormat + " (mapped)");
            log.debug(tableTag + " alternate substitution coverage table offset: " + co);
            log.debug(tableTag + " alternate substitution alternate set count: " + ns);
        }
        // read coverage table
        seMapping = readCoverageTable(tableTag + " alternate substitution coverage", subtableOffset + co);
        // read alternate set table offsets
        int[] soa = new int[ns];
        for (int i = 0, n = ns; i < n; i++) {
            soa[i] = data.readUnsignedShort();
        }
        // read alternate set tables
        for (int i = 0, n = ns; i < n; i++) {
            int so = soa[i];
            data.seek(subtableOffset + so);
            // read glyph count
            int ng = data.readUnsignedShort();
            int[] ga = new int[ng];
            for (int j = 0; j < ng; j++) {
                int gs = data.readUnsignedShort();
                ga[j] = gs;
            }
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " alternate substitution alternate set[" + i + "]: " + toString(ga));
            }
            seEntries.add(ga);
        }
    }

    private int readAlternateSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readAlternateSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported alternate substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readLigatureSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read ligature set count
        int ns = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " ligature substitution subtable format: " + subtableFormat + " (mapped)");
            log.debug(tableTag + " ligature substitution coverage table offset: " + co);
            log.debug(tableTag + " ligature substitution ligature set count: " + ns);
        }
        // read coverage table
        seMapping = readCoverageTable(tableTag + " ligature substitution coverage", subtableOffset + co);
        // read ligature set table offsets
        int[] soa = new int[ns];
        for (int i = 0, n = ns; i < n; i++) {
            soa[i] = data.readUnsignedShort();
        }
        // read ligature set tables
        for (int i = 0, n = ns; i < n; i++) {
            int so = soa[i];
            data.seek(subtableOffset + so);
            // read ligature table count
            int nl = data.readUnsignedShort();
            int[] loa = new int[nl];
            for (int j = 0; j < nl; j++) {
                loa[j] = data.readUnsignedShort();
            }
            List ligs = new java.util.ArrayList();
            for (int j = 0; j < nl; j++) {
                int lo = loa[j];
                data.seek(subtableOffset + so + lo);
                // read ligature glyph id
                int lg = data.readUnsignedShort();
                // read ligature (input) component count
                int nc = data.readUnsignedShort();
                int[] ca = new int [ nc - 1 ];
                // read ligature (input) component glyph ids
                for (int k = 0; k < nc - 1; k++) {
                    ca[k] = data.readUnsignedShort();
                }
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " ligature substitution ligature set[" + i + "]: ligature(" + lg + "), components: " + toString(ca));
                }
                ligs.add(new GlyphSubstitutionTable.Ligature(lg, ca));
            }
            seEntries.add(new GlyphSubstitutionTable.LigatureSet(ligs));
        }
    }

    private int readLigatureSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readLigatureSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported ligature substitution subtable format: " + sf);
        }
        return sf;
    }

    private AdvancedTypographicTable.RuleLookup[] readRuleLookups(int numLookups, String header) throws IOException {
        AdvancedTypographicTable.RuleLookup[] la = new AdvancedTypographicTable.RuleLookup [ numLookups ];
        for (int i = 0, n = numLookups; i < n; i++) {
            int sequenceIndex = data.readUnsignedShort();
            int lookupIndex = data.readUnsignedShort();
            la [ i ] = new AdvancedTypographicTable.RuleLookup(sequenceIndex, lookupIndex);
            // dump info if debugging and header is non-null
            if (log.isDebugEnabled() && (header != null)) {
                log.debug(header + "lookup[" + i + "]: " + la[i]);
            }
        }
        return la;
    }

    private void readContextualSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read rule set count
        int nrs = data.readUnsignedShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for (int i = 0; i < nrs; i++) {
            rsoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyphs)");
            log.debug(tableTag + " contextual substitution coverage table offset: " + co);
            log.debug(tableTag + " contextual substitution rule set count: " + nrs);
            for (int i = 0; i < nrs; i++) {
                log.debug(tableTag + " contextual substitution rule set offset[" + i + "]: " + rsoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " contextual substitution coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ nrs ];
        String header = null;
        for (int i = 0; i < nrs; i++) {
            AdvancedTypographicTable.RuleSet rs;
            int rso = rsoa [ i ];
            if (rso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + rso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    AdvancedTypographicTable.GlyphSequenceRule r;
                    int ro = roa [ j ];
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + rso + ro);
                        // read glyph count
                        int ng = data.readUnsignedShort();
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read glyphs
                        int[] glyphs = new int [ ng - 1 ];
                        for (int k = 0, nk = glyphs.length; k < nk; k++) {
                            glyphs [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.GlyphSequenceRule(lookups, ng, glyphs);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(rsa);
    }

    private void readContextualSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read class def table offset
        int cdo = data.readUnsignedShort();
        // read class rule set count
        int ngc = data.readUnsignedShort();
        // read class rule set offsets
        int[] csoa = new int [ ngc ];
        for (int i = 0; i < ngc; i++) {
            csoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph classes)");
            log.debug(tableTag + " contextual substitution coverage table offset: " + co);
            log.debug(tableTag + " contextual substitution class set count: " + ngc);
            for (int i = 0; i < ngc; i++) {
                log.debug(tableTag + " contextual substitution class set offset[" + i + "]: " + csoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " contextual substitution coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read class definition table
        GlyphClassTable cdt;
        if (cdo > 0) {
            cdt = readClassDefTable(tableTag + " contextual substitution class definition", subtableOffset + cdo);
        } else {
            cdt = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ ngc ];
        String header = null;
        for (int i = 0; i < ngc; i++) {
            int cso = csoa [ i ];
            AdvancedTypographicTable.RuleSet rs;
            if (cso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + cso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    int ro = roa [ j ];
                    AdvancedTypographicTable.ClassSequenceRule r;
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + cso + ro);
                        // read glyph count
                        int ng = data.readUnsignedShort();
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read classes
                        int[] classes = new int [ ng - 1 ];
                        for (int k = 0, nk = classes.length; k < nk; k++) {
                            classes [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ClassSequenceRule(lookups, ng, classes);
                    } else {
                        assert ro > 0 : "unexpected null subclass rule offset";
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(cdt);
        seEntries.add(Integer.valueOf(ngc));
        seEntries.add(rsa);
    }

    private void readContextualSubTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read glyph (input sequence length) count
        int ng = data.readUnsignedShort();
        // read substitution lookup count
        int nl = data.readUnsignedShort();
        // read glyph coverage offsets, one per glyph input sequence length count
        int[] gcoa = new int [ ng ];
        for (int i = 0; i < ng; i++) {
            gcoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph sets)");
            log.debug(tableTag + " contextual substitution glyph input sequence length count: " + ng);
            log.debug(tableTag + " contextual substitution lookup count: " + nl);
            for (int i = 0; i < ng; i++) {
                log.debug(tableTag + " contextual substitution coverage table offset[" + i + "]: " + gcoa[i]);
            }
        }
        // read coverage tables
        GlyphCoverageTable[] gca = new GlyphCoverageTable [ ng ];
        for (int i = 0; i < ng; i++) {
            int gco = gcoa [ i ];
            GlyphCoverageTable gct;
            if (gco > 0) {
                gct = readCoverageTable(tableTag + " contextual substitution coverage[" + i + "]", subtableOffset + gco);
            } else {
                gct = null;
            }
            gca [ i ] = gct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " contextual substitution lookups: ";
        }
        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
        // construct rule, rule set, and rule set array
        AdvancedTypographicTable.Rule r = new AdvancedTypographicTable.CoverageSequenceRule(lookups, ng, gca);
        AdvancedTypographicTable.RuleSet rs = new AdvancedTypographicTable.HomogeneousRuleSet(new AdvancedTypographicTable.Rule[] {r});
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet[] {rs};
        // store results
        assert (gca != null) && (gca.length > 0);
        seMapping = gca[0];
        seEntries.add(rsa);
    }

    private int readContextualSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readContextualSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readContextualSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 3) {
            readContextualSubTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported contextual substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readChainedContextualSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read rule set count
        int nrs = data.readUnsignedShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for (int i = 0; i < nrs; i++) {
            rsoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyphs)");
            log.debug(tableTag + " chained contextual substitution coverage table offset: " + co);
            log.debug(tableTag + " chained contextual substitution rule set count: " + nrs);
            for (int i = 0; i < nrs; i++) {
                log.debug(tableTag + " chained contextual substitution rule set offset[" + i + "]: " + rsoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " chained contextual substitution coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ nrs ];
        String header = null;
        for (int i = 0; i < nrs; i++) {
            AdvancedTypographicTable.RuleSet rs;
            int rso = rsoa [ i ];
            if (rso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + rso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    AdvancedTypographicTable.ChainedGlyphSequenceRule r;
                    int ro = roa [ j ];
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + rso + ro);
                        // read backtrack glyph count
                        int nbg = data.readUnsignedShort();
                        // read backtrack glyphs
                        int[] backtrackGlyphs = new int [ nbg ];
                        for (int k = 0, nk = backtrackGlyphs.length; k < nk; k++) {
                            backtrackGlyphs [ k ] = data.readUnsignedShort();
                        }
                        // read input glyph count
                        int nig = data.readUnsignedShort();
                        // read glyphs
                        int[] glyphs = new int [ nig - 1 ];
                        for (int k = 0, nk = glyphs.length; k < nk; k++) {
                            glyphs [ k ] = data.readUnsignedShort();
                        }
                        // read lookahead glyph count
                        int nlg = data.readUnsignedShort();
                        // read lookahead glyphs
                        int[] lookaheadGlyphs = new int [ nlg ];
                        for (int k = 0, nk = lookaheadGlyphs.length; k < nk; k++) {
                            lookaheadGlyphs [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ChainedGlyphSequenceRule(lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(rsa);
    }

    private void readChainedContextualSubTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read backtrack class def table offset
        int bcdo = data.readUnsignedShort();
        // read input class def table offset
        int icdo = data.readUnsignedShort();
        // read lookahead class def table offset
        int lcdo = data.readUnsignedShort();
        // read class set count
        int ngc = data.readUnsignedShort();
        // read class set offsets
        int[] csoa = new int [ ngc ];
        for (int i = 0; i < ngc; i++) {
            csoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph classes)");
            log.debug(tableTag + " chained contextual substitution coverage table offset: " + co);
            log.debug(tableTag + " chained contextual substitution class set count: " + ngc);
            for (int i = 0; i < ngc; i++) {
                log.debug(tableTag + " chained contextual substitution class set offset[" + i + "]: " + csoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " chained contextual substitution coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read backtrack class definition table
        GlyphClassTable bcdt;
        if (bcdo > 0) {
            bcdt = readClassDefTable(tableTag + " contextual substitution backtrack class definition", subtableOffset + bcdo);
        } else {
            bcdt = null;
        }
        // read input class definition table
        GlyphClassTable icdt;
        if (icdo > 0) {
            icdt = readClassDefTable(tableTag + " contextual substitution input class definition", subtableOffset + icdo);
        } else {
            icdt = null;
        }
        // read lookahead class definition table
        GlyphClassTable lcdt;
        if (lcdo > 0) {
            lcdt = readClassDefTable(tableTag + " contextual substitution lookahead class definition", subtableOffset + lcdo);
        } else {
            lcdt = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ ngc ];
        String header = null;
        for (int i = 0; i < ngc; i++) {
            int cso = csoa [ i ];
            AdvancedTypographicTable.RuleSet rs;
            if (cso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + cso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    int ro = roa [ j ];
                    AdvancedTypographicTable.ChainedClassSequenceRule r;
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + cso + ro);
                        // read backtrack glyph class count
                        int nbc = data.readUnsignedShort();
                        // read backtrack glyph classes
                        int[] backtrackClasses = new int [ nbc ];
                        for (int k = 0, nk = backtrackClasses.length; k < nk; k++) {
                            backtrackClasses [ k ] = data.readUnsignedShort();
                        }
                        // read input glyph class count
                        int nic = data.readUnsignedShort();
                        // read input glyph classes
                        int[] classes = new int [ nic - 1 ];
                        for (int k = 0, nk = classes.length; k < nk; k++) {
                            classes [ k ] = data.readUnsignedShort();
                        }
                        // read lookahead glyph class count
                        int nlc = data.readUnsignedShort();
                        // read lookahead glyph classes
                        int[] lookaheadClasses = new int [ nlc ];
                        for (int k = 0, nk = lookaheadClasses.length; k < nk; k++) {
                            lookaheadClasses [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ChainedClassSequenceRule(lookups, nic, classes, backtrackClasses, lookaheadClasses);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(icdt);
        seEntries.add(bcdt);
        seEntries.add(lcdt);
        seEntries.add(Integer.valueOf(ngc));
        seEntries.add(rsa);
    }

    private void readChainedContextualSubTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read backtrack glyph count
        int nbg = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for (int i = 0; i < nbg; i++) {
            bgcoa [ i ] = data.readUnsignedShort();
        }
        // read input glyph count
        int nig = data.readUnsignedShort();
        // read input glyph coverage offsets
        int[] igcoa = new int [ nig ];
        for (int i = 0; i < nig; i++) {
            igcoa [ i ] = data.readUnsignedShort();
        }
        // read lookahead glyph count
        int nlg = data.readUnsignedShort();
        // read lookahead glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for (int i = 0; i < nlg; i++) {
            lgcoa [ i ] = data.readUnsignedShort();
        }
        // read substitution lookup count
        int nl = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph sets)");
            log.debug(tableTag + " chained contextual substitution backtrack glyph count: " + nbg);
            for (int i = 0; i < nbg; i++) {
                log.debug(tableTag + " chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
            }
            log.debug(tableTag + " chained contextual substitution input glyph count: " + nig);
            for (int i = 0; i < nig; i++) {
                log.debug(tableTag + " chained contextual substitution input coverage table offset[" + i + "]: " + igcoa[i]);
            }
            log.debug(tableTag + " chained contextual substitution lookahead glyph count: " + nlg);
            for (int i = 0; i < nlg; i++) {
                log.debug(tableTag + " chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
            }
            log.debug(tableTag + " chained contextual substitution lookup count: " + nl);
        }
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for (int i = 0; i < nbg; i++) {
            int bgco = bgcoa [ i ];
            GlyphCoverageTable bgct;
            if (bgco > 0) {
                bgct = readCoverageTable(tableTag + " chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + bgco);
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read input coverage tables
        GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];
        for (int i = 0; i < nig; i++) {
            int igco = igcoa [ i ];
            GlyphCoverageTable igct;
            if (igco > 0) {
                igct = readCoverageTable(tableTag + " chained contextual substitution input coverage[" + i + "]", subtableOffset + igco);
            } else {
                igct = null;
            }
            igca[i] = igct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for (int i = 0; i < nlg; i++) {
            int lgco = lgcoa [ i ];
            GlyphCoverageTable lgct;
            if (lgco > 0) {
                lgct = readCoverageTable(tableTag + " chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + lgco);
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " chained contextual substitution lookups: ";
        }
        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
        // construct rule, rule set, and rule set array
        AdvancedTypographicTable.Rule r = new AdvancedTypographicTable.ChainedCoverageSequenceRule(lookups, nig, igca, bgca, lgca);
        AdvancedTypographicTable.RuleSet rs = new AdvancedTypographicTable.HomogeneousRuleSet(new AdvancedTypographicTable.Rule[] {r});
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet[] {rs};
        // store results
        assert (igca != null) && (igca.length > 0);
        seMapping = igca[0];
        seEntries.add(rsa);
    }

    private int readChainedContextualSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readChainedContextualSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readChainedContextualSubTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 3) {
            readChainedContextualSubTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported chained contextual substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readExtensionSubTableFormat1(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read extension lookup type
        int lt = data.readUnsignedShort();
        // read extension offset
        long eo = data.readUnsignedInt();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " extension substitution subtable format: " + subtableFormat);
            log.debug(tableTag + " extension substitution lookup type: " + lt);
            log.debug(tableTag + " extension substitution lookup table offset: " + eo);
        }
        // read referenced subtable from extended offset
        readGSUBSubtable(lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo);
    }

    private int readExtensionSubTable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readExtensionSubTableFormat1(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported extension substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readReverseChainedSingleSubTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read backtrack glyph count
        int nbg = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for (int i = 0; i < nbg; i++) {
            bgcoa [ i ] = data.readUnsignedShort();
        }
        // read lookahead glyph count
        int nlg = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for (int i = 0; i < nlg; i++) {
            lgcoa [ i ] = data.readUnsignedShort();
        }
        // read substitution (output) glyph count
        int ng = data.readUnsignedShort();
        // read substitution (output) glyphs
        int[] glyphs = new int [ ng ];
        for (int i = 0, n = ng; i < n; i++) {
            glyphs [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " reverse chained contextual substitution format: " + subtableFormat);
            log.debug(tableTag + " reverse chained contextual substitution coverage table offset: " + co);
            log.debug(tableTag + " reverse chained contextual substitution backtrack glyph count: " + nbg);
            for (int i = 0; i < nbg; i++) {
                log.debug(tableTag + " reverse chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
            }
            log.debug(tableTag + " reverse chained contextual substitution lookahead glyph count: " + nlg);
            for (int i = 0; i < nlg; i++) {
                log.debug(tableTag + " reverse chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
            }
            log.debug(tableTag + " reverse chained contextual substitution glyphs: " + toString(glyphs));
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " reverse chained contextual substitution coverage", subtableOffset + co);
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for (int i = 0; i < nbg; i++) {
            int bgco = bgcoa[i];
            GlyphCoverageTable bgct;
            if (bgco > 0) {
                bgct = readCoverageTable(tableTag + " reverse chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + bgco);
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for (int i = 0; i < nlg; i++) {
            int lgco = lgcoa[i];
            GlyphCoverageTable lgct;
            if (lgco > 0) {
                lgct = readCoverageTable(tableTag + " reverse chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + lgco);
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // store results
        seMapping = ct;
        seEntries.add(bgca);
        seEntries.add(lgca);
        seEntries.add(glyphs);
    }

    private int readReverseChainedSingleSubTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read substitution subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readReverseChainedSingleSubTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported reverse chained single substitution subtable format: " + sf);
        }
        return sf;
    }

    private void readGSUBSubtable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        initATSubState();
        int subtableFormat = -1;
        switch (lookupType) {
        case GSUBLookupType.SINGLE:
            subtableFormat = readSingleSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.MULTIPLE:
            subtableFormat = readMultipleSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.ALTERNATE:
            subtableFormat = readAlternateSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.LIGATURE:
            subtableFormat = readLigatureSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.CONTEXTUAL:
            subtableFormat = readContextualSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.CHAINED_CONTEXTUAL:
            subtableFormat = readChainedContextualSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.REVERSE_CHAINED_SINGLE:
            subtableFormat = readReverseChainedSingleSubTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GSUBLookupType.EXTENSION:
            subtableFormat = readExtensionSubTable(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset);
            break;
        default:
            break;
        }
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_SUBSTITUTION, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat);
        resetATSubState();
    }

    private GlyphPositioningTable.DeviceTable readPosDeviceTable(long subtableOffset, long deviceTableOffset) throws IOException {
        long cp = data.getCurrentPosition();
        data.seek(subtableOffset + deviceTableOffset);
        // read start size
        int ss = data.readUnsignedShort();
        // read end size
        int es = data.readUnsignedShort();
        // read delta format
        int df = data.readUnsignedShort();
        int s1;
        int m1;
        int dm;
        int dd;
        int s2;
        if (df == 1) {
            s1 = 14;
            m1 = 0x3;
            dm = 1;
            dd = 4;
            s2 = 2;
        } else if (df == 2) {
            s1 = 12;
            m1 = 0xF;
            dm = 7;
            dd = 16;
            s2 = 4;
        } else if (df == 3) {
            s1 = 8;
            m1 = 0xFF;
            dm = 127;
            dd = 256;
            s2 = 8;
        } else {
            log.debug("unsupported device table delta format: " + df + ", ignoring device table");
            return null;
        }
        // read deltas
        int n = (es - ss) + 1;
        if (n < 0) {
            log.debug("invalid device table delta count: " + n + ", ignoring device table");
            return null;
        }
        int[] da = new int [ n ];
        for (int i = 0; (i < n) && (s2 > 0);) {
            int p = data.readUnsignedShort();
            for (int j = 0, k = 16 / s2; j < k; j++) {
                int d = (p >> s1) & m1;
                if (d > dm) {
                    d -= dd;
                }
                if (i < n) {
                    da [ i++ ] = d;
                } else {
                    break;
                }
                p <<= s2;
            }
        }
        data.seek(cp);
        return new GlyphPositioningTable.DeviceTable(ss, es, da);
    }

    private GlyphPositioningTable.Value readPosValue(long subtableOffset, int valueFormat) throws IOException {
        // XPlacement
        int xp;
        if ((valueFormat & GlyphPositioningTable.Value.X_PLACEMENT) != 0) {
            xp = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
        } else {
            xp = 0;
        }
        // YPlacement
        int yp;
        if ((valueFormat & GlyphPositioningTable.Value.Y_PLACEMENT) != 0) {
            yp = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
        } else {
            yp = 0;
        }
        // XAdvance
        int xa;
        if ((valueFormat & GlyphPositioningTable.Value.X_ADVANCE) != 0) {
            xa = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
        } else {
            xa = 0;
        }
        // YAdvance
        int ya;
        if ((valueFormat & GlyphPositioningTable.Value.Y_ADVANCE) != 0) {
            ya = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
        } else {
            ya = 0;
        }
        // XPlaDevice
        GlyphPositioningTable.DeviceTable xpd;
        if ((valueFormat & GlyphPositioningTable.Value.X_PLACEMENT_DEVICE) != 0) {
            int xpdo = data.readUnsignedShort();
            xpd = readPosDeviceTable(subtableOffset, xpdo);
        } else {
            xpd = null;
        }
        // YPlaDevice
        GlyphPositioningTable.DeviceTable ypd;
        if ((valueFormat & GlyphPositioningTable.Value.Y_PLACEMENT_DEVICE) != 0) {
            int ypdo = data.readUnsignedShort();
            ypd = readPosDeviceTable(subtableOffset, ypdo);
        } else {
            ypd = null;
        }
        // XAdvDevice
        GlyphPositioningTable.DeviceTable xad;
        if ((valueFormat & GlyphPositioningTable.Value.X_ADVANCE_DEVICE) != 0) {
            int xado = data.readUnsignedShort();
            xad = readPosDeviceTable(subtableOffset, xado);
        } else {
            xad = null;
        }
        // YAdvDevice
        GlyphPositioningTable.DeviceTable yad;
        if ((valueFormat & GlyphPositioningTable.Value.Y_ADVANCE_DEVICE) != 0) {
            int yado = data.readUnsignedShort();
            yad = readPosDeviceTable(subtableOffset, yado);
        } else {
            yad = null;
        }
        return new GlyphPositioningTable.Value(xp, yp, xa, ya, xpd, ypd, xad, yad);
    }

    private void readSinglePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read value format
        int vf = data.readUnsignedShort();
        // read value
        GlyphPositioningTable.Value v = readPosValue(subtableOffset, vf);
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (delta)");
            log.debug(tableTag + " single positioning coverage table offset: " + co);
            log.debug(tableTag + " single positioning value: " + v);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " single positioning coverage", subtableOffset + co);
        // store results
        seMapping = ct;
        seEntries.add(v);
    }

    private void readSinglePosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read value format
        int vf = data.readUnsignedShort();
        // read value count
        int nv = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (mapped)");
            log.debug(tableTag + " single positioning coverage table offset: " + co);
            log.debug(tableTag + " single positioning value count: " + nv);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " single positioning coverage", subtableOffset + co);
        // read positioning values
        GlyphPositioningTable.Value[] pva = new GlyphPositioningTable.Value[nv];
        for (int i = 0, n = nv; i < n; i++) {
            GlyphPositioningTable.Value pv = readPosValue(subtableOffset, vf);
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " single positioning value[" + i + "]: " + pv);
            }
            pva[i] = pv;
        }
        // store results
        seMapping = ct;
        seEntries.add(pva);
    }

    private int readSinglePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positionining subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readSinglePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readSinglePosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported single positioning subtable format: " + sf);
        }
        return sf;
    }

    private GlyphPositioningTable.PairValues readPosPairValues(long subtableOffset, boolean hasGlyph, int vf1, int vf2) throws IOException {
        // read glyph (if present)
        int glyph;
        if (hasGlyph) {
            glyph = data.readUnsignedShort();
        } else {
            glyph = 0;
        }
        // read first value (if present)
        GlyphPositioningTable.Value v1;
        if (vf1 != 0) {
            v1 = readPosValue(subtableOffset, vf1);
        } else {
            v1 = null;
        }
        // read second value (if present)
        GlyphPositioningTable.Value v2;
        if (vf2 != 0) {
            v2 = readPosValue(subtableOffset, vf2);
        } else {
            v2 = null;
        }
        return new GlyphPositioningTable.PairValues(glyph, v1, v2);
    }

    private GlyphPositioningTable.PairValues[] readPosPairSetTable(long subtableOffset, int pairSetTableOffset, int vf1, int vf2) throws IOException {
        String tableTag = "GPOS";
        long cp = data.getCurrentPosition();
        data.seek(subtableOffset + pairSetTableOffset);
        // read pair values count
        int npv = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair set table offset: " + pairSetTableOffset);
            log.debug(tableTag + " pair set table values count: " + npv);
        }
        // read pair values
        GlyphPositioningTable.PairValues[] pva = new GlyphPositioningTable.PairValues [ npv ];
        for (int i = 0, n = npv; i < n; i++) {
            GlyphPositioningTable.PairValues pv = readPosPairValues(subtableOffset, true, vf1, vf2);
            pva [ i ] = pv;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " pair set table value[" + i + "]: " + pv);
            }
        }
        data.seek(cp);
        return pva;
    }

    private void readPairPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read value format for first glyph
        int vf1 = data.readUnsignedShort();
        // read value format for second glyph
        int vf2 = data.readUnsignedShort();
        // read number (count) of pair sets
        int nps = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyphs)");
            log.debug(tableTag + " pair positioning coverage table offset: " + co);
            log.debug(tableTag + " pair positioning value format #1: " + vf1);
            log.debug(tableTag + " pair positioning value format #2: " + vf2);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " pair positioning coverage", subtableOffset + co);
        // read pair value matrix
        GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues [ nps ][];
        for (int i = 0, n = nps; i < n; i++) {
            // read pair set offset
            int pso = data.readUnsignedShort();
            // read pair set table at offset
            pvm [ i ] = readPosPairSetTable(subtableOffset, pso, vf1, vf2);
        }
        // store results
        seMapping = ct;
        seEntries.add(pvm);
    }

    private void readPairPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read value format for first glyph
        int vf1 = data.readUnsignedShort();
        // read value format for second glyph
        int vf2 = data.readUnsignedShort();
        // read class def 1 offset
        int cd1o = data.readUnsignedShort();
        // read class def 2 offset
        int cd2o = data.readUnsignedShort();
        // read number (count) of classes in class def 1 table
        int nc1 = data.readUnsignedShort();
        // read number (count) of classes in class def 2 table
        int nc2 = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyph classes)");
            log.debug(tableTag + " pair positioning coverage table offset: " + co);
            log.debug(tableTag + " pair positioning value format #1: " + vf1);
            log.debug(tableTag + " pair positioning value format #2: " + vf2);
            log.debug(tableTag + " pair positioning class def table #1 offset: " + cd1o);
            log.debug(tableTag + " pair positioning class def table #2 offset: " + cd2o);
            log.debug(tableTag + " pair positioning class #1 count: " + nc1);
            log.debug(tableTag + " pair positioning class #2 count: " + nc2);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " pair positioning coverage", subtableOffset + co);
        // read class definition table #1
        GlyphClassTable cdt1 = readClassDefTable(tableTag + " pair positioning class definition #1", subtableOffset + cd1o);
        // read class definition table #2
        GlyphClassTable cdt2 = readClassDefTable(tableTag + " pair positioning class definition #2", subtableOffset + cd2o);
        // read pair value matrix
        GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues [ nc1 ] [ nc2 ];
        for (int i = 0; i < nc1; i++) {
            for (int j = 0; j < nc2; j++) {
                GlyphPositioningTable.PairValues pv = readPosPairValues(subtableOffset, false, vf1, vf2);
                pvm [ i ] [ j ] = pv;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " pair set table value[" + i + "][" + j + "]: " + pv);
                }
            }
        }
        // store results
        seMapping = ct;
        seEntries.add(cdt1);
        seEntries.add(cdt2);
        seEntries.add(Integer.valueOf(nc1));
        seEntries.add(Integer.valueOf(nc2));
        seEntries.add(pvm);
    }

    private int readPairPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readPairPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readPairPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported pair positioning subtable format: " + sf);
        }
        return sf;
    }

    private GlyphPositioningTable.Anchor readPosAnchor(long anchorTableOffset) throws IOException {
        GlyphPositioningTable.Anchor a;
        long cp = data.getCurrentPosition();
        data.seek(anchorTableOffset);
        // read anchor table format
        int af = data.readUnsignedShort();
        if (af == 1) {
            // read x coordinate
            int x = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            // read y coordinate
            int y = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            a = new GlyphPositioningTable.Anchor(x, y);
        } else if (af == 2) {
            // read x coordinate
            int x = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            // read y coordinate
            int y = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            // read anchor point index
            int ap = data.readUnsignedShort();
            a = new GlyphPositioningTable.Anchor(x, y, ap);
        } else if (af == 3) {
            // read x coordinate
            int x = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            // read y coordinate
            int y = otf.convertTTFUnit2PDFUnit(data.readSignedShort());
            // read x device table offset
            int xdo = data.readUnsignedShort();
            // read y device table offset
            int ydo = data.readUnsignedShort();
            // read x device table (if present)
            GlyphPositioningTable.DeviceTable xd;
            if (xdo != 0) {
                xd = readPosDeviceTable(cp, xdo);
            } else {
                xd = null;
            }
            // read y device table (if present)
            GlyphPositioningTable.DeviceTable yd;
            if (ydo != 0) {
                yd = readPosDeviceTable(cp, ydo);
            } else {
                yd = null;
            }
            a = new GlyphPositioningTable.Anchor(x, y, xd, yd);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported positioning anchor format: " + af);
        }
        data.seek(cp);
        return a;
    }

    private void readCursivePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read entry/exit count
        int ec = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " cursive positioning subtable format: " + subtableFormat);
            log.debug(tableTag + " cursive positioning coverage table offset: " + co);
            log.debug(tableTag + " cursive positioning entry/exit count: " + ec);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " cursive positioning coverage", subtableOffset + co);
        // read entry/exit records
        GlyphPositioningTable.Anchor[] aa = new GlyphPositioningTable.Anchor [ ec * 2 ];
        for (int i = 0, n = ec; i < n; i++) {
            // read entry anchor offset
            int eno = data.readUnsignedShort();
            // read exit anchor offset
            int exo = data.readUnsignedShort();
            // read entry anchor
            GlyphPositioningTable.Anchor ena;
            if (eno > 0) {
                ena = readPosAnchor(subtableOffset + eno);
            } else {
                ena = null;
            }
            // read exit anchor
            GlyphPositioningTable.Anchor exa;
            if (exo > 0) {
                exa = readPosAnchor(subtableOffset + exo);
            } else {
                exa = null;
            }
            aa [ (i * 2) + 0 ] = ena;
            aa [ (i * 2) + 1 ] = exa;
            if (log.isDebugEnabled()) {
                if (ena != null) {
                    log.debug(tableTag + " cursive entry anchor [" + i + "]: " + ena);
                }
                if (exa != null) {
                    log.debug(tableTag + " cursive exit anchor  [" + i + "]: " + exa);
                }
            }
        }
        // store results
        seMapping = ct;
        seEntries.add(aa);
    }

    private int readCursivePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readCursivePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported cursive positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readMarkToBasePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read mark coverage offset
        int mco = data.readUnsignedShort();
        // read base coverage offset
        int bco = data.readUnsignedShort();
        // read mark class count
        int nmc = data.readUnsignedShort();
        // read mark array offset
        int mao = data.readUnsignedShort();
        // read base array offset
        int bao = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning subtable format: " + subtableFormat);
            log.debug(tableTag + " mark-to-base positioning mark coverage table offset: " + mco);
            log.debug(tableTag + " mark-to-base positioning base coverage table offset: " + bco);
            log.debug(tableTag + " mark-to-base positioning mark class count: " + nmc);
            log.debug(tableTag + " mark-to-base positioning mark array offset: " + mao);
            log.debug(tableTag + " mark-to-base positioning base array offset: " + bao);
        }
        // read mark coverage table
        GlyphCoverageTable mct = readCoverageTable(tableTag + " mark-to-base positioning mark coverage", subtableOffset + mco);
        // read base coverage table
        GlyphCoverageTable bct = readCoverageTable(tableTag + " mark-to-base positioning base coverage", subtableOffset + bco);
        // read mark anchor array
        // seek to mark array
        data.seek(subtableOffset + mao);
        // read mark count
        int nm = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning mark count: " + nm);
        }
        // read mark anchor array, where i:{0...markCount}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm ];
        for (int i = 0; i < nm; i++) {
            // read mark class
            int mc = data.readUnsignedShort();
            // read mark anchor offset
            int ao = data.readUnsignedShort();
            GlyphPositioningTable.Anchor a;
            if (ao > 0) {
                a = readPosAnchor(subtableOffset + mao + ao);
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if (a != null) {
                ma = new GlyphPositioningTable.MarkAnchor(mc, a);
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-base positioning mark anchor[" + i + "]: " + ma);
            }

        }
        // read base anchor matrix
        // seek to base array
        data.seek(subtableOffset + bao);
        // read base count
        int nb = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning base count: " + nb);
        }
        // read anchor matrix, where i:{0...baseCount - 1}, j:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][] bam = new GlyphPositioningTable.Anchor [ nb ] [ nmc ];
        for (int i = 0; i < nb; i++) {
            for (int j = 0; j < nmc; j++) {
                // read base anchor offset
                int ao = data.readUnsignedShort();
                GlyphPositioningTable.Anchor a;
                if (ao > 0) {
                    a = readPosAnchor(subtableOffset + bao + ao);
                } else {
                    a = null;
                }
                bam [ i ] [ j ] = a;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " mark-to-base positioning base anchor[" + i + "][" + j + "]: " + a);
                }
            }
        }
        // store results
        seMapping = mct;
        seEntries.add(bct);
        seEntries.add(Integer.valueOf(nmc));
        seEntries.add(maa);
        seEntries.add(bam);
    }

    private int readMarkToBasePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readMarkToBasePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported mark-to-base positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readMarkToLigaturePosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read mark coverage offset
        int mco = data.readUnsignedShort();
        // read ligature coverage offset
        int lco = data.readUnsignedShort();
        // read mark class count
        int nmc = data.readUnsignedShort();
        // read mark array offset
        int mao = data.readUnsignedShort();
        // read ligature array offset
        int lao = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning subtable format: " + subtableFormat);
            log.debug(tableTag + " mark-to-ligature positioning mark coverage table offset: " + mco);
            log.debug(tableTag + " mark-to-ligature positioning ligature coverage table offset: " + lco);
            log.debug(tableTag + " mark-to-ligature positioning mark class count: " + nmc);
            log.debug(tableTag + " mark-to-ligature positioning mark array offset: " + mao);
            log.debug(tableTag + " mark-to-ligature positioning ligature array offset: " + lao);
        }
        // read mark coverage table
        GlyphCoverageTable mct = readCoverageTable(tableTag + " mark-to-ligature positioning mark coverage", subtableOffset + mco);
        // read ligature coverage table
        GlyphCoverageTable lct = readCoverageTable(tableTag + " mark-to-ligature positioning ligature coverage", subtableOffset + lco);
        // read mark anchor array
        // seek to mark array
        data.seek(subtableOffset + mao);
        // read mark count
        int nm = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning mark count: " + nm);
        }
        // read mark anchor array, where i:{0...markCount}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm ];
        for (int i = 0; i < nm; i++) {
            // read mark class
            int mc = data.readUnsignedShort();
            // read mark anchor offset
            int ao = data.readUnsignedShort();
            GlyphPositioningTable.Anchor a;
            if (ao > 0) {
                a = readPosAnchor(subtableOffset + mao + ao);
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if (a != null) {
                ma = new GlyphPositioningTable.MarkAnchor(mc, a);
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-ligature positioning mark anchor[" + i + "]: " + ma);
            }
        }
        // read ligature anchor matrix
        // seek to ligature array
        data.seek(subtableOffset + lao);
        // read ligature count
        int nl = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning ligature count: " + nl);
        }
        // read ligature attach table offsets
        int[] laoa = new int [ nl ];
        for (int i = 0; i < nl; i++) {
            laoa [ i ] = data.readUnsignedShort();
        }
        // iterate over ligature attach tables, recording maximum component count
        int mxc = 0;
        for (int i = 0; i < nl; i++) {
            int lato = laoa [ i ];
            data.seek(subtableOffset + lao + lato);
            // read component count
            int cc = data.readUnsignedShort();
            if (cc > mxc) {
                mxc = cc;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning maximum component count: " + mxc);
        }
        // read anchor matrix, where i:{0...ligatureCount - 1}, j:{0...maxComponentCount - 1}, k:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][][] lam = new GlyphPositioningTable.Anchor [ nl ][][];
        for (int i = 0; i < nl; i++) {
            int lato = laoa [ i ];
            // seek to ligature attach table for ligature[i]
            data.seek(subtableOffset + lao + lato);
            // read component count
            int cc = data.readUnsignedShort();
            GlyphPositioningTable.Anchor[][] lcm = new GlyphPositioningTable.Anchor [ cc ] [ nmc ];
            for (int j = 0; j < cc; j++) {
                for (int k = 0; k < nmc; k++) {
                    // read ligature anchor offset
                    int ao = data.readUnsignedShort();
                    GlyphPositioningTable.Anchor a;
                    if (ao > 0) {
                        a  = readPosAnchor(subtableOffset + lao + lato + ao);
                    } else {
                        a = null;
                    }
                    lcm [ j ] [ k ] = a;
                    if (log.isDebugEnabled()) {
                        log.debug(tableTag + " mark-to-ligature positioning ligature anchor[" + i + "][" + j + "][" + k + "]: " + a);
                    }
                }
            }
            lam [ i ] = lcm;
        }
        // store results
        seMapping = mct;
        seEntries.add(lct);
        seEntries.add(Integer.valueOf(nmc));
        seEntries.add(Integer.valueOf(mxc));
        seEntries.add(maa);
        seEntries.add(lam);
    }

    private int readMarkToLigaturePosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readMarkToLigaturePosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported mark-to-ligature positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readMarkToMarkPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read mark #1 coverage offset
        int m1co = data.readUnsignedShort();
        // read mark #2 coverage offset
        int m2co = data.readUnsignedShort();
        // read mark class count
        int nmc = data.readUnsignedShort();
        // read mark #1 array offset
        int m1ao = data.readUnsignedShort();
        // read mark #2 array offset
        int m2ao = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning subtable format: " + subtableFormat);
            log.debug(tableTag + " mark-to-mark positioning mark #1 coverage table offset: " + m1co);
            log.debug(tableTag + " mark-to-mark positioning mark #2 coverage table offset: " + m2co);
            log.debug(tableTag + " mark-to-mark positioning mark class count: " + nmc);
            log.debug(tableTag + " mark-to-mark positioning mark #1 array offset: " + m1ao);
            log.debug(tableTag + " mark-to-mark positioning mark #2 array offset: " + m2ao);
        }
        // read mark #1 coverage table
        GlyphCoverageTable mct1 = readCoverageTable(tableTag + " mark-to-mark positioning mark #1 coverage", subtableOffset + m1co);
        // read mark #2 coverage table
        GlyphCoverageTable mct2 = readCoverageTable(tableTag + " mark-to-mark positioning mark #2 coverage", subtableOffset + m2co);
        // read mark #1 anchor array
        // seek to mark array
        data.seek(subtableOffset + m1ao);
        // read mark count
        int nm1 = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning mark #1 count: " + nm1);
        }
        // read mark anchor array, where i:{0...mark1Count}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm1 ];
        for (int i = 0; i < nm1; i++) {
            // read mark class
            int mc = data.readUnsignedShort();
            // read mark anchor offset
            int ao = data.readUnsignedShort();
            GlyphPositioningTable.Anchor a;
            if (ao > 0) {
                a = readPosAnchor(subtableOffset + m1ao + ao);
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if (a != null) {
                ma = new GlyphPositioningTable.MarkAnchor(mc, a);
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-mark positioning mark #1 anchor[" + i + "]: " + ma);
            }
        }
        // read mark #2 anchor matrix
        // seek to mark #2 array
        data.seek(subtableOffset + m2ao);
        // read mark #2 count
        int nm2 = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning mark #2 count: " + nm2);
        }
        // read anchor matrix, where i:{0...mark2Count - 1}, j:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][] mam = new GlyphPositioningTable.Anchor [ nm2 ] [ nmc ];
        for (int i = 0; i < nm2; i++) {
            for (int j = 0; j < nmc; j++) {
                // read mark anchor offset
                int ao = data.readUnsignedShort();
                GlyphPositioningTable.Anchor a;
                if (ao > 0) {
                    a = readPosAnchor(subtableOffset + m2ao + ao);
                } else {
                    a = null;
                }
                mam [ i ] [ j ] = a;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " mark-to-mark positioning mark #2 anchor[" + i + "][" + j + "]: " + a);
                }
            }
        }
        // store results
        seMapping = mct1;
        seEntries.add(mct2);
        seEntries.add(Integer.valueOf(nmc));
        seEntries.add(maa);
        seEntries.add(mam);
    }

    private int readMarkToMarkPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readMarkToMarkPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported mark-to-mark positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readContextualPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read rule set count
        int nrs = data.readUnsignedShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for (int i = 0; i < nrs; i++) {
            rsoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyphs)");
            log.debug(tableTag + " contextual positioning coverage table offset: " + co);
            log.debug(tableTag + " contextual positioning rule set count: " + nrs);
            for (int i = 0; i < nrs; i++) {
                log.debug(tableTag + " contextual positioning rule set offset[" + i + "]: " + rsoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " contextual positioning coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ nrs ];
        String header = null;
        for (int i = 0; i < nrs; i++) {
            AdvancedTypographicTable.RuleSet rs;
            int rso = rsoa [ i ];
            if (rso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + rso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    AdvancedTypographicTable.GlyphSequenceRule r;
                    int ro = roa [ j ];
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + rso + ro);
                        // read glyph count
                        int ng = data.readUnsignedShort();
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read glyphs
                        int[] glyphs = new int [ ng - 1 ];
                        for (int k = 0, nk = glyphs.length; k < nk; k++) {
                            glyphs [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.GlyphSequenceRule(lookups, ng, glyphs);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(rsa);
    }

    private void readContextualPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read class def table offset
        int cdo = data.readUnsignedShort();
        // read class rule set count
        int ngc = data.readUnsignedShort();
        // read class rule set offsets
        int[] csoa = new int [ ngc ];
        for (int i = 0; i < ngc; i++) {
            csoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph classes)");
            log.debug(tableTag + " contextual positioning coverage table offset: " + co);
            log.debug(tableTag + " contextual positioning class set count: " + ngc);
            for (int i = 0; i < ngc; i++) {
                log.debug(tableTag + " contextual positioning class set offset[" + i + "]: " + csoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " contextual positioning coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read class definition table
        GlyphClassTable cdt;
        if (cdo > 0) {
            cdt = readClassDefTable(tableTag + " contextual positioning class definition", subtableOffset + cdo);
        } else {
            cdt = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ ngc ];
        String header = null;
        for (int i = 0; i < ngc; i++) {
            int cso = csoa [ i ];
            AdvancedTypographicTable.RuleSet rs;
            if (cso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + cso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    int ro = roa [ j ];
                    AdvancedTypographicTable.ClassSequenceRule r;
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + cso + ro);
                        // read glyph count
                        int ng = data.readUnsignedShort();
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read classes
                        int[] classes = new int [ ng - 1 ];
                        for (int k = 0, nk = classes.length; k < nk; k++) {
                            classes [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ClassSequenceRule(lookups, ng, classes);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(cdt);
        seEntries.add(Integer.valueOf(ngc));
        seEntries.add(rsa);
    }

    private void readContextualPosTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read glyph (input sequence length) count
        int ng = data.readUnsignedShort();
        // read positioning lookup count
        int nl = data.readUnsignedShort();
        // read glyph coverage offsets, one per glyph input sequence length count
        int[] gcoa = new int [ ng ];
        for (int i = 0; i < ng; i++) {
            gcoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph sets)");
            log.debug(tableTag + " contextual positioning glyph input sequence length count: " + ng);
            log.debug(tableTag + " contextual positioning lookup count: " + nl);
            for (int i = 0; i < ng; i++) {
                log.debug(tableTag + " contextual positioning coverage table offset[" + i + "]: " + gcoa[i]);
            }
        }
        // read coverage tables
        GlyphCoverageTable[] gca = new GlyphCoverageTable [ ng ];
        for (int i = 0; i < ng; i++) {
            int gco = gcoa [ i ];
            GlyphCoverageTable gct;
            if (gco > 0) {
                gct = readCoverageTable(tableTag + " contextual positioning coverage[" + i + "]", subtableOffset + gcoa[i]);
            } else {
                gct = null;
            }
            gca [ i ] = gct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " contextual positioning lookups: ";
        }
        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
        // construct rule, rule set, and rule set array
        AdvancedTypographicTable.Rule r = new AdvancedTypographicTable.CoverageSequenceRule(lookups, ng, gca);
        AdvancedTypographicTable.RuleSet rs = new AdvancedTypographicTable.HomogeneousRuleSet(new AdvancedTypographicTable.Rule[] {r});
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet[] {rs};
        // store results
        assert (gca != null) && (gca.length > 0);
        seMapping = gca[0];
        seEntries.add(rsa);
    }

    private int readContextualPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readContextualPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readContextualPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 3) {
            readContextualPosTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported contextual positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readChainedContextualPosTableFormat1(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read rule set count
        int nrs = data.readUnsignedShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for (int i = 0; i < nrs; i++) {
            rsoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyphs)");
            log.debug(tableTag + " chained contextual positioning coverage table offset: " + co);
            log.debug(tableTag + " chained contextual positioning rule set count: " + nrs);
            for (int i = 0; i < nrs; i++) {
                log.debug(tableTag + " chained contextual positioning rule set offset[" + i + "]: " + rsoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " chained contextual positioning coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ nrs ];
        String header = null;
        for (int i = 0; i < nrs; i++) {
            AdvancedTypographicTable.RuleSet rs;
            int rso = rsoa [ i ];
            if (rso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + rso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    AdvancedTypographicTable.ChainedGlyphSequenceRule r;
                    int ro = roa [ j ];
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + rso + ro);
                        // read backtrack glyph count
                        int nbg = data.readUnsignedShort();
                        // read backtrack glyphs
                        int[] backtrackGlyphs = new int [ nbg ];
                        for (int k = 0, nk = backtrackGlyphs.length; k < nk; k++) {
                            backtrackGlyphs [ k ] = data.readUnsignedShort();
                        }
                        // read input glyph count
                        int nig = data.readUnsignedShort();
                        // read glyphs
                        int[] glyphs = new int [ nig - 1 ];
                        for (int k = 0, nk = glyphs.length; k < nk; k++) {
                            glyphs [ k ] = data.readUnsignedShort();
                        }
                        // read lookahead glyph count
                        int nlg = data.readUnsignedShort();
                        // read lookahead glyphs
                        int[] lookaheadGlyphs = new int [ nlg ];
                        for (int k = 0, nk = lookaheadGlyphs.length; k < nk; k++) {
                            lookaheadGlyphs [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ChainedGlyphSequenceRule(lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(rsa);
    }

    private void readChainedContextualPosTableFormat2(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read backtrack class def table offset
        int bcdo = data.readUnsignedShort();
        // read input class def table offset
        int icdo = data.readUnsignedShort();
        // read lookahead class def table offset
        int lcdo = data.readUnsignedShort();
        // read class set count
        int ngc = data.readUnsignedShort();
        // read class set offsets
        int[] csoa = new int [ ngc ];
        for (int i = 0; i < ngc; i++) {
            csoa [ i ] = data.readUnsignedShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph classes)");
            log.debug(tableTag + " chained contextual positioning coverage table offset: " + co);
            log.debug(tableTag + " chained contextual positioning class set count: " + ngc);
            for (int i = 0; i < ngc; i++) {
                log.debug(tableTag + " chained contextual positioning class set offset[" + i + "]: " + csoa[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if (co > 0) {
            ct = readCoverageTable(tableTag + " chained contextual positioning coverage", subtableOffset + co);
        } else {
            ct = null;
        }
        // read backtrack class definition table
        GlyphClassTable bcdt;
        if (bcdo > 0) {
            bcdt = readClassDefTable(tableTag + " contextual positioning backtrack class definition", subtableOffset + bcdo);
        } else {
            bcdt = null;
        }
        // read input class definition table
        GlyphClassTable icdt;
        if (icdo > 0) {
            icdt = readClassDefTable(tableTag + " contextual positioning input class definition", subtableOffset + icdo);
        } else {
            icdt = null;
        }
        // read lookahead class definition table
        GlyphClassTable lcdt;
        if (lcdo > 0) {
            lcdt = readClassDefTable(tableTag + " contextual positioning lookahead class definition", subtableOffset + lcdo);
        } else {
            lcdt = null;
        }
        // read rule sets
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet [ ngc ];
        String header = null;
        for (int i = 0; i < ngc; i++) {
            int cso = csoa [ i ];
            AdvancedTypographicTable.RuleSet rs;
            if (cso > 0) {
                // seek to rule set [ i ]
                data.seek(subtableOffset + cso);
                // read rule count
                int nr = data.readUnsignedShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                AdvancedTypographicTable.Rule[] ra = new AdvancedTypographicTable.Rule [ nr ];
                for (int j = 0; j < nr; j++) {
                    roa [ j ] = data.readUnsignedShort();
                }
                // read glyph sequence rules
                for (int j = 0; j < nr; j++) {
                    AdvancedTypographicTable.ChainedClassSequenceRule r;
                    int ro = roa [ j ];
                    if (ro > 0) {
                        // seek to rule [ j ]
                        data.seek(subtableOffset + cso + ro);
                        // read backtrack glyph class count
                        int nbc = data.readUnsignedShort();
                        // read backtrack glyph classes
                        int[] backtrackClasses = new int [ nbc ];
                        for (int k = 0, nk = backtrackClasses.length; k < nk; k++) {
                            backtrackClasses [ k ] = data.readUnsignedShort();
                        }
                        // read input glyph class count
                        int nic = data.readUnsignedShort();
                        // read input glyph classes
                        int[] classes = new int [ nic - 1 ];
                        for (int k = 0, nk = classes.length; k < nk; k++) {
                            classes [ k ] = data.readUnsignedShort();
                        }
                        // read lookahead glyph class count
                        int nlc = data.readUnsignedShort();
                        // read lookahead glyph classes
                        int[] lookaheadClasses = new int [ nlc ];
                        for (int k = 0, nk = lookaheadClasses.length; k < nk; k++) {
                            lookaheadClasses [ k ] = data.readUnsignedShort();
                        }
                        // read rule lookup count
                        int nl = data.readUnsignedShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
                        r = new AdvancedTypographicTable.ChainedClassSequenceRule(lookups, nic, classes, backtrackClasses, lookaheadClasses);
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new AdvancedTypographicTable.HomogeneousRuleSet(ra);
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add(icdt);
        seEntries.add(bcdt);
        seEntries.add(lcdt);
        seEntries.add(Integer.valueOf(ngc));
        seEntries.add(rsa);
    }

    private void readChainedContextualPosTableFormat3(int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read backtrack glyph count
        int nbg = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for (int i = 0; i < nbg; i++) {
            bgcoa [ i ] = data.readUnsignedShort();
        }
        // read input glyph count
        int nig = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] igcoa = new int [ nig ];
        for (int i = 0; i < nig; i++) {
            igcoa [ i ] = data.readUnsignedShort();
        }
        // read lookahead glyph count
        int nlg = data.readUnsignedShort();
        // read backtrack glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for (int i = 0; i < nlg; i++) {
            lgcoa [ i ] = data.readUnsignedShort();
        }
        // read positioning lookup count
        int nl = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph sets)");
            log.debug(tableTag + " chained contextual positioning backtrack glyph count: " + nbg);
            for (int i = 0; i < nbg; i++) {
                log.debug(tableTag + " chained contextual positioning backtrack coverage table offset[" + i + "]: " + bgcoa[i]);
            }
            log.debug(tableTag + " chained contextual positioning input glyph count: " + nig);
            for (int i = 0; i < nig; i++) {
                log.debug(tableTag + " chained contextual positioning input coverage table offset[" + i + "]: " + igcoa[i]);
            }
            log.debug(tableTag + " chained contextual positioning lookahead glyph count: " + nlg);
            for (int i = 0; i < nlg; i++) {
                log.debug(tableTag + " chained contextual positioning lookahead coverage table offset[" + i + "]: " + lgcoa[i]);
            }
            log.debug(tableTag + " chained contextual positioning lookup count: " + nl);
        }
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for (int i = 0; i < nbg; i++) {
            int bgco = bgcoa [ i ];
            GlyphCoverageTable bgct;
            if (bgco > 0) {
                bgct = readCoverageTable(tableTag + " chained contextual positioning backtrack coverage[" + i + "]", subtableOffset + bgco);
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read input coverage tables
        GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];
        for (int i = 0; i < nig; i++) {
            int igco = igcoa [ i ];
            GlyphCoverageTable igct;
            if (igco > 0) {
                igct = readCoverageTable(tableTag + " chained contextual positioning input coverage[" + i + "]", subtableOffset + igco);
            } else {
                igct = null;
            }
            igca[i] = igct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for (int i = 0; i < nlg; i++) {
            int lgco = lgcoa [ i ];
            GlyphCoverageTable lgct;
            if (lgco > 0) {
                lgct = readCoverageTable(tableTag + " chained contextual positioning lookahead coverage[" + i + "]", subtableOffset + lgco);
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " chained contextual positioning lookups: ";
        }
        AdvancedTypographicTable.RuleLookup[] lookups = readRuleLookups(nl, header);
        // construct rule, rule set, and rule set array
        AdvancedTypographicTable.Rule r = new AdvancedTypographicTable.ChainedCoverageSequenceRule(lookups, nig, igca, bgca, lgca);
        AdvancedTypographicTable.RuleSet rs = new AdvancedTypographicTable.HomogeneousRuleSet(new AdvancedTypographicTable.Rule[] {r});
        AdvancedTypographicTable.RuleSet[] rsa = new AdvancedTypographicTable.RuleSet[] {rs};
        // store results
        assert (igca != null) && (igca.length > 0);
        seMapping = igca[0];
        seEntries.add(rsa);
    }

    private int readChainedContextualPosTable(int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readChainedContextualPosTableFormat1(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 2) {
            readChainedContextualPosTableFormat2(lookupType, lookupFlags, subtableOffset, sf);
        } else if (sf == 3) {
            readChainedContextualPosTableFormat3(lookupType, lookupFlags, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported chained contextual positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readExtensionPosTableFormat1(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read extension lookup type
        int lt = data.readUnsignedShort();
        // read extension offset
        long eo = data.readUnsignedInt();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " extension positioning subtable format: " + subtableFormat);
            log.debug(tableTag + " extension positioning lookup type: " + lt);
            log.debug(tableTag + " extension positioning lookup table offset: " + eo);
        }
        // read referenced subtable from extended offset
        readGPOSSubtable(lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo);
    }

    private int readExtensionPosTable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read positioning subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readExtensionPosTableFormat1(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported extension positioning subtable format: " + sf);
        }
        return sf;
    }

    private void readGPOSSubtable(int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        initATSubState();
        int subtableFormat = -1;
        switch (lookupType) {
        case GPOSLookupType.SINGLE:
            subtableFormat = readSinglePosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.PAIR:
            subtableFormat = readPairPosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.CURSIVE:
            subtableFormat = readCursivePosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.MARK_TO_BASE:
            subtableFormat = readMarkToBasePosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.MARK_TO_LIGATURE:
            subtableFormat = readMarkToLigaturePosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.MARK_TO_MARK:
            subtableFormat = readMarkToMarkPosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.CONTEXTUAL:
            subtableFormat = readContextualPosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.CHAINED_CONTEXTUAL:
            subtableFormat = readChainedContextualPosTable(lookupType, lookupFlags, subtableOffset);
            break;
        case GPOSLookupType.EXTENSION:
            subtableFormat = readExtensionPosTable(lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset);
            break;
        default:
            break;
        }
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_POSITIONING, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat);
        resetATSubState();
    }

    private void readLookupTable(String tableTag, int lookupSequence, long lookupTable) throws IOException {
        boolean isGSUB = tableTag.equals(GlyphSubstitutionTable.TAG);
        boolean isGPOS = tableTag.equals(GlyphPositioningTable.TAG);
        data.seek(lookupTable);
        // read lookup type
        int lt = data.readUnsignedShort();
        // read lookup flags
        int lf = data.readUnsignedShort();
        // read sub-table count
        int ns = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            String lts;
            if (isGSUB) {
                lts = GSUBLookupType.toString(lt);
            } else if (isGPOS) {
                lts = GPOSLookupType.toString(lt);
            } else {
                lts = "?";
            }
            log.debug(tableTag + " lookup table type: " + lt + " (" + lts + ")");
            log.debug(tableTag + " lookup table flags: " + lf + " (" + LookupFlag.toString(lf) + ")");
            log.debug(tableTag + " lookup table subtable count: " + ns);
        }
        // read subtable offsets
        int[] soa = new int[ns];
        for (int i = 0; i < ns; i++) {
            int so = data.readUnsignedShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lookup table subtable offset: " + so);
            }
            soa[i] = so;
        }
        // read mark filtering set
        if ((lf & LookupFlag.USE_MARK_FILTERING_SET) != 0) {
            // read mark filtering set
            int fs = data.readUnsignedShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lookup table mark filter set: " + fs);
            }
        }
        // read subtables
        for (int i = 0; i < ns; i++) {
            int so = soa[i];
            if (isGSUB) {
                readGSUBSubtable(lt, lf, lookupSequence, i, lookupTable + so);
            } else if (isGPOS) {
                readGPOSSubtable(lt, lf, lookupSequence, i, lookupTable + so);
            }
        }
    }

    private void readLookupList(String tableTag, long lookupList) throws IOException {
        data.seek(lookupList);
        // read lookup record count
        int nl = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lookup list record count: " + nl);
        }
        if (nl > 0) {
            int[] loa = new int[nl];
            // read lookup records
            for (int i = 0, n = nl; i < n; i++) {
                int lo = data.readUnsignedShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lookup table offset: " + lo);
                }
                loa[i] = lo;
            }
            // read lookup tables
            for (int i = 0, n = nl; i < n; i++) {
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lookup index: " + i);
                }
                readLookupTable(tableTag, i, lookupList + loa [ i ]);
            }
        }
    }

    /**
     * Read the common layout tables (used by GSUB and GPOS).
     * @param tableTag tag of table being read
     * @param scriptList offset to script list from beginning of font file
     * @param featureList offset to feature list from beginning of font file
     * @param lookupList offset to lookup list from beginning of font file
     * @throws IOException In case of a I/O problem
     */
    private void readCommonLayoutTables(String tableTag, long scriptList, long featureList, long lookupList) throws IOException {
        if (scriptList > 0) {
            readScriptList(tableTag, scriptList);
        }
        if (featureList > 0) {
            readFeatureList(tableTag, featureList);
        }
        if (lookupList > 0) {
            readLookupList(tableTag, lookupList);
        }
    }

    private void readGDEFClassDefTable(String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initATSubState();
        data.seek(subtableOffset);
        // subtable is a bare class definition table
        GlyphClassTable ct = readClassDefTable(tableTag + " glyph class definition table", subtableOffset);
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.GLYPH_CLASS, 0, lookupSequence, 0, 1);
        resetATSubState();
    }

    private void readGDEFAttachmentTable(String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initATSubState();
        data.seek(subtableOffset);
        // read coverage offset
        int co = data.readUnsignedShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " attachment point coverage table offset: " + co);
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " attachment point coverage", subtableOffset + co);
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.ATTACHMENT_POINT, 0, lookupSequence, 0, 1);
        resetATSubState();
    }

    private void readGDEFLigatureCaretTable(String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initATSubState();
        data.seek(subtableOffset);
        // read coverage offset
        int co = data.readUnsignedShort();
        // read ligature glyph count
        int nl = data.readUnsignedShort();
        // read ligature glyph table offsets
        int[] lgto = new int [ nl ];
        for (int i = 0; i < nl; i++) {
            lgto [ i ] = data.readUnsignedShort();
        }

        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " ligature caret coverage table offset: " + co);
            log.debug(tableTag + " ligature caret ligature glyph count: " + nl);
            for (int i = 0; i < nl; i++) {
                log.debug(tableTag + " ligature glyph table offset[" + i + "]: " + lgto[i]);
            }
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable(tableTag + " ligature caret coverage", subtableOffset + co);
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.LIGATURE_CARET, 0, lookupSequence, 0, 1);
        resetATSubState();
    }

    private void readGDEFMarkAttachmentTable(String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initATSubState();
        data.seek(subtableOffset);
        // subtable is a bare class definition table
        GlyphClassTable ct = readClassDefTable(tableTag + " glyph class definition table", subtableOffset);
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.MARK_ATTACHMENT, 0, lookupSequence, 0, 1);
        resetATSubState();
    }

    private void readGDEFMarkGlyphsTableFormat1(String tableTag, int lookupSequence, long subtableOffset, int subtableFormat) throws IOException {
        initATSubState();
        data.seek(subtableOffset);
        // skip over format (already known)
        data.skip(2);
        // read mark set class count
        int nmc = data.readUnsignedShort();
        long[] mso = new long [ nmc ];
        // read mark set coverage offsets
        for (int i = 0; i < nmc; i++) {
            mso [ i ] = data.readUnsignedInt();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark set subtable format: " + subtableFormat + " (glyph sets)");
            log.debug(tableTag + " mark set class count: " + nmc);
            for (int i = 0; i < nmc; i++) {
                log.debug(tableTag + " mark set coverage table offset[" + i + "]: " + mso[i]);
            }
        }
        // read mark set coverage tables, one per class
        GlyphCoverageTable[] msca = new GlyphCoverageTable[nmc];
        for (int i = 0; i < nmc; i++) {
            msca[i] = readCoverageTable(tableTag + " mark set coverage[" + i + "]", subtableOffset + mso[i]);
        }
        // create combined class table from per-class coverage tables
        GlyphClassTable ct = GlyphClassTable.createClassTable(Arrays.asList(msca));
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState(AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.MARK_ATTACHMENT, 0, lookupSequence, 0, 1);
        resetATSubState();
    }

    private void readGDEFMarkGlyphsTable(String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        data.seek(subtableOffset);
        // read mark set subtable format
        int sf = data.readUnsignedShort();
        if (sf == 1) {
            readGDEFMarkGlyphsTableFormat1(tableTag, lookupSequence, subtableOffset, sf);
        } else {
            throw new AdvancedTypographicTableFormatException("unsupported mark glyph sets subtable format: " + sf);
        }
    }

    /**
     * Read the GDEF table.
     * @throws IOException In case of a I/O problem
     */
    private void readGDEF() throws IOException {
        String tableTag = GlyphDefinitionTable.TAG;
        // Initialize temporary state
        initATState();
        // Read glyph definition (GDEF) table
        long version = data.readUnsignedInt();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + (version / 65536) + "." + (version % 65536));
        }
        // glyph class definition table offset (may be null)
        int cdo = data.readUnsignedShort();
        // attach point list offset (may be null)
        int apo = data.readUnsignedShort();
        // ligature caret list offset (may be null)
        int lco = data.readUnsignedShort();
        // mark attach class definition table offset (may be null)
        int mao = data.readUnsignedShort();
        // mark glyph sets definition table offset (may be null)
        int mgo;
        if (version >= 0x00010002) {
            mgo = data.readUnsignedShort();
        } else {
            mgo = 0;
        }
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " glyph class definition table offset: " + cdo);
            log.debug(tableTag + " attachment point list offset: " + apo);
            log.debug(tableTag + " ligature caret list offset: " + lco);
            log.debug(tableTag + " mark attachment class definition table offset: " + mao);
            log.debug(tableTag + " mark glyph set definitions table offset: " + mgo);
        }
        // initialize subtable sequence number
        int seqno = 0;
        // obtain offset to start of gdef table
        long to = table.getOffset();
        // (optionally) read glyph class definition subtable
        if (cdo != 0) {
            readGDEFClassDefTable(tableTag, seqno++, to + cdo);
        }
        // (optionally) read glyph attachment point subtable
        if (apo != 0) {
            readGDEFAttachmentTable(tableTag, seqno++, to + apo);
        }
        // (optionally) read ligature caret subtable
        if (lco != 0) {
            readGDEFLigatureCaretTable(tableTag, seqno++, to + lco);
        }
        // (optionally) read mark attachment class subtable
        if (mao != 0) {
            readGDEFMarkAttachmentTable(tableTag, seqno++, to + mao);
        }
        // (optionally) read mark glyph sets subtable
        if (mgo != 0) {
            readGDEFMarkGlyphsTable(tableTag, seqno++, to + mgo);
        }
        initializeGDEF();
    }

    /**
     * Read the GSUB table.
     * @throws IOException In case of a I/O problem
     */
    private void readGSUB() throws IOException {
        String tableTag = GlyphSubstitutionTable.TAG;
        // Initialize temporary state
        initATState();
        // Read glyph substitution (GSUB) table
        long version = data.readUnsignedInt();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + (version / 65536) + "." + (version % 65536));
        }
        int slo = data.readUnsignedShort();
        int flo = data.readUnsignedShort();
        int llo = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list offset: " + slo);
            log.debug(tableTag + " feature list offset: " + flo);
            log.debug(tableTag + " lookup list offset: " + llo);
        }
        long to = table.getOffset();
        readCommonLayoutTables(tableTag, to + slo, to + flo, to + llo);
        initializeGSUB();
    }

    /**
     * Read the GPOS table.
     * @throws IOException In case of a I/O problem
     */
    private void readGPOS() throws IOException {
        String tableTag = GlyphPositioningTable.TAG;
        // Initialize temporary state
        initATState();
        // Read glyph positioning (GPOS) table
        long version = data.readUnsignedInt();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " version: " + (version / 65536) + "." + (version % 65536));
        }
        int slo = data.readUnsignedShort();
        int flo = data.readUnsignedShort();
        int llo = data.readUnsignedShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list offset: " + slo);
            log.debug(tableTag + " feature list offset: " + flo);
            log.debug(tableTag + " lookup list offset: " + llo);
        }
        long to = table.getOffset();
        readCommonLayoutTables(tableTag, to + slo, to + flo, to + llo);
        initializeGPOS();
    }

    /**
     * Initialize the (internal representation of the) GDEF table based on previously
     * parsed state.
     * @returns glyph definition table or null if insufficient or invalid state
     */
    private void initializeGDEF() {
        List subtables;
        if ((subtables = constructGDEFSubtables()) != null) {
            if (subtables.size() > 0) {
                if (table instanceof GlyphDefinitionTable)
                    ((GlyphDefinitionTable) table).initialize(subtables);
            }
        }
        resetATState();
    }

    /**
     * Initialize the (internal representation of the) GSUB table based on previously
     * parsed state.
     * @returns glyph substitution table or null if insufficient or invalid state
     */
    private void initializeGSUB() throws IOException {
        Map lookups;
        if ((lookups = constructLookups()) != null) {
            List subtables;
            if ((subtables = constructGSUBSubtables()) != null) {
                if ((lookups.size() > 0) && (subtables.size() > 0)) {
                    if (table instanceof GlyphSubstitutionTable)
                        ((GlyphSubstitutionTable) table).initialize(otf.getGDEF(), lookups, subtables);
                }
            }
        }
        resetATState();
    }

    /**
     * Initialize the (internal representation of the) GPOS table based on previously
     * parsed state.
     * @returns glyph positioning table or null if insufficient or invalid state
     */
    private void initializeGPOS() throws IOException {
        Map lookups;
        if ((lookups = constructLookups()) != null) {
            List subtables;
            if ((subtables = constructGPOSSubtables()) != null) {
                if ((lookups.size() > 0) && (subtables.size() > 0)) {
                    if (table instanceof GlyphPositioningTable)
                        ((GlyphPositioningTable) table).initialize(otf.getGDEF(), lookups, subtables);
                }
            }
        }
        resetATState();
    }

    private void constructLookupsFeature(Map lookups, String st, String lt, String fid) {
        Object[] fp = (Object[]) seFeatures.get(fid);
        if (fp != null) {
            assert fp.length == 2;
            String ft = (String) fp[0];                 // feature tag
            List/*<String>*/ lul = (List) fp[1];        // list of lookup table ids
            if ((ft != null) && (lul != null) && (lul.size() > 0)) {
                AdvancedTypographicTable.LookupSpec ls = new AdvancedTypographicTable.LookupSpec(st, lt, ft);
                lookups.put(ls, lul);
            }
        }
    }

    private void constructLookupsFeatures(Map lookups, String st, String lt, List/*<String>*/ fids) {
        for (Iterator fit = fids.iterator(); fit.hasNext();) {
            String fid = (String) fit.next();
            constructLookupsFeature(lookups, st, lt, fid);
        }
    }

    private void constructLookupsLanguage(Map lookups, String st, String lt, Map/*<String,Object[2]>*/ languages) {
        Object[] lp = (Object[]) languages.get(lt);
        if (lp != null) {
            assert lp.length == 2;
            if (lp[0] != null) {                      // required feature id
                constructLookupsFeature(lookups, st, lt, (String) lp[0]);
            }
            if (lp[1] != null) {                      // non-required features ids
                constructLookupsFeatures(lookups, st, lt, (List) lp[1]);
            }
        }
    }

    private void constructLookupsLanguages(Map lookups, String st, List/*<String>*/ ll, Map/*<String,Object[2]>*/ languages) {
        for (Iterator lit = ll.iterator(); lit.hasNext();) {
            String lt = (String) lit.next();
            constructLookupsLanguage(lookups, st, lt, languages);
        }
    }

    private Map constructLookups() {
        Map/*<AdvancedTypographicTable.LookupSpec,List<String>>*/ lookups = new java.util.LinkedHashMap();
        for (Iterator sit = seScripts.keySet().iterator(); sit.hasNext();) {
            String st = (String) sit.next();
            Object[] sp = (Object[]) seScripts.get(st);
            if (sp != null) {
                assert sp.length == 3;
                Map/*<String,Object[2]>*/ languages = (Map) sp[2];
                if (sp[0] != null) {                  // default language
                    constructLookupsLanguage(lookups, st, (String) sp[0], languages);
                }
                if (sp[1] != null) {                  // non-default languages
                    constructLookupsLanguages(lookups, st, (List) sp[1], languages);
                }
            }
        }
        return lookups;
    }

    private List constructGDEFSubtables() {
        List/*<GlyphDefinitionSubtable>*/ subtables = new java.util.ArrayList();
        if (seSubtables != null) {
            for (Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ((st = constructGDEFSubtable(stp)) != null) {
                    subtables.add(st);
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGDEFSubtable(Object[] stp) {
        GlyphSubtable st = null;
        assert (stp != null) && (stp.length == 8);
        Integer tt = (Integer) stp[0];          // table type
        Integer lt = (Integer) stp[1];          // lookup type
        Integer ln = (Integer) stp[2];          // lookup sequence number
        Integer lf = (Integer) stp[3];          // lookup flags
        Integer sn = (Integer) stp[4];          // subtable sequence number
        Integer sf = (Integer) stp[5];          // subtable format
        GlyphMappingTable mapping = (GlyphMappingTable) stp[6];
        List entries = (List) stp[7];
        if (tt.intValue() == AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION) {
            int type = GDEFLookupType.getSubtableType(lt.intValue());
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphDefinitionTable.createSubtable(type, lid, sequence, flags, format, mapping, entries);
        }
        return st;
    }

    private List constructGSUBSubtables() {
        List/*<GlyphSubtable>*/ subtables = new java.util.ArrayList();
        if (seSubtables != null) {
            for (Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ((st = constructGSUBSubtable(stp)) != null) {
                    subtables.add(st);
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGSUBSubtable(Object[] stp) {
        GlyphSubtable st = null;
        assert (stp != null) && (stp.length == 8);
        Integer tt = (Integer) stp[0];          // table type
        Integer lt = (Integer) stp[1];          // lookup type
        Integer ln = (Integer) stp[2];          // lookup sequence number
        Integer lf = (Integer) stp[3];          // lookup flags
        Integer sn = (Integer) stp[4];          // subtable sequence number
        Integer sf = (Integer) stp[5];          // subtable format
        GlyphCoverageTable coverage = (GlyphCoverageTable) stp[6];
        List entries = (List) stp[7];
        if (tt.intValue() == AdvancedTypographicTable.GLYPH_TABLE_TYPE_SUBSTITUTION) {
            int type = GSUBLookupType.getSubtableType(lt.intValue());
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphSubstitutionTable.createSubtable(type, lid, sequence, flags, format, coverage, entries);
        }
        return st;
    }

    private List constructGPOSSubtables() {
        List/*<GlyphSubtable>*/ subtables = new java.util.ArrayList();
        if (seSubtables != null) {
            for (Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ((st = constructGPOSSubtable(stp)) != null) {
                    subtables.add(st);
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGPOSSubtable(Object[] stp) {
        GlyphSubtable st = null;
        assert (stp != null) && (stp.length == 8);
        Integer tt = (Integer) stp[0];          // table type
        Integer lt = (Integer) stp[1];          // lookup type
        Integer ln = (Integer) stp[2];          // lookup sequence number
        Integer lf = (Integer) stp[3];          // lookup flags
        Integer sn = (Integer) stp[4];          // subtable sequence number
        Integer sf = (Integer) stp[5];          // subtable format
        GlyphCoverageTable coverage = (GlyphCoverageTable) stp[6];
        List entries = (List) stp[7];
        if (tt.intValue() == AdvancedTypographicTable.GLYPH_TABLE_TYPE_POSITIONING) {
            int type = GSUBLookupType.getSubtableType(lt.intValue());
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphPositioningTable.createSubtable(type, lid, sequence, flags, format, coverage, entries);
        }
        return st;
    }

    private void initATState() {
        seScripts = new java.util.LinkedHashMap();
        seLanguages = new java.util.LinkedHashMap();
        seFeatures = new java.util.LinkedHashMap();
        seSubtables = new java.util.ArrayList();
        resetATSubState();
    }

    private void resetATState() {
        seScripts = null;
        seLanguages = null;
        seFeatures = null;
        seSubtables = null;
        resetATSubState();
    }

    private void initATSubState() {
        seMapping = null;
        seEntries = new java.util.ArrayList();
    }

    private void extractSESubState(int tableType, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, int subtableFormat) {
        if (seEntries != null) {
            if ((tableType == AdvancedTypographicTable.GLYPH_TABLE_TYPE_DEFINITION) || (seEntries.size() > 0)) {
                if (seSubtables != null) {
                    Integer tt = Integer.valueOf(tableType);
                    Integer lt = Integer.valueOf(lookupType);
                    Integer ln = Integer.valueOf(lookupSequence);
                    Integer lf = Integer.valueOf(lookupFlags);
                    Integer sn = Integer.valueOf(subtableSequence);
                    Integer sf = Integer.valueOf(subtableFormat);
                    seSubtables.add(new Object[] { tt, lt, ln, lf, sn, sf, seMapping, seEntries });
                }
            }
        }
    }

    private void resetATSubState() {
        seMapping = null;
        seEntries = null;
    }

    private void resetATStateAll() {
        resetATState();
    }

    /** helper method for formatting an integer array for output */
    private String toString(int[] ia) {
        StringBuffer sb = new StringBuffer();
        if ((ia == null) || (ia.length == 0)) {
            sb.append('-');
        } else {
            boolean first = true;
            for (int i = 0; i < ia.length; i++) {
                if (!first) {
                    sb.append(' ');
                } else {
                    first = false;
                }
                sb.append(ia[i]);
            }
        }
        return sb.toString();
    }

}
