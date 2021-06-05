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
package org.apache.pdfbox.contentstream.operator.color;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;

/**
 * CS: Set color space for stroking operations.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class SetStrokingColorSpace extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        if (arguments.isEmpty())
        {
            return;
        }
        COSBase base = arguments.get(0);
        if (!(base instanceof COSName))
        {
            return;
        }
        PDColorSpace cs = context.getResources().getColorSpace((COSName) base);
        context.getGraphicsState().setStrokingColorSpace(cs);
        context.getGraphicsState().setStrokingColor(cs.getInitialColor());
    }

    @Override
    public String getName()
    {
        return OperatorName.STROKING_COLORSPACE;
    }
}