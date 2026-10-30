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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.PDDestinationNameTreeNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDestinationDictionary;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import static org.junit.Assert.assertThrows;
import org.junit.function.ThrowingRunnable;

/**
 * Test suite for Splitter.
 *
 * @author Tilman Hausherr
 */
public class SplitterTest extends TestCase
{
    final String SRCDIR = "src/test/resources/input/merge/";
    final String TARGETTESTDIR = "target/test-output/merge/";
    private static final File TARGETPDFDIR = new File("target/pdfs");
    final int DPI = 96;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        new File(TARGETTESTDIR).mkdirs();
        if (!new File(TARGETTESTDIR).exists())
        {
            throw new IOException("could not create output directory");
        }
    }

    public void testIllegalSplitting() throws IOException
    {
        Splitter splitter = new Splitter();

        try
        {
            splitter.setSplitAtPage(0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Number of pages is smaller than one", ex.getMessage());
        }

        try
        {
            splitter.setStartPage(0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Start page is smaller than one", ex.getMessage());
        }

        try
        {
            splitter.setEndPage(0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("End page is smaller than one", ex.getMessage());
        }

        splitter.setStartPage(2);
        splitter.setEndPage(1);
        try
        {
            splitter.split(new PDDocument());
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("End page is smaller than startPage", ex.getMessage());
        }
    }

    public void testSplitWithStructureTree() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
        Splitter splitter = new Splitter();
        splitter.setStartPage(1);
        splitter.setEndPage(2);
        splitter.setSplitAtPage(2);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        PDDocument dstDoc = splitResult.get(0);
        assertEquals(2, dstDoc.getNumberOfPages());
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        // these tests just verify the status quo. Changes should be checked visually with
        // a PDF viewer that can display structural information.
        PDStructureTreeRoot structureTreeRoot = dstDoc.getDocumentCatalog().getStructureTreeRoot();
        assertEquals(126, structureTreeRoot.getIDTree().getTreeAsMap().size());
        assertEquals(2, structureTreeRoot.getParentTree().getTreeAsMap().size());
        assertEquals(6, structureTreeRoot.getRoleMap().size());
        dstDoc.close();
        doc.close();
    }

    public void testSplitWithStructureTreeAndDestinations() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR,"PDFBOX-5762-722238.pdf"));
        Splitter splitter = new Splitter();
        splitter.setStartPage(1);
        splitter.setEndPage(2);
        splitter.setSplitAtPage(2);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        PDDocument dstDoc = splitResult.get(0);
        assertEquals(2, dstDoc.getNumberOfPages());
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        // these tests just verify the status quo. Changes should be checked visually with
        // a PDF viewer that can display structural information.
        PDStructureTreeRoot structureTreeRoot = dstDoc.getDocumentCatalog().getStructureTreeRoot();
        assertEquals(7, structureTreeRoot.getParentTree().getTreeAsMap().size());
        assertEquals(4, structureTreeRoot.getRoleMap().size());

        // check that destinations are fixed (only the two first point to the split doc)
        List<PDAnnotation> annotations = dstDoc.getPage(0).getAnnotations();
        assertEquals(5, annotations.size());
        PDAnnotationLink link1 = (PDAnnotationLink) annotations.get(0);
        PDAnnotationLink link2 = (PDAnnotationLink) annotations.get(1);
        PDAnnotationLink link3 = (PDAnnotationLink) annotations.get(2);
        PDAnnotationLink link4 = (PDAnnotationLink) annotations.get(3);
        PDAnnotationLink link5 = (PDAnnotationLink) annotations.get(4);
        PDPageDestination pd1 = 
                (PDPageDestination) ((PDActionGoTo) link1.getAction()).getDestination();
        PDPageDestination pd2 = 
                (PDPageDestination) ((PDActionGoTo) link2.getAction()).getDestination();
        PDPageDestination pd3 = 
                (PDPageDestination) ((PDActionGoTo) link3.getAction()).getDestination();
        PDPageDestination pd4 = 
                (PDPageDestination) ((PDActionGoTo) link4.getAction()).getDestination();
        PDPageDestination pd5 = 
                (PDPageDestination) ((PDActionGoTo) link5.getAction()).getDestination();
        PDPageTree pageTree = dstDoc.getPages();
        assertEquals(0, pageTree.indexOf(pd1.getPage()));
        assertEquals(1, pageTree.indexOf(pd2.getPage()));
        assertNull(pd3.getPage());
        assertNull(pd4.getPage());
        assertNull(pd5.getPage());
        dstDoc.close();
        doc.close();
    }

    /**
     * PDFBOX-5929: Check that orphan annotations are removed from the structure tree if annotations
     * were removed from the pages (don't do that!).
     *
     * @throws IOException
     */
    public void testSplitWithStructureTreeAndDestinationsAndRemovedAnnotations() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-5762-722238.pdf"));
        Splitter splitter = new Splitter();
        for (PDPage page : doc.getPages())
        {
            page.setAnnotations(Collections.EMPTY_LIST);
        }
        splitter.setStartPage(1);
        splitter.setEndPage(2);
        splitter.setSplitAtPage(2);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        PDDocument dstDoc = splitResult.get(0);

        assertEquals(2, dstDoc.getNumberOfPages());
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        dstDoc.close();
        doc.close();
    }

    /**
     * Check for the bug that happened in PDFBOX-5792, where a destination was outside a target
     * document and hit an NPE in the next call of Splitter.fixDestinations().
     *
     * @throws IOException
     */
    public void testSinglePageSplit() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-5792-240045.pdf"));
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(1);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(6, splitResult.size());
        for (PDDocument dstDoc : splitResult)
        {
            assertEquals(1, dstDoc.getNumberOfPages());
            PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
            for (PDAnnotation ann : dstDoc.getPage(0).getAnnotations())
            {
                PDAnnotationLink link = (PDAnnotationLink) ann;
                PDActionGoTo action = (PDActionGoTo) link.getAction();
                PDPageDestination destination = (PDPageDestination) action.getDestination();
                assertNull(destination.getPage());
            }
        }
        PDStructureTreeRoot structureTreeRoot1 = splitResult.get(0).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(6, structureTreeRoot1.getParentTree().getTreeAsMap().size());
        assertEquals(3, structureTreeRoot1.getRoleMap().size());
        PDStructureTreeRoot structureTreeRoot2 = splitResult.get(1).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(6, structureTreeRoot2.getParentTree().getTreeAsMap().size());
        assertEquals(3, structureTreeRoot2.getRoleMap().size());
        PDStructureTreeRoot structureTreeRoot3 = splitResult.get(2).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(6, structureTreeRoot3.getParentTree().getTreeAsMap().size());
        assertEquals(4, structureTreeRoot3.getRoleMap().size());
        PDStructureTreeRoot structureTreeRoot4 = splitResult.get(3).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(5, structureTreeRoot4.getParentTree().getTreeAsMap().size());
        assertEquals(3, structureTreeRoot4.getRoleMap().size());
        PDStructureTreeRoot structureTreeRoot5 = splitResult.get(4).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(1, structureTreeRoot5.getParentTree().getTreeAsMap().size());
        assertEquals(5, structureTreeRoot5.getRoleMap().size());
        PDStructureTreeRoot structureTreeRoot6 = splitResult.get(5).getDocumentCatalog().getStructureTreeRoot();
        assertEquals(1, structureTreeRoot6.getParentTree().getTreeAsMap().size());
        assertEquals(6, structureTreeRoot6.getRoleMap().size());
        for (PDDocument dstDoc : splitResult)
        {
            dstDoc.close();
        }
        doc.close();
    }

    public void testSplitWithPopupAnnotations() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-5809-509329.pdf"));
        Splitter splitter = new Splitter();
        splitter.setStartPage(3);
        splitter.setEndPage(3);
        splitter.setSplitAtPage(1);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        PDDocument dstDoc = splitResult.get(0);
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        assertEquals(1, dstDoc.getNumberOfPages());
        List<PDAnnotation> annotations = dstDoc.getPage(0).getAnnotations();
        assertEquals(5, annotations.size());
        PDAnnotationText annotationText3 = (PDAnnotationText) annotations.get(3);
        PDAnnotationPopup annotationPopup4 = (PDAnnotationPopup) annotations.get(4);
        assertEquals(annotationText3.getPopup(), annotationPopup4);
        assertEquals(annotationPopup4.getParent(), annotationText3);
        assertEquals(annotationText3.getPage(), dstDoc.getPage(0));
        dstDoc.close();

        // Check that source document is ok
        annotations = doc.getPage(2).getAnnotations();
        assertEquals(5, annotations.size());
        annotationText3 = (PDAnnotationText) annotations.get(3);
        annotationPopup4 = (PDAnnotationPopup) annotations.get(4);
        assertEquals(annotationText3.getPopup(), annotationPopup4);
        assertEquals(annotationPopup4.getParent(), annotationText3);
        assertEquals(annotationText3.getPage(), doc.getPage(2));

        doc.close();
    }

    public void testSplitWithBrokenDestination() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-5811-362972.pdf"));        
        Splitter splitter = new Splitter();
        splitter.setStartPage(2);
        splitter.setEndPage(2);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        List<PDAnnotation> annotations;
        PDDocument dstDoc = splitResult.get(0);
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        assertEquals(1, dstDoc.getNumberOfPages());
        annotations = dstDoc.getPage(0).getAnnotations();
        assertEquals(1, annotations.size());
        PDAnnotationLink link = (PDAnnotationLink) annotations.get(0);
        assertNull(link.getDestination());
        dstDoc.close();
        // Check source document
        annotations = doc.getPage(1).getAnnotations();
        assertEquals(1, annotations.size());
        final PDAnnotationLink link2 = (PDAnnotationLink) annotations.get(0);
        assertThrows(IOException.class, new ThrowingRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                link2.getDestination();
            }
        });
        doc.close();
    }

    public void testSplitWithNamedDestinations() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-5840-410609.pdf"));
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(6);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        List<PDAnnotation> annotations;
        PDDocument dstDoc = splitResult.get(0);
        PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        assertEquals(6, dstDoc.getNumberOfPages());
        annotations = dstDoc.getPage(0).getAnnotations();
        assertEquals(5, annotations.size());
        PDAnnotationLink link1 = (PDAnnotationLink) annotations.get(0);
        PDAnnotationLink link2 = (PDAnnotationLink) annotations.get(1);
        PDAnnotationLink link3 = (PDAnnotationLink) annotations.get(2);
        PDAnnotationLink link4 = (PDAnnotationLink) annotations.get(3);
        PDAnnotationLink link5 = (PDAnnotationLink) annotations.get(4);
        PDPageDestination pd1 = 
                (PDPageDestination) ((PDActionGoTo) link1.getAction()).getDestination();
        PDPageDestination pd2 = 
                (PDPageDestination) ((PDActionGoTo) link2.getAction()).getDestination();
        PDPageDestination pd3 = 
                (PDPageDestination) ((PDActionGoTo) link3.getAction()).getDestination();
        PDPageDestination pd4 = 
                (PDPageDestination) ((PDActionGoTo) link4.getAction()).getDestination();
        PDPageDestination pd5 = 
                (PDPageDestination) ((PDActionGoTo) link5.getAction()).getDestination();
        PDPageTree pageTree = dstDoc.getPages();
        assertEquals(0, pageTree.indexOf(pd1.getPage()));
        assertEquals(1, pageTree.indexOf(pd2.getPage()));
        assertEquals(3, pageTree.indexOf(pd3.getPage()));
        assertEquals(3, pageTree.indexOf(pd4.getPage()));
        assertEquals(5, pageTree.indexOf(pd5.getPage()));
        dstDoc.close();
        // Check that source document is unchanged
        annotations = doc.getPage(0).getAnnotations();
        assertEquals(5, annotations.size());
        PDAnnotationLink link = (PDAnnotationLink) annotations.get(0);
        assertTrue(((PDActionGoTo) link.getAction()).getDestination() instanceof PDNamedDestination);
        doc.close();
    }

    /**
     * PDFBOX-6009: This test verifies that the destination PDF has a /K tree. Before the change,
     * nodes with the "wrong" /Pg entries were deleted entirely and because this file has a /Pg
     * entry with page 1 at the top, the entire /K tree would be missing.
     *
     * @throws IOException 
     */
    public void testSplitWithPgEntryAtTheTop() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-6009.pdf"));
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(1);
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(3, splitResult.size());
        for (PDDocument dstDoc : splitResult)
        {
            assertEquals(1, dstDoc.getNumberOfPages());
            PDFMergerUtilityTest.checkWithNumberTree(dstDoc);
            PDFMergerUtilityTest.checkForPageOrphans(dstDoc);
        }
        for (PDDocument dstDoc : splitResult)
        {
            dstDoc.close();
        }
        doc.close();
    }

    /**
     * PDFBOX-6018: Test split a PDF with popup annotations that are not in the annotations list.
     * Verify that after splitting, they still link back to their markup annotation and these to the
     * page.
     *
     * @throws IOException
     */
    public void testSplitWithOrphanPopupAnnotation() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(SRCDIR, "PDFBOX-6018-099267-p9-OrphanPopups.pdf"));
        Splitter splitter = new Splitter();
        List<PDDocument> splitResult = splitter.split(doc);
        assertEquals(1, splitResult.size());
        PDDocument dstDoc = splitResult.get(0);
        assertEquals(1, dstDoc.getNumberOfPages());
        PDPage page = dstDoc.getPage(0);
        List<PDAnnotation> annotations = page.getAnnotations();
        assertEquals(2, annotations.size());
        PDAnnotationText ann0 = (PDAnnotationText) annotations.get(0);
        PDAnnotationText ann1 = (PDAnnotationText) annotations.get(1);
        assertEquals(page, ann0.getPage());
        assertEquals(page, ann1.getPage());
        assertEquals(ann0, ann0.getPopup().getParent());
        assertEquals(ann1, ann1.getPopup().getParent());
        dstDoc.close();
        doc.close();
    }

    /**
     * PDFBOX-6283: Verifies that named destinations (/Names /Dests) pointing to a page are copied
     * to the split document that contains that page, remapped to the imported page, and that
     * destinations pointing to other pages are not carried over.
     */
    public void testSplitCopiesNamedDestinations() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page0 = new PDPage();
        PDPage page1 = new PDPage();
        PDPage page2 = new PDPage();
        document.addPage(page0);
        document.addPage(page1);
        document.addPage(page2);

        List<PDAnnotation> ann0 = new ArrayList<PDAnnotation>();
        PDAnnotationLink annLink0 = new PDAnnotationLink();
        annLink0.setDestination(new PDNamedDestination("dest0"));
        annLink0.setRectangle(new PDRectangle(10, 600, 100, 100));
        ann0.add(annLink0);
        page0.setAnnotations(ann0);

        List<PDAnnotation> ann1 = new ArrayList<PDAnnotation>();
        PDAnnotationLink annLink1 = new PDAnnotationLink();
        annLink1.setDestination(new PDNamedDestination("dest1"));
        annLink1.setRectangle(new PDRectangle(10, 600, 100, 100));
        ann1.add(annLink1);
        page1.setAnnotations(ann1);

        List<PDAnnotation> ann2 = new ArrayList<PDAnnotation>();
        PDAnnotationLink annLink2 = new PDAnnotationLink();
        annLink2.setDestination(new PDNamedDestination("dest2"));
        annLink2.setRectangle(new PDRectangle(10, 600, 100, 100));
        ann2.add(annLink2);
        page2.setAnnotations(ann2);

        PDPageFitDestination dest0 = new PDPageFitDestination();
        dest0.setPage(page0);
        PDPageFitDestination dest1 = new PDPageFitDestination();
        dest1.setPage(page1);
        PDPageFitDestination dest2 = new PDPageFitDestination();
        dest2.setPage(page2);

        Map<String, PDPageDestination> destNames = new LinkedHashMap<String, PDPageDestination>();
        destNames.put("dest0", dest0);
        destNames.put("dest1", dest1);
        destNames.put("dest2", dest2);

        PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
        PDDestinationNameTreeNode dests = new PDDestinationNameTreeNode();
        dests.setNames(destNames);
        names.setDests(dests);
        document.getDocumentCatalog().setNames(names);

        List<PDDocument> split = new Splitter().split(document);
        assertEquals(3, split.size());

        for (int i = 0; i < split.size(); i++)
        {
            PDDocument splitDocument = split.get(i);
            PDDocumentNameDictionary splitNames = splitDocument.getDocumentCatalog().getNames();
            assertNotNull("split document " + i + " should have a name dictionary", splitNames);
            assertNotNull("split document " + i + " should have a dests node", splitNames.getDests());

            Map<String, PDPageDestination> copiedNames = splitNames.getDests().getNames();
            assertNotNull(copiedNames);
            // only the destination pointing to this page should be present
            assertEquals(1, copiedNames.size());

            String expectedName = "dest" + i;
            COSObjectable copied = copiedNames.get(expectedName);
            assertTrue(copied instanceof PDPageFitDestination);
            // the destination must point to the imported page of the split document
            PDPage importedPage = ((PDPageFitDestination) copied).getPage();
            assertSame(splitDocument.getPage(0).getCOSObject(), importedPage.getCOSObject());
            PDAnnotationLink annLink = (PDAnnotationLink) importedPage.getAnnotations().get(0);
            PDNamedDestination namedDestination = (PDNamedDestination) annLink.getDestination();
            assertEquals(expectedName, namedDestination.getNamedDestination());            
        }
    }

    /**
     * PDFBOX-6283: Verifies that named destinations in Root/Dests pointing to a page are copied
     * to the split document that contains that page, remapped to the imported page, and that
     * destinations pointing to other pages are not carried over.
     */
    public void testSplitCopiesNamedDestinations2() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page0 = new PDPage();
        PDPage page1 = new PDPage();
        PDPage page2 = new PDPage();
        document.addPage(page0);
        document.addPage(page1);
        document.addPage(page2);

        PDPageFitDestination dest0 = new PDPageFitDestination();
        dest0.setPage(page0);
        PDPageFitDestination dest1 = new PDPageFitDestination();
        dest1.setPage(page1);
        PDPageFitDestination dest2 = new PDPageFitDestination();
        dest2.setPage(page2);
        
        COSDictionary destDict = new COSDictionary();
        destDict.setItem("dest0", dest0);
        destDict.setItem("dest1", dest1);
        destDict.setItem("dest2", dest2);
        
        document.getDocumentCatalog().getCOSObject().setItem(COSName.DESTS, new PDDocumentNameDestinationDictionary(destDict));

        List<PDDocument> split = new Splitter().split(document);
        assertEquals(3, split.size());

        for (int i = 0; i < split.size(); i++)
        {
            PDDocument splitDocument = split.get(i);
            PDDocumentNameDestinationDictionary nameDestinationDictionary = splitDocument.getDocumentCatalog().getDests();
            assertNotNull("split document " + i + " should have a dests dictionary", nameDestinationDictionary);

            // only the destination pointing to this page should be present
            assertEquals(1, nameDestinationDictionary.getCOSObject().size());

            String expectedName = "dest" + i;
            PDDestination copied = nameDestinationDictionary.getDestination(expectedName);
            assertTrue(copied instanceof PDPageFitDestination);
            // the destination must point to the imported page of the split document
            PDPage importedPage = ((PDPageFitDestination) copied).getPage();
            assertSame(splitDocument.getPage(0).getCOSObject(), importedPage.getCOSObject());
        }
    }

    // Start Claude Sonnet 5.5 generated tests

    /**
     * Links must be fixed per split document: a link to a page in the same split document points to
     * the imported page, a link to a page of another split document has no page.
     */
    public void testSplitFixesLinkDestinationsPerDocument() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage[] pages = new PDPage[4];
        for (int i = 0; i < pages.length; ++i)
        {
            pages[i] = new PDPage();
            document.addPage(pages[i]);
        }
        addLink(pages[0], pages[1]); // same split document
        addLink(pages[0], pages[2]); // other split document
        addLink(pages[2], pages[3]); // same split document

        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(2);
        List<PDDocument> split = splitter.split(document);
        try
        {
            List<PDAnnotation> annotations0 = split.get(0).getPage(0).getAnnotations();
            assertEquals(2, annotations0.size());
            PDPageDestination inside0
                    = (PDPageDestination) ((PDAnnotationLink) annotations0.get(0)).getDestination();
            PDPageDestination outside0
                    = (PDPageDestination) ((PDAnnotationLink) annotations0.get(1)).getDestination();
            assertEquals(1, split.get(0).getPages().indexOf(inside0.getPage()));
            assertNull(outside0.getPage());

            List<PDAnnotation> annotations1 = split.get(1).getPage(0).getAnnotations();
            assertEquals(1, annotations1.size());
            PDPageDestination inside1
                    = (PDPageDestination) ((PDAnnotationLink) annotations1.get(0)).getDestination();
            assertEquals(1, split.get(1).getPages().indexOf(inside1.getPage()));
        }
        finally
        {
            for (PDDocument dstDoc : split)
            {
                dstDoc.close();
            }
        }
        document.close();
    }

    private static void addLink(PDPage from, PDPage to) throws IOException
    {
        PDPageFitDestination destination = new PDPageFitDestination();
        destination.setPage(to);
        PDAnnotationLink link = new PDAnnotationLink();
        link.setDestination(destination);
        link.setRectangle(new PDRectangle(10, 600, 100, 100));
        List<PDAnnotation> annotations = new ArrayList<PDAnnotation>(from.getAnnotations());
        annotations.add(link);
        from.setAnnotations(annotations);
    }

    public void testSplitStartPageBeyondLastPage() throws IOException
    {
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());
        Splitter splitter = new Splitter();
        splitter.setStartPage(5);
        assertTrue(splitter.split(document).isEmpty());
        document.close();
    }

    /**
     * All documents created so far must be closed when splitting fails, and the original exception
     * must reach the caller.
     */
    public void testSplitClosesCreatedDocumentsOnFailure() throws IOException
    {
        final List<PDDocument> created = new ArrayList<PDDocument>();
        Splitter splitter = new Splitter()
        {
            @Override
            protected PDDocument createNewDocument() throws IOException
            {
                if (created.size() == 2)
                {
                    throw new IOException("simulated failure");
                }
                PDDocument newDocument = super.createNewDocument();
                created.add(newDocument);
                return newDocument;
            }
        };
        PDDocument source = new PDDocument();
        for (int i = 0; i < 3; ++i)
        {
            source.addPage(new PDPage());
        }
        try
        {
            splitter.split(source);
            fail("simulated failure expected");
        }
        catch (IOException ex)
        {
            assertEquals("simulated failure", ex.getMessage());
        }
        source.close();
        assertEquals(2, created.size());
        for (PDDocument newDocument : created)
        {
            assertTrue(newDocument.getDocument().isClosed());
        }
    }

    public void testSplitterCanBeReused() throws IOException
    {
        Splitter splitter = new Splitter();
        for (int pageCount : new int[] { 3, 2 })
        {
            PDDocument source = new PDDocument();
            for (int i = 0; i < pageCount; ++i)
            {
                source.addPage(new PDPage());
            }
            List<PDDocument> split = splitter.split(source);
            assertEquals(pageCount, split.size());
            for (PDDocument dstDoc : split)
            {
                assertEquals(1, dstDoc.getNumberOfPages());
                dstDoc.close();
            }
            source.close();
        }
    }
    
    // End Claude Sonnet 5.5 generated tests
}
