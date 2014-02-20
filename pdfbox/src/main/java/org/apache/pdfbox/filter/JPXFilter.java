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
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
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
public class JPXFilter implements Filter
{
    /**
     * Decode JPEG 2000 data using Java ImageIO library.
     *
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options,
                       int filterIndex) throws IOException
    {
        BufferedImage image = readJPX(compressedData);

        WritableRaster raster = image.getRaster();
        if (raster.getDataBuffer().getDataType() != DataBuffer.TYPE_BYTE)
        {
            throw new IOException("Not implemented: greater than 8-bit depth");
        }
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        result.write(buffer.getData());
    }

    private static BufferedImage readJPX(InputStream input) throws IOException
    {
        // try to read using JAI Image I/O
        ImageIO.setUseCache(false);
        BufferedImage image = ImageIO.read(input);

        if (image == null)
        {
            throw new MissingImageReaderException("Cannot read JPEG 2000 (JPX) image: " +
                    "Java Advanced Imaging (JAI) Image I/O Tools are not installed");
        }

        return image;
    }

    /**
     * Returns the embedded color space from a JPX file.
     * @param input The JPX input stream
     */
    // TODO this method is something of a hack, we'd rather be able to return info from decode(...)
    public static PDColorSpace getColorSpace(InputStream input) throws IOException
    {
        BufferedImage image = readJPX(input);
        return new PDJPXColorSpace(image.getColorModel().getColorSpace());
    }

    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options,
                       int filterIndex) throws IOException
    {
        throw new UnsupportedOperationException("JPX encoding not implemented");
    }
}
