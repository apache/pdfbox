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
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.checkIdent;
import static org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage.validate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Unit tests for CCITTFactory
 *
 * @author Tilman Hausherr
 */
@Execution(ExecutionMode.CONCURRENT)
class CCITTFactoryTest
{
    private static final File TESTRESULTSDIR = new File("target/test-output/graphics");

    @BeforeAll
    static void setUp()
    {
        TESTRESULTSDIR.mkdirs();
    }

    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document,
     * RandomAccess reader) with a single page TIFF
     */
    @Test
    void testCreateFromRandomAccessSingle() throws IOException
    {
        String tiffG3Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg3.tif";
        String tiffG4Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4.tif";
        
        try (PDDocument document = new PDDocument())
        {
            PDImageXObject ximage3 = CCITTFactory.createFromFile(document, new File(tiffG3Path));
            validate(ximage3, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
            BufferedImage bim3 = ImageIO.read(new File(tiffG3Path));
            checkIdent(bim3, ximage3.getOpaqueImage(null, 1));
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(ximage3, 0, 0, ximage3.getWidth(), ximage3.getHeight());
            }
            
            PDImageXObject ximage4 = CCITTFactory.createFromFile(document, new File(tiffG4Path));
            validate(ximage4, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
            BufferedImage bim4 = ImageIO.read(new File(tiffG3Path));
            checkIdent(bim4, ximage4.getOpaqueImage(null, 1));
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(ximage4, 0, 0);
            }
            
            document.save(TESTRESULTSDIR + "/singletiff.pdf");
        }
        
        try (PDDocument document = Loader.loadPDF(new File(TESTRESULTSDIR, "singletiff.pdf")))
        {
            assertEquals(2, document.getNumberOfPages());
        }  
    }
    
    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document,
     * RandomAccess reader) with a multi page TIFF
     *
     * @throws IOException
     */
    @Test
    void testCreateFromRandomAccessMulti() throws IOException
    {
        String tiffPath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4multi.tif";
        
        try (ImageInputStream is = ImageIO.createImageInputStream(new File(tiffPath)))
        {
            ImageReader imageReader = ImageIO.getImageReaders(is).next();
            imageReader.setInput(is);
            int countTiffImages = imageReader.getNumImages(true);
            assertTrue(countTiffImages > 1);
            try (PDDocument document = new PDDocument())
            {
                int pdfPageNum = 0;
                while (true)
                {
                    PDImageXObject ximage = CCITTFactory.createFromFile(document, new File(tiffPath), pdfPageNum);
                    if (ximage == null)
                    {
                        break;
                    }
                    BufferedImage bim = imageReader.read(pdfPageNum);
                    validate(ximage, 1, bim.getWidth(), bim.getHeight(), "tiff", PDDeviceGray.INSTANCE.getName());
                    checkIdent(bim, ximage.getOpaqueImage(null, 1));
                    PDPage page = new PDPage(PDRectangle.A4);
                    float fX = ximage.getWidth() / page.getMediaBox().getWidth();
                    float fY = ximage.getHeight() / page.getMediaBox().getHeight();
                    float factor = Math.max(fX, fY);
                    document.addPage(page);
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
                    {
                        contentStream.drawImage(ximage, 0, 0, ximage.getWidth() / factor, ximage.getHeight() / factor);
                    }
                    ++pdfPageNum;
                }
                
                assertEquals(countTiffImages, pdfPageNum);
                
                document.save(TESTRESULTSDIR + "/multitiff.pdf");
            }
            try (PDDocument document = Loader.loadPDF(new File(TESTRESULTSDIR, "multitiff.pdf"), (String) null))
            {
                assertEquals(countTiffImages, document.getNumberOfPages());
            }
            imageReader.dispose();
        }
    }

    @Test
    void testCreateFromBufferedImage() throws IOException
    {
        String tiffG4Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4.tif";

        try (PDDocument document = new PDDocument())
        {
            BufferedImage bim = ImageIO.read(new File(tiffG4Path));
            PDImageXObject ximage3 = CCITTFactory.createFromImage(document, bim);
            validate(ximage3, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
            checkIdent(bim, ximage3.getOpaqueImage(null, 1));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(ximage3, 0, 0, ximage3.getWidth(), ximage3.getHeight());
            }

            document.save(TESTRESULTSDIR + "/singletifffrombi.pdf");
        }

        try (PDDocument document = Loader.loadPDF(new File(TESTRESULTSDIR, "singletifffrombi.pdf")))
        {
            assertEquals(1, document.getNumberOfPages());
        }  
    }    
    
    @Test
    void testCreateFromBufferedChessImage() throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            BufferedImage bim = new BufferedImage(343, 287, BufferedImage.TYPE_BYTE_BINARY);
            assertNotEquals((bim.getWidth() / 8) * 8, bim.getWidth()); // not mult of 8
            int col = 0;
            for (int x = 0; x < bim.getWidth(); ++x)
            {
                for (int y = 0; y < bim.getHeight(); ++y)
                {
                    bim.setRGB(x, y, col & 0xFFFFFF);
                    col = ~col;
                }
            }
            
            PDImageXObject ximage3 = CCITTFactory.createFromImage(document, bim);
            validate(ximage3, 1, 343, 287, "tiff", PDDeviceGray.INSTANCE.getName());
            checkIdent(bim, ximage3.getOpaqueImage(null, 1));
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(ximage3, 0, 0, ximage3.getWidth(), ximage3.getHeight());
            }
            
            document.save(TESTRESULTSDIR + "/singletifffromchessbi.pdf");
        }

        try (PDDocument document = Loader.loadPDF(new File(TESTRESULTSDIR, "singletifffromchessbi.pdf")))
        {
            assertEquals(1, document.getNumberOfPages());
        }
    }
    
    /**
     * Tests that CCITTFactory#createFromFile(PDDocument document, File file) doesn't lock the
     * source file
     *
     * @throws IOException
     */
    @Test
    void testCreateFromFileLock() throws IOException
    {
        // copy the source file to a temp directory, as we will be deleting it
        String tiffG3Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg3.tif";
        File copiedTiffFile = new File(TESTRESULTSDIR, "ccittg3.tif");
        Files.copy(new File(tiffG3Path).toPath(), copiedTiffFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (PDDocument document = new PDDocument())
        {
            CCITTFactory.createFromFile(document, copiedTiffFile);
            assertTrue(copiedTiffFile.delete());
        }
    }

    /**
     * Tests that CCITTFactory#createFromFile(PDDocument document, File file, int number) doesn't
     * lock the source file
     *
     * @throws IOException
     */
    @Test
    void testCreateFromFileNumberLock() throws IOException
    {
        // copy the source file to a temp directory, as we will be deleting it
        String tiffG3Path = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg3.tif";
        File copiedTiffFile = new File(TESTRESULTSDIR, "ccittg3n.tif");
        Files.copy(new File(tiffG3Path).toPath(), copiedTiffFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (PDDocument document = new PDDocument())
        {
            CCITTFactory.createFromFile(document, copiedTiffFile, 0);
            assertTrue(copiedTiffFile.delete());
        }
    }

    /**
     * Tests that byte/short tag values are read correctly (ignoring possible garbage in remaining
     * bytes).
     *
     * @throws IOException
     */
    @Test
    void testByteShortPaddedWithGarbage() throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            String basePath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg3-garbage-padded-fields";
            for (String ext : Arrays.asList(".tif", "-bigendian.tif"))
            {
                String tiffPath = basePath + ext;
                PDImageXObject ximage3 = CCITTFactory.createFromFile(document, new File(tiffPath));
                validate(ximage3, 1, 344, 287, "tiff", PDDeviceGray.INSTANCE.getName());
            }
        }
    }

    /**
     * PDFBOX-6164: test support of TIFF-files with FillOrder=2
     *
     * @throws IOException
     * @throws URISyntaxException 
     */
    @Test
    void testFillOrder2() throws IOException, URISyntaxException
    {
        byte[] ba;
        try (InputStream is = new URI("https://issues.apache.org/jira/secure/attachment/12558110/Wing.tif").
                toURL().openStream())
        {
            ba = IOUtils.toByteArray(is);
        }
        try (PDDocument document = new PDDocument())
        {
            PDImageXObject ximg = CCITTFactory.createFromByteArray(document, ba);
            validate(ximg, 1, 4575, 2232, "tiff", PDDeviceGray.INSTANCE.getName());
            BufferedImage bim = ImageIO.read(new ByteArrayInputStream(ba));
            checkIdent(bim, ximg.getOpaqueImage(null, 1));
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false))
            {
                contentStream.drawImage(ximg, 0, 0, ximg.getWidth() / 8, ximg.getHeight() / 8);
            }
            document.save(TESTRESULTSDIR + "/Wing.pdf");
        }

        try (PDDocument document = Loader.loadPDF(new File(TESTRESULTSDIR, "Wing.pdf")))
        {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    /**
     * Tests that CCITTFactory's private readlong() reads a TIFF LONG as an unsigned 32-bit
     * value. The previous implementation returned a (possibly negative) int, which was then
     * sign-extended when widened to long, corrupting IFD offsets/counts whose high bit is set
     * (e.g. 0x80000000 and above).
     *
     * @throws IOException
     */
    @Test
    void testReadLongIsUnsigned() throws IOException
    {
        // all bits set: 0xFFFFFFFF == 4294967295 as an unsigned TIFF LONG.
        // The buggy code returned the int -1, which as a long is -1, not 4294967295.
        byte[] allOnes = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        assertReadLongUnsigned('I', allOnes, 0xFFFFFFFFL);
        assertReadLongUnsigned('M', allOnes, 0xFFFFFFFFL);

        // only the top bit set, in each byte order: 0x80000000 == 2147483648 unsigned.
        // The buggy code returned the int Integer.MIN_VALUE, which sign-extends to a
        // large negative long instead of 2147483648.
        byte[] littleEndianTopBit = { 0x00, 0x00, 0x00, (byte) 0x80 };
        assertReadLongUnsigned('I', littleEndianTopBit, 0x80000000L);

        byte[] bigEndianTopBit = { (byte) 0x80, 0x00, 0x00, 0x00 };
        assertReadLongUnsigned('M', bigEndianTopBit, 0x80000000L);
    }

    private static void assertReadLongUnsigned(char endianess, byte[] bytes,
                                               long expected) throws IOException
    {
        try (RandomAccessRead raf = new RandomAccessReadBuffer(bytes))
        {
            long value = CCITTFactory.readlong(endianess, raf);
            assertEquals(expected, value);
            assertTrue(value >= 0, "TIFF LONG must be read as unsigned, not sign-extended");
        }
    }

    @Test
    void testReadShortIsUnsigned() throws IOException
    {
        assertReadShortIsUnsigned(65535, new byte[] {(byte) 0xFF, (byte) 0xFF}, 'I');
        assertReadShortIsUnsigned(128,   new byte[] {(byte) 0x80, (byte) 0x00}, 'I');
        assertReadShortIsUnsigned(32768, new byte[] {(byte) 0x00, (byte) 0x80}, 'I');
        assertReadShortIsUnsigned(256,   new byte[] {(byte) 0x00, (byte) 0x01}, 'I');
        assertReadShortIsUnsigned(1,     new byte[] {(byte) 0x01, (byte) 0x00}, 'I');

        assertReadShortIsUnsigned(65535, new byte[] {(byte) 0xFF, (byte) 0xFF}, 'M');
        assertReadShortIsUnsigned(128,   new byte[] {(byte) 0x00, (byte) 0x80}, 'M');
        assertReadShortIsUnsigned(32768, new byte[] {(byte) 0x80, (byte) 0x00}, 'M');
        assertReadShortIsUnsigned(256,   new byte[] {(byte) 0x01, (byte) 0x00}, 'M');
        assertReadShortIsUnsigned(1,     new byte[] {(byte) 0x00, (byte) 0x01}, 'M');
    }

    private static void assertReadShortIsUnsigned(long expected, byte[] bytes, char endianess) throws IOException
    {
        try (RandomAccessRead raf = new RandomAccessReadBuffer(bytes))
        {
            long value = CCITTFactory.readshort(endianess, raf);
            assertEquals(expected, value);
            assertTrue(value >= 0, "TIFF SHORT must be read as unsigned, not sign-extended");
        }
    }
}