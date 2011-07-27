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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;

/**
 * Modeled on the JBIG2Decode filter.
 *
 * thanks to Timo Böhme <timo.boehme@ontochem.com>
 */

public class JBIG2Filter implements Filter
{

    /** Log instance. */
    private static final Log log = LogFactory.getLog(JBIG2Filter.class);

    /**
     * Decode JBIG2 data using Java ImageIO library.
     *
     * {@inheritDoc}
     *
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex )
        throws IOException
    {
        BufferedImage bi = ImageIO.read(compressedData);
        if ( bi != null )
        {
            DataBuffer dBuf = bi.getData().getDataBuffer();
            if ( dBuf.getDataType() == DataBuffer.TYPE_BYTE )
            {
                result.write( ( ( DataBufferByte ) dBuf ).getData() );
            }
            else
            {
                log.error( "Image data buffer not of type byte but type " + dBuf.getDataType() );
            }
        }
        else
        {
            Iterator<ImageReader> reader = ImageIO.getImageReadersByFormatName("JBIG2");
            if (!reader.hasNext())
            {
                log.error( "Can't find an ImageIO plugin to decode the JBIG2 encoded datastream.");
            }
            else
            {
                log.error( "Something went wrong when decoding the JBIG2 encoded datastream.");
            }
        }
    }

     /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex )
        throws IOException
    {
        System.err.println( "Warning: JBIG2.encode is not implemented yet, skipping this stream." );
    }
}
