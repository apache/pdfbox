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
package org.pdfbox.util.operator.pagedrawer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;

import org.pdfbox.pdfviewer.PageDrawer;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class FillEvenOddRule extends OperatorProcessor
{


    /**
     * process : f* : fill path using even odd rule.
     * @param operator The operator that is being executed.
     * @param arguments List
     * 
     * @throws IOException if there is an error during execution.
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
//      NOTE:changes here should probably also be made to FillNonZeroRule
        PageDrawer drawer = (PageDrawer)context;
        Graphics2D graphics = drawer.getGraphics();
        //linePath.closePath();
        graphics.setColor( drawer.getGraphicsState().getNonStrokingColorSpace().createColor() );
        drawer.getLinePath().setWindingRule( GeneralPath.WIND_EVEN_ODD );
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        List subPaths = drawer.getLineSubPaths();
        for( int i=0; i<subPaths.size(); i++ )
        {
            GeneralPath subPath = (GeneralPath)subPaths.get( i );
            subPath.closePath();
            graphics.fill( subPath );
        }
        //else
        //{
            graphics.fill( drawer.getLinePath() );
        //}
            drawer.getLinePath().reset();
    }
}
