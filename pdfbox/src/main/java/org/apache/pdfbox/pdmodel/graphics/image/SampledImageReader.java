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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDCalGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDCalRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDJPXColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDLab;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * Reads a sampled image from a PDF file.
 * @author John Hewson
 */
final class SampledImageReader
{
    private static final Log LOG = LogFactory.getLog(SampledImageReader.class);

    /**
     * Returns an ARGB image filled with the given paint and using the given image as a mask.
     * @param paint the paint to fill the visible portions of the image with
     * @return a masked image filled with the given paint
     * @throws IOException if the image cannot be read
     * @throws IllegalStateException if the image is not a stencil.
     */
    public static BufferedImage getStencilImage(PDImage pdImage, Paint paint) throws IOException
    {
        // get mask (this image)
        BufferedImage mask = getRGBImage(pdImage);

        // compose to ARGB
        BufferedImage masked = new BufferedImage(mask.getWidth(), mask.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = masked.createGraphics();

        // draw the mask
        //g.drawImage(mask, 0, 0, null);

        // fill with paint using src-in
        //g.setComposite(AlphaComposite.SrcIn);
        g.setPaint(paint);
        g.fillRect(0, 0, mask.getWidth(), mask.getHeight());
        g.dispose();

        // set the alpha
        int width = masked.getWidth();
        int height = masked.getHeight();
        WritableRaster raster = masked.getRaster();
        WritableRaster alpha = mask.getRaster();

        float[] rgba = new float[4];
        final float[] transparent = new float[4];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, rgba);

                if (alpha.getPixel(x, y, (float[])null)[0] == 255)
                {
                    raster.setPixel(x, y, transparent);
                }
                else
                {
                    raster.setPixel(x, y, rgba);
                }
            }
        }

        return masked;
    }

    /**
     * Returns the content of the given image as an AWT buffered image with an RGB color space.
     * This method never returns null.
     * @return content of this image as an RGB buffered image
     * @throws IOException if the image cannot be read
     */
    public static BufferedImage getRGBImage(PDImage pdImage) throws IOException
    {
        if (pdImage.getStream().getLength() == 0)
        {
            LOG.warn("Image has empty stream");
            return new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
        }

        // get parameters
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int numComponents = colorSpace.getNumberOfComponents();
        final int width = pdImage.getWidth();                         // todo: what if -1?
        final int height = pdImage.getHeight();                       // todo: what if -1?
        final int bitsPerComponent = pdImage.getBitsPerComponent();   // todo: what if -1?
        final float[] decode = getDecodeArray(pdImage);

        /*
         * An AWT raster must use 8/16/32 bits per component. Images with < 8bpc
         * will be unpacked into a byte-backed raster. Images with 16bpc will be reduced
         * in depth to 8bpc as they will be drawn to TYPE_INT_RGB images anyway. All code
         * in PDColorSpace#toRGBImage expects and 8-bit range, i.e. 0-255.
         */
        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, width, height,
                numComponents, new Point(0, 0));

        // read bit stream
        ImageInputStream iis = null;
        try
        {
            iis = new MemoryCacheImageInputStream(pdImage.getStream().createInputStream());
            final float sampleMax = (float)Math.pow(2, bitsPerComponent) - 1f;

            int padding = 0;
            if (width * numComponents * bitsPerComponent % 8 > 0)
            {
                padding = 8 - (width * numComponents * bitsPerComponent % 8);
            }

            byte[] srcColorValues = new byte[numComponents];
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    for (int c = 0; c < numComponents; c++)
                    {
                        int value = (int)iis.readBits(bitsPerComponent);

                        // decode array
                        final float dMin = decode[c * 2];
                        final float dMax = decode[(c * 2) + 1];

                        // interpolate to domain
                        float output = dMin + (value * ((dMax - dMin) / sampleMax));

                        // interpolate to TYPE_BYTE
                        int outputByte = Math.round(((output - Math.min(dMin, dMax)) /
                                                     Math.abs(dMax - dMin)) * 255f);

                        srcColorValues[c] = (byte)outputByte;
                    }

                    raster.setDataElements(x, y, srcColorValues);
                }

                // rows are padded to the nearest byte
                iis.readBits(padding);
            }

            // use the color space to convert the image to RGB
            return colorSpace.toRGBImage(raster);
        }
        finally
        {
            if (iis != null)
            {
                iis.close();
            }
        }
    }

    // gets decode array from dictionary or returns default
    private static float[] getDecodeArray(PDImage pdImage) throws IOException
    {
        final COSArray cosDecode = pdImage.getDecode();
        float[] decode = null;

        if (cosDecode != null)
        {
            decode = cosDecode.toFloatArray();

            // if ImageMask is true then decode must be [0 1] or [1 0]
            if (pdImage.isStencil() && (decode.length != 2 ||
                decode[0] < 0 || decode[0] > 1 ||
                decode[1] < 0 || decode[1] > 1))
            {
                LOG.warn("Ignored invalid decode array: not compatible with ImageMask");
                decode = null;
            }

            // JPX: decode shall be ignored, except when the image is treated as a mask
            if (pdImage.getStream().getFilters() != null &&
                pdImage.getStream().getFilters().contains(COSName.JPX_DECODE) &&
               !pdImage.isStencil())
            {
                decode = null;
            }

            // otherwise, its length shall be twice the number of colour
            // components required by ColorSpace
            int n = pdImage.getColorSpace().getNumberOfComponents();
            if (decode != null && decode.length != n * 2)
            {
                LOG.warn("Ignored invalid decode array: not compatible with color space");
                decode = null;
            }
        }

        // use color space default
        if (decode == null)
        {
            return pdImage.getColorSpace().getDefaultDecode();
        }

        return decode;
    }
}
