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
 * Provides the relational operators such as "eq" and "le".
 *
 * @version $Revision$
 */
class RelationalOperators
{

    /** Implements the "eq" operator. */
    static class Eq implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Object op2 = stack.pop();
            Object op1 = stack.pop();
            boolean result = isEqual(op1, op2);
            stack.push(result);
        }

        protected boolean isEqual(Object op1, Object op2)
        {
            boolean result = false;
            if (op1 instanceof Number && op2 instanceof Number)
            {
                Number num1 = (Number)op1;
                Number num2 = (Number)op2;
                result = num1.floatValue() == num2.floatValue();
            }
            else
            {
                result = op1.equals(op2);
            }
            return result;
        }

    }

    /** Abstract base class for number comparison operators. */
    private abstract static class AbstractNumberComparisonOperator implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Object op2 = stack.pop();
            Object op1 = stack.pop();
            Number num1 = (Number)op1;
            Number num2 = (Number)op2;
            boolean result = compare(num1, num2);
            stack.push(result);
        }

        protected abstract boolean compare(Number num1, Number num2);

    }

    /** Implements the "ge" operator. */
    static class Ge extends AbstractNumberComparisonOperator
    {

        @Override
        protected boolean compare(Number num1, Number num2)
        {
            return num1.floatValue() >= num2.floatValue();
        }

    }

    /** Implements the "gt" operator. */
    static class Gt extends AbstractNumberComparisonOperator
    {

        @Override
        protected boolean compare(Number num1, Number num2)
        {
            return num1.floatValue() > num2.floatValue();
        }

    }

    /** Implements the "le" operator. */
    static class Le extends AbstractNumberComparisonOperator
    {

        @Override
        protected boolean compare(Number num1, Number num2)
        {
            return num1.floatValue() <= num2.floatValue();
        }

    }

    /** Implements the "lt" operator. */
    static class Lt extends AbstractNumberComparisonOperator
    {

        @Override
        protected boolean compare(Number num1, Number num2)
        {
            return num1.floatValue() < num2.floatValue();
        }

    }

    /** Implements the "ne" operator. */
    static class Ne extends Eq
    {

        @Override
        protected boolean isEqual(Object op1, Object op2)
        {
            boolean result = super.isEqual(op1, op2);
            return !result;
        }

    }

}
