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
package org.apache.pdfbox.pdmodel.graphics.blend;

import org.apache.pdfbox.cos.COSName;

/**
 * Non-separable blend mode (supports blend function).
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public abstract class NonSeparableBlendMode extends BlendMode
{
    protected static final NonSeparableBlendMode HUE = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            float[] temp = new float[3];
            getSaturationRGB(dstValues, srcValues, temp);
            getLuminosityRGB(dstValues, temp, result);
        }
        
        @Override
        public COSName getCOSName() 
        {
        	return COSName.HUE;
        }
    };

    protected static final NonSeparableBlendMode SATURATION = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getSaturationRGB(srcValues, dstValues, result);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.SATURATION;
        }
    };

    protected static final NonSeparableBlendMode COLOR = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getLuminosityRGB(dstValues, srcValues, result);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.COLOR;
        }
    };

    protected static final NonSeparableBlendMode LUMINOSITY = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getLuminosityRGB(srcValues, dstValues, result);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.LUMINOSITY;
        }
    };

    NonSeparableBlendMode()
    {
    }

    public abstract void blend(float[] srcValues, float[] dstValues, float[] result);
    
    private static int get255Value(float val)
    {
        return (int) Math.floor(val >= 1.0 ? 255 : val * 255.0);
    }

    private static void getSaturationRGB(float[] srcValues, float[] dstValues, float[] result)
    {
        int rd = get255Value(dstValues[0]);
        int gd = get255Value(dstValues[1]);
        int bd = get255Value(dstValues[2]);
        int rs = get255Value(srcValues[0]);
        int gs = get255Value(srcValues[1]);
        int bs = get255Value(srcValues[2]);

        int minb = Math.min(rd, Math.min(gd, bd));
        int maxb = Math.max(rd, Math.max(gd, bd));
        if (minb == maxb)
        {
            /* backdrop has zero saturation, avoid divide by 0 */
            result[0] = gd / 255.0f;
            result[1] = gd / 255.0f;
            result[2] = gd / 255.0f;
            return;
        }

        int mins = Math.min(rs, Math.min(gs, bs));
        int maxs = Math.max(rs, Math.max(gs, bs));

        int scale = ((maxs - mins) << 16) / (maxb - minb);
        int y = (rd * 77 + gd * 151 + bd * 28 + 0x80) >> 8;
        int r = y + ((((rd - y) * scale) + 0x8000) >> 16);
        int g = y + ((((gd - y) * scale) + 0x8000) >> 16);
        int b = y + ((((bd - y) * scale) + 0x8000) >> 16);

        if (((r | g | b) & 0x100) == 0x100)
        {
            int scalemin;
            int scalemax;

            int min = Math.min(r, Math.min(g, b));
            int max = Math.max(r, Math.max(g, b));

            if (min < 0)
            {
                scalemin = (y << 16) / (y - min);
            }
            else
            {
                scalemin = 0x10000;
            }

            if (max > 255)
            {
                scalemax = ((255 - y) << 16) / (max - y);
            }
            else
            {
                scalemax = 0x10000;
            }

            scale = Math.min(scalemin, scalemax);
            r = y + (((r - y) * scale + 0x8000) >> 16);
            g = y + (((g - y) * scale + 0x8000) >> 16);
            b = y + (((b - y) * scale + 0x8000) >> 16);
        }
        result[0] = r / 255.0f;
        result[1] = g / 255.0f;
        result[2] = b / 255.0f;
    }

    private static void getLuminosityRGB(float[] srcValues, float[] dstValues, float[] result)
    {
        int rd = get255Value(dstValues[0]);
        int gd = get255Value(dstValues[1]);
        int bd = get255Value(dstValues[2]);
        int rs = get255Value(srcValues[0]);
        int gs = get255Value(srcValues[1]);
        int bs = get255Value(srcValues[2]);
        int delta = ((rs - rd) * 77 + (gs - gd) * 151 + (bs - bd) * 28 + 0x80) >> 8;
        int r = rd + delta;
        int g = gd + delta;
        int b = bd + delta;

        if (((r | g | b) & 0x100) == 0x100)
        {
            int scale;
            int y = (rs * 77 + gs * 151 + bs * 28 + 0x80) >> 8;
            if (delta > 0)
            {
                int max;
                max = Math.max(r, Math.max(g, b));
                scale = max == y ? 0 : ((255 - y) << 16) / (max - y);
            }
            else
            {
                int min;
                min = Math.min(r, Math.min(g, b));
                scale = y == min ? 0 : (y << 16) / (y - min);
            }
            r = y + (((r - y) * scale + 0x8000) >> 16);
            g = y + (((g - y) * scale + 0x8000) >> 16);
            b = y + (((b - y) * scale + 0x8000) >> 16);
        }
        result[0] = r / 255.0f;
        result[1] = g / 255.0f;
        result[2] = b / 255.0f;
    }
}
