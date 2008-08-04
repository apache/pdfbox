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

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdfviewer.PageDrawer;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class AppendRectangleToPath extends OperatorProcessor
{


    /**
     * process : re : append rectangle to path.
     * @param operator The operator that is being executed.
     * @param arguments List
     */
    public void process(PDFOperator operator, List arguments) 
    {
        PageDrawer drawer = (PageDrawer)context;
        
        COSNumber x = (COSNumber)arguments.get( 0 );
        COSNumber y = (COSNumber)arguments.get( 1 );
        COSNumber w = (COSNumber)arguments.get( 2 );
        COSNumber h = (COSNumber)arguments.get( 3 );
        double finalY = drawer.fixY( x.doubleValue(), y.doubleValue())-h.doubleValue();
        double finalH = h.doubleValue();
        if( finalH < 0 )
        {
            finalY += finalH;
            finalH = Math.abs( finalH );
        }
        Rectangle2D rect = new Rectangle2D.Double(
            x.doubleValue(),
            finalY,
            w.doubleValue()+1,
            finalH+1);
        drawer.getLinePath().reset();
        
        //System.out.println( "Bounds before=" + drawer.getLinePath().getBounds() );
        drawer.getLinePath().append( rect, false );
        //graphics.drawRect((int)x.doubleValue(), (int)(pageSize.getHeight() - y.doubleValue()),
        //                  (int)w.doubleValue(),(int)h.doubleValue() );
        //System.out.println( "<re x=\"" + x.getValue() + "\" y=\"" + y.getValue() + "\" width=\"" +
        //                                 width.getValue() + "\" height=\"" + height.getValue() + "\" >" );
    }
}
