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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.IOUtils;

/**
 * This class represents an Indexed color space.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDIndexed extends PDColorSpace
{

    /**
     * The name of this color space.
     */
    public static final String NAME = "Indexed";

    /**
     * The abbreviated name of this color space.
     */
    public static final String ABBREVIATED_NAME = "I";

    private COSArray array;

    private PDColorSpace baseColorspace = null;
    private ColorModel baseColorModel = null;

    /**
     * The lookup data as byte array.
     */
    private byte[] lookupData;

    private byte[] indexedColorValues;
    private int indexNumOfComponents;
    private int maxIndex;

    /**
     * Indexed color values are always 8bit based.
     */
    private static final int INDEXED_BPC = 8;

    /**
     * Constructor, default DeviceRGB, hival 255.
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
     * Constructor.
     * 
     * @param indexedArray The array containing the indexed parameters
     */
    public PDIndexed(COSArray indexedArray)
    {
        array = indexedArray;
    }

    /**
     * This will return the number of color components. This will return the number of color components in the base
     * color.
     * 
     * @return The number of components in this color space.
     * 
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return getBaseColorSpace().getNumberOfComponents();
    }

    /**
     * This will return the name of the color space.
     * 
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * Create a Java colorspace for this colorspace.
     * 
     * @return A color space that can be used for Java AWT operations.
     * 
     * @throws IOException If there is an error creating the color space.
     */
    protected ColorSpace createColorSpace() throws IOException
    {
        return getBaseColorSpace().getJavaColorSpace();
    }

    /**
     * Create a Java color model for this colorspace.
     * 
     * @param bpc The number of bits per component.
     * 
     * @return A color model that can be used for Java AWT operations.
     * 
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel(int bpc) throws IOException
    {
        return createColorModel(bpc, -1);
    }

    /**
     * Create a Java color model for this colorspace including the given mask value.
     * 
     * @param bpc The number of bits per component of the indexed color model.
     * @param mask the mask value, -1 indicates no mask
     * 
     * @return A color model that can be used for Java AWT operations.
     * 
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel(int bpc, int mask) throws IOException
    {
        ColorModel colorModel = getBaseColorModel(INDEXED_BPC);
        calculateIndexedColorValues(colorModel, bpc);
        if (mask > -1)
        {
            return new IndexColorModel(bpc, maxIndex + 1, indexedColorValues, 0, colorModel.hasAlpha(), mask);
        }
        else
        {
            return new IndexColorModel(bpc, maxIndex + 1, indexedColorValues, 0, colorModel.hasAlpha());
        }
    }

    /**
     * This will get the color space that acts as the index for this color space.
     * 
     * @return The base color space.
     * 
     * @throws IOException If there is error creating the base color space.
     */
    public PDColorSpace getBaseColorSpace() throws IOException
    {
        if (baseColorspace == null)
        {
            COSBase base = array.getObject(1);
            baseColorspace = PDColorSpaceFactory.createColorSpace(base);
        }
        return baseColorspace;
    }

    /**
     * This will set the base color space.
     * 
     * @param base The base color space to use as the index.
     */
    public void setBaseColorSpace(PDColorSpace base)
    {
        array.set(1, base.getCOSObject());
        baseColorspace = base;
    }

    /**
     * Get the highest value for the lookup.
     * 
     * @return The hival entry.
     */
    public int getHighValue()
    {
        return ((COSNumber) array.getObject(2)).intValue();
    }

    /**
     * This will set the highest value that is allowed. This cannot be higher than 255.
     * 
     * @param high The highest value for the lookup table.
     */
    public void setHighValue(int high)
    {
        array.set(2, high);
    }

    /**
     * This will perform a lookup into the color lookup table.
     * 
     * @param lookupIndex The zero-based index into the table, should not exceed the high value.
     * @param componentNumber The component number, probably 1,2,3,3.
     * 
     * @return The value that was from the lookup table.
     * 
     * @throws IOException If there is an error looking up the color.
     */
    public int lookupColor(int lookupIndex, int componentNumber) throws IOException
    {
        PDColorSpace baseColor = getBaseColorSpace();
        byte[] data = getLookupData();
        int numberOfComponents = baseColor.getNumberOfComponents();
        return (data[lookupIndex * numberOfComponents + componentNumber] + 256) % 256;
    }

    /**
     * Get the lookup data table.
     * 
     * @return a byte array containing the the lookup data.
     * @throws IOException if an error occurs.
     */
    public byte[] getLookupData() throws IOException
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
                // Data will be small so just load the whole thing into memory for
                // easier processing
                COSStream lookupStream = (COSStream) lookupTable;
                InputStream input = lookupStream.getUnfilteredStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int amountRead;
                while ((amountRead = input.read(buffer, 0, buffer.length)) != -1)
                {
                    output.write(buffer, 0, amountRead);
                }
                lookupData = output.toByteArray();
                IOUtils.closeQuietly(input);
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

    /**
     * This will set a color in the color lookup table.
     * 
     * @param lookupIndex The zero-based index into the table, should not exceed the high value.
     * @param componentNumber The component number, probably 1,2,3,3.
     * @param color The color that will go into the table.
     * 
     * @throws IOException If there is an error looking up the color.
     */
    public void setLookupColor(int lookupIndex, int componentNumber, int color) throws IOException
    {
        PDColorSpace baseColor = getBaseColorSpace();
        int numberOfComponents = baseColor.getNumberOfComponents();
        byte[] data = getLookupData();
        data[lookupIndex * numberOfComponents + componentNumber] = (byte) color;
        COSString string = new COSString(data);
        array.set(3, string);
    }

    /**
     * Returns the components of the color for the given index.
     * 
     * @param index the index of the color value
     * @return COSArray with the color components
     * @throws IOException If the tint function is not supported
     */
    public float[] calculateColorValues(int index) throws IOException
    {
        // TODO bpc != 8 ??
        calculateIndexedColorValues(getBaseColorModel(INDEXED_BPC), 8);
        float[] colorValues = null;
        if (index < maxIndex)
        {
            int bufferIndex = index * indexNumOfComponents;
            colorValues = new float[indexNumOfComponents];
            for (int i = 0; i < indexNumOfComponents; i++)
            {
                colorValues[i] = (float) indexedColorValues[bufferIndex + i];
            }
        }
        return colorValues;
    }

    private ColorModel getBaseColorModel(int bpc) throws IOException
    {
        if (baseColorModel == null)
        {
            baseColorModel = getBaseColorSpace().createColorModel(bpc);
            if (baseColorModel.getTransferType() != DataBuffer.TYPE_BYTE)
            {
                throw new IOException("Not implemented");
            }
        }
        return baseColorModel;
    }

    private void calculateIndexedColorValues(ColorModel colorModel, int bpc) throws IOException
    {
        if (indexedColorValues == null)
        {
            // number of possible color values in the target color space
            int numberOfColorValues = 1 << bpc;
            // number of indexed color values
            int highValue = getHighValue();
            // choose the correct size, sometimes there are more indexed values than needed
            // and sometimes there are fewer indexed value than possible
            maxIndex = Math.min(numberOfColorValues - 1, highValue);
            byte[] index = getLookupData();
            // despite all definitions there may be less values within the lookup data
            int numberOfColorValuesFromIndex = (index.length / baseColorModel.getNumComponents()) - 1;
            maxIndex = Math.min(maxIndex, numberOfColorValuesFromIndex);
            // does the colorspace have an alpha channel?
            boolean hasAlpha = baseColorModel.hasAlpha();
            indexNumOfComponents = 3 + (hasAlpha ? 1 : 0);
            int buffersize = (maxIndex + 1) * indexNumOfComponents;
            indexedColorValues = new byte[buffersize];
            byte[] inData = new byte[baseColorModel.getNumComponents()];
            int bufferIndex = 0;
            for (int i = 0; i <= maxIndex; i++)
            {
                System.arraycopy(index, i * inData.length, inData, 0, inData.length);
                // calculate RGB values
                indexedColorValues[bufferIndex] = (byte) colorModel.getRed(inData);
                indexedColorValues[bufferIndex + 1] = (byte) colorModel.getGreen(inData);
                indexedColorValues[bufferIndex + 2] = (byte) colorModel.getBlue(inData);
                if (hasAlpha)
                {
                    indexedColorValues[bufferIndex + 3] = (byte) colorModel.getAlpha(inData);
                }
                bufferIndex += indexNumOfComponents;
            }
        }
    }

}
