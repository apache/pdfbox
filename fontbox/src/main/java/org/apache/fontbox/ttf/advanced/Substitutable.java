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

import java.util.List;

/**
 * <p>Optional interface which indicates that glyph substitution is supported and, if supported,
 * can perform substitution.</p>
 *
 * @author Glenn Adams
 */
public interface Substitutable {

    /**
     * Determines if font performs glyph substitution.
     * @return true if performs substitution.
     */
    boolean performsSubstitution();

    /**
     * Perform substitutions on characters to effect glyph substitution. If some substitution is performed, it
     * entails mapping from one or more input characters denoting textual character information to one or more
     * output character codes denoting glyphs in this font, where the output character codes may make use of
     * private character code values that have significance only for this font.
     * @param cs character sequence to map to output font encoding character sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param associations optional list to receive list of character associations
     * @param retainControls if true, then retain control characters and their glyph mappings, otherwise remove
     * @return output sequence (represented as a character sequence, where each character in the returned sequence
     * denotes "font characters", i.e., character codes that map directly (1-1) to their associated glyphs
     */
    CharSequence performSubstitution(CharSequence cs, String script, String language, List associations, boolean retainControls);

    /**
     * Reorder combining marks in character sequence so that they precede (within the sequence) the base
     * character to which they are applied. N.B. In the case of LTR segments, marks are not reordered by this,
     * method since when the segment is reversed by BIDI processing, marks are automatically reordered to precede
     * their base character.
     * @param cs character sequence within which combining marks to be reordered
     * @param gpa associated glyph position adjustments (also reordered)
     * @param script a script identifier
     * @param language a language identifier
     * @param associations optional list of associations to be reordered
     * @return output sequence containing reordered "font characters"
     */
    CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa, String script, String language, List associations);

}
