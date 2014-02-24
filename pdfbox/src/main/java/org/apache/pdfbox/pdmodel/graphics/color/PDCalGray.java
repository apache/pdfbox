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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A CalGray colour space is a special case of a single-component CIE-based colour space.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDCalGray extends PDCalRGB
{
    private static final PDColor INITIAL_COLOR = new PDColor(new float[] { 0 });

    /**
     * Create a new CalGray color space.
     */
    public PDCalGray()
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add(COSName.CALGRAY);
        array.add(dictionary);
    }

    /**
     * Creates a new CalGray color space using the given COS array.
     * @param array the COS array which represents this color space
     */
    public PDCalGray(COSArray array)
    {
        this.array = array;
        dictionary = (COSDictionary)array.getObject(1);
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
        return INITIAL_COLOR;
    }

    @Override
    public final float[] toRGB(float[] value)
    {
        return super.toRGB(new float[] { value[0], value[0], value[0] });
    }
}
