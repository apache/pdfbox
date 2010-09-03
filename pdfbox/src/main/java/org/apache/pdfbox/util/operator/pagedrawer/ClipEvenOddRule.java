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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:Daniel.Wilson@BlackLocustSoftware.com">Daniel Wilson</a>
 * @version $Revision: 1.1 $
 */
public class ClipEvenOddRule extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(ClipEvenOddRule.class);

    /**
     * process : W* : set clipping path using even odd rule.
     * @param operator The operator that is being executed.
     * @param arguments List
     *
     * @throws IOException if there is an error during execution.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {

        try 
        {
            PageDrawer drawer = (PageDrawer)context;
            drawer.setClippingPath(GeneralPath.WIND_EVEN_ODD);
        } 
        catch (Exception e) 
        {
            log.warn(e, e);
        }
    }
}
