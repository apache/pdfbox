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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDJPXColorSpace;

/**
 * Decompress data encoded using the wavelet-based JPEG 2000 standard,
 * reproducing the original data.
 *
 * Requires the Java Advanced Imaging (JAI) Image I/O Tools to be installed from java.net, see
 * <a href="http://download.java.net/media/jai-imageio/builds/release/1.1/">jai-imageio</a>.
 * Alternatively you can build from the source available in the
 * <a href="https://java.net/projects/jai-imageio-core/">jai-imageio-core svn repo</a>.
 *
 * Mac OS X users should download the tar.gz file for linux and unpack it to obtain the
 * required jar files. The .so file can be safely ignored.
 *
 * @author John Hewson
 * @author Timo Boehme
 */
public final class JPXFilter extends Filter
{
    /**
     * {@inheritDoc}
     */
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded, COSDictionary
            parameters, int index, DecodeOptions options) throws IOException
    {
        DecodeResult result = new DecodeResult(new COSDictionary());
        result.getParameters().addAll(parameters);
        BufferedImage image = readJPX(encoded, options, result);

        Raster raster = image.getRaster();
        switch (raster.getDataBuffer().getDataType())
        {
            case DataBuffer.TYPE_BYTE:
                DataBufferByte byteBuffer = (DataBufferByte) raster.getDataBuffer();
                decoded.write(byteBuffer.getData());
                return result;

            case DataBuffer.TYPE_USHORT:
                DataBufferUShort wordBuffer = (DataBufferUShort) raster.getDataBuffer();
                for (short w : wordBuffer.getData())
                {
                    decoded.write(w >> 8);
                    decoded.write(w);
                }
                return result;

            case DataBuffer.TYPE_INT:
                // not yet used (as of October 2018) but works as fallback
                // if we decide to convert to BufferedImage.TYPE_INT_RGB
                int[] ar = new int[raster.getNumBands()];
                for (int y = 0; y < image.getHeight(); ++y)
                {
                    for (int x = 0; x < image.getWidth(); ++x)
                    {
                        raster.getPixel(x, y, ar);
                        for (int i = 0; i < ar.length; ++i)
                        {
                            decoded.write(ar[i]);
                        }
                    }
                }
                return result;

            default:
                throw new IOException("Data type " + raster.getDataBuffer().getDataType() + " not implemented");
        }
    }

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                               COSDictionary parameters, int index) throws IOException
    {
        return decode(encoded, decoded, parameters, index, DecodeOptions.DEFAULT);
    }

    // try to read using JAI Image I/O
    private BufferedImage readJPX(InputStream input, DecodeOptions options, DecodeResult result) throws IOException
    {
        ImageReader reader = findImageReader("JPEG2000", "Java Advanced Imaging (JAI) Image I/O Tools are not installed");
        ImageInputStream iis = null;
        try
        {
            // PDFBOX-4121: ImageIO.createImageInputStream() is much slower
            iis = new MemoryCacheImageInputStream(input);

            reader.setInput(iis, true, true);
            ImageReadParam irp = reader.getDefaultReadParam();
            irp.setSourceRegion(options.getSourceRegion());
            irp.setSourceSubsampling(options.getSubsamplingX(), options.getSubsamplingY(),
                    options.getSubsamplingOffsetX(), options.getSubsamplingOffsetY());
            options.setFilterSubsampled(true);

            BufferedImage image;
            try
            {
                image = reader.read(0, irp);
            }
            catch (Exception e)
            {
                // wrap and rethrow any exceptions
                throw new IOException("Could not read JPEG 2000 (JPX) image", e);
            }

            COSDictionary parameters = result.getParameters();

            // "If the image stream uses the JPXDecode filter, this entry is optional
            // and shall be ignored if present"
            //
            // note that indexed color spaces make the BPC logic tricky, see PDFBOX-2204
            int bpc = image.getColorModel().getPixelSize() / image.getRaster().getNumBands();
            parameters.setInt(COSName.BITS_PER_COMPONENT, bpc);

            // "Decode shall be ignored, except in the case where the image is treated as a mask"
            if (!parameters.getBoolean(COSName.IMAGE_MASK, false))
            {
                parameters.setItem(COSName.DECODE, null);
            }

            // override dimensions, see PDFBOX-1735
            parameters.setInt(COSName.WIDTH, reader.getWidth(0));
            parameters.setInt(COSName.HEIGHT, reader.getHeight(0));

            // extract embedded color space
            if (!parameters.containsKey(COSName.COLORSPACE))
            {
                if (image.getSampleModel() instanceof MultiPixelPackedSampleModel &&
                    image.getColorModel().getPixelSize() == 1 &&
                    image.getRaster().getNumBands() == 1 && 
                    image.getColorModel() instanceof IndexColorModel)
                {
                    // PDFBOX-4326:
                    // force CS_GRAY colorspace because colorspace in IndexColorModel
                    // has 3 colors despite that there is only 1 color per pixel
                    // in raster
                    result.setColorSpace(new PDJPXColorSpace(ColorSpace.getInstance(ColorSpace.CS_GRAY)));
                }
                else
                {
                    result.setColorSpace(new PDJPXColorSpace(image.getColorModel().getColorSpace()));
                }
            }

            return image;
        }
        finally
        {
            if (iis != null)
            {
                iis.close();
            }
            reader.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("JPX encoding not implemented");
    }
}
