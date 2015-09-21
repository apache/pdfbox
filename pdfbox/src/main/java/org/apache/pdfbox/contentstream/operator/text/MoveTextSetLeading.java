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
package org.apache.pdfbox.contentstream.operator.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;

/**
 * TD: Move text position and set leading.
 *
 * @author Laurent Huault
 */
public class MoveTextSetLeading extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        if (arguments.size() < 2)
        {
            throw new MissingOperandException(operator, arguments);
        }
        
        //move text position and set leading
        COSBase base1 = arguments.get(1);
        if (!(base1 instanceof COSNumber))
        {
            return;
        }
        COSNumber y = (COSNumber) base1;
        
        List<COSBase> args = new ArrayList<COSBase>();
        args.add(new COSFloat(-1 * y.floatValue()));
        context.processOperator("TL", args);
        context.processOperator("Td", arguments);
    }

    @Override
    public String getName()
    {
        return "TD";
    }
}
