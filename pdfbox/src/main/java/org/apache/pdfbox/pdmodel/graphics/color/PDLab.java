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
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * A Lab colour space is a CIE-based ABC colour space with two transformation stages.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDLab extends PDCIEBasedColorSpace
{
    private static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    private final COSDictionary dictionary;
    private PDColor initialColor;
    
    // we need to cache whitepoint values, because using getWhitePoint()
    // would create a new default object for each pixel conversion if the original
    // PDF didn't have a whitepoint array
    private float wpX = 1;
    private float wpY = 1;
    private float wpZ = 1;

    /**
     * Creates a new Lab color space.
     */
    public PDLab()
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add(COSName.LAB);
        array.add(dictionary);
    }

    /**
     * Creates a new Lab color space from a PDF array.
     * @param lab the color space array
     */
    public PDLab(COSArray lab)
    {
        array = lab;
        dictionary = (COSDictionary)array.getObject(1);
        
        // init whitepoint cache
        PDTristimulus whitepoint = getWhitepoint();
        wpX = whitepoint.getX();
        wpY = whitepoint.getY();
        wpZ = whitepoint.getZ();
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

        // XYZ to RGB
        return CIEXYZ.toRGB(new float[] { x, y, z });
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
                    Math.max(0, getBRange().getMin()) });
        }
        return initialColor;
    }

    /**
     * This will return the whitepoint tristimulus.
     * As this is a required field this will never return null.
     * A default of 1,1,1 will be returned if the pdf does not have any values yet.
     * @return the whitepoint tristimulus
     */
    public PDTristimulus getWhitepoint()
    {
        COSArray wp = (COSArray)dictionary.getDictionaryObject(COSName.WHITE_POINT);
        if(wp == null)
        {
            wp = new COSArray();
            wp.add(new COSFloat(1.0f));
            wp.add(new COSFloat(1.0f));
            wp.add(new COSFloat(1.0f));
        }
        return new PDTristimulus(wp);
    }

    /**
     * This will return the BlackPoint tristimulus.
     * This is an optional field but has defaults so this will never return null.
     * A default of 0,0,0 will be returned if the pdf does not have any values yet.
     * @return the blackpoint tristimulus
     */
    public PDTristimulus getBlackPoint()
    {
        COSArray bp = (COSArray)dictionary.getDictionaryObject(COSName.BLACK_POINT);
        if(bp == null)
        {
            bp = new COSArray();
            bp.add(new COSFloat(0.0f));
            bp.add(new COSFloat(0.0f));
            bp.add(new COSFloat(0.0f));
        }
        return new PDTristimulus(bp);
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
     * This will set the whitepoint tristimulus.
     * As this is a required field this null should not be passed into this function.
     * @param whitepoint the whitepoint tristimulus
     */
    public void setWhitePoint(PDTristimulus whitepoint)
    {
        COSBase wpArray = whitepoint.getCOSObject();
        if(wpArray != null)
        {
            dictionary.setItem(COSName.WHITE_POINT, wpArray);
        }
        
        // update cached values
        wpX = whitepoint.getX();
        wpY = whitepoint.getY();
        wpZ = whitepoint.getZ();
    }

    /**
     * This will set the BlackPoint tristimulus.
     * As this is a required field this null should not be passed into this function.
     * @param blackpoint the BlackPoint tristimulus
     */
    public void setBlackPoint(PDTristimulus blackpoint)
    {
        COSBase bpArray = null;
        if(blackpoint != null)
        {
            bpArray = blackpoint.getCOSObject();
        }
        dictionary.setItem(COSName.BLACK_POINT, bpArray);
    }

    /**
     * This will set the a range for the "a" component.
     * @param range the new range for the "a" component, 
     * or null if defaults (-100..100) are to be set.
     */
    public void setARange(PDRange range)
    {
        COSArray rangeArray = (COSArray) dictionary.getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = getDefaultRangeArray();
        }
        //if null then reset to defaults
        if(range == null)
        {
            rangeArray.set(0, new COSFloat(-100));
            rangeArray.set(1, new COSFloat(100));
        }
        else
        {
            rangeArray.set(0, new COSFloat(range.getMin()));
            rangeArray.set(1, new COSFloat(range.getMax()));
        }
        dictionary.setItem(COSName.RANGE, rangeArray);
        initialColor = null;
    }

    /**
     * This will set the "b" range for this color space.
     * @param range the new range for the "b" component,
     * or null if defaults (-100..100) are to be set.
     */
    public void setBRange(PDRange range)
    {
        COSArray rangeArray = (COSArray) dictionary.getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = getDefaultRangeArray();
        }
        //if null then reset to defaults
        if(range == null)
        {
            rangeArray.set(2, new COSFloat(-100));
            rangeArray.set(3, new COSFloat(100));
        }
        else
        {
            rangeArray.set(2, new COSFloat(range.getMin()));
            rangeArray.set(3, new COSFloat(range.getMax()));
        }
        dictionary.setItem(COSName.RANGE, rangeArray);
        initialColor = null;
    }
}
