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
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import junit.framework.TestCase;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.checkIdent;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;

/**
 * Unit tests for CCITTFactory
 *
 * @author Tilman Hausherr
 */
public class CCITTFactoryTest extends TestCase
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
    public void testCreateFromRandomAccessSingle() throws IOException
    {
        String tiffG3Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg3.tif";
        String tiffG4Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4.tif";
        
        PDDocument document = new PDDocument();
        RandomAccess reader = new RandomAccessFile(new File(tiffG3Path), "r");
        PDImageXObject ximage3 = CCITTFactory.createFromRandomAccess(document, reader);
        validate(ximage3, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
        BufferedImage bim3 = ImageIO.read(new File(tiffG3Path));
        checkIdent(bim3, ximage3.getOpaqueImage());
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage3, 0, 0, ximage3.getWidth(), ximage3.getHeight());
        contentStream.close();

        reader = new RandomAccessFile(new File(tiffG4Path), "r");
        PDImageXObject ximage4 = CCITTFactory.createFromRandomAccess(document, reader);
        validate(ximage4, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
        BufferedImage bim4 = ImageIO.read(new File(tiffG3Path));
        checkIdent(bim4, ximage4.getOpaqueImage());
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        contentStream = new PDPageContentStream(document, page, true, false);
        contentStream.drawXObject(ximage4, 0, 0, ximage4.getWidth(), ximage4.getHeight());
        contentStream.close();
       
        document.save(testResultsDir + "/singletiff.pdf");
        document.close();
        
        document = PDDocument.load(new File(testResultsDir, "singletiff.pdf"), null);
        assertEquals(2, document.getNumberOfPages());
        
        document.close();  
    }
    
    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document,
     * RandomAccess reader) with a multi page TIFF
     */
    public void testCreateFromRandomAccessMulti() throws IOException
    {
        String tiffPath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4multi.tif";
        
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
            PDImageXObject ximage = CCITTFactory.createFromRandomAccess(document, reader, pdfPageNum);
            if (ximage == null)
            {
                break;
            }
            BufferedImage bim = imageReader.read(pdfPageNum);
            validate(ximage, 1, bim.getWidth(), bim.getHeight(), "tiff", PDDeviceGray.INSTANCE.getName());
            checkIdent(bim, ximage.getOpaqueImage());
            PDPage page = new PDPage(PDRectangle.A4);
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
        
        document = PDDocument.load(new File(testResultsDir, "multitiff.pdf"), null);
        assertEquals(countTiffImages, document.getNumberOfPages());
        
        document.close();  
        imageReader.dispose();
    }
}
