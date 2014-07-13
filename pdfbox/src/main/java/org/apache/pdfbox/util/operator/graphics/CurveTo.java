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
package org.apache.pdfbox.util.operator.graphics;

import java.io.IOException;
import java.util.List;
import java.awt.geom.Point2D;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.operator.Operator;

/**
 * c Append curved segment to path.
 *
 * @author Ben Litchfield
 */
public class CurveTo extends GraphicsOperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        COSNumber x1 = (COSNumber)operands.get(0);
        COSNumber y1 = (COSNumber)operands.get(1);
        COSNumber x2 = (COSNumber)operands.get(2);
        COSNumber y2 = (COSNumber)operands.get(3);
        COSNumber x3 = (COSNumber)operands.get(4);
        COSNumber y3 = (COSNumber)operands.get(5);

        Point2D point1 = context.transformedPoint(x1.doubleValue(), y1.doubleValue());
        Point2D point2 = context.transformedPoint(x2.doubleValue(), y2.doubleValue());
        Point2D point3 = context.transformedPoint(x3.doubleValue(), y3.doubleValue());

        context.curveTo((float) point1.getX(), (float) point1.getY(),
                        (float) point2.getX(), (float) point2.getY(),
                        (float) point3.getX(), (float) point3.getY());
    }
}
