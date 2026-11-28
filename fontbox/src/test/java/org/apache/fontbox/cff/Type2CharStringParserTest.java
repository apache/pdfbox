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

import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.function.ThrowingRunnable;

import junit.framework.TestCase;

public class Type2CharStringParserTest extends TestCase
{
    private static final int CALLSUBR = 10;
    private static final int RETURN = 11;
    private static final int ENDCHAR = 14;
    private static final int RMOVETO = 21;
    private static final int CALLGSUBR = 29;
    // operand 100
    private static final int NUMBER_100 = 239;
    // operand -107, which is subroutine 0 in an index with less than 1240 subroutines
    private static final int SUBR_0 = 32;
    // largest subroutine number with a single byte operand encoding
    private static final int MAX_SINGLE_BYTE_SUBR = 214;

    private CharStringCommand COMMAND_RMOVETO = new CharStringCommand(RMOVETO);
    private CharStringCommand COMMAND_ENDCHAR = new CharStringCommand(ENDCHAR);

    public void testFixedPointOperand() throws IOException
    {
        // 255 followed by a 16.16 fixed point number: 0x00018000 = 1.5, 0xFFFE8000 = -1.5
        byte[] charString = { (byte) 255, 0x00, 0x01, (byte) 0x80, 0x00,
                (byte) 255, (byte) 0xFF, (byte) 0xFE, (byte) 0x80, 0x00, RMOVETO, ENDCHAR };

        List<Object> sequence = new Type2CharStringParser("test", "test").parse(charString, new byte[0][], null);

        assertEquals(1.5, ((Number) sequence.get(0)).doubleValue());
        assertEquals(-1.5, ((Number) sequence.get(1)).doubleValue());
    }

    public void testLocalSubrCallingItself()
    {
        final byte[] loop = { SUBR_0, CALLSUBR };
        final byte[][] localSubrIndex = { loop };
        final Type2CharStringParser parser = new Type2CharStringParser("test", null);

        assertThrows(IOException.class, new ThrowingRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                parser.parse(loop, new byte[0][], localSubrIndex);
            }
        });
    }

    public void testGlobalSubrCallingItself()
    {
        final byte[] loop = { SUBR_0, CALLGSUBR };
        final byte[][] globalSubrIndex = { loop };
        final Type2CharStringParser parser = new Type2CharStringParser("test", null);

        assertThrows(IOException.class, new ThrowingRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                parser.parse(loop, globalSubrIndex, null);
            }
        });
    }

    public void testSubrsCallingEachOther()
    {
        final byte[] first = { SUBR_0 + 1, CALLSUBR };
        byte[] second = { SUBR_0, CALLSUBR };
        final byte[][] localSubrIndex = { first, second };
        final Type2CharStringParser parser = new Type2CharStringParser("test", null);

        assertThrows(IOException.class, new ThrowingRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                parser.parse(first, new byte[0][], localSubrIndex);
            }
        });
    }

    public void testSubrsNestedUpToLimit() throws IOException
    {
        byte[][] localSubrIndex = createSubrChain(Type2CharStringParser.MAX_SUBR_NESTING);
        byte[] charString = { SUBR_0, CALLSUBR, ENDCHAR };
        Type2CharStringParser parser = new Type2CharStringParser("test", null);
        List<Object> sequence = parser.parse(charString, new byte[0][], localSubrIndex);

        assertEquals(Arrays.asList(100, 100, COMMAND_RMOVETO, COMMAND_ENDCHAR), sequence);
    }

    public void testSubrsNestedBeyondLimit()
    {
        final byte[][] localSubrIndex = createSubrChain(Type2CharStringParser.MAX_SUBR_NESTING + 1);
        final byte[] charString = { SUBR_0, CALLSUBR, ENDCHAR };
        final Type2CharStringParser parser = new Type2CharStringParser("test", null);

        assertThrows(IOException.class, new ThrowingRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                parser.parse(charString, new byte[0][], localSubrIndex);
            }
        });
    }

    /**
     * Creates subroutines where each one calls the next one and the last one contains the path.
     */
    private static byte[][] createSubrChain(int length)
    {
        // the subroutine numbers must fit the single byte operand encoding
        assertTrue(length - 1 <= MAX_SINGLE_BYTE_SUBR);
        byte[][] subrs = new byte[length][];
        for (int i = 1; i < length; i++)
        {
            subrs[i - 1] = new byte[] { (byte) (SUBR_0 + i), CALLSUBR, RETURN };
        }
        subrs[length - 1] = new byte[] { (byte) NUMBER_100, (byte) NUMBER_100, RMOVETO, RETURN };
        return subrs;
    }
}
