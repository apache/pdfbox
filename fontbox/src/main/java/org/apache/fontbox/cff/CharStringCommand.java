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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a CharStringCommand.
 * 
 * @author Villu Ruusmann
 */
public class CharStringCommand
{

    private final Key commandKey;

    /**
     * Constructor with one value.
     * 
     * @param b0 value
     */
    public CharStringCommand(int b0)
    {
        commandKey = new Key(b0);
    }

    /**
     * Constructor with two values.
     * 
     * @param b0 value1
     * @param b1 value2
     */
    public CharStringCommand(int b0, int b1)
    {
        commandKey = new Key(b0, b1);
    }

    /**
     * Constructor with an array as values.
     * 
     * @param values array of values
     */
    public CharStringCommand(int[] values)
    {
        commandKey = new Key(values);
    }

    /**
     * The key of the CharStringCommand.
     * @return the key
     */
    public Key getKey()
    {
        return commandKey;
    }

    public Type1KeyWord getType1KeyWord()
    {
        return Type1KeyWord.valueOfKey(commandKey);
    }

    public Type2KeyWord getType2KeyWord()
    {
        return Type2KeyWord.valueOfKey(commandKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String str = null;
        Type2KeyWord type2KeyWord = getType2KeyWord();
        if (type2KeyWord != null)
        {
            str = type2KeyWord.toString();
        }
        else
        {
            Type1KeyWord type1KeyWord = getType1KeyWord();
            if (type1KeyWord != null)
            {
                str = type1KeyWord.toString();
            }
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

        private final int[] keyValues;

        /**
         * Constructor with one value.
         * 
         * @param b0 value
         */
        public Key(int b0)
        {
            keyValues = new int[] { b0 };
        }

        /**
         * Constructor with two values.
         * 
         * @param b0 value1
         * @param b1 value2
         */
        public Key(int b0, int b1)
        {
            keyValues = new int[] { b0, b1 };
        }

        /**
         * Constructor with an array as values.
         * 
         * @param values array of values
         */
        public Key(int[] values)
        {
            keyValues = values;
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
     * Enum of all valid type1 key words
     */
    public enum Type1KeyWord
    {
        HSTEM(new Key(1)), VSTEM(new Key(3)), VMOVETO(new Key(4)), RLINETO(new Key(5)), //
        HLINETO(new Key(6)), VLINETO(new Key(7)), RRCURVETO(new Key(8)), CLOSEPATH(new Key(9)), //
        CALLSUBR(new Key(10)), RET(new Key(11)), ESCAPE(new Key(12)), DOTSECTION(new Key(12, 0)), //
        VSTEM3(new Key(12, 1)), HSTEM3(new Key(12, 2)), SEAC(new Key(12, 6)), SBW(new Key(12, 7)), //
        DIV(new Key(12, 12)), CALLOTHERSUBR(new Key(12, 16)), POP(new Key(12, 17)), //
        SETCURRENTPOINT(new Key(12, 33)), HSBW(new Key(13)), ENDCHAR(new Key(14)), //
        RMOVETO(new Key(21)), HMOVETO(new Key(22)), VHCURVETO(new Key(30)), HVCURVETO(new Key(31));

        final Key key;

        private Type1KeyWord(Key key)
        {
            this.key = key;
        }

        private static final Map<Key, Type1KeyWord> BY_KEY = new HashMap<>();
            
        static
        {
            for (Type1KeyWord e : values())
            {
                 BY_KEY.put(e.key, e);
            }
        }
        
        public static Type1KeyWord valueOfKey(Key key)
        {
            return BY_KEY.get(key);
        }

    }

    /**
     * Enum of all valid type2 key words
     */
    public enum Type2KeyWord
    {
        HSTEM(new Key(1)), VSTEM(new Key(3)), VMOVETO(new Key(4)), RLINETO(new Key(5)), //
        HLINETO(new Key(6)), VLINETO(new Key(7)), RRCURVETO(new Key(8)), CALLSUBR(new Key(10)), //
        RET(new Key(11)), ESCAPE(new Key(12)), AND(new Key(12, 3)), OR(new Key(12, 4)), //
        NOT(new Key(12, 5)), ABS(new Key(12, 9)), ADD(new Key(12, 10)), SUB(new Key(12, 11)), //
        DIV(new Key(12, 12)), NEG(new Key(12, 14)), EQ(new Key(12, 15)), DROP(new Key(12, 18)), //
        PUT(new Key(12, 20)), GET(new Key(12, 21)), IFELSE(new Key(12, 22)), //
        RANDOM(new Key(12, 23)), MUL(new Key(12, 24)), SQRT(new Key(12, 26)), DUP(new Key(12, 27)), //
        EXCH(new Key(12, 28)), INDEX(new Key(12, 29)), ROLL(new Key(12, 30)), //
        HFLEX(new Key(12, 34)), FLEX(new Key(12, 35)), HFLEX1(new Key(12, 36)), //
        FLEX1(new Key(12, 37)), ENDCHAR(new Key(14)), HSTEMHM(new Key(18)), HINTMASK(new Key(19)), //
        CNTRMASK(new Key(20)), RMOVETO(new Key(21)), HMOVETO(new Key(22)), VSTEMHM(new Key(23)), //
        RCURVELINE(new Key(24)), RLINECURVE(new Key(25)), VVCURVETO(new Key(26)), //
        HHCURVETO(new Key(27)), SHORTINT(new Key(28)), CALLGSUBR(new Key(29)), //
        VHCURVETO(new Key(30)), HVCURVETO(new Key(31));

        final Key key;

        private Type2KeyWord(Key key)
        {
            this.key = key;
        }

        private static final Map<Key, Type2KeyWord> BY_KEY = new HashMap<>();
            
        static
        {
            for (Type2KeyWord e : values())
            {
                 BY_KEY.put(e.key, e);
            }
        }
        
        public static Type2KeyWord valueOfKey(Key key)
        {
            return BY_KEY.get(key);
        }
    }
}
