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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.colorCount;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.doWritePDF;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;
import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for JPEGFactory
 *
 * @author Tilman Hausherr
 */
public class JPEGFactoryTest extends TestCase
{
    private final File testResultsDir = new File("target/test-output/graphics");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with color JPEG file
     */
    public void testCreateFromStream() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg");
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpegrgbstream.pdf");
        checkJpegStream(testResultsDir, "jpegrgbstream.pdf", JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));
    }

    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with gray JPEG file
     */
    public void testCreateFromStream256() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg");
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpeg256stream.pdf");
        checkJpegStream(testResultsDir, "jpeg256stream.pdf", JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg"));
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with color JPEG image
     */
    public void testCreateFromImageRGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));
        assertEquals(3, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpegrgb.pdf");
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with gray JPEG image
     */
    public void testCreateFromImage256() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg"));
        assertEquals(1, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpeg256.pdf");
    }

    /**
     * Tests ARGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image)
     */
    public void testCreateFromImageINT_ARGB() throws IOException
    {
        // workaround Open JDK bug
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044758
        if (System.getProperty("java.runtime.name").equals("OpenJDK Runtime Environment")
                && (System.getProperty("java.specification.version").equals("1.6")
                || System.getProperty("java.specification.version").equals("1.7")
                || System.getProperty("java.specification.version").equals("1.8")))
        {
            return;
        }

        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));

        // create an ARGB image
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics ag = argbImage.getGraphics();
        ag.drawImage(image, 0, 0, null);
        ag.dispose();

        for (int x = 0; x < argbImage.getWidth(); ++x)
        {
            for (int y = 0; y < argbImage.getHeight(); ++y)
            {
                argbImage.setRGB(x, y, (argbImage.getRGB(x, y) & 0xFFFFFF) | ((y / 10 * 10) << 24));
            }
        }

        PDImageXObject ximage = JPEGFactory.createFromImage(document, argbImage);
        validate(ximage, 8, width, height, "jpg", PDDeviceRGB.INSTANCE.getName());
        assertNotNull(ximage.getSoftMask());
        validate(ximage.getSoftMask(), 8, width, height, "jpg", PDDeviceGray.INSTANCE.getName());
        assertTrue(colorCount(ximage.getSoftMask().getImage()) > image.getHeight() / 10);

        doWritePDF(document, ximage, testResultsDir, "jpeg-intargb.pdf");
    }

    /**
     * Tests ARGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image)
     */
    public void testCreateFromImage4BYTE_ABGR() throws IOException
    {
        // workaround Open JDK bug
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044758
        if (System.getProperty("java.runtime.name").equals("OpenJDK Runtime Environment")
                && (System.getProperty("java.specification.version").equals("1.6")
                || System.getProperty("java.specification.version").equals("1.7")
                || System.getProperty("java.specification.version").equals("1.8")))
        {
            return;
        }

        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));

        // create an ARGB image
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics ag = argbImage.getGraphics();
        ag.drawImage(image, 0, 0, null);
        ag.dispose();

        for (int x = 0; x < argbImage.getWidth(); ++x)
        {
            for (int y = 0; y < argbImage.getHeight(); ++y)
            {
                argbImage.setRGB(x, y, (argbImage.getRGB(x, y) & 0xFFFFFF) | ((y / 10 * 10) << 24));
            }
        }

        PDImageXObject ximage = JPEGFactory.createFromImage(document, argbImage);
        validate(ximage, 8, width, height, "jpg", PDDeviceRGB.INSTANCE.getName());
        assertNotNull(ximage.getSoftMask());
        validate(ximage.getSoftMask(), 8, width, height, "jpg", PDDeviceGray.INSTANCE.getName());
        assertTrue(colorCount(ximage.getSoftMask().getImage()) > image.getHeight() / 10);

        doWritePDF(document, ximage, testResultsDir, "jpeg-4bargb.pdf");
    }

    // check whether it is possible to extract the jpeg stream exactly 
    // as it was passed to createFromStream
    private void checkJpegStream(File testResultsDir, String filename, InputStream resourceStream)
            throws IOException
    {
        PDDocument doc = PDDocument.load(new File(testResultsDir, filename));
        PDImageXObject img =
                (PDImageXObject) doc.getPage(0).getResources().getXObject(COSName.getPDFName("Im1"));
        InputStream dctStream = img.createInputStream(Arrays.asList(COSName.DCT_DECODE.getName()));
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        IOUtils.copy(resourceStream, baos1);
        IOUtils.copy(dctStream, baos2);
        resourceStream.close();
        dctStream.close();
        assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());
        doc.close();
    }
}
