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

import java.util.Stack;

/**
 * Makes up the execution context, holding the available operators and the execution stack.
 *
 */
public class ExecutionContext
{

    private final Operators operators;
    private final Stack<Object> stack = new Stack<>();

    /**
     * Creates a new execution context.
     * @param operatorSet the operator set
     */
    public ExecutionContext(Operators operatorSet)
    {
        this.operators = operatorSet;
    }

    /**
     * Returns the stack used by this execution context.
     * @return the stack
     */
    public Stack<Object> getStack()
    {
        return this.stack;
    }

    /**
     * Returns the operator set used by this execution context.
     * @return the operator set
     */
    public Operators getOperators()
    {
        return this.operators;
    }

    /**
     * Pops a number (int or real) from the stack. If it's neither data type, a
     * ClassCastException is thrown.
     * @return the number
     */
    public Number popNumber()
    {
        return (Number)stack.pop();
    }

    /**
     * Pops a value of type int from the stack. If the value is not of type int, a
     * ClassCastException is thrown.
     * @return the int value
     */
    public int popInt()
    {
        return ((Integer)stack.pop());
    }

    /**
     * Pops a number from the stack and returns it as a real value. If the value is not of a
     * numeric type, a ClassCastException is thrown.
     * @return the real value
     */
    public float popReal()
    {
        return ((Number)stack.pop()).floatValue();
    }

}
