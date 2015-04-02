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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic parser for Type 4 functions which is used to build up instruction sequences.
 *
 * @version $Revision$
 */
public class InstructionSequenceBuilder extends Parser.AbstractSyntaxHandler
{

    private InstructionSequence mainSequence = new InstructionSequence();
    private Stack<InstructionSequence> seqStack = new Stack<InstructionSequence>();

    private InstructionSequenceBuilder()
    {
        this.seqStack.push(this.mainSequence);
    }

    /**
     * Returns the instruction sequence that has been build from the syntactic elements.
     * @return the instruction sequence
     */
    public InstructionSequence getInstructionSequence()
    {
        return this.mainSequence;
    }

    /**
     * Parses the given text into an instruction sequence representing a Type 4 function
     * that can be executed.
     * @param text the Type 4 function text
     * @return the instruction sequence
     */
    public static InstructionSequence parse(CharSequence text)
    {
        InstructionSequenceBuilder builder = new InstructionSequenceBuilder();
        Parser.parse(text, builder);
        return builder.getInstructionSequence();
    }

    private InstructionSequence getCurrentSequence()
    {
        return this.seqStack.peek();
    }

    private static final Pattern INTEGER_PATTERN = Pattern.compile("[\\+\\-]?\\d+");
    private static final Pattern REAL_PATTERN = Pattern.compile("[\\-]?\\d*\\.\\d*([Ee]\\-?\\d+)?");

    /** {@inheritDoc} */
    public void token(CharSequence text)
    {
        String token = text.toString();
        token(token);
    }

    private void token(String token)
    {
        if ("{".equals(token))
        {
            InstructionSequence child = new InstructionSequence();
            getCurrentSequence().addProc(child);
            this.seqStack.push(child);
        }
        else if ("}".equals(token))
        {
            this.seqStack.pop();
        }
        else
        {
            Matcher m = INTEGER_PATTERN.matcher(token);
            if (m.matches())
            {
                getCurrentSequence().addInteger(parseInt(token.toString()));
                return;
            }

            m = REAL_PATTERN.matcher(token);
            if (m.matches())
            {
                getCurrentSequence().addReal(parseReal(token));
                return;
            }

            //TODO Maybe implement radix numbers, such as 8#1777 or 16#FFFE

            getCurrentSequence().addName(token.toString());
        }
    }

    /**
     * Parses a value of type "int".
     * @param token the token to be parsed
     * @return the parsed value
     */
    public static int parseInt(String token)
    {
        if (token.startsWith("+"))
        {
            token = token.substring(1);
        }
        return Integer.parseInt(token);
    }

    /**
     * Parses a value of type "real".
     * @param token the token to be parsed
     * @return the parsed value
     */
    public static float parseReal(String token)
    {
        return Float.parseFloat(token);
    }

}
