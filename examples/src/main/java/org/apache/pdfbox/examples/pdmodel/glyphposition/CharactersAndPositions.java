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
 *
 * Volker Kunert 2026
 */

package org.apache.pdfbox.examples.pdmodel.glyphposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores sublists of characters and positions in a list
 * Helper class for GlyphPositioner
 */
public class CharactersAndPositions {
    private final ArrayList<Object> list = new ArrayList<>();

    /**
     * Adds a character
     *
     * @param ch to be added
     */
    public void add(char ch) {
        Object last = !list.isEmpty() ? list.get(list.size() - 1) : null;
        GlyphSubList glyphSubList;
        if (!(last instanceof GlyphSubList)) {
            glyphSubList = new GlyphSubList();
            list.add(glyphSubList);
        } else {
            glyphSubList = (GlyphSubList) last;
        }
        glyphSubList.add(ch);
    }

    /**
     * Add a position
     *
     * @param position to be added
     */
    public void add(Float position) {
        list.add(position);
    }

    /**
     * Checks if the list is empty
     *
     * @return true if it is empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Clears the list
     */
    public void clear() {
        list.clear();
    }

    /**
     * Converts CharactersAndPositions to a list of objects (GlyphSubList and Float)
     *
     * @return the list
     */
    public List<Object> toList() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Converts CharactersAndPositions to an array of objects (GlyphSubList and Float)
     *
     * @return the array
     */
    public Object[] toArray() {
        return Collections.unmodifiableList(list).toArray();
    }

    /**
     * Sublist to store adjacent glyphs
     */
    public static class GlyphSubList extends ArrayList<Character> {
    }
}
