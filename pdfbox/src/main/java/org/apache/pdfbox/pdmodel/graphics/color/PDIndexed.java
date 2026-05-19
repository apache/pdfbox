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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * An Indexed colour space specifies that an area is to be painted using a colour table
 * of arbitrary colours from another color space.
 * 
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDIndexed extends PDSpecialColorSpace
{
    private final PDColor initialColor = new PDColor(new float[] { 0 }, this);

    private PDColorSpace baseColorSpace = null;

    // cached lookup data
    private byte[] lookupData;
    private float[][] colorTable;
    private int actualMaxIndex;
    private int[][] rgbColorTable;

    /**
     * Creates a new Indexed color space.
     * Default DeviceRGB, hival 255.
     * 
     * @deprecated This will be removed in 4.0. If you need it, please contact us.
     */
    @Deprecated
    public PDIndexed()
    {
        array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(255));
        array.add(org.apache.pdfbox.cos.COSNull.NULL);
    }

    /**
     * Creates a new indexed color space from the given PDF array.
     * 
     * @param indexedArray the array containing the indexed parameters
     * @throws IOException if the colorspace could not be created
     */
    public PDIndexed(COSArray indexedArray) throws IOException
    {
        this(indexedArray, null);
    }

    /**
     * Creates a new indexed color space from the given PDF array.
     * 
     * @param indexedArray the array containing the indexed parameters
     * @param resources the resources, can be null. Allows to use its cache for the colorspace.
     * @throws IOException if the colorspace could not be created
     */
    public PDIndexed(COSArray indexedArray, PDResources resources) throws IOException
    {
        array = indexedArray;
        // don't call getObject(1), we want to pass a reference if possible
        // to profit from caching (PDFBOX-4149)
        baseColorSpace = PDColorSpace.create(array.get(1), resources);
        readColorTable();
        initRgbColorTable();
    }

    /**
     * Factory method to create a new indexed color space.
     * 
     * @param base base colorspace, mandantory has to be != null
     * @param hival maximum valid index value for lookup data
     * @param lookupData array for lookup data, mandantory has to be != null
     * 
     * @return an instance of PDIndedex initialized with the given values
     * 
     * @throws IOException if the colorspace could not be created
     */
    public static PDIndexed create(PDColorSpace base, int hival, byte[] lookupData)
            throws IOException
    {
        if (base == null)
        {
            throw new IllegalArgumentException("base must not be null");
        }
        if (lookupData == null)
        {
            throw new IllegalArgumentException("lookupData must not be null");
        }
        if (hival < 0 || hival > 255)
        {
            throw new IllegalArgumentException(" hival has to be a positive value <= 255");
        }
        int expected = (hival + 1) * base.getNumberOfComponents();
        if (lookupData.length < expected)
        {
            throw new IllegalArgumentException("lookupData too short: expected at least " + expected
                    + " bytes ((hival+1) * components), got " + lookupData.length);
        }
        PDIndexed pdIndexed = new PDIndexed();
        pdIndexed.baseColorSpace = base;
        pdIndexed.array.set(1, base.getCOSObject());
        pdIndexed.array.set(2, hival);
        pdIndexed.lookupData = Arrays.copyOf(lookupData, lookupData.length);
        COSString cosLookupData = new COSString(pdIndexed.lookupData, true);
        pdIndexed.array.set(3, cosLookupData);
        pdIndexed.readColorTable();
        pdIndexed.initRgbColorTable();
        return pdIndexed;
    }

    @Override
    public String getName()
    {
        return COSName.INDEXED.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 1;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, (float)Math.pow(2, bitsPerComponent) - 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    private void initRgbColorTable() throws IOException
    {
        int numBaseComponents = baseColorSpace.getNumberOfComponents();

        // convert the color table into a 1-row BufferedImage in the base color space,
        // using a writable raster for high performance
        WritableRaster baseRaster;
        try
        {
            baseRaster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                    actualMaxIndex + 1, 1, numBaseComponents, new Point(0, 0));
        }
        catch (IllegalArgumentException ex)
        {
            // PDFBOX-4503: when stream is empty or null
            throw new IOException(ex);
        }

        int[] base = new int[numBaseComponents];
        for (int i = 0, n = actualMaxIndex; i <= n; i++)
        {
            for (int c = 0; c < numBaseComponents; c++)
            {
                base[c] = (int)(colorTable[i][c] * 255f);
            }
            baseRaster.setPixel(i, 0, base);
        }

        // convert the base image to RGB
        BufferedImage rgbImage = baseColorSpace.toRGBImage(baseRaster);
        WritableRaster rgbRaster = rgbImage.getRaster();

        // build an RGB lookup table from the raster
        rgbColorTable = new int[actualMaxIndex + 1][3];
        int[] nil = null;

        for (int i = 0, n = actualMaxIndex; i <= n; i++)
        {
            rgbColorTable[i] = rgbRaster.getPixel(i, 0, nil);
        }
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    @Override
    public float[] toRGB(float[] value)
    {
        if (value.length != 1)
        {
            throw new IllegalArgumentException("Indexed color spaces must have one color value");
        }
        
        // scale and clamp input value
        int index = Math.round(value[0]);
        index = Math.max(index, 0);
        index = Math.min(index, actualMaxIndex);

        // lookup rgb
        int[] rgb = rgbColorTable[index];
        return new float[] { rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f };
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        // use lookup table
        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();

        int[] src = new int[width];
        for (int y = 0; y < height; y++)
        {
            raster.getPixels(0, y, width, 1, src);
            for (int x = 0; x < width; x++)
            {
                // lookup
                int index = Math.min(src[x], actualMaxIndex);
                rgbRaster.setPixel(x, y, rgbColorTable[index]);
            }
        }

        return rgbImage;
    }

    @Override
    public BufferedImage toRawImage(WritableRaster raster)
    {
        // We can only convert sRGB index colorspaces, depending on the base colorspace
        if (baseColorSpace instanceof PDICCBased && ((PDICCBased) baseColorSpace).isSRGB())
        {
            byte[] r = new byte[colorTable.length];
            byte[] g = new byte[colorTable.length];
            byte[] b = new byte[colorTable.length];
            for (int i = 0; i < colorTable.length; i++)
            {
                r[i] = (byte) ((int) (colorTable[i][0] * 255) & 0xFF);
                g[i] = (byte) ((int) (colorTable[i][1] * 255) & 0xFF);
                b[i] = (byte) ((int) (colorTable[i][2] * 255) & 0xFF);
            }
            ColorModel colorModel = new IndexColorModel(8, colorTable.length, r, g, b);
            return new BufferedImage(colorModel, raster, false, null);
        }

        // We can't handle all other cases at the moment.
        return null;
    }

    /**
     * Returns the base color space.
     * @return the base color space.
     */
    public PDColorSpace getBaseColorSpace()
    {
        return baseColorSpace;
    }

    // returns "hival" array element
    private int getHival()
    {
        return ((COSNumber) array.getObject(2)).intValue();
    }

    // reads the lookup table data from the array
    private void readLookupData() throws IOException
    {
        if (lookupData == null)
        {
            COSBase lookupTable = array.getObject(3);
            if (lookupTable instanceof COSString)
            {
                lookupData = ((COSString) lookupTable).getBytes();
            }
            else if (lookupTable instanceof COSStream)
            {
                lookupData = new PDStream((COSStream)lookupTable).toByteArray();
            }
            else if (lookupTable == null)
            {
                lookupData = new byte[0];
            }
            else
            {
                throw new IOException("Error: Unknown type for lookup table " + lookupTable);
            }
        }
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    private void readColorTable() throws IOException
    {
        readLookupData();

        int maxIndex = Math.min(getHival(), 255);
        int numComponents = baseColorSpace.getNumberOfComponents();

        // some tables are too short
        if (lookupData.length / numComponents < maxIndex + 1)
        {
            maxIndex = lookupData.length / numComponents - 1;
        }
        actualMaxIndex = maxIndex;  // TODO "actual" is ugly, tidy this up

        colorTable = new float[maxIndex + 1][numComponents];
        for (int i = 0, offset = 0; i <= maxIndex; i++)
        {
            for (int c = 0; c < numComponents; c++)
            {
                colorTable[i][c] = (lookupData[offset] & 0xff) / 255f;
                offset++;
            }
        }
    }

    /**
     * Sets the base color space.
     * @param base the base color space
     *
     * @deprecated This will be removed in 4.0. If you need it, please contact us.
     */
    @Deprecated
    public void setBaseColorSpace(PDColorSpace base)
    {
        array.set(1, base.getCOSObject());
        baseColorSpace = base;
    }

    /**
     * Sets the highest value that is allowed. This cannot be higher than 255.
     * @param high the highest value for the lookup table
     *
     * @deprecated This will be removed in 4.0. If you need it, please contact us.
     */
    @Deprecated
    public void setHighValue(int high)
    {
        array.set(2, high);
    }

    @Override
    public String toString()
    {
        return "Indexed{base:" + baseColorSpace + " " +
                "hival:" + getHival() + " " +
                "lookup:(" + colorTable.length + " entries)}";
    }
}
