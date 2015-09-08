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
package org.apache.pdfbox_ai2.contentstream.operator.text;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox_ai2.contentstream.operator.MissingOperandException;
import org.apache.pdfbox_ai2.contentstream.operator.Operator;
import org.apache.pdfbox_ai2.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSNumber;
import org.apache.pdfbox_ai2.util.Matrix;

/**
 * Td: Move text position.
 *
 * @author Laurent Huault
 */
public class MoveText extends OperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(MoveText.class);

    @Override
    public void process(Operator operator, List<COSBase> arguments) throws MissingOperandException
    {
        if (arguments.size() < 2)
        {
            throw new MissingOperandException(operator, arguments);
        }
        Matrix textLineMatrix = context.getTextLineMatrix();
        if (textLineMatrix == null)
        {
            LOG.warn("TextLineMatrix is null, " + getName() + " operator will be ignored");
            return;
        }        
        
        COSNumber x = (COSNumber)arguments.get( 0 );
        COSNumber y = (COSNumber)arguments.get( 1 );

        Matrix matrix = new Matrix(1, 0, 0, 1, x.floatValue(), y.floatValue());
        textLineMatrix.concatenate(matrix);
        context.setTextMatrix(textLineMatrix.clone());
    }

    @Override
    public String getName()
    {
        return "Td";
    }
}
