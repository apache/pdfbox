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
package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.Dimension;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.util.Matrix;

/**
 * This class represents the PaintContext of an axial shading.
 * 
 * @author lehmi
 * @version $Revision: $
 * 
 */
public class AxialShadingContext implements PaintContext 
{

    private ColorModel colorModel;
    private PDFunction function;
    private Point2D startingPoint;
    private Point2D endingPoint;

    private float[] domain;
    private boolean[] extend;
    private double x1x0; 
    private double y1y0;
    private float d1d0;
    private double denom;

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(AxialShadingContext.class);

    /**
     * Constructor.
     * 
     * @param shadingType2 the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageSize size of the current page
     * 
     */
    public AxialShadingContext(PDShadingType2 shadingType2, ColorModel colorModelValue, 
            AffineTransform xform, Matrix ctm, Dimension pageSize) 
    {
        // colorModel
        if (colorModelValue != null)
        {
            colorModel = colorModelValue;
        }
        else
        {
            try
            {
                // TODO bpc != 8 ??  
                colorModel = shadingType2.getColorSpace().createColorModel(8);
            }
            catch(IOException exception)
            {
                LOG.error("error while creating colorModel", exception);
            }
        }
        // shading function
        try
        {
            function = shadingType2.getFunction();
        }
        catch(IOException exception)
        {
            LOG.error("error while creating a function", exception);
        }
        
        float yScaling = ctm.getYScale();
        float angle = (float)Math.acos(ctm.getValue(0, 0)/ctm.getXScale());
        if (ctm.getValue(0, 1) < 0 && ctm.getValue(1, 0) > 0)
        {
            angle = (-1)*angle;
        }
        ctm.setValue(2, 1, (float)(pageSize.height - ctm.getYPosition() - Math.cos(angle)*yScaling));
        ctm.setValue(2, 0, (float)(ctm.getXPosition() - Math.sin(angle)*yScaling));
        // because of the moved 0,0-reference, we have to shear in the opposite direction
        ctm.setValue(0, 1, (-1)*ctm.getValue(0, 1));
        ctm.setValue(1, 0, (-1)*ctm.getValue(1, 0));

        // create startingPoint
        float[] coords = shadingType2.getCoords().toFloatArray();
        startingPoint = new Point2D.Float(coords[0], coords[1]);
        startingPoint = ctm.createAffineTransform().transform(startingPoint, null);
        startingPoint = xform.transform(startingPoint, null);
        // create endingPoint
        endingPoint = new Point2D.Float(coords[2], coords[3]);
        endingPoint = ctm.createAffineTransform().transform(endingPoint, null);
        endingPoint = xform.transform(endingPoint, null);
        // domain values
        if (shadingType2.getDomain() != null)
        {
            domain = shadingType2.getDomain().toFloatArray();
        }
        else 
        {
            // set default values
            domain = new float[]{0,1};
        }
        // extend values
        COSArray extendValues = shadingType2.getExtend();
        if (shadingType2.getExtend() != null)
        {
            extend = new boolean[2];
            extend[0] = ((COSBoolean)extendValues.get(0)).getValue();
            extend[1] = ((COSBoolean)extendValues.get(1)).getValue();
        }
        else
        {
            // set default values
            extend = new boolean[]{false,false};
        }
        // calculate some constants to be used in getRaster
        x1x0 = endingPoint.getX() - startingPoint.getX(); 
        y1y0 = endingPoint.getY() - startingPoint.getY();
        d1d0 = domain[1]-domain[0];
        denom = Math.pow(x1x0,2) + Math.pow(y1y0, 2);
        // TODO take a possible Background value into account
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() 
    {
        colorModel = null;
        function = null;
        startingPoint = null;
        endingPoint = null;
    }

    /**
     * {@inheritDoc}
     */
    public ColorModel getColorModel() 
    {
        return colorModel;
    }

    /**
     * {@inheritDoc}
     */
    public Raster getRaster(int x, int y, int w, int h) 
    {
        // create writable raster
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        
        float[] input = new float[1];
        int[] data = new int[w * h * 3];
        for (int j = 0; j < h; j++) 
        {
            for (int i = 0; i < w; i++) 
            {
                double inputValue = x1x0 * (x + i - startingPoint.getX()); 
                inputValue += y1y0 * (y + j - startingPoint.getY());
                inputValue /= denom;
                // input value is out of range
                if (inputValue < domain[0])
                {
                    // the shading has to be extended if extend[0] == true
                    if (extend[0])
                    {
                        inputValue = domain[0];
                    }
                    else 
                    {
                        continue;
                    }
                }
                // input value is out of range
                else if (inputValue > domain[1])
                {
                    // the shading has to be extended if extend[1] == true
                    if (extend[1])
                    {
                        inputValue = domain[1];
                    }
                    else 
                    {
                        continue;
                    }
                }
                input[0] = (float)(domain[0] + (d1d0*inputValue));
                float[] values = null;
                try 
                {
                    values = function.eval(input);
                } 
                catch (IOException exception) 
                {
                    LOG.error("error while processing a function", exception);
                }
                int index = (j * w + i) * 3;
                data[index] = (int)(values[0]*255);
                data[index+1] = (int)(values[1]*255);
                data[index+2] = (int)(values[2]*255);
            }
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

}
