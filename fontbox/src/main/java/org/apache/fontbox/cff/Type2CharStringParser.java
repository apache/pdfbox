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

    private DataInput input = null;
    private int hstemCount = 0;
    private int vstemCount = 0;
    private List<Object> sequence = null;

    /**
     * The given byte array will be parsed and converted to a Type2 sequence.
     * @param bytes the given mapping as byte array
     * @return the Type2 sequence
     * @throws IOException if an error occurs during reading
     */
    public List<Object> parse(byte[] bytes) throws IOException
    {
        input = new DataInput(bytes);

        hstemCount = 0;
        vstemCount = 0;

        sequence = new ArrayList<Object>();

        while (input.hasRemaining())
        {
            int b0 = input.readUnsignedByte();

            if (b0 >= 0 && b0 <= 27)
            {
                sequence.add(readCommand(b0));
            } 
            else if (b0 == 28)
            {
                sequence.add(readNumber(b0));
            } 
            else if (b0 >= 29 && b0 <= 31)
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

    private Integer readNumber(int b0) throws IOException
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
            int b3 = input.readUnsignedByte();
            int b4 = input.readUnsignedByte();

            // The lower bytes are representing the digits after 
            // the decimal point and aren't needed in this context
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
