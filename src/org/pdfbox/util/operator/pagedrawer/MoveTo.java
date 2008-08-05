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

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
import java.io.IOException;

import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdfviewer.PageDrawer;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class MoveTo extends OperatorProcessor
{


    /**
     * process : m : Begin new subpath.
     * @param operator The operator that is being executed.
     * @param arguments List
     */
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        try{
            PageDrawer drawer = (PageDrawer)context;

            COSNumber x = (COSNumber)arguments.get( 0 );
            COSNumber y = (COSNumber)arguments.get( 1 );

            drawer.getLineSubPaths().add( drawer.getLinePath() );
            GeneralPath newPath = new GeneralPath();
            Point2D Ppos = drawer.TransformedPoint(x.doubleValue(), y.doubleValue());

            //newPath.moveTo( x.floatValue(), (float)drawer.fixY( x.doubleValue(), y.doubleValue()) );
            //logger().info("Ready to move to " + Ppos.getX() + ", " + Ppos.getY());

            newPath.moveTo((float)Ppos.getX(), (float)Ppos.getY());

            drawer.setLinePath( newPath );
        }catch (Exception E){
            logger().warning( E.toString() + "/n at/n" + FullStackTrace(E));
        }
    }
}
