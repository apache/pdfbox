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
    @Override
    public String toString()
    {
        String str = null;
        if (TYPE2_VOCABULARY.containsKey(getKey()))
        {
            str = TYPE2_VOCABULARY.get(getKey()).name();
        }
        else if (TYPE1_VOCABULARY.containsKey(getKey()))
        {
            str = TYPE1_VOCABULARY.get(getKey()).name();
        }
        if (str == null)
        {
            str = getKey().toString();
        }
        return str + '|';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return getKey().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        @Override
        public String toString()
        {
            return Arrays.toString(getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            if (keyValues[0] == 12 && keyValues.length > 1)
            {
                return keyValues[0] ^ keyValues[1];
            }
            return keyValues[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
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
    public static final Map<Key, Type1KeyWord> TYPE1_VOCABULARY;

    /**
     * Enum of all valid type1 key words
     */
    public enum Type1KeyWord
    {
        HSTEM, VSTEM, VMOVETO, RLINETO, HLINETO, VLINETO, RRCURVETO, CLOSEPATH, CALLSUBR, RET, ESCAPE, DOTSECTION, VSTEM3, HSTEM3, SEAC, SBW, DIV, CALLOTHERSUBR, POP, SETCURRENTPOINT, HSBW, ENDCHAR, RMOVETO, HMOVETO, VHCURVETO, HVCURVETO
    }

    static
    {
        Map<Key, Type1KeyWord> map = new LinkedHashMap<>(26);
        map.put(new Key(1), Type1KeyWord.HSTEM);
        map.put(new Key(3), Type1KeyWord.VSTEM);
        map.put(new Key(4), Type1KeyWord.VMOVETO);
        map.put(new Key(5), Type1KeyWord.RLINETO);
        map.put(new Key(6), Type1KeyWord.HLINETO);
        map.put(new Key(7), Type1KeyWord.VLINETO);
        map.put(new Key(8), Type1KeyWord.RRCURVETO);
        map.put(new Key(9), Type1KeyWord.CLOSEPATH);
        map.put(new Key(10), Type1KeyWord.CALLSUBR);
        map.put(new Key(11), Type1KeyWord.RET);
        map.put(new Key(12), Type1KeyWord.ESCAPE);
        map.put(new Key(12, 0), Type1KeyWord.DOTSECTION);
        map.put(new Key(12, 1), Type1KeyWord.VSTEM3);
        map.put(new Key(12, 2), Type1KeyWord.HSTEM3);
        map.put(new Key(12, 6), Type1KeyWord.SEAC);
        map.put(new Key(12, 7), Type1KeyWord.SBW);
        map.put(new Key(12, 12), Type1KeyWord.DIV);
        map.put(new Key(12, 16), Type1KeyWord.CALLOTHERSUBR);
        map.put(new Key(12, 17), Type1KeyWord.POP);
        map.put(new Key(12, 33), Type1KeyWord.SETCURRENTPOINT);
        map.put(new Key(13), Type1KeyWord.HSBW);
        map.put(new Key(14), Type1KeyWord.ENDCHAR);
        map.put(new Key(21), Type1KeyWord.RMOVETO);
        map.put(new Key(22), Type1KeyWord.HMOVETO);
        map.put(new Key(30), Type1KeyWord.VHCURVETO);
        map.put(new Key(31), Type1KeyWord.HVCURVETO);

        TYPE1_VOCABULARY = Collections.unmodifiableMap(map);
    }

    /**
     * A map with the Type2 vocabulary.
     */
    public static final Map<Key, Type2KeyWord> TYPE2_VOCABULARY;

    /**
     * Enum of all valid type2 key words
     */
    public enum Type2KeyWord
    {
        HSTEM, VSTEM, VMOVETO, RLINETO, HLINETO, VLINETO, RRCURVETO, CALLSUBR, RET, ESCAPE, AND, OR, NOT, ABS, ADD, SUB, DIV, NEG, EQ, DROP, PUT, GET, IFELSE, RANDOM, MUL, SQRT, DUP, EXCH, INDEX, ROLL, HFLEX, FLEX, HFLEX1, FLEX1, ENDCHAR, HSTEMHM, HINTMASK, CNTRMASK, RMOVETO, HMOVETO, VSTEMHM, RCURVELINE, RLINECURVE, VVCURVETO, HHCURVETO, SHORTINT, CALLGSUBR, VHCURVETO, HVCURVETO
    }

    static
    {
        Map<Key, Type2KeyWord> map = new LinkedHashMap<>(48);
        map.put(new Key(1), Type2KeyWord.HSTEM);
        map.put(new Key(3), Type2KeyWord.VSTEM);
        map.put(new Key(4), Type2KeyWord.VMOVETO);
        map.put(new Key(5), Type2KeyWord.RLINETO);
        map.put(new Key(6), Type2KeyWord.HLINETO);
        map.put(new Key(7), Type2KeyWord.VLINETO);
        map.put(new Key(8), Type2KeyWord.RRCURVETO);
        map.put(new Key(10), Type2KeyWord.CALLSUBR);
        map.put(new Key(11), Type2KeyWord.RET);
        map.put(new Key(12), Type2KeyWord.ESCAPE);
        map.put(new Key(12, 3), Type2KeyWord.AND);
        map.put(new Key(12, 4), Type2KeyWord.OR);
        map.put(new Key(12, 5), Type2KeyWord.NOT);
        map.put(new Key(12, 9), Type2KeyWord.ABS);
        map.put(new Key(12, 10), Type2KeyWord.ADD);
        map.put(new Key(12, 11), Type2KeyWord.SUB);
        map.put(new Key(12, 12), Type2KeyWord.DIV);
        map.put(new Key(12, 14), Type2KeyWord.NEG);
        map.put(new Key(12, 15), Type2KeyWord.EQ);
        map.put(new Key(12, 18), Type2KeyWord.DROP);
        map.put(new Key(12, 20), Type2KeyWord.PUT);
        map.put(new Key(12, 21), Type2KeyWord.GET);
        map.put(new Key(12, 22), Type2KeyWord.IFELSE);
        map.put(new Key(12, 23), Type2KeyWord.RANDOM);
        map.put(new Key(12, 24), Type2KeyWord.MUL);
        map.put(new Key(12, 26), Type2KeyWord.SQRT);
        map.put(new Key(12, 27), Type2KeyWord.DUP);
        map.put(new Key(12, 28), Type2KeyWord.EXCH);
        map.put(new Key(12, 29), Type2KeyWord.INDEX);
        map.put(new Key(12, 30), Type2KeyWord.ROLL);
        map.put(new Key(12, 34), Type2KeyWord.HFLEX);
        map.put(new Key(12, 35), Type2KeyWord.FLEX);
        map.put(new Key(12, 36), Type2KeyWord.HFLEX1);
        map.put(new Key(12, 37), Type2KeyWord.FLEX1);
        map.put(new Key(14), Type2KeyWord.ENDCHAR);
        map.put(new Key(18), Type2KeyWord.HSTEMHM);
        map.put(new Key(19), Type2KeyWord.HINTMASK);
        map.put(new Key(20), Type2KeyWord.CNTRMASK);
        map.put(new Key(21), Type2KeyWord.RMOVETO);
        map.put(new Key(22), Type2KeyWord.HMOVETO);
        map.put(new Key(23), Type2KeyWord.VSTEMHM);
        map.put(new Key(24), Type2KeyWord.RCURVELINE);
        map.put(new Key(25), Type2KeyWord.RLINECURVE);
        map.put(new Key(26), Type2KeyWord.VVCURVETO);
        map.put(new Key(27), Type2KeyWord.HHCURVETO);
        map.put(new Key(28), Type2KeyWord.SHORTINT);
        map.put(new Key(29), Type2KeyWord.CALLGSUBR);
        map.put(new Key(30), Type2KeyWord.VHCURVETO);
        map.put(new Key(31), Type2KeyWord.HVCURVETO);

        TYPE2_VOCABULARY = Collections.unmodifiableMap(map);
    }
}
