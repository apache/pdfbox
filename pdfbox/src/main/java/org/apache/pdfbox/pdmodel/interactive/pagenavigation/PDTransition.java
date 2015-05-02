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
package org.apache.pdfbox.pdmodel.interactive.pagenavigation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDDictionaryWrapper;

/**
 * Represents a page transition as defined in paragraph 12.4.4.1 of PDF 32000-1:2008
 * 
 * @author Andrea Vacondio
 *
 */
public final class PDTransition extends PDDictionaryWrapper
{

    /**
     * creates a new transition with default "replace" style {@link PDTransitionStyle#R}
     */
    public PDTransition()
    {
        this(PDTransitionStyle.R);
    }

    /**
     * creates a new transition with the given style.
     * 
     * @param style
     */
    public PDTransition(PDTransitionStyle style)
    {
        super();
        getCOSObject().setName(COSName.TYPE, COSName.TRANS.getName());
        getCOSObject().setName(COSName.S, style.name());
    }

    /**
     * creates a new transition for an existing dictionary
     * 
     * @param dictionary
     */
    public PDTransition(COSDictionary dictionary)
    {
        super(dictionary);
    }

    /**
     * @return the style for this transition
     * @see PDTransitionStyle#valueOf(String)
     */
    public String getStyle()
    {
        return getCOSObject().getNameAsString(COSName.S, PDTransitionStyle.R.name());
    }

    /**
     * @return The dimension in which the specified transition effect shall occur or the default
     * {@link PDTransitionDimension#H} if no dimension is found.
     * @see PDTransitionDimension
     */
    public String getDimension()
    {
        return getCOSObject().getNameAsString(COSName.DM, PDTransitionDimension.H.name());
    }

    /**
     * Sets the dimension in which the specified transition effect shall occur. Only for {@link PDTransitionStyle#Split}
     * and {@link PDTransitionStyle#Blinds}.
     */
    public void setDimension(PDTransitionDimension dimension)
    {
        getCOSObject().setName(COSName.DM, dimension.name());
    }

    /**
     * @return The direction of motion for the specified transition effect or the default {@link PDTransitionMotion#I}
     * if no motion is found.
     * @see PDTransitionMotion
     */
    public String getMotion()
    {
        return getCOSObject().getNameAsString(COSName.M, PDTransitionMotion.I.name());
    }

    /**
     * Sets the direction of motion for the specified transition effect. Only for {@link PDTransitionStyle#Split},
     * {@link PDTransitionStyle#Blinds} and {@link PDTransitionStyle#Fly}.
     */
    public void setMotion(PDTransitionMotion motion)
    {
        getCOSObject().setName(COSName.M, motion.name());
    }

    /**
     * @return the direction in which the specified transition effect shall moves. It can be either a {@link COSInteger}
     * or {@link COSName#NONE}. Default to {@link COSInteger#ZERO}
     * @see PDTransitionDirection
     */
    public COSBase getDirection()
    {
        COSBase item = getCOSObject().getItem(COSName.DI);
        if (item == null)
        {
            return COSInteger.ZERO;
        }
        return item;
    }

    /**
     * Sets the direction in which the specified transition effect shall moves. Only for {@link PDTransitionStyle#Wipe},
     * {@link PDTransitionStyle#Glitter}, {@link PDTransitionStyle#Fly}, {@link PDTransitionStyle#Cover},
     * {@link PDTransitionStyle#Uncover} and {@link PDTransitionStyle#Push}.
     */
    public void setDirection(PDTransitionDirection direction)
    {
        getCOSObject().setItem(COSName.DI, direction.getCOSBase());
    }

    /**
     * @return The duration in seconds of the transition effect or the default 1 if no duration is found.
     */
    public float getDuration()
    {
        return getCOSObject().getFloat(COSName.D, 1);
    }

    /**
     * @param duration The duration of the transition effect, in seconds.
     */
    public void setDuration(float duration)
    {
        getCOSObject().setItem(COSName.D, new COSFloat(duration));
    }

    /**
     * @return The starting or ending scale at which the changes shall be drawn or the default 1 if no scale is found.
     * Only for {@link PDTransitionStyle#Fly}.
     */
    public float getFlyScale()
    {
        return getCOSObject().getFloat(COSName.SS, 1);
    }

    /**
     * @param scale The starting or ending scale at which the changes shall be drawn. Only for
     * {@link PDTransitionStyle#Fly}.
     */
    public void setFlyScale(float scale)
    {
        getCOSObject().setItem(COSName.SS, new COSFloat(scale));
    }

    /**
     * @return true if the area that shall be flown in is rectangular and opaque. Default is false. Only for
     * {@link PDTransitionStyle#Fly}.
     */
    public boolean isFlyAreaOpaque()
    {
        return getCOSObject().getBoolean(COSName.B, false);
    }

    /**
     * @param opaque If true, the area that shall be flown in is rectangular and opaque. Only for
     * {@link PDTransitionStyle#Fly}.
     */
    public void setFlyAreaOpaque(boolean opaque)
    {
        getCOSObject().setItem(COSName.B, COSBoolean.getBoolean(opaque));
    }
}
