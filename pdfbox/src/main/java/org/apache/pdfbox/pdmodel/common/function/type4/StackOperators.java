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

    private StackOperators()
    {
        // Private constructor.
    }

    /** Implements the "copy" operator. */
    static class Copy implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final int n = ((Number)stack.pop()).intValue();
            if (n > 0)
            {
                final int size = stack.size();
                //Need to copy to a new list to avoid ConcurrentModificationException
                final List<Object> copy = new java.util.ArrayList<>(
                        stack.subList(size - n, size));
                stack.addAll(copy);
            }
        }

    }

    /** Implements the "dup" operator. */
    static class Dup implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            stack.push(stack.peek());
        }

    }

    /** Implements the "exch" operator. */
    static class Exch implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final Object any2 = stack.pop();
            final Object any1 = stack.pop();
            stack.push(any2);
            stack.push(any1);
        }

    }

    /** Implements the "index" operator. */
    static class Index implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final int n = ((Number)stack.pop()).intValue();
            if (n < 0)
            {
                throw new IllegalArgumentException("rangecheck: " + n);
            }
            final int size = stack.size();
            stack.push(stack.get(size - n - 1));
        }

    }

    /** Implements the "pop" operator. */
    static class Pop implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            stack.pop();
        }

    }

    /** Implements the "roll" operator. */
    static class Roll implements Operator
    {
        @Override
        public void execute(final ExecutionContext context)
        {
            final Stack<Object> stack = context.getStack();
            final int j = ((Number)stack.pop()).intValue();
            final int n = ((Number)stack.pop()).intValue();
            if (j == 0)
            {
                return; //Nothing to do
            }
            if (n < 0)
            {
                throw new IllegalArgumentException("rangecheck: " + n);
            }

            final LinkedList<Object> rolled = new LinkedList<>();
            final LinkedList<Object> moved = new LinkedList<>();
            if (j < 0)
            {
                //negative roll
                final int n1 = n + j;
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
                final int n1 = n - j;
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
