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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a CFF operator.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class CFFOperator
{

    private Key operatorKey = null;
    private String operatorName = null;

    private CFFOperator(Key key, String name)
    {
        setKey(key);
        setName(name);
    }

    /**
     * The key of the operator.
     * @return the key
     */
    public Key getKey()
    {
        return operatorKey;
    }

    private void setKey(Key key)
    {
        operatorKey = key;
    }

    /**
     * The name of the operator.
     * @return the name
     */
    public String getName()
    {
        return operatorName;
    }

    private void setName(String name)
    {
        operatorName = name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getKey().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object object)
    {
        if (object instanceof CFFOperator)
        {
            CFFOperator that = (CFFOperator) object;
            return getKey().equals(that.getKey());
        }
        return false;
    }

    private static void register(Key key, String name)
    {
        CFFOperator operator = new CFFOperator(key, name);
        keyMap.put(key, operator);
        nameMap.put(name, operator);
    }

    /**
     * Returns the operator corresponding to the given key.
     * @param key the given key
     * @return the corresponding operator
     */
    public static CFFOperator getOperator(Key key)
    {
        return keyMap.get(key);
    }

    /**
     * Returns the operator corresponding to the given name.
     * @param key the given name
     * @return the corresponding operator
     */
    public static CFFOperator getOperator(String name)
    {
        return nameMap.get(name);
    }

    /**
     * This class is a holder for a key value. It consists of one or two bytes.  
     * @author Villu Ruusmann
     */
    public static class Key
    {
        private int[] value = null;

        /**
         * Constructor.
         * @param b0 the one byte value
         */
        public Key(int b0)
        {
            this(new int[] { b0 });
        }

        /**
         * Constructor.
         * @param b0 the first byte of a two byte value
         * @param b1 the second byte of a two byte value
         */
        public Key(int b0, int b1)
        {
            this(new int[] { b0, b1 });
        }

        private Key(int[] value)
        {
            setValue(value);
        }

        /**
         * Returns the value of the key.
         * @return the value
         */
        public int[] getValue()
        {
            return value;
        }

        private void setValue(int[] value)
        {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return Arrays.toString(getValue());
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            return Arrays.hashCode(getValue());
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object object)
        {
            if (object instanceof Key)
            {
                Key that = (Key) object;
                return Arrays.equals(getValue(), that.getValue());
            }
            return false;
        }
    }

    private static Map<CFFOperator.Key, CFFOperator> keyMap = new LinkedHashMap<CFFOperator.Key, CFFOperator>();
    private static Map<String, CFFOperator> nameMap = new LinkedHashMap<String, CFFOperator>();

    static
    {
        // Top DICT
        register(new Key(0), "version");
        register(new Key(1), "Notice");
        register(new Key(12, 0), "Copyright");
        register(new Key(2), "FullName");
        register(new Key(3), "FamilyName");
        register(new Key(4), "Weight");
        register(new Key(12, 1), "isFixedPitch");
        register(new Key(12, 2), "ItalicAngle");
        register(new Key(12, 3), "UnderlinePosition");
        register(new Key(12, 4), "UnderlineThickness");
        register(new Key(12, 5), "PaintType");
        register(new Key(12, 6), "CharstringType");
        register(new Key(12, 7), "FontMatrix");
        register(new Key(13), "UniqueID");
        register(new Key(5), "FontBBox");
        register(new Key(12, 8), "StrokeWidth");
        register(new Key(14), "XUID");
        register(new Key(15), "charset");
        register(new Key(16), "Encoding");
        register(new Key(17), "CharStrings");
        register(new Key(18), "Private");
        register(new Key(12, 20), "SyntheticBase");
        register(new Key(12, 21), "PostScript");
        register(new Key(12, 22), "BaseFontName");
        register(new Key(12, 23), "BaseFontBlend");
        register(new Key(12, 30), "ROS");
        register(new Key(12, 31), "CIDFontVersion");
        register(new Key(12, 32), "CIDFontRevision");
        register(new Key(12, 33), "CIDFontType");
        register(new Key(12, 34), "CIDCount");
        register(new Key(12, 35), "UIDBase");
        register(new Key(12, 36), "FDArray");
        register(new Key(12, 37), "FDSelect");
        register(new Key(12, 38), "FontName");

        // Private DICT
        register(new Key(6), "BlueValues");
        register(new Key(7), "OtherBlues");
        register(new Key(8), "FamilyBlues");
        register(new Key(9), "FamilyOtherBlues");
        register(new Key(12, 9), "BlueScale");
        register(new Key(12, 10), "BlueShift");
        register(new Key(12, 11), "BlueFuzz");
        register(new Key(10), "StdHW");
        register(new Key(11), "StdVW");
        register(new Key(12, 12), "StemSnapH");
        register(new Key(12, 13), "StemSnapV");
        register(new Key(12, 14), "ForceBold");
        register(new Key(12, 15), "LanguageGroup");
        register(new Key(12, 16), "ExpansionFactor");
        register(new Key(12, 17), "initialRandomSeed");
        register(new Key(19), "Subrs");
        register(new Key(20), "defaultWidthX");
        register(new Key(21), "nominalWidthX");
    }
}