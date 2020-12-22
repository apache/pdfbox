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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Tf: Set text font and size.
 *
 * @author Laurent Huault
 */
public class SetFontAndSize extends OperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(SetFontAndSize.class);

    @Override
    public void process(final Operator operator, final List<COSBase> arguments) throws IOException
    {
        if (arguments.size() < 2)
        {
            throw new MissingOperandException(operator, arguments);
        }

        final COSBase base0 = arguments.get(0);
        final COSBase base1 = arguments.get(1);
        if (!(base0 instanceof COSName))
        {
            return;
        }
        if (!(base1 instanceof COSNumber))
        {
            return;
        }
        final COSName fontName = (COSName) base0;
        final float fontSize = ((COSNumber) base1).floatValue();
        context.getGraphicsState().getTextState().setFontSize(fontSize);
        final PDFont font = context.getResources().getFont(fontName);
        if (font == null)
        {
            LOG.warn("font '" + fontName.getName() + "' not found in resources");
        }
        context.getGraphicsState().getTextState().setFont(font);
    }

    @Override
    public String getName()
    {
        return OperatorName.SET_FONT_AND_SIZE;
    }
}
