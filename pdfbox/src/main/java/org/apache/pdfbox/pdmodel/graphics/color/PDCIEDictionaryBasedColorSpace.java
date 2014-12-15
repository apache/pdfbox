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
import org.apache.pdfbox.cos.COSBase;
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
    protected COSDictionary dictionary;

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
        COSArray wp = (COSArray) dictionary.getDictionaryObject(COSName.WHITE_POINT);
        if (wp == null)
        {
            wp = new COSArray();
            wp.add(new COSFloat(1.0f));
            wp.add(new COSFloat(1.0f));
            wp.add(new COSFloat(1.0f));
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
        COSArray bp = (COSArray) dictionary.getDictionaryObject(COSName.BLACK_POINT);
        if (bp == null)
        {
            bp = new COSArray();
            bp.add(new COSFloat(0.0f));
            bp.add(new COSFloat(0.0f));
            bp.add(new COSFloat(0.0f));
        }
        return new PDTristimulus(bp);
    }

    /**
     * This will set the whitepoint tristimulus. As this is a required field
     * this null should not be passed into this function.
     *
     * @param whitepoint the whitepoint tristimulus
     */
    public void setWhitePoint(PDTristimulus whitepoint)
    {
        COSBase wpArray = whitepoint.getCOSObject();
        if (wpArray != null)
        {
            dictionary.setItem(COSName.WHITE_POINT, wpArray);
        }
        fillWhitepointCache(whitepoint);
    }

    /**
     * This will set the BlackPoint tristimulus. As this is a required field
     * this null should not be passed into this function.
     *
     * @param blackpoint the BlackPoint tristimulus
     */
    public void setBlackPoint(PDTristimulus blackpoint)
    {
        COSBase bpArray = null;
        if (blackpoint != null)
        {
            bpArray = blackpoint.getCOSObject();
        }
        dictionary.setItem(COSName.BLACK_POINT, bpArray);
    }

}
