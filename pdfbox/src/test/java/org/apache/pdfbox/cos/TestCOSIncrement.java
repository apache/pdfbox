/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.cos;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ConcurrentModificationException;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import org.junit.jupiter.api.BeforeAll;

class TestCOSIncrement
{
    @BeforeAll
    static void init()
    {
        new File("target/test-output").mkdirs();
    }

    // TODO Very basic and primitive test - add in depth testing for all this.
    /**
     * Create a document from scratch - incrementally making changes - checking results of previous steps.
     */
    @Test
    void testIncrementallyCreateDocument()
    {
        byte[] documentData = new byte[0];

        // Add page 1.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = assertDoesNotThrow(
                (ThrowingSupplier<PDDocument>) PDDocument::new, "Creating the document failed.")
        )
        {
            document.addPage(new PDPage(new PDRectangle(100, 100)));
            document.save(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Add page 2 and 3.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertEquals(1, document.getNumberOfPages(), "Document should have contained 1 page.");
            document.addPage(new PDPage(new PDRectangle(200, 200)));
            document.addPage(new PDPage(new PDRectangle(100, 100)));
            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Remove page 2.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertEquals(3, document.getNumberOfPages(), "Document should have contained 3 pages.");
            document.removePage(document.getPage(1));
            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Add an image to page 1.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertNotEquals(200, document.getPage(1).getMediaBox().getWidth(), "Page 2 removal failed.");
            assertEquals(2, document.getNumberOfPages(), "Document should have contained 2 pages.");
            assertFalse(document.getPage(0).hasContents(), "Page 1 should not have had contents.");
            assertNull(document.getPage(0).getResources(), "Page 1 should not have contained resources");
            assertFalse(document.getPage(1).hasContents(), "Page 2 should not have had contents.");
            assertNull(document.getPage(1).getResources(), "Page 2 should not have contained resources");
            try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0)))
            {
                URL imageResource = TestCOSIncrement.class.getResource("simple.png");
                assertNotNull(imageResource, "Image resource not found.");
                File image = assertDoesNotThrow(() -> new File(imageResource.toURI()),
                    "Image file could not be loaded");
                contentStream.drawImage(PDImageXObject.createFromFileByExtension(image, document), 15, 20);
            }
            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Write a text to page 2.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertTrue(document.getPage(0).hasContents(), "Page 1 should have had contents.");
            assertNotNull(document.getPage(0).getResources(), "Page 1 should have contained resources");
            assertFalse(document.getPage(0).getResources().getFontNames().iterator().hasNext(),
                "Page 1 should not have contained a font");
            assertTrue(document.getPage(0).getResources().getXObjectNames().iterator().hasNext(),
                "Page 1 should have contained an XObject");
            assertFalse(document.getPage(1).hasContents(), "Page 2 should not have had contents.");
            assertNull(document.getPage(1).getResources(), "Page 2 should not have contained resources");
            try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(1)))
            {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 20);
                contentStream.newLineAtOffset(20, 50);
                contentStream.showText("Page 2");
                contentStream.endText();
            }
            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // add an annotation to page 2.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertTrue(document.getPage(0).hasContents(), "Page 1 should have had contents.");
            assertNotNull(document.getPage(0).getResources(), "Page 1 should have contained resources");
            assertNotNull(document.getPage(1).getResources(), "Page 2 should have contained resources");
            assertFalse(document.getPage(1).getAnnotations().size() > 0,
                "Page 2 should not have contained an annotation.");
            assertTrue(document.getPage(1).hasContents(), "Page 2 should have had contents.");
            assertTrue(document.getPage(1).getResources().getFontNames().iterator().hasNext(),
                "Page 2 should have contained a font");
            assertFalse(document.getPage(1).getResources().getXObjectNames().iterator().hasNext(),
                "Page 1 should not have contained an XObject");
            PDAnnotationText textAnnotation = new PDAnnotationText();
            textAnnotation.setName("text annotation");
            textAnnotation.setContents("text annotation");
            textAnnotation.setOpen(true);
            textAnnotation.setColor(new PDColor(new float[]{1, 0, 0}, PDDeviceRGB.INSTANCE));
            textAnnotation.setRectangle(new PDRectangle(4, 5, 10, 10));
            textAnnotation.constructAppearances(document);
            document.getPage(1).getAnnotations().add(textAnnotation);
            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Do nothing.
        try (
            ByteArrayOutputStream documentOutput = new ByteArrayOutputStream();
            PDDocument document = loadDocument(documentData)
        )
        {
            assertEquals(1, document.getPage(1).getAnnotations().size(), "Page 2 should have contained an annotation.");

            document.saveIncremental(documentOutput);
            documentData = documentOutput.toByteArray();
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // Check the result.
        try (
            PDDocument document = loadDocument(documentData)
        )
        {
            assertEquals(2, document.getNumberOfPages(), "Document should have contained 2 pages.");
            assertNotNull(document.getPage(0).getResources(), "Page 1 should have contained resources");
            assertNotNull(document.getPage(1).getResources(), "Page 2 should have contained resources");
            assertTrue(document.getPage(0).hasContents(), "Page 1 should have had contents.");
            assertFalse(document.getPage(0).getResources().getFontNames().iterator().hasNext(),
                "Page 1 should not have contained a font");
            assertTrue(document.getPage(0).getResources().getXObjectNames().iterator().hasNext(),
                "Page 1 should have contained an XObject");
            assertTrue(document.getPage(1).hasContents(),
                "Page 2 should have had contents.");
            assertEquals(1, document.getPage(1).getAnnotations().size(),
                "Page 2 should have contained an annotation.");
            assertTrue(document.getPage(1).getResources().getFontNames().iterator().hasNext(),
                "Page 2 should have contained a font");
        }
        catch (IOException e)
        {
            fail("Closing streams failed.");
        }

        // TODO: remove the following - Convenience code - this creates the output file at some path,
        // to see and touch it.
        /*File outFile = new File("Some/path", "out.pdf");
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            outputStream.write(documentData);
        } catch (IOException ex) {
            fail("Writing 'out.pdf' failed.");
        }*/
    }

    /**
     * PDFBOX-5263: There was a ConcurrentModificationException with
     * YTW2VWJQTDAE67PGJT6GS7QSKW3GNUQR.pdf - test that this issues has been resolved.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testConcurrentModification() throws IOException, URISyntaxException
    {
        URL pdfLocation = 
            new URI("https://issues.apache.org/jira/secure/attachment/12891316/YTW2VWJQTDAE67PGJT6GS7QSKW3GNUQR.pdf").toURL();
        
        try (PDDocument document = Loader
                .loadPDF(RandomAccessReadBuffer.createBufferFromStream(pdfLocation.openStream())))
        {
            document.setAllSecurityToBeRemoved(true);
            try
            {
                document.save(new ByteArrayOutputStream());
            }
            catch (ConcurrentModificationException e)
            {
                fail("There shouldn't be a ConcurrentModificationException", e.getCause());
            }
        }
    }

    private PDDocument loadDocument(byte[] documentData)
    {
        return assertDoesNotThrow(() -> Loader.loadPDF(documentData), "Loading the document failed.");
    }

    /**
     * Check that subsetting takes place in incremental saving.
     */
    @Test
    void testSubsetting() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            document.save(baos);
        }

        try (PDDocument document = Loader.loadPDF(baos.toByteArray()))
        {
            PDPage page = document.getPage(0);

            PDFont font = PDType0Font.load(document, TestCOSIncrement.class.getResourceAsStream(
                    "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
            {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(75, 750);
                contentStream.showText("Apache PDFBox");
                contentStream.endText();
            }

            COSDictionary catalog = document.getDocumentCatalog().getCOSObject();
            catalog.setNeedToBeUpdated(true);
            COSDictionary pages = catalog.getCOSDictionary(COSName.PAGES);
            pages.setNeedToBeUpdated(true);
            page.getCOSObject().setNeedToBeUpdated(true);

            document.saveIncremental(new FileOutputStream("target/test-output/PDFBOX-5627.pdf"));
        }

        try (PDDocument document = Loader.loadPDF(new File("target/test-output/PDFBOX-5627.pdf")))
        {
            PDPage page = document.getPage(0);
            COSName fontName = page.getResources().getFontNames().iterator().next();
            PDFont font = page.getResources().getFont(fontName);
            assertTrue(font.isEmbedded());
        }
    }
}
