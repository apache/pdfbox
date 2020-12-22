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


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

/**
 * Test that the convenience methods are really doing what's expected, and having the same as
 * the more focused factory methods.
 *
 * @author Tilman Hausherr
 */
class PDImageXObjectTest
{
    

    /**
     * Test of createFromFileByExtension method, of class PDImageXObject.
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Test
    void testCreateFromFileByExtension() throws IOException, URISyntaxException
    {
        testCompareCreatedFileByExtensionWithCreatedByCCITTFactory("ccittg4.tif");

        testCompareCreatedFileByExtensionWithCreatedByJPEGFactory("jpeg.jpg");
        testCompareCreatedFileByExtensionWithCreatedByJPEGFactory("jpegcmyk.jpg");

        testCompareCreatedFileByExtensionWithCreatedByLosslessFactory("gif.gif");
        testCompareCreatedFileByExtensionWithCreatedByLosslessFactory("gif-1bit-transparent.gif");
        testCompareCreatedFileByExtensionWithCreatedByLosslessFactory("png_indexed_8bit_alpha.png");
        testCompareCreatedFileByExtensionWithCreatedByLosslessFactory("png.png");
    }

    /**
     * Test of createFromFile method, of class PDImageXObject.
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Test
    void testCreateFromFile() throws IOException, URISyntaxException
    {
        testCompareCreatedFileWithCreatedByCCITTFactory("ccittg4.tif");

        testCompareCreatedFileWithCreatedByJPEGFactory("jpeg.jpg");
        testCompareCreatedFileWithCreatedByJPEGFactory("jpegcmyk.jpg");

        testCompareCreatedFileWithCreatedByLosslessFactory("gif.gif");
        testCompareCreatedFileWithCreatedByLosslessFactory("gif-1bit-transparent.gif");
        testCompareCreatedFileWithCreatedByLosslessFactory("png_indexed_8bit_alpha.png");
        testCompareCreatedFileWithCreatedByLosslessFactory("png.png");
    }


    /**
     * Test of createFromFileByContent method, of class PDImageXObject.
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Test
    void testCreateFromFileByContent() throws IOException, URISyntaxException
    {
        testCompareCreateByContentWithCreatedByCCITTFactory("ccittg4.tif");

        testCompareCreatedByContentWithCreatedByJPEGFactory("jpeg.jpg");
        testCompareCreatedByContentWithCreatedByJPEGFactory("jpegcmyk.jpg");

        testCompareCreatedByContentWithCreatedByLosslessFactory("gif.gif");
        testCompareCreatedByContentWithCreatedByLosslessFactory("gif-1bit-transparent.gif");
        testCompareCreatedByContentWithCreatedByLosslessFactory("png_indexed_8bit_alpha.png");
        testCompareCreatedByContentWithCreatedByLosslessFactory("png.png");
    }


    /**
     * Test of createFromByteArray method, of class PDImageXObject.
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Test
    void testCreateFromByteArray() throws IOException, URISyntaxException
    {
        testCompareCreatedFromByteArrayWithCreatedByCCITTFactory("ccittg4.tif");

        testCompareCreatedFromByteArrayWithCreatedByJPEGFactory("jpeg.jpg");
        testCompareCreatedFromByteArrayWithCreatedByJPEGFactory("jpegcmyk.jpg");

        testCompareCreatedFromByteArrayWithCreatedByLosslessFactory("gif.gif");
        testCompareCreatedFromByteArrayWithCreatedByLosslessFactory("gif-1bit-transparent.gif");
        testCompareCreatedFromByteArrayWithCreatedByLosslessFactory("png_indexed_8bit_alpha.png");
        testCompareCreatedFromByteArrayWithCreatedByLosslessFactory("png.png");
    }

    private void testCompareCreatedFileByExtensionWithCreatedByLosslessFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByExtension(file, doc);
            
            final BufferedImage bim = ImageIO.read(PDImageXObjectTest.class.getResourceAsStream(filename));
            final PDImageXObject expectedImage = LosslessFactory.createFromImage(doc, bim);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFileByExtensionWithCreatedByCCITTFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByExtension(file, doc);
            
            final PDImageXObject expectedImage = CCITTFactory.createFromFile(doc, file);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFileByExtensionWithCreatedByJPEGFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByExtension(file, doc);
            
            final PDImageXObject expectedImage = JPEGFactory.createFromStream(doc, new FileInputStream(file));
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFileWithCreatedByLosslessFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFile(file.getAbsolutePath(), doc);
            
            final BufferedImage bim = ImageIO.read(PDImageXObjectTest.class.getResourceAsStream(filename));
            final PDImageXObject expectedImage = LosslessFactory.createFromImage(doc, bim);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFileWithCreatedByCCITTFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFile(file.getAbsolutePath(), doc);
            
            final PDImageXObject expectedImage = CCITTFactory.createFromFile(doc, file);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFileWithCreatedByJPEGFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFile(file.getAbsolutePath(), doc);
            
            final PDImageXObject expectedImage = JPEGFactory.createFromStream(doc, new FileInputStream(file));
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedByContentWithCreatedByLosslessFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByContent(file, doc);
            
            final BufferedImage bim = ImageIO.read(PDImageXObjectTest.class.getResourceAsStream(filename));
            final PDImageXObject expectedImage = LosslessFactory.createFromImage(doc, bim);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreateByContentWithCreatedByCCITTFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByContent(file, doc);
            
            final PDImageXObject expectedImage = CCITTFactory.createFromFile(doc, file);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedByContentWithCreatedByJPEGFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final PDImageXObject image = PDImageXObject.createFromFileByContent(file, doc);
            
            final PDImageXObject expectedImage = JPEGFactory.createFromStream(doc, new FileInputStream(file));
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }
    
    
    
    
    private void testCompareCreatedFromByteArrayWithCreatedByLosslessFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final byte[] byteArray = IOUtils.toByteArray(new FileInputStream(file));
            final PDImageXObject image = PDImageXObject.createFromByteArray(doc, byteArray, null);
            
            final BufferedImage bim = ImageIO.read(PDImageXObjectTest.class.getResourceAsStream(filename));
            final PDImageXObject expectedImage = LosslessFactory.createFromImage(doc, bim);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFromByteArrayWithCreatedByCCITTFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final byte[] byteArray = IOUtils.toByteArray(new FileInputStream(file));
            final PDImageXObject image = PDImageXObject.createFromByteArray(doc, byteArray, null);
            
            final PDImageXObject expectedImage = CCITTFactory.createFromFile(doc, file);
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void testCompareCreatedFromByteArrayWithCreatedByJPEGFactory(final String filename)
            throws IOException, URISyntaxException
    {
        try (PDDocument doc = new PDDocument())
        {
            final File file = new File(PDImageXObjectTest.class.getResource(filename).toURI());
            final byte[] byteArray = IOUtils.toByteArray(new FileInputStream(file));
            final PDImageXObject image = PDImageXObject.createFromByteArray(doc, byteArray, null);
            
            final PDImageXObject expectedImage = JPEGFactory.createFromStream(doc, new FileInputStream(file));
            
            assertEquals(expectedImage.getSuffix(), image.getSuffix());
            checkIdentARGB(image.getImage(), expectedImage.getImage());
        }
    }

    private void checkIdentARGB(final BufferedImage expectedImage, final BufferedImage actualImage)
    {
        String errMsg = "";

        final int w = expectedImage.getWidth();
        final int h = expectedImage.getHeight();
        assertEquals(w, actualImage.getWidth());
        assertEquals(h, actualImage.getHeight());
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                if (expectedImage.getRGB(x, y) != actualImage.getRGB(x, y))
                {
                    errMsg = String.format("(%d,%d) %06X != %06X", x, y, expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
                }
                assertEquals(expectedImage.getRGB(x, y), actualImage.getRGB(x, y), errMsg);
            }
        }
    }    
}
