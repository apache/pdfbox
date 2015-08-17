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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
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
     */
    public PDIndexed()
    {
        array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(255));
        array.add(org.apache.pdfbox.cos.COSNull.NULL);
    }

    /**
     * Creates a new Indexed color space from the given PDF array.
     * @param indexedArray the array containing the indexed parameters
     */
    public PDIndexed(COSArray indexedArray) throws IOException
    {
        array = indexedArray;
        baseColorSpace = PDColorSpace.create(array.getObject(1));
        readColorTable();
        initRgbColorTable();
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
        WritableRaster baseRaster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                actualMaxIndex + 1, 1, numBaseComponents, new Point(0, 0));

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
        if (value.length > 1)
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

        int[] src = new int[1];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, src);

                // lookup
                int index = Math.min(src[0], actualMaxIndex);
                rgbRaster.setPixel(x, y, rgbColorTable[index]);
            }
        }

        return rgbImage;
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
    private byte[] getLookupData() throws IOException
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
        return lookupData;
    }

    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    private void readColorTable() throws IOException
    {
        byte[] lookupData = getLookupData();
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
     */
    public void setBaseColorSpace(PDColorSpace base)
    {
        array.set(1, base.getCOSObject());
        baseColorSpace = base;
    }

    /**
     * Sets the highest value that is allowed. This cannot be higher than 255.
     * @param high the highest value for the lookup table
     */
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
