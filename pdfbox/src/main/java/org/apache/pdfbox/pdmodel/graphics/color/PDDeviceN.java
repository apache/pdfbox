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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

/**
 * DeviceN colour spaces may contain an arbitrary number of colour components.
 * DeviceN represents a colour space containing multiple components that correspond to colorants
 * of some target device. As with Separation colour spaces, readers are able to approximate the
 * colorants if they are not available on the current output device, such as a display
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public class PDDeviceN extends PDSpecialColorSpace
{
    // array indexes
    private static final int COLORANT_NAMES = 1;
    private static final int ALTERNATE_CS = 2;
    private static final int TINT_TRANSFORM = 3;
    private static final int DEVICEN_ATTRIBUTES = 4;

    // fields
    private PDColorSpace alternateColorSpace = null;
    private PDFunction tintTransform = null;
    private PDDeviceNAttributes attributes;
    private PDColor initialColor;

    // color conversion cache
    private int numColorants;
    private int[] colorantToComponent;
    private PDColorSpace processColorSpace;
    private PDSeparation[] spotColorSpaces;

    /**
     * Creates a new DeviceN color space.
     */
    public PDDeviceN()
    {
        array = new COSArray();
        array.add(COSName.DEVICEN);

        // empty placeholder
        array.add(COSNull.NULL);
        array.add(COSNull.NULL);
        array.add(COSNull.NULL);
    }

    /**
     * Creates a new DeviceN color space from the given COS array.
     * @param deviceN an array containing the color space information
     */
    public PDDeviceN(COSArray deviceN) throws IOException
    {
        array = deviceN;
        alternateColorSpace = PDColorSpace.create(array.getObject(ALTERNATE_CS));
        tintTransform = PDFunction.create(array.getObject(TINT_TRANSFORM));

        if (array.size() > DEVICEN_ATTRIBUTES)
        {
            attributes = new PDDeviceNAttributes((COSDictionary)array.getObject(DEVICEN_ATTRIBUTES));
        }
        initColorConversionCache();

        // set initial color space
        int n = getNumberOfComponents();
        float[] initial = new float[n];
        for (int i = 0; i < n; i++)
        {
            initial[i] = 1;
        }
        initialColor = new PDColor(initial, this);
    }

    // initializes the color conversion cache
    private void initColorConversionCache() throws IOException
    {
        // there's nothing to cache for non-attribute spaces
        if (attributes == null)
        {
            return;
        }

        // colorant names
        List<String> colorantNames = getColorantNames();
        numColorants = colorantNames.size();

        // process components
        colorantToComponent = new int[numColorants];
        for (int c = 0; c < numColorants; c++)
        {
            colorantToComponent[c] = -1;
        }

        if (attributes.getProcess() != null)
        {
            List<String> components = attributes.getProcess().getComponents();

            // map each colorant to the corresponding process component (if any)
            for (int c = 0; c < numColorants; c++)
            {
                colorantToComponent[c] = components.indexOf(colorantNames.get(c));
            }

            // process color space
            processColorSpace = attributes.getProcess().getColorSpace();
        }

        // spot colorants
        spotColorSpaces = new PDSeparation[numColorants];
        if (attributes.getColorants() != null)
        {
            // spot color spaces
            Map<String, PDSeparation> spotColorants = attributes.getColorants();

            // map each colorant to the corresponding spot color space
            for (int c = 0; c < numColorants; c++)
            {
                String name = colorantNames.get(c);
                PDSeparation spot = spotColorants.get(name);
                if (spot != null)
                {
                    // spot colorant
                    spotColorSpaces[c] = spot;

                    // spot colors may replace process colors with same name
                    // providing that the subtype is not NChannel.
                    if (!isNChannel())
                    {
                        colorantToComponent[c] = -1;
                    }
                }
                else
                {
                    // process colorant
                    spotColorSpaces[c] = null;
                }
            }
        }
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        if (attributes != null)
        {
            return toRGBWithAttributes(raster);
        }
        else
        {
            return toRGBWithTintTransform(raster);
        }
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    private BufferedImage toRGBWithAttributes(WritableRaster raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();

        // white background
        Graphics2D g = rgbImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, width, height);
        g.dispose();

        // look up each colorant
        for (int c = 0; c < numColorants; c++)
        {
            PDColorSpace componentColorSpace;
            if (colorantToComponent[c] >= 0)
            {
                // process color
                componentColorSpace = processColorSpace;
            }
            else if (spotColorSpaces[c] == null)
            {
                // TODO this happens in the Altona Visual test, is there a better workaround?
                // missing spot color, fallback to using tintTransform
                return toRGBWithTintTransform(raster);
            }
            else
            {
                // spot color
                componentColorSpace = spotColorSpaces[c];
            }

            // copy single-component to its own raster in the component color space
            WritableRaster componentRaster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                width, height, componentColorSpace.getNumberOfComponents(), new Point(0, 0));

            int[] samples = new int[numColorants];
            int[] componentSamples = new int[componentColorSpace.getNumberOfComponents()];
            boolean isProcessColorant = colorantToComponent[c] >= 0;
            int componentIndex = colorantToComponent[c];
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    raster.getPixel(x, y, samples);
                    if (isProcessColorant)
                    {
                        // process color
                        componentSamples[componentIndex] = samples[c];
                    }
                    else
                    {
                        // spot color
                        componentSamples[0] = samples[c];
                    }
                    componentRaster.setPixel(x, y, componentSamples);
                }
            }

            // convert single-component raster to RGB
            BufferedImage rgbComponentImage = componentColorSpace.toRGBImage(componentRaster);
            WritableRaster rgbComponentRaster = rgbComponentImage.getRaster();

            // combine the RGB component with the RGB composite raster
            int[] rgbChannel = new int[3];
            int[] rgbComposite = new int[3];
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    rgbComponentRaster.getPixel(x, y, rgbChannel);
                    rgbRaster.getPixel(x, y, rgbComposite);

                    // multiply (blend mode)
                    rgbChannel[0] = rgbChannel[0] * rgbComposite[0] >> 8;
                    rgbChannel[1] = rgbChannel[1] * rgbComposite[1] >> 8;
                    rgbChannel[2] = rgbChannel[2] * rgbComposite[2] >> 8;

                    rgbRaster.setPixel(x, y, rgbChannel);
                }
            }
        }

        return rgbImage;
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    private BufferedImage toRGBWithTintTransform(WritableRaster raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();

        // use the tint transform to convert the sample into
        // the alternate color space (this is usually 1:many)
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();
        int[] rgb = new int[3];
        int numSrcComponents = getColorantNames().size();
        float[] src = new float[numSrcComponents];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, src);

                int[] intSrc = new int[numSrcComponents];
                raster.getPixel(x, y, intSrc);

                // scale to 0..1
                for (int s = 0; s < numSrcComponents; s++)
                {
                    src[s] = src[s] / 255;
                }

                // convert to alternate color space via tint transform
                float[] result = tintTransform.eval(src);
                
                // convert from alternate color space to RGB
                float[] rgbFloat = alternateColorSpace.toRGB(result);
                
                for (int s = 0; s < 3; s++)
                {
                    // scale to 0..255
                    rgb[s] = (int) (rgbFloat[s] * 255f);
                }                

                rgbRaster.setPixel(x, y, rgb);
            }
        }
        return rgbImage;
    }

    @Override
    public float[] toRGB(float[] value) throws IOException
    {
        if (attributes != null)
        {
            return toRGBWithAttributes(value);
        }
        else
        {
            return toRGBWithTintTransform(value);
        }
    }

    private float[] toRGBWithAttributes(float[] value) throws IOException
    {
        float[] rgbValue = new float[] { 1, 1, 1 };

        // look up each colorant
        for (int c = 0; c < numColorants; c++)
        {
            PDColorSpace componentColorSpace;
            if (colorantToComponent[c] >= 0)
            {
                // process color
                componentColorSpace = processColorSpace;
            }
            else if (spotColorSpaces[c] == null)
            {
                // TODO this happens in the Altona Visual test, is there a better workaround?
                // missing spot color, fallback to using tintTransform
                return toRGBWithTintTransform(value);
            }
            else
            {
                // spot color
                componentColorSpace = spotColorSpaces[c];
            }

            // get the single component
            boolean isProcessColorant = colorantToComponent[c] >= 0;
            float[] componentSamples = new float[componentColorSpace.getNumberOfComponents()];
            int componentIndex = colorantToComponent[c];

            if (isProcessColorant)
            {
                // process color
                componentSamples[componentIndex] = value[c];
            }
            else
            {
                // spot color
                componentSamples[0] = value[c];
            }

            // convert single component to RGB
            float[] rgbComponent = componentColorSpace.toRGB(componentSamples);

            // combine the RGB component value with the RGB composite value

            // multiply (blend mode)
            rgbValue[0] *= rgbComponent[0];
            rgbValue[1] *= rgbComponent[1];
            rgbValue[2] *= rgbComponent[2];
        }

        return rgbValue;
    }

    private float[] toRGBWithTintTransform(float[] value) throws IOException
    {
        // use the tint transform to convert the sample into
        // the alternate color space (this is usually 1:many)
        float[] altValue = tintTransform.eval(value);

        // convert the alternate color space to RGB
        return alternateColorSpace.toRGB(altValue);
    }

    /**
     * Returns true if this color space has the NChannel subtype.
     * @return true if subtype is NChannel
     */
    public boolean isNChannel()
    {
        return attributes != null && attributes.isNChannel();
    }

    @Override
    public String getName()
    {
        return COSName.DEVICEN.getName();
    }

    @Override
    public final int getNumberOfComponents()
    {
        return getColorantNames().size();
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        int n = getNumberOfComponents();
        float[] decode = new float[n * 2];
        for (int i = 0; i < n; i++)
        {
            decode[i * 2 + 1] = 1;
        }
        return decode;
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    /**
     * Returns the list of colorants.
     * @return the list of colorants
     */
    public List<String> getColorantNames()
    {
        COSArray names = (COSArray)array.getObject(COLORANT_NAMES);
        return COSArrayList.convertCOSNameCOSArrayToList(names);
    }

    /**
     * Returns the attributes associated with the DeviceN color space.
     * @return the DeviceN attributes
     */
    public PDDeviceNAttributes getAttributes()
    {
        return attributes;
    }

    /**
     * Sets the list of colorants
     * @param names the list of colorants
     */
    public void setColorantNames(List<String> names)
    {
        COSArray namesArray = COSArrayList.convertStringListToCOSNameCOSArray(names);
        array.set(COLORANT_NAMES, namesArray);
    }

    /**
     * Sets the color space attributes.
     * If null is passed in then all attribute will be removed.
     * @param attributes the color space attributes, or null
     */
    public void setAttributes(PDDeviceNAttributes attributes)
    {
        this.attributes = attributes;
        if (attributes == null)
        {
            array.remove(DEVICEN_ATTRIBUTES);
        }
        else
        {
            // make sure array is large enough
            while (array.size() <= DEVICEN_ATTRIBUTES)
            {
                array.add(COSNull.NULL);
            }
            array.set(DEVICEN_ATTRIBUTES, attributes.getCOSDictionary());
        }
    }
 
    /**
     * This will get the alternate color space for this separation.
     *
     * @return The alternate color space.
     *
     * @throws IOException If there is an error getting the alternate color
     * space.
     */
    public PDColorSpace getAlternateColorSpace() throws IOException
    {
        if (alternateColorSpace == null)
        {
            alternateColorSpace = PDColorSpace.create(array.getObject(ALTERNATE_CS));
        }
        return alternateColorSpace;
    }

    /**
     * This will set the alternate color space.
     *
     * @param cs The alternate color space.
     */
    public void setAlternateColorSpace(PDColorSpace cs)
    {
        alternateColorSpace = cs;
        COSBase space = null;
        if (cs != null)
        {
            space = cs.getCOSObject();
        }
        array.set(ALTERNATE_CS, space);
    }

    /**
     * This will get the tint transform function.
     *
     * @return The tint transform function.
     *
     * @throws IOException if there is an error creating the function.
     */
    public PDFunction getTintTransform() throws IOException
    {
        if (tintTransform == null)
        {
            tintTransform = PDFunction.create(array.getObject(TINT_TRANSFORM));
        }
        return tintTransform;
    }

    /**
     * This will set the tint transform function.
     *
     * @param tint The tint transform function.
     */
    public void setTintTransform(PDFunction tint)
    {
        tintTransform = tint;
        array.set(TINT_TRANSFORM, tint);
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getName());
        sb.append('{');
        for (String col : getColorantNames())
        {
            sb.append('\"');
            sb.append(col);
            sb.append("\" ");
        }
        sb.append(alternateColorSpace.getName());
        sb.append(' ');
        sb.append(tintTransform);
        sb.append(' ');
        if (attributes != null)
        {
            sb.append(attributes);
        }
        sb.append('}');
        return sb.toString();
    }
}
