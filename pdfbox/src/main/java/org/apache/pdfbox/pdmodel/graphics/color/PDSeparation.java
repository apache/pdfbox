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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

/**
 * A Separation color space used to specify either additional colorants or for isolating the
 * control of individual colour components of a device colour space for a subtractive device.
 * When such a space is the current colour space, the current colour shall be a single-component
 * value, called a tint, that controls the given colorant or colour components only.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDSeparation extends PDSpecialColorSpace
{
    private final PDColor initialColor = new PDColor(new float[] { 1 }, this);

    // array indexes
    private static final int COLORANT_NAMES = 1;
    private static final int ALTERNATE_CS = 2;
    private static final int TINT_TRANSFORM = 3;

    // fields
    private PDColorSpace alternateColorSpace = null;
    private PDFunction tintTransform = null;

    /**
     * Map used to speed up {@link #toRGB(float[])}. Note that this class contains three maps (this
     * and the two in {@link #toRGBImage(java.awt.image.WritableRaster) } and {@link #toRGBImage2(java.awt.image.WritableRaster)
     * }. The maps use different key intervals. This map here is needed for shading, which produce
     * more than 256 different float values, which we cast to int so that the map can work.
     */
    private Map<Integer, float[]> toRGBMap = null;

    /**
     * Creates a new Separation color space.
     */
    public PDSeparation()
    {
        array = new COSArray();
        array.add(COSName.SEPARATION);
        array.add(COSName.getPDFName(""));
        // add some placeholder
        array.add(COSNull.NULL);
        array.add(COSNull.NULL);
    }

    /**
     * Creates a new Separation color space from a PDF color space array.
     * @param separation an array containing all separation information.
     * @throws IOException if the color space or the function could not be created.
     */
    public PDSeparation(COSArray separation) throws IOException
    {
        array = separation;
        alternateColorSpace = PDColorSpace.create(array.getObject(ALTERNATE_CS));
        tintTransform = PDFunction.create(array.getObject(TINT_TRANSFORM));
    }

    @Override
    public String getName()
    {
        return COSName.SEPARATION.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 1;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value) throws IOException
    {
        if (toRGBMap == null)
        {
            toRGBMap = new HashMap<>();
        }
        int key = (int) (value[0] * 255);
        float[] retval = toRGBMap.get(key);
        if (retval != null)
        {
            return retval;
        }
        float[] altColor = tintTransform.eval(value);
        retval = alternateColorSpace.toRGB(altColor);
        toRGBMap.put(key, retval);
        return retval;
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        if (alternateColorSpace instanceof PDLab)
        {
            // PDFBOX-3622 - regular converter fails for Lab colorspaces
            return toRGBImage2(raster);
        }
        
        // use the tint transform to convert the sample into
        // the alternate color space (this is usually 1:many)
        WritableRaster altRaster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                raster.getWidth(), raster.getHeight(),
                alternateColorSpace.getNumberOfComponents(),
                new Point(0, 0));

        int numAltComponents = alternateColorSpace.getNumberOfComponents();
        int width = raster.getWidth();
        int height = raster.getHeight();
        float[] samples = new float[1];

        Map<Integer, int[]> calculatedValues = new HashMap<>();
        Integer hash;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, samples);
                int[] alt = calculatedValues.get(hash = Float.floatToIntBits(samples[0]));
                if (alt == null)
                {
                    alt = new int[numAltComponents];
                    tintTransform(samples, alt);
                    calculatedValues.put(hash, alt);
                }                
                altRaster.setPixel(x, y, alt);
            }
        }

        // convert the alternate color space to RGB
        return alternateColorSpace.toRGBImage(altRaster);
    }

    // converter that works without using super implementation of toRGBImage()
    private BufferedImage toRGBImage2(WritableRaster raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();
        float[] samples = new float[1];

        Map<Integer, int[]> calculatedValues = new HashMap<>();
        Integer hash;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, samples);
                int[] rgb = calculatedValues.get(hash = Float.floatToIntBits(samples[0]));
                if (rgb == null)
                {
                    samples[0] /= 255;
                    float[] altColor = tintTransform.eval(samples);
                    float[] fltab = alternateColorSpace.toRGB(altColor);
                    rgb = new int[3];
                    rgb[0] = (int) (fltab[0] * 255);
                    rgb[1] = (int) (fltab[1] * 255);
                    rgb[2] = (int) (fltab[2] * 255);
                    calculatedValues.put(hash, rgb);
                }
                rgbRaster.setPixel(x, y, rgb);
            }
        }
        return rgbImage;
    }

    protected void tintTransform(float[] samples, int[] alt) throws IOException
    {
        samples[0] /= 255; // 0..1
        float[] result = tintTransform.eval(samples);
        for (int s = 0; s < alt.length; s++)
        {
            // scale to 0..255
            alt[s] = (int) (result[s] * 255);
        }
    }

    /**
     * Returns the colorant name.
     * @return the name of the colorant
     */
    public PDColorSpace getAlternateColorSpace()
    {
       return alternateColorSpace;
    }

    /**
     * Returns the colorant name.
     * @return the name of the colorant
     */
    public String getColorantName()
    {
        COSName name = (COSName)array.getObject(COLORANT_NAMES);
        return name.getName();
    }

    /**
     * Sets the colorant name.
     * @param name the name of the colorant
     */
    public void setColorantName(String name)
    {
        array.set(1, COSName.getPDFName(name));
    }

    /**
     * Sets the alternate color space.
     * @param colorSpace The alternate color space.
     */
    public void setAlternateColorSpace(PDColorSpace colorSpace)
    {
        alternateColorSpace = colorSpace;
        COSBase space = null;
        if (colorSpace != null)
        {
            space = colorSpace.getCOSObject();
        }
        array.set(ALTERNATE_CS, space);
    }

    /**
     * Sets the tint transform function.
     * @param tint the tint transform function
     */
    public void setTintTransform(PDFunction tint)
    {
        tintTransform = tint;
        array.set(TINT_TRANSFORM, tint);
    }

    @Override
    public String toString()
    {
        return getName() + "{" +
                "\"" + getColorantName() + "\"" + " " +
                alternateColorSpace.getName() + " " +
                tintTransform + "}";
    }
}
