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
package org.apache.pdfbox.contentstream.operator;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.util.List;
import java.io.IOException;

/**
 * Processes a PDF operator.
 *
 * @author Laurent Huault
 */
public abstract class OperatorProcessor
{
    /** The processing context. */
    private final PDFStreamEngine context;

    /**
     * Creates a new OperatorProcessor.
     * 
     * @param context the processing context to be used
     */
    protected OperatorProcessor(PDFStreamEngine context)
    {
        this.context = context;
    }

    /**
     * Returns the processing context.
     * 
     * @return the processing context
     */
    protected final PDFStreamEngine getContext()
    {
        return context;
    }

    /**
     * Process the operator.
     * @param operator the operator to process
     * @param operands the operands to use when processing
     * @throws IOException if the operator cannot be processed
     */
    public abstract void process(Operator operator, List<COSBase> operands) throws IOException;

    /**
     * Returns the name of this operator, e.g. "BI".
     * 
     * @return the name of the operator
     */
    public abstract String getName();
    
    /**
     * Check whether all operands list elements are an instance of a specific class.
     *
     * @param operands The operands list.
     * @param clazz The expected class.
     * @return true if all operands list elements are an instance of the given class
     */
    public boolean checkArrayTypesClass(List<COSBase> operands, Class<?> clazz)
    {
        return operands.stream().allMatch(clazz::isInstance);
    }
}
