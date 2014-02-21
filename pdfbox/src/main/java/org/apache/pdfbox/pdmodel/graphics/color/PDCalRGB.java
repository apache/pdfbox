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

import org.apache.pdfbox.pdmodel.common.PDMatrix;

import java.awt.color.ColorSpace;

/**
 * A CalRGB colour space is a CIE-based colour space with one transformation stage instead of two.
 * In this type of space, A, B, and C represent calibrated red, green, and blue colour values.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCalRGB extends PDCIEBasedColorSpace
{
    private static final PDColor INITIAL_COLOR = new PDColor(new float[] { 0, 0, 0 });

    protected COSArray array;
    protected COSDictionary dictionary;

    /**
     * Creates a new CalRGB color space.
     */
    public PDCalRGB()
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add(COSName.CALRGB);
        array.add(dictionary);
    }

    /**
     * Creates a new CalRGB color space using the given COS array.
     * @param rgb the cos array which represents this color space
     */
    public PDCalRGB(COSArray rgb)
    {
        array = rgb;
        dictionary = (COSDictionary)array.getObject(1);
    }

    @Override
    public String getName()
    {
        return COSName.CALRGB.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 3;
    }

    @Override
    public float[] getDefaultDecode()
    {
        return new float[] { 0, 1, 0, 1, 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return INITIAL_COLOR;
    }

    @Override
    public final float[] toRGB(float[] value)
    {
        // this is a hack, we simply skip CIE calibration of the RGB value
        return new float[] { value[0], value[1], value[2] };
    }

    private float clamp(float value)
    {
        return Math.min(Math.max(value, 0), 1);
    }

    /**
     * Returns the whitepoint tristimulus.
     * A default of 1,1,1 will be returned if the PDF does not have any values yet.
     * @return the whitepoint tristimulus.
     */
    public final PDTristimulus getWhitepoint()
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
     * Returns the blackpoint tristimulus.
     * A default of 0,0,0 will be returned if the PDF does not have any values yet.
     * @return the blackpoint tristimulus
     */
    public final PDTristimulus getBlackPoint()
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

    /**
     * Returns the the gamma value.
     * If none is present then the default of 1,1,1 will be returned.
     * @return the gamma value
     */
    public final PDGamma getGamma()
    {
        COSArray gamma = (COSArray)dictionary.getDictionaryObject(COSName.GAMMA);
        if(gamma == null)
        {
            gamma = new COSArray();
            gamma.add(new COSFloat(1.0f));
            gamma.add(new COSFloat(1.0f));
            gamma.add(new COSFloat(1.0f));
            dictionary.setItem(COSName.GAMMA, gamma);
        }
        return new PDGamma(gamma);
    }

    /**
     * Returns the linear interpretation matrix.
     * If the underlying dictionary contains null then the identity matrix will be returned.
     * @return the linear interpretation matrix
     */
    public final PDMatrix getGammaMatrix()
    {
        COSArray matrix = (COSArray)dictionary.getDictionaryObject(COSName.MATRIX);
        if(matrix == null)
        {
            return new PDMatrix();
        }
        else
        {
           return new PDMatrix(matrix);
        }
    }

    /**
     * Sets the whitepoint tristimulus
     * @param whitepoint the whitepoint tristimulus, which may not be null
     */
    public final void setWhitepoint(PDTristimulus whitepoint)
    {
        COSBase wpArray = whitepoint.getCOSObject();
        if(wpArray != null)
        {
            dictionary.setItem(COSName.WHITE_POINT, wpArray);
        }
    }

    /**
     * Sets the blackpoint tristimulus
     * @param blackpoint the blackpoint tristimulus, which may not be null
     */
    public final void setBlackPoint(PDTristimulus blackpoint)
    {
        COSBase bpArray = null;
        if(blackpoint != null)
        {
            bpArray = blackpoint.getCOSObject();
        }
        dictionary.setItem(COSName.BLACK_POINT, bpArray);
    }

    /**
     * Sets the gamma value.
     * @param gamma the new gamma value
     */
    public final void setGamma(PDGamma gamma)
    {
        COSArray array = null;
        if(gamma != null)
        {
            array = gamma.getCOSArray();
        }
        dictionary.setItem(COSName.GAMMA, array);
    }

    /**
     * Sets the linear interpretation matrix.
     * Passing in null will clear the matrix.
     * @param matrix the new linear interpretation matrix, or null
     */
    public final void setGammaMatrix(PDMatrix matrix)
    {
        COSArray matrixArray = null;
        if(matrix != null)
        {
            matrixArray = matrix.getCOSArray();
        }
        dictionary.setItem(COSName.MATRIX, matrixArray);
    }
}
