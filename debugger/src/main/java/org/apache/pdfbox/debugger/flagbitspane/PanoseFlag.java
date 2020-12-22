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
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.font.PDPanose;
import org.apache.pdfbox.pdmodel.font.PDPanoseClassification;

/**
 * @author Khyrul Bashar
 * A class that provide Panose classification data
 */
public class PanoseFlag extends Flag
{

    private final byte[] bytes;
    private final COSString byteValue;

    /**
     * Constructor.
     * @param dictionary COSDictionary instance. style dictionary that contains panose object.
     */
    public PanoseFlag(final COSDictionary dictionary)
    {
        byteValue = (COSString)dictionary.getDictionaryObject(COSName.PANOSE);
        bytes = getPanoseBytes(dictionary);
    }


    @Override
    String getFlagType()
    {
        return "Panose classification";
    }

    @Override
    String getFlagValue()
    {
        return "Panose byte :" + byteValue.toHexString();
    }

    @Override
    Object[][] getFlagBits()
    {
        final PDPanoseClassification pc = new PDPanose(bytes).getPanose();
        return new Object[][]{
                {2, "Family Kind", pc.getFamilyKind(), getFamilyKindValue(pc.getFamilyKind())},
                {3, "Serif Style", pc.getSerifStyle(), getSerifStyleValue(pc.getSerifStyle())},
                {4, "Weight", pc.getWeight(), getWeightValue(pc.getWeight())},
                {5, "Proportion", pc.getProportion(), getProportionValue(pc.getProportion())},
                {6, "Contrast", pc.getContrast(), getContrastValue(pc.getContrast())},
                {7, "Stroke Variation", pc.getStrokeVariation(), getStrokeVariationValue(pc.getStrokeVariation())},
                {8, "Arm Style", pc.getArmStyle(), getArmStyleValue(pc.getArmStyle())},
                {9, "Letterform", pc.getLetterform(), getLetterformValue(pc.getLetterform())},
                {10, "Midline", pc.getMidline(), getMidlineValue(pc.getMidline())},
                {11, "X-height", pc.getXHeight(), getXHeightValue(pc.getXHeight())},
        };
    }

    @Override
    String[] getColumnNames()
    {
        return new String[] {"Byte Position", "Name", "Byte Value", "Value"};
    }

    private String getFamilyKindValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Latin Text",
                "Latin Hand Written",
                "Latin Decorative",
                "Latin Symbol"
        }[index];
    }

    private String getSerifStyleValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Cove",
                "Obtuse Cove",
                "Square Cove",
                "Obtuse Square Cove",
                "Square",
                "Thin",
                "Oval",
                "Exaggerated",
                "Triangle",
                "Normal Sans",
                "Obtuse Sans",
                "Perpendicular Sans",
                "Flared",
                "Rounded"
        }[index];
    }

    private String getWeightValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Very Light",
                "Light",
                "Thin",
                "Book",
                "Medium",
                "Demi",
                "Bold",
                "Heavy",
                "Black",
                "Extra Black"
        }[index];
    }

    private String getProportionValue(final int index)
    {
        return new String[]{
                "Any",
                "No fit",
                "Old Style",
                "Modern",
                "Even Width",
                "Extended",
                "Condensed",
                "Very Extended",
                "Very Condensed",
                "Monospaced"
        }[index];
    }

    private String getContrastValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "None",
                "Very Low",
                "Low",
                "Medium Low",
                "Medium",
                "Medium High",
                "High",
                "Very High"
        }[index];
    }

    private String getStrokeVariationValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "No Variation",
                "Gradual/Diagonal",
                "Gradual/Transitional",
                "Gradual/Vertical",
                "Gradual/Horizontal",
                "Rapid/Vertical",
                "Rapid/Horizontal",
                "Instant/Vertical",
                "Instant/Horizontal",
        }[index];
    }

    private String getArmStyleValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Straight Arms/Horizontal",
                "Straight Arms/Wedge",
                "Straight Arms/Vertical",
                "Straight Arms/Single Serif",
                "Straight Arms/Double Serif",
                "Non-Straight/Horizontal",
                "Non-Straight/Wedge",
                "Non-Straight/Vertical",
                "Non-Straight/Single Serif",
                "Non-Straight/Double Serif",
        }[index];
    }

    private String getLetterformValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Normal/Contact",
                "Normal/Weighted",
                "Normal/Boxed",
                "Normal/Flattened",
                "Normal/Rounded",
                "Normal/Off Center",
                "Normal/Square",
                "Oblique/Contact",
                "Oblique/Weighted",
                "Oblique/Boxed",
                "Oblique/Flattened",
                "Oblique/Rounded",
                "Oblique/Off Center",
                "Oblique/Square",
        }[index];
    }

    private String getMidlineValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Standard/Trimmed",
                "Standard/Pointed",
                "Standard/Serifed",
                "High/Trimmed",
                "High/Pointed",
                "High/Serifed",
                "Constant/Trimmed",
                "Constant/Pointed",
                "Constant/Serifed",
                "Low/Trimmed",
                "Low/Pointed",
                "Low/Serifed"
        }[index];
    }

    private String getXHeightValue(final int index)
    {
        return new String[]{
                "Any",
                "No Fit",
                "Constant/Small",
                "Constant/Standard",
                "Constant/Large",
                "Ducking/Small",
                "Ducking/Standard",
                "Ducking/Large",
        }[index];
    }

    public final byte[] getPanoseBytes(final COSDictionary style)
    {
        final COSString panose = (COSString)style.getDictionaryObject(COSName.PANOSE);
        return panose.getBytes();
    }
}
