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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * Factory for creating a PDImageXObject containing a lossless compressed image.
 * 
 * @author Tilman Hausherr
 */
class LosslessFactory
{
    /**
     * Creates a new lossless encoded Image XObject from a Buffered Image.
     *
     * @param document the document where the image will be created
     * @param image the buffered image to embed
     * @return a new Image XObject
     * @throws IOException if something goes wrong
     */
    static PDImageXObject createLosslessFromImage(PDDocument document, BufferedImage bim)
            throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //TODO use bit writing, indexed, etc
        int h = bim.getHeight();
        int w = bim.getWidth();
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                Color co = new Color(bim.getRGB(x, y));
                bos.write(co.getRed());
                bos.write(co.getGreen());
                bos.write(co.getBlue());
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
        pdImage.setColorSpace(PDDeviceRGB.INSTANCE);  //TODO from image
        pdImage.setBitsPerComponent(8); //TODO other sizes
        pdImage.setHeight(bim.getHeight());
        pdImage.setWidth(bim.getWidth());
        return pdImage;
    }

}
