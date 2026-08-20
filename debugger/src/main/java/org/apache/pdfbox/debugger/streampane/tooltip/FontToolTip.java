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

import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * @author Khyrul Bashar
 * A class that provides tooltip text for font. This shows the name of the font.
 */
final class FontToolTip implements ToolTip
{
    private static final Logger LOG = LogManager.getLogger(FontToolTip.class);
    private String markup;

    /**
     * Constructor.
     * @param resources PDResources instance. Which corresponds the resource dictionary containing
     *                  the concern font.
     * @param rowText String instance of the tooltip row.
     */
    FontToolTip(PDResources resources, String rowText)
    {
        initUI(extractFontReference(rowText), resources);
    }

    private void initUI(String fontReferenceName, PDResources resources)
    {
        PDFont font = null;
        for (COSName name: resources.getFontNames())
        {
            if (name.getName().equals(fontReferenceName))
            {
                try
                {
                    font = resources.getFont(name);
                }
                catch (IOException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        if (font != null)
        {
            markup = "<html>" + escapeHtml(font.getName()) + "</html>";
        }
    }

    /**
     * Escape a document-derived string so that Swing's HTML renderer treats it as inert text. Font
     * names come straight from the PDF (the /BaseFont entry) and may contain arbitrary characters,
     * including markup such as &lt;img&gt; tags whose URLs Swing would fetch when the tooltip is
     * shown.
     *
     * @param text the raw text, may be null
     * @return the escaped text, or null if text was null
     */
    private static String escapeHtml(String text)
    {
        if (text == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            switch (c)
            {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    private String extractFontReference(String rowText)
    {
        return rowText.trim().split(" ")[0].substring(1);
    }

    @Override
    public String getToolTipText()
    {
        return markup;
    }
}
