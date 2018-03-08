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
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

/**
 * Decompresses data encoded using the JBIG2 standard, reproducing the original
 * monochrome (1 bit per pixel) image data (or an approximation of that data).
 *
 * Requires a JBIG2 plugin for Java Image I/O to be installed. A known working
 * plug-in is the Apache PDFBox JBIG2 plugin.
 *
 * @author Timo Boehme
 */
final class JBIG2Filter extends Filter
{
    private static final Log LOG = LogFactory.getLog(JBIG2Filter.class);

    private static boolean levigoLogged = false;

    private static synchronized void logLevigoDonated()
    {
        if (!levigoLogged)
        {
            LOG.info("The Levigo JBIG2 plugin has been donated to the Apache Foundation");
            LOG.info("and an improved version is available for download at "
                    + "https://pdfbox.apache.org/download.cgi");
            levigoLogged = true;
        }
    }

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded, COSDictionary
            parameters, int index, DecodeOptions options) throws IOException
    {
        ImageReader reader = findImageReader("JBIG2", "jbig2-imageio is not installed");
        if (reader.getClass().getName().contains("levigo"))
        {
            logLevigoDonated();
        }

        int bits = parameters.getInt(COSName.BITS_PER_COMPONENT, 1);
        COSDictionary params = getDecodeParams(parameters, index);

        ImageReadParam irp = reader.getDefaultReadParam();
        irp.setSourceSubsampling(options.getSubsamplingX(), options.getSubsamplingY(),
                options.getSubsamplingOffsetX(), options.getSubsamplingOffsetY());
        irp.setSourceRegion(options.getSourceRegion());
        options.setFilterSubsampled(true);

        COSStream globals = null;
        if (params != null)
        {
            globals = (COSStream) params.getDictionaryObject(COSName.JBIG2_GLOBALS);
        }

        ImageInputStream iis = null;
        try
        {
            if (globals != null)
            {
                iis = ImageIO.createImageInputStream(
                        new SequenceInputStream(globals.createInputStream(), encoded));
                reader.setInput(iis);
            }
            else
            {
                iis = ImageIO.createImageInputStream(encoded);
                reader.setInput(iis);
            }

            BufferedImage image;
            try
            {
                image = reader.read(0, irp);
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
            if (iis != null)
            {
                iis.close();
            }
            reader.dispose();
        }
        return new DecodeResult(parameters);
    }

    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
                               COSDictionary parameters, int index) throws IOException
    {
        return decode(encoded, decoded, parameters, index, DecodeOptions.DEFAULT);
    }

    @Override
    protected void encode(InputStream input, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        throw new UnsupportedOperationException("JBIG2 encoding not implemented");
    }
}
