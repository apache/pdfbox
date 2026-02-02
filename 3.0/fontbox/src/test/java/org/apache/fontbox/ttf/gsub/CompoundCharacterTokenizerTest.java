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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class CompoundCharacterTokenizerTest
{
    @Test
    void testTokenize_happyPath_2()
    {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "_84_93_", "_104_82_", "_104_87_" })));
        String text = "_84_112_93_104_82_61_96_102_93_104_87_110_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_84_112_93", "_104_82_", "_61_96_102_93", "_104_87_", "_110_"),
                tokens);
    }

    @Test
    void testTokenize_happyPath_3()
    {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "_67_112_96_", "_74_112_76_" })));
        String text = "_67_112_96_103_93_108_93_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_67_112_96_", "_103_93_108_93_"), tokens);
    }

    @Test
    void testTokenize_happyPath_4()
    {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "_67_112_96_", "_74_112_76_" })));
        String text = "_94_67_112_96_112_91_103_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_94", "_67_112_96_", "_112_91_103_"), tokens);
    }

    @Test
    void testTokenize_happyPath_5()
    {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "_67_112_", "_76_112_" })));
        String text = "_94_167_112_91_103_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_94_167_112_91_103_"), tokens);
    }
    
    @Test
    void testTokenize_happyPath_6()
    {

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
    void testTokenize_happyPath_7()
    {

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
    void testTokenize_happyPath_8()
    {
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
    void testTokenize_happyPath_9()
    {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList("_101_102_", "_101_102_")));
        String text = "_100_101_102_103_104_";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(Arrays.asList("_100", "_101_102_", "_103_104_"), tokens);
    }

    @Test
    void testTokenize_happyPath_10()
    {
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
