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
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.TilingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.AxialShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType1;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType2;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType4;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType5;
import org.apache.pdfbox.pdmodel.graphics.shading.RadialShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type1ShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type4ShadingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.Type5ShadingPaint;
import org.apache.pdfbox.rendering.PDFRenderer;

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

    private Map<String, PDAbstractPattern> patterns;
    private PDColorSpace underlyingColorSpace;

    /**
     * Creates a new pattern color space.
     */
    public PDPattern(Map<String, PDAbstractPattern> patterns)
    {
        this.patterns = patterns;
    }

    /**
     * Creates a new uncolored tiling pattern color space.
     */
    public PDPattern(Map<String, PDAbstractPattern> patterns, PDColorSpace colorSpace)
    {
        this.patterns = patterns;
        this.underlyingColorSpace = colorSpace;
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
    public float[] getDefaultDecode(int bitsPerComponent)
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
    public Paint toPaint(PDFRenderer renderer, PDColor color) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Paint toPaint(PDFRenderer renderer, PDColor color, int pageHeight) throws IOException
    {
        if (!patterns.containsKey(color.getPatternName()))
        {
            throw new IOException("pattern " + color.getPatternName() + " was not found");
        }

        PDAbstractPattern pattern = patterns.get(color.getPatternName());
        if (pattern instanceof PDTilingPattern)
        {
            return toTilingPaint(renderer, (PDTilingPattern)pattern, color);
        }
        else
        {
            return toShadingPaint((PDShadingPattern)pattern, pageHeight);
        }
    }

    private Paint toTilingPaint(PDFRenderer renderer, PDTilingPattern tilingPattern, PDColor color)
            throws IOException
    {
        if (tilingPattern.getPaintType() == PDTilingPattern.PAINT_COLORED)
        {
            // colored tiling pattern
            return new TilingPaint(renderer, tilingPattern);
        }
        else
        {
            // uncolored tiling pattern
            return new TilingPaint(renderer, tilingPattern, underlyingColorSpace, color);
        }
    }

    private Paint toShadingPaint(PDShadingPattern shadingPattern, int pageHeight) throws IOException
    {
        PDShading shadingResources = shadingPattern.getShading();
        int shadingType = shadingResources != null ? shadingResources.getShadingType() : 0;
        switch (shadingType)
        {
            case PDShading.SHADING_TYPE1:
                return new Type1ShadingPaint((PDShadingType1)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShading.SHADING_TYPE2:
                return new AxialShadingPaint((PDShadingType2)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShading.SHADING_TYPE3:
                return new RadialShadingPaint((PDShadingType3)shadingResources,
                                              shadingPattern.getMatrix(), pageHeight);
            case PDShading.SHADING_TYPE4:
                return new Type4ShadingPaint((PDShadingType4)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShading.SHADING_TYPE5:
                return new Type5ShadingPaint((PDShadingType5)shadingResources,
                                             shadingPattern.getMatrix(), pageHeight);
            case PDShading.SHADING_TYPE6:
            case PDShading.SHADING_TYPE7:
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
