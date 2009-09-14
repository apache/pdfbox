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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
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
    public void process(PDFOperator operator, List arguments) throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        PDPage page = drawer.getPage();
        Dimension pageSize = drawer.getPageSize();
        Graphics2D graphics = drawer.getGraphics();
        COSName objectName = (COSName)arguments.get( 0 );
        Map xobjects = drawer.getResources().getXObjects();
        PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
        if( xobject instanceof PDXObjectImage )
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
                double pageHeight = pageSize.getHeight();

                log.info("imageWidth: " + imageWidth + "\t\timageHeight: " + imageHeight);
        
                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                int pageRotation = page.findRotation();

                AffineTransform ctmAT = ctm.createAffineTransform();
                ctmAT.scale(1f/imageWidth, 1f/imageHeight);
                Matrix rotationMatrix = new Matrix();
                rotationMatrix.setFromAffineTransform( ctmAT );
                if (pageRotation == 0 || pageRotation == 180) 
                {
                    rotationMatrix.setValue(2,1,(float)pageHeight-ctm.getYPosition()-ctm.getYScale());
                }
                else if (pageRotation == 90 || pageRotation == 270) 
                {
                    rotationMatrix.setValue(2,0,(float)ctm.getXPosition()-ctm.getYScale());
                    rotationMatrix.setValue(2,1,(float)pageHeight-ctm.getYPosition());
                }
                rotationMatrix.setValue(0, 1, (-1)*rotationMatrix.getValue(0, 1));
                rotationMatrix.setValue(1, 0, (-1)*rotationMatrix.getValue(1, 0));

                AffineTransform at = new AffineTransform(
                        rotationMatrix.getValue(0,0),rotationMatrix.getValue(0,1),
                        rotationMatrix.getValue(1,0), rotationMatrix.getValue( 1, 1),
                        rotationMatrix.getValue(2,0),rotationMatrix.getValue(2,1)
                    );
                graphics.setClip(context.getGraphicsState().getCurrentClippingPath());
                graphics.drawImage( awtImage, at, null );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                log.error(e, e);
            }
        }
        else if(xobject instanceof PDXObjectForm)
        {
            PDXObjectForm form = (PDXObjectForm)xobject;
            COSStream invoke = (COSStream)form.getCOSObject();
            PDResources pdResources = form.getResources();
            if(pdResources == null)
            {
                pdResources = page.findResources();
            }

            getContext().processSubStream( page, pdResources, invoke );
        }
        else
        {
            //unknown xobject type
        }


        //invoke named object.
    }
}
