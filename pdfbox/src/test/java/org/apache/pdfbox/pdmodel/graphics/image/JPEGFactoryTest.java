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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tilman Hausherr
 */
public class JPEGFactoryTest extends TestCase
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * Test of createFromStream method, of class JPEGFactory.
     */
    @Test
    public void testCreateFromStream() throws Exception
    {
        PDDocument document = new PDDocument();
        InputStream stream = new FileInputStream(new File("src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/jpeg.jpg"));
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(8, ximage.getBitsPerComponent());
        assertEquals(344, ximage.getWidth());
        assertEquals(287, ximage.getHeight());
        assertEquals("jpg", ximage.getSuffix());

        //TODO shouldn't ximage.getImage() return a real image?
//        assertNotNull(ximage.getImage());
//        assertEquals(344, ximage.getImage().getWidth());
//        assertEquals(287, ximage.getImage().getHeight());
        document.close();
    }

    /**
     * Test of createFromImage method, of class JPEGFactory.
     */
    @Test
    public void testCreateFromImage() throws Exception
    {

        PDDocument document = new PDDocument();
        BufferedImage rgbImage = ImageIO.read(new File("src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/jpeg.jpg"));

        // Create an ARGB image
        int w = rgbImage.getWidth();
        int h = rgbImage.getHeight();
        BufferedImage argbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics ag = argbImage.getGraphics();
        ag.drawImage(rgbImage, 0, 0, null);
        ag.dispose();
        // left half of image with 1/2 transparency
        for (int x = 0; x < w / 2; ++x)
        {
            for (int y = 0; y < h; ++y)
            {
                argbImage.setRGB(x, y, rgbImage.getRGB(x, y) & 0x7FFFFFFF);
            }
        }

        PDImageXObject ximage = JPEGFactory.createFromImage(document, rgbImage);
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(8, ximage.getBitsPerComponent());
        assertEquals(344, ximage.getWidth());
        assertEquals(287, ximage.getHeight());
        assertEquals("jpg", ximage.getSuffix());
        assertNull(ximage.getSoftMask());
        
//TODO when ARGB works        
//        PDImageXObject ximage = JPEGFactory.createFromImage(document, argbImage);
//        assertNotNull(ximage.getSoftMask());
// etc...        


        
        //TODO shouldn't ximage.getImage() return a real image?
//        assertNotNull(ximage.getImage());
//        assertEquals(344, ximage.getImage().getWidth());
//        assertEquals(287, ximage.getImage().getHeight());
//        document.close();
    }

}
