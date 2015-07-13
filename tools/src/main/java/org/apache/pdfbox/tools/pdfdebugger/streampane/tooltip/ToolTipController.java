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

package org.apache.pdfbox.tools.pdfdebugger.streampane.tooltip;

import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import org.apache.pdfbox.pdmodel.PDResources;

interface ToolTip
{
    String getToolTipText();
}

/**
 * @author Khyrul Bashar
 * A class that provieds the tooltip for an operator.
 */
public class ToolTipController
{
    private final static String FONT_OPERATOR = "Tf";
    private final static String STROKING_COLOR = "SCN";
    private final static String STROKING_COLOR_SPACE = "CS";
    private final static String NON_STROKING_COLOR_SPACE = "cs";
    private final static String NON_STROKING_COLOR = "scn";
    private final static String RGB_STROKING_COLOR = "RG";
    private final static String RGB_NON_STROKING_COLOR = "rg";
    private final static String CMYK_STROKING_COLOR = "K";
    private final static String CMYK_NON_STROKING_COLOR = "k";
    private final static String GRAY_STROKING_COLOR = "G";
    private final static String GRAY_NON_STROKING_COLOR = "g";

    private final PDResources resources;
    private JTextComponent textComponent;

    /**
     * Constructor.
     * @param resources PDResources instance.
     */
    public ToolTipController(PDResources resources)
    {
        this.resources = resources;
    }

    static ArrayList<String> getWords(String str)
    {
        ArrayList<String> words = new ArrayList<String>();
        for (String string : str.trim().split(" "))
        {
            string = string.trim();
            if (!string.equals("") && !string.equals("\n"))
            {
                words.add(string);
            }
        }
        return words;
    }

    /**
     * Returns the tooltip text for the operator. null if there isn't any tooltip.
     * @param offset The position of the mouse in the text component.
     * @param textComponent JTextComponent instance.
     * @return Tooltip text, String instance.
     */
    public String getToolTip(int offset, JTextComponent textComponent)
    {
        this.textComponent = textComponent;

        String word = getWord(offset);
        String rowText = getRowText(offset);

        if (word != null)
        {
            ToolTip toolTip;
            if (word.equals(FONT_OPERATOR))
            {
                toolTip = new FontToolTip(resources, rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(STROKING_COLOR))
            {
                String colorSpaceName = findColorSapce(offset, STROKING_COLOR_SPACE);
                if (colorSpaceName != null)
                {
                    toolTip = new SCNToolTip(resources, colorSpaceName, rowText);
                    return toolTip.getToolTipText();
                }
            }
            else if (word.equals(NON_STROKING_COLOR))
            {
                String colorSpaceName = findColorSapce(offset, NON_STROKING_COLOR_SPACE);
                if (colorSpaceName != null)
                {
                    toolTip = new SCNToolTip(resources, colorSpaceName, rowText);
                    return toolTip.getToolTipText();
                }
            }
            else if (word.equals(RGB_STROKING_COLOR) || word.equals(RGB_NON_STROKING_COLOR))
            {
                toolTip = new RGToolTip(rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(CMYK_STROKING_COLOR) || word.equals(CMYK_NON_STROKING_COLOR))
            {
                toolTip = new KToolTip(rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(GRAY_STROKING_COLOR) || word.equals(GRAY_NON_STROKING_COLOR))
            {
                toolTip = new GToolTip(rowText);
                return toolTip.getToolTipText();
            }
        }
        return null;
    }

    private String findColorSapce(int offset, String colorSpaceType)
    {
        try
        {
            while (offset != -1)
            {
                offset = Utilities.getPositionAbove(textComponent, offset, 0);
                String previousRowText = getRowText(offset);
                if (isColorSpace(colorSpaceType, previousRowText))
                {
                    if (previousRowText != null)
                    {
                        return previousRowText.split(" ")[0];
                    }
                }
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private boolean isColorSpace(String colorSpaceType, String rowText)
    {
        ArrayList<String> words = getWords(rowText);
        if (words.size() == 2)
        {
            if (words.get(1).equals(colorSpaceType))
            {
                return true;
            }
        }
        return false;
    }

    private String getWord(int offset)
    {
        try
        {
            int start = Utilities.getWordStart(textComponent, offset);
            int end = Utilities.getWordEnd(textComponent, offset);
            return textComponent.getDocument().getText(start, end - start + 1).trim();
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String getRowText(int offset)
    {
        try
        {
            int rowStart = Utilities.getRowStart(textComponent, offset);
            int rowEnd = Utilities.getRowEnd(textComponent, offset);
            return textComponent.getDocument().getText(rowStart, rowEnd - rowStart + 1);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
