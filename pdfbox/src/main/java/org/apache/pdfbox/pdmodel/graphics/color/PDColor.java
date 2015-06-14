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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

import java.io.IOException;
import java.util.Arrays;

/**
 * A color value, consisting of one or more color components, or for pattern color spaces,
 * a name and optional color components.
 * Color values are not associated with any given color space.
 *
 * Instances of PDColor are immutable.
 *
 * @author John Hewson
 */
public final class PDColor
{
    private final float[] components;
    private final COSName patternName;
    private final PDColorSpace colorSpace;

    /**
     * Creates a PDColor containing the given color value.
     * @param array a COS array containing the color value
     * @param colorSpace color space in which the color value is defined
     */
    public PDColor(COSArray array, PDColorSpace colorSpace)
    {
        if (array.size() > 0 && array.get(array.size() - 1) instanceof COSName)
        {
            // color components (optional), for the color of an uncoloured tiling pattern
            components = new float[array.size() - 1];
            for (int i = 0; i < array.size() - 1; i++)
            {
                components[i] = ((COSNumber)array.get(i)).floatValue();
            }

            // pattern name (required)
            patternName = (COSName)array.get(array.size() - 1);
        }
        else
        {
            // color components only
            components = new float[array.size()];
            for (int i = 0; i < array.size(); i++)
            {
                components[i] = ((COSNumber)array.get(i)).floatValue();
            }
            patternName = null;
        }
        this.colorSpace = colorSpace;
    }

    /**
     * Creates a PDColor containing the given color component values.
     * @param components array of color component values
     * @param colorSpace color space in which the components are defined
     */
    public PDColor(float[] components, PDColorSpace colorSpace)
    {
        this.components = components.clone();
        this.patternName = null;
        this.colorSpace = colorSpace;
    }

    /**
     * Creates a PDColor containing the given pattern name.
     * @param patternName the name of a pattern in a pattern dictionary
     * @param colorSpace color space in which the pattern is defined
     */
    public PDColor(COSName patternName, PDColorSpace colorSpace)
    {
        this.components = new float[0];
        this.patternName = patternName;
        this.colorSpace = colorSpace;
    }

    /**
     * Creates a PDColor containing the given color component values and pattern name.
     * @param components array of color component values
     * @param patternName the name of a pattern in a pattern dictionary
     * @param colorSpace color space in which the pattern/components are defined
     */
    public PDColor(float[] components, COSName patternName, PDColorSpace colorSpace)
    {
        this.components = components.clone();
        this.patternName = patternName;
        this.colorSpace = colorSpace;
    }

    /**
     * Returns the components of this color value.
     * @return the components of this color value
     */
    public float[] getComponents()
    {
        return components.clone();
    }

    /**
     * Returns the pattern name from this color value.
     * @return the pattern name from this color value
     */
    public COSName getPatternName()
    {
        return patternName;
    }

    /**
     * Returns true if this color value is a pattern.
     * @return true if this color value is a pattern
     */
    public boolean isPattern()
    {
        return patternName != null;
    }

    /**
     * Returns the packed RGB value for this color, if any.
     * @return RGB
     * @throws IOException if the color conversion fails
     * @throws IllegalStateException if this color value is a pattern.
     */
    public int toRGB() throws IOException
    {
        float[] floats = colorSpace.toRGB(components);
        int r = Math.round(floats[0] * 255);
        int g = Math.round(floats[1] * 255);
        int b = Math.round(floats[2] * 255);
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
    }

    /**
     * Returns the color component values as a COS array
     * @return the color component values as a COS array
     */
    public COSArray toCOSArray()
    {
        COSArray array = new COSArray();
        array.setFloatArray(components);
        if (patternName != null)
        {
            array.add(patternName);
        }
        return array;
    }

    /**
     * Returns the color space in which this color value is defined.
     */
    public PDColorSpace getColorSpace()
    {
        return colorSpace;
    }

    @Override
    public String toString()
    {
        return "PDColor{components=" + Arrays.toString(components) +
                ", patternName=" + patternName + "}";
    }
}
