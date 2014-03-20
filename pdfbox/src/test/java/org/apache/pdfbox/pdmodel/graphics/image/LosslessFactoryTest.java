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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;

/**
 * Unit tests for LosslessFactory
 *
 * @author Tilman Hausherr
 */
public class LosslessFactoryTest extends TestCase
{
    /**
     * Tests RGB LosslessFactoryTest#createFromImage(PDDocument document,
     * BufferedImage image)
     *
     * @throws java.io.IOException
     */
    public void testCreateLosslessFromImageRGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));
        PDImageXObject ximage = LosslessFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "png");
        document.close();
    }

    /**
     * Tests ARGB LosslessFactoryTest#createFromImage(PDDocument document,
     * BufferedImage image)
     *
     * @throws java.io.IOException
     */
    public void testCreateLosslessFromImageARGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

        // create an ARGB image
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage argbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics ag = argbImage.getGraphics();
        ag.drawImage(image, 0, 0, null);
        ag.dispose();

        // create a weird transparency triangle
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < Math.min(y, w); ++x)
            {
                argbImage.setRGB(x, y, image.getRGB(x, y) & 0xFFFFFF | ((x * 255 / w) << 24));
            }
        }

        PDImageXObject ximage = LosslessFactory.createFromImage(document, argbImage);
        validate(ximage, 8, 344, 287, "png");

        assertNotNull(ximage.getSoftMask());
        validate(ximage.getSoftMask(), 8, 344, 287, "png");

        document.close();
    }

}
