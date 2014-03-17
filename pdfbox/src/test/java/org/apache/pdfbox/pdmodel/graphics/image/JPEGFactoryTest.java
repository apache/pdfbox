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
import java.io.InputStream;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Unit tests for JPEGFactory
 * @author Tilman Hausherr
 */
public class JPEGFactoryTest extends TestCase
{
    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream stream)
     */
    public void testCreateFromStream() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg");
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        validate(ximage);
        document.close();
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage image)
     */
    public void testCreateFromImageRGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage);
        document.close();
    }

    /**
     * Tests ARGB JPEGFactory#createFromImage(PDDocument document, BufferedImage image)
     */
    public void testCreateFromImageARGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));

        // create an ARGB image
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage argbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics ag = argbImage.getGraphics();
        ag.drawImage(image, 0, 0, null);
        ag.dispose();

        // left half of image with 50% alpha
        for (int x = 0; x < w / 2; ++x)
        {
            for (int y = 0; y < h; ++y)
            {
                argbImage.setRGB(x, y, image.getRGB(x, y) & 0x7FFFFFFF);
            }
        }

        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage);
        assertNull(ximage.getSoftMask());

        document.close();
    }

    private void validate(PDImageXObject ximage) throws IOException
    {
        // check the dictionary
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(8, ximage.getBitsPerComponent());
        assertEquals(344, ximage.getWidth());
        assertEquals(287, ximage.getHeight());
        assertEquals("jpg", ximage.getSuffix());

        // check the image
        assertNotNull(ximage.getImage());
        assertEquals(344, ximage.getImage().getWidth());
        assertEquals(287, ximage.getImage().getHeight());
        
        // dummy write the image
        boolean writeOk = ImageIOUtil.writeImage(ximage.getImage(), "png", new NullOutputStream());
        assertTrue(writeOk);
    }
}
