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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.util.PDFOperator;

/**
 * Process the Q operator.
 * 
 * @author Huault : huault@free.fr
 * 
 */
public class GRestore extends OperatorProcessor
{
	
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(GRestore.class);

    /**
     * {@inheritDoc}
     */
    public void process(PDFOperator operator, List<COSBase> arguments)
    {
    	if (context.getGraphicsStack().size() > 0)
    	{
    		context.setGraphicsState( (PDGraphicsState)context.getGraphicsStack().pop() );
    	}
    	else
    	{
    		// this shouldn't happen but it does, see PDFBOX-161
    		// TODO make this self healing mechanism optional for preflight??
    		LOG.debug("GRestore: no graphics state left to be restored.");
    	}
    }
}
