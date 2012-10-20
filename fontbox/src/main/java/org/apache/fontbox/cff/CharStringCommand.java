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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a CharStringCommand.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CharStringCommand
{

    private Key commandKey = null;

    /**
     * Constructor with one value.
     * 
     * @param b0 value
     */
    public CharStringCommand(int b0)
    {
        setKey(new Key(b0));
    }

    /**
     * Constructor with two values.
     * 
     * @param b0 value1
     * @param b1 value2
     */
    public CharStringCommand(int b0, int b1)
    {
        setKey(new Key(b0, b1));
    }

    /**
     * Constructor with an array as values.
     * 
     * @param values array of values
     */
    public CharStringCommand(int[] values)
    {
        setKey(new Key(values));
    }

    /**
     * The key of the CharStringCommand.
     * @return the key
     */
    public Key getKey()
    {
        return commandKey;
    }

    private void setKey(Key key)
    {
        commandKey = key;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getKey().toString();
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
        if (object instanceof CharStringCommand)
        {
            CharStringCommand that = (CharStringCommand) object;
            return getKey().equals(that.getKey());
        }
        return false;
    }

    /**
     * A static class to hold one or more int values as key. 
     */
    public static class Key
    {

        private int[] keyValues = null;

        /**
         * Constructor with one value.
         * 
         * @param b0 value
         */
        public Key(int b0)
        {
            setValue(new int[] { b0 });
        }

        /**
         * Constructor with two values.
         * 
         * @param b0 value1
         * @param b1 value2
         */
        public Key(int b0, int b1)
        {
            setValue(new int[] { b0, b1 });
        }

        /**
         * Constructor with an array as values.
         * 
         * @param values array of values
         */
        public Key(int[] values)
        {
            setValue(values);
        }

        /**
         * Array the with the values.
         * 
         * @return array with the values
         */
        public int[] getValue()
        {
            return keyValues;
        }

        private void setValue(int[] value)
        {
            keyValues = value;
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
            if (keyValues[0] == 12)
            {
                if (keyValues.length > 1)
                {
                    return keyValues[0] ^ keyValues[1];
                }
            }
            return keyValues[0];
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object object)
        {
            if (object instanceof Key)
            {
                Key that = (Key) object;
                if (keyValues[0] == 12 && that.keyValues[0] == 12)
                {
                    if (keyValues.length > 1 && that.keyValues.length > 1)
                    {
                        return keyValues[1] == that.keyValues[1];
                    }

                    return keyValues.length == that.keyValues.length;
                }
                return keyValues[0] == that.keyValues[0];
            }
            return false;
        }
    }

    /**
     * A map with the Type1 vocabulary.
     */
    public static final Map<Key, String> TYPE1_VOCABULARY;

    static
    {
        Map<Key, String> map = new LinkedHashMap<Key, String>();
        map.put(new Key(1), "hstem");
        map.put(new Key(3), "vstem");
        map.put(new Key(4), "vmoveto");
        map.put(new Key(5), "rlineto");
        map.put(new Key(6), "hlineto");
        map.put(new Key(7), "vlineto");
        map.put(new Key(8), "rrcurveto");
        map.put(new Key(9), "closepath");
        map.put(new Key(10), "callsubr");
        map.put(new Key(11), "return");
        map.put(new Key(12), "escape");
        map.put(new Key(12, 0), "dotsection");
        map.put(new Key(12, 1), "vstem3");
        map.put(new Key(12, 2), "hstem3");
        map.put(new Key(12, 6), "seac");
        map.put(new Key(12, 7), "sbw");
        map.put(new Key(12, 12), "div");
        map.put(new Key(12, 16), "callothersubr");
        map.put(new Key(12, 17), "pop");
        map.put(new Key(12, 33), "setcurrentpoint");
        map.put(new Key(13), "hsbw");
        map.put(new Key(14), "endchar");
        map.put(new Key(21), "rmoveto");
        map.put(new Key(22), "hmoveto");
        map.put(new Key(30), "vhcurveto");
        map.put(new Key(31), "hvcurveto");

        TYPE1_VOCABULARY = Collections.unmodifiableMap(map);
    }

    /**
     * A map with the Type2 vocabulary.
     */
    public static final Map<Key, String> TYPE2_VOCABULARY;

    static
    {
        Map<Key, String> map = new LinkedHashMap<Key, String>();
        map.put(new Key(1), "hstem");
        map.put(new Key(3), "vstem");
        map.put(new Key(4), "vmoveto");
        map.put(new Key(5), "rlineto");
        map.put(new Key(6), "hlineto");
        map.put(new Key(7), "vlineto");
        map.put(new Key(8), "rrcurveto");
        map.put(new Key(10), "callsubr");
        map.put(new Key(11), "return");
        map.put(new Key(12), "escape");
        map.put(new Key(12, 3), "and");
        map.put(new Key(12, 4), "or");
        map.put(new Key(12, 5), "not");
        map.put(new Key(12, 9), "abs");
        map.put(new Key(12, 10), "add");
        map.put(new Key(12, 11), "sub");
        map.put(new Key(12, 12), "div");
        map.put(new Key(12, 14), "neg");
        map.put(new Key(12, 15), "eq");
        map.put(new Key(12, 18), "drop");
        map.put(new Key(12, 20), "put");
        map.put(new Key(12, 21), "get");
        map.put(new Key(12, 22), "ifelse");
        map.put(new Key(12, 23), "random");
        map.put(new Key(12, 24), "mul");
        map.put(new Key(12, 26), "sqrt");
        map.put(new Key(12, 27), "dup");
        map.put(new Key(12, 28), "exch");
        map.put(new Key(12, 29), "index");
        map.put(new Key(12, 30), "roll");
        map.put(new Key(12, 34), "hflex");
        map.put(new Key(12, 35), "flex");
        map.put(new Key(12, 36), "hflex1");
        map.put(new Key(12, 37), "flex1");
        map.put(new Key(14), "endchar");
        map.put(new Key(18), "hstemhm");
        map.put(new Key(19), "hintmask");
        map.put(new Key(20), "cntrmask");
        map.put(new Key(21), "rmoveto");
        map.put(new Key(22), "hmoveto");
        map.put(new Key(23), "vstemhm");
        map.put(new Key(24), "rcurveline");
        map.put(new Key(25), "rlinecurve");
        map.put(new Key(26), "vvcurveto");
        map.put(new Key(27), "hhcurveto");
        map.put(new Key(28), "shortint");
        map.put(new Key(29), "callgsubr");
        map.put(new Key(30), "vhcurveto");
        map.put(new Key(31), "hvcurveto");

        TYPE2_VOCABULARY = Collections.unmodifiableMap(map);
    }
}