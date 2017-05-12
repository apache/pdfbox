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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

/**
 * Reads a sampled image from a PDF file.
 * @author John Hewson
 */
final class SampledImageReader
{
    private static final Log LOG = LogFactory.getLog(SampledImageReader.class);
    
    private SampledImageReader()
    {
    }

    /**
     * Returns an ARGB image filled with the given paint and using the given image as a mask.
     * @param paint the paint to fill the visible portions of the image with
     * @return a masked image filled with the given paint
     * @throws IOException if the image cannot be read
     * @throws IllegalStateException if the image is not a stencil.
     */
    public static BufferedImage getStencilImage(PDImage pdImage, Paint paint) throws IOException
    {
        int width = pdImage.getWidth();
        int height = pdImage.getHeight();

        // compose to ARGB
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = masked.createGraphics();

        // draw the mask
        //g.drawImage(mask, 0, 0, null);

        // fill with paint using src-in
        //g.setComposite(AlphaComposite.SrcIn);
        g.setPaint(paint);
        g.fillRect(0, 0, width, height);
        g.dispose();

        // set the alpha
        WritableRaster raster = masked.getRaster();

        final int[] transparent = new int[4];

        // avoid getting a BufferedImage for the mask to lessen memory footprint.
        // Such masks are always bpc=1 and have no colorspace, but have a decode.
        // (see 8.9.6.2 Stencil Masking)
        try (InputStream iis = pdImage.createInputStream())
        {
            final float[] decode = getDecodeArray(pdImage);
            int value = decode[0] < decode[1] ? 1 : 0;
            int rowLen = width / 8;
            if (width % 8 > 0)
            {
                rowLen++;
            }
            byte[] buff = new byte[rowLen];
            for (int y = 0; y < height; y++)
            {
                int x = 0;
                int readLen = iis.read(buff);
                for (int r = 0; r < rowLen && r < readLen; r++)
                {
                    int byteValue = buff[r];
                    int mask = 128;
                    int shift = 7;
                    for (int i = 0; i < 8; i++)
                    {
                        int bit = (byteValue & mask) >> shift;
                        mask >>= 1;
                        --shift;
                        if (bit == value)
                        {
                            raster.setPixel(x, y, transparent);
                        }
                        x++;
                        if (x == width)
                        {
                            break;
                        }
                    }
                }
                if (readLen != rowLen)
                {
                    LOG.warn("premature EOF, image will be incomplete");
                    break;
                }
            }            
        }

        return masked;
    }

    /**
     * Returns the content of the given image as an AWT buffered image with an RGB color space.
     * If a color key mask is provided then an ARGB image is returned instead.
     * This method never returns null.
     * @param pdImage the image to read
     * @param colorKey an optional color key mask
     * @return content of this image as an RGB buffered image
     * @throws IOException if the image cannot be read
     */
    public static BufferedImage getRGBImage(PDImage pdImage, COSArray colorKey) throws IOException
    {
        if (pdImage.isEmpty())
        {
            throw new IOException("Image stream is empty");
        }

        // get parameters, they must be valid or have been repaired
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int numComponents = colorSpace.getNumberOfComponents();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final int bitsPerComponent = pdImage.getBitsPerComponent();
        final float[] decode = getDecodeArray(pdImage);

        if (width <= 0 || height <= 0)
        {
            throw new IOException("image width and height must be positive");
        }

        if (bitsPerComponent == 1 && colorKey == null && numComponents == 1)
        {
            return from1Bit(pdImage);
        }

        //
        // An AWT raster must use 8/16/32 bits per component. Images with < 8bpc
        // will be unpacked into a byte-backed raster. Images with 16bpc will be reduced
        // in depth to 8bpc as they will be drawn to TYPE_INT_RGB images anyway. All code
        // in PDColorSpace#toRGBImage expects an 8-bit range, i.e. 0-255.
        //
        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, width, height,
                numComponents, new Point(0, 0));
        final float[] defaultDecode = pdImage.getColorSpace().getDefaultDecode(8);
        if (bitsPerComponent == 8 && Arrays.equals(decode, defaultDecode) && colorKey == null)
        {
            // convert image, faster path for non-decoded, non-colormasked 8-bit images
            return from8bit(pdImage, raster);
        }
        return fromAny(pdImage, raster, colorKey);
    }

    private static BufferedImage from1Bit(PDImage pdImage) throws IOException
    {
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final float[] decode = getDecodeArray(pdImage);
        BufferedImage bim = null;
        WritableRaster raster;
        byte[] output;
        if (colorSpace instanceof PDDeviceGray)
        {
            // TYPE_BYTE_GRAY and not TYPE_BYTE_BINARY because this one is handled
            // without conversion to RGB by Graphics.drawImage
            // this reduces the memory footprint, only one byte per pixel instead of three.
            bim = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            raster = bim.getRaster();
        }
        else
        {
            raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, width, height, 1, new Point(0, 0));
        }
        output = ((DataBufferByte) raster.getDataBuffer()).getData();

        // read bit stream
        try (InputStream iis = pdImage.createInputStream())
        {
            final boolean isIndexed = colorSpace instanceof PDIndexed;

            int rowLen = width / 8;
            if (width % 8 > 0)
            {
                rowLen++;
            }

            // read stream
            byte value0;
            byte value1;
            if (isIndexed || decode[0] < decode[1])
            {
                value0 = 0;
                value1 = (byte) 255;
            }
            else
            {
                value0 = (byte) 255;
                value1 = 0;
            }
            byte[] buff = new byte[rowLen];
            int idx = 0;
            for (int y = 0; y < height; y++)
            {
                int x = 0;
                int readLen = iis.read(buff);
                for (int r = 0; r < rowLen && r < readLen; r++)
                {
                    int value = buff[r];
                    int mask = 128;
                    for (int i = 0; i < 8; i++)
                    {
                        int bit = value & mask;
                        mask >>= 1;
                        output[idx++] = bit == 0 ? value0 : value1;
                        x++;
                        if (x == width)
                        {
                            break;
                        }
                    }
                }
                if (readLen != rowLen)
                {
                    LOG.warn("premature EOF, image will be incomplete");
                    break;
                }
            }

            if (bim != null)
            {
                return bim;
            }

            // use the color space to convert the image to RGB
            return colorSpace.toRGBImage(raster);
        }
    }

    // faster, 8-bit non-decoded, non-colormasked image conversion
    private static BufferedImage from8bit(PDImage pdImage, WritableRaster raster)
            throws IOException
    {
        try (InputStream input = pdImage.createInputStream())
        {
            // get the raster's underlying byte buffer
            byte[][] banks = ((DataBufferByte) raster.getDataBuffer()).getBankData();
            final int width = pdImage.getWidth();
            final int height = pdImage.getHeight();
            final int numComponents = pdImage.getColorSpace().getNumberOfComponents();
            byte[] tempBytes = new byte[numComponents * width];
            // compromise between memory and time usage:
            // reading the whole image consumes too much memory
            // reading one pixel at a time makes it slow in our buffering infrastructure 
            int i = 0;
            for (int y = 0; y < height; ++y)
            {
                input.read(tempBytes);
                for (int x = 0; x < width; ++x)
                {
                    for (int c = 0; c < numComponents; c++)
                    {
                        banks[c][i] = tempBytes[x * numComponents + c];
                    }
                    ++i;
                }
            }
            // use the color space to convert the image to RGB
            return pdImage.getColorSpace().toRGBImage(raster);
        }
    }

    // slower, general-purpose image conversion from any image format
    private static BufferedImage fromAny(PDImage pdImage, WritableRaster raster, COSArray colorKey)
            throws IOException
    {
        final PDColorSpace colorSpace = pdImage.getColorSpace();
        final int numComponents = colorSpace.getNumberOfComponents();
        final int width = pdImage.getWidth();
        final int height = pdImage.getHeight();
        final int bitsPerComponent = pdImage.getBitsPerComponent();
        final float[] decode = getDecodeArray(pdImage);

        // read bit stream
        try (ImageInputStream iis = new MemoryCacheImageInputStream(pdImage.createInputStream()))
        {
            final float sampleMax = (float) Math.pow(2, bitsPerComponent) - 1f;
            final boolean isIndexed = colorSpace instanceof PDIndexed;

            // init color key mask
            float[] colorKeyRanges = null;
            BufferedImage colorKeyMask = null;
            if (colorKey != null)
            {
                colorKeyRanges = colorKey.toFloatArray();
                colorKeyMask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            }

            // calculate row padding
            int padding = 0;
            if (width * numComponents * bitsPerComponent % 8 > 0)
            {
                padding = 8 - (width * numComponents * bitsPerComponent % 8);
            }

            // read stream
            byte[] srcColorValues = new byte[numComponents];
            byte[] alpha = new byte[1];
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    boolean isMasked = true;
                    for (int c = 0; c < numComponents; c++)
                    {
                        int value = (int)iis.readBits(bitsPerComponent);

                        // color key mask requires values before they are decoded
                        if (colorKeyRanges != null)
                        {
                            isMasked &= value >= colorKeyRanges[c * 2] &&
                                        value <= colorKeyRanges[c * 2 + 1];
                        }

                        // decode array
                        final float dMin = decode[c * 2];
                        final float dMax = decode[(c * 2) + 1];

                        // interpolate to domain
                        float output = dMin + (value * ((dMax - dMin) / sampleMax));

                        if (isIndexed)
                        {
                            // indexed color spaces get the raw value, because the TYPE_BYTE
                            // below cannot be reversed by the color space without it having
                            // knowledge of the number of bits per component
                            srcColorValues[c] = (byte)Math.round(output);
                        }
                        else
                        {
                            // interpolate to TYPE_BYTE
                            int outputByte = Math.round(((output - Math.min(dMin, dMax)) /
                                    Math.abs(dMax - dMin)) * 255f);

                            srcColorValues[c] = (byte)outputByte;
                        }
                    }
                    raster.setDataElements(x, y, srcColorValues);

                    // set alpha channel in color key mask, if any
                    if (colorKeyMask != null)
                    {
                        alpha[0] = (byte)(isMasked ? 255 : 0);
                        colorKeyMask.getRaster().setDataElements(x, y, alpha);
                    }
                }

                // rows are padded to the nearest byte
                iis.readBits(padding);
            }

            // use the color space to convert the image to RGB
            BufferedImage rgbImage = colorSpace.toRGBImage(raster);

            // apply color mask, if any
            if (colorKeyMask != null)
            {
                return applyColorKeyMask(rgbImage, colorKeyMask);
            }
            else
            {
                return rgbImage;
            }
        }
    }

    // color key mask: RGB + Binary -> ARGB
    private static BufferedImage applyColorKeyMask(BufferedImage image, BufferedImage mask)
            throws IOException
    {
        int width = image.getWidth();
        int height = image.getHeight();

        // compose to ARGB
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        WritableRaster src = image.getRaster();
        WritableRaster dest = masked.getRaster();
        WritableRaster alpha = mask.getRaster();

        float[] rgb = new float[3];
        float[] rgba = new float[4];
        float[] alphaPixel = null;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                src.getPixel(x, y, rgb);

                rgba[0] = rgb[0];
                rgba[1] = rgb[1];
                rgba[2] = rgb[2];
                alphaPixel = alpha.getPixel(x, y, alphaPixel);
                rgba[3] = 255 - alphaPixel[0];

                dest.setPixel(x, y, rgba);
            }
        }

        return masked;
    }

    // gets decode array from dictionary or returns default
    private static float[] getDecodeArray(PDImage pdImage) throws IOException
    {
        final COSArray cosDecode = pdImage.getDecode();
        float[] decode = null;

        if (cosDecode != null)
        {
            int numberOfComponents = pdImage.getColorSpace().getNumberOfComponents();
            if (cosDecode.size() != numberOfComponents * 2)
            {
                if (pdImage.isStencil() && cosDecode.size() >= 2
                        && cosDecode.get(0) instanceof COSNumber
                        && cosDecode.get(1) instanceof COSNumber)
                {
                    float decode0 = ((COSNumber) cosDecode.get(0)).floatValue();
                    float decode1 = ((COSNumber) cosDecode.get(1)).floatValue();
                    if (decode0 >= 0 && decode0 <= 1 && decode1 >= 0 && decode1 <= 1)
                    {
                        LOG.warn("decode array " + cosDecode
                                + " not compatible with color space, using the first two entries");
                        return new float[]
                        {
                            decode0, decode1
                        };
                    }
                }
                LOG.error("decode array " + cosDecode
                        + " not compatible with color space, using default");
            }
            else
            {
                decode = cosDecode.toFloatArray();
            }
        }

        // use color space default
        if (decode == null)
        {
            return pdImage.getColorSpace().getDefaultDecode(pdImage.getBitsPerComponent());
        }

        return decode;
    }
}
