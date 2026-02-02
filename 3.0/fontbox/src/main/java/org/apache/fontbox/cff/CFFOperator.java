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
package org.apache.fontbox.cff;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a CFF operator.
 * @author Villu Ruusmann
 */
public final class CFFOperator
{

    private CFFOperator()
    {
    }

    private static void register(int b0, String name)
    {
        register(b0, 0, name);
    }

    private static void register(int b0, int b1, String name)
    {
        keyMap.put(calculateKey(b0, b1), name);
    }

    /**
     * Returns the operator name corresponding to the given one byte representation.
     * 
     * @param b0 the first byte of the operator
     * @return the corresponding operator name
     */
    public static String getOperator(int b0)
    {
        return getOperator(b0, 0);
    }

    /**
     * Returns the operator name corresponding to the given two byte representation.
     * 
     * @param b0 the first byte of the operator
     * @param b1 the second byte of the operator
     * @return the corresponding operator name
     */
    public static String getOperator(int b0, int b1)
    {
        return keyMap.get(calculateKey(b0, b1));
    }

    private static int calculateKey(int b0, int b1)
    {
        return (b1 << 8) + b0;
    }

    private static final Map<Integer, String> keyMap = new LinkedHashMap<>(52);

    static
    {
        // Top DICT
        register(0, "version");
        register(1, "Notice");
        register(12, 0, "Copyright");
        register(2, "FullName");
        register(3, "FamilyName");
        register(4, "Weight");
        register(12, 1, "isFixedPitch");
        register(12, 2, "ItalicAngle");
        register(12, 3, "UnderlinePosition");
        register(12, 4, "UnderlineThickness");
        register(12, 5, "PaintType");
        register(12, 6, "CharstringType");
        register(12, 7, "FontMatrix");
        register(13, "UniqueID");
        register(5, "FontBBox");
        register(12, 8, "StrokeWidth");
        register(14, "XUID");
        register(15, "charset");
        register(16, "Encoding");
        register(17, "CharStrings");
        register(18, "Private");
        register(12, 20, "SyntheticBase");
        register(12, 21, "PostScript");
        register(12, 22, "BaseFontName");
        register(12, 23, "BaseFontBlend");
        register(12, 30, "ROS");
        register(12, 31, "CIDFontVersion");
        register(12, 32, "CIDFontRevision");
        register(12, 33, "CIDFontType");
        register(12, 34, "CIDCount");
        register(12, 35, "UIDBase");
        register(12, 36, "FDArray");
        register(12, 37, "FDSelect");
        register(12, 38, "FontName");

        // Private DICT
        register(6, "BlueValues");
        register(7, "OtherBlues");
        register(8, "FamilyBlues");
        register(9, "FamilyOtherBlues");
        register(12, 9, "BlueScale");
        register(12, 10, "BlueShift");
        register(12, 11, "BlueFuzz");
        register(10, "StdHW");
        register(11, "StdVW");
        register(12, 12, "StemSnapH");
        register(12, 13, "StemSnapV");
        register(12, 14, "ForceBold");
        register(12, 15, "LanguageGroup");
        register(12, 16, "ExpansionFactor");
        register(12, 17, "initialRandomSeed");
        register(19, "Subrs");
        register(20, "defaultWidthX");
        register(21, "nominalWidthX");
    }
}
