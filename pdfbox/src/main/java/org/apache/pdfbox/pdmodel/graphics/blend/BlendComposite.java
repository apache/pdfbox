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
package org.apache.pdfbox.pdmodel.graphics.blend;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AWT composite for blend modes.
 * 
 * @author Kühn &amp; Weyh Software GmbH
 */
public final class BlendComposite implements Composite
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(BlendComposite.class);

    /**
     * Creates a blend composite
     *
     * @param blendMode Desired blend mode
     * @param constantAlpha Constant alpha, must be in the inclusive range
     * [0.0...1.0] or it will be clipped.
     * @return a blend composite.
     * @throws IllegalArgumentException if blendMode is null.
     */
    public static Composite getInstance(BlendMode blendMode, float constantAlpha)
    {
        if (constantAlpha < 0)
        {
            LOG.warn("using 0 instead of incorrect Alpha " + constantAlpha);
            constantAlpha = 0;
        }
        else if (constantAlpha > 1)
        {
            LOG.warn("using 1 instead of incorrect Alpha " + constantAlpha);
            constantAlpha = 1;
        }
        if (blendMode == null)
        {
            throw new IllegalArgumentException("blendMode parameter cannot be null");
        }
        if (blendMode == BlendMode.NORMAL)
        {
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, constantAlpha);
        }
        else
        {
            return new BlendComposite(blendMode, constantAlpha);
        }
    }

    private final BlendMode blendMode;
    private final float constantAlpha;

    private BlendComposite(BlendMode blendMode, float constantAlpha)
    {
        this.blendMode = blendMode;
        this.constantAlpha = constantAlpha;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel,
            RenderingHints hints)
    {
        return new BlendCompositeContext(srcColorModel, dstColorModel);
    }

    class BlendCompositeContext implements CompositeContext
    {
        private final ColorModel srcColorModel;
        private final ColorModel dstColorModel;

        BlendCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel)
        {
            this.srcColorModel = srcColorModel;
            this.dstColorModel = dstColorModel;
        }

        @Override
        public void dispose()
        {
            // nothing needed
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
        {
            int x0 = src.getMinX();
            int y0 = src.getMinY();
            int width = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
            int height = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());
            int x1 = x0 + width;
            int y1 = y0 + height;
            int dstInXShift = dstIn.getMinX() - x0;
            int dstInYShift = dstIn.getMinY() - y0;
            int dstOutXShift = dstOut.getMinX() - x0;
            int dstOutYShift = dstOut.getMinY() - y0;

            ColorSpace srcColorSpace = srcColorModel.getColorSpace();
            int numSrcColorComponents = srcColorModel.getNumColorComponents();
            int numSrcComponents = src.getNumBands();
            boolean srcHasAlpha = (numSrcComponents > numSrcColorComponents);
            ColorSpace dstColorSpace = dstColorModel.getColorSpace();
            int numDstColorComponents = dstColorModel.getNumColorComponents();
            int numDstComponents = dstIn.getNumBands();
            boolean dstHasAlpha = (numDstComponents > numDstColorComponents);

            int srcColorSpaceType = srcColorSpace.getType();
            int dstColorSpaceType = dstColorSpace.getType();
            boolean subtractive = (dstColorSpaceType != ColorSpace.TYPE_RGB)
                    && (dstColorSpaceType != ColorSpace.TYPE_GRAY);

            boolean blendModeIsSeparable = blendMode.isSeparableBlendMode();
            boolean needsColorConversion = !srcColorSpace.equals(dstColorSpace);

            Object srcPixel = null;
            Object dstPixel = null;
            float[] srcComponents = new float[numSrcComponents];
            // PDFBOX-3501 let getNormalizedComponents allocate to avoid 
            // ArrayIndexOutOfBoundsException for bitonal target
            float[] dstComponents = null;

            float[] srcColor = new float[numSrcColorComponents];
            float[] srcConverted;
            float[] dstConverted;
            float[] rgbResult = blendModeIsSeparable ? null : new float[dstHasAlpha ? 4 : 3];

            for (int y = y0; y < y1; y++)
            {
                for (int x = x0; x < x1; x++)
                {
                    srcPixel = src.getDataElements(x, y, srcPixel);
                    dstPixel = dstIn.getDataElements(dstInXShift + x, dstInYShift + y, dstPixel);

                    srcComponents = srcColorModel.getNormalizedComponents(srcPixel, srcComponents,
                            0);
                    dstComponents = dstColorModel.getNormalizedComponents(dstPixel, dstComponents,
                            0);

                    float srcAlpha = srcHasAlpha ? srcComponents[numSrcColorComponents] : 1.0f;
                    float dstAlpha = dstHasAlpha ? dstComponents[numDstColorComponents] : 1.0f;

                    srcAlpha = srcAlpha * constantAlpha;

                    float resultAlpha = dstAlpha + srcAlpha - srcAlpha * dstAlpha;
                    float srcAlphaRatio = (resultAlpha > 0) ? srcAlpha / resultAlpha : 0;

                    if (blendModeIsSeparable)
                    {
                        // convert color
                        System.arraycopy(srcComponents, 0, srcColor, 0, numSrcColorComponents);
                        if (needsColorConversion)
                        {
                            // TODO - very very slow - Hash results???
                            float[] cieXYZ = srcColorSpace.toCIEXYZ(srcColor);
                            srcConverted = dstColorSpace.fromCIEXYZ(cieXYZ);
                        }
                        else
                        {
                            srcConverted = srcColor;
                        }
                        
                        for (int k = 0; k < numDstColorComponents; k++)
                        {
                            float srcValue = srcConverted[k];
                            float dstValue = dstComponents[k];

                            if (subtractive)
                            {
                                srcValue = 1 - srcValue;
                                dstValue = 1 - dstValue;
                            }

                            float value = blendMode.getBlendChannelFunction().blendChannel(srcValue,
                                    dstValue);
                            value = srcValue + dstAlpha * (value - srcValue);
                            value = dstValue + srcAlphaRatio * (value - dstValue);

                            if (subtractive)
                            {
                                value = 1 - value;
                            }

                            dstComponents[k] = value;
                        }
                    }
                    else
                    {
                        // Nonseparable blend modes are computed in RGB color space.
                        // TODO - CMYK color spaces need special treatment.

                        if (srcColorSpaceType == ColorSpace.TYPE_RGB)
                        {
                            srcConverted = srcComponents;
                        }
                        else
                        {
                            srcConverted = srcColorSpace.toRGB(srcComponents);
                        }

                        if (dstColorSpaceType == ColorSpace.TYPE_RGB)
                        {
                            dstConverted = dstComponents;
                        }
                        else
                        {
                            dstConverted = dstColorSpace.toRGB(dstComponents);
                        }
                        
                        blendMode.getBlendFunction().blend(srcConverted, dstConverted, rgbResult);

                        for (int k = 0; k < 3; k++)
                        {
                            float srcValue = srcConverted[k];
                            float dstValue = dstConverted[k];
                            float value = rgbResult[k];
                            value = Math.max(Math.min(value, 1.0f), 0.0f);
                            value = srcValue + dstAlpha * (value - srcValue);
                            value = dstValue + srcAlphaRatio * (value - dstValue);
                            rgbResult[k] = value;
                        }

                        if (dstColorSpaceType == ColorSpace.TYPE_RGB)
                        {
                            System.arraycopy(rgbResult, 0, dstComponents, 0, dstComponents.length);
                        }
                        else
                        {
                            float[] temp = dstColorSpace.fromRGB(rgbResult);
                            System.arraycopy(temp, 0, dstComponents, 0,
                                Math.min(dstComponents.length, temp.length));
                        }
                    }

                    if (dstHasAlpha)
                    {
                        dstComponents[numDstColorComponents] = resultAlpha;
                    }

                    dstPixel = dstColorModel.getDataElements(dstComponents, 0, dstPixel);
                    dstOut.setDataElements(dstOutXShift + x, dstOutYShift + y, dstPixel);
                }
            }
        }
    }
}
