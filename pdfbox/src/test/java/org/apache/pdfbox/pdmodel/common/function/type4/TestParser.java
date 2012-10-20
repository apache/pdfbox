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

import junit.framework.TestCase;

/**
 * Tests the type 4 function parser.
 *
 * @version $Revision$
 */
public class TestParser extends TestCase
{

    /**
     * Test the very basics.
     * @throws Exception if an error occurs
     */
    public void testParserBasics() throws Exception
    {
        Type4Tester.create("3 4 add 2 sub").pop(5).isEmpty();
    }

    /**
     * Test nested blocks.
     * @throws Exception if an error occurs
     */
    public void testNested() throws Exception
    {
        Type4Tester.create("true { 2 1 add } { 2 1 sub } ifelse")
            .pop(3).isEmpty();

        Type4Tester.create("{ true }").pop(true).isEmpty();
    }

    /**
     * Tests parsing of real values.
     * @throws Exception if an error occurs
     */
    public void testParseFloat() throws Exception
    {
        assertEquals(0, InstructionSequenceBuilder.parseReal("0"), 0.00001f);
        assertEquals(1, InstructionSequenceBuilder.parseReal("1"), 0.00001f);
        assertEquals(1, InstructionSequenceBuilder.parseReal("+1"), 0.00001f);
        assertEquals(-1, InstructionSequenceBuilder.parseReal("-1"), 0.00001f);
        assertEquals(3.14157, InstructionSequenceBuilder.parseReal("3.14157"), 0.00001f);
        assertEquals(-1.2, InstructionSequenceBuilder.parseReal("-1.2"), 0.00001f);

        assertEquals(1.0E-5, InstructionSequenceBuilder.parseReal("1.0E-5"), 0.00001f);
    }

    /**
     * Tests problematic functions from PDFBOX-804.
     * @throws Exception if an error occurs
     */
    public void testJira804() throws Exception
    {
        //This is an example of a tint to CMYK function
        //Problems here were:
        //1. no whitespace between "mul" and "}" (token was detected as "mul}")
        //2. line breaks cause endless loops
        Type4Tester.create("1 {dup dup .72 mul exch 0 exch .38 mul}\n")
            .pop(0.38f).pop(0f).pop(0.72f).pop(1.0f).isEmpty();

    }
}
