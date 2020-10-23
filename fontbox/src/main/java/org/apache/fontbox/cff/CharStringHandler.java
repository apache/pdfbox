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
package org.apache.fontbox.cff;

import java.util.ArrayList;
import java.util.List;

/**
 * A Handler for CharStringCommands.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 * 
 */
@FunctionalInterface
public interface CharStringHandler
{
    /**
     * Handler for a sequence of CharStringCommands.
     *
     * @param sequence of CharStringCommands
     *
     */
    default void handleSequence(List<Object> sequence)
    {
        List<Number> numbers = new ArrayList<>();
        sequence.forEach(obj ->
        {
            if (obj instanceof CharStringCommand)
            {
                List<Number> results = handleCommand(numbers, (CharStringCommand) obj);
                numbers.clear();
                numbers.addAll(results);
            }
            else
            {
                numbers.add((Number) obj);
            }
        });
    }

    /**
     * Handler for CharStringCommands.
     *
     * @param numbers a list of numbers
     * @param command the CharStringCommand
     * @return a list of commands. This can be empty but never be null.
     */
    List<Number> handleCommand(List<Number> numbers, CharStringCommand command);
}