/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.debugger.fontencodingpane;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;

abstract class FontPane
{
    abstract JPanel getPanel();

    /**
     * Calculate vertical bounds common to all rendered glyphs.
     *
     * @param tableData
     * @param glyphIndex the table index that has glyphs.
     * @return an array with two elements: min lower bound (but max 0), and max upper bound (but min
     * 0).
     */
    double[] getYBounds(Object[][] tableData, int glyphIndex)
    {
        double minY = 0;
        double maxY = 0;
        for (Object[] aTableData : tableData)
        {
            GeneralPath path = (GeneralPath) aTableData[glyphIndex];
            Rectangle2D bounds2D = path.getBounds2D();
            if (bounds2D.isEmpty())
            {
                continue;
            }
            minY = Math.min(minY, bounds2D.getMinY());
            maxY = Math.max(maxY, bounds2D.getMaxY());
        }
        return new double[]{minY, maxY};
    }
}

/**
 * @author Khyrul Bashar
 *
 * A class that shows the glyph table or CIDToGID map depending on the font type. PDSimple and
 * PDType0Font are supported.
 */
public class FontEncodingPaneController
{
    private static final Log LOG = LogFactory.getLog(FontEncodingPaneController.class);

    private FontPane fontPane;

    /**
     * Constructor.
     * @param fontName COSName instance, Font name in the fonts dictionary.
     * @param dictionary COSDictionary instance for resources which resides the font.
     */
    public FontEncodingPaneController(COSName fontName, COSDictionary dictionary)
    {
        PDResources resources = new PDResources(dictionary);
        try
        {
            PDFont font = resources.getFont(fontName);
            if (font instanceof PDType3Font)
            {
                fontPane = new Type3Font((PDType3Font) font, resources);
            }
            else if (font instanceof PDSimpleFont)
            {
                fontPane = new SimpleFont((PDSimpleFont) font);
            }
            else if (font instanceof PDType0Font)
            {
                fontPane = new Type0Font(((PDType0Font) font).getDescendantFont(), (PDType0Font) font);
            }
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Return a pane to display details of a font.
     * 
     * @return a pane for font information, or null if that font type is not supported.
     */
    public JPanel getPane()
    {
        if (fontPane != null)
        {
            return fontPane.getPanel();
        }
        return null;
    }
}
