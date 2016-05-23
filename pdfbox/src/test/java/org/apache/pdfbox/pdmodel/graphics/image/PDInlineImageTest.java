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
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Unit tests for PDInlineImage
 *
 * @author Tilman Hausherr
 */
public class PDInlineImageTest extends TestCase
{
    private final File testResultsDir = new File("target/test-output/graphics");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests PDInlineImage#PDInlineImage(COSDictionary parameters, byte[] data,
     * Map<String, PDColorSpace> colorSpaces)
     */
    public void testInlineImage() throws IOException
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
        boolean writeOk = ImageIO.write(image1, "png",
                new FileOutputStream(new File(testResultsDir + "/inline-grid1.png")));
        assertTrue(writeOk);
        BufferedImage bim1 = ImageIO.read(new File(testResultsDir + "/inline-grid1.png"));
        assertNotNull(bim1);
        assertEquals(width, bim1.getWidth());
        assertEquals(height, bim1.getHeight());
        
        writeOk = ImageIO.write(image2, "png",
                new FileOutputStream(new File(testResultsDir + "/inline-grid2.png")));
        assertTrue(writeOk);
        BufferedImage bim2 = ImageIO.read(new File(testResultsDir + "/inline-grid2.png"));
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

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false);
        contentStream.drawImage(inlineImage1, 150, 400);
        contentStream.drawImage(inlineImage1, 150, 500, inlineImage1.getWidth() * 2, inlineImage1.getHeight() * 2);
        contentStream.drawImage(inlineImage1, 150, 600, inlineImage1.getWidth() * 4, inlineImage1.getHeight() * 4);
        contentStream.drawImage(inlineImage2, 350, 400);
        contentStream.drawImage(inlineImage2, 350, 500, inlineImage2.getWidth() * 2, inlineImage2.getHeight() * 2);
        contentStream.drawImage(inlineImage2, 350, 600, inlineImage2.getWidth() * 4, inlineImage2.getHeight() * 4);
        contentStream.close();

        File pdfFile = new File(testResultsDir, "inline.pdf");
        document.save(pdfFile);
        document.close();

        document = PDDocument.load(pdfFile);
        new PDFRenderer(document).renderImage(0);
        document.close();

    }
}

