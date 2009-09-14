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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.IOException;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class StrokePath extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(StrokePath.class);

    /**
     * S stroke the path.
     * @param operator The operator that is being executed.
     * @param arguments List
     *
     * @throws IOException If an error occurs while processing the font.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        ///dwilson 3/19/07 refactor
        try
        {
            PageDrawer drawer = (PageDrawer)context;
    
            float lineWidth = (float)context.getGraphicsState().getLineWidth();
            Matrix ctm = context.getGraphicsState().getCurrentTransformationMatrix();
            if ( ctm != null && ctm.getXScale() > 0) 
            {
                lineWidth = lineWidth * ctm.getXScale();
            }
            Graphics2D graphics = ((PageDrawer)context).getGraphics();
            BasicStroke stroke = (BasicStroke)graphics.getStroke();
            if (stroke == null)
            {
                graphics.setStroke( new BasicStroke( lineWidth ) );
            }
            else
            {
                graphics.setStroke( new BasicStroke(lineWidth, stroke.getEndCap(), stroke.getLineJoin(), 
                        stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()) );
            }
            drawer.strokePath();
        }
        catch (Exception exception)
        {
            log.warn(exception, exception);
        }
    }
}
