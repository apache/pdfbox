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
import java.awt.Paint;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;

/**
 * This class represents a color space and the color value for that colorspace.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDColorState implements Cloneable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDColorState.class);

    /**
     * The default color that can be set to replace all colors in {@link ICC_ColorSpace ICC color spaces}.
     * 
     * @see #setIccOverrideColor(Color)
     */
    private static volatile Color iccOverrideColor = Color.getColor("org.apache.pdfbox.ICC_override_color");

    /**
     * Sets the default color to replace all colors in {@link ICC_ColorSpace ICC color spaces}. This will work around a
     * potential JVM crash caused by broken native ICC color manipulation code in the Sun class libraries.
     * <p>
     * The default override can be specified by setting the color code in
     * <code>org.apache.pdfbox.ICC_override_color</code> system property (see {@link Color#getColor(String)}. If this
     * system property is not specified, then the override is not enabled unless this method is explicitly called.
     * 
     * @param color ICC override color, or <code>null</code> to disable the override
     * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-511">PDFBOX-511</a>
     * @since Apache PDFBox 0.8.1
     */
    public static void setIccOverrideColor(Color color)
    {
        iccOverrideColor = color;
    }

    private PDColorSpace colorSpace = new PDDeviceGray();
    private COSArray colorSpaceValue = new COSArray();
    private PDPatternResources pattern = null;

    /**
     * Cached Java AWT color based on the current color space and value. The value is cleared whenever the color space
     * or value is set.
     * 
     * @see #getJavaColor()
     */
    private Color color = null;
    private Paint paint = null;

    /**
     * Default constructor.
     * 
     */
    public PDColorState()
    {
        setColorSpaceValue(new float[] { 0 });
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDColorState retval = new PDColorState();
        retval.colorSpace = colorSpace;
        retval.colorSpaceValue.clear();
        retval.colorSpaceValue.addAll(colorSpaceValue);
        retval.setPattern(getPattern());
        return retval;
    }

    /**
     * Returns the Java AWT color based on the current color space and value.
     * 
     * @return current Java AWT color
     * @throws IOException if the current color can not be created
     */
    public Color getJavaColor() throws IOException
    {
        if (color == null && colorSpaceValue.size() > 0)
        {
            color = createColor();
        }
        return color;
    }

    /**
     * Returns the Java AWT paint based on the current pattern.
     * 
     * @param pageHeight the height of the current page
     * @return current Java AWT paint
     * 
     * @throws IOException if the current color can not be created
     */
    public Paint getPaint(int pageHeight) throws IOException
    {
        if (paint == null && pattern != null)
        {
            paint = pattern.getPaint(pageHeight);
        }
        return paint;
    }

    /**
     * Create the current color from the colorspace and values.
     * 
     * @return The current awt color.
     * @throws IOException If there is an error creating the color.
     */
    private Color createColor() throws IOException
    {
        float[] components = colorSpaceValue.toFloatArray();
        try
        {
            String csName = colorSpace.getName();
            if (PDDeviceRGB.NAME.equals(csName) && components.length == 3)
            {
                // for some reason, when using RGB and the RGB colorspace
                // the new Color doesn't maintain exactly the same values
                // I think some color conversion needs to take place first
                // for now we will just make rgb a special case.
                return new Color(components[0], components[1], components[2]);
            }
            else if (PDLab.NAME.equals(csName))
            {
                // transform the color values from Lab- to RGB-space
                float[] csComponents = colorSpace.getJavaColorSpace().toRGB(components);
                return new Color(csComponents[0], csComponents[1], csComponents[2]);
            }
            else
            {
                if (components.length == 1)
                {
                    if (PDSeparation.NAME.equals(csName))
                    {
                        // Use that component as a single-integer RGB value
                        return new Color((int) components[0]);
                    }
                    if (PDDeviceGray.NAME.equals(csName))
                    {
                        // Handling DeviceGray as a special case as with JVM 1.5.0_15
                        // and maybe others printing on Windows fails with an
                        // ArrayIndexOutOfBoundsException when selecting colors
                        // and strokes e.g. sun.awt.windows.WPrinterJob.setTextColor
                        return new Color(components[0], components[0], components[0]);
                    }
                }
                Color override = iccOverrideColor;
                ColorSpace cs = colorSpace.getJavaColorSpace();
                if (cs instanceof ICC_ColorSpace && override != null)
                {
                    LOG.warn("Using an ICC override color to avoid a potential" + " JVM crash (see PDFBOX-511)");
                    return override;
                }
                else
                {
                    return new Color(cs, components, 1f);
                }
            }
        }
        // Catch IOExceptions from PDColorSpace.getJavaColorSpace(), but
        // possibly also IllegalArgumentExceptions or other RuntimeExceptions
        // from the potentially complex color management code.
        catch (Exception e)
        {
            Color cGuess;
            String sMsg = "Unable to create the color instance " + Arrays.toString(components) + " in color space "
                    + colorSpace + "; guessing color ... ";
            try
            {
                switch (components.length)
                {
                case 1:// Use that component as a single-integer RGB value
                    cGuess = new Color((int) components[0]);
                    sMsg += "\nInterpretating as single-integer RGB";
                    break;
                case 3: // RGB
                    cGuess = new Color(components[0], components[1], components[2]);
                    sMsg += "\nInterpretating as RGB";
                    break;
                case 4: // CMYK
                    // do a rough conversion to RGB as I'm not getting the CMYK to work.
                    // http://www.codeproject.com/KB/applications/xcmyk.aspx
                    float r,
                    g,
                    b,
                    k;
                    k = components[3];

                    r = components[0] * (1f - k) + k;
                    g = components[1] * (1f - k) + k;
                    b = components[2] * (1f - k) + k;

                    r = (1f - r);
                    g = (1f - g);
                    b = (1f - b);

                    cGuess = new Color(r, g, b);
                    sMsg += "\nInterpretating as CMYK";
                    break;
                default:

                    sMsg += "\nUnable to guess using " + components.length + " components; using black instead";
                    cGuess = Color.BLACK;
                }
            }
            catch (Exception e2)
            {
                sMsg += "\nColor interpolation failed; using black instead\n";
                sMsg += e2.toString();
                cGuess = Color.BLACK;
            }
            LOG.warn(sMsg, e);
            return cGuess;
        }
    }

    /**
     * Constructor with an existing color set. Default colorspace is PDDeviceGray.
     * 
     * @param csValues The color space values.
     */
    public PDColorState(COSArray csValues)
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
        // Clear color cache and current pattern
        color = null;
        pattern = null;
    }

    /**
     * This will get the color space values. Either 1 for gray or 3 for RGB.
     * 
     * @return The colorspace values.
     */
    public float[] getColorSpaceValue()
    {
        return colorSpaceValue.toFloatArray();
    }

    /**
     * This will get the color space values. Either 1 for gray or 3 for RGB.
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
        colorSpaceValue.setFloatArray(value);
        // Clear color cache and current pattern
        color = null;
        pattern = null;
    }

    /**
     * This will get the current pattern.
     * 
     * @return The current pattern.
     */
    public PDPatternResources getPattern()
    {
        return pattern;
    }

    /**
     * This will update the current pattern.
     * 
     * @param patternValue The new pattern.
     */
    public void setPattern(PDPatternResources patternValue)
    {
        pattern = patternValue;
        // Clear color cache
        color = null;
    }

}
