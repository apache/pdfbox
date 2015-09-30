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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;

/**
 * @author Khyrul Bashar
 *  A class that provide tooltip for K and k.
 */
final class KToolTip extends ColorToolTip

{
    /**
     * Constructor.
     * @param rowText String instance.
     */
    KToolTip(String rowText)
    {
        createMarkUp(rowText);
    }

    private void createMarkUp(String rowText)
    {
        float[] colorValues = extractColorValues(rowText);
        if (colorValues != null)
        {
            try
            {
                float[] rgbValues = getICCColorSpace().toRGB(colorValues);
                setToolTipText(getMarkUp(colorHexValue(new Color(rgbValues[0], rgbValues[1], rgbValues[2]))));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    ICC_ColorSpace getICCColorSpace() throws IOException
    {
        // loads the ICC color profile for CMYK
        ICC_Profile iccProfile = getICCProfile();
        if (iccProfile == null)
        {
            throw new IOException("Default CMYK color profile could not be loaded");
        }

        return new ICC_ColorSpace(iccProfile);
    }

    ICC_Profile getICCProfile() throws IOException
    {
        // Adobe Acrobat uses "U.S. Web Coated (SWOP) v2" as the default
        // CMYK profile, however it is not available under an open license.
        // Instead, the "ISO Coated v2 300% (basICColor)" is used, which
        // is an open alternative to the "ISO Coated v2 300% (ECI)" profile.

        String name = "org/apache/pdfbox/resources/icc/ISOcoated_v2_300_bas.icc";

        URL url = PDDeviceCMYK.class.getClassLoader().getResource(name);
        if (url == null)
        {
            throw new IOException("Error loading resource: " + name);
        }

        InputStream input = url.openStream();
        ICC_Profile iccProfile = ICC_Profile.getInstance(input);
        input.close();

        return iccProfile;
    }
}
