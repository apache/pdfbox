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

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.MissingResourceException;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.contentstream.operator.Operator;

/**
 * Do: Draws an XObject.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class DrawObject extends GraphicsOperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        if (operands.size() < 1)
        {
            throw new MissingOperandException(operator, operands);
        }
        COSBase base0 = operands.get(0);
        if (!(base0 instanceof COSName))
        {
            return;
        }
        COSName objectName = (COSName) base0;
        PDXObject xobject = context.getResources().getXObject(objectName);

        if (xobject == null)
        {
            throw new MissingResourceException("Missing XObject: " + objectName.getName());
        }
        else if (xobject instanceof PDImageXObject)
        {
            PDImageXObject image = (PDImageXObject)xobject;
            context.drawImage(image);
        }
        else if (xobject instanceof PDTransparencyGroup)
        {
            getContext().showTransparencyGroup((PDTransparencyGroup) xobject);
        }
        else if (xobject instanceof PDFormXObject)
        {
            getContext().showForm((PDFormXObject) xobject);
        }
    }

    @Override
    public String getName()
    {
        return "Do";
    }
}
