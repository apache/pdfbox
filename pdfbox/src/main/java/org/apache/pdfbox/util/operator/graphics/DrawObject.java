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
package org.apache.pdfbox.util.operator.graphics;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.operator.Operator;

/**
 * Do: Draws an XObject.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class DrawObject extends GraphicsOperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(DrawObject.class);

    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        COSName objectName = (COSName)operands.get(0);
        Map<String, PDXObject> xobjects = context.getResources().getXObjects();
        PDXObject xobject = xobjects.get(objectName.getName());

        if (xobject == null)
        {
            LOG.warn("Can't find the XObject named '" + objectName.getName() + "'");
        }
        else if (xobject instanceof PDImageXObject)
        {
            PDImageXObject image = (PDImageXObject)xobject;
            try
            {
                context.drawImage(image);
            }
            catch (MissingImageReaderException e)
            {
                // missing ImageIO plug-in  TODO how far should we escalate this? (after all the user can fix the problem)
                LOG.error(e.getMessage());
            }
            catch (Exception e)
            {
                // TODO we probably shouldn't catch Exception, what errors are expected here?
                LOG.error(e, e);
            }
        }
        else if (xobject instanceof PDFormXObject)
        {
            PDFormXObject form = (PDFormXObject) xobject;
            if (form.getGroup() != null && COSName.TRANSPARENCY.equals(form.getGroup().getSubType())) {
                getContext().showTransparencyGroup(form);
            }
            else
            {
                // save the graphics state
                context.saveGraphicsState();

                // if there is an optional form matrix, we have to map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null)
                {
                    Matrix xobjectCTM = matrix.multiply(
                                    context.getGraphicsState().getCurrentTransformationMatrix());
                    context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }

                // clip to the form's BBox
                if (form.getBBox() != null)
                {
                    PDGraphicsState graphicsState = context.getGraphicsState();
                    PDRectangle bBox = form.getBBox();
                    GeneralPath bboxPath = context.transformedPDRectanglePath(bBox);
                    graphicsState.intersectClippingPath(bboxPath);
                }
                getContext().showForm(form);

                // restore the graphics state
                context.restoreGraphicsState();
            }
        }
    }
}
