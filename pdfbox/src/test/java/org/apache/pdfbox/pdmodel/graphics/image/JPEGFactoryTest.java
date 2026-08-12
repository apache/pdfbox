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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import junit.framework.TestCase;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.colorCount;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.doWritePDF;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeTrue;

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
        byte[] ba = IOUtils.toByteArray(stream);
        stream.close();
        PDImageXObject ximage = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba));
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpegrgbstream.pdf");
        checkJpegStream(testResultsDir, "jpegrgbstream.pdf", new ByteArrayInputStream(ba));
    }

    /*
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with CMYK color JPEG file
     */
    public void testCreateFromStreamCMYK() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpegcmyk.jpg");
        byte[] ba = IOUtils.toByteArray(stream);
        stream.close();
        PDImageXObject ximage = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba));
        validate(ximage, 8, 343, 287, "jpg", PDDeviceCMYK.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpegcmykstream.pdf");
        checkJpegStream(testResultsDir, "jpegcmykstream.pdf", new ByteArrayInputStream(ba));
    }

    /**
     * Tests JPEGFactory#createFromStream(PDDocument document, InputStream
     * stream) with gray JPEG file
     */
    public void testCreateFromStream256() throws IOException
    {
        PDDocument document = new PDDocument();
        InputStream stream = JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg");
        byte[] ba = IOUtils.toByteArray(stream);
        stream.close();
        PDImageXObject ximage = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba));
        validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "jpeg256stream.pdf");
        checkJpegStream(testResultsDir, "jpeg256stream.pdf", new ByteArrayInputStream(ba));
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with color JPEG image
     */
    public void testCreateFromImageRGB() throws IOException
    {
        InputStream is = JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg");
        byte[] ba = IOUtils.toByteArray(is);
        is.close();
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(ba));
        assertEquals(3, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.INSTANCE.getName());
        BufferedImage expected = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba)).getImage();
        float meanAbsDiffPerPixel = computeMeanAbsDiffPerPixel(expected, ximage.getImage());
        assertTrue(meanAbsDiffPerPixel < 5);

        doWritePDF(document, ximage, testResultsDir, "jpegrgb.pdf");
    }

    /**
     * Tests RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image) with gray JPEG image
     */
    public void testCreateFromImage256() throws IOException
    {
        InputStream is = JPEGFactoryTest.class.getResourceAsStream("jpeg256.jpg");
        byte[] ba = IOUtils.toByteArray(is);
        is.close();
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(ba));
        assertEquals(1, image.getColorModel().getNumComponents());
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceGray.INSTANCE.getName());
        BufferedImage expected = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba)).getImage();
        float meanAbsDiffPerPixel = computeMeanAbsDiffPerPixel(expected, ximage.getImage());
        assertTrue(meanAbsDiffPerPixel < 5);

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

    /**
     * Tests USHORT_555_RGB JPEGFactory#createFromImage(PDDocument document, BufferedImage
     * image), see also PDFBOX-4674.
     * @throws java.io.IOException
     */
    public void testCreateFromImageUSHORT_555_RGB() throws IOException
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

        InputStream is = JPEGFactoryTest.class.getResourceAsStream("jpeg.jpg");
        byte[] ba = IOUtils.toByteArray(is);
        is.close();

        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(ba));

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
        BufferedImage expected = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba)).getImage();
        float meanAbsDiffPerPixel = computeMeanAbsDiffPerPixel(expected, ximage.getImage());
        assertTrue(meanAbsDiffPerPixel < 5);

        doWritePDF(document, ximage, testResultsDir, "jpeg-ushort555rgb.pdf");
    }

    /**
     * PDFBOX-5137 and PDFBOX-5196: check that numFrameComponents and not numScanComponents is used
     * to determine the color space.
     *
     * @throws IOException
     */
    public void testPDFBox5137() throws IOException
    {
        InputStream is = new FileInputStream("target/imgs/PDFBOX-5196-lotus.jpg");
        byte[] ba = IOUtils.toByteArray(is);
        is.close();

        PDDocument document = new PDDocument();

        PDImageXObject ximage = JPEGFactory.createFromByteArray(document, ba);

        validate(ximage, 8, 500, 500, "jpg", PDDeviceRGB.INSTANCE.getName());

        doWritePDF(document, ximage, testResultsDir, "PDFBOX-5196-lotus.pdf");
        checkJpegStream(testResultsDir, "PDFBOX-5196-lotus.pdf", new ByteArrayInputStream(ba));        
    }

    // PDFBOX-6235
    public void testCreateFromImageCMYK() throws IOException
    {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;
        while (readers.hasNext())
        {
            reader = readers.next();
            if (reader.getClass().getName().startsWith("com.sun.imageio.plugins.jpeg.JPEGImageReader"))
            {
                break;
            }
            reader = null;
        }
        assumeTrue("This test works only with the original java imaging reader", reader != null);

        // magick -size 200x200 gradient:red-blue -colorspace CMYK PDFBOX-6235-cmyk.jpg
        InputStream is = JPEGFactoryTest.class.getResourceAsStream("PDFBOX-6235-cmyk.jpg");
        byte[] ba = IOUtils.toByteArray(is);
        is.close();

        reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(ba)));
        BufferedImage image = reader.read(0);

        // This test works only with the original java imaging, not with twelvemonkeys
        assertEquals(ColorSpace.TYPE_CMYK, image.getColorModel().getColorSpace().getType());
        assertEquals(BufferedImage.TYPE_CUSTOM, image.getType());
        assertEquals(4, image.getColorModel().getNumComponents());

        PDDocument document = new PDDocument();
        PDImageXObject ximage = JPEGFactory.createFromImage(document, image);
        validate(ximage, 8, 200, 200, "jpg", PDDeviceCMYK.INSTANCE.getName());
        // the samples are inverted, so a /Decode array is required
        assertTrue(Arrays.equals(new float[] { 1, 0, 1, 0, 1, 0, 1, 0 }, ximage.getDecode().toFloatArray()));

        // using the one created from the stream is more reliable than using "image"
        // because of flaws in converting CMYK to RGB
        // See https://stackoverflow.com/questions/19540064/
        BufferedImage expected = JPEGFactory.createFromStream(document, new ByteArrayInputStream(ba)).getImage();

        float meanAbsDiffPerPixel = computeMeanAbsDiffPerPixel(expected, ximage.getImage());
        assertTrue(meanAbsDiffPerPixel < 1);

        doWritePDF(document, ximage, testResultsDir, "PDFBOX-6235-cmyk.pdf");
    }

    // check whether it is possible to extract the jpeg stream exactly 
    // as it was passed to createFromStream
    private void checkJpegStream(File testResultsDir, String filename, InputStream expected)
            throws IOException
    {
        PDDocument doc = PDDocument.load(new File(testResultsDir, filename));
        PDImageXObject img =
                (PDImageXObject) doc.getPage(0).getResources().getXObject(COSName.getPDFName("Im1"));
        InputStream dctStream = img.createInputStream(Arrays.asList(COSName.DCT_DECODE.getName()));
        assertArrayEquals(IOUtils.toByteArray(expected), IOUtils.toByteArray(dctStream));
        expected.close();
        dctStream.close();
        doc.close();
    }

    private float computeMeanAbsDiffPerPixel(BufferedImage expected, BufferedImage actual)
    {
        // assumption: both sizes are identical
        int w = expected.getWidth();
        int h = expected.getHeight();
        long sum = 0;
        long count = 0;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                count += 3;
                Color expectedRGB = new Color(expected.getRGB(x, y));
                Color actualRGB = new Color(actual.getRGB(x, y));
                if (expectedRGB == actualRGB)
                {
                    continue;
                }
                sum += Math.abs(expectedRGB.getRed() - actualRGB.getRed());
                sum += Math.abs(expectedRGB.getGreen() - actualRGB.getGreen());
                sum += Math.abs(expectedRGB.getBlue() - actualRGB.getBlue());
            }
        }
        return sum / (float) count;
    }
}
