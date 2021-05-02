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

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.MissingResourceException;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.ResourceCache;

/**
 * A color space specifies how the colours of graphics objects will be painted on the page.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public abstract class PDColorSpace implements COSObjectable
{
    /**
     * Creates a color space given a name or array.
     * @param colorSpace the color space COS object
     * @return a new color space
     * @throws IOException if the color space is unknown or cannot be created
     */
    public static PDColorSpace create(COSBase colorSpace) throws IOException
    {
        return create(colorSpace, null);
    }

    /**
     * Creates a color space given a name or array. Abbreviated device color names are not supported
     * here, please replace them first.
     *
     * @param colorSpace the color space COS object
     * @param resources the current resources.
     * @return a new color space
     * @throws MissingResourceException if the color space is missing in the resources dictionary
     * @throws IOException if the color space is unknown or cannot be created
     */
    public static PDColorSpace create(COSBase colorSpace,
                                      PDResources resources)
                                      throws IOException
    {
        return create(colorSpace, resources, false);
    }
    
    /**
     * Creates a color space given a name or array. Abbreviated device color names are not supported
     * here, please replace them first. This method is for PDFBox internal use only, others should
     * use {@link #create(COSBase, PDResources)}.
     *
     * @param colorSpace the color space COS object
     * @param resources the current resources.
     * @param wasDefault if current color space was used by a default color space.
     * @return a new color space.
     * @throws MissingResourceException if the color space is missing in the resources dictionary
     * @throws IOException if the color space is unknown or cannot be created.
     */
    public static PDColorSpace create(COSBase colorSpace,
                                      PDResources resources,
                                      boolean wasDefault)
                                      throws IOException
    {
        if (colorSpace instanceof COSObject)
        {
            return createFromCOSObject((COSObject) colorSpace, resources);
        }
        else if (colorSpace instanceof COSName)
        {
            COSName name = (COSName)colorSpace;

            // default color spaces
            if (resources != null)
            {
                COSName defaultName = null;
                if (name.equals(COSName.DEVICECMYK) &&
                    resources.hasColorSpace(COSName.DEFAULT_CMYK))
                {
                    defaultName = COSName.DEFAULT_CMYK;
                }
                else if (name.equals(COSName.DEVICERGB) &&
                         resources.hasColorSpace(COSName.DEFAULT_RGB))
                {
                    defaultName = COSName.DEFAULT_RGB;
                }
                else if (name.equals(COSName.DEVICEGRAY) &&
                         resources.hasColorSpace(COSName.DEFAULT_GRAY))
                {
                    defaultName = COSName.DEFAULT_GRAY;
                }

                if (resources.hasColorSpace(defaultName) && !wasDefault)
                {
                    return resources.getColorSpace(defaultName, true);
                }
            }

            // built-in color spaces
            if (name == COSName.DEVICECMYK)
            {
                return PDDeviceCMYK.INSTANCE;
            }
            else if (name == COSName.DEVICERGB)
            {
                return PDDeviceRGB.INSTANCE;
            }
            else if (name == COSName.DEVICEGRAY)
            {
                return PDDeviceGray.INSTANCE;
            }
            else if (name == COSName.PATTERN)
            {
                return new PDPattern(resources);
            }
            else if (resources != null)
            {
                if (!resources.hasColorSpace(name))
                {
                    throw new MissingResourceException("Missing color space: " + name.getName());
                }
                return resources.getColorSpace(name);
            }
            else
            {
                throw new MissingResourceException("Unknown color space: " + name.getName());
            }
        }
        else if (colorSpace instanceof COSArray)
        {
            COSArray array = (COSArray)colorSpace;
            if (array.size() == 0)
            {
                throw new IOException("Colorspace array is empty");
            }
            COSBase base = array.getObject(0);
            if (!(base instanceof COSName))
            {
                throw new IOException("First element in colorspace array must be a name");
            }
            COSName name = (COSName) base;

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
            else if (name == COSName.INDEXED)
            {
                return new PDIndexed(array, resources);
            }
            else if (name == COSName.SEPARATION)
            {
                return new PDSeparation(array);
            }
            else if (name == COSName.ICCBASED)
            {
                return PDICCBased.create(array, resources);
            }
            else if (name == COSName.LAB)
            {
                return new PDLab(array);
            }
            else if (name == COSName.PATTERN)
            {
                if (array.size() == 1)
                {
                    return new PDPattern(resources);
                }
                else
                {
                    return new PDPattern(resources, PDColorSpace.create(array.get(1)));
                }
            }
            else if (name == COSName.DEVICECMYK ||
                     name == COSName.DEVICERGB ||
                     name == COSName.DEVICEGRAY)
            {
                // not allowed in an array, but we sometimes encounter these regardless
                return create(name, resources, wasDefault);
            }
            else
            {
                throw new IOException("Invalid color space kind: " + name);
            }
        }
        else if (colorSpace instanceof COSDictionary && 
                ((COSDictionary) colorSpace).containsKey(COSName.COLORSPACE))
        {
            // PDFBOX-4833: dictionary with /ColorSpace entry
            return create(((COSDictionary) colorSpace).getDictionaryObject(COSName.COLORSPACE), resources, wasDefault);
        }
        else
        {
            throw new IOException("Expected a name or array but got: " + colorSpace);
        }
    }

    private static PDColorSpace createFromCOSObject(COSObject colorSpace, PDResources resources)
            throws IOException
    {
        PDColorSpace cs;
        if (resources != null && resources.getResourceCache() != null)
        {
            ResourceCache resourceCache = resources.getResourceCache();
            cs = resourceCache.getColorSpace(colorSpace);
            if (cs != null)
            {
                return cs;
            }
        }
        cs = create(colorSpace.getObject(), resources);
        if (resources != null && resources.getResourceCache() != null && cs != null)
        {
            ResourceCache resourceCache = resources.getResourceCache();
            resourceCache.put(colorSpace, cs);
        }
        return cs;
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
     * @param bitsPerComponent the number of bits per component.
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
     * Returns the image in this colorspace or null. No conversion is performed.
     *
     * For special colorspaces like PDSeparation the image is returned in the gray colorspace.
     * For undefined colorspaces like DeviceCMYK/DeviceRGB and DeviceGray null is returned.
     *
     * You can always fallback to {@link #toRGBImage(WritableRaster)} if this returns null.
     *
     * @param raster the source raster
     * @return an buffered image in this colorspace. Or null if it is not possible to extract
     * that image with the original colorspace without conversion.
     */
    public abstract BufferedImage toRawImage(WritableRaster raster) throws IOException;

    /**
     * Returns the given raster as BufferedImage with the given awtColorSpace using a
     * ComponentColorModel.
     * @param raster the source raster
     * @param awtColorSpace the AWT colorspace
     * @return a BufferedImage in this colorspace
     */
    protected final BufferedImage toRawImage(WritableRaster raster, ColorSpace awtColorSpace)
    {
        ColorModel colorModel = new ComponentColorModel(awtColorSpace,
                false, false, Transparency.OPAQUE, raster.getDataBuffer().getDataType());
        return new BufferedImage(colorModel, raster, false, null);
    }

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
        if (src.getWidth() == 1 || src.getHeight() == 1)
        {
            // PDFBOX-5051: ColorConvertOp is too slow for tiny images. But we can't use drawImage()
            // for all images because it is slower when used for all images.
            // Re quality & speed: the quality gold standard is setRGB(getRGB()) but this is also 
            // the slowest. drawImage() is identical in quality (but faster) except for ICC based
            // images with 1 component. ColorConvertOp is fastest except for small images, there's
            // somehow a slowness "price" paid per call and the quality is slightly flawed sometimes,
            // and rendering hints are ignored.
            // All the above tested with jdk8 and LCMS.
            Graphics g2d = dest.getGraphics();
            g2d.drawImage(src, 0, 0, null);
            g2d.dispose();
            return dest;
        }
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(src, dest);
        return dest;
    }

    @Override
    public COSBase getCOSObject()
    {
        return array;
    }
}
