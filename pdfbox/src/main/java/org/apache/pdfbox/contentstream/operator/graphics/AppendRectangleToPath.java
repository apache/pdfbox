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

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.contentstream.operator.Operator;

/**
 * re Appends a rectangle to the path.
 *
 * @author Ben Litchfield
 */
public final class AppendRectangleToPath extends GraphicsOperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        if (operands.size() < 4)
        {
            throw new MissingOperandException(operator, operands);
        }
        if (!checkArrayTypesClass(operands, COSNumber.class))
        {
            return;
        }
        COSNumber x = (COSNumber) operands.get(0);
        COSNumber y = (COSNumber) operands.get(1);
        COSNumber w = (COSNumber) operands.get(2);
        COSNumber h = (COSNumber) operands.get(3);

        float x1 = x.floatValue();
        float y1 = y.floatValue();

        // create a pair of coordinates for the transformation
        float x2 = w.floatValue() + x1;
        float y2 = h.floatValue() + y1;

        Point2D p0 = context.transformedPoint(x1, y1);
        Point2D p1 = context.transformedPoint(x2, y1);
        Point2D p2 = context.transformedPoint(x2, y2);
        Point2D p3 = context.transformedPoint(x1, y2);

        context.appendRectangle(p0, p1, p2, p3);
    }

    @Override
    public String getName()
    {
        return "re";
    }
}
