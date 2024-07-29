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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.colorCount;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.doWritePDF;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Unit tests for JPEGFactory
 *
 * @author Tilman Hausherr
 */
@Execution(ExecutionMode.CONCURRENT)
class JPEGFactoryTest
{
    private static final File TESTRESULTSDIR = new File("target/test-output/graphics");

    @BeforeAll
    static void setUp()
    {
        TESTRESULTSDIR.mkdirs();
    }

    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with color JPEG file
     */
    @Test
    void testCreateFromStream() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg");
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpegrgbstream.pdf");
        checkJpegStream(TESTRESULTSDIR, "jpegrgbstream.pdf", JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));
    }

    /*
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with CMYK color JPEG file
     */
    @Test
    void testCreateFromStreamCMYK() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpegcmyk.jpg");
        PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
        validate(ximage, 8, 343, 287, "jpg", PDDeviceCMYK.INSTANCE.getName());

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpegcmykstream.pdf");
        checkJpegStream(TESTRESULTSDIR, "jpegcmykstream.pdf", JPEGFactoryTest.class.getResourceAsStream("jpegcmyk.jpg"));
    }
    
    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with gray JPEG file
     */
    @Test
    void testCreateFromStream256() throws IOException
    {
        try
        {
            PDDocument document = new PDDocument();
            InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg");
            PDImageXObject ximage = JPEGFactory.createFromStream(document, stream);
            validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());
            
            doWritePDF(document, ximage, TESTRESULTSDIR, "jpeg256stream.pdf");
            checkJpegStream(TESTRESULTSDIR, "jpeg256stream.pdf", JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg"));
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with color JPEG image
     */
    @Test
    void testCreateFromImageRGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));
        assertEquals(3, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpegrgb.pdf");
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with gray JPEG image
     */
    @Test
    void testCreateFromImage256() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg"));
        assertEquals(1, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpeg256.pdf");
    }

    /**
     * Tests ARGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image)
     */
    @Test
    void testCreateFromImageINT_ARGB() throws IOException
    {
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

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpeg-intargb.pdf");
    }

    /**
     * Tests ARGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image)
     */
    @Test
    void testCreateFromImage4BYTE_ABGR() throws IOException
    {
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

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpeg-4bargb.pdf");
    }

    /**
     * Tests USHORT_555_RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image), see also PDFBOX-4674.
     * @throws java.io.IOException
     */
    @Test
    void testCreateFromImageUSHORT_555_RGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg"));

        // create an USHORT_555_RGB image
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
        Graphics ag = rgbImage.getGraphics();
        ag.drawImage(image, 0, 0, null);
        ag.dispose();

        for (int x = 0; x < rgbImage.getWidth(); ++x)
        {
            for (int y = 0; y < rgbImage.getHeight(); ++y)
            {
                rgbImage.setRGB(x, y, (rgbImage.getRGB(x, y) & 0xFFFFFF) | ((y / 10 * 10) << 24));
            }
        }

        PDImageXObject ximage = JPEGFactory.createFromImage(document, rgbImage);
        validate(ximage, 8, width, height, "jpg", PDDeviceRGB.INSTANCE.getName());
        assertNull(ximage.getSoftMask());

        doWritePDF(document, ximage, TESTRESULTSDIR, "jpeg-ushort555rgb.pdf");
    }

    /**
     * PDFBOX-5137 and PDFBOX-5196: check that numFrameComponents and not numScanComponents is used
     * to determine the color space.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox5137() throws IOException
    {
        byte[] ba = Files.readAllBytes(Paths.get("target/imgs", "PDFBOX-5196-lotus.jpg"));

        PDDocument document = new PDDocument();

        PDImageXObject ximage = JPEGFactory.createFromByteArray(document, ba);

        validate(ximage, 8, 500, 500, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, TESTRESULTSDIR, "PDFBOX-5196-lotus.pdf");
        checkJpegStream(TESTRESULTSDIR, "PDFBOX-5196-lotus.pdf", new ByteArrayInputStream(ba));        
    }

    // check whether it is possible to extract the jpeg stream exactly 
    // as it was passed to createFromStream
    private void checkJpegStream(File testResultsDir, String filename, InputStream resourceStream)
            throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(testResultsDir, filename)))
        {
            PDImageXObject img =
                    (PDImageXObject) doc.getPage(0).getResources().getXObject(COSName.getPDFName("Im1"));
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            try (InputStream dctStream = img.createInputStream(Arrays.asList(COSName.DCT_DECODE.getName())))
            {
                resourceStream.transferTo(baos1);
                dctStream.transferTo(baos2);
            }
            resourceStream.close();
            assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());
        }
    }
}
