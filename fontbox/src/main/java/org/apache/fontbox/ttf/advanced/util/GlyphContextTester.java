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

package org.apache.fontbox.ttf.advanced.util;

// CSOFF: LineLengthCheck

/**
 * <p>Interface for testing the originating (source) character context of a glyph sequence.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public interface GlyphContextTester {

    /**
     * Perform a test on a glyph sequence in a specific (originating) character context.
     * @param script governing script
     * @param language governing language
     * @param feature governing feature
     * @param gs glyph sequence to test
     * @param index index into glyph sequence to test
     * @param flags that apply to lookup in scope
     * @return true if test is satisfied
     */
    boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags);

}
