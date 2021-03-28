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
    private static void register(int key, String name)
    {
        keyMap.put(key, name);
    }

    /**
     * Returns the operator corresponding to the given name.
     * 
     * @param name the given name
     * @return the corresponding operator
     */
    public static String getOperator(int b0)
    {
        return keyMap.get(calculateKey(b0));
    }

    /**
     * Returns the operator corresponding to the given name.
     * 
     * @param name the given name
     * @return the corresponding operator
     */
    public static String getOperator(int b0, int b1)
    {
        return keyMap.get(calculateKey(b0, b1));
    }

    private static int calculateKey(int b0)
    {
        return calculateKey(b0, 0);
    }

    private static int calculateKey(int b0, int b1)
    {
        return (b1 << 8) + b0;
    }

    private static final Map<Integer, String> keyMap = new LinkedHashMap<>(52);

    static
    {
        // Top DICT
        register(calculateKey(0), "version");
        register(calculateKey(1), "Notice");
        register(calculateKey(12, 0), "Copyright");
        register(calculateKey(2), "FullName");
        register(calculateKey(3), "FamilyName");
        register(calculateKey(4), "Weight");
        register(calculateKey(12, 1), "isFixedPitch");
        register(calculateKey(12, 2), "ItalicAngle");
        register(calculateKey(12, 3), "UnderlinePosition");
        register(calculateKey(12, 4), "UnderlineThickness");
        register(calculateKey(12, 5), "PaintType");
        register(calculateKey(12, 6), "CharstringType");
        register(calculateKey(12, 7), "FontMatrix");
        register(calculateKey(13), "UniqueID");
        register(calculateKey(5), "FontBBox");
        register(calculateKey(12, 8), "StrokeWidth");
        register(calculateKey(14), "XUID");
        register(calculateKey(15), "charset");
        register(calculateKey(16), "Encoding");
        register(calculateKey(17), "CharStrings");
        register(calculateKey(18), "Private");
        register(calculateKey(12, 20), "SyntheticBase");
        register(calculateKey(12, 21), "PostScript");
        register(calculateKey(12, 22), "BaseFontName");
        register(calculateKey(12, 23), "BaseFontBlend");
        register(calculateKey(12, 30), "ROS");
        register(calculateKey(12, 31), "CIDFontVersion");
        register(calculateKey(12, 32), "CIDFontRevision");
        register(calculateKey(12, 33), "CIDFontType");
        register(calculateKey(12, 34), "CIDCount");
        register(calculateKey(12, 35), "UIDBase");
        register(calculateKey(12, 36), "FDArray");
        register(calculateKey(12, 37), "FDSelect");
        register(calculateKey(12, 38), "FontName");

        // Private DICT
        register(calculateKey(6), "BlueValues");
        register(calculateKey(7), "OtherBlues");
        register(calculateKey(8), "FamilyBlues");
        register(calculateKey(9), "FamilyOtherBlues");
        register(calculateKey(12, 9), "BlueScale");
        register(calculateKey(12, 10), "BlueShift");
        register(calculateKey(12, 11), "BlueFuzz");
        register(calculateKey(10), "StdHW");
        register(calculateKey(11), "StdVW");
        register(calculateKey(12, 12), "StemSnapH");
        register(calculateKey(12, 13), "StemSnapV");
        register(calculateKey(12, 14), "ForceBold");
        register(calculateKey(12, 15), "LanguageGroup");
        register(calculateKey(12, 16), "ExpansionFactor");
        register(calculateKey(12, 17), "initialRandomSeed");
        register(calculateKey(19), "Subrs");
        register(calculateKey(20), "defaultWidthX");
        register(calculateKey(21), "nominalWidthX");
    }
}
