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
package org.apache.pdfbox.contentstream.operator.graphics;

import java.io.IOException;
import java.util.List;
import java.awt.geom.Point2D;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;

/**
 * c Append curved segment to path.
 *
 * @author Ben Litchfield
 */
public class CurveTo extends GraphicsOperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(CurveTo.class);
    
    @Override
    public void process(final Operator operator, final List<COSBase> operands) throws IOException
    {
        if (operands.size() < 6)
        {
            throw new MissingOperandException(operator, operands);
        }
        if (!checkArrayTypesClass(operands, COSNumber.class))
        {
            return;
        }
        final COSNumber x1 = (COSNumber)operands.get(0);
        final COSNumber y1 = (COSNumber)operands.get(1);
        final COSNumber x2 = (COSNumber)operands.get(2);
        final COSNumber y2 = (COSNumber)operands.get(3);
        final COSNumber x3 = (COSNumber)operands.get(4);
        final COSNumber y3 = (COSNumber)operands.get(5);

        final Point2D.Float point1 = context.transformedPoint(x1.floatValue(), y1.floatValue());
        final Point2D.Float point2 = context.transformedPoint(x2.floatValue(), y2.floatValue());
        final Point2D.Float point3 = context.transformedPoint(x3.floatValue(), y3.floatValue());

        if (context.getCurrentPoint() == null)
        {
            LOG.warn("curveTo (" + point3.x + "," + point3.y + ") without initial MoveTo");
            context.moveTo(point3.x, point3.y);
        }
        else
        {
            context.curveTo(point1.x, point1.y,
                    point2.x, point2.y,
                    point3.x, point3.y);
        }
    }

    @Override
    public String getName()
    {
        return OperatorName.CURVE_TO;
    }
}
