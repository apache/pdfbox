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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class to translate Type2 CharString command sequence to Type1 CharString command sequence.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CharStringConverter extends CharStringHandler
{

    private int defaultWidthX = 0;
    private int nominalWidthX = 0;
    private List<Object> sequence = null;
    private int pathCount = 0;
    private IndexData globalSubrIndex = null;
    private IndexData localSubrIndex = null;

    /**
     * Constructor.
     * 
     * @param defaultWidth default width
     * @param nominalWidth nominal width
     */
    public CharStringConverter(int defaultWidth, int nominalWidth, IndexData fontGlobalSubrIndex, IndexData fontLocalSubrIndex)
    {
        defaultWidthX = defaultWidth;
        nominalWidthX = nominalWidth;
        globalSubrIndex = fontGlobalSubrIndex;
        localSubrIndex = fontLocalSubrIndex;
    }

    /**
     * Converts a sequence of Type1/Type2 commands into a sequence of CharStringCommands.
     * @param commandSequence the type1/type2 sequence
     * @return the CHarStringCommandSequence
     */
    public List<Object> convert(List<Object> commandSequence)
    {
        sequence = new ArrayList<Object>();
        pathCount = 0;
        handleSequence(commandSequence);
        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> handleCommand(List<Integer> numbers, CharStringCommand command)
    {

        if (CharStringCommand.TYPE1_VOCABULARY.containsKey(command.getKey()))
        {
            return handleType1Command(numbers, command);
        } 
        else
        {
            return handleType2Command(numbers, command);
        }
    }

    private List<Integer> handleType1Command(List<Integer> numbers,
            CharStringCommand command)
    {
        String name = CharStringCommand.TYPE1_VOCABULARY.get(command.getKey());

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
            numbers = clearStack(numbers, numbers.size() > 0);
            closePath();
            addCommand(numbers, command);
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
        else if ("callsubr".equals(name))
        {
            //get subrbias
            int bias = 0;
            int nSubrs = localSubrIndex.getCount();
            
            if (nSubrs < 1240)
            {
                bias = 107;
            }
            else if (nSubrs < 33900) 
            {
                bias = 1131;
            }
            else 
            {
                bias = 32768;
            }
           
            List<Integer> result = null;
            int subrNumber = bias+numbers.get(numbers.size()-1);
            if (subrNumber < localSubrIndex.getCount())
            {
                Type2CharStringParser parser = new Type2CharStringParser();
                byte[] bytes = localSubrIndex.getBytes(subrNumber);
                List<Object> parsed = null;
                try {
                    parsed = parser.parse(bytes);
                    parsed.addAll(0,numbers.subList(0, numbers.size()-1));
                    result = handleSequence(parsed);
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
            return result;
        }
        else if ("return".equals(name))
        {
            return numbers;
        }
        else
        {
            addCommand(numbers, command);
        }
        return null;
    }

    @SuppressWarnings(value = { "unchecked" })
    private List<Integer> handleType2Command(List<Integer> numbers,
            CharStringCommand command)
    {
        String name = CharStringCommand.TYPE2_VOCABULARY.get(command.getKey());
        if ("hflex".equals(name))
        {
            List<Integer> first = Arrays.asList(numbers.get(0), Integer.valueOf(0), 
                    numbers.get(1), numbers.get(2), numbers.get(3), Integer.valueOf(0));
            List<Integer> second = Arrays.asList(numbers.get(4), Integer.valueOf(0), 
                    numbers.get(5), Integer.valueOf(-numbers.get(2).intValue()), 
                    numbers.get(6), Integer.valueOf(0));
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        } 
        else if ("flex".equals(name))
        {
            List<Integer> first = numbers.subList(0, 6);
            List<Integer> second = numbers.subList(6, 12);
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        }
        else if ("hflex1".equals(name))
        {
            List<Integer> first = Arrays.asList(numbers.get(0), numbers.get(1), 
                    numbers.get(2), numbers.get(3), numbers.get(4), Integer.valueOf(0));
            List<Integer> second = Arrays.asList(numbers.get(5), Integer.valueOf(0),
                    numbers.get(6), numbers.get(7), numbers.get(8), Integer.valueOf(0));
            addCommandList(Arrays.asList(first, second), new CharStringCommand(8));
        }
        else if ("flex1".equals(name))
        {
            int dx = 0;
            int dy = 0;
            for(int i = 0; i < 5; i++){
                dx += numbers.get(i * 2).intValue();
                dy += numbers.get(i * 2 + 1).intValue();
            }
            List<Integer> first = numbers.subList(0, 6);
            List<Integer> second = Arrays.asList(numbers.get(6), numbers.get(7), numbers.get(8), 
                    numbers.get(9), (Math.abs(dx) > Math.abs(dy) ? numbers.get(10) : Integer.valueOf(-dx)), 
                    (Math.abs(dx) > Math.abs(dy) ? Integer.valueOf(-dy) : numbers.get(10)));
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
            addCommandList(split(numbers.subList(0, numbers.size() - 2), 6),
                    new CharStringCommand(8));
            addCommand(numbers.subList(numbers.size() - 2, numbers.size()),
                    new CharStringCommand(5));
        } 
        else if ("rlinecurve".equals(name))
        {
            addCommandList(split(numbers.subList(0, numbers.size() - 6), 2),
                    new CharStringCommand(5));
            addCommand(numbers.subList(numbers.size() - 6, numbers.size()),
                    new CharStringCommand(8));
        } 
        else if ("vvcurveto".equals(name))
        {
            drawCurve(numbers, false);
        } 
        else if ("hhcurveto".equals(name))
        {
            drawCurve(numbers, true);
        } 
        else if ("callgsubr".equals(name))
        {
            //get subrbias
            int bias = 0;
            int nSubrs = globalSubrIndex.getCount();
            
            if (nSubrs < 1240)
            {
                bias = 107;
            }
            else if (nSubrs < 33900)
            {
                bias = 1131;
            }
            else 
            {
                bias = 32768;
            }
            List<Integer> result = null;
            int subrNumber = bias+numbers.get(numbers.size()-1);
            if (subrNumber < nSubrs)
            {
                Type2CharStringParser parser = new Type2CharStringParser();
                byte[] bytes = globalSubrIndex.getBytes(subrNumber);
                List<Object> parsed = null;
                try {
                    parsed = parser.parse(bytes);
                    parsed.addAll(0,numbers.subList(0, numbers.size()-1));
                    result = handleSequence(parsed);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
            return result;
        }
        else
        {
            addCommand(numbers, command);
        }
        return null;
    }

    private List<Integer> clearStack(List<Integer> numbers, boolean flag)
    {

        if (sequence.size() == 0)
        {
            if (flag)
            {
                addCommand(Arrays.asList(Integer.valueOf(0), Integer
                        .valueOf(numbers.get(0).intValue() + nominalWidthX)),
                        new CharStringCommand(13));

                numbers = numbers.subList(1, numbers.size());
            } 
            else
            {
                addCommand(Arrays.asList(Integer.valueOf(0), Integer
                        .valueOf(defaultWidthX)), new CharStringCommand(13));
            }
        }

        return numbers;
    }

    private void expandStemHints(List<Integer> numbers, boolean horizontal)
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
        CharStringCommand command = pathCount > 0 ? (CharStringCommand) sequence
                .get(sequence.size() - 1)
                : null;

        CharStringCommand closepathCommand = new CharStringCommand(9);
        if (command != null && !closepathCommand.equals(command))
        {
            addCommand(Collections.<Integer> emptyList(), closepathCommand);
        }
    }

    private void drawAlternatingLine(List<Integer> numbers, boolean horizontal)
    {
        while (numbers.size() > 0)
        {
            addCommand(numbers.subList(0, 1), new CharStringCommand(
                    horizontal ? 6 : 7));
            numbers = numbers.subList(1, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void drawAlternatingCurve(List<Integer> numbers, boolean horizontal)
    {
        while (numbers.size() > 0)
        {
            boolean last = numbers.size() == 5;
            if (horizontal)
            {
                addCommand(Arrays.asList(numbers.get(0), Integer.valueOf(0),
                        numbers.get(1), numbers.get(2), last ? numbers.get(4)
                                : Integer.valueOf(0), numbers.get(3)),
                        new CharStringCommand(8));
            } 
            else
            {
                addCommand(Arrays.asList(Integer.valueOf(0), numbers.get(0),
                        numbers.get(1), numbers.get(2), numbers.get(3),
                        last ? numbers.get(4) : Integer.valueOf(0)),
                        new CharStringCommand(8));
            }
            numbers = numbers.subList(last ? 5 : 4, numbers.size());
            horizontal = !horizontal;
        }
    }

    private void drawCurve(List<Integer> numbers, boolean horizontal)
    {
        while (numbers.size() > 0)
        {
            boolean first = numbers.size() % 4 == 1;

            if (horizontal)
            {
                addCommand(Arrays.asList(numbers.get(first ? 1 : 0),
                        first ? numbers.get(0) : Integer.valueOf(0), numbers
                                .get(first ? 2 : 1),
                        numbers.get(first ? 3 : 2), numbers.get(first ? 4 : 3),
                        Integer.valueOf(0)), new CharStringCommand(8));
            } 
            else
            {
                addCommand(Arrays.asList(first ? numbers.get(0) : Integer
                        .valueOf(0), numbers.get(first ? 1 : 0), numbers
                        .get(first ? 2 : 1), numbers.get(first ? 3 : 2),
                        Integer.valueOf(0), numbers.get(first ? 4 : 3)),
                        new CharStringCommand(8));
            }
            numbers = numbers.subList(first ? 5 : 4, numbers.size());
        }
    }

    private void addCommandList(List<List<Integer>> numbers,
            CharStringCommand command)
    {
        for (int i = 0; i < numbers.size(); i++)
        {
            addCommand(numbers.get(i), command);
        }
    }

    private void addCommand(List<Integer> numbers, CharStringCommand command)
    {
        sequence.addAll(numbers);
        sequence.add(command);
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