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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterManager;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.ImageParameters;

/**
 * This class represents an inlined image.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDInlinedImage
{
    private ImageParameters params;
    private byte[] imageData;

    /**
     * This will get the image parameters.
     * 
     * @return The image parameters.
     */
    public ImageParameters getImageParameters()
    {
        return params;
    }

    /**
     * This will set the image parameters for this image.
     * 
     * @param imageParams
     *            The imageParams.
     */
    public void setImageParameters(ImageParameters imageParams)
    {
        params = imageParams;
    }

    /**
     * Get the bytes for the image.
     * 
     * @return The image data.
     */
    public byte[] getImageData()
    {
        return imageData;
    }

    /**
     * Set the bytes that make up the image.
     * 
     * @param value
     *            The image data.
     */
    public void setImageData(byte[] value)
    {
        imageData = value;
    }

    /**
     * This will take the inlined image information and create a java.awt.Image from it.
     * 
     * @return The image that this object represents.
     * 
     * @throws IOException
     *             If there is an error creating the image.
     */
    public BufferedImage createImage() throws IOException
    {
        return createImage(null);
    }

    /**
     * This will take the inlined image information and create a java.awt.Image from it.
     * 
     * @param colorSpaces
     *            The ColorSpace dictionary from the current resources, if any.
     * 
     * @return The image that this object represents.
     * 
     * @throws IOException
     *             If there is an error creating the image.
     */
    public BufferedImage createImage(Map<String, PDColorSpace> colorSpaces) throws IOException
    {
        PDColorSpace pcs = params.getColorSpace(colorSpaces);
        ColorModel colorModel = null;
        if (pcs != null)
        {
            colorModel = pcs.createColorModel(params.getBitsPerComponent());
        }
        else
        {
            byte[] transparentColors = new byte[] { (byte) 0xFF, (byte) 0xFF };
            byte[] colors = null;

            COSArray decode = params.getDecodeValues();
            if (decode != null && decode.getInt(0) == 1)
            {
                colors = new byte[] { (byte) 0xFF, 0 };
            }
            else
            {
                colors = new byte[] { 0, (byte) 0xFF };
            }
            colorModel = new IndexColorModel(1, 2, colors, colors, colors, transparentColors);
        }
        List<String> filters = params.getFilters();
        byte[] finalData = null;
        if (filters == null)
        {
            finalData = getImageData();
        }
        else
        {
            ByteArrayInputStream in = new ByteArrayInputStream(getImageData());
            ByteArrayOutputStream out = new ByteArrayOutputStream(getImageData().length);
            FilterManager filterManager = new FilterManager();
            for (int i = 0; i < filters.size(); i++)
            {
                out.reset();
                Filter filter = filterManager.getFilter((String) filters.get(i));
                filter.decode(in, out, params.getDictionary(), i);
                in = new ByteArrayInputStream(out.toByteArray());
            }
            finalData = out.toByteArray();
        }

        WritableRaster raster = colorModel.createCompatibleWritableRaster(params.getWidth(), params.getHeight());
        DataBuffer rasterBuffer = raster.getDataBuffer();
        if (rasterBuffer instanceof DataBufferByte)
        {
            DataBufferByte byteBuffer = (DataBufferByte) rasterBuffer;
            byte[] data = byteBuffer.getData();
            System.arraycopy(finalData, 0, data, 0, data.length);
        }
        else if (rasterBuffer instanceof DataBufferInt)
        {
            DataBufferInt byteBuffer = (DataBufferInt) rasterBuffer;
            int[] data = byteBuffer.getData();
            for (int i = 0; i < finalData.length; i++)
            {
                data[i] = (finalData[i] + 256) % 256;
            }
        }
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);
        image.setData(raster);
        return image;
    }
}
