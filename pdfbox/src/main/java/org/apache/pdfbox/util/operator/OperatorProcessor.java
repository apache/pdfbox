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
package org.apache.pdfbox.util.operator;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import java.util.List;
import java.io.IOException;

/**
 * @author Huault : huault@free.fr
 * @version $Revision: 1.3 $
 */
public abstract class OperatorProcessor
{

    /**
     * The stream engine processing context.
     */
    protected PDFStreamEngine context = null;

    /**
     * Constructor.
     *
     */
    protected OperatorProcessor()
    {
    }

    /**
     * Get the context for processing.
     *
     * @return The processing context.
     */
    protected PDFStreamEngine getContext()
    {
        return context;
    }

    /**
     * Set the processing context.
     *
     * @param ctx The context for processing.
     */
    public void setContext(PDFStreamEngine ctx)
    {
        context = ctx;
    }

    /**
     * process the operator.
     * @param operator The operator that is being processed.
     * @param arguments arguments needed by this operator.
     *
     * @throws IOException If there is an error processing the operator.
     */
    public abstract void process(PDFOperator operator, List<COSBase> arguments) throws IOException;
}
