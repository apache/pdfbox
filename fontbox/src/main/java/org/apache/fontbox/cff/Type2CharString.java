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
    private final List<Object> type2sequence;
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
        type2sequence = sequence;
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
     * Returns the Type 2 charstring sequence.
     */
    public List<Object> getType2Sequence()
    {
        return type2sequence;
    }

    /**
     * Converts a sequence of Type 2 commands into a sequence of Type 1 commands.
     * @param sequence the Type 2 char string sequence
     */
    private void convertType1ToType2(List<Object> sequence)
    {
        type1Sequence = new ArrayList<Object>();
        pathCount = 0;
        CharStringHandler handler = new CharStringHandler() {
            @Override
            public List<Number> handleCommand(List<Number> numbers, CharStringCommand command)
            {
                return Type2CharString.this.handleCommand(numbers, command);
            }
        };
        handler.handleSequence(sequence);
    }

    @SuppressWarnings(value = { "unchecked" })
    private List<Number> handleCommand(List<Number> numbers, CharStringCommand command)
    {
        commandCount++;
        String name = CharStringCommand.TYPE2_VOCABULARY.get(command.getKey());

        if ("hstem".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, true);
        }
        else if ("vstem".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, false);
        }
        else if ("vmoveto".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() > 1);
            markPath();
            addCommand(numbers, command);
        }
        else if ("rlineto".equals(name))
        {
            addCommandList(split(numbers, 2), command);
        }
        else if ("hlineto".equals(name))
        {
            drawAlternatingLine(numbers, true);
        }
        else if ("vlineto".equals(name))
        {
            drawAlternatingLine(numbers, false);
        }
        else if ("rrcurveto".equals(name))
        {
            addCommandList(split(numbers, 6), command);
        }
        else if ("endchar".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() == 5 || numbers.size() == 1);
            closePath();
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
        }
        else if ("rmoveto".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() > 2);
            markPath();
            addCommand(numbers, command);
        }
        else if ("hmoveto".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() > 1);
            markPath();
            addCommand(numbers, command);
        }
        else if ("vhcurveto".equals(name))
        {
            drawAlternatingCurve(numbers, false);
        }
        else if ("hvcurveto".equals(name))
        {
            drawAlternatingCurve(numbers, true);
        }
        else if ("hflex".equals(name))
        {
            List<Number> first = Arrays.asList(numbers.get(0), 0,
                    numbers.get(1), numbers.get(2), numbers.get(3), 0);
            List<Number> second = Arrays.asList(numbers.get(4), 0,
                    numbers.get(5), -(numbers.get(2).floatValue()),
                    numbers.get(6), 0);
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        } 
        else if ("flex".equals(name))
        {
            List<Number> first = numbers.subList(0, 6);
            List<Number> second = numbers.subList(6, 12);
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        }
        else if ("hflex1".equals(name))
        {
            List<Number> first = Arrays.asList(numbers.get(0), numbers.get(1), 
                    numbers.get(2), numbers.get(3), numbers.get(4), 0);
            List<Number> second = Arrays.asList(numbers.get(5), 0,
                    numbers.get(6), numbers.get(7), numbers.get(8), 0);
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        }
        else if ("flex1".equals(name))
        {
            int dx = 0;
            int dy = 0;
            for(int i = 0; i < 5; i++)
            {
                dx += numbers.get(i * 2).intValue();
                dy += numbers.get(i * 2 + 1).intValue();
            }
            List<Number> first = numbers.subList(0, 6);
            List<Number> second = Arrays.asList(numbers.get(6), numbers.get(7), numbers.get(8), 
                    numbers.get(9), (Math.abs(dx) > Math.abs(dy) ? numbers.get(10) : -dx), 
                    (Math.abs(dx) > Math.abs(dy) ? -dy : numbers.get(10)));
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        }
        else if ("hstemhm".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, true);
        } 
        else if ("hintmask".equals(name) || "cntrmask".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            if (numbers.size() > 0)
            {
                expandStemHints(numbers, false);
            }
        } 
        else if ("vstemhm".equals(name))
        {
            numbers = clearStack(numbers, numbers.size() % 2 != 0);
            expandStemHints(numbers, false);
        } 
        else if ("rcurveline".equals(name))
        {
            if (numbers.size() >= 2)
            {
                addCommandList(split(numbers.subList(0, numbers.size() - 2), 6),
                        new CharStringCommand(8));
                addCommand(numbers.subList(numbers.size() - 2, numbers.size()),
                        new CharStringCommand(5));
            }
        } 
        else if ("rlinecurve".equals(name))
        {
            if (numbers.size() >= 6)
            {
                addCommandList(split(numbers.subList(0, numbers.size() - 6), 2),
                        new CharStringCommand(5));
                addCommand(numbers.subList(numbers.size() - 6, numbers.size()),
                        new CharStringCommand(8));
            }
        } 
        else if ("vvcurveto".equals(name))
        {
            drawCurve(numbers, false);
        } 
        else if ("hhcurveto".equals(name))
        {
            drawCurve(numbers, true);
        } 
        else
        {
            addCommand(numbers, command);
        }
        return null;
    }

    private List<Number> clearStack(List<Number> numbers, boolean flag)
    {
        if (type1Sequence.isEmpty())
        {
            if (flag)
            {
                addCommand(Arrays.asList((Number) 0f, numbers.get(0).floatValue() + nominalWidthX),
                        new CharStringCommand(13));
                numbers = numbers.subList(1, numbers.size());
            }
            else
            {
                addCommand(Arrays.asList((Number) 0f, defWidthX),
                        new CharStringCommand(13));
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
            closePath();
        }
        pathCount++;
    }

    private void closePath()
    {
        CharStringCommand command = pathCount > 0 ? (CharStringCommand) type1Sequence
                .get(type1Sequence.size() - 1)
                : null;

        CharStringCommand closepathCommand = new CharStringCommand(9);
        if (command != null && !closepathCommand.equals(command))
        {
            addCommand(Collections.<Number> emptyList(), closepathCommand);
        }
    }

    private void drawAlternatingLine(List<Number> numbers, boolean horizontal)
    {
        while (numbers.size() > 0)
        {
            addCommand(numbers.subList(0, 1), new CharStringCommand(
                    horizontal ? 6 : 7));
            numbers = numbers.subList(1, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void drawAlternatingCurve(List<Number> numbers, boolean horizontal)
    {
        while (numbers.size() >= 4)
        {
            boolean last = numbers.size() == 5;
            if (horizontal)
            {
                addCommand(Arrays.asList(numbers.get(0), 0,
                        numbers.get(1), numbers.get(2), last ? numbers.get(4)
                                : 0, numbers.get(3)),
                        new CharStringCommand(8));
            } 
            else
            {
                addCommand(Arrays.asList(0, numbers.get(0),
                        numbers.get(1), numbers.get(2), numbers.get(3),
                        last ? numbers.get(4) : 0),
                        new CharStringCommand(8));
            }
            numbers = numbers.subList(last ? 5 : 4, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void drawCurve(List<Number> numbers, boolean horizontal)
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
                        0), new CharStringCommand(8));
            } 
            else
            {
                addCommand(Arrays.asList(first ? numbers.get(0) : 0, numbers.get(first ? 1 : 0), numbers
                        .get(first ? 2 : 1), numbers.get(first ? 3 : 2),
                        0, numbers.get(first ? 4 : 3)),
                        new CharStringCommand(8));
            }
            numbers = numbers.subList(first ? 5 : 4, numbers.size());
        }
    }

    private void addCommandList(List<List<Number>> numbers, CharStringCommand command)
    {
        for (List<Number> ns : numbers)
        {
            addCommand(ns, command);
        }
    }

    private void addCommand(List<Number> numbers, CharStringCommand command)
    {
        type1Sequence.addAll(numbers);
        type1Sequence.add(command);
    }

    private static <E> List<List<E>> split(List<E> list, int size)
    {
        List<List<E>> result = new ArrayList<List<E>>();
        for (int i = 0; i < list.size() / size; i++)
        {
            result.add(list.subList(i * size, (i + 1) * size));
        }
        return result;
    }
}
