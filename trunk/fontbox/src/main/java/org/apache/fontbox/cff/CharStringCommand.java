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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a CharStringCommand.
 * 
 * @author Villu Ruusmann
 */
public class CharStringCommand
{
    private final Type1KeyWord type1KeyWord;
    private final Type2KeyWord type2KeyWord;

    private static final Map<Integer, CharStringCommand> CHAR_STRING_COMMANDS = createMap();

    public static final CharStringCommand COMMAND_CLOSEPATH = CharStringCommand
            .getInstance(Key.CLOSEPATH.hashValue);
    public static final CharStringCommand COMMAND_RLINETO = CharStringCommand
            .getInstance(Key.RLINETO.hashValue);
    public static final CharStringCommand COMMAND_HLINETO = CharStringCommand
            .getInstance(Key.HLINETO.hashValue);
    public static final CharStringCommand COMMAND_VLINETO = CharStringCommand
            .getInstance(Key.VLINETO.hashValue);
    public static final CharStringCommand COMMAND_RRCURVETO = CharStringCommand
            .getInstance(Key.RRCURVETO.hashValue);
    public static final CharStringCommand COMMAND_HSBW = CharStringCommand
            .getInstance(Key.HSBW.hashValue);
    public static final CharStringCommand COMMAND_CALLOTHERSUBR = CharStringCommand
            .getInstance(Key.CALLOTHERSUBR.hashValue);

    private static final int KEY_UNKNOWN = 99;
    private static final CharStringCommand COMMAND_UNKNOWN = new CharStringCommand(KEY_UNKNOWN, 0);

    private static Map<Integer, CharStringCommand> createMap()
    {
        Map<Integer, CharStringCommand> charStringCommandMap = new HashMap<>();
        // one byte commands
        charStringCommandMap.put(Key.HSTEM.hashValue, new CharStringCommand(Key.HSTEM));
        charStringCommandMap.put(Key.VSTEM.hashValue, new CharStringCommand(Key.VSTEM));
        charStringCommandMap.put(Key.VMOVETO.hashValue, new CharStringCommand(Key.VMOVETO));
        charStringCommandMap.put(Key.RLINETO.hashValue, new CharStringCommand(Key.RLINETO));
        charStringCommandMap.put(Key.HLINETO.hashValue, new CharStringCommand(Key.HLINETO));
        charStringCommandMap.put(Key.VLINETO.hashValue, new CharStringCommand(Key.VLINETO));
        charStringCommandMap.put(Key.RRCURVETO.hashValue, new CharStringCommand(Key.RRCURVETO));
        charStringCommandMap.put(Key.CLOSEPATH.hashValue, new CharStringCommand(Key.CLOSEPATH));
        charStringCommandMap.put(Key.CALLSUBR.hashValue, new CharStringCommand(Key.CALLSUBR));
        charStringCommandMap.put(Key.RET.hashValue, new CharStringCommand(Key.RET));
        charStringCommandMap.put(Key.ESCAPE.hashValue, new CharStringCommand(Key.ESCAPE));

        charStringCommandMap.put(Key.HSBW.hashValue, new CharStringCommand(Key.HSBW));
        charStringCommandMap.put(Key.ENDCHAR.hashValue, new CharStringCommand(Key.ENDCHAR));
        charStringCommandMap.put(Key.HSTEMHM.hashValue, new CharStringCommand(Key.HSTEMHM));
        charStringCommandMap.put(Key.HINTMASK.hashValue, new CharStringCommand(Key.HINTMASK));
        charStringCommandMap.put(Key.CNTRMASK.hashValue, new CharStringCommand(Key.CNTRMASK));
        charStringCommandMap.put(Key.RMOVETO.hashValue, new CharStringCommand(Key.RMOVETO));
        charStringCommandMap.put(Key.HMOVETO.hashValue, new CharStringCommand(Key.HMOVETO));
        charStringCommandMap.put(Key.VSTEMHM.hashValue, new CharStringCommand(Key.VSTEMHM));
        charStringCommandMap.put(Key.RCURVELINE.hashValue, new CharStringCommand(Key.RCURVELINE));
        charStringCommandMap.put(Key.RLINECURVE.hashValue, new CharStringCommand(Key.RLINECURVE));
        charStringCommandMap.put(Key.VVCURVETO.hashValue, new CharStringCommand(Key.VVCURVETO));
        charStringCommandMap.put(Key.HHCURVETO.hashValue, new CharStringCommand(Key.HHCURVETO));
        charStringCommandMap.put(Key.SHORTINT.hashValue, new CharStringCommand(Key.SHORTINT));
        charStringCommandMap.put(Key.CALLGSUBR.hashValue, new CharStringCommand(Key.CALLGSUBR));
        charStringCommandMap.put(Key.VHCURVETO.hashValue, new CharStringCommand(Key.VHCURVETO));
        charStringCommandMap.put(Key.HVCURVETO.hashValue, new CharStringCommand(Key.HVCURVETO));

        // two byte commands
        charStringCommandMap.put(Key.DOTSECTION.hashValue, new CharStringCommand(12, 0));
        charStringCommandMap.put(Key.VSTEM3.hashValue, new CharStringCommand(12, 1));
        charStringCommandMap.put(Key.HSTEM3.hashValue, new CharStringCommand(12, 2));
        charStringCommandMap.put(Key.AND.hashValue, new CharStringCommand(12, 3));
        charStringCommandMap.put(Key.OR.hashValue, new CharStringCommand(12, 4));
        charStringCommandMap.put(Key.NOT.hashValue, new CharStringCommand(12, 5));
        charStringCommandMap.put(Key.SEAC.hashValue, new CharStringCommand(12, 6));
        charStringCommandMap.put(Key.SBW.hashValue, new CharStringCommand(12, 7));
        charStringCommandMap.put(Key.ABS.hashValue, new CharStringCommand(12, 9));
        charStringCommandMap.put(Key.ADD.hashValue, new CharStringCommand(12, 10));
        charStringCommandMap.put(Key.SUB.hashValue, new CharStringCommand(12, 11));
        charStringCommandMap.put(Key.DIV.hashValue, new CharStringCommand(12, 12));
        charStringCommandMap.put(Key.NEG.hashValue, new CharStringCommand(12, 14));
        charStringCommandMap.put(Key.EQ.hashValue, new CharStringCommand(12, 15));
        charStringCommandMap.put(Key.CALLOTHERSUBR.hashValue, new CharStringCommand(12, 16));
        charStringCommandMap.put(Key.POP.hashValue, new CharStringCommand(12, 17));
        charStringCommandMap.put(Key.DROP.hashValue, new CharStringCommand(12, 18));
        charStringCommandMap.put(Key.PUT.hashValue, new CharStringCommand(12, 20));
        charStringCommandMap.put(Key.GET.hashValue, new CharStringCommand(12, 21));
        charStringCommandMap.put(Key.IFELSE.hashValue, new CharStringCommand(12, 22));
        charStringCommandMap.put(Key.RANDOM.hashValue, new CharStringCommand(12, 23));
        charStringCommandMap.put(Key.MUL.hashValue, new CharStringCommand(12, 24));
        charStringCommandMap.put(Key.SQRT.hashValue, new CharStringCommand(12, 26));
        charStringCommandMap.put(Key.DUP.hashValue, new CharStringCommand(12, 27));
        charStringCommandMap.put(Key.EXCH.hashValue, new CharStringCommand(12, 28));
        charStringCommandMap.put(Key.INDEX.hashValue, new CharStringCommand(12, 29));
        charStringCommandMap.put(Key.ROLL.hashValue, new CharStringCommand(12, 30));
        charStringCommandMap.put(Key.SETCURRENTPOINT.hashValue, new CharStringCommand(12, 33));
        charStringCommandMap.put(Key.HFLEX.hashValue, new CharStringCommand(12, 34));
        charStringCommandMap.put(Key.FLEX.hashValue, new CharStringCommand(12, 35));
        charStringCommandMap.put(Key.HFLEX1.hashValue, new CharStringCommand(12, 36));
        charStringCommandMap.put(Key.FLEX1.hashValue, new CharStringCommand(12, 37));
        return charStringCommandMap;
    }

    /**
     * Constructor with the CharStringCommand key as value.
     * 
     * @param key the key of the char string command
     */
    private CharStringCommand(CharStringCommand.Key key)
    {
        type1KeyWord = Type1KeyWord.valueOfKey(key.hashValue);
        type2KeyWord = Type2KeyWord.valueOfKey(key.hashValue);
    }

    /**
     * Constructor with two values.
     * 
     * @param b0 value1
     * @param b1 value2
     */
    private CharStringCommand(int b0, int b1)
    {
        type1KeyWord = Type1KeyWord.valueOfKey(b0, b1);
        type2KeyWord = Type2KeyWord.valueOfKey(b0, b1);
    }


    /**
     * Get an instance of the CharStringCommand represented by the given value.
     * 
     * @param b0 value
     * @return CharStringCommand represented by the given value
     */
    public static CharStringCommand getInstance(int b0)
    {
        CharStringCommand command = CHAR_STRING_COMMANDS.get(b0);
        return command != null ? command : COMMAND_UNKNOWN;
    }

    /**
     * Get an instance of the CharStringCommand represented by the given two values.
     * 
     * @param b0 value1
     * @param b1 value2
     * 
     * @return CharStringCommand represented by the given two values
     */
    public static CharStringCommand getInstance(int b0, int b1)
    {
        CharStringCommand command = CHAR_STRING_COMMANDS.get(getKeyHashValue(b0, b1));
        return command != null ? command : COMMAND_UNKNOWN;
    }

    /**
     * Get an instance of the CharStringCommand represented by the given array.
     * 
     * @param values array of values
     * 
     * @return CharStringCommand represented by the given values
     */
    public static CharStringCommand getInstance(int[] values)
    {
        if (values.length == 1)
        {
            return getInstance(values[0]);
        }
        else if (values.length == 2)
        {
            return getInstance(values[0], values[1]);
        }
        return COMMAND_UNKNOWN;
    }

    private static int getKeyHashValue(int b0, int b1)
    {
        Type1KeyWord type1Key = Type1KeyWord.valueOfKey(b0, b1);
        if (type1Key != null)
        {
            return type1Key.key.hashValue;
        }
        Type2KeyWord type2Key = Type2KeyWord.valueOfKey(b0, b1);
        if (type2Key != null)
        {
            return type2Key.key.hashValue;
        }
        return KEY_UNKNOWN;
    }

    /**
     * Return the underlying type1 key word.
     * 
     * @return the type1 key word
     */
    public Type1KeyWord getType1KeyWord()
    {
        return type1KeyWord;
    }

    /**
     * Return the underlying type2 key word.
     * 
     * @return the type2 key word
     */
    public Type2KeyWord getType2KeyWord()
    {
        return type2KeyWord;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String str;
        if (type2KeyWord != null)
        {
            str = type2KeyWord.toString();
        }
        else if (type1KeyWord != null)
        {
            str = type1KeyWord.toString();
        }
        else
        {
            str = "unknown command";
        }
        return str + '|';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (type1KeyWord != null)
        {
            return type1KeyWord.key.hashCode();
        }
        if (type2KeyWord != null)
        {
            return type2KeyWord.key.hashCode();
        }
        return 0;
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
            if (type1KeyWord != null && type1KeyWord.equals(that.getType1KeyWord()))
            {
                return true;
            }
            if (type2KeyWord != null && type2KeyWord.equals(that.getType2KeyWord()))
            {
                return true;
            }
            if (type1KeyWord == null && type2KeyWord == null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Enum of all valid type1 key words
     */
    public enum Type1KeyWord
    {
        HSTEM(Key.HSTEM), VSTEM(Key.VSTEM), VMOVETO(Key.VMOVETO), RLINETO(Key.RLINETO), //
        HLINETO(Key.HLINETO), VLINETO(Key.VLINETO), RRCURVETO(Key.RRCURVETO), //
        CLOSEPATH(Key.CLOSEPATH), CALLSUBR(Key.CALLSUBR), RET(Key.RET), //
        ESCAPE(Key.ESCAPE), DOTSECTION(Key.DOTSECTION), //
        VSTEM3(Key.VSTEM3), HSTEM3(Key.HSTEM3), SEAC(Key.SEAC), SBW(Key.SBW), //
        DIV(Key.DIV), CALLOTHERSUBR(Key.CALLOTHERSUBR), POP(Key.POP), //
        SETCURRENTPOINT(Key.SETCURRENTPOINT), HSBW(Key.HSBW), ENDCHAR(Key.ENDCHAR), //
        RMOVETO(Key.RMOVETO), HMOVETO(Key.HMOVETO), VHCURVETO(Key.VHCURVETO), //
        HVCURVETO(Key.HVCURVETO);

        final Key key;

        private Type1KeyWord(Key key)
        {
            this.key = key;
        }

        private static final Map<Key, Type1KeyWord> BY_KEY = new EnumMap<>(Key.class);
            
        static
        {
            for (Type1KeyWord e : values())
            {
                 BY_KEY.put(e.key, e);
            }
        }

        public static Type1KeyWord valueOfKey(int b0)
        {
            return BY_KEY.get(Key.valueOfKey(b0));
        }

        public static Type1KeyWord valueOfKey(int b0, int b1)
        {
            return BY_KEY.get(Key.valueOfKey(b0, b1));
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
        HSTEM(Key.HSTEM), VSTEM(Key.VSTEM), VMOVETO(Key.VMOVETO), RLINETO(Key.RLINETO), //
        HLINETO(Key.HLINETO), VLINETO(Key.VLINETO), RRCURVETO(Key.RRCURVETO), CALLSUBR(Key.CALLSUBR), //
        RET(Key.RET), ESCAPE(Key.ESCAPE), AND(Key.AND), OR(Key.OR), //
        NOT(Key.NOT), ABS(Key.ABS), ADD(Key.ADD), SUB(Key.SUB), //
        DIV(Key.DIV), NEG(Key.NEG), EQ(Key.EQ), DROP(Key.DROP), //
        PUT(Key.PUT), GET(Key.GET), IFELSE(Key.IFELSE), //
        RANDOM(Key.RANDOM), MUL(Key.MUL), SQRT(Key.SQRT), DUP(Key.DUP), //
        EXCH(Key.EXCH), INDEX(Key.INDEX), ROLL(Key.ROLL), //
        HFLEX(Key.HFLEX), FLEX(Key.FLEX), HFLEX1(Key.HFLEX1), //
        FLEX1(Key.FLEX1), ENDCHAR(Key.ENDCHAR), HSTEMHM(Key.HSTEMHM), HINTMASK(Key.HINTMASK), //
        CNTRMASK(Key.CNTRMASK), RMOVETO(Key.RMOVETO), HMOVETO(Key.HMOVETO), VSTEMHM(Key.VSTEMHM), //
        RCURVELINE(Key.RCURVELINE), RLINECURVE(Key.RLINECURVE), VVCURVETO(Key.VVCURVETO), //
        HHCURVETO(Key.HHCURVETO), SHORTINT(Key.SHORTINT), CALLGSUBR(Key.CALLGSUBR), //
        VHCURVETO(Key.VHCURVETO), HVCURVETO(Key.HVCURVETO);

        final Key key;

        private Type2KeyWord(Key key)
        {
            this.key = key;
        }

        private static final Map<Key, Type2KeyWord> BY_KEY = new EnumMap<>(Key.class);
            
        static
        {
            for (Type2KeyWord e : values())
            {
                 BY_KEY.put(e.key, e);
            }
        }

        public static Type2KeyWord valueOfKey(int b0)
        {
            return BY_KEY.get(Key.valueOfKey(b0));
        }

        public static Type2KeyWord valueOfKey(int b0, int b1)
        {
            return BY_KEY.get(Key.valueOfKey(b0, b1));
        }

        public static Type2KeyWord valueOfKey(Key key)
        {
            return BY_KEY.get(key);
        }
    }

    public enum Key
    {
        HSTEM(1), VSTEM(3), VMOVETO(4), RLINETO(5), //
        HLINETO(6), VLINETO(7), RRCURVETO(8), CLOSEPATH(9), CALLSUBR(10), //
        RET(11), ESCAPE(12), DOTSECTION(12, 0), VSTEM3(12, 1), HSTEM3(12, 2), //
        AND(12, 3), OR(12, 4), NOT(12, 5), SEAC(12, 6), SBW(12, 7), //
        ABS(12, 9), ADD(12, 10), SUB(12, 11), DIV(12, 12), NEG(12, 14), EQ(12, 15), //
        CALLOTHERSUBR(12, 16), POP(12, 17), DROP(12, 18), //
        PUT(12, 20), GET(12, 21), IFELSE(12, 22), //
        RANDOM(12, 23), MUL(12, 24), SQRT(12, 26), DUP(12, 27), //
        EXCH(12, 28), INDEX(12, 29), ROLL(12, 30), SETCURRENTPOINT(12, 33), //
        HFLEX(12, 34), FLEX(12, 35), HFLEX1(12, 36), FLEX1(12, 37), //
        HSBW(13), ENDCHAR(14), HSTEMHM(18), HINTMASK(19), //
        CNTRMASK(20), RMOVETO(21), HMOVETO(22), VSTEMHM(23), //
        RCURVELINE(24), RLINECURVE(25), VVCURVETO(26), //
        HHCURVETO(27), SHORTINT(28), CALLGSUBR(29), //
        VHCURVETO(30), HVCURVETO(31);

        private final int hashValue;

        private Key(int b0)
        {
            hashValue = b0;
        }

        private Key(int b0, int b1)
        {
            hashValue = (b0 << 4) + b1;
        }

        private static final Map<Integer, Key> BY_KEY = new HashMap<>();
        
        static
        {
            for (Key e : values())
            {
                BY_KEY.put(e.hashValue, e);
            }
        }

        public static Key valueOfKey(int b0)
        {
            return BY_KEY.get(b0);
        }

        public static Key valueOfKey(int b0, int b1)
        {
            return BY_KEY.get((b0 << 4) + b1);
        }
    }
}
