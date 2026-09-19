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
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Program-tier (Tier 2) tests: hand-assembled bytecode fed through the dispatch loop with no font.
 * These exercise the engine - dispatch, the push family, branching, function definition and calling,
 * and the {@link BytecodeStream} bounds checks - independently of any glyph.
 */
class TrueTypeInterpreterTest
{
    // opcodes used to assemble test programs
    private static final byte PUSHB1 = (byte) 0xB0; // PUSHB[0] - push one byte
    private static final byte PUSHB2 = (byte) 0xB1; // PUSHB[1] - push two bytes
    private static final byte NPUSHW = (byte) 0x41;
    private static final byte ADD = 0x60;
    private static final byte SUB = 0x61;
    private static final byte MUL = 0x63;
    private static final byte DUP = 0x20;
    private static final byte SWAP = 0x23;
    private static final byte DEPTH = 0x24;
    private static final byte ROLL = (byte) 0x8A;
    private static final byte CINDEX = 0x25;
    private static final byte MINDEX = 0x26;
    private static final byte NPUSHB = 0x40;
    private static final byte JROF = 0x79;
    private static final byte POP = 0x21;
    private static final byte GT = 0x52;
    private static final byte ODD = 0x56;
    private static final byte EVEN = 0x57;
    private static final byte AND = 0x5A;
    private static final byte OR = 0x5B;
    private static final byte NOT = 0x5C;
    private static final byte RTHG = 0x19;
    private static final byte RDTG = 0x7D;
    private static final byte ROFF = 0x7A;
    private static final byte IF = 0x58;
    private static final byte ELSE = 0x1B;
    private static final byte EIF = 0x59;
    private static final byte PUSHW1 = (byte) 0xB8; // PUSHW[0] - push one signed word
    private static final byte JMPR = 0x1C;
    private static final byte JROT = 0x78;
    private static final byte FDEF = 0x2C;
    private static final byte ENDF = 0x2D;
    private static final byte CALL = 0x2B;
    private static final byte LOOPCALL = 0x2A;
    private static final byte MPPEM = 0x4B;
    private static final byte WS = 0x42;
    private static final byte RS = 0x43;
    private static final byte SZPS = 0x16;
    private static final byte SCFS = 0x48;
    private static final byte GC = 0x46;

    private static TrueTypeInterpreter interpreter()
    {
        return new TrueTypeInterpreter(256, 16, 0, 2048);
    }

    private static int runTop(byte[] program)
    {
        ExecutionContext ctx = interpreter().executeProgram(program, 16);
        return ctx.peek(0);
    }

    /** Runs the program and returns the whole stack, bottom to top. */
    private static int[] runStack(byte[] program)
    {
        ExecutionContext ctx = interpreter().executeProgram(program, 16);
        int[] stack = new int[ctx.getStackDepth()];
        for (int i = 0; i < stack.length; i++)
        {
            stack[i] = ctx.peek(stack.length - 1 - i);
        }
        return stack;
    }

    @Test
    void testPushAndAdd()
    {
        // PUSHB[1] 2 3 ; ADD  ->  5
        assertEquals(5, runTop(new byte[] { PUSHB2, 2, 3, ADD }));
    }

    @Test
    void testNpushwSigned()
    {
        // NPUSHW 1 0xFFFF ; -> -1 on the stack
        assertEquals(-1, runTop(new byte[] { NPUSHW, 1, (byte) 0xFF, (byte) 0xFF }));
    }

    @Test
    void testArithmetic()
    {
        // 10 - 3 == 7
        assertEquals(7, runTop(new byte[] { PUSHB2, 10, 3, SUB }));
        // 64(=1.0) * 192(=3.0) == 192(=3.0) ... use F26Dot6: PUSHB 64, then need words; use small ints
        // 2.0 * 3.0 in F26Dot6: push 128 and 192 via NPUSHW
        assertEquals(Fixed.fromInt(6),
                runTop(new byte[] { NPUSHW, 2, 0, (byte) 128, 0, (byte) 192, MUL }));
    }

    @Test
    void testStackOps()
    {
        // DUP: push 7, dup, add -> 14
        assertEquals(14, runTop(new byte[] { PUSHB1, 7, DUP, ADD }));
        // SWAP then SUB: push 3,10 swap -> 10,3 ; SUB pops b=3,a=10 -> 7
        assertEquals(7, runTop(new byte[] { PUSHB2, 3, 10, SWAP, SUB }));
        // DEPTH after pushing three values -> 3
        assertEquals(3, runTop(new byte[] { PUSHB2, 1, 2, PUSHB1, 9, DEPTH }));
        // ROLL: 1 2 3 -> 2 3 1, top is 1
        assertEquals(1, runTop(new byte[] { PUSHB2, 1, 2, PUSHB1, 3, ROLL }));
    }

    /**
     * ROLL moves the third element to the top (FreeType's Ins_ROLL: [a b c] -> [b c a] with c on top
     * before and a on top after). Checks all three resulting positions, not just the top.
     */
    @Test
    void testRollFullPermutation()
    {
        // after ROLL the stack is b c a; pop a (top) with a SUB against 0 pushed... simpler: use
        // arithmetic that only gives the expected value for the expected order:
        // [1 2 3] ROLL -> [2 3 1]; SUB -> 3 - 1 = 2; SUB -> 2 - 2 = 0
        assertEquals(0, runTop(new byte[] { PUSHB2, 1, 2, PUSHB1, 3, ROLL, SUB, SUB }));
        // [5 7 9] ROLL -> [7 9 5]; SUB -> 9 - 5 = 4; SUB -> 7 - 4 = 3
        assertEquals(3, runTop(new byte[] { PUSHB2, 5, 7, PUSHB1, 9, ROLL, SUB, SUB }));
    }

    /**
     * ODD/EVEN round the operand with the <em>current round state</em> before testing the pixel's
     * parity (spec, and FreeType's Ins_ODD/Ins_EVEN), so the same value can be odd under one round
     * state and even under another. AND/OR/NOT are C truthiness on F26Dot6 values.
     */
    @Test
    void testOddEvenUseRoundState()
    {
        // 1.4 px (90) rounds to 1 px under round-to-grid: odd
        assertEquals(1, runTop(new byte[] { PUSHB1, 90, ODD }));
        assertEquals(0, runTop(new byte[] { PUSHB1, 90, EVEN }));
        // 1.6 px (102) rounds to 2 px: even
        assertEquals(0, runTop(new byte[] { PUSHB1, 102, ODD }));
        assertEquals(1, runTop(new byte[] { PUSHB1, 102, EVEN }));
        // round-down-to-grid: 1.6 px -> 1 px: odd
        assertEquals(1, runTop(new byte[] { RDTG, PUSHB1, 102, ODD }));
        // round-to-half-grid: 1.4 px -> 1.5 px, which is neither an odd nor an even pixel
        assertEquals(0, runTop(new byte[] { RTHG, PUSHB1, 90, ODD }));
        assertEquals(0, runTop(new byte[] { RTHG, PUSHB1, 90, EVEN }));
        // round off: the value is tested as-is; 64 is odd, 128 even, 90 neither
        assertEquals(1, runTop(new byte[] { ROFF, PUSHB1, 64, ODD }));
        assertEquals(1, runTop(new byte[] { ROFF, PUSHB1, (byte) 128, EVEN }));
        assertEquals(0, runTop(new byte[] { ROFF, PUSHB1, 90, ODD }));
        assertEquals(0, runTop(new byte[] { ROFF, PUSHB1, 90, EVEN }));
    }

    @Test
    void testAndOrNot()
    {
        assertEquals(1, runTop(new byte[] { PUSHB2, 3, 64, AND }));
        assertEquals(0, runTop(new byte[] { PUSHB2, 3, 0, AND }));
        assertEquals(1, runTop(new byte[] { PUSHB2, 0, 5, OR }));
        assertEquals(0, runTop(new byte[] { PUSHB2, 0, 0, OR }));
        assertEquals(1, runTop(new byte[] { PUSHB1, 0, NOT }));
        assertEquals(0, runTop(new byte[] { PUSHB1, 9, NOT }));
    }

    @Test
    void testIfElseTrueBranch()
    {
        // push 1 (true) ; IF push 10 ELSE push 20 EIF -> 10
        assertEquals(10, runTop(new byte[] { PUSHB1, 1, IF, PUSHB1, 10, ELSE, PUSHB1, 20, EIF }));
    }

    @Test
    void testIfElseFalseBranch()
    {
        // push 0 (false) ; IF push 10 ELSE push 20 EIF -> 20
        assertEquals(20, runTop(new byte[] { PUSHB1, 0, IF, PUSHB1, 10, ELSE, PUSHB1, 20, EIF }));
    }

    @Test
    void testNestedIf()
    {
        // outer true, inner (5>3) true -> 99
        // PUSHB 1 ; IF [ PUSHB 5 3 ; GT ; IF PUSHB 99 ELSE PUSHB 1 EIF ] ELSE PUSHB 7 EIF
        byte[] program = new byte[] {
                PUSHB1, 1, IF,
                    PUSHB2, 5, 3, GT, IF,
                        PUSHB1, 99,
                    ELSE,
                        PUSHB1, 1,
                    EIF,
                ELSE,
                    PUSHB1, 7,
                EIF };
        assertEquals(99, runTop(program));
    }

    @Test
    void testJmpr()
    {
        // PUSHB 3 ; JMPR (jump +3 from the JMPR opcode) skips a push, lands on PUSHB 42
        // layout: [0]PUSHB1 [1]3 [2]JMPR [3]PUSHB1 [4]7(skipped) [5]PUSHB1 [6]42
        ExecutionContext ctx = interpreter().executeProgram(
                new byte[] { PUSHB1, 3, JMPR, PUSHB1, 7, PUSHB1, 42 }, 16);
        assertEquals(42, ctx.peek(0));
        assertEquals(1, ctx.getStackDepth()); // the skipped push never ran
    }

    @Test
    void testFunctionDefAndCall()
    {
        // define function 5 = "double the top" (DUP ADD); call it on 21 -> 42
        TrueTypeInterpreter interp = interpreter();
        interp.setFontProgram(new byte[] { PUSHB1, 5, FDEF, DUP, ADD, ENDF });
        interp.prepareFontProgram();
        assertEquals(1, interp.getFunctions().size());

        ExecutionContext ctx = interp.executeProgram(new byte[] { PUSHB1, 21, PUSHB1, 5, CALL }, 16);
        assertEquals(42, ctx.peek(0));
    }

    @Test
    void testLoopCall()
    {
        // function 1 = "add 1"; LOOPCALL it 3 times starting from 0 -> 3
        TrueTypeInterpreter interp = interpreter();
        interp.setFontProgram(new byte[] { PUSHB1, 1, FDEF, PUSHB1, 1, ADD, ENDF });
        interp.prepareFontProgram();

        // stack: value=0, count=3, fn=1 ; LOOPCALL pops fn then count
        ExecutionContext ctx = interp.executeProgram(
                new byte[] { PUSHB1, 0, PUSHB2, 3, 1, LOOPCALL }, 16);
        assertEquals(3, ctx.peek(0));
    }

    @Test
    void testCallDepthLimitTrips()
    {
        // function 0 calls itself unconditionally -> must trip the depth cap, not StackOverflowError
        TrueTypeInterpreter interp = interpreter();
        interp.setFontProgram(new byte[] { PUSHB1, 0, FDEF, PUSHB1, 0, CALL, ENDF });
        interp.prepareFontProgram();

        HintingException ex = assertThrows(HintingException.class,
                () -> interp.executeProgram(new byte[] { PUSHB1, 0, CALL }, 16));
        assertEquals(true, ex.getMessage().contains("call depth"));
    }

    @Test
    void testIdefDefinesOpcode()
    {
        // IDEF binds reserved opcode 0x83 to "push 42"; invoking 0x83 then runs that body.
        // PUSHB[0] 0x83 ; IDEF ; PUSHB[0] 42 ; ENDF ; <0x83>
        ExecutionContext ctx = interpreter().executeProgram(
                new byte[] { PUSHB1, (byte) 0x83, (byte) 0x89, PUSHB1, 42, ENDF, (byte) 0x83 }, 16);
        assertEquals(42, ctx.peek(0));
    }

    @Test
    void testUndefinedFunctionThrows()
    {
        assertThrows(HintingException.class,
                () -> interpreter().executeProgram(new byte[] { PUSHB1, 9, CALL }, 16));
    }

    @Test
    void testMppemReflectsPpem()
    {
        ExecutionContext ctx = interpreter().executeProgram(new byte[] { MPPEM }, 19);
        assertEquals(19, ctx.peek(0));
    }

    @Test
    void testUnsupportedOpcodeThrows()
    {
        // 0x28 is a reserved/unused opcode; unimplemented opcodes must throw, not no-op
        assertThrows(HintingException.class,
                () -> interpreter().executeProgram(new byte[] { 0x28 }, 16));
    }

    @Test
    void testControlValueScalingThroughPrep()
    {
        // raw cvt [2048] at 16 ppem, unitsPerEm 2048 -> scaled to 16px (1024 in F26Dot6)
        TrueTypeInterpreter interp = interpreter();
        interp.setControlValues(new int[] { 2048 });
        interp.setPpem(16, 16);
        // RCVT 0 -> the scaled value
        ExecutionContext ctx = interp.executeProgram(new byte[] { PUSHB1, 0, 0x45 }, 16);
        assertEquals(Fixed.fromInt(16), ctx.peek(0));
    }

    /**
     * A backward jump is one of only two ways TrueType bytecode can loop, and this four-byte program -
     * {@code PUSHW -3 ; JMPR}, which jumps back onto its own push - used to spin forever. It must now
     * hit the execution budget and throw, so {@code GlyphHinter} falls back to the raw outline.
     */
    @Test
    void testBackwardJumpIsBounded()
    {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(HintingException.class,
                        () -> interpreter().executeProgram(
                                new byte[] { PUSHW1, (byte) 0xFF, (byte) 0xFD, JMPR }, 16)));
    }

    /**
     * A backward-jump loop that stays inside the budget must still run to completion - the bound exists
     * to stop runaway programs, not legitimately loop-heavy ones.
     */
    @Test
    void testBackwardJumpWithinBudgetCompletes()
    {
        // counter = 60; loop { counter -= 1; if (counter != 0) jump back } -> 59 backward jumps
        // [0]PUSHW1 60 [3]PUSHB1 1 [5]SUB [6]DUP [7]PUSHW1 -8 [10]SWAP [11]JROT
        byte[] program =
        {
            PUSHW1, 0, 60,
            PUSHB1, 1, SUB, DUP, PUSHW1, (byte) 0xFF, (byte) 0xF8, SWAP, JROT
        };
        ExecutionContext ctx = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> interpreter().executeProgram(program, 16));
        assertEquals(0, ctx.peek(0));
        assertEquals(1, ctx.getStackDepth());
    }

    /**
     * {@code LOOPCALL} takes its iteration count off the stack, so a crafted font can ask for billions.
     * The whole loop is charged against the budget up front, so an absurd count fails before a single
     * iteration runs.
     */
    @Test
    void testLoopCallCountIsBounded()
    {
        TrueTypeInterpreter interp = interpreter();
        interp.setFontProgram(new byte[] { PUSHB1, 1, FDEF, PUSHB1, 1, ADD, ENDF });
        interp.prepareFontProgram();

        // stack: value=0, count=32767, fn=1
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(HintingException.class,
                        () -> interp.executeProgram(
                                new byte[] { PUSHB1, 0, PUSHW1, 0x7F, (byte) 0xFF, PUSHB1, 1,
                                        LOOPCALL }, 16)));
    }

    /**
     * The spec calls the {@code LOOPCALL} count unsigned; FreeType runs nothing at all when it is not
     * positive, so a negative count must be a no-op rather than an error or an underflowing loop.
     */
    @Test
    void testNonPositiveLoopCallCountRunsNothing()
    {
        TrueTypeInterpreter interp = interpreter();
        interp.setFontProgram(new byte[] { PUSHB1, 1, FDEF, PUSHB1, 1, ADD, ENDF });
        interp.prepareFontProgram();

        // stack: value=7, count=-1, fn=1 -> the function never runs, 7 is left untouched
        ExecutionContext ctx = interp.executeProgram(
                new byte[] { PUSHB1, 7, PUSHW1, (byte) 0xFF, (byte) 0xFF, PUSHB1, 1, LOOPCALL }, 16);
        assertEquals(7, ctx.peek(0));
        assertEquals(1, ctx.getStackDepth());
    }

    /**
     * A font may compute values into the storage area in {@code prep} and read them back from every
     * glyph program, so storage belongs to the size and not to one program run. Building a fresh
     * storage array per run made {@code RS} read zeros - silently wrong outlines rather than a failure.
     */
    @Test
    void testStoragePersistsFromPrepIntoGlyphProgram()
    {
        TrueTypeInterpreter interp = interpreter();
        interp.setControlValueProgram(new byte[] { PUSHB2, 5, 42, WS }); // storage[5] = 42
        interp.setPpem(16, 16);

        ExecutionContext ctx = interp.executeProgram(new byte[] { PUSHB1, 5, RS }, 16);
        assertEquals(42, ctx.peek(0));
    }

    /** The twilight zone belongs to the size for the same reason: {@code prep} seeds points there. */
    @Test
    void testTwilightPointsPersistFromPrep()
    {
        TrueTypeInterpreter interp = new TrueTypeInterpreter(256, 16, 4, 2048);
        // prep: aim every zone pointer at the twilight zone, then set point 1's x to 128
        interp.setControlValueProgram(new byte[] { PUSHB1, 0, SZPS, PUSHB2, 1, (byte) 128, SCFS });
        interp.setPpem(16, 16);

        ExecutionContext ctx = interp.executeProgram(new byte[] { PUSHB1, 0, SZPS, PUSHB1, 1, GC }, 16);
        assertEquals(128, ctx.peek(0));
    }

    /** A new size starts clean, as FreeType clears both in {@code tt_size_run_prep}. */
    @Test
    void testStorageIsClearedOnPpemChange()
    {
        TrueTypeInterpreter interp = interpreter();
        interp.setPpem(16, 16);
        interp.executeProgram(new byte[] { PUSHB2, 5, 42, WS }, 16);
        assertEquals(42, interp.executeProgram(new byte[] { PUSHB1, 5, RS }, 16).peek(0));

        interp.setPpem(24, 24);
        assertEquals(0, interp.executeProgram(new byte[] { PUSHB1, 5, RS }, 24).peek(0));
    }
    /**
     * MINDEX moves the k-th element (1 = top) to the top and closes the gap, leaving everything
     * else in order (FreeType's Ins_MINDEX). Checked for k = 1 (no-op), 2 (= SWAP), 3 (= ROLL) and a
     * deeper k, on the whole stack rather than just the top.
     */
    @Test
    void testMindex()
    {
        // stack bottom->top: 10 20 30 40 50
        byte[] five = { NPUSHB, 5, 10, 20, 30, 40, 50 };
        assertArrayEquals(new int[] { 10, 20, 30, 40, 50 }, runStack(concat(five, PUSHB1, 1, MINDEX)));
        assertArrayEquals(new int[] { 10, 20, 30, 50, 40 }, runStack(concat(five, PUSHB1, 2, MINDEX)));
        assertArrayEquals(new int[] { 10, 20, 40, 50, 30 }, runStack(concat(five, PUSHB1, 3, MINDEX)));
        assertArrayEquals(new int[] { 20, 30, 40, 50, 10 }, runStack(concat(five, PUSHB1, 5, MINDEX)));
        // k = 2 is SWAP and k = 3 is ROLL
        assertArrayEquals(runStack(concat(five, SWAP)), runStack(concat(five, PUSHB1, 2, MINDEX)));
        assertArrayEquals(runStack(concat(five, ROLL)), runStack(concat(five, PUSHB1, 3, MINDEX)));
        // out of range: k is consumed, the rest of the stack is untouched (FreeType, non-pedantic)
        assertArrayEquals(new int[] { 10, 20, 30, 40, 50 }, runStack(concat(five, PUSHB1, 6, MINDEX)));
        assertArrayEquals(new int[] { 10, 20, 30, 40, 50 }, runStack(concat(five, PUSHB1, 0, MINDEX)));
    }

    @Test
    void testCindex()
    {
        byte[] three = { PUSHB2, 10, 20, PUSHB1, 30 };
        assertArrayEquals(new int[] { 10, 20, 30, 30 }, runStack(concat(three, PUSHB1, 1, CINDEX)));
        assertArrayEquals(new int[] { 10, 20, 30, 10 }, runStack(concat(three, PUSHB1, 3, CINDEX)));
        assertArrayEquals(new int[] { 10, 20, 30 }, runStack(concat(three, PUSHB1, 4, CINDEX)));
    }

    /**
     * Jump offsets are relative to the jump opcode itself (offset 1 is the next instruction). A
     * forward JMPR skips code; a backward JROT builds a countdown loop; JROF jumps only on false.
     */
    @Test
    void testJumpsAreRelativeToTheJumpOpcode()
    {
        // JMPR +4 skips the "PUSHB1 99 POP" that follows... layout:
        //  0: PUSHB1 4   2: JMPR   3: PUSHB1 99   5: POP   6: PUSHB1 7
        // JMPR at 2, offset 4 -> 6, so 99 is never pushed
        assertArrayEquals(new int[] { 7 }, runStack(new byte[] { PUSHB1, 4, JMPR, PUSHB1, 99, POP, PUSHB1, 7 }));

        // countdown: push 3; loop: push 1, SUB, DUP, push -6 (offset), SWAP, JROT -> loops while != 0
        //  0: PUSHB1 3
        //  2: PUSHB1 1   4: SUB   5: DUP   6: PUSHW1 FF FA (-6)   9: SWAP   10: JROT  (10 - 6 = 4? no: 10-8=2)
        // JROT at 10 must land on 2: offset -8
        assertArrayEquals(new int[] { 0 }, runStack(new byte[] { PUSHB1, 3,
                PUSHB1, 1, SUB, DUP, PUSHW1, (byte) 0xFF, (byte) 0xF8, SWAP, JROT }));

        // JROF: jumps when the condition is false
        //  0: PUSHB2 4 0   3: JROF   4: PUSHB1 99   6: POP   7: PUSHB1 8
        assertArrayEquals(new int[] { 8 }, runStack(new byte[] { PUSHB2, 4, 0, JROF, PUSHB1, 99, POP, PUSHB1, 8 }));
        //  with a true condition JROF falls through
        assertArrayEquals(new int[] { 99, 8 }, runStack(new byte[] { PUSHB2, 4, 1, JROF, PUSHB1, 99, PUSHB1, 8 }));
    }

    /**
     * Skipping a false IF branch must respect nesting and must not mistake push <em>data</em> for
     * opcodes: the skipped branch here contains PUSHB operands equal to the IF, ELSE and EIF opcode
     * bytes, and a nested IF/ELSE/EIF.
     */
    @Test
    void testNestedIfElseWithOpcodeBytesAsPushData()
    {
        byte[] program = {
            PUSHB1, 0, IF,                       // false: skip to ELSE
                PUSHB1, EIF,                     //   data byte 0x59 must not end the skip
                PUSHB1, 1, IF, PUSHB1, ELSE, ELSE, PUSHB1, IF, EIF, // nested, with data 0x1B / 0x58
                PUSHB1, 99,
            ELSE,
                PUSHB1, 1, IF, PUSHB1, 5, ELSE, PUSHB1, 6, EIF,     // nested true: 5
                PUSHB1, 0, IF, PUSHB1, 7, ELSE, PUSHB1, 8, EIF,     // nested false: 8
            EIF,
            PUSHB1, 9 };
        assertArrayEquals(new int[] { 5, 8, 9 }, runStack(program));

        // and a true outer branch skips the else-branch, which itself contains nested IFs and data
        byte[] program2 = {
            PUSHB1, 1, IF,
                PUSHB1, 1,
            ELSE,
                PUSHB1, EIF, PUSHB1, 0, IF, PUSHB1, IF, ELSE, PUSHB1, ELSE, EIF, PUSHB1, 99,
            EIF,
            PUSHB1, 2 };
        assertArrayEquals(new int[] { 1, 2 }, runStack(program2));
    }

    /**
     * Function bodies: an IF/ELSE inside a body, a body containing push data equal to ENDF, a
     * function calling another function (return flag must not leak to the caller), and a recursive
     * function terminated by its own IF.
     */
    @Test
    void testFunctionBodiesNestedCallsAndReturns()
    {
        // fn 0: doubles the top (DUP ADD), but contains an IF whose skipped branch holds ENDF as data
        // fn 1: calls fn 0 twice, then pushes 100 - the push after the inner calls proves the inner
        //       ENDF did not return from fn 1
        byte[] program = {
            PUSHB1, 0, FDEF,
                PUSHB1, 0, IF, PUSHB1, ENDF, PUSHB1, ENDF, ELSE, DUP, ADD, EIF,
            ENDF,
            PUSHB1, 1, FDEF,
                PUSHB1, 0, CALL, PUSHB1, 0, CALL, PUSHB1, 100,
            ENDF,
            PUSHB1, 3, PUSHB1, 1, CALL };
        assertArrayEquals(new int[] { 12, 100 }, runStack(program));

        // fn 2: recursive countdown: if top != 0 { push 1, SUB, call 2 } ; leaves 0
        byte[] recursive = {
            PUSHB1, 2, FDEF,
                DUP, IF, PUSHB1, 1, SUB, PUSHB1, 2, CALL, EIF,
            ENDF,
            PUSHB1, 5, PUSHB1, 2, CALL, PUSHB1, 42 };
        assertArrayEquals(new int[] { 0, 42 }, runStack(recursive));
    }

    @Test
    void testEndfOutsideFunctionIsAnError()
    {
        assertThrows(HintingException.class, () -> runStack(new byte[] { PUSHB1, 1, ENDF }));
    }

    private static byte[] concat(byte[] head, int... tail)
    {
        byte[] out = new byte[head.length + tail.length];
        System.arraycopy(head, 0, out, 0, head.length);
        for (int i = 0; i < tail.length; i++)
        {
            out[head.length + i] = (byte) tail[i];
        }
        return out;
    }
    /**
     * A jump that leaves a function body is an error (FreeType: {@code IP > Def->end} in JMPR), and
     * a body never runs past its own ENDF even if control flow tries to.
     */
    @Test
    void testJumpOutOfFunctionBodyIsAnError()
    {
        // fn 0 body: PUSHB1 20 JMPR -> would land far past ENDF
        byte[] program = { PUSHB1, 0, FDEF, PUSHB1, 20, JMPR, ENDF, PUSHB1, 0, CALL, PUSHB1, 1 };
        assertThrows(HintingException.class, () -> runStack(program));
        // a jump to the ENDF itself is fine (offset 3 from JMPR at 5 lands on ENDF at 8)
        byte[] ok = { PUSHB1, 0, FDEF, PUSHB1, 3, JMPR, PUSHB1, 99, ENDF, PUSHB1, 0, CALL, PUSHB1, 1 };
        assertArrayEquals(new int[] { 1 }, runStack(ok));
    }

    @Test
    void testNestedFunctionDefinitionIsAnError()
    {
        byte[] program = { PUSHB1, 0, FDEF, PUSHB1, 1, FDEF, ENDF, ENDF };
        assertThrows(HintingException.class, () -> runStack(program));
    }
}
