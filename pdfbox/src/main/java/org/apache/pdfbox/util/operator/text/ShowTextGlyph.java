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
package org.apache.pdfbox.util.operator.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import java.io.IOException;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.util.operator.Operator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * TJ: Show text, with position adjustments.
 *
 * @author Laurent Huault
 */
public class ShowTextGlyph extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        COSArray array = (COSArray)arguments.get(0);

        List<Float> adjustments = new ArrayList<Float>();
        List<byte[]> strings = new ArrayList<byte[]>();
        boolean lastWasAdjustment = false;

        for(int i = 0, len = array.size(); i < len; i++)
        {
            COSBase next = array.get(i);
            if(next instanceof COSNumber)
            {
                adjustments.add(((COSNumber)next).floatValue());
                lastWasAdjustment = true;
            }
            else if(next instanceof COSString)
            {
                if (!lastWasAdjustment)
                {
                    adjustments.add(0f);
                }
                strings.add(((COSString)next).getBytes());
                lastWasAdjustment = false;
            }
            else
            {
                throw new IOException("Unknown type in array for TJ operation:" + next);
            }
        }

        context.showAdjustedText(strings, adjustments);
    }
}
