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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.PDFOperator;

/**
 *
 * @author Huault : huault@free.fr
 * @version $Revision: 1.5 $
 */
public class MoveTextSetLeading extends OperatorProcessor
{

    /**
     * process : TD Move text position and set leading.
     * @param operator The operator that is being executed.
     * @param arguments List
     *
     * @throws IOException If there is an error during processing.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        //move text position and set leading
        COSNumber y = (COSNumber)arguments.get( 1 );

        ArrayList<COSBase> args = new ArrayList<COSBase>();
        args.add(new COSFloat(-1*y.floatValue()));
        context.processOperator("TL", args);
        context.processOperator("Td", arguments);

    }
}
