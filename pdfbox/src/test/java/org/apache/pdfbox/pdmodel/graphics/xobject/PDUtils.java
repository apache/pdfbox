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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

/**
 *
 * @author Tilman Hausherr
 */
public class PDUtils
{
    static BufferedImage createInterestingImage(int type)
    {
        BufferedImage awtImage = new BufferedImage(256, 256, type);
        Graphics g = awtImage.getGraphics();
        g.setColor(Color.blue);
        g.fillRect(0, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.white);
        g.fillRect(awtImage.getWidth() / 3, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.red);
        g.fillRect(awtImage.getWidth() / 3 * 2, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.black);
        g.drawRect(0, 0, awtImage.getWidth() - 1, awtImage.getHeight() - 1);
        g.dispose();
        return awtImage;
    }

    /**
     * Check whether images are identical.
     *
     * @param expectedImage
     * @param actualImage
     */
    static void checkIdent(BufferedImage expectedImage, BufferedImage actualImage)
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
    static void doWritePDF(PDDocument document, PDXObjectImage ximage, File testResultsDir, String filename)
            throws IOException, COSVisitorException
    {
        File pdfFile = new File(testResultsDir, filename);

        // This part isn't really needed because this test doesn't break
        // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
        // if something goes wrong in the future and we want to have a PDF to open.
        int width = ximage.getWidth();
        int height = ximage.getHeight();

        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage, 150, 300, width, height);
        contentStream.drawXObject(ximage, 200, 350, width, height);
        contentStream.close();

        document.save(pdfFile);
        document.close();

        document = PDDocument.loadNonSeq(pdfFile, null);
        List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
        for (PDPage pdPage : pdPages)
        {
            pdPage.convertToImage();
        }
        document.close();
    }
        
    
}
