/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.pdfbox.debugger.flagbitspane;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * @author Khyrul Bashar
 *
 * A class that provides Annotation flag bits.
 */
class AnnotFlag extends Flag
{
    private final COSDictionary annotDictionary;

    /**
     * Constructor
     * @param annotDictionary COSDictionary instance
     */
    AnnotFlag(COSDictionary annotDictionary)
    {
        this.annotDictionary = annotDictionary;
    }

    @Override
    String getFlagType()
    {
        return "Annot flag";
    }

    @Override
    String getFlagValue()
    {
        return "Flag value: " + annotDictionary.getInt(COSName.F);
    }

    @Override
    Object[][] getFlagBits()
    {
        PDAnnotation annotation = new PDAnnotation(annotDictionary)
        {
        };
        return new Object[][]{
                new Object[]{1, "Invisible", annotation.isInvisible()},
                new Object[]{2, "Hidden", annotation.isHidden()},
                new Object[]{3, "Print", annotation.isPrinted()},
                new Object[]{4, "NoZoom", annotation.isNoZoom()},
                new Object[]{5, "NoRotate", annotation.isNoRotate()},
                new Object[]{6, "NoView", annotation.isNoView()},
                new Object[]{7, "ReadOnly", annotation.isReadOnly()},
                new Object[]{8, "Locked", annotation.isLocked()},
                new Object[]{9, "ToggleNoView", annotation.isToggleNoView()},
                new Object[]{10, "LockedContents", annotation.isLocked()}
        };
    }
}
