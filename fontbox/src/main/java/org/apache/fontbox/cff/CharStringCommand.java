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

/**
 * This class represents a CharStringCommand.
 * 
 * @author Villu Ruusmann
 */
public enum CharStringCommand
{
    HSTEM(Type1KeyWord.HSTEM, Type2KeyWord.HSTEM, 1),
    VSTEM(Type1KeyWord.VSTEM, Type2KeyWord.VSTEM, 3),
    VMOVETO(Type1KeyWord.VMOVETO, Type2KeyWord.VMOVETO,4),
    RLINETO(Type1KeyWord.RLINETO, Type2KeyWord.RLINETO,5),
    HLINETO(Type1KeyWord.HLINETO, Type2KeyWord.HLINETO,6),
    VLINETO(Type1KeyWord.VLINETO, Type2KeyWord.VLINETO,7),
    RRCURVETO(Type1KeyWord.RRCURVETO, Type2KeyWord.RRCURVETO,8),
    CLOSEPATH(Type1KeyWord.CLOSEPATH, null,9),
    CALLSUBR(Type1KeyWord.CALLSUBR, Type2KeyWord.CALLSUBR,10),
    RET(Type1KeyWord.RET, Type2KeyWord.RET,11),
    ESCAPE(Type1KeyWord.ESCAPE, Type2KeyWord.ESCAPE,12),
    HSBW(Type1KeyWord.HSBW, null,13),
    ENDCHAR(Type1KeyWord.ENDCHAR, Type2KeyWord.ENDCHAR,14),
    HSTEMHM(null, Type2KeyWord.HSTEMHM,18),
    HINTMASK(null, Type2KeyWord.HINTMASK,19),
    CNTRMASK(null, Type2KeyWord.CNTRMASK,20),
    RMOVETO(Type1KeyWord.RMOVETO, Type2KeyWord.RMOVETO,21),
    HMOVETO(Type1KeyWord.HMOVETO, Type2KeyWord.HMOVETO,22),
    VSTEMHM(null, Type2KeyWord.VSTEMHM,23),
    RCURVELINE(null, Type2KeyWord.RCURVELINE,24),
    RLINECURVE(null, Type2KeyWord.RLINECURVE,25),
    VVCURVETO(null, Type2KeyWord.VVCURVETO,26),
    HHCURVETO(null, Type2KeyWord.HHCURVETO,27),
    SHORTINT(null, Type2KeyWord.SHORTINT,28),
    CALLGSUBR(null, Type2KeyWord.CALLGSUBR,29),
    VHCURVETO(Type1KeyWord.VHCURVETO, Type2KeyWord.VHCURVETO,30),
    HVCURVETO(Type1KeyWord.HVCURVETO, Type2KeyWord.HVCURVETO,31),
    DOTSECTION(Type1KeyWord.DOTSECTION, null,192),
    VSTEM3(Type1KeyWord.VSTEM3, null,193),
    HSTEM3(Type1KeyWord.HSTEM3, null,194),
    AND(null, Type2KeyWord.AND,195),
    OR(null, Type2KeyWord.OR,196),
    NOT(null, Type2KeyWord.NOT,197),
    SEAC(Type1KeyWord.SEAC, null,198),
    SBW(Type1KeyWord.SBW, null,199),
    ABS(null, Type2KeyWord.ABS,201),
    ADD(null, Type2KeyWord.ADD,202),
    SUB(null, Type2KeyWord.SUB,203),
    DIV(Type1KeyWord.DIV, Type2KeyWord.DIV,204),
    NEG(null, Type2KeyWord.NEG,206),
    EQ(null, Type2KeyWord.EQ,207),
    CALLOTHERSUBR(Type1KeyWord.CALLOTHERSUBR, null,208),
    POP(Type1KeyWord.POP, null,209),
    DROP(null, Type2KeyWord.DROP,210),
    PUT(null, Type2KeyWord.PUT,212),
    GET(null, Type2KeyWord.GET,213),
    IFELSE(null, Type2KeyWord.IFELSE,214),
    RANDOM(null, Type2KeyWord.RANDOM,215),
    MUL(null, Type2KeyWord.MUL,216),
    SQRT(null, Type2KeyWord.SQRT,218),
    DUP(null, Type2KeyWord.DUP,219),
    EXCH(null, Type2KeyWord.EXCH,220),
    INDEX(null, Type2KeyWord.INDEX,221),
    ROLL(null, Type2KeyWord.ROLL,222),
    SETCURRENTPOINT(Type1KeyWord.SETCURRENTPOINT, null,225),
    HFLEX(null, Type2KeyWord.HFLEX,226),
    FLEX(null, Type2KeyWord.FLEX,227),
    HFLEX1(null, Type2KeyWord.HFLEX1,228),
    FLEX1(null, Type2KeyWord.FLEX1,229),
    UNKNOWN(null, null, 99);

    private static final CharStringCommand[] COMMANDS_BY_VALUE;

    static
    {
        int max = Arrays.stream(CharStringCommand.values()).mapToInt(CharStringCommand::getValue)
                .max().orElseThrow();
        COMMANDS_BY_VALUE = new CharStringCommand[max + 1];
        Arrays.stream(CharStringCommand.values()).forEach(c -> COMMANDS_BY_VALUE[c.getValue()] = c);
    }

    private final Type1KeyWord type1KeyWord;
    private final Type2KeyWord type2KeyWord;
    private final int value;
    private final String stringValue;

    CharStringCommand(Type1KeyWord type1KeyWord, Type2KeyWord type2KeyWord, int value)
    {
        this.type1KeyWord = type1KeyWord;
        this.type2KeyWord = type2KeyWord;
        this.value = value;
        this.stringValue = value == 99 ? "unknown command|" : name() + "|";
    }

    public int getValue()
    {
        return value;
    }

    /**
     * Get an instance of the CharStringCommand represented by the given value.
     *
     * @param b0 value
     * @return CharStringCommand represented by the given value
     */
    public static CharStringCommand getInstance(int b0)
    {
        CharStringCommand c = null;
        if (b0 >= 0 && b0 < COMMANDS_BY_VALUE.length)
        {
            c = COMMANDS_BY_VALUE[b0];
        }
        return c != null ? c : UNKNOWN;
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
        return getInstance((b0 << 4) + b1);
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
        switch (values.length)
        {
        case 1:
            return getInstance(values[0]);
        case 2:
            return getInstance(values[0], values[1]);
        default:
            return UNKNOWN;
        }
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

    @Override
    public String toString()
    {
        return stringValue;
    }

    /**
     * Enum of all valid type1 key words
     */
    public enum Type1KeyWord
    {
        HSTEM,
        VSTEM,
        VMOVETO,
        RLINETO,
        HLINETO,
        VLINETO,
        RRCURVETO,
        CLOSEPATH,
        CALLSUBR,
        RET,
        ESCAPE,
        HSBW,
        ENDCHAR,
        RMOVETO,
        HMOVETO,
        VHCURVETO,
        HVCURVETO,
        DOTSECTION,
        VSTEM3,
        HSTEM3,
        SEAC,
        SBW,
        DIV,
        CALLOTHERSUBR,
        POP,
        SETCURRENTPOINT;
    }

    /**
     * Enum of all valid type2 key words
     */
    public enum Type2KeyWord
    {
        HSTEM,
        VSTEM,
        VMOVETO,
        RLINETO,
        HLINETO,
        VLINETO,
        RRCURVETO,
        CALLSUBR,
        RET,
        ESCAPE,
        ENDCHAR,
        HSTEMHM,
        HINTMASK,
        CNTRMASK,
        RMOVETO,
        HMOVETO,
        VSTEMHM,
        RCURVELINE,
        RLINECURVE,
        VVCURVETO,
        HHCURVETO,
        SHORTINT,
        CALLGSUBR,
        VHCURVETO,
        HVCURVETO,
        AND,
        OR,
        NOT,
        ABS,
        ADD,
        SUB,
        DIV,
        NEG,
        EQ,
        DROP,
        PUT,
        GET,
        IFELSE,
        RANDOM,
        MUL,
        SQRT,
        DUP,
        EXCH,
        INDEX,
        ROLL,
        HFLEX,
        FLEX,
        HFLEX1,
        FLEX1;
    }
}
