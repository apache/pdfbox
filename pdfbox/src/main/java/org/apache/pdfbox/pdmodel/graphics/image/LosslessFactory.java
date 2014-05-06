/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * Factory for creating a PDImageXObject containing a lossless compressed image.
 *
 * @author Tilman Hausherr
 */
public class LosslessFactory
{
    /**
     * Creates a new lossless encoded Image XObject from a Buffered Image.
     *
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @return a new Image XObject
     * @throws IOException if something goes wrong
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image)
            throws IOException
    {
        int bpc;
        PDDeviceColorSpace deviceColorSpace;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int height = image.getHeight();
        int width = image.getWidth();

        if ((image.getType() == BufferedImage.TYPE_BYTE_GRAY
                || image.getType() == BufferedImage.TYPE_BYTE_BINARY)
                && image.getColorModel().getPixelSize() <= 8)
        {
            MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(bos);

            // grayscale images need one color per sample
            bpc = image.getColorModel().getPixelSize();
            deviceColorSpace = PDDeviceGray.INSTANCE;
            for (int y = 0; y < height; ++y)
            {
                for (int x = 0; x < width; ++x)
                {
                    mcios.writeBits(image.getRGB(x, y) & 0xFF, bpc);
                }
            }
            while (mcios.getBitOffset() != 0)
            {
                mcios.writeBit(0);
            }
            mcios.flush();
            mcios.close();
        }
        else
        {
            // RGB
            bpc = 8;
            deviceColorSpace = PDDeviceRGB.INSTANCE;
            for (int y = 0; y < height; ++y)
            {
                for (int x = 0; x < width; ++x)
                {
                    Color color = new Color(image.getRGB(x, y));
                    bos.write(color.getRed());
                    bos.write(color.getGreen());
                    bos.write(color.getBlue());
                }
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());

        Filter filter = FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        filter.encode(bais, bos2, new COSDictionary(), 0);

        ByteArrayInputStream filteredByteStream = new ByteArrayInputStream(bos2.toByteArray());
        PDImageXObject pdImage = new PDImageXObject(document, filteredByteStream);

        COSDictionary dict = pdImage.getCOSStream();
        dict.setItem(COSName.FILTER, COSName.FLATE_DECODE);

        pdImage.setColorSpace(deviceColorSpace);
        pdImage.setBitsPerComponent(bpc);
        pdImage.setHeight(image.getHeight());
        pdImage.setWidth(image.getWidth());

        // alpha -> soft mask
        PDImage xAlpha = createAlphaFromARGBImage(document, image);
        if (xAlpha != null)
        {
            dict.setItem(COSName.SMASK, xAlpha);
        }

        return pdImage;
    }

    /**
     * Creates a grayscale PDImageXObject from the alpha channel of an image.
     *
     * @param document the document where the image will be created.
     * @param image an ARGB image.
     *
     * @return the alpha channel of an image as a grayscale image.
     *
     * @throws IOException if something goes wrong
     */
    private static PDImageXObject createAlphaFromARGBImage(PDDocument document, BufferedImage image)
            throws IOException
    {
        // this implementation makes the assumption that the raster uses 
        // SinglePixelPackedSampleModel, i.e. the values can be used 1:1 for
        // the stream. 
        // Sadly the type of the databuffer is TYPE_INT and not TYPE_BYTE.
        //TODO: optimize this to lessen the memory footprint.
        // possible idea? Derive an inputStream that reads from the raster.

        if (!image.getColorModel().hasAlpha())
        {
            return null;
        }

        // extract the alpha information
        WritableRaster alphaRaster = image.getAlphaRaster();

        int[] pixels = alphaRaster.getPixels(0, 0,
                alphaRaster.getSampleModel().getWidth(),
                alphaRaster.getSampleModel().getHeight(),
                (int[]) null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int pixel : pixels)
        {
            bos.write(pixel);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());

        Filter filter = FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        filter.encode(bais, bos2, new COSDictionary(), 0);

        ByteArrayInputStream filteredByteStream = new ByteArrayInputStream(bos2.toByteArray());
        PDImageXObject pdImage = new PDImageXObject(document, filteredByteStream);

        COSDictionary dict = pdImage.getCOSStream();
        dict.setItem(COSName.FILTER, COSName.FLATE_DECODE);

        pdImage.setColorSpace(PDDeviceGray.INSTANCE);
        pdImage.setBitsPerComponent(8);
        pdImage.setHeight(image.getHeight());
        pdImage.setWidth(image.getWidth());

        return pdImage;
    }

}
