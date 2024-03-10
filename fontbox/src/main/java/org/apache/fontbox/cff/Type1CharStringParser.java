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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.fontbox.cff.CharStringCommand.Type1KeyWord;

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
    private static final Logger LOG = LogManager.getLogger(Type1CharStringParser.class);

    // 1-byte commands
    private static final int CALLSUBR = 10;

    // 2-byte commands
    private static final int TWO_BYTE = 12;
    private static final int CALLOTHERSUBR = 16;
    private static final int POP = 17;

    private final String fontName;
    private String currentGlyph;

    /**
     * Constructs a new Type1CharStringParser object.
     *
     * @param fontName font name
     */
    public Type1CharStringParser(String fontName)
    {
        this.fontName = fontName;
    }

    /**
     * The given byte array will be parsed and converted to a Type1 sequence.
     *
     * @param bytes the given mapping as byte array
     * @param subrs list of local subroutines
     * @param glyphName name of the current glyph
     * @return the Type1 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes, List<byte[]> subrs, String glyphName) throws IOException
    {
        currentGlyph = glyphName;
        return parse(bytes, subrs, new ArrayList<>());
    }

    private List<Object> parse(byte[] bytes, List<byte[]> subrs, List<Object> sequence)
            throws IOException
    {
        DataInput input = new DataInputByteArray(bytes);
        while (input.hasRemaining())
        {
            int b0 = input.readUnsignedByte();
            if (b0 == CALLSUBR)
            {
                processCallSubr(subrs, sequence);
            }
            else if (b0 == TWO_BYTE && input.peekUnsignedByte(0) == CALLOTHERSUBR)
            {
                processCallOtherSubr(input, sequence);
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

    private void processCallSubr(List<byte[]> subrs, List<Object> sequence) throws IOException
    {
        // callsubr command
        Object obj = sequence.remove(sequence.size() - 1);
        if (!(obj instanceof Integer))
        {
            LOG.warn(
                    "Parameter {} for CALLSUBR is ignored, integer expected in glyph '{}' of font {}",
                    obj, currentGlyph, fontName);
            return;
        }
        int operand = (int) obj;

        if (operand >= 0 && operand < subrs.size())
        {
            byte[] subrBytes = subrs.get(operand);
            parse(subrBytes, subrs, sequence);
            Object lastItem = sequence.get(sequence.size() - 1);
            if (lastItem instanceof CharStringCommand
                    && Type1KeyWord.RET == ((CharStringCommand) lastItem).getType1KeyWord())
            {
                sequence.remove(sequence.size() - 1); // remove "return" command
            }
        }
        else
        {
            LOG.warn("CALLSUBR is ignored, operand: {}, subrs.size(): {} in glyph '{}' of font {}",
                    operand, subrs.size(), currentGlyph, fontName);
            // remove all parameters (there can be more than one)
            while (sequence.get(sequence.size() - 1) instanceof Integer)
            {
                sequence.remove(sequence.size() - 1);
            }
        }
    }

    private void processCallOtherSubr(DataInput input, List<Object> sequence) throws IOException
    {
        // callothersubr command (needed in order to expand Subrs)
        input.readByte();

        Integer othersubrNum = (Integer) sequence.remove(sequence.size() - 1);
        Integer numArgs = (Integer) sequence.remove(sequence.size() - 1);

        // othersubrs 0-3 have their own semantics
        Deque<Integer> results = new ArrayDeque<>();
        switch (othersubrNum)
        {
        case 0:
            results.push(removeInteger(sequence));
            results.push(removeInteger(sequence));
            sequence.remove(sequence.size() - 1);
            // end flex
            sequence.add(0);
            sequence.add(CharStringCommand.CALLOTHERSUBR);
            break;
        case 1:
            // begin flex
            sequence.add(1);
            sequence.add(CharStringCommand.CALLOTHERSUBR);
            break;
        case 3:
            // allows hint replacement
            results.push(removeInteger(sequence));
            break;
        default:
            // all remaining othersubrs use this fallback mechanism
            for (int i = 0; i < numArgs; i++)
            {
                results.push(removeInteger(sequence));
            }
            break;
        }

        // pop must follow immediately
        while (input.peekUnsignedByte(0) == TWO_BYTE && input.peekUnsignedByte(1) == POP)
        {
            input.readByte(); // B0_POP
            input.readByte(); // B1_POP
            sequence.add(results.pop());
        }

        if (!results.isEmpty())
        {
            LOG.warn("Value left on the PostScript stack in glyph {} of font {}", currentGlyph,
                    fontName);
        }
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
        if (Type1KeyWord.DIV == command.getType1KeyWord())
        {
            int a = (Integer) sequence.remove(sequence.size() - 1);
            int b = (Integer) sequence.remove(sequence.size() - 1);
            return b / a;
        }
        throw new IOException("Unexpected char string command: " + command.getType1KeyWord());
    }

    private CharStringCommand readCommand(DataInput input, int b0) throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return CharStringCommand.getInstance(b0, b1);
        }
        return CharStringCommand.getInstance(b0);
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
