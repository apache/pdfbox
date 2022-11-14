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

import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;

/**
 * Tw: Set word spacing.
 *
 * @author Laurent Huault
 */
public class SetWordSpacing extends OperatorProcessor
{
    public SetWordSpacing(PDFStreamEngine context)
    {
        super(context);
    }

    @Override
    public void process(Operator operator, List<COSBase> arguments)
    {
        if (arguments.isEmpty())
        {
            return;
        }
        COSBase base = arguments.get(0);
        if (!(base instanceof COSNumber))
        {
            return;
        }
        COSNumber wordSpacing = (COSNumber) base;
        getContext().getGraphicsState().getTextState().setWordSpacing(wordSpacing.floatValue());
    }

    @Override
    public String getName()
    {
        return OperatorName.SET_WORD_SPACING;
    }
}
