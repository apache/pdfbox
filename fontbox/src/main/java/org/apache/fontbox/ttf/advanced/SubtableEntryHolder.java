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

import org.apache.fontbox.ttf.advanced.GlyphMappingTable.MappingRange;
import org.apache.fontbox.ttf.advanced.GlyphPositioningTable.Anchor;
import org.apache.fontbox.ttf.advanced.GlyphPositioningTable.MarkAnchor;
import org.apache.fontbox.ttf.advanced.GlyphPositioningTable.PairValues;

public class SubtableEntryHolder {
    private SubtableEntryHolder() { }

    public interface SubtableEntry { }

    public static class SEInteger implements SubtableEntry {
        public final int value;
        SEInteger(int value) {
            this.value = value;
        }
        static SEInteger valueOf(int value) {
            return new SEInteger(value);
        }
        public int get() {
          return this.value;
        }
    }

    private static class SEObject<T> implements SubtableEntry {
        final T value;
        SEObject(T value) {
            this.value = value;
        }
        T get() {
            return this.value;
        }
    }

    public static class SEMappingRange extends SEObject<MappingRange> {
        SEMappingRange(MappingRange value) { super(value); }
    }

    public static class SERuleSetList extends SEObject<AdvancedTypographicTable.RuleSet[]> {
        SERuleSetList(AdvancedTypographicTable.RuleSet[] value) { super(value); }
    }

    public static class SESequenceList extends SEObject<int[][]> {
        SESequenceList(int[][] value) { super(value); }
    }

    public static class SEIntList extends SEObject<int[]> {
        SEIntList(int[] value) { super(value); }
    }

    public static class SELigatureSet extends SEObject<GlyphSubstitutionTable.LigatureSet> {
        SELigatureSet(GlyphSubstitutionTable.LigatureSet value) { super(value); }
    }

    public static class SEGlyphClassTable extends SEObject<GlyphClassTable> {
        SEGlyphClassTable(GlyphClassTable value) { super(value); }
    }

    public static class SEGlyphCoverageTable extends SEObject<GlyphCoverageTable> {
        SEGlyphCoverageTable(GlyphCoverageTable value) { super(value); }
    }

    public static class SEGlyphCoverageTableList extends SEObject<GlyphCoverageTable[]> {
        SEGlyphCoverageTableList(GlyphCoverageTable[] value) { super(value); }
    }

    public static class SEValue extends SEObject<GlyphPositioningTable.Value> {
        SEValue(GlyphPositioningTable.Value value) { super(value); }
    }

    public static class SEValueList extends SEObject<GlyphPositioningTable.Value[]> {
        SEValueList(GlyphPositioningTable.Value[] value) { super(value); }
    }

    public static class SEPairValueMatrix extends SEObject<PairValues[][]> {
        SEPairValueMatrix(PairValues[][] value) { super(value); }
    }

    public static class SEAnchorList extends SEObject<Anchor[]> {
        SEAnchorList(Anchor[] value) { super(value); }
    }

    public static class SEAnchorMatrix extends SEObject<Anchor[][]> {
        SEAnchorMatrix(Anchor[][] value) { super(value); }
    }

    public static class SEAnchorMultiMatrix extends SEObject<Anchor[][][]> {
        SEAnchorMultiMatrix(Anchor[][][] value) { super(value); }
    }

    public static class SEMarkAnchorList extends SEObject<MarkAnchor[]> {
        SEMarkAnchorList(MarkAnchor[] value) { super(value); }
    }
}
