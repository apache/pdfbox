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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Unit tests for PDInlineImage
 *
 * @author Tilman Hausherr
 */
public class PDInlineImageTest extends TestCase
{
    /**
     * Tests PDInlineImage#PDInlineImage(COSDictionary parameters, byte[] data,
     * Map<String, PDColorSpace> colorSpaces)
     */
    public void testInlineImage() throws IOException
    {
        String outDir = "target/test-output/graphics";

        new File(outDir).mkdirs();
        if (!new File(outDir).exists())
        {
            throw new IOException("could not create output directory");
        }

        COSDictionary dict = new COSDictionary();
        dict.setBoolean(COSName.IM, true);
        int w = 30;
        int h = 28;
        dict.setInt(COSName.W, w);
        dict.setInt(COSName.H, h);
        dict.setInt(COSName.BPC, 1);
        int rowbytes = w / 8;
        if (rowbytes * 8 < w)
        {
            // PDF spec:
            // If the number of data bits per row is not a multiple of 8, 
            // the end of the row is padded with extra bits to fill out the last byte. 
            ++rowbytes;
        }
        
        // draw a grid
        int datalen = rowbytes * h;
        byte[] data = new byte[datalen];
        for (int i = 0; i < datalen; ++i)
        {
            data[i] = (i / 4 % 2 == 0) ? (byte) Integer.parseInt("10101010", 2) : 0;
        }
        PDInlineImage inlineImage = new PDInlineImage(dict, data, null);
        assertTrue(inlineImage.isStencil());
        assertEquals(w, inlineImage.getWidth());
        assertEquals(h, inlineImage.getHeight());
        assertEquals(1, inlineImage.getBitsPerComponent());
        assertEquals(data.length, inlineImage.getStream().getLength());

        Paint paint = new Color(0, 0, 0);
        BufferedImage stencilImage = inlineImage.getStencilImage(paint);
        assertEquals(w, stencilImage.getWidth());
        assertEquals(h, stencilImage.getHeight());

        BufferedImage image = inlineImage.getImage();
        assertEquals(w, image.getWidth());
        assertEquals(h, image.getHeight());

        // write and read
        boolean writeOk = ImageIOUtil.writeImage(image, "png", 
                new FileOutputStream(new File(outDir + "/inline-grid.png")));
        assertTrue(writeOk);
        BufferedImage bim = ImageIO.read(new File(outDir + "/inline-grid.png"));
        assertNotNull(bim);
        assertEquals(w, bim.getWidth());
        assertEquals(h, bim.getHeight());

        // compare: pixels with even coordinates are white (FF), all others are black (0)
        for (int x = 0; x < w; ++x)
        {
            for (int y = 0; y < h; ++y)
            {
                if (x % 2 == 0 && y % 2 == 0)
                {
                    assertEquals(0xFFFFFF, bim.getRGB(x, y) & 0xFFFFFF);
                }
                else
                    assertEquals(0, bim.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }
}

