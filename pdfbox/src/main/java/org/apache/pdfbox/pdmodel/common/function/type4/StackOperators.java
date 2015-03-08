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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Provides the stack operators such as "pop" and "dup".
 *
 */
class StackOperators
{

    /** Implements the "copy" operator. */
    static class Copy implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            int n = ((Number)stack.pop()).intValue();
            if (n > 0)
            {
                int size = stack.size();
                //Need to copy to a new list to avoid ConcurrentModificationException
                List<Object> copy = new java.util.ArrayList<Object>(
                        stack.subList(size - n, size));
                stack.addAll(copy);
            }
        }

    }

    /** Implements the "dup" operator. */
    static class Dup implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            stack.push(stack.peek());
        }

    }

    /** Implements the "exch" operator. */
    static class Exch implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            Object any2 = stack.pop();
            Object any1 = stack.pop();
            stack.push(any2);
            stack.push(any1);
        }

    }

    /** Implements the "index" operator. */
    static class Index implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            int n = ((Number)stack.pop()).intValue();
            if (n < 0)
            {
                throw new IllegalArgumentException("rangecheck: " + n);
            }
            int size = stack.size();
            stack.push(stack.get(size - n - 1));
        }

    }

    /** Implements the "pop" operator. */
    static class Pop implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            stack.pop();
        }

    }

    /** Implements the "roll" operator. */
    static class Roll implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            int j = ((Number)stack.pop()).intValue();
            int n = ((Number)stack.pop()).intValue();
            if (j == 0)
            {
                return; //Nothing to do
            }
            if (n < 0)
            {
                throw new IllegalArgumentException("rangecheck: " + n);
            }

            LinkedList<Object> rolled = new LinkedList<Object>();
            LinkedList<Object> moved = new LinkedList<Object>();
            if (j < 0)
            {
                //negative roll
                int n1 = n + j;
                for (int i = 0; i < n1; i++)
                {
                    moved.addFirst(stack.pop());
                }
                for (int i = j; i < 0; i++)
                {
                    rolled.addFirst(stack.pop());
                }
                stack.addAll(moved);
                stack.addAll(rolled);
            }
            else
            {
                //positive roll
                int n1 = n - j;
                for (int i = j; i > 0; i--)
                {
                    rolled.addFirst(stack.pop());
                }
                for (int i = 0; i < n1; i++)
                {
                    moved.addFirst(stack.pop());
                }
                stack.addAll(rolled);
                stack.addAll(moved);
            }
        }

    }

}
