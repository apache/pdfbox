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
package org.apache.fontbox_ai2.cff;


import java.util.List;
import java.util.Stack;

/**
 * A Handler for CharStringCommands.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 * 
 */
public abstract class CharStringHandler
{
    /**
     * Handler for a sequence of CharStringCommands.
     *
     * @param sequence of CharStringCommands
     *
     */
    public List<Integer> handleSequence(List<Object> sequence)
    {
        Stack<Integer> stack = new Stack<Integer>();
        for (Object obj : sequence)
        {
            if (obj instanceof CharStringCommand)
            {
                List<Integer> results = handleCommand(stack, (CharStringCommand)obj);
                stack.clear();  // this is basically returning the new stack
                if (results != null)
                {
                    stack.addAll(results);
                }
            }
            else
            {
                stack.push((Integer)obj);
            }
        }
        return stack;
    }

    /**
     * Handler for CharStringCommands.
     *
     * @param numbers a list of numbers
     * @param command the CharStringCommand
     */
    public abstract List<Integer> handleCommand(List<Integer> numbers, CharStringCommand command);
}