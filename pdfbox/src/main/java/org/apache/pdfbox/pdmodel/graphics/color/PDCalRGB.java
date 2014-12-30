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

import org.apache.pdfbox.util.Matrix;

/**
 * A CalRGB colour space is a CIE-based colour space with one transformation stage instead of two.
 * In this type of space, A, B, and C represent calibrated red, green, and blue colour values.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCalRGB extends PDCIEDictionaryBasedColorSpace
{
    private final PDColor initialColor = new PDColor(new float[] { 0, 0, 0 }, this);

    /**
     * Creates a new CalRGB color space.
     */
    public PDCalRGB()
    {
        super(COSName.CALRGB);
    }

    /**
     * Creates a new CalRGB color space using the given COS array.
     * @param rgb the cos array which represents this color space
     */
    public PDCalRGB(COSArray rgb)
    {
        super(rgb);
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
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1, 0, 1, 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        if (wpX == 1 && wpY == 1 && wpZ == 1)
        {
            float a = value[0];
            float b = value[1];
            float c = value[2];

            PDGamma gamma = getGamma();
            float powAR = (float)Math.pow(a, gamma.getR());
            float powBG = (float)Math.pow(b, gamma.getG());
            float powCB = (float)Math.pow(c, gamma.getB());

            float[] matrix = getMatrix();
            float mXA = matrix[0];
            float mYA = matrix[1];
            float mZA = matrix[2];
            float mXB = matrix[3];
            float mYB = matrix[4];
            float mZB = matrix[5];
            float mXC = matrix[6];
            float mYC = matrix[7];
            float mZC = matrix[8];

            float x = mXA * powAR + mXB * powBG + mXC * powCB;
            float y = mYA * powAR + mYB * powBG + mYC * powCB;
            float z = mZA * powAR + mZB * powBG + mZC * powCB;
            return convXYZtoRGB(x, y, z);
        }
        else
        {
            // this is a hack, we simply skip CIE calibration of the RGB value
            // this works only with whitepoint D65 (0.9505 1.0 1.089)
            // see PDFBOX-2553
            return new float[] { value[0], value[1], value[2] };
        }
    }

    /**
     * Returns the gamma value.
     * If none is present then the default of 1,1,1 will be returned.
     * @return the gamma value
     */
    public final PDGamma getGamma()
    {
        COSArray gammaArray = (COSArray) dictionary.getDictionaryObject(COSName.GAMMA);
        if (gammaArray == null)
        {
            gammaArray = new COSArray();
            gammaArray.add(new COSFloat(1.0f));
            gammaArray.add(new COSFloat(1.0f));
            gammaArray.add(new COSFloat(1.0f));
            dictionary.setItem(COSName.GAMMA, gammaArray);
        }
        return new PDGamma(gammaArray);
    }

    /**
     * Returns the linear interpretation matrix, which is an array of nine numbers.
     * If the underlying dictionary contains null then the identity matrix will be returned.
     * @return the linear interpretation matrix
     */
    public final float[] getMatrix()
    {
        COSArray matrix = (COSArray)dictionary.getDictionaryObject(COSName.MATRIX);
        if (matrix == null)
        {
            return new float[] {  1, 0, 0, 0, 1, 0, 0, 0, 1 };
        }
        else
        {
           return matrix.toFloatArray();
        }
    }

    /**
     * Sets the gamma value.
     * @param gamma the new gamma value
     */
    public final void setGamma(PDGamma gamma)
    {
        COSArray gammaArray = null;
        if(gamma != null)
        {
            gammaArray = gamma.getCOSArray();
        }
        dictionary.setItem(COSName.GAMMA, gammaArray);
    }

    /**
     * Sets the linear interpretation matrix.
     * Passing in null will clear the matrix.
     * @param matrix the new linear interpretation matrix, or null
     */
    public final void setMatrix(Matrix matrix)
    {
        COSArray matrixArray = null;
        if(matrix != null)
        {
            matrixArray = matrix.toCOSArray();
        }
        dictionary.setItem(COSName.MATRIX, matrixArray);
    }
}
