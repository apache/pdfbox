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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
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
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        DecodeResult result = new DecodeResult(new COSDictionary());
        result.getParameters().addAll(parameters);
        BufferedImage image = readJPX(encoded, result);

        WritableRaster raster = image.getRaster();
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

            default:
                throw new IOException("Data type " + raster.getDataBuffer().getDataType() + " not implemented");
        }        
    }

    // try to read using JAI Image I/O
    private BufferedImage readJPX(InputStream input, DecodeResult result) throws IOException
    {
        ImageReader reader = findImageReader("JPEG2000", "Java Advanced Imaging (JAI) Image I/O Tools are not installed");
        try (ImageInputStream iis = ImageIO.createImageInputStream(input))
        {
            reader.setInput(iis, true, true);

            BufferedImage image;
            try
            {
                image = reader.read(0);
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
            parameters.setInt(COSName.WIDTH, image.getWidth());
            parameters.setInt(COSName.HEIGHT, image.getHeight());

            // extract embedded color space
            if (!parameters.containsKey(COSName.COLORSPACE))
            {
                result.setColorSpace(new PDJPXColorSpace(image.getColorModel().getColorSpace()));
            }

            return image;
        }
        finally
        {
            reader.dispose();
        }
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("JPX encoding not implemented");
    }
}
