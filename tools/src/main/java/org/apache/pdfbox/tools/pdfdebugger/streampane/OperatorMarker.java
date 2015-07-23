package org.apache.pdfbox.tools.pdfdebugger.streampane;

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
    private Map<String, Style> operatorStyleMap;

    OperatorMarker()
    {
        operatorStyleMap = new HashMap<String, Style>();
        initOperatorStyles();
    }

    private void initOperatorStyles()
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

        final String BEGIN_TEXT_OBJECT = "BT";
        final String END_TEXT_OBJECT = "ET";
        final String SAVE_GRAPHICS_STATE = "q";
        final String RESTORE_GRAPHICS_STATE = "Q";
        final String CONCAT = "cm";
        final String INLINE_IMAGE_BEGIN = "BI";
        final String IMAGE_DATA = "ID";
        final String INLINE_IMAGE_END = "EI";


        operatorStyleMap.put(BEGIN_TEXT_OBJECT, textObjectStyle);
        operatorStyleMap.put(END_TEXT_OBJECT, textObjectStyle);
        operatorStyleMap.put(SAVE_GRAPHICS_STATE, graphicsStyle);
        operatorStyleMap.put(RESTORE_GRAPHICS_STATE, graphicsStyle);
        operatorStyleMap.put(CONCAT, concatStyle);
        operatorStyleMap.put(INLINE_IMAGE_BEGIN, inlineImage);
        operatorStyleMap.put(IMAGE_DATA, imageData);
        operatorStyleMap.put(INLINE_IMAGE_END, inlineImage);
    }

    Style getStyle(String operator)
    {
        if (operatorStyleMap.containsKey(operator))
        {
            return operatorStyleMap.get(operator);
        }
        return null;
    }
}
