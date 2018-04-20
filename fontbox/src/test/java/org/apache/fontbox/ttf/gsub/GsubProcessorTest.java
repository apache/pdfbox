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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class GsubProcessorTest
{

    @Test
    public void testTokenize_1()
    {
        // given
        Set<List<Integer>> matchers = new HashSet<>(Arrays.asList(Arrays.asList(84, 93),
                Arrays.asList(102, 82), Arrays.asList(104, 87)));
        GsubProcessor testClass = new GsubProcessor(matchers);
        List<Integer> glyphIds = Arrays.asList(84, 112, 93, 104, 82, 61, 96, 102, 93, 104, 87, 110);

        // when
        List<List<Integer>> tokens = testClass.tokenize(glyphIds);

        // then
        assertEquals(Arrays.asList(Arrays.asList(84, 112, 93, 104, 82, 61, 96, 102, 93),
                Arrays.asList(104, 87), Arrays.asList(110)), tokens);
    }

    @Test
    public void testTokenize_2()
    {

        // given
        Set<List<Integer>> matchers = new HashSet<>(
                Arrays.asList(Arrays.asList(67, 112, 96), Arrays.asList(74, 112, 76)));
        GsubProcessor testClass = new GsubProcessor(matchers);
        List<Integer> glyphIds = Arrays.asList(67, 112, 96, 103, 93, 108, 93);

        // when
        List<List<Integer>> tokens = testClass.tokenize(glyphIds);

        // then
        assertEquals(Arrays.asList(Arrays.asList(67, 112, 96), Arrays.asList(103, 93, 108, 93)),
                tokens);
    }

    @Test
    public void testTokenize_3()
    {

        // given
        Set<List<Integer>> matchers = new HashSet<>(
                Arrays.asList(Arrays.asList(67, 112, 96), Arrays.asList(74, 112, 76)));
        GsubProcessor testClass = new GsubProcessor(matchers);
        List<Integer> glyphIds = Arrays.asList(94, 67, 112, 96, 112, 91, 103);

        // when
        List<List<Integer>> tokens = testClass.tokenize(glyphIds);

        // then
        assertEquals(Arrays.asList(Arrays.asList(94), Arrays.asList(67, 112, 96),
                Arrays.asList(112, 91, 103)), tokens);
    }

    @Test
    public void testTokenize_4()
    {

        // given
        Set<List<Integer>> matchers = new HashSet<>(
                Arrays.asList(Arrays.asList(67, 112), Arrays.asList(76, 112)));
        GsubProcessor testClass = new GsubProcessor(matchers);
        List<Integer> glyphIds = Arrays.asList(94, 167, 112, 91, 103);

        // when
        List<List<Integer>> tokens = testClass.tokenize(glyphIds);

        // then
        assertEquals(Arrays.asList(Arrays.asList(94, 167, 112, 91, 103)), tokens);
    }

}
