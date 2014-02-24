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

    private COSArray array;
    private COSDictionary dictionary;
    private PDColor initialColor;

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
    }

    @Override
    public String getName()
    {
        return COSName.LAB.getName();
    }

    @Override
    public COSBase getCOSObject()
    {
        return array;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        float minA = getARange().getMin();
        float maxA = getARange().getMax();
        float minB = getBRange().getMin();
        float maxB = getBRange().getMax();

        // scale to range
        float l = value[0] * 100;
        float a = minA + (value[1] * (maxA - minA));
        float b = minB + (value[2] * (maxB - minB));

        return labToRGB(l, a, b, getWhitepoint(), getBlackPoint());
    }

    // CIE LAB to RGB, see http://en.wikipedia.org/wiki/Lab_color_space
    private float[] labToRGB(float l, float a, float b,
                             PDTristimulus whitepoint,
                             PDTristimulus blackpoint)
    {
        // L*
        float lstar = (l + 16f) * (1f / 116f);

        // white point
        float wpX = whitepoint.getX();
        float wpY = whitepoint.getY();
        float wpZ = whitepoint.getZ();

        // TODO: how to use the blackpoint? scale linearly between black & white?

        // XYZ
        float x = wpX * inverse(lstar + a * (1f / 500f));
        float y = wpY * inverse(lstar);
        float z = wpZ * inverse(lstar - b * (1f / 200f));

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
        if (initialColor != null)
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
            dictionary.setItem(COSName.WHITE_POINT, wp);
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
            dictionary.setItem(COSName.BLACK_POINT, bp);
        }
        return new PDTristimulus(bp);
    }

    private COSArray getRangeArray()
    {
        COSArray range = (COSArray)dictionary.getDictionaryObject(COSName.RANGE);
        if(range == null)
        {
            range = new COSArray();
            dictionary.setItem(COSName.RANGE, array);
            range.add(new COSFloat(-100));
            range.add(new COSFloat(100));
            range.add(new COSFloat(-100));
            range.add(new COSFloat(100));
        }
        return range;
    }

    /**
     * This will get the valid range for the "a" component.
     * If none is found then the default will be returned, which is -100 to 100.
     * @return the "a" range
     */
    public PDRange getARange()
    {
        COSArray range = getRangeArray();
        return new PDRange(range, 0);
    }

    /**
     * This will get the valid range for the "b" component.
     * If none is found  then the default will be returned, which is -100 to 100.
     * @return the "b" range
     */
    public PDRange getBRange()
    {
        COSArray range = getRangeArray();
        return new PDRange(range, 1);
    }

    /**
     * This will set the whitepoint tristimulus.
     * As this is a required field this null should not be passed into this function.
     * @param whitepoint the whitepoint tristimulus
     */
    public void setWhitepoint(PDTristimulus whitepoint)
    {
        COSBase wpArray = whitepoint.getCOSObject();
        if(wpArray != null)
        {
            dictionary.setItem(COSName.WHITE_POINT, wpArray);
        }
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
     * @param range the new range for the "a" component
     */
    public void setARange(PDRange range)
    {
        COSArray rangeArray = null;
        //if null then reset to defaults
        if(range == null)
        {
            rangeArray = getRangeArray();
            rangeArray.set(0, new COSFloat(-100));
            rangeArray.set(1, new COSFloat(100));
        }
        else
        {
            rangeArray = range.getCOSArray();
        }
        dictionary.setItem(COSName.RANGE, rangeArray);
        initialColor = null;
    }

    /**
     * This will set the "b" range for this color space.
     * @param range the new range for the "b" component
     */
    public void setBRange(PDRange range)
    {
        COSArray rangeArray = null;
        //if null then reset to defaults
        if(range == null)
        {
            rangeArray = getRangeArray();
            rangeArray.set(2, new COSFloat(-100));
            rangeArray.set(3, new COSFloat(100));
        }
        else
        {
            rangeArray = range.getCOSArray();
        }
        dictionary.setItem(COSName.RANGE, rangeArray);
        initialColor = null;
    }
}
