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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFMarkedContentExtractor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Do: Draws an XObject.
 *
 * @author Ben Litchfield
 * @author Mario Ivankovits
 */
public class DrawObject extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        COSName name = (COSName) arguments.get(0);

        Map<String,PDXObject> xobjects = context.getXObjects();
        PDXObject xobject = xobjects.get(name.getName());
        if (context instanceof PDFMarkedContentExtractor)
        {
            ((PDFMarkedContentExtractor) context).xobject(xobject);
        }

        if(xobject instanceof PDFormXObject)
        {
            PDFormXObject form = (PDFormXObject)xobject;

            // if there is an optional form matrix, we have to map the form space to the user space
            Matrix matrix = form.getMatrix();
            if (matrix != null) 
            {
                Matrix xobjectCTM = matrix.multiply(context.getGraphicsState().getCurrentTransformationMatrix());
                context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
            }

            context.showForm(form);
        }
    }
}
