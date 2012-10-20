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
package org.apache.pdfbox.util.operator;

import java.util.List;
import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;

/**
 *
 * @author Huault : huault@free.fr
 * @version $Revision: 1.5 $
 */

public class Concatenate extends OperatorProcessor
{

    /**
     * process : cm : Concatenate matrix to current transformation matrix.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error processing the operator.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {

        //concatenate matrix to current transformation matrix
        COSNumber a = (COSNumber) arguments.get(0);
        COSNumber b = (COSNumber) arguments.get(1);
        COSNumber c = (COSNumber) arguments.get(2);
        COSNumber d = (COSNumber) arguments.get(3);
        COSNumber e = (COSNumber) arguments.get(4);
        COSNumber f = (COSNumber) arguments.get(5);

        Matrix newMatrix = new Matrix();
        newMatrix.setValue(0, 0, a.floatValue());
        newMatrix.setValue(0, 1, b.floatValue());
        newMatrix.setValue(1, 0, c.floatValue());
        newMatrix.setValue(1, 1, d.floatValue());
        newMatrix.setValue(2, 0, e.floatValue());
        newMatrix.setValue(2, 1, f.floatValue());

        //this line has changed
        context.getGraphicsState().setCurrentTransformationMatrix(
                newMatrix.multiply( context.getGraphicsState().getCurrentTransformationMatrix() ) );


    }
}
