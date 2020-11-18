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
package org.apache.pdfbox.pdmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * @author Andrea Vacondio
 *
 */
class TestPDPageTree
{
    private PDDocument doc;

    @AfterEach
    public void tearDown() throws IOException
    {
        if (doc != null)
        {
            doc.close();
        }
    }

    @Test
    void indexOfPageFromOutlineDestination() throws IOException
    {
        doc = Loader.loadPDF(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
        PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
        for (PDOutlineItem current : outline.children())
        {
            if (current.getTitle().contains("Second"))
            {
                assertEquals(2, doc.getPages().indexOf(current.findDestinationPage(doc)));
            }
        }
    }

    @Test
    void positiveSingleLevel() throws IOException
    {
        doc = Loader.loadPDF(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
        for (int i = 0; i < doc.getNumberOfPages(); i++)
        {
            assertEquals(i, doc.getPages().indexOf(doc.getPage(i)));
        }
    }

    @Test
    void positiveMultipleLevel() throws IOException
    {
        doc = Loader
                .loadPDF(TestPDPageTree.class.getResourceAsStream("page_tree_multiple_levels.pdf"));
        for (int i = 0; i < doc.getNumberOfPages(); i++)
        {
            assertEquals(i, doc.getPages().indexOf(doc.getPage(i)));
        }
    }

    @Test
    void negative() throws IOException
    {
        doc = Loader.loadPDF(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
        assertEquals(-1, doc.getPages().indexOf(new PDPage()));
    }

    @Test
    void testInsertBeforeBlankPage() throws Exception
    {
        try (PDDocument document = new PDDocument()) {
            PDPage pageOne = new PDPage();
            PDPage pageTwo = new PDPage();
            PDPage pageThree = new PDPage();

            document.addPage(pageOne);
            document.addPage(pageTwo);
            document.getPages().insertBefore(pageThree, pageTwo);

            assertEquals(0, (document.getPages().indexOf(pageOne)),
                    "Page one should be placed at index 0.");
            assertEquals(2,(document.getPages().indexOf(pageTwo)),"Page two should be placed at index 2.");
            assertEquals(1,(document.getPages().indexOf(pageThree)),"Page three should be placed at index 1.");
        }
    }

    @Test
    void testInsertAfterBlankPage() throws Exception
    {
        try (PDDocument document = new PDDocument()) {
            PDPage pageOne = new PDPage();
            PDPage pageTwo = new PDPage();
            PDPage pageThree = new PDPage();

            document.addPage(pageOne);
            document.addPage(pageTwo);
            document.getPages().insertAfter(pageThree, pageTwo);

            assertEquals(0, (document.getPages().indexOf(pageOne)),
                    "Page one should be placed at index 0.");
            assertEquals(1, (document.getPages().indexOf(pageTwo)),
                    "Page two should be placed at index 1.");
            assertEquals(2, (document.getPages().indexOf(pageThree)),
                    "Page three should be placed at index 2.");
        }
    }
}
