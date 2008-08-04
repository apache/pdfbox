/**
 * Copyright (c) 2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
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
