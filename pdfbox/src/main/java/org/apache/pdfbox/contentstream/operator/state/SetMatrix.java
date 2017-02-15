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
package org.apache.pdfbox.contentstream.operator.state;

import java.util.List;

import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.Matrix;

/**
 * Tm: Set text matrix and text line matrix.
 *
 * @author Laurent Huault
 */
public class SetMatrix extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws MissingOperandException
    {
        if (arguments.size() < 6)
        {
            throw new MissingOperandException(operator, arguments);
        }
        if (!checkArrayTypesClass(arguments, COSNumber.class))
        {
            return;
        }

        COSNumber a = (COSNumber)arguments.get( 0 );
        COSNumber b = (COSNumber)arguments.get( 1 );
        COSNumber c = (COSNumber)arguments.get( 2 );
        COSNumber d = (COSNumber)arguments.get( 3 );
        COSNumber e = (COSNumber)arguments.get( 4 );
        COSNumber f = (COSNumber)arguments.get( 5 );

        Matrix matrix = new Matrix(a.floatValue(), b.floatValue(), c.floatValue(),
                                   d.floatValue(), e.floatValue(), f.floatValue());

        context.setTextMatrix(matrix);
        context.setTextLineMatrix(matrix.clone());
    }

    @Override
    public String getName()
    {
        return "Tm";
    }
}
