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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.util.Map;

/**
 * A color space specifies how the colours of graphics objects will be painted on the page.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public abstract class PDColorSpace implements COSObjectable
{
    /**
     * Creates a color space space given a name or array.
     * @param colorSpace the color space COS object
     * @return a new color space
     * @throws IOException if the color space is unknown or cannot be created
     */
    public static PDColorSpace create(COSBase colorSpace) throws IOException
    {
        return create(colorSpace, null, null);
    }

    /**
     * Creates a color space given a name or array.
     * @param colorSpace the color space COS object
     * @param colorSpaces the ColorSpace dictionary from the current resources, if any
     * @param patterns The Pattern dictionary from the current resources, if any
     * @return a new color space
     * @throws IOException if the color space is unknown or cannot be created
     */
    public static PDColorSpace create(COSBase colorSpace,
                                      Map<String, PDColorSpace> colorSpaces,
                                      Map<String, PDAbstractPattern> patterns)
                                      throws IOException
    {
        if (colorSpace instanceof COSObject)
        {
            return create(((COSObject) colorSpace).getObject(), colorSpaces, patterns);
        }
        else if (colorSpace instanceof COSName)
        {
            COSName name = (COSName)colorSpace;

            if (name == COSName.DEVICECMYK || name == COSName.CMYK)
            {
                return PDDeviceCMYK.INSTANCE;
            }
            else if (name == COSName.DEVICERGB || name == COSName.RGB)
            {
                return PDDeviceRGB.INSTANCE;
            }
            else if (name == COSName.DEVICEGRAY || name == COSName.G)
            {
                return PDDeviceGray.INSTANCE;
            }
            else if (name == COSName.PATTERN)
            {
                return new PDPattern(patterns);
            }
            else if (colorSpaces != null && colorSpaces.get(name.getName()) != null)
            {
                // a color space resource
                return colorSpaces.get(name.getName());
            }
            else
            {
                throw new IOException("The color space '" + name + "' does not exist in the " +
                                      "current page's resources");
            }
        }
        else if (colorSpace instanceof COSArray)
        {
            COSArray array = (COSArray)colorSpace;
            COSName name = (COSName)array.get(0);

            // TODO cache these returned color spaces?

            if (name == COSName.CALGRAY)
            {
                return new PDCalGray(array);
            }
            else if (name == COSName.CALRGB)
            {
                return new PDCalRGB(array);
            }
            else if (name == COSName.DEVICEN)
            {
                return new PDDeviceN(array);
            }
            else if (name == COSName.INDEXED || name == COSName.I)
            {
                return new PDIndexed(array);
            }
            else if (name == COSName.SEPARATION)
            {
                return new PDSeparation(array);
            }
            else if (name == COSName.ICCBASED)
            {
                return new PDICCBased(array);
            }
            else if (name == COSName.LAB)
            {
                return new PDLab(array);
            }
            else if (name == COSName.PATTERN)
            {
                if (array.size() == 1)
                {
                    return new PDPattern(patterns);
                }
                else
                {
                    return new PDPattern(patterns, PDColorSpace.create(array.get(1)));
                }
            }
            else if (name == COSName.DEVICECMYK || name == COSName.CMYK ||
                     name == COSName.DEVICERGB  || name == COSName.RGB ||
                     name == COSName.DEVICEGRAY || name == COSName.PATTERN)
            {
                // not allowed in an array, but we sometimes encounter these regardless
                return create(name, colorSpaces, patterns);
            }
            else
            {
                throw new IOException("Invalid color space kind: " + name);
            }
        }
        else
        {
            throw new IOException("Expected a name or array but got: " + colorSpace);
        }
    }

    // array for the given parameters
    protected COSArray array;

    /**
     * Returns the name of the color space.
     * @return the name of the color space
     */
    public abstract String getName();

    /**
     * Returns the number of components in this color space
     * @return the number of components in this color space
     */
    public abstract int getNumberOfComponents();

    /**
     * Returns the default decode array for this color space.
     * @return the default decode array
     */
    public abstract float[] getDefaultDecode(int bitsPerComponent);

    /**
     * Returns the initial color value for this color space.
     * @return the initial color value for this color space
     */
    public abstract PDColor getInitialColor();

    /**
     * Returns the RGB equivalent of the given color value.
     * @param value a color value with component values between 0 and 1
     * @return an array of R,G,B value between 0 and 255
     * @throws IOException if the color conversion fails
     */
    public abstract float[] toRGB(float[] value) throws IOException;

    /**
     * Returns the (A)RGB equivalent of the given raster.
     * @param raster the source raster
     * @return an (A)RGB buffered image
     * @throws IOException if the color conversion fails
     */
    public abstract BufferedImage toRGBImage(WritableRaster raster) throws IOException;

    /**
     * Returns the (A)RGB equivalent of the given raster, using the given AWT color space
     * to perform the conversion.
     * @param raster the source raster
     * @param colorSpace the AWT
     * @return an (A)RGB buffered image
     */
    protected BufferedImage toRGBImageAWT(WritableRaster raster, ColorSpace colorSpace)
    {
        //
        // WARNING: this method is performance sensitive, modify with care!
        //

        // ICC Profile color transforms are only fast when performed using ColorConvertOp
        ColorModel colorModel = new ComponentColorModel(colorSpace,
            false, false, Transparency.OPAQUE, raster.getDataBuffer().getDataType());

        BufferedImage src = new BufferedImage(colorModel, raster, false, null);
        BufferedImage dest = new BufferedImage(raster.getWidth(), raster.getHeight(),
                                               BufferedImage.TYPE_INT_RGB);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(src, dest);
        return dest;
    }

    /**
     * Returns the AWT paint which corresponds to the given color value in this color space.
     * @param color the color value
     * @return an AWT paint
     * @throws IOException if the color conversion fails
     */
    public Paint toPaint(PDFRenderer renderer,  PDColor color) throws IOException
    {
        return toPaint(renderer, color, 0);
    }

    /**
     * Returns the AWT paint which corresponds to the given color value in this color space
     * and the height of the current page.
     * This is for use with pattern color spaces
     * @param color the color value
     * @param pageHeight the height of the current page, used by pattern color spaces
     * @return an AWT paint
     * @throws IOException if the color conversion fails
     */
    public Paint toPaint(PDFRenderer renderer, PDColor color, int pageHeight) throws IOException
    {
        float[] rgb = toRGB(color.getComponents());
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    @Override
    public COSBase getCOSObject()
    {
        return array;
    }

}
