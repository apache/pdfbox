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
package org.apache.pdfbox.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

/**
 * Test suite for PDFCloneUtility, see PDFBOX-2052.
 *
 * @author <a href="mailto:c.hoeflake@gmail.com">Cornelis Hoeflake</a>
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
        PDDocument srcDoc = new PDDocument();
        PDDocument dstDoc = new PDDocument();
        PDPage pdPage = new PDPage();
        srcDoc.addPage(pdPage);
        new PDPageContentStream(srcDoc, pdPage, true, true).close();
        new PDPageContentStream(srcDoc, pdPage, true, true).close();
        new PDFCloneUtility(dstDoc).cloneForNewDocument(pdPage.getCOSObject());
        srcDoc.close();
        dstDoc.close();
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
        PDPageContentStream pdPageContentStream1 = new PDPageContentStream(srcDoc, pdPage, true, false);
        pdPageContentStream1.setNonStrokingColor(Color.black);
        pdPageContentStream1.fillRect(100, 600, 300, 100);
        pdPageContentStream1.close();
        PDPageContentStream pdPageContentStream2 = new PDPageContentStream(srcDoc, pdPage, true, false);
        pdPageContentStream2.setNonStrokingColor(Color.red);
        pdPageContentStream2.fillRect(100, 500, 300, 100);
        pdPageContentStream2.close();
        PDPageContentStream pdPageContentStream3 = new PDPageContentStream(srcDoc, pdPage, true, false);
        pdPageContentStream3.setNonStrokingColor(Color.yellow);
        pdPageContentStream3.fillRect(100, 400, 300, 100);
        pdPageContentStream3.close();

        srcDoc.save(TESTDIR + CLONESRC);
        PDFMergerUtility merger = new PDFMergerUtility();
        PDDocument dstDoc = new PDDocument();

        // this calls PDFCloneUtility.cloneForNewDocument(), 
        // which would fail before the fix in PDFBOX-2052
        merger.appendDocument(dstDoc, srcDoc);

        // save and reload PDF, so that one can see that the files are legit
        dstDoc.save(TESTDIR + CLONEDST);
        PDDocument.load(new File(TESTDIR + CLONESRC)).close();
        PDDocument.load(new File(TESTDIR + CLONESRC), null).close();
        PDDocument.load(new File(TESTDIR + CLONEDST)).close();
        PDDocument.load(new File(TESTDIR + CLONEDST), null).close();
    }
}
