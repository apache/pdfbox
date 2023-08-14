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

import java.awt.Color;
import java.util.List;

/**
 * @author Khyrul Bashar
 * An abstract class for tooltips of color operators.
 */
abstract class ColorToolTip implements ToolTip
{
    private String toolTipText;
    /**
     * provides the Hex value for a Color instance.
     * @param color
     * @return
     */
    static String colorHexValue(Color color)
    {
        return String.format("%02x", color.getRed()) + String.format("%02x", color.getGreen()) +
                String.format("%02x", color.getBlue());
    }

    /**
     * Extract Color values from the row for which tooltip is going to be shown.
     * @param rowtext String instance,
     * @return float array containing color values.
     */
    float[] extractColorValues(String rowtext)
    {
        List<String> words = ToolTipController.getWords(rowtext);
        words.remove(words.size()-1);
        float[] values = new float[words.size()];
        int index = 0;
        try
        {
            for (String word : words)
            {
                values[index++] = Float.parseFloat(word);
            }
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        return values;
    }

    /**
     * Create a html string that actually shows a colored rect.
     * @param hexValue
     * @return String instance, In html format.
     */
    String getMarkUp(String hexValue)
    {
         return  "<html>\n" +
                "<body bgcolor=#ffffff>\n" +
                "<div style=\"width:50px;height:20px;border:1px; background-color:#"+hexValue+";\"></div></body>\n" +
                "</html>";
    }

    public void setToolTipText(String toolTip)
    {
        this.toolTipText = toolTip;
    }

    @Override
    public String getToolTipText()
    {
        return toolTipText;
    }
}
