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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * @author Khyrul Bashar
 * A class that provides tooltip text for font. This shows the name of the font.
 */
final class FontToolTip implements ToolTip
{
    private static final Log LOG = LogFactory.getLog(FontToolTip.class);
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
            markup = "<html>" + font.getName() + "</html>";
        }
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
