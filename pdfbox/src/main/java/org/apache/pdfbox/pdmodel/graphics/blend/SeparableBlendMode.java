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
 * Separable blend mode (support blendChannel)
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public abstract class SeparableBlendMode extends BlendMode
{
	protected static final SeparableBlendMode NORMAL = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.NORMAL;
        }
    };
	
    protected static final SeparableBlendMode MULTIPLY = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue * dstValue;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.MULTIPLY;
        }
    };

    protected static final SeparableBlendMode SCREEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return srcValue + dstValue - srcValue * dstValue;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.SCREEN;
        }
    };

    protected static final SeparableBlendMode OVERLAY = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return (dstValue <= 0.5) ? 2 * dstValue * srcValue : 2 * (srcValue + dstValue - srcValue
                    * dstValue) - 1;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.OVERLAY;
        }
    };

    protected static final SeparableBlendMode DARKEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.min(srcValue, dstValue);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.DARKEN;
        }
    };

    protected static final SeparableBlendMode LIGHTEN = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.max(srcValue, dstValue);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.LIGHTEN;
        }
    };

    protected static final SeparableBlendMode COLOR_DODGE = new SeparableBlendMode()
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

        @Override
        public COSName getCOSName() 
        {
        	return COSName.COLOR_DODGE;
        }
    };

    protected static final SeparableBlendMode COLOR_BURN = new SeparableBlendMode()
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

        @Override
        public COSName getCOSName() 
        {
        	return COSName.COLOR_BURN;
        }
    };

    protected static final SeparableBlendMode HARD_LIGHT = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return (srcValue <= 0.5) ? 2 * dstValue * srcValue :
                    2 * (srcValue + dstValue - srcValue * dstValue) - 1;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.HARD_LIGHT;
        }
    };

    protected static final SeparableBlendMode SOFT_LIGHT = new SeparableBlendMode()
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

        @Override
        public COSName getCOSName() 
        {
        	return COSName.SOFT_LIGHT;
        }
    };

    protected static final SeparableBlendMode DIFFERENCE = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return Math.abs(dstValue - srcValue);
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.DIFFERENCE;
        }
    };

    protected static final SeparableBlendMode EXCLUSION = new SeparableBlendMode()
    {
        @Override
        public float blendChannel(float srcValue, float dstValue)
        {
            return dstValue + srcValue - 2 * dstValue * srcValue;
        }

        @Override
        public COSName getCOSName() 
        {
        	return COSName.EXCLUSION;
        }
    };


    SeparableBlendMode()
    {
    }

    public abstract float blendChannel(float srcValue, float dstValue);
}
