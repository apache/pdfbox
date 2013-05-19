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
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFMarkedContentExtractor;
import org.apache.pdfbox.util.PDFOperator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Invoke named XObject.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Mario Ivankovits
 *
 * @version $Revision: 1.9 $
 */
public class Invoke extends OperatorProcessor
{
    /**
     * process : Do - Invoke a named xobject.
     * 
     * @param operator The operator that is being executed.
     * @param arguments List
     *
     * @throws IOException If there is an error processing this operator.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        COSName name = (COSName) arguments.get( 0 );

        Map<String,PDXObject> xobjects = context.getXObjects();
        PDXObject xobject = (PDXObject) xobjects.get(name.getName());
        if (context instanceof PDFMarkedContentExtractor)
        {
            ((PDFMarkedContentExtractor) context).xobject(xobject);
        }

        if(xobject instanceof PDXObjectForm)
        {
            PDXObjectForm form = (PDXObjectForm)xobject;
            COSStream formContentstream = form.getCOSStream();
            // if there is an optional form matrix, we have to map the form space to the user space
            Matrix matrix = form.getMatrix();
            if (matrix != null) 
            {
                Matrix xobjectCTM = matrix.multiply( context.getGraphicsState().getCurrentTransformationMatrix());
                context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
            }
            // find some optional resources, instead of using the current resources
            PDResources pdResources = form.getResources();
            context.processSubStream( context.getCurrentPage(), pdResources, formContentstream );
        }
    }
}
