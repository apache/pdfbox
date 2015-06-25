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

package org.apache.pdfbox.tools.pdfdebugger.flagbitspane;

/**
 * @author Khyrul Bashar
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * A class that displays flag bits found in many Flags entry in PDF document's dictionaries
 * detail whether a particular bit is set or unset.
 */
public class FlagBitsPane
{
    private final static String[] columnNames = {"Bit\nPosition", "Name", "Status"};
    private JPanel panel;

    /**
     * Constructor
     * @param dictionary COSDictionary instance that contains the flag
     * @param flagKey the flag key in the dictionary
     */
    public FlagBitsPane(COSDictionary dictionary, COSName flagKey)
    {
        initUI(dictionary, flagKey);
    }

    private void initUI(final COSDictionary dictionary, final COSName flagKey)
    {
        Object[][] flagBits = getFlagBits(dictionary, flagKey);

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 500));

        JLabel flagLabel = new JLabel(getFlagTypeString(dictionary, flagKey) + " Flags");
        flagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        JPanel flagLabelPanel = new JPanel();
        flagLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        flagLabelPanel.add(flagLabel);

        JLabel flagValueLabel = new JLabel("Flag value: " + getFlagValue(dictionary, flagKey));
        flagValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        flagValueLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));

        JTable table = new JTable(flagBits, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(flagValueLabel);
        box.add(scrollPane);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;

        panel.add(flagLabelPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.9;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;

        panel.add(box, gbc);
    }

    private String getFlagTypeString(final COSDictionary dictionary, final COSName flagKey)
    {
        if (dictionary.getCOSName(COSName.TYPE).equals(COSName.FONT_DESC) && flagKey.equals(COSName.FLAGS))
        {
            return "Font";
        }
        else if (dictionary.getCOSName(COSName.TYPE).equals(COSName.ANNOT) && flagKey.equals(COSName.F))
        {
            return "Annot:" + dictionary.getCOSName(COSName.SUBTYPE).getName();
        }

        else if (flagKey.equals(COSName.FF) && flagKey.equals(COSName.FF))
        {
            if (dictionary.getCOSName(COSName.FT).equals(COSName.TX))
            {
                return "Text field";
            }
            else if (dictionary.getCOSName(COSName.FT).equals(COSName.BTN))
            {
                return "Button field";
            }
            else if (dictionary.getCOSName(COSName.FT).equals(COSName.CH))
            {
                return "Choice field";
            }
        }
        return null;
    }

    private int getFlagValue(final COSDictionary dictionary, final COSName flagKey)
    {
        if (dictionary.getCOSName(COSName.TYPE).equals(COSName.FONT_DESC) && flagKey.equals(COSName.FLAGS))
        {
            return dictionary.getInt(COSName.FLAGS);
        }
        //TODO Type key is not Required field in the dictionary. So we need a better way to Identify.
        if (dictionary.getCOSName(COSName.TYPE).equals(COSName.ANNOT) && flagKey.equals(COSName.F))
        {
            return dictionary.getInt(COSName.F);
        }
        if (dictionary.containsKey(COSName.FT))
        {
            return dictionary.getInt(COSName.FF);
        }
        return 0;
    }

    private Object[][] getFlagBits(final COSDictionary dictionary, COSName flagKey)
    {
        if (dictionary.getCOSName(COSName.TYPE).equals(COSName.FONT_DESC) && flagKey.equals(COSName.FLAGS))
        {
            return getFontFlagBits(dictionary);
        }
        if (dictionary.getCOSName(COSName.TYPE).equals(COSName.ANNOT) && flagKey.equals(COSName.F))
        {
            return getAnnotFlagBits(dictionary);
        }
        if (dictionary.containsKey(COSName.FT) && flagKey.equals(COSName.FF))
        {
            return getFieldFlagBits(dictionary, flagKey);
        }
        return null;
    }

    private Object[][] getAnnotFlagBits(COSDictionary dictionary)
    {
        PDAnnotation annotation = new PDAnnotation(dictionary)
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

    private Object[][] getFontFlagBits(final COSDictionary dictionary)
    {
        PDFontDescriptor fontDesc = new PDFontDescriptor(dictionary);
        return new Object[][]{
                new Object[]{1, "FixedPitch", fontDesc.isFixedPitch()},
                new Object[]{2, "Serif", fontDesc.isSerif()},
                new Object[]{3, "Symbolic", fontDesc.isSymbolic()},
                new Object[]{4, "Script", fontDesc.isScript()},
                new Object[]{6, "NonSymbolic", fontDesc.isNonSymbolic()},
                new Object[]{7, "Italic", fontDesc.isItalic()},
                new Object[]{17, "AllCap", fontDesc.isAllCap()},
                new Object[]{18, "SmallCap", fontDesc.isSmallCap()},
                new Object[]{19, "ForceBold", fontDesc.isForceBold()}
        };
    }

    private Object[][] getFieldFlagBits(final COSDictionary dictionary, final COSName flagKey)
    {
        int flagValue = getFlagValue(dictionary, flagKey);

        if (dictionary.getCOSName(COSName.FT).equals(COSName.TX))
        {
            return getTextFieldFlagBits(flagValue);
        }
        else if (dictionary.getCOSName(COSName.FT).equals(COSName.BTN))
        {
            return getButtonFieldFlagBits(flagValue);
        }
        else if (dictionary.getCOSName(COSName.FT).equals(COSName.CH))
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
    private boolean isFlagBitSet(int flagValue, int bitPosition)
    {
        int binaryFormat = 1 << (bitPosition - 1);
        return (flagValue & binaryFormat) == binaryFormat;
    }

    /**
     * @return returns the JPanel instance
     */
    public JPanel getPanel()
    {
        return panel;
    }
}
