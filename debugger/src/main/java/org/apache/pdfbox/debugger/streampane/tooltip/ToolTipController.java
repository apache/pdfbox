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

package org.apache.pdfbox.debugger.streampane.tooltip;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.pdmodel.PDResources;

interface ToolTip
{
    String getToolTipText();
}

/**
 * @author Khyrul Bashar
 * A class that provides the tooltip for an operator.
 */
public class ToolTipController
{
    private static final Log LOG = LogFactory.getLog(ToolTipController.class);

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

    static List<String> getWords(String str)
    {
        List<String> words = new ArrayList<String>();
        for (String string : str.trim().split(" "))
        {
            string = string.trim();
            if (!string.isEmpty() && !string.equals("\n"))
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
            if (word.equals(OperatorName.SET_FONT_AND_SIZE))
            {
                toolTip = new FontToolTip(resources, rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(OperatorName.STROKING_COLOR_N))
            {
                String colorSpaceName = findColorSpace(offset, OperatorName.STROKING_COLORSPACE);
                if (colorSpaceName != null)
                {
                    toolTip = new SCNToolTip(resources, colorSpaceName, rowText);
                    return toolTip.getToolTipText();
                }
            }
            else if (word.equals(OperatorName.NON_STROKING_COLOR_N))
            {
                String colorSpaceName = findColorSpace(offset, OperatorName.NON_STROKING_COLORSPACE);
                if (colorSpaceName != null)
                {
                    toolTip = new SCNToolTip(resources, colorSpaceName, rowText);
                    return toolTip.getToolTipText();
                }
            }
            else if (word.equals(OperatorName.STROKING_COLOR_RGB) || word.equals(OperatorName.NON_STROKING_RGB))
            {
                toolTip = new RGToolTip(rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(OperatorName.STROKING_COLOR_CMYK) || word.equals(OperatorName.NON_STROKING_CMYK))
            {
                toolTip = new KToolTip(rowText);
                return toolTip.getToolTipText();
            }
            else if (word.equals(OperatorName.STROKING_COLOR_GRAY) || word.equals(OperatorName.NON_STROKING_GRAY))
            {
                toolTip = new GToolTip(rowText);
                return toolTip.getToolTipText();
            }
        }
        return null;
    }

    private String findColorSpace(int offset, String colorSpaceType)
    {
        try
        {
            while (offset != -1)
            {
                offset = Utilities.getPositionAbove(textComponent, offset, 0);
                String previousRowText = getRowText(offset);
                if (previousRowText == null)
                {
                    return null;
                }
                previousRowText = previousRowText.trim();
                if (isColorSpace(colorSpaceType, previousRowText))
                {
                    return previousRowText.split(" ")[0];
                }
            }
        }
        catch (BadLocationException e)
        {
            LOG.error(e, e);
        }
        return null;
    }

    private boolean isColorSpace(String colorSpaceType, String rowText)
    {
        List<String> words = getWords(rowText);
        return words.size() == 2 && words.get(1).equals(colorSpaceType);
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
            LOG.error(e, e);
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
            LOG.error(e, e);
        }
        return null;
    }
}
