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
 * Provides the conditional operators such as "if" and "ifelse".
 *
 * @version $Revision$
 */
class ConditionalOperators
{

    /** Implements the "if" operator. */
    static class If implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            InstructionSequence proc = (InstructionSequence)stack.pop();
            Boolean condition = (Boolean)stack.pop();
            if (condition)
            {
                proc.execute(context);
            }
        }

    }

    /** Implements the "ifelse" operator. */
    static class IfElse implements Operator
    {

        public void execute(ExecutionContext context)
        {
            Stack<Object> stack = context.getStack();
            InstructionSequence proc2 = (InstructionSequence)stack.pop();
            InstructionSequence proc1 = (InstructionSequence)stack.pop();
            Boolean condition = (Boolean)stack.pop();
            if (condition)
            {
                proc1.execute(context);
            }
            else
            {
                proc2.execute(context);
            }
        }

    }

}
