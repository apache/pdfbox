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

    /** Abstract base class for logical operators. */
    private abstract static class AbstractLogicalOperator implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Object op2 = stack.pop();
            Object op1 = stack.pop();
            if (op1 instanceof Boolean && op2 instanceof Boolean)
            {
                boolean bool1 = (Boolean)op1;
                boolean bool2 = (Boolean)op2;
                boolean result = applyForBoolean(bool1, bool2);
                stack.push(result);
            }
            else if (op1 instanceof Integer && op2 instanceof Integer)
            {
                int int1 = (Integer)op1;
                int int2 = (Integer)op2;
                int result = applyforInteger(int1, int2);
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
        protected boolean applyForBoolean(boolean bool1, boolean bool2)
        {
            return bool1 & bool2;
        }

        @Override
        protected int applyforInteger(int int1, int int2)
        {
            return int1 & int2;
        }
    }

    /** Implements the "bitshift" operator. */
    static class Bitshift implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            int shift = (Integer)stack.pop();
            int int1 = (Integer)stack.pop();
            if (shift < 0)
            {
                int result = int1 >> Math.abs(shift);
                stack.push(result);
            }
            else
            {
                int result = int1 << shift;
                stack.push(result);
            }
        }

    }

    /** Implements the "false" operator. */
    static class False implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            stack.push(Boolean.FALSE);
        }

    }

    /** Implements the "not" operator. */
    static class Not implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Object op1 = stack.pop();
            if (op1 instanceof Boolean)
            {
                boolean bool1 = (Boolean)op1;
                boolean result = !bool1;
                stack.push(result);
            }
            else if (op1 instanceof Integer)
            {
                int int1 = (Integer)op1;
                int result = -int1;
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
        protected boolean applyForBoolean(boolean bool1, boolean bool2)
        {
            return bool1 | bool2;
        }

        @Override
        protected int applyforInteger(int int1, int int2)
        {
            return int1 | int2;
        }

    }

    /** Implements the "true" operator. */
    static class True implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            stack.push(Boolean.TRUE);
        }

    }

    /** Implements the "xor" operator. */
    static class Xor extends AbstractLogicalOperator
    {

        @Override
        protected boolean applyForBoolean(boolean bool1, boolean bool2)
        {
            return bool1 ^ bool2;
        }

        @Override
        protected int applyforInteger(int int1, int int2)
        {
            return int1 ^ int2;
        }

    }

}
