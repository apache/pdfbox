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

package org.apache.fontbox.ttf.advanced.util;

import java.util.Arrays;

/**
 * <p>Normalization related utilities. N.B. This implementation is an experimental
 * shortcut, the full version of which would require either using ICU4J or an extraction
 * of its normalization function, either being a significant undertaking. At present
 * we handle only specialized decomposition of Indic two part matras.</p>
 *
 * @author Glenn Adams
 */
public final class CharNormalize {

    // CSOFF: LineLength

    private CharNormalize() {
    }

    private static final int[] DECOMPOSABLES = {
        // bengali
        0x09CB,
        0x09CC,
        // oriya
        0x0B4B,
        0x0B4C,
        // tamil
        0x0BCA,
        0x0BCB,
        0x0BCC,
        // malayalam
        0x0D4A,
        0x0D4B,
        0x0D4C,
        // sinhala
        0x0DDA,
        0x0DDC,
        0x0DDD,
        0x0DDE,
    };

    private static final int[][] DECOMPOSITIONS = {
        // bengali
        { 0x09C7, 0x09BE },             // 0x09CB
        { 0x09C7, 0x09D7 },             // 0x09CC
        // oriya
        { 0x0B47, 0x0B4E },             // 0x0B4B
        { 0x0B47, 0x0B57 },             // 0x0B4C
        // tamil
        { 0x0BC6, 0x0BBE },             // 0x0BCA
        { 0x0BC7, 0x0BBE },             // 0x0BCB
        { 0x0BC6, 0x0BD7 },             // 0x0BCC
        // malayalam
        { 0x0D46, 0x0D3E },             // 0x0D4A
        { 0x0D47, 0x0D3E },             // 0x0D4B
        { 0x0D46, 0x0D57 },             // 0x0D4C
        // sinhala
        { 0x0DD9, 0x0DCA },             // 0x0DDA
        { 0x0DD9, 0x0DCF },             // 0x0DDC
        { 0x0DD9, 0x0DCF, 0x0DCA },     // 0x0DDD
        { 0x0DD9, 0x0DDF },             // 0x0DDE
    };

    private static final int MAX_DECOMPOSITION_LENGTH = 3;

    public static boolean isDecomposable(int c) {
        return Arrays.binarySearch(DECOMPOSABLES, c) >= 0;
    }

    public static int maximumDecompositionLength() {
        return MAX_DECOMPOSITION_LENGTH;
    }

    public static int[] decompose(int c, int[] da) {
        int di = Arrays.binarySearch(DECOMPOSABLES, c);
        if (di >= 0) {
            return DECOMPOSITIONS[di];
        } else if ((da != null) && (da.length > 1)) {
            da[0] = c;
            da[1] = 0;
            return da;
        } else {
            return new int[] { c };
        }
    }

}
