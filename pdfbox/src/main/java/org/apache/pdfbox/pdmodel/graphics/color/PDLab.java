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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * A Lab colour space is a CIE-based ABC colour space with two transformation stages.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDLab extends PDCIEDictionaryBasedColorSpace
{
    private PDColor initialColor;
    
    /**
     * Creates a new Lab color space.
     */
    public PDLab()
    {
        super(COSName.LAB);
    }

    /**
     * Creates a new Lab color space from a PDF array.
     * @param lab the color space array
     */
    public PDLab(COSArray lab)
    {
        super(lab);
    }
    
    @Override
    public String getName()
    {
        return COSName.LAB.getName();
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();

        float minA = getARange().getMin();
        float maxA = getARange().getMax();
        float minB = getBRange().getMin();
        float maxB = getBRange().getMax();

        // always three components: ABC
        float[] abc = new float[3];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, abc);

                // 0..255 -> 0..1
                abc[0] /= 255;
                abc[1] /= 255;
                abc[2] /= 255;
                
                // scale to range
                abc[0] *= 100;
                abc[1] = minA + (abc[1] * (maxA - minA));
                abc[2] = minB + (abc[2] * (maxB - minB));

                float[] rgb = toRGB(abc);

                // 0..1 -> 0..255
                rgb[0] *= 255;
                rgb[1] *= 255;
                rgb[2] *= 255;

                rgbRaster.setPixel(x, y, rgb);
            }
        }

        return rgbImage;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        // CIE LAB to RGB, see http://en.wikipedia.org/wiki/Lab_color_space

        // L*
        float lstar = (value[0] + 16f) * (1f / 116f);

        // TODO: how to use the blackpoint? scale linearly between black & white?

        // XYZ
        float x = wpX * inverse(lstar + value[1] * (1f / 500f));
        float y = wpY * inverse(lstar);
        float z = wpZ * inverse(lstar - value[2] * (1f / 200f));
        
        return convXYZtoRGB(x, y, z);
    }

    // reverse transformation (f^-1)
    private float inverse(float x)
    {
        if (x > 6.0 / 29.0)
        {
            return x * x * x;
        }
        else
        {
            return (108f / 841f) * (x - (4f / 29f));
        }
    }

    @Override
    public int getNumberOfComponents()
    {
        return 3;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        PDRange a = getARange();
        PDRange b = getARange();
        return new float[] { 0, 100, a.getMin(), a.getMax(), b.getMin(), b.getMax() };
    }

    @Override
    public PDColor getInitialColor()
    {
        if (initialColor == null)
        {
            initialColor = new PDColor(new float[] {
                    0,
                    Math.max(0, getARange().getMin()),
                    Math.max(0, getBRange().getMin()) },
                    this);
        }
        return initialColor;
    }

    /**
     * creates a range array with default values (-100..100 -100..100).
     * @return the new range array.
     */
    private COSArray getDefaultRangeArray()
    {
        COSArray range = new COSArray();
        range.add(new COSFloat(-100));
        range.add(new COSFloat(100));
        range.add(new COSFloat(-100));
        range.add(new COSFloat(100));
        return range;
    }

    /**
     * This will get the valid range for the "a" component.
     * If none is found then the default will be returned, which is -100..100.
     * @return the "a" range.
     */
    public PDRange getARange()
    {
        COSArray rangeArray = (COSArray) dictionary.getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = getDefaultRangeArray();
        }
        return new PDRange(rangeArray, 0);
    }

    /**
     * This will get the valid range for the "b" component.
     * If none is found  then the default will be returned, which is -100..100.
     * @return the "b" range.
     */
    public PDRange getBRange()
    {
        COSArray rangeArray = (COSArray) dictionary.getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = getDefaultRangeArray();
        }
        return new PDRange(rangeArray, 1);
    }

    /**
     * This will set the a range for the "a" component.
     * @param range the new range for the "a" component, 
     * or null if defaults (-100..100) are to be set.
     */
    public void setARange(PDRange range)
    {
        setComponentRangeArray(range, 0);
    }

    /**
     * This will set the "b" range for this color space.
     * @param range the new range for the "b" component,
     * or null if defaults (-100..100) are to be set.
     */
    public void setBRange(PDRange range)
    {
        setComponentRangeArray(range, 2);
    }

    private void setComponentRangeArray(PDRange range, int index)
    {
        COSArray rangeArray = (COSArray) dictionary.getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = getDefaultRangeArray();
        }
        if (range == null)
        {
            // reset to defaults
            rangeArray.set(index, new COSFloat(-100));
            rangeArray.set(index + 1, new COSFloat(100));
        }
        else
        {
            rangeArray.set(index, new COSFloat(range.getMin()));
            rangeArray.set(index + 1, new COSFloat(range.getMax()));
        }
        dictionary.setItem(COSName.RANGE, rangeArray);
        initialColor = null;
    }

}
