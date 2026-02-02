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
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;

/**
 * @author Khyrul Bashar
 *  A class that provide tooltip for SCN and scn.
 */
final class SCNToolTip extends ColorToolTip
{
    private static final Log LOG = LogFactory.getLog(SCNToolTip.class);

    /**
     * Constructor.
     * @param rowText String instance.
     */
    SCNToolTip(PDResources resources, String colorSpaceName, String rowText)
    {
        createMarkUp(resources, colorSpaceName.substring(1).trim(), rowText);
    }

    private void createMarkUp(PDResources resources, String colorSpaceName, String rowText)
    {
        PDColorSpace colorSpace = null;
        try
        {
            colorSpace = resources.getColorSpace(COSName.getPDFName(colorSpaceName));
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        }
        if (colorSpace instanceof PDPattern)
        {
            setToolTipText("<html>Pattern</html>");
            return;
        }
        if (colorSpace != null)
        {
            try
            {
                float[] rgbValues = colorSpace.toRGB(extractColorValues(rowText));
                if (rgbValues != null)
                {
                    Color color = new Color(rgbValues[0], rgbValues[1], rgbValues[2]);
                    setToolTipText(getMarkUp(colorHexValue(color)));
                }
            }
            catch (IOException e)
            {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
