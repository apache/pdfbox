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
package org.apache.pdfbox.filter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Decompresses data encoded using a DCT (discrete cosine transform)
 * technique based on the JPEG standard.
 *
 * @author John Hewson
 */
final class DCTFilter extends Filter
{
    private static final Log LOG = LogFactory.getLog(DCTFilter.class);

    private static final int POS_TRANSFORM = 11;
    private static final String ADOBE = "Adobe";

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        ImageReader reader = findImageReader("JPEG", "a suitable JAI I/O image filter is not installed");
        try (ImageInputStream iis = ImageIO.createImageInputStream(encoded))
        {

            // skip one LF if there
            if (iis.read() != 0x0A)
            {
                iis.seek(0);
            }
            
            reader.setInput(iis);
            
            String numChannels = getNumChannels(reader);

            // get the raster using horrible JAI workarounds
            ImageIO.setUseCache(false);
            Raster raster;

            // Strategy: use read() for RGB or "can't get metadata"
            // use readRaster() for CMYK and gray and as fallback if read() fails 
            // after "can't get metadata" because "no meta" file was CMYK
            if ("3".equals(numChannels) || numChannels.isEmpty())
            {
                try
                {
                    // I'd like to use ImageReader#readRaster but it is buggy and can't read RGB correctly
                    BufferedImage image = reader.read(0);
                    raster = image.getRaster();
                }
                catch (IIOException e)
                {
                    // JAI can't read CMYK JPEGs using ImageReader#read or ImageIO.read but
                    // fortunately ImageReader#readRaster isn't buggy when reading 4-channel files
                    raster = reader.readRaster(0, null);
                }
            }
            else
            {
                // JAI can't read CMYK JPEGs using ImageReader#read or ImageIO.read but
                // fortunately ImageReader#readRaster isn't buggy when reading 4-channel files
                raster = reader.readRaster(0, null);
            }

            // special handling for 4-component images
            if (raster.getNumBands() == 4)
            {
                // get APP14 marker
                Integer transform;
                try
                {
                    transform = getAdobeTransform(reader.getImageMetadata(0));
                }
                catch (IIOException | NegativeArraySizeException e)
                {
                    // we really tried asking nicely, now we're using brute force.
                    transform = getAdobeTransformByBruteForce(iis);
                }
                int colorTransform = transform != null ? transform : 0;

                // 0 = Unknown (RGB or CMYK), 1 = YCbCr, 2 = YCCK
                switch (colorTransform)
                {
                    case 0:
                        // already CMYK
                        break;
                    case 1:
                        // TODO YCbCr
                        LOG.warn("YCbCr JPEGs not implemented");
                        break;
                    case 2:
                        raster = fromYCCKtoCMYK(raster);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown colorTransform");
                }
            }
            else if (raster.getNumBands() == 3)
            {
                // BGR to RGB
                raster = fromBGRtoRGB(raster);
            }

            DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
            decoded.write(dataBuffer.getData());
        }
        finally
        {
            reader.dispose();
        }
        return new DecodeResult(parameters);
    }

    // reads the APP14 Adobe transform tag and returns its value, or 0 if unknown
    private Integer getAdobeTransform(IIOMetadata metadata)
    {
        Element tree = (Element)metadata.getAsTree("javax_imageio_jpeg_image_1.0");
        Element markerSequence = (Element)tree.getElementsByTagName("markerSequence").item(0);
        NodeList app14AdobeNodeList = markerSequence.getElementsByTagName("app14Adobe");
        if (app14AdobeNodeList != null && app14AdobeNodeList.getLength() > 0)
        {
            Element adobe = (Element) app14AdobeNodeList.item(0);
            return Integer.parseInt(adobe.getAttribute("transform"));
        }
        return 0;
    }
        
    // See in https://github.com/haraldk/TwelveMonkeys
    // com.twelvemonkeys.imageio.plugins.jpeg.AdobeDCT class for structure of APP14 segment
    private int getAdobeTransformByBruteForce(ImageInputStream iis) throws IOException
    {
        int a = 0;
        iis.seek(0);
        int by;
        while ((by = iis.read()) != -1)
        {
            if (ADOBE.charAt(a) == by)
            {
                ++a;
                if (a != ADOBE.length())
                {
                    continue;
                }
                // match
                a = 0;
                long afterAdobePos = iis.getStreamPosition();
                iis.seek(iis.getStreamPosition() - 9);
                int tag = iis.readUnsignedShort();
                if (tag != 0xFFEE)
                {
                    iis.seek(afterAdobePos);
                    continue;
                }
                int len = iis.readUnsignedShort();
                if (len >= POS_TRANSFORM + 1)
                {
                    byte[] app14 = new byte[Math.max(len, POS_TRANSFORM + 1)];
                    if (iis.read(app14) >= POS_TRANSFORM + 1)
                    {
                        return app14[POS_TRANSFORM];
                    }
                }
            }
            else
            {
                a = 0;
            }
        }
        return 0;
    }

    // converts YCCK image to CMYK. YCCK is an equivalent encoding for
    // CMYK data, so no color management code is needed here, nor does the
    // PDF color space have to be consulted
    private WritableRaster fromYCCKtoCMYK(Raster raster)
    {
        WritableRaster writableRaster = raster.createCompatibleWritableRaster();

        int[] value = new int[4];
        for (int y = 0, height = raster.getHeight(); y < height; y++)
        {
            for (int x = 0, width = raster.getWidth(); x < width; x++)
            {
                raster.getPixel(x, y, value);

                // 4-channels 0..255
                float Y = value[0];
                float Cb = value[1];
                float Cr = value[2];
                float K = value[3];

                // YCCK to RGB, see http://software.intel.com/en-us/node/442744
                int r = clamp(Y + 1.402f * Cr - 179.456f);
                int g = clamp(Y - 0.34414f * Cb - 0.71414f * Cr + 135.45984f);
                int b = clamp(Y + 1.772f * Cb - 226.816f);

                // naive RGB to CMYK
                int cyan = 255 - r;
                int magenta = 255 - g;
                int yellow = 255 - b;

                // update new raster
                value[0] = cyan;
                value[1] = magenta;
                value[2] = yellow;
                value[3] = (int)K;
                writableRaster.setPixel(x, y, value);
            }
        }
        return writableRaster;
    }

    // converts from BGR to RGB
    private WritableRaster fromBGRtoRGB(Raster raster)
    {
        WritableRaster writableRaster = raster.createCompatibleWritableRaster();

        int width = raster.getWidth();
        int height = raster.getHeight();
        int w3 = width * 3;
        int[] tab = new int[w3];
        //BEWARE: handling the full image at a time is slower than one line at a time        
        for (int y = 0; y < height; y++)
        {
            raster.getPixels(0, y, width, 1, tab);
            for (int off = 0; off < w3; off += 3)
            {
                int tmp = tab[off];
                tab[off] = tab[off + 2];
                tab[off + 2] = tmp;
            }
            writableRaster.setPixels(0, y, width, 1, tab);
        }
        return writableRaster;
    }
    
    // returns the number of channels as a string, or an empty string if there is an error getting the meta data
    private String getNumChannels(ImageReader reader)
    {
        try
        {
            IIOMetadata imageMetadata = reader.getImageMetadata(0);
            if (imageMetadata == null)
            {
                return "";
            }
            IIOMetadataNode metaTree = (IIOMetadataNode) imageMetadata.getAsTree("javax_imageio_1.0");
            Element numChannelsItem = (Element) metaTree.getElementsByTagName("NumChannels").item(0);
            if (numChannelsItem == null)
            {
                return "";
            }
            return numChannelsItem.getAttribute("value");
        }
        catch (IOException | NegativeArraySizeException e)
        {
            return "";
        }
    }    

    // clamps value to 0-255 range
    private int clamp(float value)
    {
        return (int)((value < 0) ? 0 : ((value > 255) ? 255 : value));
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("DCTFilter encoding not implemented, use the JPEGFactory methods instead");
    }
}
