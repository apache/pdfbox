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

import org.apache.pdfbox.contentstream.operator.OperatorName;

/**
 * @author Khyrul Bashar
 */
final class OperatorMarker
{
    private static final Map<String, Style> operatorStyleMap;

    static
    {
        final StyleContext styleContext = StyleContext.getDefaultStyleContext();

        final Style common = styleContext.addStyle("common", null);
        StyleConstants.setBold(common, true);

        final Style textObjectStyle = styleContext.addStyle("text_object", common);
        StyleConstants.setForeground(textObjectStyle, new Color(0, 100, 0));

        final Style graphicsStyle = styleContext.addStyle("graphics", common);
        StyleConstants.setForeground(graphicsStyle, new Color(255, 68, 68));

        final Style concatStyle = styleContext.addStyle("cm", common);
        StyleConstants.setForeground(concatStyle, new Color(1, 169, 219));

        final Style inlineImage = styleContext.addStyle("inline_image", common);
        StyleConstants.setForeground(inlineImage, new Color(71, 117, 163));

        final Style imageData = styleContext.addStyle("ID", common);
        StyleConstants.setForeground(imageData, new Color(255, 165, 0));

        final Map<String, Style> styleMap = new HashMap<>();

        styleMap.put(OperatorName.BEGIN_TEXT, textObjectStyle);
        styleMap.put(OperatorName.END_TEXT, textObjectStyle);
        styleMap.put(OperatorName.SAVE, graphicsStyle);
        styleMap.put(OperatorName.RESTORE, graphicsStyle);
        styleMap.put(OperatorName.CONCAT, concatStyle);
        styleMap.put(OperatorName.BEGIN_INLINE_IMAGE, inlineImage);
        styleMap.put(OperatorName.BEGIN_INLINE_IMAGE_DATA, imageData);
        styleMap.put(OperatorName.END_INLINE_IMAGE, inlineImage);

        operatorStyleMap = styleMap;
    }

    private OperatorMarker()
    {
    }

    public static Style getStyle(final String operator)
    {
        if (operatorStyleMap.containsKey(operator))
        {
            return operatorStyleMap.get(operator);
        }
        return null;
    }
}
