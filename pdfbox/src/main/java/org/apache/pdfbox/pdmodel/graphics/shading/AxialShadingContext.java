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
 * This class represents the PaintContext of an axial shading.
 * 
 * @author lehmi
 * 
 */
public class AxialShadingContext implements PaintContext 
{

    private ColorModel colorModel;
    private ColorSpace shadingColorSpace;
    private PDFunction shadingTinttransform;
    private PDShadingType2 shadingType;

    private float[] coords;
    private float[] domain;
    private int[] extend0Values;
    private int[] extend1Values;
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
     * Constructor creates an instance to be used for fill operations.
     * 
     * @param shadingType2 the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * 
     */
    public AxialShadingContext(PDShadingType2 shadingType2, ColorModel colorModelValue, 
            AffineTransform xform, Matrix ctm, int pageHeight) 
    {
        shadingType = shadingType2;
        coords = shadingType.getCoords().toFloatArray();
        if (ctm != null)
        {
            // the shading is used in combination with the sh-operator
            float[] coordsTemp = new float[coords.length]; 
            // transform the coords from shading to user space
            ctm.createAffineTransform().transform(coords, 0, coordsTemp, 0, 2);
            // move the 0,0-reference
            coordsTemp[1] = pageHeight - coordsTemp[1];
            coordsTemp[3] = pageHeight - coordsTemp[3];
            // transform the coords from user to device space
            xform.transform(coordsTemp, 0, coords, 0, 2);
        }
        else
        {
            // the shading is used as pattern colorspace in combination
            // with a fill-, stroke- or showText-operator
            float translateY = (float)xform.getTranslateY();
            // move the 0,0-reference including the y-translation from user to device space
            coords[1] = pageHeight + translateY - coords[1];
            coords[3] = pageHeight + translateY - coords[3];
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
        x1x0 = coords[2] - coords[0]; 
        y1y0 = coords[3] - coords[1];
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
        shadingColorSpace = null;
        shadingTinttransform = null;
        shadingType = null;
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
        int[] data = new int[w * h * 3];
        boolean saveExtend0 = false;
        boolean saveExtend1 = false;
        for (int j = 0; j < h; j++) 
        {
            for (int i = 0; i < w; i++) 
            {
                int index = (j * w + i) * 3;
                double inputValue = x1x0 * (x + i - coords[0]); 
                inputValue += y1y0 * (y + j - coords[1]);
                inputValue /= denom;
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

}
