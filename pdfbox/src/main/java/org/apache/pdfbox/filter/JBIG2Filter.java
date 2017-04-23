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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

/**
 * Decompresses data encoded using the JBIG2 standard, reproducing the original
 * monochrome (1 bit per pixel) image data (or an approximation of that data).
 *
 * Requires a JBIG2 plugin for Java Image I/O to be installed. A known working
 * plug-in is <a href="https://github.com/levigo/jbig2-imageio">jbig2-imageio</a>
 * which is available under the GPL v3 license.
 *
 * @author Timo Boehme
 */
final class JBIG2Filter extends Filter
{
    private static final Log LOG = LogFactory.getLog(JBIG2Filter.class);

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                                         COSDictionary parameters, int index) throws IOException
    {
        ImageReader reader = findImageReader("JBIG2", "jbig2-imageio is not installed");
        DecodeResult result = new DecodeResult(new COSDictionary());
        result.getParameters().addAll(parameters);

        int bits = parameters.getInt(COSName.BITS_PER_COMPONENT, 1);
        COSDictionary params = getDecodeParams(parameters, index);

        InputStream source = encoded;
        if (params != null)
        {
            COSBase globals = params.getDictionaryObject(COSName.JBIG2_GLOBALS);
            if (globals instanceof COSStream)
            {
                source = new SequenceInputStream(((COSStream) globals).createInputStream(), encoded);
            }
        }

        try (ImageInputStream iis = ImageIO.createImageInputStream(source))
        {
            reader.setInput(iis);

            BufferedImage image;
            try
            {
                image = reader.read(0, reader.getDefaultReadParam());
            }
            catch (Exception e)
            {
                // wrap and rethrow any exceptions
                throw new IOException("Could not read JBIG2 image", e);
            }

            // I am assuming since JBIG2 is always black and white
            // depending on your renderer this might or might be needed
            if (image.getColorModel().getPixelSize() != bits)
            {
                if (bits != 1)
                {
                    LOG.warn("Attempting to handle a JBIG2 with more than 1-bit depth");
                }
                BufferedImage packedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_BYTE_BINARY);
                Graphics graphics = packedImage.getGraphics();
                graphics.drawImage(image, 0, 0, null);
                graphics.dispose();
                image = packedImage;
            }

            DataBuffer dBuf = image.getData().getDataBuffer();
            if (dBuf.getDataType() == DataBuffer.TYPE_BYTE)
            {
                decoded.write(((DataBufferByte) dBuf).getData());
            }
            else
            {
                throw new IOException("Unexpected image buffer type");
            }
        }
        finally
        {
            reader.dispose();
        }

        // repair missing color space
        if (!parameters.containsKey(COSName.COLORSPACE))
        {
            result.getParameters().setName(COSName.COLORSPACE, COSName.DEVICEGRAY.getName());
        }

        return new DecodeResult(parameters);
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("JBIG2 encoding not implemented");
    }
}
