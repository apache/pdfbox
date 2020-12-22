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
 * Provides the bitwise operators such as "and" and "xor".
 *
 */
class BitwiseOperators
{

    private BitwiseOperators()
    {
        // Private constructor.
    }

    /** Abstract base class for logical operators. */
    private abstract static class AbstractLogicalOperator implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final Object op2 = stack.pop();
            final Object op1 = stack.pop();
            if (op1 instanceof Boolean && op2 instanceof Boolean)
            {
                final boolean bool1 = (Boolean)op1;
                final boolean bool2 = (Boolean)op2;
                final boolean result = applyForBoolean(bool1, bool2);
                stack.push(result);
            }
            else if (op1 instanceof Integer && op2 instanceof Integer)
            {
                final int int1 = (Integer)op1;
                final int int2 = (Integer)op2;
                final int result = applyforInteger(int1, int2);
                stack.push(result);
            }
            else
            {
                throw new ClassCastException("Operands must be bool/bool or int/int");
            }
        }


        protected abstract boolean applyForBoolean(boolean bool1, boolean bool2);

        protected abstract int applyforInteger(int int1, int int2);

    }

    /** Implements the "and" operator. */
    static class And extends AbstractLogicalOperator
    {

        @Override
        protected boolean applyForBoolean(final boolean bool1, final boolean bool2)
        {
            return bool1 && bool2;
        }

        @Override
        protected int applyforInteger(final int int1, final int int2)
        {
            return int1 & int2;
        }
    }

    /** Implements the "bitshift" operator. */
    static class Bitshift implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final int shift = (Integer)stack.pop();
            final int int1 = (Integer)stack.pop();
            if (shift < 0)
            {
                final int result = int1 >> Math.abs(shift);
                stack.push(result);
            }
            else
            {
                final int result = int1 << shift;
                stack.push(result);
            }
        }

    }

    /** Implements the "false" operator. */
    static class False implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            stack.push(Boolean.FALSE);
        }

    }

    /** Implements the "not" operator. */
    static class Not implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final Object op1 = stack.pop();
            if (op1 instanceof Boolean)
            {
                final boolean bool1 = (Boolean)op1;
                final boolean result = !bool1;
                stack.push(result);
            }
            else if (op1 instanceof Integer)
            {
                final int int1 = (Integer)op1;
                final int result = -int1;
                stack.push(result);
            }
            else
            {
                throw new ClassCastException("Operand must be bool or int");
            }
        }

    }

    /** Implements the "or" operator. */
    static class Or extends AbstractLogicalOperator
    {

        @Override
        protected boolean applyForBoolean(final boolean bool1, final boolean bool2)
        {
            return bool1 || bool2;
        }

        @Override
        protected int applyforInteger(final int int1, final int int2)
        {
            return int1 | int2;
        }

    }

    /** Implements the "true" operator. */
    static class True implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            stack.push(Boolean.TRUE);
        }

    }

    /** Implements the "xor" operator. */
    static class Xor extends AbstractLogicalOperator
    {

        @Override
        protected boolean applyForBoolean(final boolean bool1, final boolean bool2)
        {
            return bool1 ^ bool2;
        }

        @Override
        protected int applyforInteger(final int int1, final int int2)
        {
            return int1 ^ int2;
        }

    }

}
