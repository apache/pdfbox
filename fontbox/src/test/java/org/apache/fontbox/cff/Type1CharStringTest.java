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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;

/**
 * This class includes some test cases for the Type1CharStringFormatter and the Type1CharStringParser.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class Type1CharStringTest
{

    /**
     * Tests the encoding and decoding of a command sequence.
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void commandEncoding() throws IOException
    {
        List<Object> commands = createCommandSequence(new int[] { 0 },
                new int[] { 12, 0 }, new int[] { 31 });

        byte[] encodedCommands = new Type1CharStringFormatter().format(commands);
        List<Object> decodedCommands = new Type1CharStringParser().parse(encodedCommands);

        assertEquals(1 + 2 + 1, encodedCommands.length);

        assertEquals(commands, decodedCommands);
    }

    /**
     * Tests the encoding and decoding of a number sequence. 
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void numberEncoding() throws IOException
    {
        List<Object> numbers = createNumberSequence(-10000, -1131, -108, -107,
                0, 107, 108, 1131, 10000);

        byte[] encodedNumbers = new Type1CharStringFormatter().format(numbers);
        List<Object> decodedNumbers = new Type1CharStringParser()
                .parse(encodedNumbers);

        assertEquals(5 + 2 * 2 + 3 * 1 + 2 * 2 + 5, encodedNumbers.length);

        assertEquals(numbers, decodedNumbers);
    }

    private static List<Object> createCommandSequence(int[]... values)
    {
        List<Object> sequence = new ArrayList<Object>();

        for (int[] value : values)
        {
            sequence.add(value.length > 1 ? new CharStringCommand(value[0],
                    value[1]) : new CharStringCommand(value[0]));
        }
        return sequence;
    }

    private static List<Object> createNumberSequence(int... values)
    {
        List<Object> sequence = new ArrayList<Object>();

        for (int value : values)
        {
            sequence.add(Integer.valueOf(value));
        }

        return sequence;
    }
}