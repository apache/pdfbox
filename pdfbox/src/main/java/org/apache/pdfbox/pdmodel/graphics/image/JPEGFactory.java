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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Factory for creating a PDImageXObject containing a JPEG compressed image.
 * @author John Hewson
 */
public final class JPEGFactory extends ImageFactory
{
    private JPEGFactory()
    {
    }

    /**
     * Creates a new JPEG Image XObject from an input stream containing JPEG data.
     * 
     * The input stream data will be preserved and embedded in the PDF file without modification.
     * @param document the document where the image will be created
     * @param stream a stream of JPEG data
     * @return a new Image XObject
     * 
     * @throws IOException if the input stream cannot be read
     */
    public static PDImageXObject createFromStream(PDDocument document, InputStream stream)
            throws IOException
    {
        // copy stream
        ByteArrayInputStream byteStream = new ByteArrayInputStream(IOUtils.toByteArray(stream));

        // read image
        BufferedImage awtImage = readJPEG(byteStream);
        byteStream.reset();

        // create Image XObject from stream
        PDImageXObject pdImage = new PDImageXObject(document, byteStream);

        // add DCT filter
        pdImage.getCOSStream().setItem(COSName.FILTER, COSName.DCT_DECODE);

        // no alpha
        if (awtImage.getColorModel().hasAlpha())
        {
            throw new UnsupportedOperationException("alpha channel not implemented");
        }

        // set properties (width, height, depth, color space, etc.)
        setPropertiesFromAWT(awtImage, pdImage);

        return pdImage;
    }

    private static BufferedImage readJPEG(InputStream stream) throws IOException
    {
        // find suitable image reader
        Iterator readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;
        while (readers.hasNext())
        {
            reader = (ImageReader) readers.next();
            if (reader.canReadRaster())
            {
                break;
            }
        }

        if (reader == null)
        {
            throw new MissingImageReaderException("Cannot read JPEG image: " +
                    "a suitable JAI I/O image filter is not installed");
        }

        ImageInputStream iis = null;
        try
        {
            iis = ImageIO.createImageInputStream(stream);
            reader.setInput(iis);

            ImageIO.setUseCache(false);
            return reader.read(0);
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
     * Creates a new JPEG Image XObject from a Buffered Image.
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image)
        throws IOException
    {
        return createFromImage(document, image, 0.75f);
    }

    /**
     * Creates a new JPEG Image XObject from a Buffered Image and a given quality.
     * The image will be created at 72 DPI.
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @param quality the desired JPEG compression quality
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image,
                                                 float quality) throws IOException
    {
        return createFromImage(document, image, quality, 72);
    }

    /**
     * Creates a new JPEG Image XObject from a Buffered Image, a given quality and DPI.
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @param quality the desired JPEG compression quality
     * @param dpi the desired DPI (resolution) of the JPEG
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image,
                                                 float quality, int dpi) throws IOException
    {
        return createJPEG(document, image, quality, dpi);
    }
    
    // Creates an Image XObject from a Buffered Image using JAI Image I/O
    private static PDImageXObject createJPEG(PDDocument document, BufferedImage image,
                                             float quality, int dpi) throws IOException
    {
        // extract alpha channel (if any)
        BufferedImage awtColorImage = getColorImage(image);
        BufferedImage awtAlphaImage = getAlphaImage(image);

        // create XObject
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(image, "jpeg", bos, dpi, quality);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bos.toByteArray());
        PDImageXObject pdImage = new PDImageXObject(document, byteStream);
        
        // add DCT filter
        COSStream dict = pdImage.getCOSStream();
        dict.setItem(COSName.FILTER, COSName.DCT_DECODE);

        // alpha -> soft mask
        if (awtAlphaImage != null)
        {
            PDImage xAlpha = JPEGFactory.createFromImage(document, awtAlphaImage, quality);
            dict.setItem(COSName.SMASK, xAlpha);
        }

        // set properties (width, height, depth, color space, etc.)
        setPropertiesFromAWT(awtColorImage, pdImage);

        return pdImage;
    }
}
