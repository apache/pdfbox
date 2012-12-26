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
import java.util.List;

/**
 * This class represents a converter for a mapping into a Type2-sequence.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class Type2CharStringParser
{

    private int hstemCount = 0;
    private int vstemCount = 0;
    private List<Object> sequence = null;

    /**
     * The given byte array will be parsed and converted to a Type2 sequence.
     * @param bytes the given mapping as byte array
     * @param globalSubrIndex index containing all global subroutines
     * @param localSubrIndex index containing all local subroutines
     * 
     * @return the Type2 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes, IndexData globalSubrIndex, IndexData localSubrIndex) throws IOException
    {
    	return parse(bytes, globalSubrIndex, localSubrIndex, true);
    }
    
    private List<Object> parse(byte[] bytes, IndexData globalSubrIndex, IndexData localSubrIndex, boolean init) throws IOException
    {
        if (init) 
        {
	        hstemCount = 0;
	        vstemCount = 0;
	        sequence = new ArrayList<Object>();
        }
        DataInput input = new DataInput(bytes);
        boolean localSubroutineIndexProvided = localSubrIndex != null && localSubrIndex.getCount() > 0;
        boolean globalSubroutineIndexProvided = globalSubrIndex != null && globalSubrIndex.getCount() > 0;

        while (input.hasRemaining())
        {
            int b0 = input.readUnsignedByte();
            if (b0 == 10 && localSubroutineIndexProvided) 
            { // process subr command
            	Integer operand=(Integer)sequence.remove(sequence.size()-1);
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
                int subrNumber = bias+operand;
                if (subrNumber < localSubrIndex.getCount())
                {
                    byte[] subrBytes = localSubrIndex.getBytes(subrNumber);
                    parse(subrBytes, globalSubrIndex, localSubrIndex, false);
                    Object lastItem=sequence.get(sequence.size()-1);
                    if (lastItem instanceof CharStringCommand && ((CharStringCommand)lastItem).getKey().getValue()[0] == 11)
                    {
                        sequence.remove(sequence.size()-1); // remove "return" command
                    }
                }
            	
            } 
            else if (b0 == 29 && globalSubroutineIndexProvided) 
            { // process globalsubr command
                Integer operand=(Integer)sequence.remove(sequence.size()-1);
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
                
                int subrNumber = bias+operand;
                if (subrNumber < globalSubrIndex.getCount())
                {
                    byte[] subrBytes = globalSubrIndex.getBytes(subrNumber);
                    parse(subrBytes, globalSubrIndex, localSubrIndex, false);
                    Object lastItem=sequence.get(sequence.size()-1);
                    if (lastItem instanceof CharStringCommand && ((CharStringCommand)lastItem).getKey().getValue()[0]==11) 
                    {
                        sequence.remove(sequence.size()-1); // remove "return" command
                    }
                }
                	
            } 
            else if (b0 >= 0 && b0 <= 27)
            {
                sequence.add(readCommand(b0, input));
            } 
            else if (b0 == 28)
            {
                sequence.add(readNumber(b0, input));
            } 
            else if (b0 >= 29 && b0 <= 31)
            {
                sequence.add(readCommand(b0, input));
            } 
            else if (b0 >= 32 && b0 <= 255)
            {
                sequence.add(readNumber(b0, input));
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        return sequence;
    }

    private CharStringCommand readCommand(int b0, DataInput input) throws IOException
    {

        if (b0 == 1 || b0 == 18)
        {
            hstemCount += peekNumbers().size() / 2;
        } 
        else if (b0 == 3 || b0 == 19 || b0 == 20 || b0 == 23)
        {
            vstemCount += peekNumbers().size() / 2;
        } // End if

        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();

            return new CharStringCommand(b0, b1);
        } 
        else if (b0 == 19 || b0 == 20)
        {
            int[] value = new int[1 + getMaskLength()];
            value[0] = b0;

            for (int i = 1; i < value.length; i++)
            {
                value[i] = input.readUnsignedByte();
            }

            return new CharStringCommand(value);
        }

        return new CharStringCommand(b0);
    }

    private Integer readNumber(int b0, DataInput input) throws IOException
    {

        if (b0 == 28)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();

            return Integer.valueOf((short) (b1 << 8 | b2));
        } 
        else if (b0 >= 32 && b0 <= 246)
        {
            return Integer.valueOf(b0 - 139);
        } 
        else if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();

            return Integer.valueOf((b0 - 247) * 256 + b1 + 108);
        } 
        else if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();

            return Integer.valueOf(-(b0 - 251) * 256 - b1 - 108);
        } 
        else if (b0 == 255)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();
            // The lower bytes are representing the digits after 
            // the decimal point and aren't needed in this context
            input.readUnsignedByte();
            input.readUnsignedByte();
            return Integer.valueOf((short)(b1 << 8 | b2));
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private int getMaskLength()
    {
        int length = 1;

        int hintCount = hstemCount + vstemCount;
        while ((hintCount -= 8) > 0)
        {
            length++;
        }

        return length;
    }

    private List<Number> peekNumbers()
    {
        List<Number> numbers = new ArrayList<Number>();

        for (int i = sequence.size() - 1; i > -1; i--)
        {
            Object object = sequence.get(i);

            if (object instanceof Number)
            {
                Number number = (Number) object;

                numbers.add(0, number);

                continue;
            }

            return numbers;
        }

        return numbers;
    }
}
