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

import java.awt.PaintContext;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.Matrix;

/**
 * This class represents the PaintContext of an radial shading.
 * 
 * @author lehmi
 * 
 */
public class RadialShadingContext implements PaintContext 
{

    private ColorModel colorModel;
    private ColorSpace shadingColorSpace;
    private PDFunction shadingTinttransform;
    private PDShadingType3 shadingType;

    private float[] coords;
    private float[] domain;
    private int[] extend0Values;
    private int[] extend1Values;
    private boolean[] extend;
    private double x1x0; 
    private double y1y0;
    private double r1r0;
    private double x1x0pow2;
    private double y1y0pow2;
    private double r0pow2;

    private float d1d0;
    private double denom;
    
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(RadialShadingContext.class);

    /**
     * Constructor creates an instance to be used for fill operations.
     * 
     * @param shadingType3 the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * 
     */
    public RadialShadingContext(PDShadingType3 shadingType3, ColorModel colorModelValue, 
            AffineTransform xform, Matrix ctm, int pageHeight) 
    {
        shadingType = shadingType3;
        coords = shadingType.getCoords().toFloatArray();
        if (ctm != null)
        {
            // the shading is used in combination with the sh-operator
            float[] coordsTemp = new float[coords.length]; 
            // transform the coords from shading to user space
            ctm.createAffineTransform().transform(coords, 0, coordsTemp, 0, 1);
            ctm.createAffineTransform().transform(coords, 3, coordsTemp, 3, 1);
            // move the 0,0-reference
            coordsTemp[1] = pageHeight - coordsTemp[1];
            coordsTemp[4] = pageHeight - coordsTemp[4];
            // transform the coords from user to device space
            xform.transform(coordsTemp, 0, coords, 0, 1);
            xform.transform(coordsTemp, 3, coords, 3, 1);
        }
        else
        {
            // the shading is used as pattern colorspace in combination
            // with a fill-, stroke- or showText-operator
            float translateY = (float)xform.getTranslateY();
            // move the 0,0-reference including the y-translation from user to device space
            coords[1] = pageHeight + translateY - coords[1];
            coords[4] = pageHeight + translateY - coords[4];
        }
        // colorSpace 
        try 
        {
            PDColorSpace cs = shadingType.getColorSpace();
            if (!(cs instanceof PDDeviceRGB))
            {
                // we have to create an instance of the shading colorspace if it isn't RGB
                shadingColorSpace = cs.getJavaColorSpace();
                if (cs instanceof PDDeviceN)
                {
                    shadingTinttransform = ((PDDeviceN)cs).getTintTransform();
                }
                else if (cs instanceof PDSeparation)
                {
                    shadingTinttransform = ((PDSeparation)cs).getTintTransform();
                }
            }
        } 
        catch (IOException exception) 
        {
            LOG.error("error while creating colorSpace", exception);
        }
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
                colorModel = shadingType.getColorSpace().createColorModel(8);
            }
            catch(IOException exception)
            {
                LOG.error("error while creating colorModel", exception);
            }
        }
        // domain values
        if (shadingType.getDomain() != null)
        {
            domain = shadingType.getDomain().toFloatArray();
        }
        else 
        {
            // set default values
            domain = new float[]{0,1};
        }
        // extend values
        COSArray extendValues = shadingType.getExtend();
        if (shadingType.getExtend() != null)
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
        x1x0 = coords[3] - coords[0]; 
        y1y0 = coords[4] - coords[1];
        r1r0 = coords[5] - coords[2];
        x1x0pow2 = Math.pow(x1x0,2);
        y1y0pow2 = Math.pow(y1y0,2);
        r0pow2 = Math.pow(coords[2],2);
        denom = x1x0pow2 + y1y0pow2 - Math.pow(r1r0, 2);
        d1d0 = domain[1]-domain[0];
        // TODO take a possible Background value into account
        
    }
    /**
     * {@inheritDoc}
     */
    public void dispose() 
    {
        colorModel = null;
        shadingType = null;
        shadingColorSpace = null;
        shadingTinttransform = null;
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
        float inputValue;
        boolean saveExtend0 = false;
        boolean saveExtend1 = false;
        int[] data = new int[w * h * 3];
        for (int j = 0; j < h; j++) 
        {
            for (int i = 0; i < w; i++) 
            {
                int index = (j * w + i) * 3;
                float[] inputValues = calculateInputValues(x + i, y + j);
                // choose 1 of the 2 values
                if (inputValues[0] >= domain[0] && inputValues[0] <= domain[1])
                {
                    // both values are in the domain -> choose the larger one 
                    if(inputValues[1] >= domain[0] && inputValues[1] <= domain[1])
                    {
                        inputValue = Math.max(inputValues[0], inputValues[1]);
                    } 
                    // first value is in the domain, the second not -> choose first value
                    else
                    {
                        inputValue = inputValues[0];
                    }
                }
                else
                {
                    // first value is not in the domain, but the second -> choose second value
                    if(inputValues[1] >= domain[0] && inputValues[1] <= domain[1])
                    {
                        inputValue = inputValues[1];
                    }
                    // TODO
                    // both are not in the domain -> choose the first as I don't know it better
                    else
                    {
                        inputValue = inputValues[0];
                    }
                }
                // input value is out of range
                if (inputValue < domain[0])
                {
                    // the shading has to be extended if extend[0] == true
                    if (extend[0])
                    {
                        if (extend0Values == null)
                        {
                            inputValue = domain[0];
                            saveExtend0 = true;
                        }
                        else
                        {
                            // use the chached values
                            System.arraycopy(extend0Values, 0, data, index, 3);
                            continue;
                        }
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
                        if (extend1Values == null)
                        {
                            inputValue = domain[1];
                            saveExtend1 = true;
                        }
                        else
                        {
                            // use the chached values 
                            System.arraycopy(extend1Values, 0, data, index, 3);
                            continue;
                        }
                    }
                    else 
                    {
                        continue;
                    }
                }
                float input = (float)(domain[0] + (d1d0*inputValue));
                float[] values = null;
                try 
                {
                    values = shadingType.evalFunction(input);
                    // convert color values from shading colorspace to RGB 
                    if (shadingColorSpace != null)
                    {
                        if (shadingTinttransform != null)
                        {
                            values = shadingTinttransform.eval(values);
                        }
                        values = shadingColorSpace.toRGB(values);
                    }
                }
                catch (IOException exception) 
                {
                    LOG.error("error while processing a function", exception);
                }
                data[index] = (int)(values[0]*255);
                data[index+1] = (int)(values[1]*255);
                data[index+2] = (int)(values[2]*255);
                if (saveExtend0)
                {
                    // chache values
                    extend0Values = new int[3];
                    System.arraycopy(data, index, extend0Values, 0, 3);
                }
                if (saveExtend1)
                {
                    // chache values
                    extend1Values = new int[3];
                    System.arraycopy(data, index, extend1Values, 0, 3);
                }
            }
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

    private float[] calculateInputValues(int x, int y) 
    {
        
        /** 
         *  According to Adobes Technical Note #5600 we have to do the following 
         *  
         *  x0, y0, r0 defines the start circle
         *  x1, y1, r1 defines the end circle
         *  
         *  The parametric equations for the center and radius of the gradient fill
         *  circle moving between the start circle and the end circle as a function 
         *  of s are as follows:
         *  
         *  xc(s) = x0 + s * (x1 - x0)
         *  yc(s) = y0 + s * (y1 - y0)
         *  r(s)  = r0 + s * (r1 - r0)
         * 
         *  Given a geometric coordinate position (x, y) in or along the gradient fill, 
         *  the corresponding value of s can be determined by solving the quadratic 
         *  constraint equation:
         *  
         *  [x - xc(s)]2 + [y - yc(s)]2 = [r(s)]2
         *  
         *  The following code calculates the 2 possible values of s
         */
        
        float[] values = new float[2];
        double p = (-0.25)*((x - coords[0])*x1x0 + (y - coords[1])*y1y0 - r1r0) / denom;
        double q = (Math.pow(x - coords[0],2) + Math.pow(y - coords[1],2) - r0pow2) / denom;
        double root = Math.sqrt(Math.pow(p , 2) - q);
        values[0] = (float)((-1)*p + root);
        values[1] = (float)((-1)*p - root);
        return values;
    }
}
