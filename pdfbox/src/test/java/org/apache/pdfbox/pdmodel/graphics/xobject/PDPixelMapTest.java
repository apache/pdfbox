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

import java.awt.Color;
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
import org.apache.pdfbox.util.ImageIOUtil;

/**
 *
 * @author Tilman Hausherr
 */
public class PDPixelMapTest extends TestCase
{
    private final File testResultsDir = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests RGB PDPixelMapTest() with color, gray and bitonal image
     *
     * @throws java.io.IOException
     */
    public void testCreateLosslessFromImageRGB() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

        PDXObjectImage ximage = new PDPixelMap(document, image);
        validate(ximage, 8, 344, 287, "png", PDDeviceRGB.NAME);
        checkIdent(image, ximage.getRGBImage());

        // Create a grayscale image
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ximage = new PDPixelMap(document, grayImage);
        validate(ximage, 8, 344, 287, "png", PDDeviceRGB.NAME);
        checkIdent(grayImage, ximage.getRGBImage());

        // Create a bitonal image
        BufferedImage bitonalImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        g = bitonalImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ximage = new PDPixelMap(document, bitonalImage);
        checkIdent(bitonalImage, ximage.getRGBImage());
        validate(ximage, 1, 344, 287, "png", PDDeviceGray.NAME);
        document.close();
    }

    /**
     * Tests RGB PDPixelMapTest() with image from a bitonal GIF
     *
     * @throws java.io.IOException
     */
    public void testCreateLosslessFromBitonalGIF() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage imageFromBitonalGif = ImageIO.read(this.getClass().getResourceAsStream("bitonal.gif"));

        PDXObjectImage ximage = new PDPixelMap(document, imageFromBitonalGif);
        checkIdent(imageFromBitonalGif, ximage.getRGBImage());
        validate(ximage, 1, 344, 287, "png", PDDeviceGray.NAME);
        document.close();
    }

    /**
     * Tests RGB PDPixelMapTest() with TYPE_4BYTE_ABGR image.
     *
     * @throws java.io.IOException
     */
    public void testCreateLossless4BYTE_ABGR() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        BufferedImage awtImage = new BufferedImage(300, 300, BufferedImage.TYPE_4BYTE_ABGR);

        // draw something
        Graphics g = awtImage.getGraphics();
        g.setColor(Color.blue);
        g.fillRect(0, 0, 100, 300);
        g.setColor(Color.white);
        g.fillRect(100, 0, 100, 300);
        g.setColor(Color.red);
        g.fillRect(200, 0, 100, 300);
        g.setColor(Color.black);
        g.drawRect(0, 0, 299, 299);
        g.dispose();

        PDPixelMap ximage = new PDPixelMap(document, awtImage);
        validate(ximage, 8, 300, 300, "png", PDDeviceRGB.NAME);
        validate(ximage.getSMaskImage(), 8, 300, 300, "png", PDDeviceGray.NAME);
        assertEquals(ximage.getColorSpace().getName(), PDDeviceRGB.NAME);
        assertEquals(ximage.getSMaskImage().getColorSpace().getName(), PDDeviceGray.NAME);
        checkIdent(awtImage, ximage.getRGBImage());

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.drawXObject(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
        contentStream.close();
        File pdfFile = new File(testResultsDir, "4babgr.pdf");
        document.save(pdfFile);
        document.close();
        document = PDDocument.loadNonSeq(pdfFile, null);
        document.close();
    }

    /**
     * Tests RGB PDPixelMapTest() with image from a color GIF
     *
     * @throws java.io.IOException
     */
    public void testCreateLosslessFromColorGIF() throws IOException
    {
        PDDocument document = new PDDocument();
        BufferedImage imageFromColorGif = ImageIO.read(this.getClass().getResourceAsStream("color.gif"));
        PDXObjectImage ximage = new PDPixelMap(document, imageFromColorGif);
        checkIdent(imageFromColorGif, ximage.getRGBImage());
        validate(ximage, 8, 344, 287, "png", PDDeviceRGB.NAME);
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

    /**
     * Check whether images are identical.
     *
     * @param expectedImage
     * @param actualImage
     */
    private void checkIdent(BufferedImage expectedImage, BufferedImage actualImage)
    {
        String errMsg = "";

        int w = expectedImage.getWidth();
        int h = expectedImage.getHeight();
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                if ((expectedImage.getRGB(x, y) & 0xFFFFFF) != (actualImage.getRGB(x, y) & 0xFFFFFF))
                {
                    errMsg = String.format("(%d,%d) %X != %X", x, y, expectedImage.getRGB(x, y) & 0xFFFFFF, actualImage.getRGB(x, y) & 0xFFFFFF);
                }
                assertEquals(errMsg, expectedImage.getRGB(x, y) & 0xFFFFFF, actualImage.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }
}
