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
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;

import java.awt.Paint;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

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
    /** The color black in the DeviceGray color space. */
    public static PDColor DEVICE_GRAY_BLACK = new PDColor(new float[] { 0 });

    /** A pattern which leaves no marks on the page. */
    public static PDColor EMPTY_PATTERN = new PDColor(new float[] { }, null);

    private final float[] components;
    private final String patternName;

    /**
     * Creates a PDColor containing the given color value.
     * @param array a COS array containing the color value
     */
    public PDColor(COSArray array)
    {
        if (array.get(array.size() - 1) instanceof COSName)
        {
            // color components (optional)
            components = new float[array.size() - 1];
            for (int i = 0; i < array.size() - 1; i++)
            {
                components[i] = ((COSNumber)array.get(i)).floatValue();
            }

            // pattern name (required)
            patternName = ((COSName)array.get(array.size() - 1)).getName();
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
    }

    /**
     * Creates a PDColor containing the given color component values.
     * @param components array of color component values
     */
    public PDColor(float[] components)
    {
        this.components = components.clone();
        this.patternName = null;
    }

    /**
     * Creates a PDColor containing the given pattern name.
     * @param patternName the name of a pattern in a pattern dictionary
     */
    public PDColor(String patternName)
    {
        this.components = new float[0];
        this.patternName = patternName;
    }

    /**
     * Creates a PDColor containing the given color component values and pattern name.
     * @param components array of color component values
     * @param patternName the name of a pattern in a pattern dictionary
     */
    public PDColor(float[] components, String patternName)
    {
        this.components = components.clone();
        this.patternName = patternName;
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
    public String getPatternName()
    {
        return patternName;
    }

    /**
     * Returns this color value as a COS array
     * @return the color value as a COS array
     */
    public COSArray toCOSArray()
    {
        COSArray array = new COSArray();
        array.setFloatArray(components);
        array.add(new COSString(patternName));
        return array;
    }

    @Override
    public String toString()
    {
        return "PDColor{components=" + Arrays.toString(components) +
                ", patternName=" + patternName + "}";
    }
}
