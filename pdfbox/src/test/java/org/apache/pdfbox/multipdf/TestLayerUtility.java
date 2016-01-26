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
package org.apache.pdfbox.multipdf;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.util.Matrix;

/**
 * Tests the {@link org.apache.pdfbox.multipdf.LayerUtility} class.
 *
 */
public class TestLayerUtility extends TestCase
{

    private final File testResultsDir = new File("target/test-output");

    /** {@inheritDoc} */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Tests layer import.
     * @throws Exception if an error occurs
     */
    public void testLayerImport() throws Exception
    {
        File mainPDF = createMainPDF();
        File overlay1 = createOverlay1();
        File targetFile = new File(testResultsDir, "text-with-form-overlay.pdf");

        PDDocument targetDoc = PDDocument.load(mainPDF);
        PDDocument overlay1Doc = PDDocument.load(overlay1);
        try
        {
            LayerUtility layerUtil = new LayerUtility(targetDoc);
            PDFormXObject form = layerUtil.importPageAsForm(overlay1Doc, 0);
            PDPage targetPage = targetDoc.getPage(0);
            layerUtil.wrapInSaveRestore(targetPage);
            AffineTransform at = new AffineTransform();
            layerUtil.appendFormAsLayer(targetPage, form, at, "overlay");

            targetDoc.save(targetFile.getAbsolutePath());
        }
        finally
        {
            targetDoc.close();
            overlay1Doc.close();
        }

        PDDocument doc = PDDocument.load(targetFile);
        try
        {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            //OCGs require PDF 1.5 or later
            assertEquals(1.5f, doc.getVersion());

            PDPage page = doc.getPage(0);
            PDOptionalContentGroup ocg = (PDOptionalContentGroup)page.getResources()
                    .getProperties(COSName.getPDFName("oc1"));
            assertNotNull(ocg);
            assertEquals("overlay", ocg.getName());

            PDOptionalContentProperties ocgs = catalog.getOCProperties();
            PDOptionalContentGroup overlay = ocgs.getGroup("overlay");
            assertEquals(ocg.getName(), overlay.getName());
        }
        finally
        {
            doc.close();
        }
    }

    private File createMainPDF() throws IOException
    {
        File targetFile = new File(testResultsDir, "text-doc.pdf");
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

            final String[] text = new String[] {
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer fermentum lacus in eros",
                    "condimentum eget tristique risus viverra. Sed ac sem et lectus ultrices placerat. Nam",
                    "fringilla tincidunt nulla id euismod. Vivamus eget mauris dui. Mauris luctus ullamcorper",
                    "leo, et laoreet diam suscipit et. Nulla viverra commodo sagittis. Integer vitae rhoncus velit.",
                    "Mauris porttitor ipsum in est sagittis non luctus purus molestie. Sed placerat aliquet",
                    "vulputate."
            };

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 720);
            contentStream.setFont(font, 14);
            contentStream.showText("Simple test document with text.");
            contentStream.endText();
            font = PDType1Font.HELVETICA;
            contentStream.beginText();
            int fontSize = 12;
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(50, 700);
            for (String line : text)
            {
                contentStream.newLineAtOffset(0, -fontSize * 1.2f);
                contentStream.showText(line);
            }
            contentStream.endText();
            contentStream.close();

            doc.save(targetFile.getAbsolutePath());
        }
        finally
        {
            doc.close();
        }
        return targetFile;
    }

    private File createOverlay1() throws IOException
    {
        File targetFile = new File(testResultsDir, "overlay1.pdf");
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

            //Setup page content stream and paint background/title
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
            contentStream.beginText();
            float fontSize = 96;
            contentStream.setFont(font, fontSize);
            String text = "OVERLAY";
            //float sw = font.getStringWidth(text);
            //Too bad, base 14 fonts don't return character metrics.
            PDRectangle crop = page.getCropBox();
            float cx = crop.getWidth() / 2f;
            float cy = crop.getHeight() / 2f;
            Matrix transform = new Matrix();
            transform.translate(cx, cy);
            transform.rotate(Math.toRadians(45));
            transform.translate(-190 /* sw/2 */, 0);
            contentStream.setTextMatrix(transform);
            contentStream.showText(text);
            contentStream.endText();
            contentStream.close();

            doc.save(targetFile.getAbsolutePath());
        }
        finally
        {
            doc.close();
        }
        return targetFile;
    }

}
