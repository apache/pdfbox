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

package org.apache.fontbox.ttf.gsub;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompoundCharacterTokenizerTest {

    @Test
    void testTokenize_happyPath_1() {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_100_", "_101_", "_102_", "_103_", "_104_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_100_", "_101_", "_102_", "_103_", "_104_"), tokens);
    }

    @Test
    void testTokenize_happyPath_2() {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_100_101_", "_102_", "_103_104_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_100_101_", "_102_", "_103_104_"), tokens);
    }

    @Test
    void testTokenize_happyPath_3() {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_100_101_102_", "_101_102_", "_103_104_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_100_101_102_", "_103_104_"), tokens);
    }

    @Test
    void testTokenize_happyPath_4() {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_101_102_", "_101_102_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_100_", "_101_102_", "_103_104_"), tokens);
    }

    @Test
    void testTokenize_happyPath_5() {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_201_", "_202_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Collections.singletonList("_100_101_102_103_104_"), tokens);
    }

}
