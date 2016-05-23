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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Helper class to do some validations for PDImageXObject.
 *
 * @author Tilman Hausherr
 */
public class ValidateXImage
{
    public static void validate(PDImageXObject ximage, int bpc, int width, int height, String format, String colorSpaceName) throws IOException
    {
        // check the dictionary
        assertNotNull(ximage);
        COSStream cosStream = ximage.getCOSObject();
        assertNotNull(cosStream);
        assertEquals(COSName.XOBJECT, cosStream.getItem(COSName.TYPE));
        assertEquals(COSName.IMAGE, cosStream.getItem(COSName.SUBTYPE));
        assertTrue(ximage.getCOSObject().getLength() > 0);
        assertEquals(bpc, ximage.getBitsPerComponent());
        assertEquals(width, ximage.getWidth());
        assertEquals(height, ximage.getHeight());
        assertEquals(format, ximage.getSuffix());
        assertEquals(colorSpaceName, ximage.getColorSpace().getName());

        // check the image
        assertNotNull(ximage.getImage());
        assertEquals(ximage.getWidth(), ximage.getImage().getWidth());
        assertEquals(ximage.getHeight(), ximage.getImage().getHeight());

        boolean writeOk = ImageIO.write(ximage.getImage(),
                format, new ByteArrayOutputStream());
        assertTrue(writeOk);
        writeOk = ImageIO.write(ximage.getOpaqueImage(),
                format, new ByteArrayOutputStream());
        assertTrue(writeOk);
    }

    static int colorCount(BufferedImage bim)
    {
        Set<Integer> colors = new HashSet<Integer>();
        int w = bim.getWidth();
        int h = bim.getHeight();
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                colors.add(bim.getRGB(x, y));
            }
        }
        return colors.size();
    }

    // write image twice (overlapped) in document, close document and re-read PDF
    static void doWritePDF(PDDocument document, PDImageXObject ximage, File testResultsDir, String filename)
            throws IOException
    {
        File pdfFile = new File(testResultsDir, filename);

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.

        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false);
        contentStream.drawImage(ximage, 150, 300);
        contentStream.drawImage(ximage, 200, 350);
        contentStream.close();
        
        // check that the resource map is up-to-date
        assertEquals(1, count(document.getPage(0).getResources().getXObjectNames()));

        document.save(pdfFile);
        document.close();

        document = PDDocument.load(pdfFile);
        assertEquals(1, count(document.getPage(0).getResources().getXObjectNames()));
        new PDFRenderer(document).renderImage(0);
        document.close();
    }

    private static int count(Iterable<COSName> iterable)
    {
        int count = 0;
        for (COSName name : iterable)
        {
            count++;
        }
        return count;
    }

    /**
     * Check whether the images are identical.
     *
     * @param expectedImage
     * @param actualImage
     */
    public static void checkIdent(BufferedImage expectedImage, BufferedImage actualImage)
    {
        String errMsg = "";

        int w = expectedImage.getWidth();
        int h = expectedImage.getHeight();
        assertEquals(w, actualImage.getWidth());
        assertEquals(h, actualImage.getHeight());
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                if (expectedImage.getRGB(x, y) != actualImage.getRGB(x, y))
                {
                    errMsg = String.format("(%d,%d) %08X != %08X", x, y, expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
                }
                assertEquals(errMsg, expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
            }
        }
    }

    

}
