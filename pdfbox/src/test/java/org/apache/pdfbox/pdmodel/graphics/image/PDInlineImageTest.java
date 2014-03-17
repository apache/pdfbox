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
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

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
        COSDictionary dict = new COSDictionary();
        dict.setBoolean(COSName.IM, true);
        dict.setInt(COSName.W, 30);
        dict.setInt(COSName.H, 28);
        dict.setInt(COSName.BPC, 1);
        byte[] data = new byte[113];
        PDInlineImage inlineImage = new PDInlineImage(dict, data, null);
        assertTrue(inlineImage.isStencil());
        assertEquals(30, inlineImage.getWidth());
        assertEquals(28, inlineImage.getHeight());
        assertEquals(1, inlineImage.getBitsPerComponent());
        assertEquals(data.length, inlineImage.getStream().getLength());

        Paint paint = new Color(0, 0, 0);
        BufferedImage stencilImage = inlineImage.getStencilImage(paint);
        assertEquals(30, stencilImage.getWidth());
        assertEquals(28, stencilImage.getHeight());

        BufferedImage image = inlineImage.getImage();
        assertEquals(30, image.getWidth());
        assertEquals(28, image.getHeight());
        
        boolean writeOk = ImageIOUtil.writeImage(image, "png", new NullOutputStream());
        assertTrue(writeOk);
    }
}
