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

package org.apache.pdfbox.pdmodel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Stores sublists of glyphs and positions in a list
 *
 * @author Volker Kunert
 */
public class GlyphsAndPositions
{

    private final ArrayList<Object> list = new ArrayList<>();

    /**
     * Sublist to store adjacent glyphs
     */
    public static class GlyphSubList extends ArrayList<Integer>
    {

        /**
         * Creates an int array containing the elements of the list
         *
         * @return int array
         */
        public int[] toIntArray()
        {
            int[] intArray = new int[size()];
            for (int i = 0; i < intArray.length; i++)
            {
                intArray[i] = get(i);
            }
            return intArray;
        }
    }

    /**
     * Adds a glyph
     *
     * @param glyph to be added
     */
    public void add(Integer glyph)
    {
        Object last = !list.isEmpty() ? list.get(list.size() - 1) : null;
        GlyphSubList glyphSubList;
        if (!(last instanceof GlyphSubList))
        {
            glyphSubList = new GlyphSubList();
            list.add(glyphSubList);
        }
        else
        {
            glyphSubList = (GlyphSubList) last;
        }
        glyphSubList.add(glyph);
    }

    /**
     * Add a position
     *
     * @param position to be added
     */
    public void add(Float position)
    {
        list.add(position);
    }

    /**
     * Checks if the list is empty
     *
     * @return true if it is empty
     */
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    /**
     * Clears the list
     */
    public void clear()
    {
        list.clear();
    }

    /**
     * Converts GlyphsAndPositions to an array of objects (GlyphSubList and Float)
     *
     * @return the array
     */
    public Object[] toArray()
    {
        return Collections.unmodifiableList(list).toArray();
    }
}
