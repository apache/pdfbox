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
package org.pdfbox.util.operator.pagedrawer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.pdfviewer.PageDrawer;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.PDResources;
import org.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.pdfbox.util.Matrix;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class Invoke extends OperatorProcessor
{
    /**
     * process : re : append rectangle to path.
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
                BufferedImage awtImage = image.getRGBImage();
                Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
                
                int width = awtImage.getWidth();
                int height = awtImage.getHeight();

                double rotationInRadians =(page.findRotation() * Math.PI)/180;
                 
                
                AffineTransform rotation = new AffineTransform();
                rotation.setToRotation( rotationInRadians );
                AffineTransform rotationInverse = rotation.createInverse();
                Matrix rotationInverseMatrix = new Matrix();
                rotationInverseMatrix.setFromAffineTransform( rotationInverse );
                Matrix rotationMatrix = new Matrix();
                rotationMatrix.setFromAffineTransform( rotation );
                
                Matrix unrotatedCTM = ctm.multiply( rotationInverseMatrix );
                
                Matrix scalingParams = unrotatedCTM.extractScaling();
                Matrix scalingMatrix = Matrix.getScaleInstance(1f/width,1f/height);
                scalingParams = scalingParams.multiply( scalingMatrix );
                
                Matrix translationParams = unrotatedCTM.extractTranslating();
                Matrix translationMatrix = null;
                int pageRotation = page.findRotation();
                if( pageRotation == 0 )
                {
                    translationParams.setValue(2,1, -translationParams.getValue( 2,1 ));
                    translationMatrix = Matrix.getTranslatingInstance( 
                        0, (float)pageSize.getHeight()-height*scalingParams.getYScale() );
                }
                else if( pageRotation == 90 )
                {
                    translationMatrix = Matrix.getTranslatingInstance( 0, (float)pageSize.getHeight() );
                }
                else 
                {
                    //TODO need to figure out other cases
                }
                translationParams = translationParams.multiply( translationMatrix );

                AffineTransform at = new AffineTransform( 
                        scalingParams.getValue( 0,0), 0,
                        0, scalingParams.getValue( 1, 1),
                        translationParams.getValue(2,0), translationParams.getValue( 2,1 )
                    );
                
                
                

                //at.setToTranslation( pageSize.getHeight()-ctm.getValue(2,0),ctm.getValue(2,1) );
                //at.setToScale( ctm.getValue(0,0)/width, ctm.getValue(1,1)/height);
                //at.setToRotation( (page.findRotation() * Math.PI)/180 );
                
                
                
                //AffineTransform rotation = new AffineTransform();
                //rotation.rotate( (90*Math.PI)/180);
                
                /*
                
                // The transformation should be done 
                // 1 - Translation
                // 2 - Rotation
                // 3 - Scale or Skew
                AffineTransform at = new AffineTransform();

                // Translation
                at = new AffineTransform();
                //at.setToTranslation((double)ctm.getValue(0,0),
                //                    (double)ctm.getValue(0,1));
                
                // Rotation
                //AffineTransform toAdd = new AffineTransform();
                toAdd.setToRotation(1.5705);
                toAdd.setToRotation(ctm.getValue(2,0)*(Math.PI/180));
                at.concatenate(toAdd);
                */
                
                // Scale / Skew?
                //toAdd.setToScale(1, 1); 
                //at.concatenate(toAdd);

                graphics.drawImage( awtImage, at, null );
            }
            catch( Exception e )
            {
                e.printStackTrace();
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
