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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Khyrul Bashar
 * A class that shows the CIDToGID table along with unicode characters for Type0Fonts when descendent
 * font is of type PDCIDFontType2.
 */
class Type0Font extends FontPane
{
    public static final String NO_GLYPH = "No glyph";
    private final FontEncodingView view;
    private int totalAvailableGlyph = 0;
    
    /**
     * Constructor.
     * @param descendantFont PDCIDFontType2 instance.
     * @param parentFont PDFont instance.
     * @throws IOException If fails to parse cidtogid map.
     */
    Type0Font(PDCIDFont descendantFont, PDType0Font parentFont) throws IOException
    {
        Object[][] cidtogid = readCIDToGIDMap(descendantFont, parentFont);
        if (cidtogid != null)
        {
            Map<String, String> attributes = new LinkedHashMap<String, String>();
            attributes.put("Font", descendantFont.getName());
            attributes.put("CIDs", Integer.toString(cidtogid.length));

            view = new FontEncodingView(cidtogid, attributes, 
                    new String[]{"CID", "GID", "Unicode Character", "Glyph"}, getYBounds(cidtogid, 3));
        }
        else
        {
            Object[][] tab = readMap(descendantFont, parentFont);
            Map<String, String> attributes = new LinkedHashMap<String, String>();
            attributes.put("Font", descendantFont.getName());
            attributes.put("CIDs", Integer.toString(tab.length));
            attributes.put("Glyphs", Integer.toString(totalAvailableGlyph));
            attributes.put("Standard 14", Boolean.toString(parentFont.isStandard14()));

            view = new FontEncodingView(tab, attributes, 
                    new String[]{"Code", "CID", "GID", "Unicode Character", "Glyph"}, getYBounds(tab, 4));
        }
    }

    private Object[][] readMap(PDCIDFont descendantFont, PDType0Font parentFont) throws IOException
    {
        int codes = 0;
        for (int code = 0; code < 65535; ++code)
        {
            if (descendantFont.hasGlyph(code))
            {
                ++codes;
            }
        }
        Object[][] tab = new Object[codes][5];
        int index = 0;
        for (int code = 0; code < 65535; ++code)
        {
            if (descendantFont.hasGlyph(code))
            {
                tab[index][0] = code;
                tab[index][1] = descendantFont.codeToCID(code);
                tab[index][2] = descendantFont.codeToGID(code);
                tab[index][3] = parentFont.toUnicode(code);
                GeneralPath path = descendantFont.getPath(code);
                tab[index][4] = path;
                if (!path.getBounds2D().isEmpty())
                {
                    ++totalAvailableGlyph;
                }
                ++index;
            }
        }
        return tab;
    }

    private Object[][] readCIDToGIDMap(PDCIDFont font, PDFont parentFont) throws IOException
    {
        Object[][] cid2gid = null;
        COSDictionary dict = font.getCOSObject();
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        if (map instanceof COSStream)
        {
            COSStream stream = (COSStream) map;

            InputStream is = stream.createInputStream();
            byte[] mapAsBytes = IOUtils.toByteArray(is);
            IOUtils.closeQuietly(is);
            int numberOfInts = mapAsBytes.length / 2;
            cid2gid = new Object[numberOfInts][4];
            int offset = 0;
            for (int index = 0; index < numberOfInts; index++)
            {
                int gid = (mapAsBytes[offset] & 0xff) << 8 | mapAsBytes[offset + 1] & 0xff;
                cid2gid[index][0] = index;
                cid2gid[index][1] = gid;
                if (gid != 0 && parentFont.toUnicode(index) != null)
                {
                    cid2gid[index][2] = parentFont.toUnicode(index);
                }
                GeneralPath path = font.getPath(index);
                cid2gid[index][3] = path;
                if (!path.getBounds2D().isEmpty())
                {
                    ++totalAvailableGlyph;
                }
                offset += 2;
            }
        }
        return cid2gid;
    }

    @Override
    public JPanel getPanel()
    {
        if (view != null)
        {
            return view.getPanel();
        }
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 500));
        return panel;
    }
}
