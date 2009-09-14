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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;

/**
 * This class represents a color space and the color value for that colorspace.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class PDColorState implements Cloneable
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDColorState.class);

    /**
     * The default color that can be set to replace all colors in
     * {@link ICC_ColorSpace ICC color spaces}.
     *
     * @see #setIccOverrideColor(Color)
     */
    private static volatile Color iccOverrideColor =
        Color.getColor("org.apache.pdfbox.ICC_override_color");

    /**
     * Sets the default color to replace all colors in
     * {@link ICC_ColorSpace ICC color spaces}. This will work around
     * a potential JVM crash caused by broken native ICC color manipulation
     * code in the Sun class libraries.
     * <p>
     * The default override can be specified by setting the color code in
     * <code>org.apache.pdfbox.ICC_override_color</code> system property
     * (see {@link Color#getColor(String)}. If this system property is not
     * specified, then the override is not enabled unless this method is
     * explicitly called.
     *
     * @param color ICC override color,
     *              or <code>null</code> to disable the override
     * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-511">PDFBOX-511</a>
     * @since Apache PDFBox 0.8.1
     */
    public static void setIccOverrideColor(Color color) {
        iccOverrideColor = color;
    }

    private PDColorSpace colorSpace = new PDDeviceGray();
    private COSArray colorSpaceValue = new COSArray();

    /**
     * Cached Java AWT color based on the current color space and value.
     * The value is cleared whenever the color space or value is set.
     *
     * @see #getJavaColor()
     */
    private Color color = null;

    /**
     * Default constructor.
     *
     */
    public PDColorState()
    {
        setColorSpaceValue( new float[] {0});
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDColorState retval = new PDColorState();
        retval.colorSpace = this.colorSpace;
        retval.colorSpaceValue.clear();
        retval.colorSpaceValue.addAll( this.colorSpaceValue );
        return retval;
    }

    /**
     * Returns the Java AWT color based on the current color space and value.
     *
     * @return current Java AWT color
     * @throws IOException if the current color can not be created
     */
    public Color getJavaColor() throws IOException {
        if (color == null) {
            color = createColor();
        }
        return color;
    }

    /**
     * Create the current color from the colorspace and values.
     * @return The current awt color.
     * @throws IOException If there is an error creating the color.
     */
    private Color createColor() throws IOException
    {
        float[] components = colorSpaceValue.toFloatArray();
        try
        {
            if( colorSpace.getName().equals(PDDeviceRGB.NAME) && components.length == 3 )
            {
                //for some reason, when using RGB and the RGB colorspace
                //the new Color doesn't maintain exactly the same values
                //I think some color conversion needs to take place first
                //for now we will just make rgb a special case.
                return new Color( components[0], components[1], components[2] );
            }
            else
            {
                Color override = iccOverrideColor;
                ColorSpace cs = colorSpace.getJavaColorSpace();
                if (colorSpace.getName().equals(PDSeparation.NAME) && components.length == 1)
                {
                    //Use that component as a single-integer RGB value
                    return new Color((int)components[0]);
                }
                else if (cs instanceof ICC_ColorSpace && override != null)
                {
                    log.warn(
                            "Using an ICC override color to avoid a potential"
                            + " JVM crash (see PDFBOX-511)");
                    return override;
                }
                else
                {
                    return new Color( cs, components, 1f );
                }
            }
        }
        catch (java.lang.IllegalArgumentException exception)
        {
            String values = "Color Values: ";
            for(int i=0; i< components.length; i++)
            {
                values = values + components[i] + "\t";
            }
            log.error(exception + "\n" + values, exception);
            throw exception;
        }
        catch (IOException ioexception)
        {
            log.error(ioexception, ioexception);
            throw ioexception;
        }
        catch (Exception e)
        {
            log.error(e, e);
            throw new IOException("Failed to Create Color");
         }
    }

    /**
     * Constructor with an existing color set.  Default colorspace is PDDeviceGray.
     *
     * @param csValues The color space values.
     */
    public PDColorState( COSArray csValues )
    {
        colorSpaceValue = csValues;
    }


    /**
     * This will get the current colorspace.
     *
     * @return The current colorspace.
     */
    public PDColorSpace getColorSpace()
    {
        return colorSpace;
    }

    /**
     * This will set the current colorspace.
     *
     * @param value The new colorspace.
     */
    public void setColorSpace(PDColorSpace value)
    {
        colorSpace = value;
        // Clear color cache
        color = null;
    }

    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public float[] getColorSpaceValue()
    {
        return colorSpaceValue.toFloatArray();
    }

    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public COSArray getCOSColorSpaceValue()
    {
        return colorSpaceValue;
    }

    /**
     * This will update the colorspace values.
     *
     * @param value The new colorspace values.
     */
    public void setColorSpaceValue(float[] value)
    {
        colorSpaceValue.setFloatArray( value );
        // Clear color cache
        color = null;
    }
}
