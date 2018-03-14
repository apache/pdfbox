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
package org.apache.pdfbox.pdmodel.graphics.color;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;

/**
 * Colours in the DeviceRGB colour space are specified according to the additive
 * RGB (red-green-blue) colour model.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDDeviceRGB extends PDDeviceColorSpace
{
    /**  This is the single instance of this class. */
    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB();
    
    private final PDColor initialColor = new PDColor(new float[] { 0, 0, 0 }, this);
    private volatile ColorSpace awtColorSpace;

    private static final Log LOG = LogFactory.getLog(PDDeviceRGB.class);

    private PDDeviceRGB()
    {
    }

    /**
     * Lazy setting of the AWT color space due to JDK race condition.
     */
    private void init()
    {
        // no need to synchronize this check as it is atomic
        if (awtColorSpace != null)
        {
            return;
        }

        suggestKCMS();

        synchronized (this)
        {
            // we might have been waiting for another thread, so check again
            if (awtColorSpace != null)
            {
                return;
            }
            awtColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            
            // there is a JVM bug which results in a CMMException which appears to be a race
            // condition caused by lazy initialization of the color transform, so we perform
            // an initial color conversion while we're still synchronized, see PDFBOX-2184
            awtColorSpace.toRGB(new float[] { 0, 0, 0, 0 });
        }
    }
    
    @Override
    public String getName()
    {
        return COSName.DEVICERGB.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfComponents()
    {
        return 3;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1, 0, 1, 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        return value;
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        init();

        //
        // WARNING: this method is performance sensitive, modify with care!
        //
        // Please read PDFBOX-3854 and PDFBOX-2092 and look at the related commits first.
        // The current code returns TYPE_INT_RGB images which prevents slowness due to threads
        // blocking each other when TYPE_CUSTOM images are used.
        BufferedImage image = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.setData(raster);
        return image;
    }

    private static void suggestKCMS()
    {
        String cmmProperty = System.getProperty("sun.java2d.cmm");
        if (isMinJdk8() && !"sun.java2d.cmm.kcms.KcmsServiceProvider".equals(cmmProperty))
        {
            try
            {
                // Make sure that class exists
                Class.forName("sun.java2d.cmm.kcms.KcmsServiceProvider");

                LOG.info("To get higher rendering speed on JDK8 or later,");
                LOG.info("  use the option -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider");
                LOG.info("  or call System.setProperty(\"sun.java2d.cmm\", \"sun.java2d.cmm.kcms.KcmsServiceProvider\")");
            }
            catch (ClassNotFoundException e)
            {
                LOG.debug("KCMS doesn't exist anymore. SO SAD!");
            }
        }
    }

    private static boolean isMinJdk8()
    {
        // strategy from lucene-solr/lucene/core/src/java/org/apache/lucene/util/Constants.java
        String version = System.getProperty("java.specification.version");
        final StringTokenizer st = new StringTokenizer(version, ".");
        try
        {
            int major = Integer.parseInt(st.nextToken());
            int minor = 0;
            if (st.hasMoreTokens())
            {
                minor = Integer.parseInt(st.nextToken());
            }
            return major > 1 || (major == 1 && minor >= 8);
        }
        catch (NumberFormatException nfe)
        {
            // maybe some new numbering scheme in the 22nd century
            return true;
        }
    }
}
