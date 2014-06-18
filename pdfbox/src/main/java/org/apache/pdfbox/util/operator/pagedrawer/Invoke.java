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
package org.apache.pdfbox.util.operator.pagedrawer;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.operator.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Do Draws an XObject.
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class Invoke extends OperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(Invoke.class);

    @Override
    public void process(PDFOperator operator, List<COSBase> operands) throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        COSName objectName = (COSName)operands.get(0);
        Map<String, PDXObject> xobjects = drawer.getResources().getXObjects();
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
                BufferedImage awtImage;
                if (image.isStencil())
                {
                    PDColorSpace colorSpace = drawer.getGraphicsState().getNonStrokingColorSpace();
                    PDColor color = drawer.getGraphicsState().getNonStrokingColor();
                    awtImage = image.getStencilImage(colorSpace.toPaint(drawer.getRenderer(), color)); // <--- TODO: pass page height?
                }
                else
                {
                    awtImage = image.getImage();
                }
                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                AffineTransform imageTransform = ctm.createAffineTransform();
                drawer.drawImage(awtImage, imageTransform);
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
            if ((form.getGroup() != null) && (COSName.TRANSPARENCY.equals(form.getGroup().getSubType()))) {
                PageDrawer.Group group = drawer.createPageDrawerGroup(form);

                // Draw the result of the group to the page...
                group.drawResult();
            }
            else 
            {
                // save the graphics state
                context.saveGraphicsState();

                COSStream formContentStream = form.getCOSStream();

                // find some optional resources, instead of using the current resources
                PDResources pdResources = form.getResources();

                // if there is an optional form matrix, we have to map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null)
                {
                    Matrix xobjectCTM = matrix.multiply(
                                    context.getGraphicsState().getCurrentTransformationMatrix());
                    context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                getContext().processSubStream(pdResources, formContentStream);

                // restore the graphics state
                context.restoreGraphicsState();
            }
        }
    }
}
