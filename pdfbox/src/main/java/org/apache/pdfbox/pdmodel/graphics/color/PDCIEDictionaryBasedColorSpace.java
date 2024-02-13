/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.color.ColorSpace;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;

/**
 * CIE-based colour spaces that use a dictionary.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public abstract class PDCIEDictionaryBasedColorSpace extends PDCIEBasedColorSpace
{
    protected final COSDictionary dictionary;

    private static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    // we need to cache whitepoint values, because using getWhitePoint()
    // would create a new default object for each pixel conversion if the original
    // PDF didn't have a whitepoint array
    protected float wpX = 1;
    protected float wpY = 1;
    protected float wpZ = 1;

    protected PDCIEDictionaryBasedColorSpace(COSName cosName)
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add(cosName);
        array.add(dictionary);

        fillWhitepointCache(getWhitepoint());
    }

    /**
     * Creates a new CalRGB color space using the given COS array.
     *
     * @param rgb the cos array which represents this color space
     */
    protected PDCIEDictionaryBasedColorSpace(COSArray rgb)
    {
        array = rgb;
        dictionary = (COSDictionary) array.getObject(1);

        fillWhitepointCache(getWhitepoint());
    }

    /**
     * Tests if the current point is the white point.
     *
     * @return true if the current point is the white point.
     */
    protected boolean isWhitePoint()
    {
        return  Float.compare(wpX, 1) == 0 &&
                Float.compare(wpY, 1) == 0 && 
                Float.compare(wpZ, 1)  == 0;
    }

    private void fillWhitepointCache(PDTristimulus whitepoint)
    {
        wpX = whitepoint.getX();
        wpY = whitepoint.getY();
        wpZ = whitepoint.getZ();
    }

    protected float[] convXYZtoRGB(float x, float y, float z)
    {
        // toRGB() malfunctions with negative values
        // XYZ must be non-negative anyway:
        // http://ninedegreesbelow.com/photography/icc-profile-negative-tristimulus.html
        if (x < 0)
        {
            x = 0;
        }
        if (y < 0)
        {
            y = 0;
        }
        if (z < 0)
        {
            z = 0;
        }
        return CIEXYZ.toRGB(new float[]
        {
            x, y, z
        });
    }

    /**
     * This will return the whitepoint tristimulus. As this is a required field
     * this will never return null. A default of 1,1,1 will be returned if the
     * pdf does not have any values yet.
     *
     * @return the whitepoint tristimulus
     */
    public final PDTristimulus getWhitepoint()
    {
        COSArray wp = dictionary.getCOSArray(COSName.WHITE_POINT);
        if (wp == null)
        {
            wp = new COSArray();
            wp.add(COSFloat.ONE);
            wp.add(COSFloat.ONE);
            wp.add(COSFloat.ONE);
        }
        return new PDTristimulus(wp);
    }

    /**
     * This will return the BlackPoint tristimulus. This is an optional field
     * but has defaults so this will never return null. A default of 0,0,0 will
     * be returned if the pdf does not have any values yet.
     *
     * @return the blackpoint tristimulus
     */
    public final PDTristimulus getBlackPoint()
    {
        COSArray bp = dictionary.getCOSArray(COSName.BLACK_POINT);
        if (bp == null)
        {
            bp = new COSArray();
            bp.add(COSFloat.ZERO);
            bp.add(COSFloat.ZERO);
            bp.add(COSFloat.ZERO);
        }
        return new PDTristimulus(bp);
    }

    /**
     * This will set the whitepoint tristimulus. As this is a required field, null should not be
     * passed into this function.
     *
     * @param whitepoint the whitepoint tristimulus.
     * @throws IllegalArgumentException if null is passed as argument.
     */
    public void setWhitePoint(PDTristimulus whitepoint)
    {
        if (whitepoint == null)
        {
            throw new IllegalArgumentException("Whitepoint may not be null");
        }
        dictionary.setItem(COSName.WHITE_POINT, whitepoint);
        fillWhitepointCache(whitepoint);
    }

    /**
     * This will set the BlackPoint tristimulus.
     *
     * @param blackpoint the BlackPoint tristimulus
     */
    public void setBlackPoint(PDTristimulus blackpoint)
    {
        dictionary.setItem(COSName.BLACK_POINT, blackpoint);
    }

}
