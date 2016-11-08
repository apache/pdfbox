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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;

/**
 * A Pattern color space is either a Tiling pattern or a Shading pattern.
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDPattern extends PDSpecialColorSpace
{
    /** A pattern which leaves no marks on the page. */
    private static PDColor EMPTY_PATTERN = new PDColor(new float[] { }, null);
    
    private final PDResources resources;
    private PDColorSpace underlyingColorSpace;

    /**
     * Creates a new pattern color space.
     * 
     * @param resources The current resources.
     */
    public PDPattern(PDResources resources)
    {
        this.resources = resources;
        array = new COSArray();
        array.add(COSName.PATTERN);
    }

    /**
     * Creates a new uncolored tiling pattern color space.
     * 
     * @param resources The current resources.
     * @param colorSpace The underlying color space.
     */
    public PDPattern(PDResources resources, PDColorSpace colorSpace)
    {
        this.resources = resources;
        this.underlyingColorSpace = colorSpace;
        array = new COSArray();
        array.add(COSName.PATTERN);
        array.add(colorSpace);
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
        return EMPTY_PATTERN;
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

    /**
     * Returns the pattern for the given color.
     * 
     * @param color color containing a pattern name
     * @return pattern for the given color
     * @throws java.io.IOException if the pattern name was not found.
     */
    public PDAbstractPattern getPattern(PDColor color) throws IOException
    {
        PDAbstractPattern pattern = resources.getPattern(color.getPatternName());
        if (pattern == null)
        {
            throw new IOException("pattern " + color.getPatternName() + " was not found");
        }
        else
        {
            return pattern;
        }
    }

    /**
     * Returns the underlying color space, if this is an uncolored tiling pattern, otherwise null.
     */
    public PDColorSpace getUnderlyingColorSpace()
    {
        return underlyingColorSpace;
    }

    @Override
    public String toString()
    {
        return "Pattern";
    }
}
