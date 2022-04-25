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

/**
 * Blend mode.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public abstract class BlendMode
{
	// Separable blend modes
	public static final BlendMode NORMAL = SeparableBlendMode.NORMAL;
	public static final BlendMode COMPATIBLE = SeparableBlendMode.NORMAL;
	public static final BlendMode MULTIPLY = SeparableBlendMode.MULTIPLY;
	public static final BlendMode SCREEN = SeparableBlendMode.SCREEN;
	public static final BlendMode OVERLAY = SeparableBlendMode.OVERLAY;
	public static final BlendMode DARKEN = SeparableBlendMode.DARKEN;
	public static final BlendMode LIGHTEN = SeparableBlendMode.LIGHTEN;
	public static final BlendMode COLOR_DODGE = SeparableBlendMode.COLOR_DODGE;
	public static final BlendMode COLOR_BURN = SeparableBlendMode.COLOR_BURN;
	public static final BlendMode HARD_LIGHT = SeparableBlendMode.HARD_LIGHT;
	public static final BlendMode SOFT_LIGHT = SeparableBlendMode.SOFT_LIGHT;
	public static final BlendMode DIFFERENCE = SeparableBlendMode.DIFFERENCE;
	public static final BlendMode EXCLUSION = SeparableBlendMode.EXCLUSION;

	// non-separable blend modes
	public static final BlendMode HUE = NonSeparableBlendMode.HUE;
	public static final BlendMode SATURATION = NonSeparableBlendMode.SATURATION;
	public static final BlendMode COLOR = NonSeparableBlendMode.COLOR;
	public static final BlendMode LUMINOSITY = NonSeparableBlendMode.LUMINOSITY;

    BlendMode()
    {
    }

    /**
     * The blend mode name from the BM object.
     *
     * @return name of blend mode.
     */
    public abstract COSName getCOSName();

    
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
            result = getBlendMode((COSName)cosBlendMode);
        }
        else if (cosBlendMode instanceof COSArray)
        {
            COSArray cosBlendModeArray = (COSArray) cosBlendMode;
            for (int i = 0; i < cosBlendModeArray.size(); i++)
            {
            	COSBase cosBase = cosBlendModeArray.getObject(i);
            	if (cosBase instanceof COSName)
            	{
	                result = getBlendMode((COSName)cosBase);
	                if (result != null)
	                {
	                    break;
	                }
            	}
            }
        }
        return result != null ? result : SeparableBlendMode.NORMAL;
    }
    
    private static BlendMode getBlendMode(COSName cosBlendMode)
    {
    	BlendMode result = SeparableBlendMode.getBlendMode((COSName)cosBlendMode);
        if (result == null)
        {
            result = NonSeparableBlendMode.getBlendMode((COSName)cosBlendMode);
        }
    	return result;
    }
}
