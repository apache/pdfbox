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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class CompoundCharacterTokenizerTest
{

    @Test
    void testTokenize_happyPath_1()
    {

        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        final String text = "12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(Arrays.asList("12345", "HrkJj", "xabbcc", "68RetP", "xxxcfb1245678",
                "Yx!23uyt", "889000"), tokens);
    }

    @Test
    void testTokenize_happyPath_2()
    {

        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "84_93", "104_82", "104_87" })));
        final String text = "84_112_93_104_82_61_96_102_93_104_87_110";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(Arrays.asList("84_112_93_", "104_82", "_61_96_102_93_", "104_87", "_110"),
                tokens);
    }

    @Test
    void testTokenize_happyPath_3()
    {

        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "67_112_96", "74_112_76" })));
        final String text = "67_112_96_103_93_108_93";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(Arrays.asList("67_112_96", "_103_93_108_93"), tokens);
    }

    @Test
    void testTokenize_happyPath_4()
    {

        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "67_112_96", "74_112_76" })));
        final String text = "94_67_112_96_112_91_103";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(Arrays.asList("94_", "67_112_96", "_112_91_103"), tokens);
    }

    @Test
    void testTokenize_happyPath_5()
    {

        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "67_112", "76_112" })));
        final String text = "94_167_112_91_103";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(Arrays.asList("94_1", "67_112", "_91_103"), tokens);
    }

    @Test
    void testTokenize_regexAtStart()
    {
        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        final String text = "Yx!23uyte12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(0, tokens.indexOf("Yx!23uyt"));
    }

    @Test
    void testTokenize_regexAtEnd()
    {
        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        final String text = "Yx!23uyte12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000HrkJj";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertEquals(0, tokens.indexOf("Yx!23uyt"));
        assertEquals(2, tokens.indexOf("HrkJj"));
        assertEquals(tokens.size() - 1, tokens.lastIndexOf("HrkJj"));
    }

    @Test
    void testTokenize_Bangla()
    {
        // given
        final CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(new HashSet<>(
                Arrays.asList(new String[] { "\u0995\u09cd\u09b7", "\u09aa\u09c1\u09a4\u09c1" })));
        final String text = "\u0986\u09ae\u09bf \u0995\u09cb\u09a8 \u09aa\u09a5\u09c7  \u0995\u09cd\u09b7\u09c0\u09b0\u09c7\u09b0 \u09b7\u09a8\u09cd\u09a1  \u09aa\u09c1\u09a4\u09c1\u09b2 \u09b0\u09c1\u09aa\u09cb  \u0997\u0999\u09cd\u0997\u09be \u098b\u09b7\u09bf";

        // when
        final List<String> tokens = tokenizer.tokenize(text);

        // then
        assertEquals(text, String.join("", tokens));
        assertTrue(tokens.contains("\u0995\u09cd\u09b7"));
        assertTrue(tokens.contains("\u09aa\u09c1\u09a4\u09c1"));
    }

}
