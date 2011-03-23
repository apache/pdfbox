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
 * @version $Revision: 1.4 $
 */
public class Invoke extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(Invoke.class);

    /**
     * process : Do : Paint the specified XObject (section 4.7).
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error invoking the sub object.
     */
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        PDPage page = drawer.getPage();
        COSName objectName = (COSName)arguments.get( 0 );
        Map xobjects = drawer.getResources().getXObjects();
        PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
        if ( xobject == null )
        {
            log.warn("Can't find the XObject for '"+objectName.getName()+"'");
        }
        else if( xobject instanceof PDXObjectImage )
        {
            PDXObjectImage image = (PDXObjectImage)xobject;
            try
            {
                image.setGraphicsState(drawer.getGraphicsState());
                BufferedImage awtImage = image.getRGBImage();
                if (awtImage == null) 
                {
                    log.warn("getRGBImage returned NULL");
                    return;//TODO PKOCH
                }
                int imageWidth = awtImage.getWidth();
                int imageHeight = awtImage.getHeight();
                double pageHeight = drawer.getPageSize().getHeight();

                log.debug("imageWidth: " + imageWidth + "\t\timageHeight: " + imageHeight);
        
                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                int pageRotation = page.findRotation();

                AffineTransform ctmAT = ctm.createAffineTransform();
                ctmAT.scale(1f/imageWidth, 1f/imageHeight);
                Matrix rotationMatrix = new Matrix();
                rotationMatrix.setFromAffineTransform( ctmAT );
                // calculate the inverse rotation angle
                // scaleX = m00 = cos
                // shearX = m01 = -sin
                // tan = sin/cos
                double angle = Math.atan(ctmAT.getShearX()/ctmAT.getScaleX());
                Matrix translationMatrix = null;
                if (pageRotation == 0 || pageRotation == 180) 
                {
                    translationMatrix = Matrix.getTranslatingInstance((float)(Math.sin(angle)*ctm.getXScale()), (float)(pageHeight-2*ctm.getYPosition()-Math.cos(angle)*ctm.getYScale())); 
                }
                else if (pageRotation == 90 || pageRotation == 270) 
                {
                    translationMatrix = Matrix.getTranslatingInstance((float)(Math.sin(angle)*ctm.getYScale()), (float)(pageHeight-2*ctm.getYPosition())); 
                }
                rotationMatrix = rotationMatrix.multiply(translationMatrix);
                rotationMatrix.setValue(0, 1, (-1)*rotationMatrix.getValue(0, 1));
                rotationMatrix.setValue(1, 0, (-1)*rotationMatrix.getValue(1, 0));

                AffineTransform at = new AffineTransform(
                        rotationMatrix.getValue(0,0),rotationMatrix.getValue(0,1),
                        rotationMatrix.getValue(1,0),rotationMatrix.getValue(1,1),
                        rotationMatrix.getValue(2,0),rotationMatrix.getValue(2,1)
                    );
               
                drawer.drawImage( awtImage, at );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                log.error(e, e);
            }
        }
        else if(xobject instanceof PDXObjectForm)
        {
            // save the graphics state
            context.getGraphicsStack().push( (PDGraphicsState)context.getGraphicsState().clone() );
            
            PDXObjectForm form = (PDXObjectForm)xobject;
            COSStream invoke = (COSStream)form.getCOSObject();
            PDResources pdResources = form.getResources();
            if(pdResources == null)
            {
                pdResources = page.findResources();
            }
            // if there is an optional form matrix, we have to
            // map the form space to the user space
            Matrix matrix = form.getMatrix();
            if (matrix != null) 
            {
                Matrix xobjectCTM = matrix.multiply( context.getGraphicsState().getCurrentTransformationMatrix());
                context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
            }
            getContext().processSubStream( page, pdResources, invoke );
            
            // restore the graphics state
            context.setGraphicsState( (PDGraphicsState)context.getGraphicsStack().pop() );
        }
    }
}
