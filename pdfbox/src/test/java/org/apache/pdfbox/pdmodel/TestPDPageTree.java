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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import org.junit.After;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class TestPDPageTree
{
    private PDDocument doc;

    @After
    public void tearDown() throws IOException
    {
        if (doc != null)
        {
            doc.close();
        }
    }

    @Test
    public void indexOfPageFromOutlineDestination() throws IOException
    {
        doc = PDDocument.load(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
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
    public void positiveSingleLevel() throws IOException
    {
        doc = PDDocument.load(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
        for (int i = 0; i < doc.getNumberOfPages(); i++)
        {
            assertEquals(i, doc.getPages().indexOf(doc.getPage(i)));
        }
    }

    @Test
    public void positiveMultipleLevel() throws IOException
    {
        doc = PDDocument.load(TestPDPageTree.class.getResourceAsStream("page_tree_multiple_levels.pdf"));
        for (int i = 0; i < doc.getNumberOfPages(); i++)
        {
            assertEquals(i, doc.getPages().indexOf(doc.getPage(i)));
        }
    }

    @Test
    public void negative() throws IOException
    {
        doc = PDDocument.load(TestPDPageTree.class.getResourceAsStream("with_outline.pdf"));
        assertEquals(-1, doc.getPages().indexOf(new PDPage()));
    }

    /**
     * PDFBOX-6040: avoid loop in getInheritableAttribute().
     *
     * @throws IOException 
     */
    @Test
    public void testNodeLoop() throws IOException
    {
        doc = PDDocument.load(TestPDPageTree.class.getResourceAsStream("PDFBOX-6040-nodeloop.pdf"));
        assertNull(doc.getPage(0).getResources());
    }

    /**
     * PDFBOX-6286
     *
     * @throws IOException 
     */
    @Test
    public void getValidPageAfterOutOfBoundsLookup() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageTree pages = document.getPages();
        try
        {
            pages.get(1);
            fail("should throw IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException expected)
        {
        }
        // unexpectedly reported recursion before PDFBOX-6286
        assertSame(page.getCOSObject(), pages.get(0).getCOSObject());
        document.close();
    }

    /**
     * Test no argument constructor and an add(). By copilot.
     */
    @Test
    public void testNoArgConstructorInitializesEmptyPagesRoot()
    {
        PDPageTree pageTree = new PDPageTree();
        PDPage page = new PDPage();

        pageTree.add(page);

        COSDictionary root = pageTree.getCOSObject();
        COSArray kids = root.getCOSArray(COSName.KIDS);

        assertNotNull("Kids array should exist", kids);
        assertEquals("Kids should contain one page", 1, kids.size());
        assertEquals("Added page should be first kid", page.getCOSObject(), kids.getObject(0));
        assertEquals("Root count should be incremented to 1", 1, root.getInt(COSName.COUNT));
        assertEquals("Page tree count should be 1", 1, pageTree.getCount());
        assertSame("Page tree count should be 1", root, page.getCOSObject().getDictionaryObject(COSName.PARENT));
    }
}
