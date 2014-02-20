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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.tiling.ColoredTilingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.AxialShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingResources;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType1;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType4;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType5;
import org.apache.pdfbox.pdmodel.graphics.shading.RadialShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type1ShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type4ShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type5ShadingPaint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Map;

/**
 * A Pattern color space is either a Tiling pattern or a Shading pattern.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDPattern extends PDSpecialColorSpace
{
    private static final Log LOG = LogFactory.getLog(PDPattern.class);

    private Map<String, PDPatternResources> patterns;

    /**
     * Creates a new Pattern color space.
     */
    public PDPattern(Map<String, PDPatternResources> patterns)
    {
        this.patterns = patterns;
    }

    @Override
    public String getName()
    {
        return COSName.PATTERN.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] getDefaultDecode()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PDColor getInitialColor()
    {
        return PDColor.EMPTY_PATTERN;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Paint toPaint(PDColor color) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Paint toPaint(PDColor color, int pageHeight) throws IOException
    {
        if (!patterns.containsKey(color.getPatternName()))
        {
            throw new IOException("pattern " + color.getPatternName() + " was not found");
        }

        PDPatternResources pattern = patterns.get(color.getPatternName());
        if (pattern instanceof PDTilingPatternResources)
        {
            return toTilingPaint((PDTilingPatternResources)pattern, color);
        }
        else
        {
            return toShadingPaint((PDShadingPatternResources)pattern, pageHeight);
        }
    }

    public Paint toTilingPaint(PDTilingPatternResources tilingPattern, PDColor color)
            throws IOException
    {
        if (tilingPattern.getPatternType() == PDTilingPatternResources.COLORED_TILING_PATTERN)
        {
            // colored tiling pattern
            // TODO we should be passing the color to ColoredTilingPaint
            return new ColoredTilingPaint(tilingPattern);
        }
        else
        {
            // uncolored tiling pattern
            // TODO ...
            LOG.debug("Not implemented: uncoloured tiling patterns");
            return new Color(0, 0, 0, 0); // transparent
        }
    }
    public Paint toShadingPaint(PDShadingPatternResources shadingPattern, int pageHeight)
            throws IOException
    {
        PDShadingResources shadingResources = shadingPattern.getShading();
        int shadingType = shadingResources != null ? shadingResources.getShadingType() : 0;
        switch (shadingType)
        {
            case PDShadingResources.SHADING_TYPE1:
                return new Type1ShadingPaint((PDShadingType1)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShadingResources.SHADING_TYPE2:
                return new AxialShadingPaint((PDShadingType2)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShadingResources.SHADING_TYPE3:
                return new RadialShadingPaint((PDShadingType3)shadingResources,
                                              shadingPattern.getMatrix(), pageHeight);
            case PDShadingResources.SHADING_TYPE4:
                return new Type4ShadingPaint((PDShadingType4)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShadingResources.SHADING_TYPE5:
                return new Type5ShadingPaint((PDShadingType5)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShadingResources.SHADING_TYPE6:
            case PDShadingResources.SHADING_TYPE7:
                // TODO ...
                LOG.debug("Not implemented, shading type: " + shadingType);
                return new Color(0, 0, 0, 0); // transparent
            default:
                throw new IOException("Invalid shading type: " + shadingType);
        }
    }

    @Override
    public String toString()
    {
        return "Pattern";
    }
}
