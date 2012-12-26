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
 * This class represents a converter for a mapping into a Type1-sequence.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class Type1CharStringParser
{

    private DataInput input = null;
    private List<Object> sequence = null;

    /**
     * The given byte array will be parsed and converted to a Type1 sequence.
     * @param bytes the given mapping as byte array
     * @param localSubrIndex index containing all local subroutines
     * 
     * @return the Type1 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes, IndexData localSubrIndex) throws IOException
    {
        return parse(bytes, localSubrIndex, true);
    }
    
    private List<Object> parse(byte[] bytes, IndexData localSubrIndex, boolean init) throws IOException
    {
        if (init) 
        {
            sequence = new ArrayList<Object>();
        }
        input = new DataInput(bytes);
        boolean localSubroutineIndexProvided = localSubrIndex != null && localSubrIndex.getCount() > 0;
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
                    parse(subrBytes, localSubrIndex, false);
                    Object lastItem = sequence.get(sequence.size()-1);
                    if (lastItem instanceof CharStringCommand && ((CharStringCommand)lastItem).getKey().getValue()[0] == 11)
                    {
                        sequence.remove(sequence.size()-1); // remove "return" command
                    }
                }
                
            } 
            else if (b0 >= 0 && b0 <= 31)
            {
                sequence.add(readCommand(b0));
            } 
            else if (b0 >= 32 && b0 <= 255)
            {
                sequence.add(readNumber(b0));
            } 
            else
            {
                throw new IllegalArgumentException();
            }
        }
        return sequence;
    }

    private CharStringCommand readCommand(int b0) throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return new CharStringCommand(b0, b1);
        }
        return new CharStringCommand(b0);
    }

    private Integer readNumber(int b0) throws IOException
    {
        if (b0 >= 32 && b0 <= 246)
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
            int b3 = input.readUnsignedByte();
            int b4 = input.readUnsignedByte();

            return Integer.valueOf(b1 << 24 | b2 << 16 | b3 << 8 | b4);
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }
}