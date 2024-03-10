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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.fontbox.cff.CharStringCommand.Type1KeyWord;
import org.apache.fontbox.cff.CharStringCommand.Type2KeyWord;
import org.apache.fontbox.type1.Type1CharStringReader;

/**
 * Represents a Type 2 CharString by converting it into an equivalent Type 1 CharString.
 * 
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class Type2CharString extends Type1CharString
{
    private float defWidthX = 0;
    private float nominalWidthX = 0;
    private int pathCount = 0;
    private final int gid;

    /**
     * Constructor.
     * @param font Parent CFF font
     * @param fontName font name
     * @param glyphName glyph name (or CID as hex string)
     * @param gid GID
     * @param sequence Type 2 char string sequence
     * @param defaultWidthX default width
     * @param nomWidthX nominal width
     */
    public Type2CharString(Type1CharStringReader font, String fontName, String glyphName, int gid, List<Object> sequence,
                           int defaultWidthX, int nomWidthX)
    {
        super(font, fontName, glyphName);
        this.gid = gid;
        defWidthX = defaultWidthX;
        nominalWidthX = nomWidthX;
        convertType1ToType2(sequence);
    }

    /**
     * Return the GID (glyph id) of this charstring.
     * 
     * @return the GID of this charstring
     */
    public int getGID()
    {
        return gid;
    }

    /**
     * Converts a sequence of Type 2 commands into a sequence of Type 1 commands.
     * @param sequence the Type 2 char string sequence
     */
    private void convertType1ToType2(List<Object> sequence)
    {
        pathCount = 0;
        List<Number> numbers = new ArrayList<>();
        sequence.forEach(obj -> {
            if (obj instanceof CharStringCommand)
            {
                List<Number> results = convertType2Command(numbers, (CharStringCommand) obj);
                numbers.clear();
                numbers.addAll(results);
            }
            else
            {
                numbers.add((Number) obj);
            }
        });
    }

    private List<Number> convertType2Command(List<Number> numbers, CharStringCommand command)
    {
        Type2KeyWord type2KeyWord = command.getType2KeyWord();
        if (type2KeyWord == null)
        {
            addCommand(numbers, command);
            return Collections.emptyList();
        }
        switch (type2KeyWord)
        {
        case HSTEM:
        case HSTEMHM:
        case VSTEM:
        case VSTEMHM:
        case HINTMASK:
        case CNTRMASK:
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers,
                    type2KeyWord == Type2KeyWord.HSTEM || type2KeyWord == Type2KeyWord.HSTEMHM);
            break;
        case HMOVETO:
        case VMOVETO:
            numbers = clearStack(numbers, numbers.size() > 1);
            markPath();
            addCommand(numbers, command);
            break;
        case RLINETO:
            addCommandList(split(numbers, 2), command);
            break;
        case HLINETO:
        case VLINETO:
            addAlternatingLine(numbers, type2KeyWord == Type2KeyWord.HLINETO);
            break;
        case RRCURVETO:
            addCommandList(split(numbers, 6), command);
            break;
        case ENDCHAR:
            numbers = clearStack(numbers, numbers.size() == 5 || numbers.size() == 1);
            closeCharString2Path();
            if (numbers.size() == 4)
            {
                // deprecated "seac" operator
                numbers.add(0, 0);
                addCommand(numbers, CharStringCommand.getInstance(12, 6));
            }
            else
            {
                addCommand(numbers, command);
            }
            break;
        case RMOVETO:
            numbers = clearStack(numbers, numbers.size() > 2);
            markPath();
            addCommand(numbers, command);
            break;
        case HVCURVETO:
        case VHCURVETO:
            addAlternatingCurve(numbers, type2KeyWord == Type2KeyWord.HVCURVETO);
            break;
        case HFLEX:
            if (numbers.size() >= 7)
            {
                List<Number> first = Arrays.asList(numbers.get(0), 0, numbers.get(1), numbers.get(2),
                        numbers.get(3), 0);
                List<Number> second = Arrays.asList(numbers.get(4), 0, numbers.get(5),
                        -(numbers.get(2).floatValue()), numbers.get(6), 0);
                addCommandList(Arrays.asList(first, second), CharStringCommand.RRCURVETO);
            }
            break;
        case FLEX:
        {
            List<Number> first = numbers.subList(0, 6);
            List<Number> second = numbers.subList(6, 12);
            addCommandList(Arrays.asList(first, second), CharStringCommand.RRCURVETO);
            break;
        }
        case HFLEX1:
            if (numbers.size() >= 9)
            {
                List<Number> first = Arrays.asList(numbers.get(0), numbers.get(1), numbers.get(2),
                        numbers.get(3), numbers.get(4), 0);
                List<Number> second = Arrays.asList(numbers.get(5), 0, numbers.get(6), numbers.get(7),
                        numbers.get(8), 0);
                addCommandList(Arrays.asList(first, second), CharStringCommand.RRCURVETO);
            }
            break;
        case FLEX1:
        {
            int dx = 0;
            int dy = 0;
            for (int i = 0; i < 5; i++)
            {
                dx += numbers.get(i * 2).intValue();
                dy += numbers.get(i * 2 + 1).intValue();
            }
            List<Number> first = numbers.subList(0, 6);
            boolean dxIsBigger = Math.abs(dx) > Math.abs(dy);
            List<Number> second = Arrays.asList(
                    numbers.get(6),
                    numbers.get(7),
                    numbers.get(8),
                    numbers.get(9),
                    (dxIsBigger ? numbers.get(10) : -dx),
                    (dxIsBigger ? -dy : numbers.get(10)));
            addCommandList(Arrays.asList(first, second),
                    CharStringCommand.RRCURVETO);
            break;
        }
        case RCURVELINE:
            if (numbers.size() >= 2)
            {
                addCommandList(split(numbers.subList(0, numbers.size() - 2), 6),
                        CharStringCommand.RRCURVETO);
                addCommand(numbers.subList(numbers.size() - 2, numbers.size()),
                        CharStringCommand.RLINETO);
            }
            break;
        case RLINECURVE:
            if (numbers.size() >= 6)
            {
                addCommandList(split(numbers.subList(0, numbers.size() - 6), 2),
                        CharStringCommand.RLINETO);
                addCommand(numbers.subList(numbers.size() - 6, numbers.size()),
                        CharStringCommand.RRCURVETO);
            }
            break;
        case HHCURVETO:
        case VVCURVETO:
            addCurve(numbers, type2KeyWord == Type2KeyWord.HHCURVETO);
            break;
        default:
            addCommand(numbers, command);
            break;
        }
        return Collections.emptyList();
    }

    private List<Number> clearStack(List<Number> numbers, boolean flag)
    {
        if (isSequenceEmpty())
        {
            if (flag)
            {
                addCommand(Arrays.asList(0, numbers.get(0).floatValue() + nominalWidthX),
                        CharStringCommand.HSBW);
                numbers = numbers.subList(1, numbers.size());
            }
            else
            {
                addCommand(Arrays.asList(0, defWidthX), CharStringCommand.HSBW);
            }
        }
        return numbers;
    }

    /**
     * @param numbers  
     * @param horizontal 
     */
    private void expandStemHints(List<Number> numbers, boolean horizontal)
    {
        // TODO
    }

    private void markPath()
    {
        if (pathCount > 0)
        {
            closeCharString2Path();
        }
        pathCount++;
    }

    private void closeCharString2Path()
    {
        CharStringCommand command = pathCount > 0 ? (CharStringCommand) getLastSequenceEntry()
                : null;
        if (command != null && command.getType1KeyWord() != Type1KeyWord.CLOSEPATH)
        {
            addCommand(Collections.emptyList(), CharStringCommand.CLOSEPATH);
        }
    }

    private void addAlternatingLine(List<Number> numbers, boolean horizontal)
    {
        while (!numbers.isEmpty())
        {
            addCommand(numbers.subList(0, 1), horizontal ? CharStringCommand.HLINETO
                    : CharStringCommand.VLINETO);
            numbers = numbers.subList(1, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void addAlternatingCurve(List<Number> numbers, boolean horizontal)
    {
        while (numbers.size() >= 4)
        {
            boolean last = numbers.size() == 5;
            if (horizontal)
            {
                addCommand(Arrays.asList(numbers.get(0), 0,
                        numbers.get(1), numbers.get(2), last ? numbers.get(4)
                                : 0, numbers.get(3)),
                        CharStringCommand.RRCURVETO);
            } 
            else
            {
                addCommand(Arrays.asList(0, numbers.get(0),
                        numbers.get(1), numbers.get(2), numbers.get(3),
                        last ? numbers.get(4) : 0),
                        CharStringCommand.RRCURVETO);
            }
            numbers = numbers.subList(last ? 5 : 4, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void addCurve(List<Number> numbers, boolean horizontal)
    {
        while (numbers.size() >= 4)
        {
            boolean first = numbers.size() % 4 == 1;

            if (horizontal)
            {
                addCommand(Arrays.asList(numbers.get(first ? 1 : 0),
                        first ? numbers.get(0) : 0, numbers
                                .get(first ? 2 : 1),
                        numbers.get(first ? 3 : 2), numbers.get(first ? 4 : 3),
                        0), CharStringCommand.RRCURVETO);
            } 
            else
            {
                addCommand(Arrays.asList(first ? numbers.get(0) : 0, numbers.get(first ? 1 : 0), numbers
                        .get(first ? 2 : 1), numbers.get(first ? 3 : 2),
                        0, numbers.get(first ? 4 : 3)),
                        CharStringCommand.RRCURVETO);
            }
            numbers = numbers.subList(first ? 5 : 4, numbers.size());
        }
    }

    private void addCommandList(List<List<Number>> numbers, CharStringCommand command)
    {
        numbers.forEach(ns -> addCommand(ns, command));
    }

    private static <E> List<List<E>> split(List<E> list, int size)
    {
        int listSize = list.size() / size;
        List<List<E>> result = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++)
        {
            result.add(list.subList(i * size, (i + 1) * size));
        }
        return result;
    }
}
