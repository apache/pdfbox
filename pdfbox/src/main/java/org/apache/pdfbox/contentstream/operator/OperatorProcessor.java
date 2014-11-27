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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes a PDF operator.
 *
 * @author Laurent Huault
 */
public abstract class OperatorProcessor
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(OperatorProcessor.class);

    /** The processing context. */
    protected PDFStreamEngine context;

    /**
     * Creates a new OperatorProcessor.
     */
    protected OperatorProcessor()
    {
    }

    /**
     * Returns the processing context.
     * @return the processing context
     */
    protected PDFStreamEngine getContext()
    {
        return context;
    }

    /**
     * Sets the processing context.
     * @param context the processing context.
     */
    public void setContext(PDFStreamEngine context)
    {
        this.context = context;
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
     * @return operator name.
     */
    public abstract String getName();

    /**
     * Check the size of the arguments and puts out a warning if the size doesn't match.
     *
     * @param arguments Arguments for this operator.
     * @param expectedSize Expected arguments size.
     * @return true if size is correct, false if not.
     */
    protected boolean checkArgumentSize(List<COSBase> arguments, int expectedSize)
    {
        if (arguments.size() != expectedSize)
        {
            LOG.warn("'" + getName() + "' operator must have " + expectedSize
                    + " parameters, but has " + arguments.size());
            return false;
}
        return true;
    }
}
