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
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a converter for a mapping into a Type 1 sequence.
 *
 * @see "Adobe Type 1 Font Format, Adobe Systems (1999)"
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class Type1CharStringParser
{
    private static final Log LOG = LogFactory.getLog(Type1CharStringParser.class);

    // 1-byte commands
    static final int RETURN = 11;
    static final int CALLSUBR = 10;

    // 2-byte commands
    static final int TWO_BYTE = 12;
    static final int CALLOTHERSUBR = 16;
    static final int POP = 17;

    private final String fontName, glyphName;

    /**
     * Constructs a new Type1CharStringParser object.
     *
     * @param fontName font name
     * @param glyphName glyph name
     */
    public Type1CharStringParser(String fontName, String glyphName)
    {
        this.fontName = fontName;
        this.glyphName = glyphName;
    }

    /**
     * The given byte array will be parsed and converted to a Type1 sequence.
     *
     * @param bytes the given mapping as byte array
     * @param subrs list of local subroutines
     * @return the Type1 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes, List<byte[]> subrs) throws IOException
    {
        return parse(bytes, subrs, new ArrayList<Object>());
    }

    private List<Object> parse(byte[] bytes, List<byte[]> subrs, List<Object> sequence) throws IOException
    {
        DataInput input = new DataInput(bytes);
        while (input.hasRemaining())
        {
            int b0 = input.readUnsignedByte();
            if (b0 == CALLSUBR)
            {
                // callsubr command
                Object obj = sequence.remove(sequence.size() - 1);
                if (!(obj instanceof Integer))
                {
                    LOG.warn("Parameter " + obj + " for CALLSUBR is ignored, integer expected in glyph '"
                            + glyphName + "' of font " + fontName);
                    continue;
                }
                Integer operand = (Integer) obj;

                if (operand >= 0 && operand < subrs.size())
                {
                    byte[] subrBytes = subrs.get(operand);
                    parse(subrBytes, subrs, sequence);
                    Object lastItem = sequence.get(sequence.size()-1);
                    if (lastItem instanceof CharStringCommand &&
                          ((CharStringCommand)lastItem).getKey().getValue()[0] == RETURN)
                    {
                        sequence.remove(sequence.size()-1); // remove "return" command
                    }
                }
            }
            else if (b0 == TWO_BYTE && input.peekUnsignedByte(0) == CALLOTHERSUBR)
            {
                // callothersubr command (needed in order to expand Subrs)
                input.readByte();

                Integer othersubrNum = (Integer)sequence.remove(sequence.size()-1);
                Integer numArgs = (Integer)sequence.remove(sequence.size()-1);

                // othersubrs 0-3 have their own semantics
                Stack<Integer> results = new Stack<Integer>();
                if (othersubrNum == 0)
                {
                    results.push(removeInteger(sequence));
                    results.push(removeInteger(sequence));
                    sequence.remove(sequence.size() - 1);
                    // end flex
                    sequence.add(0);
                    sequence.add(new CharStringCommand(TWO_BYTE, CALLOTHERSUBR));
                }
                else if (othersubrNum == 1)
                {
                    // begin flex
                    sequence.add(1);
                    sequence.add(new CharStringCommand(TWO_BYTE, CALLOTHERSUBR));
                }
                else if (othersubrNum == 3)
                {
                    // allows hint replacement
                    results.push(removeInteger(sequence));
                }
                else
                {
                    // all remaining othersubrs use this fallback mechanism
                    for (int i = 0; i < numArgs; i++)
                    {
                        results.push(removeInteger(sequence));
                    }
                }

                // pop must follow immediately
                while (input.peekUnsignedByte(0) == TWO_BYTE && input.peekUnsignedByte(1) == POP)
                {
                    input.readByte(); // B0_POP
                    input.readByte(); // B1_POP
                    sequence.add(results.pop());
                }

                if (results.size() > 0)
                {
                    LOG.warn("Value left on the PostScript stack in glyph " + glyphName + " of font " + fontName);
                }
            }
            else if (b0 >= 0 && b0 <= 31)
            {
                sequence.add(readCommand(input, b0));
            } 
            else if (b0 >= 32 && b0 <= 255)
            {
                sequence.add(readNumber(input, b0));
            } 
            else
            {
                throw new IllegalArgumentException();
            }
        }
        return sequence;
    }

    // this method is a workaround for the fact that Type1CharStringParser assumes that subrs and
    // othersubrs can be unrolled without executing the 'div' operator, which isn't true
    private static Integer removeInteger(List<Object> sequence) throws IOException
    {
        Object item = sequence.remove(sequence.size() - 1);
        if (item instanceof Integer)
        {
            return (Integer)item;
        }
        CharStringCommand command = (CharStringCommand) item;

        // div
        if (command.getKey().getValue()[0] == 12 && command.getKey().getValue()[1] == 12)
        {
            int a = (Integer) sequence.remove(sequence.size() - 1);
            int b = (Integer) sequence.remove(sequence.size() - 1);
            return b / a;
        }
        throw new IOException("Unexpected char string command: " + command.getKey());
    }

    private CharStringCommand readCommand(DataInput input, int b0) throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return new CharStringCommand(b0, b1);
        }
        return new CharStringCommand(b0);
    }

    private Integer readNumber(DataInput input, int b0) throws IOException
    {
        if (b0 >= 32 && b0 <= 246)
        {
            return b0 - 139;
        } 
        else if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();
            return (b0 - 247) * 256 + b1 + 108;
        } 
        else if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();
            return -(b0 - 251) * 256 - b1 - 108;
        } 
        else if (b0 == 255)
        {
            return input.readInt();
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
