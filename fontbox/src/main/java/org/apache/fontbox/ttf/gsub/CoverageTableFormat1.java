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

import java.util.Arrays;

class CoverageTableFormat1 extends CoverageTable
{
    int[] glyphArray;

    @Override
    int getCoverageIndex(int gid)
    {
        return Arrays.binarySearch(glyphArray, gid);
    }

    @Override
    int getGlyphId(int index)
    {
        return glyphArray[index];
    }

    @Override
    int getSize()
    {
        return glyphArray.length;
    }

    @Override
    public String toString()
    {
        return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]",
                coverageFormat, Arrays.toString(glyphArray));
    }


}