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

import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

/**
 * Blend mode.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public class BlendMode
{
    @FunctionalInterface
    public interface BlendChannelFunction
    {
        /**
         * BlendChannel function for separable blend modes.
         *
         * @param src the source value
         * @param dest the destination value
         * @return the function result
         */
        float blendChannel(float src, float dest);
    }

    @FunctionalInterface
    public interface BlendFunction
    {
        /**
         * Blend function for non separable blend modes.
         *
         * @param src the source values
         * @param dest the destination values
         * @param result the function result values
         */
        void blend(float[] src, float[] dest, float[] result);
    }

    /**
     * Functions for the blend operation of separable blend modes
     */
    private static final BlendChannelFunction fNormal = (src, dest) -> src;

    private static final BlendChannelFunction fMultiply = (src, dest) -> src * dest;

    private static final BlendChannelFunction fScreen = (src, dest) -> src + dest - src * dest;

    private static final BlendChannelFunction fOverlay = (src, dest) -> (dest <= 0.5) ? 2 * dest * src
            : 2 * (src + dest - src * dest) - 1;

    private static final BlendChannelFunction fDarken = Math::min;

    private static final BlendChannelFunction fLighten = Math::max;

    private static final BlendChannelFunction fColorDodge = (src, dest) -> {
        // See PDF 2.0 specification
        if (Float.compare(dest, 0) == 0)
        {
            return 0f;
        }
        if (dest >= 1 - src)
        {
            return 1f;
        }
        return dest / (1 - src);
    };

    private static final BlendChannelFunction fColorBurn = (src, dest) -> {
        // See PDF 2.0 specification
        if (Float.compare(dest, 1) == 0)
        {
            return 1f;
        }
        if (1 - dest >= src)
        {
            return 0f;
        }
        return 1 - (1 - dest) / src;
    };

    private static final BlendChannelFunction fHardLight = (src, dest) -> (src <= 0.5) ? 2 * dest * src
            : 2 * (src + dest - src * dest) - 1;

    private static final BlendChannelFunction fSoftLight = (src, dest) -> {
        if (src <= 0.5)
        {
            return dest - (1 - 2 * src) * dest * (1 - dest);
        }
        else
        {
            float d = (dest <= 0.25) ? ((16 * dest - 12) * dest + 4) * dest
                    : (float) Math.sqrt(dest);
            return dest + (2 * src - 1) * (d - dest);
        }
    };

    private static final BlendChannelFunction fDifference = (src, dest) -> Math.abs(dest - src);

    private static final BlendChannelFunction fExclusion = (src, dest) -> dest + src - 2 * dest * src;

    /**
     * Functions for the blend operation of non-separable blend modes
     */
    private static final BlendFunction fHue = (src, dest, result) -> {
        float[] temp = new float[3];
        getSaturationRGB(dest, src, temp);
        getLuminosityRGB(dest, temp, result);
    };

    private static final BlendFunction fSaturation = BlendMode::getSaturationRGB;

    private static final BlendFunction fColor = (src, dest, result) -> getLuminosityRGB(dest, src,
            result);

    private static final BlendFunction fLuminosity = BlendMode::getLuminosityRGB;

    /**
     * Separable blend modes as defined in the PDF specification
     */
    public static final BlendMode NORMAL = new BlendMode(COSName.NORMAL, fNormal, null);
    public static final BlendMode COMPATIBLE = BlendMode.NORMAL;
    public static final BlendMode MULTIPLY = new BlendMode(COSName.MULTIPLY, fMultiply, null);
    public static final BlendMode SCREEN = new BlendMode(COSName.SCREEN, fScreen, null);
    public static final BlendMode OVERLAY = new BlendMode(COSName.OVERLAY, fOverlay, null);
    public static final BlendMode DARKEN = new BlendMode(COSName.DARKEN, fDarken, null);
    public static final BlendMode LIGHTEN = new BlendMode(COSName.LIGHTEN, fLighten, null);
    public static final BlendMode COLOR_DODGE = new BlendMode(COSName.COLOR_DODGE, fColorDodge,
            null);
    public static final BlendMode COLOR_BURN = new BlendMode(COSName.COLOR_BURN, fColorBurn, null);
    public static final BlendMode HARD_LIGHT = new BlendMode(COSName.HARD_LIGHT, fHardLight, null);
    public static final BlendMode SOFT_LIGHT = new BlendMode(COSName.SOFT_LIGHT, fSoftLight, null);
    public static final BlendMode DIFFERENCE = new BlendMode(COSName.DIFFERENCE, fDifference, null);
    public static final BlendMode EXCLUSION = new BlendMode(COSName.EXCLUSION, fExclusion, null);

    /**
     * Non-separable blend modes as defined in the PDF specification
     */
    public static final BlendMode HUE = new BlendMode(COSName.HUE, null, fHue);
    public static final BlendMode SATURATION = new BlendMode(COSName.SATURATION, null, fSaturation);
    public static final BlendMode COLOR = new BlendMode(COSName.COLOR, null, fColor);
    public static final BlendMode LUMINOSITY = new BlendMode(COSName.LUMINOSITY, null, fLuminosity);

    private static final Map<COSName, BlendMode> BLEND_MODES = createBlendModeMap();

    private static Map<COSName, BlendMode> createBlendModeMap()
    {
        Map<COSName, BlendMode> map = new HashMap<>(13);
        map.put(COSName.NORMAL, NORMAL);
        // BlendMode.COMPATIBLE should not be used
        map.put(COSName.COMPATIBLE, NORMAL);
        map.put(COSName.MULTIPLY, MULTIPLY);
        map.put(COSName.SCREEN, SCREEN);
        map.put(COSName.OVERLAY, OVERLAY);
        map.put(COSName.DARKEN, DARKEN);
        map.put(COSName.LIGHTEN, LIGHTEN);
        map.put(COSName.COLOR_DODGE, COLOR_DODGE);
        map.put(COSName.COLOR_BURN, COLOR_BURN);
        map.put(COSName.HARD_LIGHT, HARD_LIGHT);
        map.put(COSName.SOFT_LIGHT, SOFT_LIGHT);
        map.put(COSName.DIFFERENCE, DIFFERENCE);
        map.put(COSName.EXCLUSION, EXCLUSION);
        map.put(COSName.HUE, HUE);
        map.put(COSName.SATURATION, SATURATION);
        map.put(COSName.LUMINOSITY, LUMINOSITY);
        map.put(COSName.COLOR, COLOR);
        return map;
    }

    private final COSName name;
    private final BlendChannelFunction blendChannel;
    private final BlendFunction blend;
    private final boolean isSeparable;

    /**
     * Private constructor due to the limited set of possible blend modes.
     * 
     * @param name the corresponding COSName of the blend mode
     * @param blendChannel the blend function for separable blend modes
     * @param blend the blend function for non-separable blend modes
     */
    private BlendMode(COSName name, BlendChannelFunction blendChannel, BlendFunction blend)
    {
        this.name = name;
        this.blendChannel = blendChannel;
        this.blend = blend;
        isSeparable = blendChannel != null;
    }

    /**
     * The blend mode name from the BM object.
     *
     * @return name of blend mode.
     */
    public COSName getCOSName()
    {
        return name;
    }

    /**
     * Determines if the blend mode is a separable blend mode.
     * 
     * @return true for separable blend modes
     */
    public boolean isSeparableBlendMode()
    {
        return isSeparable;
    }
    
    /**
     * Returns the blend channel function, only available for separable blend modes.
     * 
     * @return the blend channel function
     */
    public BlendChannelFunction getBlendChannelFunction()
    {
        return blendChannel;
    }

    /**
     * Returns the blend function, only available for non separable blend modes.
     * 
     * @return the blend function
     */
    public BlendFunction getBlendFunction()
    {
        return blend;
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
                COSBase cosBase = cosBlendModeArray.getObject(i);
                if (cosBase instanceof COSName)
                {
                    result = BLEND_MODES.get(cosBase);
                    if (result != null)
                    {
                        break;
                    }
                }
            }
        }
        return result != null ? result : BlendMode.NORMAL;
    }

    private static int get255Value(float val)
    {
        return (int) Math.floor(val >= 1.0 ? 255 : val * 255.0);
    }

    private static void getSaturationRGB(float[] srcValues, float[] dstValues, float[] result)
    {
        int rd = get255Value(dstValues[0]);
        int gd = get255Value(dstValues[1]);
        int bd = get255Value(dstValues[2]);

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

        int rs = get255Value(srcValues[0]);
        int gs = get255Value(srcValues[1]);
        int bs = get255Value(srcValues[2]);

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
