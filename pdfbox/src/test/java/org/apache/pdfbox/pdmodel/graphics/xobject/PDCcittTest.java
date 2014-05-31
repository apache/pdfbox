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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import junit.framework.TestCase;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import static org.apache.pdfbox.pdmodel.graphics.xobject.PDJpegTest.validate;
import static org.apache.pdfbox.pdmodel.graphics.xobject.PDUtils.checkIdent;
import static org.apache.pdfbox.pdmodel.graphics.xobject.PDUtils.doWritePDF;

/**
 *
 * @author Tilman Hausherr
 */
public class PDCcittTest extends TestCase
{
    private final File testResultsDir = new File("target/test-output/graphics");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document,
     * RandomAccess reader) with a single page TIFF
     */
    public void testCreateFromRandomAccessSingle() throws IOException, COSVisitorException
    {
        String tiffPath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/xobject/ccittg4.tif";
        
        PDDocument document = new PDDocument();
        RandomAccess reader = new RandomAccessFile(new File(tiffPath), "r");
        PDXObjectImage ximage = new PDCcitt(document, reader);
        validate(ximage, 1, 344, 287, "tiff", PDDeviceGray.NAME);
        
        doWritePDF(document, ximage, testResultsDir, "singletiff.pdf");
    }
    
    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document,
     * RandomAccess reader) with a multi page TIFF
     */
    public void testCreateFromRandomAccessMulti() throws IOException, COSVisitorException
    {
        String tiffPath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/xobject/ccittg4multi.tif";
        
        ImageInputStream is = ImageIO.createImageInputStream(new File(tiffPath));
        ImageReader imageReader = ImageIO.getImageReaders(is).next();
        imageReader.setInput(is);
        int countTiffImages = imageReader.getNumImages(true);
        assertTrue(countTiffImages > 1);
        
        PDDocument document = new PDDocument();
        RandomAccess reader = new RandomAccessFile(new File(tiffPath), "r");
        
        int pdfPageNum = 0;
        while (true)
        {
            PDXObjectImage ximage = null;
            try
            {
                ximage = new PDCcitt(document, reader, pdfPageNum);
            }
            catch (IllegalArgumentException e)
            {
                break;
            }
            BufferedImage bim = imageReader.read(pdfPageNum);
            validate(ximage, 1, bim.getWidth(), bim.getHeight(), "tiff", PDDeviceGray.NAME);
            checkIdent(bim, ximage.getRGBImage());
            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            float fX = ximage.getWidth() / page.getMediaBox().getWidth();
            float fY = ximage.getHeight() / page.getMediaBox().getHeight();
            float factor = Math.max(fX, fY);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
            contentStream.drawXObject(ximage, 0, 0, ximage.getWidth() / factor, ximage.getHeight() / factor);
            contentStream.close();
            ++pdfPageNum;
        }
        
        assertEquals(countTiffImages, pdfPageNum);

        document.save(testResultsDir + "/multitiff.pdf");
        document.close();
        
        document = PDDocument.loadNonSeq(new File(testResultsDir, "multitiff.pdf"), null);
        List pages = document.getDocumentCatalog().getAllPages();
        assertEquals(countTiffImages, pages.size());
        
        document.close();  
        imageReader.dispose();
    }
    
}
