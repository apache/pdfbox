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

import org.apache.fontbox.cff.CharStringCommand.Type2KeyWord;

/**
 * This class represents a converter for a mapping into a Type2-sequence.
 * 
 * @author Villu Ruusmann
 */
public class Type2CharStringParser
{
    // 1-byte commands
    private static final int CALLSUBR = CharStringCommand.CALLSUBR.getValue();
    private static final int CALLGSUBR = CharStringCommand.CALLGSUBR.getValue();

    // not yet supported commands
    private static final int HINTMASK = CharStringCommand.HINTMASK.getValue();
    private static final int CNTRMASK = CharStringCommand.CNTRMASK.getValue();

    private final String fontName;

    /**
     * Constructs a new Type1CharStringParser object for a Type 1-equivalent font.
     *
     * @param fontName font name
     */
    public Type2CharStringParser(String fontName)
    {
        this.fontName = fontName;
    }

    /**
     * The given byte array will be parsed and converted to a Type2 sequence.
     * 
     * @param bytes the given mapping as byte array
     * @param globalSubrIndex array containing all global subroutines
     * @param localSubrIndex array containing all local subroutines
     * 
     * @return the Type2 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes, byte[][] globalSubrIndex, byte[][] localSubrIndex)
            throws IOException
    {
        GlyphData glyphData = new GlyphData();
        parseSequence(bytes, globalSubrIndex, localSubrIndex, glyphData);
        return glyphData.sequence;
    }

    private void parseSequence(byte[] bytes, byte[][] globalSubrIndex, byte[][] localSubrIndex,
            GlyphData glyphData) throws IOException
    {
        DataInput input = new DataInputByteArray(bytes);

        while (input.hasRemaining())
        {
            int b0 = input.readUnsignedByte();
            if (b0 == CALLSUBR)
            {
                processCallSubr(globalSubrIndex, localSubrIndex, glyphData);
            }
            else if (b0 == CALLGSUBR)
            {
                processCallGSubr(globalSubrIndex, localSubrIndex, glyphData);
            }
            else if (b0 == HINTMASK || b0 == CNTRMASK)
            {
                glyphData.vstemCount += countNumbers(glyphData.sequence) / 2;
                int maskLength = getMaskLength(glyphData.hstemCount, glyphData.vstemCount);
                // drop the following bytes representing the mask as long as we don't support HINTMASK and CNTRMASK
                for (int i = 0; i < maskLength; i++)
                {
                    input.readUnsignedByte();
                }
                glyphData.sequence.add(CharStringCommand.getInstance(b0));
            }
            else if ((b0 >= 0 && b0 <= 18) || (b0 >= 21 && b0 <= 27) || (b0 >= 29 && b0 <= 31))
            {
                glyphData.sequence.add(readCommand(b0, input, glyphData));
            }
            else if (b0 == 28 || (b0 >= 32 && b0 <= 255))
            {
                glyphData.sequence.add(readNumber(b0, input));
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    }

    private byte[] getSubrBytes(byte[][] subrIndex, GlyphData glyphData)
    {
        int subrNumber = calculateSubrNumber(
                (Integer) glyphData.sequence.remove(glyphData.sequence.size() - 1),
                subrIndex.length);
        return subrNumber < subrIndex.length ? subrIndex[subrNumber] : null;
    }

    private void processCallSubr(byte[][] globalSubrIndex, byte[][] localSubrIndex,
            GlyphData glyphData) throws IOException
    {
        if (localSubrIndex != null && localSubrIndex.length > 0)
        {
            byte[] subrBytes = getSubrBytes(localSubrIndex, glyphData);
            processSubr(globalSubrIndex, localSubrIndex, subrBytes, glyphData);
        }
    }

    private void processCallGSubr(byte[][] globalSubrIndex, byte[][] localSubrIndex,
            GlyphData glyphData) throws IOException
    {
        if (globalSubrIndex != null && globalSubrIndex.length > 0)
        {
            byte[] subrBytes = getSubrBytes(globalSubrIndex, glyphData);
            processSubr(globalSubrIndex, localSubrIndex, subrBytes, glyphData);
        }
    }

    private void processSubr(byte[][] globalSubrIndex, byte[][] localSubrIndex, byte[] subrBytes,
            GlyphData glyphData) throws IOException
    {
        parseSequence(subrBytes, globalSubrIndex, localSubrIndex, glyphData);
        Object lastItem = glyphData.sequence.get(glyphData.sequence.size() - 1);
        if (lastItem instanceof CharStringCommand
                && Type2KeyWord.RET == ((CharStringCommand) lastItem).getType2KeyWord())
        {
            // remove "return" command
            glyphData.sequence.remove(glyphData.sequence.size() - 1);
        }
    }

    private int calculateSubrNumber(int operand, int subrIndexlength)
    {
        if (subrIndexlength < 1240)
        {
            return 107 + operand;
        }
        if (subrIndexlength < 33900)
        {
            return 1131 + operand;
        }
        return 32768 + operand;
    }

    private CharStringCommand readCommand(int b0, DataInput input, GlyphData glyphData)
            throws IOException
    {
        switch (b0)
        {
        case 1:
        case 18:
            glyphData.hstemCount += countNumbers(glyphData.sequence) / 2;
            return CharStringCommand.getInstance(b0);
        case 3:
        case 23:
            glyphData.vstemCount += countNumbers(glyphData.sequence) / 2;
            return CharStringCommand.getInstance(b0);
        case 12:
            return CharStringCommand.getInstance(b0, input.readUnsignedByte());
        default:
            return CharStringCommand.getInstance(b0);
        }
    }

    private Number readNumber(int b0, DataInput input) throws IOException
    {
        if (b0 == 28)
        {
            return (int) input.readShort();
        }
        if (b0 >= 32 && b0 <= 246)
        {
            return b0 - 139;
        }
        if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();
            return (b0 - 247) * 256 + b1 + 108;
        }
        if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();
            return -(b0 - 251) * 256 - b1 - 108;
        }
        if (b0 == 255)
        {
            short value = input.readShort();
            // The lower bytes are representing the digits after the decimal point
            double fraction = input.readUnsignedShort() / 65535d;
            return value + fraction;
        }
        throw new IllegalArgumentException();
    }

    private int getMaskLength(int hstemCount, int vstemCount)
    {
        int hintCount = hstemCount + vstemCount;
        int length = hintCount / 8;
        if (hintCount % 8 > 0)
        {
            length++;
        }
        return length;
    }

    private int countNumbers(List<Object> sequence)
    {
        int count = 0;
        for (int i = sequence.size() - 1; i > -1; i--)
        {
            if (!(sequence.get(i) instanceof Number))
            {
                return count;
            }
            count++;
        }
        return count;
    }

    @Override
    public String toString()
    {
        return fontName;
    }

    private class GlyphData
    {
        final List<Object> sequence = new ArrayList<>();
        int hstemCount = 0;
        int vstemCount = 0;

        private GlyphData()
        {
        }
    }
}
