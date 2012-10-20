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
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties.BaseState;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;

/**
 * Tests optional content group functionality (also called layers).
 *
 * @version $Revision$
 */
public class TestOptionalContentGroups extends TestCase
{

    private File testResultsDir = new File("target/test-output");

    /** {@inheritDoc} */
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
            //OCGs have been introduced with PDF 1.5
            doc.getDocument().setHeaderString("%PDF-1.5");
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            catalog.setVersion("1.5");

            //Create new page
            PDPage page = new PDPage();
            doc.addPage(page);
            PDResources resources = page.findResources();
            if( resources == null )
            {
                resources = new PDResources();
                page.setResources( resources );
            }

            //Prepare OCG functionality
            PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
            catalog.setOCProperties(ocprops);
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


            //Add mapping to page
            PDPropertyList props = new PDPropertyList();
            resources.setProperties(props);
            COSName mc0 = COSName.getPDFName("MC0");
            props.putMapping(mc0, background);
            COSName mc1 = COSName.getPDFName("MC1");
            props.putMapping(mc1, enabled);
            COSName mc2 = COSName.getPDFName("MC2");
            props.putMapping(mc2, disabled);

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginMarkedContentSequence(COSName.OC, mc0);
            contentStream.beginText();
            contentStream.setFont(font, 14);
            contentStream.moveTextPositionByAmount(80, 700);
            contentStream.drawString("PDF 1.5: Optional Content Groups");
            contentStream.endText();
            font = PDType1Font.HELVETICA;
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(80, 680);
            contentStream.drawString("You should see a green textline, but no red text line.");
            contentStream.endText();
            contentStream.endMarkedContentSequence();

            //Paint enabled layer
            contentStream.beginMarkedContentSequence(COSName.OC, mc1);
            contentStream.setNonStrokingColor(Color.GREEN);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(80, 600);
            contentStream.drawString(
                    "This is from an enabled layer. If you see this, that's good.");
            contentStream.endText();
            contentStream.endMarkedContentSequence();

            //Paint disabled layer
            contentStream.beginMarkedContentSequence(COSName.OC, mc2);
            contentStream.setNonStrokingColor(Color.RED);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(80, 500);
            contentStream.drawString(
                    "This is from a disabled layer. If you see this, that's NOT good!");
            contentStream.endText();
            contentStream.endMarkedContentSequence();

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
            assertEquals("%PDF-1.5", doc.getDocument().getHeaderString());
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            assertEquals("1.5", catalog.getVersion());

            PDPage page = (PDPage)catalog.getAllPages().get(0);
            PDPropertyList props = page.findResources().getProperties();
            assertNotNull(props);
            PDOptionalContentGroup ocg = props.getOptionalContentGroup(COSName.getPDFName("MC0"));
            assertNotNull(ocg);
            assertEquals("background", ocg.getName());

            assertNull(props.getOptionalContentGroup(COSName.getPDFName("inexistent")));

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
            coll.contains(background);

        }
        finally
        {
            doc.close();
        }
    }

}
