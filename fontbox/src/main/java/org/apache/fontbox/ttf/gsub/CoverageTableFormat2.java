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

import java.util.ArrayList;
import java.util.List;

class CoverageTableFormat2 extends CoverageTableFormat1
{
    private final RangeRecord[] rangeRecords;

    CoverageTableFormat2(int coverageFormat, RangeRecord[] rangeRecords)
    {
        super(coverageFormat, getRangeRecordsAsArray(rangeRecords));
        this.rangeRecords = rangeRecords;
    }

    RangeRecord[] getRangeRecords()
    {
        return rangeRecords;
    }

    private static int[] getRangeRecordsAsArray(RangeRecord[] rangeRecords)
    {

        List<Integer> glyphIds = new ArrayList<>();

        for (int i = 0; i < rangeRecords.length; i++)
        {
            for (int glyphId = rangeRecords[i].startGlyphID; glyphId <= rangeRecords[i].endGlyphID; glyphId++)
            {
                glyphIds.add(glyphId);
            }
        }

        int[] glyphArray = new int[glyphIds.size()];

        for (int i = 0; i < glyphArray.length; i++)
        {
            glyphArray[i] = glyphIds.get(i);
        }

        return glyphArray;
    }

    @Override
    public String toString()
    {
        return String.format("CoverageTableFormat2[coverageFormat=%d]", getCoverageFormat());
    }
}
