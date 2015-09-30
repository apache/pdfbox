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

package org.apache.pdfbox.debugger.streampane;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * @author Khyrul Bashar
 */
final class OperatorMarker
{
    public static final String BEGIN_TEXT_OBJECT = "BT";
    public static final String END_TEXT_OBJECT = "ET";
    public static final String SAVE_GRAPHICS_STATE = "q";
    public static final String RESTORE_GRAPHICS_STATE = "Q";
    public static final String CONCAT = "cm";
    public static final String INLINE_IMAGE_BEGIN = "BI";
    public static final String IMAGE_DATA = "ID";
    public static final String INLINE_IMAGE_END = "EI";

    private static final Map<String, Style> operatorStyleMap;

    static
    {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();

        Style common = styleContext.addStyle("common", null);
        StyleConstants.setBold(common, true);

        Style textObjectStyle = styleContext.addStyle("text_object", common);
        StyleConstants.setForeground(textObjectStyle, new Color(0, 100, 0));

        Style graphicsStyle = styleContext.addStyle("graphics", common);
        StyleConstants.setForeground(graphicsStyle, new Color(255, 68, 68));

        Style concatStyle = styleContext.addStyle("cm", common);
        StyleConstants.setForeground(concatStyle, new Color(1, 169, 219));

        Style inlineImage = styleContext.addStyle("inline_image", common);
        StyleConstants.setForeground(inlineImage, new Color(71, 117, 163));

        Style imageData = styleContext.addStyle("ID", common);
        StyleConstants.setForeground(imageData, new Color(255, 165, 0));

        Map<String, Style> styleMap = new HashMap<String, Style>();

        styleMap.put(BEGIN_TEXT_OBJECT, textObjectStyle);
        styleMap.put(END_TEXT_OBJECT, textObjectStyle);
        styleMap.put(SAVE_GRAPHICS_STATE, graphicsStyle);
        styleMap.put(RESTORE_GRAPHICS_STATE, graphicsStyle);
        styleMap.put(CONCAT, concatStyle);
        styleMap.put(INLINE_IMAGE_BEGIN, inlineImage);
        styleMap.put(IMAGE_DATA, imageData);
        styleMap.put(INLINE_IMAGE_END, inlineImage);

        operatorStyleMap = styleMap;
    }

    private OperatorMarker()
    {
    }

    public static Style getStyle(String operator)
    {
        if (operatorStyleMap.containsKey(operator))
        {
            return operatorStyleMap.get(operator);
        }
        return null;
    }
}
