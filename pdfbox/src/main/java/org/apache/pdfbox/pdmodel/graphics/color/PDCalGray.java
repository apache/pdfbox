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

import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;

/**
 * A CalGray colour space is a special case of a single-component CIE-based
 * colour space.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDCalGray extends PDCIEDictionaryBasedColorSpace
{
    private final PDColor initialColor = new PDColor(new float[] { 0 }, this);
    
    // PDFBOX-4119: cache the results for much improved performance
    // cached values MUST be cloned, because they are modified by the caller.
    // this can be observed in rendering of PDFBOX-1724
    private final Map<Float, float[]> map1 = new HashMap<>();

    /**
     * Create a new CalGray color space.
     */
    public PDCalGray()
    {
        super(COSName.CALGRAY);
    }

    /**
     * Creates a new CalGray color space using the given COS array.
     *
     * @param array the COS array which represents this color space
     */
    public PDCalGray(COSArray array)
    {
        super(array);
    }

    @Override
    public String getName()
    {
        return COSName.CALGRAY.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 1;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        // see implementation of toRGB in PDCalRGB, and PDFBOX-2971
        if (isWhitePoint())
        {
            float a = value[0];
            float[] result = map1.get(a);
            if (result != null)
            {
                return result.clone();
            }
            float gamma = getGamma();
            float powAG = (float) Math.pow(a, gamma);
            result = convXYZtoRGB(powAG, powAG, powAG);
            map1.put(a, result.clone());
            return result;
        }
        else
        {
            return new float[] { value[0], value[0], value[0] };
        }
    }

    /**
     * This will get the gamma value. If none is present then the default of 1
     * will be returned.
     *
     * @return The gamma value.
     */
    public float getGamma()
    {
        return dictionary.getFloat(COSName.GAMMA, 1.0f);
    }

    /**
     * Set the gamma value.
     *
     * @param value The new gamma value.
     */
    public void setGamma(float value)
    {
        dictionary.setItem(COSName.GAMMA, new COSFloat(value));
    }
}
