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
package org.apache.pdfbox.multipdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;

/**
 * Test suite for PDFCloneUtility, see PDFBOX-2052.
 *
 * @author Cornelis Hoeflake
 * @author Tilman Hausherr
 */
public class PDFCloneUtilityTest extends TestCase
{
    /**
     * original (minimal) test from PDFBOX-2052.
     * 
     * @throws IOException 
     */
    public void testClonePDFWithCosArrayStream() throws IOException
    {
        try (PDDocument srcDoc = new PDDocument();
             PDDocument dstDoc = new PDDocument())
        {

            PDPage pdPage = new PDPage();
            srcDoc.addPage(pdPage);
            new PDPageContentStream(srcDoc, pdPage, AppendMode.APPEND, true).close();
            new PDPageContentStream(srcDoc, pdPage, AppendMode.APPEND, true).close();
            new PDFCloneUtility(dstDoc).cloneForNewDocument(pdPage.getCOSObject());
        }
    }

    /**
     * broader test that saves to a real PDF document.
     * 
     * @throws IOException 
     */
    public void testClonePDFWithCosArrayStream2() throws IOException
    {
        final String TESTDIR = "target/test-output/clone/";
        final String CLONESRC = "clone-src.pdf";
        final String CLONEDST = "clone-dst.pdf";

        new File(TESTDIR).mkdirs();

        PDDocument srcDoc = new PDDocument();
        PDPage pdPage = new PDPage();
        srcDoc.addPage(pdPage);
        try (PDPageContentStream pdPageContentStream1 = new PDPageContentStream(srcDoc, pdPage, AppendMode.APPEND, false))
        {
            pdPageContentStream1.setNonStrokingColor(Color.black);
            pdPageContentStream1.addRect(100, 600, 300, 100);
            pdPageContentStream1.fill();
        }
        try (PDPageContentStream pdPageContentStream2 = new PDPageContentStream(srcDoc, pdPage, AppendMode.APPEND, false))
        {
            pdPageContentStream2.setNonStrokingColor(Color.red);
            pdPageContentStream2.addRect(100, 500, 300, 100);
            pdPageContentStream2.fill();
        }
        try (PDPageContentStream pdPageContentStream3 = new PDPageContentStream(srcDoc, pdPage, AppendMode.APPEND, false))
        {
            pdPageContentStream3.setNonStrokingColor(Color.yellow);
            pdPageContentStream3.addRect(100, 400, 300, 100);
            pdPageContentStream3.fill();
        }

        srcDoc.save(TESTDIR + CLONESRC);
        PDFMergerUtility merger = new PDFMergerUtility();
        PDDocument dstDoc = new PDDocument();

        // this calls PDFCloneUtility.cloneForNewDocument(), 
        // which would fail before the fix in PDFBOX-2052
        merger.appendDocument(dstDoc, srcDoc);

        // save and reload PDF, so that one can see that the files are legit
        dstDoc.save(TESTDIR + CLONEDST);
        PDDocument.load(new File(TESTDIR + CLONESRC)).close();
        PDDocument.load(new File(TESTDIR + CLONESRC), (String)null).close();
        PDDocument.load(new File(TESTDIR + CLONEDST)).close();
        PDDocument.load(new File(TESTDIR + CLONEDST), (String)null).close();
    }
}
