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
import java.awt.geom.Point2D;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * y Append curved segment to path with final point replicated.
 * @author Ben Litchfield
 */
public final class CurveToReplicateFinalPoint extends OperatorProcessor
{
    @Override
    public void process(PDFOperator operator, List<COSBase> operands)
    {
        PageDrawer drawer = (PageDrawer)context;

        COSNumber x1 = (COSNumber)operands.get(0);
        COSNumber y1 = (COSNumber)operands.get(1);
        COSNumber x3 = (COSNumber)operands.get(2);
        COSNumber y3 = (COSNumber)operands.get(3);

        Point2D point1 = drawer.transformedPoint(x1.doubleValue(), y1.doubleValue());
        Point2D point3 = drawer.transformedPoint(x3.doubleValue(), y3.doubleValue());

        drawer.getLinePath().curveTo((float)point1.getX(), (float)point1.getY(), 
                                     (float)point3.getX(), (float)point3.getY(),
                                     (float)point3.getX(), (float)point3.getY());
    }
}
