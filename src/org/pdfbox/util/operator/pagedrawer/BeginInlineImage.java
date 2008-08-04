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
