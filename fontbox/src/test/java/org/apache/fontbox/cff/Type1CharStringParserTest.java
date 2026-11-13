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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class Type1CharStringParserTest
{
    private static final int CALLSUBR = 10;
    private static final int RETURN = 11;
    private static final int ENDCHAR = 14;
    private static final int RMOVETO = 21;
    // operand 0
    private static final int NUMBER_0 = 139;
    // operand 100
    private static final int NUMBER_100 = 239;
    // largest operand with a single byte encoding
    private static final int MAX_SINGLE_BYTE_NUMBER = 107;

    @Test
    void testSubrCallingItself()
    {
        byte[] loop = { (byte) NUMBER_0, CALLSUBR };
        List<byte[]> subrs = Collections.singletonList(loop);
        Type1CharStringParser parser = new Type1CharStringParser("test");

        assertThrows(IOException.class, () -> parser.parse(loop, subrs, ".notdef"));
    }

    @Test
    void testSubrsCallingEachOther()
    {
        byte[] first = { (byte) (NUMBER_0 + 1), CALLSUBR };
        byte[] second = { (byte) NUMBER_0, CALLSUBR };
        List<byte[]> subrs = Arrays.asList(first, second);
        Type1CharStringParser parser = new Type1CharStringParser("test");

        assertThrows(IOException.class, () -> parser.parse(first, subrs, ".notdef"));
    }

    @Test
    void testSubrsNestedUpToLimit() throws IOException
    {
        List<byte[]> subrs = createSubrChain(Type1CharStringParser.MAX_SUBR_NESTING);
        byte[] charString = { (byte) NUMBER_0, CALLSUBR, ENDCHAR };

        List<Object> sequence = new Type1CharStringParser("test").parse(charString, subrs, "a");

        assertEquals(Arrays.asList(100, 100, CharStringCommand.RMOVETO, CharStringCommand.ENDCHAR),
                sequence);
    }

    @Test
    void testSubrsNestedBeyondLimit()
    {
        List<byte[]> subrs = createSubrChain(Type1CharStringParser.MAX_SUBR_NESTING + 1);
        byte[] charString = { (byte) NUMBER_0, CALLSUBR, ENDCHAR };
        Type1CharStringParser parser = new Type1CharStringParser("test");

        assertThrows(IOException.class, () -> parser.parse(charString, subrs, "a"));
    }

    /**
     * Creates subroutines where each one calls the next one and the last one contains the path.
     */
    private static List<byte[]> createSubrChain(int length)
    {
        // the subroutine numbers must fit the single byte operand encoding
        assertTrue(length - 1 <= MAX_SINGLE_BYTE_NUMBER);
        List<byte[]> subrs = new ArrayList<>();
        for (int i = 1; i < length; i++)
        {
            subrs.add(new byte[] { (byte) (NUMBER_0 + i), CALLSUBR, RETURN });
        }
        subrs.add(new byte[] { (byte) NUMBER_100, (byte) NUMBER_100, RMOVETO, RETURN });
        return subrs;
    }
}
