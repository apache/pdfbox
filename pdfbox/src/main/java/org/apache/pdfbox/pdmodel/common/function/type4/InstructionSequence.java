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

import java.util.List;
import java.util.Stack;

/**
 * Represents an instruction sequence, a combination of values, operands and nested procedures.
 *
 */
public class InstructionSequence
{

    private final List<Object> instructions = new java.util.ArrayList<>();

    /**
     * Add a name (ex. an operator)
     * @param name the name
     */
    public void addName(final String name)
    {
        this.instructions.add(name);
    }

    /**
     * Adds an int value.
     * @param value the value
     */
    public void addInteger(final int value)
    {
        this.instructions.add(value);
    }

    /**
     * Adds a real value.
     * @param value the value
     */
    public void addReal(final float value)
    {
        this.instructions.add(value);
    }

    /**
     * Adds a bool value.
     * @param value the value
     */
    public void addBoolean(final boolean value)
    {
        this.instructions.add(value);
    }

    /**
     * Adds a proc (sub-sequence of instructions).
     * @param child the child proc
     */
    public void addProc(final InstructionSequence child)
    {
        this.instructions.add(child);
    }

    /**
     * Executes the instruction sequence.
     * @param context the execution context
     */
    public void execute(final ExecutionContext context)
    {
        final Stack<Object> stack = context.getStack();
        for (final Object o : instructions)
        {
            if (o instanceof String)
            {
                final String name = (String)o;
                final Operator cmd = context.getOperators().getOperator(name);
                if (cmd != null)
                {
                    cmd.execute(context);
                }
                else
                {
                    throw new UnsupportedOperationException("Unknown operator or name: " + name);
                }
            }
            else
            {
                stack.push(o);
            }
        }

        //Handles top-level procs that simply need to be executed
        while (!stack.isEmpty() && stack.peek() instanceof InstructionSequence)
        {
            final InstructionSequence nested = (InstructionSequence)stack.pop();
            nested.execute(context);
        }
    }

}
