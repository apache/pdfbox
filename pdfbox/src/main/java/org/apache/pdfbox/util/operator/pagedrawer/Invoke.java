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
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class Invoke extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(Invoke.class);

    /**
     * process : Do : Paint the specified XObject (section 4.7).
     * 
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error invoking the sub object.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        PageDrawer drawer = (PageDrawer) context;
        PDPage page = drawer.getPage();
        COSName objectName = (COSName) arguments.get(0);
        Map<String, PDXObject> xobjects = drawer.getResources().getXObjects();
        PDXObject xobject = (PDXObject) xobjects.get(objectName.getName());
        if (xobject == null)
        {
            LOG.warn("Can't find the XObject for '" + objectName.getName() + "'");
        }
        else if (xobject instanceof PDXObjectImage)
        {
            PDXObjectImage image = (PDXObjectImage) xobject;
            try
            {
                if (image.getImageMask())
                {
                    // set the current non stroking colorstate, so that it can
                    // be used to create a stencil masked image
                    image.setStencilColor(drawer.getGraphicsState().getNonStrokingColor());
                }
                BufferedImage awtImage = image.getRGBImage();
                if (awtImage == null)
                {
                    LOG.warn("getRGBImage returned NULL");
                    return;// TODO PKOCH
                }
                int imageWidth = awtImage.getWidth();
                int imageHeight = awtImage.getHeight();

                LOG.debug("imageWidth: " + imageWidth + "\t\timageHeight: " + imageHeight);

                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                AffineTransform imageTransform = ctm.createAffineTransform();
                drawer.drawImage(awtImage, imageTransform);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                LOG.error(e, e);
            }
        }
        else if (xobject instanceof PDXObjectForm)
        {
            // save the graphics state
            context.getGraphicsStack().push((PDGraphicsState) context.getGraphicsState().clone());

            PDXObjectForm form = (PDXObjectForm) xobject;
            COSStream formContentstream = form.getCOSStream();
            // find some optional resources, instead of using the current resources
            PDResources pdResources = form.getResources();
            // if there is an optional form matrix, we have to map the form space to the user space
            Matrix matrix = form.getMatrix();
            if (matrix != null)
            {
                Matrix xobjectCTM = matrix.multiply(context.getGraphicsState().getCurrentTransformationMatrix());
                context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
            }
            getContext().processSubStream(page, pdResources, formContentstream);

            // restore the graphics state
            context.setGraphicsState((PDGraphicsState) context.getGraphicsStack().pop());
        }
    }
}
