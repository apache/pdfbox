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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

import java.util.HashMap;
import java.util.Map;

/**
 * Blend mode.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public abstract class BlendMode
{
    public static final SeparableBlendMode NORMAL = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue;
        }
    };

    public static final SeparableBlendMode COMPATIBLE = NORMAL;

    public static final SeparableBlendMode MULTIPLY = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue * dstValue;
        }
    };

    public static final SeparableBlendMode SCREEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue + dstValue - srcValue * dstValue;
        }
    };

    public static final SeparableBlendMode OVERLAY = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return (dstValue <= 0.5) ? 2 * dstValue * srcValue : 2 * (srcValue + dstValue - srcValue
                    * dstValue) - 1;
        }
    };

    public static final SeparableBlendMode DARKEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.min(srcValue, dstValue);
        }
    };

    public static final SeparableBlendMode LIGHTEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.max(srcValue, dstValue);
        }
    };

    public static final SeparableBlendMode COLOR_DODGE = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            // See PDF 2.0 specification
            if (Float.compare(dstValue,0) == 0)
            {
                return 0;
            }
            if (dstValue >= 1 - srcValue)
            {
                return 1;
            }
            return dstValue / (1 - srcValue);
        }
    };

    public static final SeparableBlendMode COLOR_BURN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            // See PDF 2.0 specification
            if (Float.compare(dstValue, 1) == 0)
            {
                return 1;
            }
            if (1 - dstValue >= srcValue)
            {
                return 0;
            }
            return 1 - (1 - dstValue) / srcValue;
        }
    };

    public static final SeparableBlendMode HARD_LIGHT = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return (srcValue <= 0.5) ? 2 * dstValue * srcValue :
                    2 * (srcValue + dstValue - srcValue * dstValue) - 1;
        }
    };

    public static final SeparableBlendMode SOFT_LIGHT = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            if (srcValue <= 0.5)
            {
                return dstValue - (1 - 2 * srcValue) * dstValue * (1 - dstValue);
            }
            else
            {
                float d = (dstValue <= 0.25) ? ((16 * dstValue - 12) * dstValue + 4) * dstValue
                        : (float) Math .sqrt(dstValue);
                return dstValue + (2 * srcValue - 1) * (d - dstValue);
            }
        }
    };

    public static final SeparableBlendMode DIFFERENCE = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.abs(dstValue - srcValue);
        }
    };

    public static final SeparableBlendMode EXCLUSION = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return dstValue + srcValue - 2 * dstValue * srcValue;
        }
    };

    public static final NonSeparableBlendMode HUE = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            float[] temp = new float[3];
            getSaturationRGB(dstValues, srcValues, temp);
            getLuminosityRGB(dstValues, temp, result);
        }
    };

    public static final NonSeparableBlendMode SATURATION = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getSaturationRGB(srcValues, dstValues, result);
        }
    };

    public static final NonSeparableBlendMode COLOR = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getLuminosityRGB(dstValues, srcValues, result);
        }
    };

    public static final NonSeparableBlendMode LUMINOSITY = new NonSeparableBlendMode()
    {
        @Override
        public void blend(float[] srcValues, float[] dstValues, float[] result)
        {
            getLuminosityRGB(srcValues, dstValues, result);
        }
    };

    // these maps *must* come after the BlendMode.* constant declarations, otherwise their values would be null
    private static final Map<COSName, BlendMode> BLEND_MODES = createBlendModeMap();
    private static final Map<BlendMode, COSName> BLEND_MODE_NAMES = createBlendModeNamesMap();

    BlendMode()
    {
    }

    /**
     * Determines the blend mode from the BM entry in the COS ExtGState.
     *
     * @param cosBlendMode name or array
     * @return blending mode
     */
    public static BlendMode getInstance(COSBase cosBlendMode)
    {
        BlendMode result = null;
        if (cosBlendMode instanceof COSName)
        {
            result = BLEND_MODES.get(cosBlendMode);
        }
        else if (cosBlendMode instanceof COSArray)
        {
            COSArray cosBlendModeArray = (COSArray) cosBlendMode;
            for (int i = 0; i < cosBlendModeArray.size(); i++)
            {
                result = BLEND_MODES.get(cosBlendModeArray.getObject(i));
                if (result != null)
                {
                    break;
                }
            }
        }

        if (result != null)
        {
            return result;
        }
        return BlendMode.NORMAL;
    }
    
    /**
     * Determines the blend mode name from the BM object.
     *
     * @param bm Blend mode.
     * @return name of blend mode.
     */
    public static COSName getCOSName(BlendMode bm)
    {
        return BLEND_MODE_NAMES.get(bm);
    }

    private static int get255Value(float val)
    {
        return (int) Math.floor(val >= 1.0 ? 255 : val * 255.0);
    }

    private static void getSaturationRGB(float[] srcValues, float[] dstValues, float[] result)
    {
        int minb;
        int maxb;
        int mins;
        int maxs;
        int y;
        int scale;
        int r;
        int g;
        int b;

        int rd = get255Value(dstValues[0]);
        int gd = get255Value(dstValues[1]);
        int bd = get255Value(dstValues[2]);
        int rs = get255Value(srcValues[0]);
        int gs = get255Value(srcValues[1]);
        int bs = get255Value(srcValues[2]);

        minb = Math.min(rd, Math.min(gd, bd));
        maxb = Math.max(rd, Math.max(gd, bd));
        if (minb == maxb)
        {
            /* backdrop has zero saturation, avoid divide by 0 */
            result[0] = gd / 255.0f;
            result[1] = gd / 255.0f;
            result[2] = gd / 255.0f;
            return;
        }

        mins = Math.min(rs, Math.min(gs, bs));
        maxs = Math.max(rs, Math.max(gs, bs));

        scale = ((maxs - mins) << 16) / (maxb - minb);
        y = (rd * 77 + gd * 151 + bd * 28 + 0x80) >> 8;
        r = y + ((((rd - y) * scale) + 0x8000) >> 16);
        g = y + ((((gd - y) * scale) + 0x8000) >> 16);
        b = y + ((((bd - y) * scale) + 0x8000) >> 16);

        if (((r | g | b) & 0x100) == 0x100)
        {
            int scalemin;
            int scalemax;
            int min;
            int max;

            min = Math.min(r, Math.min(g, b));
            max = Math.max(r, Math.max(g, b));

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
        int delta;
        int scale;
        int r;
        int g;
        int b;
        int y;
        int rd = get255Value(dstValues[0]);
        int gd = get255Value(dstValues[1]);
        int bd = get255Value(dstValues[2]);
        int rs = get255Value(srcValues[0]);
        int gs = get255Value(srcValues[1]);
        int bs = get255Value(srcValues[2]);
        delta = ((rs - rd) * 77 + (gs - gd) * 151 + (bs - bd) * 28 + 0x80) >> 8;
        r = rd + delta;
        g = gd + delta;
        b = bd + delta;

        if (((r | g | b) & 0x100) == 0x100)
        {
            y = (rs * 77 + gs * 151 + bs * 28 + 0x80) >> 8;
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
    
    private static Map<COSName, BlendMode> createBlendModeMap()
    {
        Map<COSName, BlendMode> map = new HashMap<>(13);
        map.put(COSName.NORMAL, BlendMode.NORMAL);
        // BlendMode.COMPATIBLE should not be used
        map.put(COSName.COMPATIBLE, BlendMode.NORMAL);
        map.put(COSName.MULTIPLY, BlendMode.MULTIPLY);
        map.put(COSName.SCREEN, BlendMode.SCREEN);
        map.put(COSName.OVERLAY, BlendMode.OVERLAY);
        map.put(COSName.DARKEN, BlendMode.DARKEN);
        map.put(COSName.LIGHTEN, BlendMode.LIGHTEN);
        map.put(COSName.COLOR_DODGE, BlendMode.COLOR_DODGE);
        map.put(COSName.COLOR_BURN, BlendMode.COLOR_BURN);
        map.put(COSName.HARD_LIGHT, BlendMode.HARD_LIGHT);
        map.put(COSName.SOFT_LIGHT, BlendMode.SOFT_LIGHT);
        map.put(COSName.DIFFERENCE, BlendMode.DIFFERENCE);
        map.put(COSName.EXCLUSION, BlendMode.EXCLUSION);
        map.put(COSName.HUE, BlendMode.HUE);
        map.put(COSName.SATURATION, BlendMode.SATURATION);
        map.put(COSName.LUMINOSITY, BlendMode.LUMINOSITY);
        map.put(COSName.COLOR, BlendMode.COLOR);
        return map;
    }

    private static Map<BlendMode, COSName> createBlendModeNamesMap()
    {
        Map<BlendMode, COSName> map = new HashMap<>(13);
        map.put(BlendMode.NORMAL, COSName.NORMAL);
        // BlendMode.COMPATIBLE should not be used
        map.put(BlendMode.COMPATIBLE, COSName.NORMAL);
        map.put(BlendMode.MULTIPLY, COSName.MULTIPLY);
        map.put(BlendMode.SCREEN, COSName.SCREEN);
        map.put(BlendMode.OVERLAY, COSName.OVERLAY);
        map.put(BlendMode.DARKEN, COSName.DARKEN);
        map.put(BlendMode.LIGHTEN, COSName.LIGHTEN);
        map.put(BlendMode.COLOR_DODGE, COSName.COLOR_DODGE);
        map.put(BlendMode.COLOR_BURN, COSName.COLOR_BURN);
        map.put(BlendMode.HARD_LIGHT, COSName.HARD_LIGHT);
        map.put(BlendMode.SOFT_LIGHT, COSName.SOFT_LIGHT);
        map.put(BlendMode.DIFFERENCE, COSName.DIFFERENCE);
        map.put(BlendMode.EXCLUSION, COSName.EXCLUSION);
        map.put(BlendMode.HUE, COSName.HUE);
        map.put(BlendMode.SATURATION, COSName.SATURATION);
        map.put(BlendMode.LUMINOSITY, COSName.LUMINOSITY);
        map.put(BlendMode.COLOR, COSName.COLOR);
        return map;
    }
}
