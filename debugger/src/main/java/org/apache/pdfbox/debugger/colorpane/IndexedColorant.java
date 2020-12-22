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
package org.apache.pdfbox.debugger.colorpane;

import java.awt.Color;

/**
 * @author Khyrul Bashar.
 */

/**
 * Class to represent Colorant in Indexed color.
 */
public class IndexedColorant
{
    private int index;
    private float[] rgbValues;

    /**
     * Constructor.
     */
    public IndexedColorant(){}

    public int getIndex()
    {
        return index;
    }

    public void setIndex(final int index)
    {
        this.index = index;
    }

    public void setRgbValues(final float[] rgbValues)
    {
        this.rgbValues = rgbValues;
    }

    public Color getColor()
    {
        return new Color(rgbValues[0], rgbValues[1], rgbValues[2]);
    }

    public String getRGBValuesString()
    {
        final StringBuilder builder = new StringBuilder();
        for (final float i: rgbValues)
        {
            builder.append((int)(i*255));
            builder.append(", ");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }
}
