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

/**
 * @author Khyrul Bashar
 * A class that provides field flag bits.
 */
class FieldFlag extends Flag
{
    private final COSDictionary dictionary;

    /**
     * Constructor
     * @param dictionary COSDictionary instance
     */
    FieldFlag(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    @Override
    String getFlagType()
    {
        COSName fieldType = dictionary.getCOSName(COSName.FT);
        if (COSName.TX.equals(fieldType))
        {
            return "Text field flag";
        }
        else if (COSName.BTN.equals(fieldType))
        {
            return "Button field flag";
        }
        else if (COSName.CH.equals(fieldType))
        {
            return "Choice field flag";
        }
        return null;
    }

    @Override
    String getFlagValue()
    {
        return "Flag value: " + dictionary.getInt(COSName.FF);
    }

    @Override
    Object[][] getFlagBits()
    {
        int flagValue = dictionary.getInt(COSName.FF);
        COSName fieldType = dictionary.getCOSName(COSName.FT);

        if (COSName.TX.equals(fieldType))
        {
            return getTextFieldFlagBits(flagValue);
        }
        else if (COSName.BTN.equals(fieldType))
        {
            return getButtonFieldFlagBits(flagValue);
        }
        else if (COSName.CH.equals(fieldType))
        {
            return getChoiceFieldFlagBits(flagValue);
        }
        return null;
    }

    private Object[][] getTextFieldFlagBits(final int flagValue)
    {
        return new Object[][]{
                new Object[]{1, "ReadOnly", isFlagBitSet(flagValue, 1)},
                new Object[]{2, "Required", isFlagBitSet(flagValue, 2)},
                new Object[]{3, "NoExport", isFlagBitSet(flagValue, 3)},
                new Object[]{13, "Multiline", isFlagBitSet(flagValue, 13)},
                new Object[]{14, "Password", isFlagBitSet(flagValue, 14)},
                new Object[]{21, "FileSelect", isFlagBitSet(flagValue, 21)},
                new Object[]{23, "DoNotSpellCheck", isFlagBitSet(flagValue, 23)},
                new Object[]{24, "DoNotScroll", isFlagBitSet(flagValue, 24)},
                new Object[]{25, "Comb", isFlagBitSet(flagValue, 25)},
                new Object[]{26, "RichText", isFlagBitSet(flagValue, 26)}
        };
    }

    private Object[][] getButtonFieldFlagBits(final int flagValue)
    {
        return new Object[][]{
                new Object[]{1, "ReadOnly", isFlagBitSet(flagValue, 1)},
                new Object[]{2, "Required", isFlagBitSet(flagValue, 2)},
                new Object[]{3, "NoExport", isFlagBitSet(flagValue, 3)},
                new Object[]{15, "NoToggleToOff", isFlagBitSet(flagValue, 15)},
                new Object[]{16, "Radio", isFlagBitSet(flagValue, 16)},
                new Object[]{17, "Pushbutton", isFlagBitSet(flagValue, 17)},
                new Object[]{26, "RadiosInUnison", isFlagBitSet(flagValue, 26)}
        };
    }

    private Object[][] getChoiceFieldFlagBits(final int flagValue)
    {
        return new Object[][]{
                new Object[]{1, "ReadOnly", isFlagBitSet(flagValue, 1)},
                new Object[]{2, "Required", isFlagBitSet(flagValue, 2)},
                new Object[]{3, "NoExport", isFlagBitSet(flagValue, 3)},
                new Object[]{18, "Combo", isFlagBitSet(flagValue, 18)},
                new Object[]{19, "Edit", isFlagBitSet(flagValue, 19)},
                new Object[]{20, "Sort", isFlagBitSet(flagValue, 20)},
                new Object[]{22, "MultiSelect", isFlagBitSet(flagValue, 22)},
                new Object[]{23, "DoNotSpellCheck", isFlagBitSet(flagValue, 23)},
                new Object[]{27, "CommitOnSelChange", isFlagBitSet(flagValue, 27)}
        };
    }

    /**
     * Check the corresponding flag bit if set or not
     * @param flagValue the flag integer
     * @param bitPosition bit position to check
     * @return if set return true else false
     */
    private Boolean isFlagBitSet(int flagValue, int bitPosition)
    {
        int binaryFormat = 1 << (bitPosition - 1);
        return (flagValue & binaryFormat) == binaryFormat;
    }
}
