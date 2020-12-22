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
import java.util.Locale;

import org.apache.fontbox.cff.CharStringCommand.Type2KeyWord;

/**
 * This class represents a converter for a mapping into a Type2-sequence.
 * @author Villu Ruusmann
 */
public class Type2CharStringParser
{

    // 1-byte commands
    private static final int CALLSUBR = 10;
    private static final int CALLGSUBR = 29;

    private int hstemCount = 0;
    private int vstemCount = 0;
    private List<Object> sequence = null;
    @SuppressWarnings("unused")
    private final String fontName;
    @SuppressWarnings("unused")
    private final String glyphName;

    /**
     * Constructs a new Type1CharStringParser object for a Type 1-equivalent font.
     *
     * @param fontName font name
     * @param glyphName glyph name
     */
    public Type2CharStringParser(final String fontName, final String glyphName)
    {
        this.fontName = fontName;
        this.glyphName = glyphName;
    }

    /**
     * Constructs a new Type1CharStringParser object for a CID-Keyed font.
     *
     * @param fontName font name
     * @param cid CID
     */
    public Type2CharStringParser(final String fontName, final int cid)
    {
        this.fontName = fontName;
        this.glyphName = String.format(Locale.US, "%04x", cid); // for debugging only
    }

    /**
     * The given byte array will be parsed and converted to a Type2 sequence.
     * @param bytes the given mapping as byte array
     * @param globalSubrIndex array containing all global subroutines
     * @param localSubrIndex array containing all local subroutines
     * 
     * @return the Type2 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(final byte[] bytes, final byte[][] globalSubrIndex, final byte[][] localSubrIndex) throws IOException
    {
        return parse(bytes, globalSubrIndex, localSubrIndex, true);
    }
    
    private List<Object> parse(final byte[] bytes, final byte[][] globalSubrIndex, final byte[][] localSubrIndex, final boolean init) throws IOException
    {
        if (init) 
        {
            hstemCount = 0;
            vstemCount = 0;
            sequence = new ArrayList<>();
        }
        final DataInput input = new DataInput(bytes);
        final boolean localSubroutineIndexProvided = localSubrIndex != null && localSubrIndex.length > 0;
        final boolean globalSubroutineIndexProvided = globalSubrIndex != null && globalSubrIndex.length > 0;

        while (input.hasRemaining())
        {
            final int b0 = input.readUnsignedByte();
            if (b0 == CALLSUBR && localSubroutineIndexProvided)
            {
                processCallSubr(globalSubrIndex, localSubrIndex);
            } 
            else if (b0 == CALLGSUBR && globalSubroutineIndexProvided)
            {
                processCallGSubr(globalSubrIndex, localSubrIndex);
            } 
            else if ( (b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31))
            {
                sequence.add(readCommand(b0, input));
            } 
            else if (b0 == 28 || (b0 >= 32 && b0 <= 255))
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

    private void processCallSubr(final byte[][] globalSubrIndex, final byte[][] localSubrIndex)
            throws IOException
    {
        final Integer operand = (Integer) sequence.remove(sequence.size() - 1);
        // get subrbias
        int bias = 0;
        final int nSubrs = localSubrIndex.length;

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
        final int subrNumber = bias + operand;
        if (subrNumber < localSubrIndex.length)
        {
            final byte[] subrBytes = localSubrIndex[subrNumber];
            parse(subrBytes, globalSubrIndex, localSubrIndex, false);
            final Object lastItem = sequence.get(sequence.size() - 1);
            if (lastItem instanceof CharStringCommand
                    && Type2KeyWord.RET == ((CharStringCommand) lastItem).getType2KeyWord())
            {
                sequence.remove(sequence.size() - 1); // remove "return" command
            }
        }
    }

    private void processCallGSubr(final byte[][] globalSubrIndex, final byte[][] localSubrIndex)
            throws IOException
    {
        final Integer operand = (Integer) sequence.remove(sequence.size() - 1);
        // get subrbias
        final int bias;
        final int nSubrs = globalSubrIndex.length;

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

        final int subrNumber = bias + operand;
        if (subrNumber < globalSubrIndex.length)
        {
            final byte[] subrBytes = globalSubrIndex[subrNumber];
            parse(subrBytes, globalSubrIndex, localSubrIndex, false);
            final Object lastItem = sequence.get(sequence.size() - 1);
            if (lastItem instanceof CharStringCommand
                    && Type2KeyWord.RET == ((CharStringCommand) lastItem).getType2KeyWord())
            {
                sequence.remove(sequence.size() - 1); // remove "return" command
            }
        }
    }

    private CharStringCommand readCommand(final int b0, final DataInput input) throws IOException
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
            final int b1 = input.readUnsignedByte();

            return new CharStringCommand(b0, b1);
        } 
        else if (b0 == 19 || b0 == 20)
        {
            final int[] value = new int[1 + getMaskLength()];
            value[0] = b0;

            for (int i = 1; i < value.length; i++)
            {
                value[i] = input.readUnsignedByte();
            }

            return new CharStringCommand(value);
        }

        return new CharStringCommand(b0);
    }

    private Number readNumber(final int b0, final DataInput input) throws IOException
    {
        if (b0 == 28)
        {
            return (int) input.readShort();
        } 
        else if (b0 >= 32 && b0 <= 246)
        {
            return b0 - 139;
        } 
        else if (b0 >= 247 && b0 <= 250)
        {
            final int b1 = input.readUnsignedByte();

            return (b0 - 247) * 256 + b1 + 108;
        } 
        else if (b0 >= 251 && b0 <= 254)
        {
            final int b1 = input.readUnsignedByte();

            return -(b0 - 251) * 256 - b1 - 108;
        }
        else if (b0 == 255)
        {
            final short value = input.readShort();
            // The lower bytes are representing the digits after the decimal point
            final double fraction = input.readUnsignedShort() / 65535d;
            return value + fraction;
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private int getMaskLength()
    {
        final int hintCount = hstemCount + vstemCount;
        int length = hintCount / 8; 
        if (hintCount % 8 > 0)
        {
            length++;
        }
        return length;
    }

    private List<Number> peekNumbers()
    {
        final List<Number> numbers = new ArrayList<>();
        for (int i = sequence.size() - 1; i > -1; i--)
        {
            final Object object = sequence.get(i);

            if (!(object instanceof Number))
            {
                return numbers;
            }
            numbers.add(0, (Number) object);
        }
        return numbers;
    }
}
