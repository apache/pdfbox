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
import static org.apache.pdfbox.pdmodel.graphics.image.ImageFactory.getColorImage;

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

        // extract color channel
        BufferedImage awtColorImage = getColorImage(image);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //TODO: using the grayscale branch for BufferedImage.TYPE_BYTE_GRAY
        // fails the test. We use the RGB branch instead until this is fixed.
        if ((//image.getType() == BufferedImage.TYPE_BYTE_GRAY || 
                image.getType() == BufferedImage.TYPE_BYTE_BINARY)
                && image.getColorModel().getPixelSize() <= 8)
        {
            MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(bos);

            // grayscale images need one color per sample
            bpc = image.getColorModel().getPixelSize();
            deviceColorSpace = PDDeviceGray.INSTANCE;
            int h = awtColorImage.getHeight();
            int w = awtColorImage.getWidth();
            for (int y = 0; y < h; ++y)
            {
                for (int x = 0; x < w; ++x)
                {
                    mcios.writeBits(awtColorImage.getRGB(x, y), bpc);
                }
            }
            mcios.writeBits(0, 7); // padding
            mcios.flush();
            mcios.close();
        }
        else
        {
            // RGB
            bpc = 8;
            deviceColorSpace = PDDeviceRGB.INSTANCE;
            int h = awtColorImage.getHeight();
            int w = awtColorImage.getWidth();
            for (int y = 0; y < h; ++y)
            {
                for (int x = 0; x < w; ++x)
                {
                    Color color = new Color(awtColorImage.getRGB(x, y));
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
        pdImage.setHeight(awtColorImage.getHeight());
        pdImage.setWidth(awtColorImage.getWidth());

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
