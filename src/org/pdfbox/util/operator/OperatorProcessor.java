/**
 * Copyright (c) 2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.util.operator;

import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.PDFStreamEngine;
import java.util.List;
import java.io.IOException;

/**
 *
 * <p>Titre : OperatorProcessor</p>
 * <p>Description : This class is the strategy abstract class
 * in the strategy GOF pattern. After instancated, you must ever call
* the setContext method to initiamise OPeratorProcessor</p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Société : DBGS</p>
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
    public abstract void process(PDFOperator operator, List arguments) throws IOException;
}
