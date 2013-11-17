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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;

/**
 * This is used for the JPXDecode filter.
 *
 * @author <a href="mailto:timo.boehme@ontochem.com">Timo Boehme</a>
 *
 */
public class JPXFilter implements Filter
{

    /** Log instance. */
    private static final Log LOG = LogFactory.getLog(JPXFilter.class);

    /**
     * Decode JPEG2000 data using Java ImageIO library.
     *
     * {@inheritDoc}
     *
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex)
            throws IOException
    {
        BufferedImage bi = ImageIO.read(compressedData);
        if (bi != null)
        {
            DataBuffer dBuf = bi.getData().getDataBuffer();
            if (dBuf.getDataType() == DataBuffer.TYPE_BYTE)
            {
                // maybe some wrong/missing values have to be revised/added
                ColorModel colorModel = bi.getColorModel();
                if (options.getItem(COSName.COLORSPACE) == null)
                {
                    options.setItem(COSName.COLORSPACE,
                            PDColorSpaceFactory.createColorSpace(null, colorModel.getColorSpace()));
                }
                options.setInt(COSName.BITS_PER_COMPONENT, colorModel.getPixelSize() / colorModel.getNumComponents());
                options.setInt(COSName.HEIGHT, bi.getHeight());
                options.setInt(COSName.WIDTH, bi.getWidth());
                result.write(((DataBufferByte) dBuf).getData());
            }
            else
            {
                LOG.error("Image data buffer not of type byte but type " + dBuf.getDataType());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options, int filterIndex)
            throws IOException
    {
        LOG.error("Warning: JPXFilter.encode is not implemented yet, skipping this stream.");
    }
}
