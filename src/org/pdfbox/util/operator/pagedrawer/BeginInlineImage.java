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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.pdfbox.pdfviewer.PageDrawer;
import org.pdfbox.pdmodel.graphics.xobject.PDInlinedImage;
import org.pdfbox.util.ImageParameters;
import org.pdfbox.util.Matrix;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class BeginInlineImage extends OperatorProcessor
{


    /**
     * process : BI : begin inline image.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error displaying the inline image.
     */
    public void process(PDFOperator operator, List arguments)  throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        Graphics2D graphics = drawer.getGraphics();
        //begin inline image object
        ImageParameters params = operator.getImageParameters();
        PDInlinedImage image = new PDInlinedImage();
        image.setImageParameters( params );
        image.setImageData( operator.getImageData() );
        BufferedImage awtImage = image.createImage();
        
        Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
        
        int width = awtImage.getWidth();
        int height = awtImage.getHeight();

        
        AffineTransform at = new AffineTransform(
            ctm.getValue(0,0)/width,
            ctm.getValue(0,1),
            ctm.getValue(1,0),
            ctm.getValue(1,1)/height,
            ctm.getValue(2,0),
            ctm.getValue(2,1)
        );
        //at.setToRotation((double)page.getRotation());

        
        // The transformation should be done 
        // 1 - Translation
        // 2 - Rotation
        // 3 - Scale or Skew
        //AffineTransform at = new AffineTransform();

        // Translation
        //at = new AffineTransform();
        //at.setToTranslation((double)ctm.getValue(0,0),
        //                    (double)ctm.getValue(0,1));

        // Rotation
        //AffineTransform toAdd = new AffineTransform();
        //toAdd.setToRotation(1.5705);
        //toAdd.setToRotation(ctm.getValue(2,0)*(Math.PI/180));
        //at.concatenate(toAdd);

        // Scale / Skew?
        //toAdd.setToScale(width, height); 
        //at.concatenate(toAdd);
        //at.setToScale( width, height );
        graphics.drawImage( awtImage, at, null );
        //graphics.drawImage( awtImage,0,0, width,height,null);
    }
}
