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
package org.apache.pdfbox.pdmodel.common.function;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.function.type4.ExecutionContext;
import org.apache.pdfbox.pdmodel.common.function.type4.InstructionSequence;
import org.apache.pdfbox.pdmodel.common.function.type4.InstructionSequenceBuilder;
import org.apache.pdfbox.pdmodel.common.function.type4.Operators;

import java.io.IOException;

/**
 * This class represents a type 4 function in a PDF document.
 * <p>
 * See section 3.9.4 of the PDF 1.4 Reference.
 *
 * @version $Revision: 1.2 $
 */
public class PDFunctionType4 extends PDFunction
{

    private static final Operators OPERATORS = new Operators();

    private final InstructionSequence instructions;

    /**
     * Constructor.
     *
     * @param functionStream The function stream.
     * @throws IOException if an I/O error occurs while reading the function
     */
    public PDFunctionType4(COSBase functionStream) throws IOException
    {
        super( functionStream );
        this.instructions = InstructionSequenceBuilder.parse(
                getPDStream().getInputStreamAsString());
    }


    /**
     * {@inheritDoc}
     */
    public int getFunctionType()
    {
        return 4;
    }

    /**
    * {@inheritDoc}
    */
    public float[] eval(float[] input) throws IOException
    {
        //Setup the input values
        int numberOfInputValues = input.length;
        ExecutionContext context = new ExecutionContext(OPERATORS);
        for (int i = numberOfInputValues - 1; i >= 0; i--)
        {
            PDRange domain = getDomainForInput(i);
            float value = clipToRange(input[i], domain.getMin(), domain.getMax());
            context.getStack().push(value);
        }

        //Execute the type 4 function.
        instructions.execute(context);

        //Extract the output values
        int numberOfOutputValues = getNumberOfOutputParameters();
        int numberOfActualOutputValues = context.getStack().size();
        if (numberOfActualOutputValues < numberOfOutputValues)
        {
            throw new IllegalStateException("The type 4 function returned "
                    + numberOfActualOutputValues
                    + " values but the Range entry indicates that "
                    + numberOfOutputValues + " values be returned.");
        }
        float[] outputValues = new float[numberOfOutputValues];
        for (int i = numberOfOutputValues - 1; i >= 0; i--)
        {
            PDRange range = getRangeForOutput(i);
            outputValues[i] = context.popReal();
            outputValues[i] = clipToRange(outputValues[i], range.getMin(), range.getMax());
        }

        //Return the resulting array
        return outputValues;
    }
}
