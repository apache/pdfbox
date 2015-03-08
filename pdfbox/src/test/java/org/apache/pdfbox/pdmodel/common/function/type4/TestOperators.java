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
package org.apache.pdfbox.pdmodel.common.function.type4;

import org.junit.Assert;

import junit.framework.TestCase;

/**
 * Tests all implemented PostScript operators.
 *
 */
public class TestOperators extends TestCase
{

    /**
     * Tests the "add" operator.
     * @throws Exception if an error occurs
     */
    public void testAdd() throws Exception
    {
        Type4Tester.create("5 6 add").pop(11).isEmpty();

        Type4Tester.create("5 0.23 add").pop(5.23f).isEmpty();

        int bigValue = Integer.MAX_VALUE - 2;
        ExecutionContext context = Type4Tester.create(
                bigValue + " " + bigValue + " add").toExecutionContext();
        float floatResult = (Float)context.getStack().pop();
        assertEquals(2 * (long)Integer.MAX_VALUE - 4, floatResult, 1);
        assertTrue(context.getStack().isEmpty());
    }

    /**
     * Tests the "abs" operator.
     * @throws Exception if an error occurs
     */
    public void testAbs() throws Exception
    {
        Type4Tester.create("-3 abs 2.1 abs -2.1 abs -7.5 abs")
            .pop(7.5f).pop(2.1f).pop(2.1f).pop(3).isEmpty();
    }

    /**
     * Tests the "and" operator.
     * @throws Exception if an error occurs
     */
    public void testAnd() throws Exception
    {
        Type4Tester.create("true true and true false and")
            .pop(false).pop(true).isEmpty();

        Type4Tester.create("99 1 and 52 7 and")
            .pop(4).pop(1).isEmpty();
    }

    /**
     * Tests the "atan" operator.
     * @throws Exception if an error occurs
     */
    public void testAtan() throws Exception
    {
        Type4Tester.create("0 1 atan").pop(0f).isEmpty();
        Type4Tester.create("1 0 atan").pop(90f).isEmpty();
        Type4Tester.create("-100 0 atan").pop(270f).isEmpty();
        Type4Tester.create("4 4 atan").pop(45f).isEmpty();
    }

    /**
     * Tests the "ceiling" operator.
     * @throws Exception if an error occurs
     */
    public void testCeiling() throws Exception
    {
        Type4Tester.create("3.2 ceiling -4.8 ceiling 99 ceiling")
            .pop(99).pop(-4f).pop(4f).isEmpty();
    }

    /**
     * Tests the "cos" operator.
     * @throws Exception if an error occurs
     */
    public void testCos() throws Exception
    {
        Type4Tester.create("0 cos").popReal(1f).isEmpty();
        Type4Tester.create("90 cos").popReal(0f).isEmpty();
    }

    /**
     * Tests the "cvi" operator.
     * @throws Exception if an error occurs
     */
    public void testCvi() throws Exception
    {
        Type4Tester.create("-47.8 cvi").pop(-47).isEmpty();
        Type4Tester.create("520.9 cvi").pop(520).isEmpty();
    }

    /**
     * Tests the "cvr" operator.
     * @throws Exception if an error occurs
     */
    public void testCvr() throws Exception
    {
        Type4Tester.create("-47.8 cvr").popReal(-47.8f).isEmpty();
        Type4Tester.create("520.9 cvr").popReal(520.9f).isEmpty();
        Type4Tester.create("77 cvr").popReal(77f).isEmpty();

        //Check that the data types are really right
        ExecutionContext context = Type4Tester.create("77 77 cvr").toExecutionContext();
        Assert.assertTrue("Expected a real as the result of 'cvr'",
                context.getStack().pop() instanceof Float);
        Assert.assertTrue("Expected an int from an integer literal",
                context.getStack().pop() instanceof Integer);
    }

    /**
     * Tests the "div" operator.
     * @throws Exception if an error occurs
     */
    public void testDiv() throws Exception
    {
        Type4Tester.create("3 2 div").popReal(1.5f).isEmpty();
        Type4Tester.create("4 2 div").popReal(2.0f).isEmpty();
    }

    /**
     * Tests the "exp" operator.
     * @throws Exception if an error occurs
     */
    public void testExp() throws Exception
    {
        Type4Tester.create("9 0.5 exp").popReal(3.0f).isEmpty();
        Type4Tester.create("-9 -1 exp").popReal(-0.111111f, 0.000001).isEmpty();
    }

    /**
     * Tests the "floor" operator.
     * @throws Exception if an error occurs
     */
    public void testFloor() throws Exception
    {
        Type4Tester.create("3.2 floor -4.8 floor 99 floor")
            .pop(99).pop(-5f).pop(3f).isEmpty();
    }

    /**
     * Tests the "div" operator.
     * @throws Exception if an error occurs
     */
    public void testIDiv() throws Exception
    {
        Type4Tester.create("3 2 idiv").pop(1).isEmpty();
        Type4Tester.create("4 2 idiv").pop(2).isEmpty();
        Type4Tester.create("-5 2 idiv").pop(-2).isEmpty();
        try
        {
            Type4Tester.create("4.4 2 idiv");
            Assert.fail("Expected typecheck");
        }
        catch (ClassCastException cce)
        {
            //expected
        }
    }

    /**
     * Tests the "ln" operator.
     * @throws Exception if an error occurs
     */
    public void testLn() throws Exception
    {
        Type4Tester.create("10 ln").popReal(2.30259f, 0.00001f).isEmpty();
        Type4Tester.create("100 ln").popReal(4.60517f, 0.00001f).isEmpty();
    }

    /**
     * Tests the "log" operator.
     * @throws Exception if an error occurs
     */
    public void testLog() throws Exception
    {
        Type4Tester.create("10 log").popReal(1.0f).isEmpty();
        Type4Tester.create("100 log").popReal(2.0f).isEmpty();
    }

    /**
     * Tests the "mod" operator.
     * @throws Exception if an error occurs
     */
    public void testMod() throws Exception
    {
        Type4Tester.create("5 3 mod").pop(2).isEmpty();
        Type4Tester.create("5 2 mod").pop(1).isEmpty();
        Type4Tester.create("-5 3 mod").pop(-2).isEmpty();
        try
        {
            Type4Tester.create("4.4 2 mod");
            Assert.fail("Expected typecheck");
        }
        catch (ClassCastException cce)
        {
            //expected
        }
    }

    /**
     * Tests the "mul" operator.
     * @throws Exception if an error occurs
     */
    public void testMul() throws Exception
    {
        Type4Tester.create("1 2 mul").pop(2).isEmpty();
        Type4Tester.create("1.5 2 mul").popReal(3.0f).isEmpty();
        Type4Tester.create("1.5 2.1 mul").popReal(3.15f, 0.001).isEmpty();
        Type4Tester.create((Integer.MAX_VALUE - 3) + " 2 mul") //integer overflow -> real
            .popReal(2L * (Integer.MAX_VALUE - 3), 0.001).isEmpty();
    }

    /**
     * Tests the "neg" operator.
     * @throws Exception if an error occurs
     */
    public void testNeg() throws Exception
    {
        Type4Tester.create("4.5 neg").popReal(-4.5f).isEmpty();
        Type4Tester.create("-3 neg").pop(3).isEmpty();

        //Border cases
        Type4Tester.create((Integer.MIN_VALUE + 1) + " neg")
            .pop(Integer.MAX_VALUE).isEmpty();
        Type4Tester.create(Integer.MIN_VALUE + " neg")
            .popReal(-(float)Integer.MIN_VALUE).isEmpty();
    }

    /**
     * Tests the "round" operator.
     * @throws Exception if an error occurs
     */
    public void testRound() throws Exception
    {
        Type4Tester.create("3.2 round").popReal(3.0f).isEmpty();
        Type4Tester.create("6.5 round").popReal(7.0f).isEmpty();
        Type4Tester.create("-4.8 round").popReal(-5.0f).isEmpty();
        Type4Tester.create("-6.5 round").popReal(-6.0f).isEmpty();
        Type4Tester.create("99 round").pop(99).isEmpty();
    }

    /**
     * Tests the "sin" operator.
     * @throws Exception if an error occurs
     */
    public void testSin() throws Exception
    {
        Type4Tester.create("0 sin").popReal(0f).isEmpty();
        Type4Tester.create("90 sin").popReal(1f).isEmpty();
        Type4Tester.create("-90.0 sin").popReal(-1f).isEmpty();
    }

    /**
     * Tests the "sqrt" operator.
     * @throws Exception if an error occurs
     */
    public void testSqrt() throws Exception
    {
        Type4Tester.create("0 sqrt").popReal(0f).isEmpty();
        Type4Tester.create("1 sqrt").popReal(1f).isEmpty();
        Type4Tester.create("4 sqrt").popReal(2f).isEmpty();
        Type4Tester.create("4.4 sqrt").popReal(2.097617f, 0.000001).isEmpty();
        try
        {
            Type4Tester.create("-4.1 sqrt");
            Assert.fail("Expected rangecheck");
        }
        catch (IllegalArgumentException iae)
        {
            //expected
        }
    }

    /**
     * Tests the "sub" operator.
     * @throws Exception if an error occurs
     */
    public void testSub() throws Exception
    {
        Type4Tester.create("5 2 sub -7.5 1 sub").pop(-8.5f).pop(3).isEmpty();
    }

    /**
     * Tests the "truncate" operator.
     * @throws Exception if an error occurs
     */
    public void testTruncate() throws Exception
    {
        Type4Tester.create("3.2 truncate").popReal(3.0f).isEmpty();
        Type4Tester.create("-4.8 truncate").popReal(-4.0f).isEmpty();
        Type4Tester.create("99 truncate").pop(99).isEmpty();
    }

    /**
     * Tests the "bitshift" operator.
     * @throws Exception if an error occurs
     */
    public void testBitshift() throws Exception
    {
        Type4Tester.create("7 3 bitshift 142 -3 bitshift")
            .pop(17).pop(56).isEmpty();
    }

    /**
     * Tests the "eq" operator.
     * @throws Exception if an error occurs
     */
    public void testEq() throws Exception
    {
        Type4Tester.create("7 7 eq 7 6 eq 7 -7 eq true true eq false true eq 7.7 7.7 eq")
            .pop(true).pop(false).pop(true).pop(false).pop(false).pop(true).isEmpty();
    }

    /**
     * Tests the "ge" operator.
     * @throws Exception if an error occurs
     */
    public void testGe() throws Exception
    {
        Type4Tester.create("5 7 ge 7 5 ge 7 7 ge -1 2 ge")
            .pop(false).pop(true).pop(true).pop(false).isEmpty();
    }

    /**
     * Tests the "gt" operator.
     * @throws Exception if an error occurs
     */
    public void testGt() throws Exception
    {
        Type4Tester.create("5 7 gt 7 5 gt 7 7 gt -1 2 gt")
            .pop(false).pop(false).pop(true).pop(false).isEmpty();
    }

    /**
     * Tests the "le" operator.
     * @throws Exception if an error occurs
     */
    public void testLe() throws Exception
    {
        Type4Tester.create("5 7 le 7 5 le 7 7 le -1 2 le")
            .pop(true).pop(true).pop(false).pop(true).isEmpty();
    }

    /**
     * Tests the "lt" operator.
     * @throws Exception if an error occurs
     */
    public void testLt() throws Exception
    {
        Type4Tester.create("5 7 lt 7 5 lt 7 7 lt -1 2 lt")
            .pop(true).pop(false).pop(false).pop(true).isEmpty();
    }

    /**
     * Tests the "ne" operator.
     * @throws Exception if an error occurs
     */
    public void testNe() throws Exception
    {
        Type4Tester.create("7 7 ne 7 6 ne 7 -7 ne true true ne false true ne 7.7 7.7 ne")
            .pop(false).pop(true).pop(false).pop(true).pop(true).pop(false).isEmpty();
    }

    /**
     * Tests the "not" operator.
     * @throws Exception if an error occurs
     */
    public void testNot() throws Exception
    {
        Type4Tester.create("true not false not")
            .pop(true).pop(false).isEmpty();

        Type4Tester.create("52 not -37 not")
            .pop(37).pop(-52).isEmpty();
    }

    /**
     * Tests the "or" operator.
     * @throws Exception if an error occurs
     */
    public void testOr() throws Exception
    {
        Type4Tester.create("true true or true false or false false or")
            .pop(false).pop(true).pop(true).isEmpty();

        Type4Tester.create("17 5 or 1 1 or")
            .pop(1).pop(21).isEmpty();
    }

    /**
     * Tests the "cor" operator.
     * @throws Exception if an error occurs
     */
    public void testXor() throws Exception
    {
        Type4Tester.create("true true xor true false xor false false xor")
            .pop(false).pop(true).pop(false).isEmpty();

        Type4Tester.create("7 3 xor 12 3 or")
            .pop(15).pop(4);
    }

    /**
     * Tests the "if" operator.
     * @throws Exception if an error occurs
     */
    public void testIf() throws Exception
    {
        Type4Tester.create("true { 2 1 add } if")
            .pop(3).isEmpty();

        Type4Tester.create("false { 2 1 add } if")
            .isEmpty();

        try
        {
            Type4Tester.create("0 { 2 1 add } if");
            fail("Need typecheck error for the '0'");
        }
        catch (ClassCastException cce)
        {
            //expected
        }
    }

    /**
     * Tests the "ifelse" operator.
     * @throws Exception if an error occurs
     */
    public void testIfElse() throws Exception
    {
        Type4Tester.create("true { 2 1 add } { 2 1 sub } ifelse")
            .pop(3).isEmpty();

        Type4Tester.create("false { 2 1 add } { 2 1 sub } ifelse")
            .pop(1).isEmpty();
    }

    /**
     * Tests the "copy" operator.
     * @throws Exception if an error occurs
     */
    public void testCopy() throws Exception
    {
        Type4Tester.create("true 1 2 3 3 copy")
            .pop(3).pop(2).pop(1)
            .pop(3).pop(2).pop(1)
            .pop(true)
            .isEmpty();
    }

    /**
     * Tests the "dup" operator.
     * @throws Exception if an error occurs
     */
    public void testDup() throws Exception
    {
        Type4Tester.create("true 1 2 dup")
            .pop(2).pop(2).pop(1)
            .pop(true)
            .isEmpty();
        Type4Tester.create("true dup")
            .pop(true).pop(true).isEmpty();
    }

    /**
     * Tests the "exch" operator.
     * @throws Exception if an error occurs
     */
    public void testExch() throws Exception
    {
        Type4Tester.create("true 1 exch")
            .pop(true).pop(1).isEmpty();
        Type4Tester.create("1 2.5 exch")
            .pop(1).pop(2.5f).isEmpty();
    }

    /**
     * Tests the "index" operator.
     * @throws Exception if an error occurs
     */
    public void testIndex() throws Exception
    {
        Type4Tester.create("1 2 3 4 0 index")
            .pop(4).pop(4).pop(3).pop(2).pop(1).isEmpty();
        Type4Tester.create("1 2 3 4 3 index")
            .pop(1).pop(4).pop(3).pop(2).pop(1).isEmpty();
    }

    /**
     * Tests the "pop" operator.
     * @throws Exception if an error occurs
     */
    public void testPop() throws Exception
    {
        Type4Tester.create("1 pop 7 2 pop")
            .pop(7).isEmpty();
        Type4Tester.create("1 2 3 pop pop")
            .pop(1).isEmpty();
    }

    /**
     * Tests the "roll" operator.
     * @throws Exception if an error occurs
     */
    public void testRoll() throws Exception
    {
        Type4Tester.create("1 2 3 4 5 5 -2 roll")
            .pop(2).pop(1).pop(5).pop(4).pop(3).isEmpty();
        Type4Tester.create("1 2 3 4 5 5 2 roll")
            .pop(3).pop(2).pop(1).pop(5).pop(4).isEmpty();
        Type4Tester.create("1 2 3 3 0 roll")
            .pop(3).pop(2).pop(1).isEmpty();
    }

}
