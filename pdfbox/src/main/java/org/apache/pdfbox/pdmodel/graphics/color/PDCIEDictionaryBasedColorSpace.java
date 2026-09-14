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
    protected COSDictionary dictionary;

    private static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    // java.awt.color.ColorSpace(CS_CIEXYZ) treats its input as relative to the D50 profile
    // connection space (verified: XYZ(0.9642 1.0 0.8249) is the only whitepoint that round-trips
    // to RGB(1,1,1)). A CalRGB/CalGray/Lab dictionary can declare any other whitepoint, so the
    // XYZ computed from it must be chromatically adapted to D50 first, or non-D50 whitepoints
    // pick up a uniform color cast - see PDFBOX-6260, where whitepoint (0.5505 1.0 1.989) made
    // pure white input render as cyan and mid-gray as a cyan-blue.
    private static final double[][] BRADFORD = {
        { 0.8951000,  0.2664000, -0.1614000},
        {-0.7502000,  1.7135000,  0.0367000},
        { 0.0389000, -0.0685000,  1.0296000}
    };
    private static final double[][] BRADFORD_INV = invert(BRADFORD);
    private static final double[] PCS_WHITE_D50 = {0.9642, 1.0, 0.8249};

    // we need to cache whitepoint values, because using getWhitePoint()
    // would create a new default object for each pixel conversion if the original
    // PDF didn't have a whitepoint array
    protected float wpX = 1;
    protected float wpY = 1;
    protected float wpZ = 1;

    // chromatic adaptation matrix from this space's whitepoint to the D50 PCS white, cached
    // per color space instance (not per pixel) since it only depends on the declared whitepoint
    private float[] chromaticAdaptation;

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
        chromaticAdaptation = adaptationMatrix(new double[] { wpX, wpY, wpZ }, PCS_WHITE_D50);
    }

    protected float[] convXYZtoRGB(float x, float y, float z)
    {
        float ax = chromaticAdaptation[0] * x + chromaticAdaptation[1] * y + chromaticAdaptation[2] * z;
        float ay = chromaticAdaptation[3] * x + chromaticAdaptation[4] * y + chromaticAdaptation[5] * z;
        float az = chromaticAdaptation[6] * x + chromaticAdaptation[7] * y + chromaticAdaptation[8] * z;

        // toRGB() malfunctions with negative values
        // XYZ must be non-negative anyway:
        // http://ninedegreesbelow.com/photography/icc-profile-negative-tristimulus.html
        if (ax < 0)
        {
            ax = 0;
        }
        if (ay < 0)
        {
            ay = 0;
        }
        if (az < 0)
        {
            az = 0;
        }
        return CIEXYZ.toRGB(new float[]
        {
            ax, ay, az
        });
    }

    private static float[] adaptationMatrix(double[] srcWhite, double[] dstWhite)
    {
        double[] srcCone = multiply(BRADFORD, srcWhite);
        double[] dstCone = multiply(BRADFORD, dstWhite);
        double[][] scale = {
            { dstCone[0] / srcCone[0], 0, 0 },
            { 0, dstCone[1] / srcCone[1], 0 },
            { 0, 0, dstCone[2] / srcCone[2] }
        };
        double[][] m = multiply(multiply(BRADFORD_INV, scale), BRADFORD);
        return new float[]
        {
            (float) m[0][0], (float) m[0][1], (float) m[0][2],
            (float) m[1][0], (float) m[1][1], (float) m[1][2],
            (float) m[2][0], (float) m[2][1], (float) m[2][2]
        };
    }

    private static double[] multiply(double[][] m, double[] v)
    {
        return new double[]
        {
            m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
            m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
            m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]
        };
    }

    private static double[][] multiply(double[][] a, double[][] b)
    {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    private static double[][] invert(double[][] m)
    {
        double det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                   - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                   + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
        return new double[][]
        {
            {
                (m[1][1] * m[2][2] - m[1][2] * m[2][1]) / det,
                -(m[0][1] * m[2][2] - m[0][2] * m[2][1]) / det,
                (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det
            },
            {
                -(m[1][0] * m[2][2] - m[1][2] * m[2][0]) / det,
                (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det,
                -(m[0][0] * m[1][2] - m[0][2] * m[1][0]) / det
            },
            {
                (m[1][0] * m[2][1] - m[1][1] * m[2][0]) / det,
                -(m[0][0] * m[2][1] - m[0][1] * m[2][0]) / det,
                (m[0][0] * m[1][1] - m[0][1] * m[1][0]) / det
            }
        };
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
        COSArray bp = dictionary.getCOSArray(COSName.BLACK_POINT);
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
