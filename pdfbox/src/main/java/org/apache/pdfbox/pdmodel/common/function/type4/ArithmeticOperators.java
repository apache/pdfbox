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
 * Provides the arithmetic operators such as "add" and "sub".
 *
 */
class ArithmeticOperators
{

    /** Implements the "abs" operator. */
    static class Abs implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                context.getStack().push(Math.abs(num.intValue()));
            }
            else
            {
                context.getStack().push(Math.abs(num.floatValue()));
            }
        }

    }

    /** Implements the "add" operator. */
    static class Add implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num2 = context.popNumber();
            Number num1 = context.popNumber();
            if (num1 instanceof Integer && num2 instanceof Integer)
            {
                long sum = num1.longValue() + num2.longValue();
                if (sum < Integer.MIN_VALUE || sum > Integer.MAX_VALUE)
                {
                    context.getStack().push((float) sum);
                }
                else
                {
                    context.getStack().push((int)sum);
                }
            }
            else
            {
                float sum = num1.floatValue() + num2.floatValue();
                context.getStack().push(sum);
            }
        }

    }

    /** Implements the "atan" operator. */
    static class Atan implements Operator
    {

        public void execute(ExecutionContext context)
        {
            float den = context.popReal();
            float num = context.popReal();
            float atan = (float)Math.atan2(num, den);
            atan = (float)Math.toDegrees(atan) % 360;
            if (atan < 0)
            {
                atan = atan + 360;
            }
            context.getStack().push(atan);
        }

    }

    /** Implements the "ceiling" operator. */
    static class Ceiling implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                context.getStack().push(num);
            }
            else
            {
                context.getStack().push((float)Math.ceil(num.doubleValue()));
            }
        }

    }

    /** Implements the "cos" operator. */
    static class Cos implements Operator
    {

        public void execute(ExecutionContext context)
        {
            float angle = context.popReal();
            float cos = (float)Math.cos(Math.toRadians(angle));
            context.getStack().push(cos);
        }

    }

    /** Implements the "cvi" operator. */
    static class Cvi implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            context.getStack().push(num.intValue());
        }

    }

    /** Implements the "cvr" operator. */
    static class Cvr implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            context.getStack().push(num.floatValue());
        }

    }

    /** Implements the "div" operator. */
    static class Div implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num2 = context.popNumber();
            Number num1 = context.popNumber();
            context.getStack().push(num1.floatValue() / num2.floatValue());
        }

    }

    /** Implements the "exp" operator. */
    static class Exp implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number exp = context.popNumber();
            Number base = context.popNumber();
            double value = Math.pow(base.doubleValue(), exp.doubleValue());
            context.getStack().push((float)value);
        }

    }

    /** Implements the "floor" operator. */
    static class Floor implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                context.getStack().push(num);
            }
            else
            {
                context.getStack().push((float)Math.floor(num.doubleValue()));
            }
        }

    }

    /** Implements the "idiv" operator. */
    static class IDiv implements Operator
    {

        public void execute(ExecutionContext context)
        {
            int num2 = context.popInt();
            int num1 = context.popInt();
            context.getStack().push(num1 / num2);
        }

    }

    /** Implements the "ln" operator. */
    static class Ln implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            context.getStack().push((float)Math.log(num.doubleValue()));
        }

    }

    /** Implements the "log" operator. */
    static class Log implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            context.getStack().push((float)Math.log10(num.doubleValue()));
        }

    }

    /** Implements the "mod" operator. */
    static class Mod implements Operator
    {

        public void execute(ExecutionContext context)
        {
            int int2 = context.popInt();
            int int1 = context.popInt();
            context.getStack().push(int1 % int2);
        }

    }

    /** Implements the "mul" operator. */
    static class Mul implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num2 = context.popNumber();
            Number num1 = context.popNumber();
            if (num1 instanceof Integer && num2 instanceof Integer)
            {
                long result = num1.longValue() * num2.longValue();
                if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
                {
                    context.getStack().push((int)result);
                }
                else
                {
                    context.getStack().push((float)result);
                }
            }
            else
            {
                double result = num1.doubleValue() * num2.doubleValue();
                context.getStack().push((float)result);
            }
        }

    }

    /** Implements the "neg" operator. */
    static class Neg implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                int v = num.intValue();
                if (v == Integer.MIN_VALUE)
                {
                    context.getStack().push(-num.floatValue());
                }
                else
                {
                    context.getStack().push(-num.intValue());
                }
            }
            else
            {
                context.getStack().push(-num.floatValue());
            }
        }

    }

    /** Implements the "round" operator. */
    static class Round implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                context.getStack().push(num.intValue());
            }
            else
            {
                context.getStack().push((float)Math.round(num.doubleValue()));
            }
        }

    }

    /** Implements the "sin" operator. */
    static class Sin implements Operator
    {

        public void execute(ExecutionContext context)
        {
            float angle = context.popReal();
            float sin = (float)Math.sin(Math.toRadians(angle));
            context.getStack().push(sin);
        }

    }

    /** Implements the "sqrt" operator. */
    static class Sqrt implements Operator
    {

        public void execute(ExecutionContext context)
        {
            float num = context.popReal();
            if (num < 0)
            {
                throw new IllegalArgumentException("argument must be nonnegative");
            }
            context.getStack().push((float)Math.sqrt(num));
        }

    }

    /** Implements the "sub" operator. */
    static class Sub implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Number num2 = context.popNumber();
            Number num1 = context.popNumber();
            if (num1 instanceof Integer && num2 instanceof Integer)
            {
                long result = num1.longValue() - num2.longValue();
                if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE)
                {
                    stack.push((float) result);
                }
                else
                {
                    stack.push((int)result);
                }
            }
            else
            {
                float result = num1.floatValue() - num2.floatValue();
                stack.push(result);
            }
        }

    }

    /** Implements the "truncate" operator. */
    static class Truncate implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Number num = context.popNumber();
            if (num instanceof Integer)
            {
                context.getStack().push(num.intValue());
            }
            else
            {
                context.getStack().push((float)(int)(num.floatValue()));
            }
        }

    }

}
