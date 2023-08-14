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

package org.apache.fontbox.ttf.table.common;

import java.util.Arrays;

/**
 * This class models the
 * <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#coverage-format-1">Coverage format 1</a>
 * in the Open Type layout common tables.
 * 
 * @author Palash Ray
 *
 */
public class CoverageTableFormat1 extends CoverageTable
{

    private final int[] glyphArray;

    public CoverageTableFormat1(int coverageFormat, int[] glyphArray)
    {
        super(coverageFormat);
        this.glyphArray = glyphArray;
    }

    @Override
    public int getCoverageIndex(int gid)
    {
        return Arrays.binarySearch(glyphArray, gid);
    }

    @Override
    public int getGlyphId(int index)
    {
        return glyphArray[index];
    }

    @Override
    public int getSize()
    {
        return glyphArray.length;
    }

    public int[] getGlyphArray()
    {
        return glyphArray;
    }

    @Override
    public String toString()
    {
        return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]",
                getCoverageFormat(), Arrays.toString(glyphArray));
    }


}