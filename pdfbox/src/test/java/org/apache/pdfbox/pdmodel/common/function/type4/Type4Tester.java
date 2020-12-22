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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing helper class for testing type 4 functions from the PDF specification.
 */
public class Type4Tester
{

    private final ExecutionContext context;

    private Type4Tester(final ExecutionContext ctxt)
    {
        this.context = ctxt;
    }

    /**
     * Creates a new instance for the given type 4 function.
     * @param text the text of the type 4 function
     * @return the tester instance
     */
    public static Type4Tester create(final String text)
    {
        final InstructionSequence instructions = InstructionSequenceBuilder.parse(text);

        final ExecutionContext context = new ExecutionContext(new Operators());
        instructions.execute(context);
        return new Type4Tester(context);
    }

    /**
     * Pops a bool value from the stack and checks it against the expected result.
     * @param expected the expected bool value
     * @return this instance
     */
    public Type4Tester pop(final boolean expected)
    {
        final boolean value = (Boolean)context.getStack().pop();
        assertEquals(expected, value);
        return this;
    }

    /**
     * Pops a real value from the stack and checks it against the expected result.
     * @param expected the expected real value
     * @return this instance
     */
    public Type4Tester popReal(final float expected)
    {
        return popReal(expected, 0.0000001);
    }

    /**
     * Pops a real value from the stack and checks it against the expected result.
     * @param expected the expected real value
     * @param delta the allowed deviation of the value from the expected result
     * @return this instance
     */
    public Type4Tester popReal(final float expected, final double delta)
    {
        final Float value = (Float)context.getStack().pop();
        assertEquals(expected, value, delta);
        return this;
    }

    /**
     * Pops an int value from the stack and checks it against the expected result.
     * @param expected the expected int value
     * @return this instance
     */
    public Type4Tester pop(final int expected)
    {
        final int value = context.popInt();
        assertEquals(expected, value);
        return this;
    }

    /**
     * Pops a numeric value from the stack and checks it against the expected result.
     * @param expected the expected numeric value
     * @return this instance
     */
    public Type4Tester pop(final float expected)
    {
        return pop(expected, 0.0000001);
    }

    /**
     * Pops a numeric value from the stack and checks it against the expected result.
     * @param expected the expected numeric value
     * @param delta the allowed deviation of the value from the expected result
     * @return this instance
     */
    public Type4Tester pop(final float expected, final double delta)
    {
        final Number value = context.popNumber();
        assertEquals(expected, value.doubleValue(), delta);
        return this;
    }

    /**
     * Checks that the stack is empty at this point.
     * @return this instance
     */
    public Type4Tester isEmpty()
    {
        assertTrue(context.getStack().isEmpty());
        return this;
    }

    /**
     * Returns the execution context so some custom checks can be performed.
     * @return the associated execution context
     */
    public ExecutionContext toExecutionContext()
    {
        return this.context;
    }

}
