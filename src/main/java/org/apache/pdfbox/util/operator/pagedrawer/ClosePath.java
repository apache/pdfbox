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
package org.apache.pdfbox.util.operator.pagedrawer;

import java.util.List;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class ClosePath extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(ClosePath.class);

    /**
     * process : h : Close path.
     * @param operator The operator that is being executed.
     * @param arguments List
     * 
     * @throws IOException if something went wrong during logging
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        try
        {
            drawer.getLinePath().closePath();
        }
        catch( Throwable t )
        {
            log.warn(t, t);
        }
    }
}
