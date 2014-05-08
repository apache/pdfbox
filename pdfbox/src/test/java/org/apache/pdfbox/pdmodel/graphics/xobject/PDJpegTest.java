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

package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import static org.apache.pdfbox.pdmodel.graphics.xobject.PDUtils.checkIdent;
import static org.apache.pdfbox.pdmodel.graphics.xobject.PDUtils.createInterestingImage;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 *
 * @author Tilman Hausherr
 */
public class PDJpegTest extends TestCase
{
    private final File testResultsDir = new File("target/test-output/graphics");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests RGB PDJpegTest() with color and gray images
     *
     * @throws java.io.IOException
     */
    public void testCreateJpegFromImages() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("jpeg.jpg"));

        PDXObjectImage ximage = new PDJpeg(document, image);
        validate(ximage, 8, image.getWidth(), image.getHeight(), "jpg", PDDeviceRGB.NAME);

        // Create a grayscale image
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ximage = new PDJpeg(document, grayImage);
        validate(ximage, 8, grayImage.getWidth(), grayImage.getHeight(), "jpg", PDDeviceGray.NAME);

        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with color file
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpegFromRGBImageFile() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("jpeg.jpg"));

        PDXObjectImage ximage = new PDJpeg(document, this.getClass().getResourceAsStream("jpeg.jpg"));
        validate(ximage, 8, image.getWidth(), image.getHeight(), "jpg", PDDeviceRGB.NAME);

        // this can be checked for ident, because the actual jpeg stream is stored
        PDUtils.checkIdent(image, ximage.getRGBImage());
        
        assertNull(ximage.getSMaskImage());

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpegfile.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with gray file
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpegFromGrayImageFile() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("jpeg256.jpg"));

        PDXObjectImage ximage = new PDJpeg(document, this.getClass().getResourceAsStream("jpeg256.jpg"));
        validate(ximage, 8, image.getWidth(), image.getHeight(), "jpg", PDDeviceGray.NAME);

        // this can be checked for ident, because the actual jpeg stream is stored
        checkIdent(image, ximage.getRGBImage()); 
        
        assertNull(ximage.getSMaskImage());

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpeg256file.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    public void testCreateJpegFromImageGray() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
        int w = image.getWidth();
        int h = image.getHeight();
        for (int x = 0; x < w; ++x)
        {
            for (int y = 0; y < h; ++y)
            {
                int color = ((x + y) / 2) & 0xFF;
                color += (color << 8) + (color << 16);
                image.setRGB(x, y, color);
            }
        }
        PDXObjectImage ximage = new PDJpeg(document, image);
        validate(ximage, 8, w, h, "jpg", PDDeviceGray.NAME);
        assertNull(ximage.getSMaskImage());
        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with TYPE_4BYTE_ABGR image.
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpeg4BYTE_ABGR() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage awtImage = createInterestingImage(BufferedImage.TYPE_4BYTE_ABGR);

        for (int x = 0; x < awtImage.getWidth(); ++x)
        {
            for (int y = 0; y < awtImage.getHeight(); ++y)
            {
                awtImage.setRGB(x, y, (awtImage.getRGB(x, y) & 0xFFFFFF) | ((y / 10 * 10) << 24));
            }
        }

        PDJpeg ximage = new PDJpeg(document, awtImage);
        validate(ximage, 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceRGB.NAME);
        validate(ximage.getSMaskImage(), 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceGray.NAME);

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpeg-4babgr.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with TYPE_INT_ARGB image.
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpegINT_ARGB() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage awtImage = createInterestingImage(BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < awtImage.getWidth(); ++x)
        {
            for (int y = 0; y < awtImage.getHeight(); ++y)
            {
                awtImage.setRGB(x, y, (awtImage.getRGB(x, y) & 0xFFFFFF) | ((y / 10 * 10) << 24));
            }
        }

        PDJpeg ximage = new PDJpeg(document, awtImage);
        validate(ximage, 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceRGB.NAME);
        validate(ximage.getSMaskImage(), 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceGray.NAME);

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpeg-intargb.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with TYPE_INT_RGB image.
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpegINT_RGB() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage awtImage = createInterestingImage(BufferedImage.TYPE_INT_RGB);

        PDJpeg ximage = new PDJpeg(document, awtImage);
        validate(ximage, 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceRGB.NAME);
        assertNull(ximage.getSMaskImage());

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpeg-intrgb.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    /**
     * Tests RGB PDJpegTest() with TYPE_INT_BGR image.
     *
     * @throws java.io.IOException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public void testCreateJpegINT_BGR() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage awtImage = createInterestingImage(BufferedImage.TYPE_INT_BGR);

        PDJpeg ximage = new PDJpeg(document, awtImage);
        validate(ximage, 8, awtImage.getWidth(), awtImage.getHeight(), "jpg", PDDeviceRGB.NAME);
        assertNull(ximage.getSMaskImage());

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.drawXObject(ximage, 200, 350, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "jpeg-intbgr.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }
    
    /**
     * Tests RGB PDJpegTest() with image from a color GIF
     *
     * @throws java.io.IOException
     */
    public void testCreateJpegFromColorGIF() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage imageFromColorGif = ImageIO.read(this.getClass().getResourceAsStream("color.gif"));
        PDXObjectImage ximage = new PDJpeg(document, imageFromColorGif);
        validate(ximage, 8, 344, 287, "jpg", PDDeviceRGB.NAME);
        document.close();
    }

    static public void validate(PDXObjectImage ximage, int bpc, int width, int height, String format, String colorSpaceName) throws IOException
    {
        // check the dictionary
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(bpc, ximage.getBitsPerComponent());
        assertEquals(width, ximage.getWidth());
        assertEquals(height, ximage.getHeight());
        assertEquals(format, ximage.getSuffix());
        assertEquals(colorSpaceName, ximage.getColorSpace().getName());

        // check the image
        assertNotNull(ximage.getRGBImage());
        assertEquals(ximage.getWidth(), ximage.getRGBImage().getWidth());
        assertEquals(ximage.getHeight(), ximage.getRGBImage().getHeight());

        boolean writeOk = ImageIOUtil.writeImage(ximage.getRGBImage(), format, new NullOutputStream());
        assertTrue(writeOk);
    }

}
