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
package org.apache.pdfbox.pdmodel.graphics.optionalcontent;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties.BaseState;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;

/**
 * Tests optional content group functionality (also called layers).
 */
public class TestOptionalContentGroups extends TestCase
{
    private final File testResultsDir = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests OCG generation.
     * @throws Exception if an error occurs
     */
    public void testOCGGeneration() throws Exception
    {
        PDDocument doc = new PDDocument();
        try
        {
            //Create new page
            PDPage page = new PDPage();
            doc.addPage(page);
            PDResources resources = page.getResources();
            if( resources == null )
            {
                resources = new PDResources();
                page.setResources( resources );
            }

            //Prepare OCG functionality
            PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
            doc.getDocumentCatalog().setOCProperties(ocprops);
            //ocprops.setBaseState(BaseState.ON); //ON=default

            //Create OCG for background
            PDOptionalContentGroup background = new PDOptionalContentGroup("background");
            ocprops.addGroup(background);
            assertTrue(ocprops.isGroupEnabled("background"));

            //Create OCG for enabled
            PDOptionalContentGroup enabled = new PDOptionalContentGroup("enabled");
            ocprops.addGroup(enabled);
            assertFalse(ocprops.setGroupEnabled("enabled", true));
            assertTrue(ocprops.isGroupEnabled("enabled"));

            //Create OCG for disabled
            PDOptionalContentGroup disabled = new PDOptionalContentGroup("disabled");
            ocprops.addGroup(disabled);
            assertFalse(ocprops.setGroupEnabled("disabled", true));
            assertTrue(ocprops.isGroupEnabled("disabled"));
            assertTrue(ocprops.setGroupEnabled("disabled", false));
            assertFalse(ocprops.isGroupEnabled("disabled"));

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginMarkedContent(COSName.OC, background);
            contentStream.beginText();
            contentStream.setFont(font, 14);
            contentStream.newLineAtOffset(80, 700);
            contentStream.showText("PDF 1.5: Optional Content Groups");
            contentStream.endText();
            font = PDType1Font.HELVETICA;
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 680);
            contentStream.showText("You should see a green textline, but no red text line.");
            contentStream.endText();
            contentStream.endMarkedContent();

            //Paint enabled layer
            contentStream.beginMarkedContent(COSName.OC, enabled);
            contentStream.setNonStrokingColor(Color.GREEN);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 600);
            contentStream.showText(
                    "This is from an enabled layer. If you see this, that's good.");
            contentStream.endText();
            contentStream.endMarkedContent();

            //Paint disabled layer
            contentStream.beginMarkedContent(COSName.OC, disabled);
            contentStream.setNonStrokingColor(Color.RED);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 500);
            contentStream.showText(
                    "This is from a disabled layer. If you see this, that's NOT good!");
            contentStream.endText();
            contentStream.endMarkedContent();

            contentStream.close();

            File targetFile = new File(testResultsDir, "ocg-generation.pdf");
            doc.save(targetFile.getAbsolutePath());
        }
        finally
        {
            doc.close();
        }
    }

    /**
     * Tests OCG functions on a loaded PDF.
     * @throws Exception if an error occurs
     */
    public void testOCGConsumption() throws Exception
    {
        File pdfFile = new File(testResultsDir, "ocg-generation.pdf");
        if (!pdfFile.exists())
        {
            testOCGGeneration();
        }

        PDDocument doc = PDDocument.load(pdfFile);
        try
        {
            assertEquals(1.5f, doc.getVersion());
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            PDPage page = doc.getPage(0);
            PDResources resources = page.getResources();

            COSName mc0 = COSName.getPDFName("oc1");
            PDOptionalContentGroup ocg = (PDOptionalContentGroup)resources.getProperties(mc0);
            assertNotNull(ocg);
            assertEquals("background", ocg.getName());

            assertNull(resources.getProperties(COSName.getPDFName("inexistent")));

            PDOptionalContentProperties ocgs = catalog.getOCProperties();
            assertEquals(BaseState.ON, ocgs.getBaseState());
            Set<String> names = new java.util.HashSet<String>(Arrays.asList(ocgs.getGroupNames()));
            assertEquals(3, names.size());
            assertTrue(names.contains("background"));

            assertTrue(ocgs.isGroupEnabled("background"));
            assertTrue(ocgs.isGroupEnabled("enabled"));
            assertFalse(ocgs.isGroupEnabled("disabled"));

            ocgs.setGroupEnabled("background", false);
            assertFalse(ocgs.isGroupEnabled("background"));

            PDOptionalContentGroup background = ocgs.getGroup("background");
            assertEquals(ocg.getName(), background.getName());
            assertNull(ocgs.getGroup("inexistent"));

            Collection<PDOptionalContentGroup> coll = ocgs.getOptionalContentGroups();
            assertEquals(3, coll.size());
            Set<String> nameSet = new HashSet<String>();
            for (PDOptionalContentGroup ocg2 : coll)
            {
                nameSet.add(ocg2.getName());
            }
            assertTrue(nameSet.contains("background"));
            assertTrue(nameSet.contains("enabled"));
            assertTrue(nameSet.contains("disabled"));
        }
        finally
        {
            doc.close();
        }
    }

    public void testOCGsWithSameNameCanHaveDifferentVisibility() throws Exception
    {
        PDDocument doc = new PDDocument();
        try
        {
            //Create new page
            PDPage page = new PDPage();
            doc.addPage(page);
            PDResources resources = page.getResources();
            if( resources == null )
            {
                resources = new PDResources();
                page.setResources( resources );
            }

            //Prepare OCG functionality
            PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
            doc.getDocumentCatalog().setOCProperties(ocprops);
            //ocprops.setBaseState(BaseState.ON); //ON=default

            //Create visible OCG
            PDOptionalContentGroup visible = new PDOptionalContentGroup("layer");
            ocprops.addGroup(visible);
            assertTrue(ocprops.isGroupEnabled(visible));

            //Create invisible OCG
            PDOptionalContentGroup invisible = new PDOptionalContentGroup("layer");
            ocprops.addGroup(invisible);
            assertFalse(ocprops.setGroupEnabled(invisible, false));
            assertFalse(ocprops.isGroupEnabled(invisible));

            //Check that visible layer is still visible
            assertTrue(ocprops.isGroupEnabled(visible));

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginMarkedContent(COSName.OC, visible);
            contentStream.beginText();
            contentStream.setFont(font, 14);
            contentStream.newLineAtOffset(80, 700);
            contentStream.showText("PDF 1.5: Optional Content Groups");
            contentStream.endText();
            font = PDType1Font.HELVETICA;
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 680);
            contentStream.showText("You should see this text, but no red text line.");
            contentStream.endText();
            contentStream.endMarkedContent();

            //Paint disabled layer
            contentStream.beginMarkedContent(COSName.OC, invisible);
            contentStream.setNonStrokingColor(Color.RED);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 500);
            contentStream.showText(
                    "This is from a disabled layer. If you see this, that's NOT good!");
            contentStream.endText();
            contentStream.endMarkedContent();

            contentStream.close();

            File targetFile = new File(testResultsDir, "ocg-generation-same-name.pdf");
            doc.save(targetFile.getAbsolutePath());
        }
        finally
        {
            doc.close();
        }
    }

    /**
     * PDFBOX-4496: setGroupEnabled(String, boolean) must catch all OCGs of a name even when several
     * names are identical.
     *
     * @throws IOException
     */
    public void testOCGGenerationSameNameCanHaveSameVisibilityOff() throws IOException
    {
        BufferedImage expectedImage;
        BufferedImage actualImage;

        PDDocument doc = new PDDocument();
        try
        {
            //Create new page
            PDPage page = new PDPage();
            doc.addPage(page);
            PDResources resources = page.getResources();
            if (resources == null)
            {
                resources = new PDResources();
                page.setResources(resources);
            }

            //Prepare OCG functionality
            PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
            doc.getDocumentCatalog().setOCProperties(ocprops);
            //ocprops.setBaseState(BaseState.ON); //ON=default

            //Create OCG for background
            PDOptionalContentGroup background = new PDOptionalContentGroup("background");
            ocprops.addGroup(background);
            assertTrue(ocprops.isGroupEnabled("background"));

            //Create OCG for enabled
            PDOptionalContentGroup enabled = new PDOptionalContentGroup("science");
            ocprops.addGroup(enabled);
            assertFalse(ocprops.setGroupEnabled("science", true));
            assertTrue(ocprops.isGroupEnabled("science"));

            //Create OCG for disabled1
            PDOptionalContentGroup disabled1 = new PDOptionalContentGroup("alternative");
            ocprops.addGroup(disabled1);

            //Create OCG for disabled2 with same name as disabled1
            PDOptionalContentGroup disabled2 = new PDOptionalContentGroup("alternative");
            ocprops.addGroup(disabled2);

            assertFalse(ocprops.setGroupEnabled("alternative", false));
            assertFalse(ocprops.isGroupEnabled("alternative"));

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginMarkedContent(COSName.OC, background);
            contentStream.beginText();
            contentStream.setFont(font, 14);
            contentStream.newLineAtOffset(80, 700);
            contentStream.showText("PDF 1.5: Optional Content Groups");
            contentStream.endText();
            contentStream.endMarkedContent();

            font = PDType1Font.HELVETICA;

            //Paint enabled layer
            contentStream.beginMarkedContent(COSName.OC, enabled);
            contentStream.setNonStrokingColor(Color.GREEN);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 600);
            contentStream.showText("The earth is a sphere");
            contentStream.endText();
            contentStream.endMarkedContent();

            //Paint disabled layer1
            contentStream.beginMarkedContent(COSName.OC, disabled1);
            contentStream.setNonStrokingColor(Color.RED);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 500);
            contentStream.showText("Alternative 1: The earth is a flat circle");
            contentStream.endText();
            contentStream.endMarkedContent();

            //Paint disabled layer2
            contentStream.beginMarkedContent(COSName.OC, disabled2);
            contentStream.setNonStrokingColor(Color.BLUE);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 450);
            contentStream.showText("Alternative 2: The earth is a flat parallelogram");
            contentStream.endText();
            contentStream.endMarkedContent();

            contentStream.close();

            doc.getDocumentCatalog().setPageMode(PageMode.USE_OPTIONAL_CONTENT);

            File targetFile = new File(testResultsDir, "ocg-generation-same-name-off.pdf");
            doc.save(targetFile.getAbsolutePath());
            doc.close();

            // render PDF with science disabled and alternatives with same name enabled
            doc = PDDocument.load(new File(testResultsDir, "ocg-generation-same-name-off.pdf"));
            doc.getDocumentCatalog().getOCProperties().setGroupEnabled("background", false);
            doc.getDocumentCatalog().getOCProperties().setGroupEnabled("science", false);
            doc.getDocumentCatalog().getOCProperties().setGroupEnabled("alternative", true);
            actualImage = new PDFRenderer(doc).renderImage(0, 2);
            ImageIO.write(actualImage, "png", new File(testResultsDir, "ocg-generation-same-name-off-actual.png"));
        }
        finally
        {
            doc.close();
        }

        // create PDF without OCGs to created expected rendering
        PDDocument doc2 = new PDDocument();
        try
        {
            //Create new page
            PDPage page = new PDPage();
            doc2.addPage(page);
            PDResources resources = page.getResources();
            if (resources == null)
            {
                resources = new PDResources();
                page.setResources(resources);
            }

            PDPageContentStream contentStream = new PDPageContentStream(doc2, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA;

            contentStream.setNonStrokingColor(Color.RED);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 500);
            contentStream.showText("Alternative 1: The earth is a flat circle");
            contentStream.endText();

            contentStream.setNonStrokingColor(Color.BLUE);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(80, 450);
            contentStream.showText("Alternative 2: The earth is a flat parallelogram");
            contentStream.endText();

            contentStream.close();

            expectedImage = new PDFRenderer(doc2).renderImage(0, 2);
            ImageIO.write(expectedImage, "png", new File(testResultsDir, "ocg-generation-same-name-off-expected.png"));
        }
        finally
        {
            doc2.close();
        }

        // compare images
        DataBufferInt expectedData = (DataBufferInt) expectedImage.getRaster().getDataBuffer();
        DataBufferInt actualData = (DataBufferInt) actualImage.getRaster().getDataBuffer();
        Assert.assertArrayEquals(expectedData.getData(), actualData.getData());
    }
}
