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
package org.apache.pdfbox.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

class StringUtilTest
{
    @Test
    void testSplitOnSpace_happyPath()
    {
        String[] result = StringUtil.splitOnSpace("a b c");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);
    }

    @Test
    void testSplitOnSpace_emptyString()
    {
        String[] result = StringUtil.splitOnSpace("");
        assertArrayEquals(new String[] {""}, result);
    }

    @Test
    void testSplitOnSpace_onlySpaces()
    {
        String[] result = StringUtil.splitOnSpace("   ");
        assertArrayEquals(new String[] {}, result);
    }

    @Test
    void testTokenizeOnSpace_happyPath()
    {
        String[] result = StringUtil.tokenizeOnSpace("a b c");
        assertArrayEquals(new String[] {"a", " ", "b", " ", "c"}, result);
    }

    @Test
    void testTokenizeOnSpace_emptyString()
    {
        String[] result = StringUtil.tokenizeOnSpace("");
        assertArrayEquals(new String[] {""}, result);
    }

    @Test
    void testTokenizeOnSpace_onlySpaces()
    {
        String[] result = StringUtil.tokenizeOnSpace("   ");
        assertArrayEquals(new String[] {" ", " ", " "}, result);
    }

    @Test
    void testTokenizeOnSpace_onlySpacesWithText()
    {
        String[] result = StringUtil.tokenizeOnSpace("  a  ");
        assertArrayEquals(new String[] {" ", " ", "a", " ", " "}, result);
    }
}