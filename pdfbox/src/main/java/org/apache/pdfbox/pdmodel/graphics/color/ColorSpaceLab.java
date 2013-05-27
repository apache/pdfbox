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

import java.awt.color.ColorSpace;

import org.apache.pdfbox.pdmodel.common.PDRange;

/**
 * This class represents a CalRGB color space.
 * 
 * The color conversion uses the algorithm described on wikipedia.
 * 
 * The blackpoint isn't used, as I can't find any hint how to do that.
 * 
 */
public class ColorSpaceLab extends ColorSpace
{
    private static final long serialVersionUID = -5769360600770807798L;

    private PDTristimulus whitepoint = null;
    // TODO unused??
    private PDTristimulus blackpoint = null;
    private PDRange aRange = null;
    private PDRange bRange = null;

    /**
     * Default Constructor.
     * 
     */
    public ColorSpaceLab()
    {
        super(ColorSpace.TYPE_3CLR, 3);
    }

    /** 
     * Constructor.
     * 
     * @param whitept whitepoint values
     * @param blackpt blackpoint values
     * @param a range for value a 
     * @param b range for value b
     */
    public ColorSpaceLab(PDTristimulus whitept, PDTristimulus blackpt, PDRange a, PDRange b)
    {
        this();
        whitepoint = whitept;
        blackpoint = blackpt;
        aRange = a;
        bRange = b;
    }

    /**
     * Clip the given value to the given range.
     * 
     * @param x
     *            the given value to be clipped
     * @param range
     *            the range to be used to clip the value to
     * 
     * @return the clipped value
     */
    private float clipToRange(float x, PDRange range)
    {
        return Math.min(Math.max(x, range.getMin()), range.getMax());
    }

    private static final float VALUE_6_29 = 6 / 29;
    private static final float VALUE_4_29 = 4 / 29;
    private static final float VALUE_108_841 = 108 / 841;
    private static final float VALUE_841_108 = 841 / 108;
    private static final float VALUE_216_24389 = 216 / 24389;

    private float calculateStage2ToXYZ(float value)
    {
        if (value >= VALUE_6_29)
        {
            return (float) Math.pow(value, 3);
        }
        else
        {
            return VALUE_108_841 * (value - VALUE_4_29);
        }
    }

    private float calculateStage2FromXYZ(float value)
    {
        if (value >= VALUE_216_24389)
        {
            return (float) Math.pow(value, 1 / 3);
        }
        else
        {
            return VALUE_841_108 * value + VALUE_4_29;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public float[] toRGB(float[] colorvalue)
    {
        ColorSpace colorspaceXYZ = ColorSpace.getInstance(CS_CIEXYZ);
        return colorspaceXYZ.toRGB(toCIEXYZ(colorvalue));
    }

    /**
     * {@inheritDoc}
     * 
     */
    public float[] fromRGB(float[] rgbvalue)
    {
        ColorSpace colorspaceXYZ = ColorSpace.getInstance(CS_CIEXYZ);
        return fromCIEXYZ(colorspaceXYZ.fromRGB(rgbvalue));
    }

    /**
     * {@inheritDoc}
     * 
     */
    public float[] toCIEXYZ(float[] colorvalue)
    {
        float a = colorvalue[1];
        if (aRange != null)
        {
            // clip the a value to the given range
            a = clipToRange(a, aRange);
        }
        float b = colorvalue[2];
        if (bRange != null)
        {
            // clip the b value to the given range
            b = clipToRange(b, bRange);
        }
        float m = (colorvalue[0] + 16) / 116;
        float l = m + (a / 500);
        float n = m - (b / 200);

        float x = whitepoint.getX() * calculateStage2ToXYZ(l);
        float y = whitepoint.getY() * calculateStage2ToXYZ(m);
        float z = whitepoint.getZ() * calculateStage2ToXYZ(n);
        return new float[] { x, y, z };
    }

    /**
     * {@inheritDoc}
     * 
     */
    public float[] fromCIEXYZ(float[] colorvalue)
    {
        float x = calculateStage2FromXYZ(colorvalue[0] / whitepoint.getX());
        float y = calculateStage2FromXYZ(colorvalue[1] / whitepoint.getY());
        float z = calculateStage2FromXYZ(colorvalue[2] / whitepoint.getZ());

        float l = 116 * y - 116;
        float a = 500 * (x - y);
        float b = 200 * (y - z);
        if (aRange != null)
        {
            // clip the a value to the given range
            a = clipToRange(a, aRange);
        }
        if (bRange != null)
        {
            // clip the b value to the given range
            b = clipToRange(b, bRange);
        }
        return new float[] { l, a, b };
    }

}
