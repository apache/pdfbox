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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PDInlineImage
 *
 * @author Tilman Hausherr
 */
class PDInlineImageTest
{
    private static final File TESTRESULTSDIR = new File("target/test-output/graphics");

    @BeforeAll
    static void setUp()
    {
        TESTRESULTSDIR.mkdirs();
    }

    /**
     * Tests PDInlineImage#PDInlineImage(COSDictionary parameters, byte[] data,
     * Map<String, PDColorSpace> colorSpaces)
     */
    @Test
    void testInlineImage() throws IOException
    {
        COSDictionary dict = new COSDictionary();
        dict.setBoolean(COSName.IM, true);
        int width = 31;
        int height = 27;
        dict.setInt(COSName.W, width);
        dict.setInt(COSName.H, height);
        dict.setInt(COSName.BPC, 1);
        int rowbytes = width / 8;
        if (rowbytes * 8 < width)
        {
            // PDF spec:
            // If the number of data bits per row is not a multiple of 8, 
            // the end of the row is padded with extra bits to fill out the last byte. 
            ++rowbytes;
        }
        
        // draw a grid
        int datalen = rowbytes * height;
        byte[] data = new byte[datalen];
        for (int i = 0; i < datalen; ++i)
        {
            data[i] = (i / 4 % 2 == 0) ? (byte) Integer.parseInt("10101010", 2) : 0;
        }
        
        PDInlineImage inlineImage1 = new PDInlineImage(dict, data, null);
        assertTrue(inlineImage1.isStencil());
        assertEquals(width, inlineImage1.getWidth());
        assertEquals(height, inlineImage1.getHeight());
        assertEquals(1, inlineImage1.getBitsPerComponent());
        
        COSDictionary dict2 = new COSDictionary();
        dict2.addAll(dict);
        // use decode array to revert in image2
        COSArray decodeArray = new COSArray();
        decodeArray.add(COSInteger.ONE);
        decodeArray.add(COSInteger.ZERO);                
        dict2.setItem(COSName.DECODE, decodeArray);
     
        PDInlineImage inlineImage2 = new PDInlineImage(dict2, data, null);

        Paint paint = new Color(0, 0, 0);
        BufferedImage stencilImage = inlineImage1.getStencilImage(paint);
        assertEquals(width, stencilImage.getWidth());
        assertEquals(height, stencilImage.getHeight());

        BufferedImage stencilImage2 = inlineImage2.getStencilImage(paint);
        assertEquals(width, stencilImage2.getWidth());
        assertEquals(height, stencilImage2.getHeight());
        
        BufferedImage image1 = inlineImage1.getImage();
        assertEquals(width, image1.getWidth());
        assertEquals(height, image1.getHeight());

        BufferedImage image2 = inlineImage2.getImage();
        assertEquals(width, image2.getWidth());
        assertEquals(height, image2.getHeight());

        // write and read
        boolean writeOk = ImageIO.write(image1, "png", new File(TESTRESULTSDIR + "/inline-grid1.png"));
        assertTrue(writeOk);
        BufferedImage bim1 = ImageIO.read(new File(TESTRESULTSDIR + "/inline-grid1.png"));
        assertNotNull(bim1);
        assertEquals(width, bim1.getWidth());
        assertEquals(height, bim1.getHeight());

        writeOk = ImageIO.write(image2, "png", new File(TESTRESULTSDIR + "/inline-grid2.png"));
        assertTrue(writeOk);
        BufferedImage bim2 = ImageIO.read(new File(TESTRESULTSDIR + "/inline-grid2.png"));
        assertNotNull(bim2);
        assertEquals(width, bim2.getWidth());
        assertEquals(height, bim2.getHeight());

        // compare: pixels with even coordinates are white (FF), all others are black (0)
        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                if (x % 2 == 0 && y % 2 == 0)
                {
                    assertEquals(0xFFFFFF, bim1.getRGB(x, y) & 0xFFFFFF);
                }
                else
                {
                    assertEquals(0, bim1.getRGB(x, y) & 0xFFFFFF);
                }
            }
        }
        
        // compare: pixels with odd coordinates are white (FF), all others are black (0)
        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                if (x % 2 == 0 && y % 2 == 0)
                {
                    assertEquals(0, bim2.getRGB(x, y) & 0xFFFFFF);
                }
                else
                {
                    assertEquals(0xFFFFFF, bim2.getRGB(x, y) & 0xFFFFFF);
                }
            }
        }        

        File pdfFile = new File(TESTRESULTSDIR, "inline.pdf");

        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(inlineImage1, 150, 400);
                contentStream.drawImage(inlineImage1, 150, 500, inlineImage1.getWidth() * 2, inlineImage1.getHeight() * 2);
                contentStream.drawImage(inlineImage1, 150, 600, inlineImage1.getWidth() * 4, inlineImage1.getHeight() * 4);
                contentStream.drawImage(inlineImage2, 350, 400);
                contentStream.drawImage(inlineImage2, 350, 500, inlineImage2.getWidth() * 2, inlineImage2.getHeight() * 2);
                contentStream.drawImage(inlineImage2, 350, 600, inlineImage2.getWidth() * 4, inlineImage2.getHeight() * 4);
            }
            document.save(pdfFile);
        }

        try (PDDocument document = Loader.loadPDF(pdfFile))
        {
            new PDFRenderer(document).renderImage(0);
        }
    }

    // 3 Tests for PDFBOX-5360 with very small images (the last one based on a comment 
    // by Oliver Schmidtmer at the end of PDFBOX-5340). All images are fully black bars.

    @Test
    void testShortCCITT1() throws IOException
    {
        byte ba[] = { 8, 0x10, 0x20, 0x40, (byte) 0x81, 2, 4, 8, 0x10, 0, 0x40, 4, 0, 0x40, 4, 0, 0x40, 4 };
        doInlineCcittImage(23, 10, ba);
    }

    @Test
    void testShortCCITT2() throws IOException
    {
        byte ba[] = { 8, 0x10, 0x20, 0x40, (byte) 0x81, 2, 0, 8, 0, (byte) 0x80, 8, 8, (byte) 0x80, 8, 0, (byte) 0x80};
        doInlineCcittImage(23, 7, ba);
    }

    @Test
    void testShortCCITT3() throws IOException
    {
        byte ba[] = { 103, 44, 103, 44, 103, 44, 103, 44, 0, 16, 1, 0, 16, 1, 0, 16, 1, 10};
        doInlineCcittImage(683, 4, ba);
    }

    private void doInlineCcittImage(int width, int height, byte[] ba) throws IOException
    {
        COSDictionary dict = new COSDictionary();
        dict.setInt(COSName.W, width);
        dict.setInt(COSName.H, height);
        dict.setInt(COSName.BPC, 1);
        COSArray array = new COSArray();
        array.add(COSInteger.ONE);
        array.add(COSInteger.ZERO);
        dict.setItem(COSName.D, array);
        dict.setBoolean(COSName.IM, true);
        dict.setItem(COSName.F, COSName.CCITTFAX_DECODE_ABBREVIATION);
        COSDictionary dict2 = new COSDictionary();
        dict2.setInt(COSName.COLUMNS, dict.getInt(COSName.W));
        dict.setItem(COSName.DP, dict2);
        PDInlineImage inlineImage = new PDInlineImage(dict, ba, null);
        Assertions.assertEquals(true, inlineImage.isStencil());
        Assertions.assertEquals(false, inlineImage.isEmpty());
        Assertions.assertEquals(false, inlineImage.getInterpolate());
        Assertions.assertEquals(dict, inlineImage.getCOSObject());
        Assertions.assertEquals(PDDeviceGray.INSTANCE, inlineImage.getColorSpace());
        Assertions.assertEquals(1, inlineImage.getBitsPerComponent());
        Assertions.assertEquals("tiff", inlineImage.getSuffix());
        BufferedImage bim = inlineImage.getImage();
        Assertions.assertEquals(width, bim.getWidth());
        Assertions.assertEquals(height, bim.getHeight());
        Assertions.assertEquals(inlineImage.getWidth(), bim.getWidth());
        Assertions.assertEquals(inlineImage.getHeight(), bim.getHeight());
        Assertions.assertEquals(BufferedImage.TYPE_BYTE_GRAY, bim.getType());
        DataBufferByte dbb = (DataBufferByte) bim.getRaster().getDataBuffer();
        Assertions.assertEquals(bim.getWidth() * bim.getHeight(), dbb.getSize());
        byte[] data = dbb.getData();
        for (int i = 0; i < data.length; ++i)
        {
            Assertions.assertEquals(0, data[i]);
        }
    }
}
