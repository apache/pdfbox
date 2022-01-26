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
import java.util.Collections;
import java.util.List;

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
    //todo think about to use an CharStringCommand(8) as static final field
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
        type1Sequence = new ArrayList<>();
        pathCount = 0;
        CharStringHandler handler = Type2CharString.this::handleType2Command;
        handler.handleSequence(sequence);
    }

    private List<Number> handleType2Command(List<Number> numbers, CharStringCommand command)
    {
        commandCount++;
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
            clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, true);
            break;
        case VSTEM:
        case VSTEMHM:
            clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, false);
            break;
        case VMOVETO:
        case HMOVETO:
            clearStack(numbers, numbers.size() > 1);
            markPath();
            addCommand(numbers, command);
            break;
        case RLINETO:
            addCommandsList(numbers, 2, command);
            break;
        case HLINETO:
            drawAlternatingLine(numbers, true);
            break;
        case VLINETO:
            drawAlternatingLine(numbers, false);
            break;
        case RRCURVETO:
            addCommandsList(numbers, 6, command);
            break;
        case ENDCHAR:
            clearStack(numbers, numbers.size() == 5 || numbers.size() == 1);
            closeCharString2Path();
            if (numbers.size() == 4)
            {
                // deprecated "seac" operator
                numbers.add(0, 0);
                addCommand(numbers, new CharStringCommand(12, 6));
            }
            else
            {
                addCommand(numbers, command);
            }
            break;
        case RMOVETO:
            clearStack(numbers, numbers.size() > 2);
            markPath();
            addCommand(numbers, command);
            break;
        case VHCURVETO:
            drawAlternatingCurve(numbers, false);
            break;
        case HVCURVETO:
            drawAlternatingCurve(numbers, true);
            break;
        case HFLEX:
            if (numbers.size() >= 7)
            {
                List<Number> first = Arrays.asList(numbers.get(0), 0, numbers.get(1), numbers.get(2),
                        numbers.get(3), 0);
                List<Number> second = Arrays.asList(numbers.get(4), 0, numbers.get(5),
                        -(numbers.get(2).floatValue()), numbers.get(6), 0);
                addCommand(first, command8);
                addCommand(second, command8);
            }
            break;
        case FLEX:
        {
            Number[] first = subArray(numbers,0, 6);
            Number[] second = subArray(numbers,6, 12);
            CharStringCommand command8 = new CharStringCommand(8);
            addCommand(first, command8);
            addCommand(second, command8);
            break;
        }
        case HFLEX1:
            if (numbers.size() >= 9)
            {
                List<Number> first = Arrays.asList(numbers.get(0), numbers.get(1), numbers.get(2),
                        numbers.get(3), numbers.get(4), 0);
                List<Number> second = Arrays.asList(numbers.get(5), 0, numbers.get(6), numbers.get(7),
                        numbers.get(8), 0);
                addCommand(first, command8);
                addCommand(second, command8);
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
            Number[] first = subArray(numbers,0, 6);
            Number[] second = new Number[]{numbers.get(6), numbers.get(7), numbers.get(8),
                    numbers.get(9), Math.abs(dx) > Math.abs(dy) ? numbers.get(10) : -dx,
                    Math.abs(dx) > Math.abs(dy) ? -dy : numbers.get(10)};
            CharStringCommand command8 = new CharStringCommand(8);
            addCommand(first, command8);
            addCommand(second, command8);
            break;
        }
        case HINTMASK:
        case CNTRMASK:
            clearStack(numbers, numbers.size() % 2 != 0);
            if (!numbers.isEmpty())
            {
                expandStemHints(numbers, false);
            }
            break;
        case RCURVELINE:
            if (numbers.size() >= 2)
            {
                addCommandsList(numbers.subList(0, numbers.size() - 2), 6,
                        new CharStringCommand(8));
                addCommand(subArray(numbers,numbers.size() - 2, numbers.size()),
                        new CharStringCommand(5));
            }
            break;
        case RLINECURVE:
            if (numbers.size() >= 6)
            {
                addCommandsList(numbers.subList(0, numbers.size() - 6), 2,
                        new CharStringCommand(5));
                addCommand(subArray(numbers, numbers.size() - 6, numbers.size()),
                        new CharStringCommand(8));
            }
            break;
        case VVCURVETO:
            drawCurve(numbers, false);
            break;
        case HHCURVETO:
            drawCurve(numbers, true);
            break;
        default:
            addCommand(numbers, command);
            break;
        }
        return Collections.emptyList();
    }

    private void clearStack(List<Number> numbers, boolean flag)
    {
        if (type1Sequence.isEmpty())
        {
            if (flag)
            {
                addCommand(new Number[]{0, numbers.get(0).floatValue() + nominalWidthX},
                        new CharStringCommand(13));
                numbers.remove(0);
            }
            else
            {
                addCommand(new Number[]{0, defWidthX}, new CharStringCommand(13));
            }
        }
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
        if (pathCount > 0)
        {
            CharStringCommand command = (CharStringCommand) type1Sequence
                    .get(type1Sequence.size() - 1);

            CharStringCommand closepathCommand = new CharStringCommand(9);
            if (!closepathCommand.equals(command))
            {
                addCommand(0, closepathCommand);
            }
        }
    }

    private void drawAlternatingLine(List<Number> numbers, boolean horizontal)
    {
        int i = 0;
        CharStringCommand command6 = new CharStringCommand(6);
        CharStringCommand command7 = new CharStringCommand(7);
        while (i < numbers.size())
        {
            addCommand(numbers.get(i), horizontal ? command6 : command7);
            horizontal = !horizontal;
            ++i;
        }
    }

    private void drawAlternatingCurve(List<Number> numbers, boolean horizontal)
    {
        int startIndex = 0;
        int numbersCount = numbers.size();
        CharStringCommand command8 = new CharStringCommand(8);
        while (numbersCount >= 4)
        {
            boolean last = numbersCount == 5;
            if (horizontal)
            {
                addCommand(new Number[]{numbers.get(startIndex), 0,
                        numbers.get(startIndex + 1), numbers.get(startIndex + 2),
                        last ? numbers.get(startIndex + 4) : 0, numbers.get(startIndex + 3)},
                        command8);
            } 
            else
            {
                addCommand(new Number[]{0, numbers.get(startIndex),
                        numbers.get(startIndex + 1), numbers.get(startIndex + 2), numbers.get(startIndex + 3),
                        last ? numbers.get(startIndex + 4) : 0},
                        command8);
            }

            if (last)
            {
                startIndex += 5;
                numbersCount -= 5;
            }
            else
            {
                startIndex += 4;
                numbersCount -= 4;
            }

            horizontal = !horizontal;
        }
    }

    private void drawCurve(List<Number> numbers, boolean horizontal)
    {
        int startIndex = 0;
        int numbersCount = numbers.size();
        CharStringCommand command8 = new CharStringCommand(8);
        if (horizontal)
        {
            while (numbersCount >= 4)
            {
                //is first?
                if (numbersCount % 4 == 1)
                {
                    addCommand(new Number[]{numbers.get(startIndex + 1),
                            numbers.get(startIndex), numbers.get(startIndex + 2),
                            numbers.get(startIndex + 3), numbers.get(startIndex + 4),
                            0}, command8);

                    startIndex += 5;
                    numbersCount -= 5;
                }
                else
                {
                    addCommand(new Number[]{numbers.get(startIndex),
                            0, numbers.get(startIndex + 1),
                            numbers.get(startIndex + 2), numbers.get(startIndex + 3),
                            0}, command8);

                    startIndex += 4;
                    numbersCount -= 4;
                }
            }
        }
        else
        {
            while (numbersCount >= 4)
            {
                //is first?
                if (numbersCount % 4 == 1)
                {
                    addCommand(new Number[]{numbers.get(startIndex), numbers.get(startIndex + 1),
                                numbers.get(startIndex + 2), numbers.get(startIndex + 3),
                                0, numbers.get(startIndex + 4)},
                                command8);

                    startIndex += 5;
                    numbersCount -= 5;
                }
                else
                {
                    addCommand(new Number[]{0, numbers.get(startIndex),
                                numbers.get(startIndex + 1), numbers.get(startIndex + 2),
                                0, numbers.get(startIndex + 3)},
                                command8);

                    startIndex += 4;
                    numbersCount -= 4;
                }
            }
        }
    }

    private void addCommandsList(List<Number> list, int size, CharStringCommand command)
    {
        int listSize = list.size() / size;
        for (int i = 0; i < listSize; i++)
        {
            addCommand(list.subList(i * size, (i + 1) * size), command);
        }
    }

    private void addCommand(Number number, CharStringCommand command)
    {
        type1Sequence.add(number);
        type1Sequence.add(command);
    }

    private void addCommand(List<Number> numbers, CharStringCommand command)
    {
        type1Sequence.addAll(numbers);
        type1Sequence.add(command);
    }

    private void addCommand(Number[] numbers, CharStringCommand command)
    {
        for (int i = 0; i < numbers.length; ++i)
        {
            type1Sequence.add(numbers[i]);
        }
        type1Sequence.add(command);
    }

    private Number[] subArray(List<Number> numbers, int startIndex, int endIndex)
    {
        Number[] arr = new Number[endIndex - startIndex];

        for (int i = 0; i < arr.length; ++i)
        {
            arr[i] = numbers.get(i);
        }

        return arr;
    }
}
