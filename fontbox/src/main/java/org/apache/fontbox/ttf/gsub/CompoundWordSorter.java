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

import java.util.Comparator;

/**
 * Sorts given words according to their lengths. Words with greater length are put above others. This can affect the way
 * Glyph-substitution happens. This is a bit of a hack, and has scope for improvement.
 * 
 */
public class CompoundWordSorter implements Comparator<String>
{

    @Override
    public int compare(String first, String second)
    {
        if (first.length() < second.length())
        {
            return 1;
        }
        else if (first.length() > second.length())
        {
            return -1;
        }
        else
        {
            return first.compareTo(second);
        }
    }

}
