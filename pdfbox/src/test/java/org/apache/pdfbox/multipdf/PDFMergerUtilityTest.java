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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test suite for PDFMergerUtility.
 *
 * @author Maruan Sahyoun (PDF files)
 * @author Tilman Hausherr (code)
 */
@Execution(ExecutionMode.CONCURRENT)
class PDFMergerUtilityTest
{
    private static final String SRCDIR = "src/test/resources/input/merge/";
    private static final String TARGETTESTDIR = "target/test-output/merge/";
    private static final File TARGETPDFDIR = new File("target/pdfs");
    private static final int DPI = 96;

    @BeforeAll
    static void setUp()
    {
        new File(TARGETTESTDIR).mkdirs();
    }

    /**
     * Tests whether the merge of two PDF files with identically named but
     * different global resources works. The two PDF files have two fonts each
     * named /TT1 and /TT0 that are Arial and Courier and vice versa in the
     * second file. Revisions before 1613017 fail this test because global
     * resources were merged which made trouble when resources of the same kind
     * had the same name.
     *
     * @throws IOException if something goes wrong.
     */
    @Test
    void testPDFMergerUtility() throws IOException
    {
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult1.pdf", 
                IOUtils.createMemoryOnlyStreamCache());
        
        // once again, with scratch file
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult2.pdf", 
                IOUtils.createTempFileOnlyStreamCache());
    }

    // see PDFBOX-2893
    @Test
    void testPDFMergerUtility2() throws IOException
    {
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
                "GlobalResourceMergeTestResult3.pdf",
                IOUtils.createMemoryOnlyStreamCache());

        // once again, with scratch file
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
                "GlobalResourceMergeTestResult4.pdf",
                IOUtils.createTempFileOnlyStreamCache());
    }
    
    /**
     * Tests whether the merge of two PDF files with JPEG and CCITT works. A few revisions before
     * 1704911 this test failed because the clone utility attempted to decode and re-encode the
     * streams, see PDFBOX-2893 on 23.9.2015.
     *
     * @throws IOException if something goes wrong.
     */
    @Test
    void testJpegCcitt() throws IOException
    {
        checkMergeIdentical("jpegrgb.pdf",
                "multitiff.pdf",
                "JpegMultiMergeTestResult.pdf",
                IOUtils.createMemoryOnlyStreamCache());

        // once again, with scratch file
        checkMergeIdentical("jpegrgb.pdf",
                "multitiff.pdf",
                "JpegMultiMergeTestResult.pdf",
                IOUtils.createTempFileOnlyStreamCache());
    }

    /**
     * PDFBOX-3972: Test that OpenAction page destination isn't lost after merge.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFMergerOpenAction() throws IOException
    {
        try (PDDocument doc1 = new PDDocument())
        {
            doc1.addPage(new PDPage());
            doc1.addPage(new PDPage());
            doc1.addPage(new PDPage());
            doc1.save(new File(TARGETTESTDIR,"MergerOpenActionTest1.pdf"));
        }
        
        PDPageDestination dest;
        try (PDDocument doc2 = new PDDocument())
        {
            doc2.addPage(new PDPage());
            doc2.addPage(new PDPage());
            doc2.addPage(new PDPage());
            dest = new PDPageFitDestination();
            dest.setPage(doc2.getPage(1));
            doc2.getDocumentCatalog().setOpenAction(dest);
            doc2.save(new File(TARGETTESTDIR,"MergerOpenActionTest2.pdf"));
        }

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest1.pdf"));
        pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest2.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "MergerOpenActionTestResult.pdf");
        pdfMergerUtility.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

        try (PDDocument mergedDoc = Loader
                .loadPDF(new File(TARGETTESTDIR, "MergerOpenActionTestResult.pdf")))
        {
            PDDocumentCatalog documentCatalog = mergedDoc.getDocumentCatalog();
            dest = (PDPageDestination) documentCatalog.getOpenAction();
            assertEquals(4, documentCatalog.getPages().indexOf(dest.getPage()));
        }
    }

    /**
     * PDFBOX-3999: check that page entries in the structure tree only reference pages from the page
     * tree, i.e. that no orphan pages exist.
     * 
     * @throws IOException 
     */
    @Test
    void testStructureTreeMerge() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(134, singleCnt);
        assertEquals(134, singleSetSize);

        PDDocument dst = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));
        dst.close();

        PDDocument doc = Loader
                .loadPDF(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));

        // Assume that the merged tree has double element count
        elementCounter = new ElementCounter();
        elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
        assertEquals(singleCnt * 2, elementCounter.cnt);
        assertEquals(singleSetSize * 2, elementCounter.set.size());
        checkForPageOrphans(doc);

        doc.close();
    }

    /**
     * PDFBOX-3999: check that no streams are kept from the source document by the destination
     * document, despite orphan annotations remaining in the structure tree.
     *
     * @throws IOException
     */
    @Test
    void testStructureTreeMerge2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
        doc.getDocumentCatalog().getAcroForm().flatten();
        doc.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(134, singleCnt);
        assertEquals(134, singleSetSize);

        doc.close();

        PDDocument src = Loader
                .loadPDF(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
        PDDocument dst = Loader
                .loadPDF(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        // before solving PDFBOX-3999, the close() below brought
        // IOException: COSStream has been closed and cannot be read.
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));
        dst.close();

        doc = Loader.loadPDF(
                new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));

        checkForPageOrphans(doc);

        // Assume that the merged tree has double element count
        elementCounter = new ElementCounter();
        elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
        assertEquals(singleCnt * 2, elementCounter.cnt);
        assertEquals(singleSetSize * 2, elementCounter.set.size());

        doc.close();
    }

    /**
     * PDFBOX-4408: Check that /StructParents values from pages and /StructParent values from
     * annotations are found in the /ParentTree.
     *
     * @throws IOException
     */
    @Test
    void testStructureTreeMerge3() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(25, singleCnt);
        assertEquals(25, singleSetSize);

        PDDocument dst = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));
        dst.close();

        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));

        // Assume that the merged tree has double element count
        elementCounter = new ElementCounter();
        elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
        assertEquals(singleCnt * 2, elementCounter.cnt);
        assertEquals(singleSetSize * 2, elementCounter.set.size());

        checkWithNumberTree(dst);
        checkForPageOrphans(dst);
        dst.close();
        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));
    }

    /**
     * PDFBOX-4417: Same as the previous tests, but this one failed when the previous tests
     * succeeded because of more bugs with cloning.
     *
     * @throws IOException
     */
    @Test
    void testStructureTreeMerge4() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(104, singleCnt);
        assertEquals(104, singleSetSize);

        PDDocument dst = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));
        dst.close();
        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));

        // Assume that the merged tree has double element count
        elementCounter = new ElementCounter();
        elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
        assertEquals(singleCnt * 2, elementCounter.cnt);
        assertEquals(singleSetSize * 2, elementCounter.set.size());

        checkWithNumberTree(dst);
        checkForPageOrphans(dst);
        dst.close();
        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));
    }

    /**
     * PDFBOX-4417: Same as the previous tests, but this one failed when the previous tests
     * succeeded because the /K tree started with two dictionaries and not with an array.
     *
     * @throws IOException 
     */
    @Test
    void testStructureTreeMerge5() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();

        PDDocument dst = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
        dst.close();
        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        // Assume that the merged tree has double element count
        elementCounter = new ElementCounter();
        elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
        assertEquals(singleCnt * 2, elementCounter.cnt);
        assertEquals(singleSetSize * 2, elementCounter.set.size());

        dst.close();
        
        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
    }

    /**
     * PDFBOX-4418: test merging PDFs where ParentTree have a hierarchy.
     * 
     * @throws IOException 
     */
    @Test
    void testStructureTreeMerge6() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4418-000671.pdf"));

        PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
        PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        assertEquals(381, numberTreeAsMap.size());
        assertEquals(743, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(743, structureTreeRoot.getParentTreeNextKey());        

        PDDocument dst = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        assertEquals(7, numberTreeAsMap.size());
        assertEquals(328, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(321, (int) Collections.min(numberTreeAsMap.keySet()));
        // ParentTreeNextKey should be 321 but PDF has a higher value
        assertEquals(408, structureTreeRoot.getParentTreeNextKey());

        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
        dst.close();

        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        assertEquals(381+7, numberTreeAsMap.size());
        assertEquals(408+743, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(321, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(408+743, structureTreeRoot.getParentTreeNextKey());
        dst.close();

        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
    }

    /**
     * PDFBOX-4423: test merging a PDF where a widget has no StructParent.
     * 
     * @throws IOException 
     */
    @Test
    void testStructureTreeMerge7() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4423-000746.pdf"));

        PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
        PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        assertEquals(33, numberTreeAsMap.size());
        assertEquals(64, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(31, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(126, structureTreeRoot.getParentTreeNextKey());        

        PDDocument dst = new PDDocument();

        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
        dst.close();

        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        assertEquals(33, numberTreeAsMap.size());
        assertEquals(64, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(31, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(64, structureTreeRoot.getParentTreeNextKey());
        dst.close();

        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
    }

    /**
     * PDFBOX-4009: Test that ParentTreeNextKey is recalculated correctly.
     */
    @Test
    void testMissingParentTreeNextKey() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
        PDDocument dst = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
        // existing numbers are 321..327; ParentTreeNextKey is 408. 
        // After deletion, it is recalculated in the merge 328.
        // That value is added to all numbers of the destination,
        // so the new numbers should be 321+328..327+328, i.e. 649..655,
        // and this ParentTreeNextKey is 656 at the end.
        dst.getDocumentCatalog().getStructureTreeRoot().getCOSObject().removeItem(COSName.PARENT_TREE_NEXT_KEY);
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4418-000314-merged.pdf"));
        dst.close();
        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4418-000314-merged.pdf"));
        assertEquals(656, dst.getDocumentCatalog().getStructureTreeRoot().getParentTreeNextKey());
        dst.close();
    }

    /**
     * PDFBOX-4416: Test merging of /IDTree
     * <br>
     * PDFBOX-4009: test merging to empty destination
     *
     * @throws IOException 
     */
    @Test
    void testStructureTreeMergeIDTree() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
        PDDocument dst = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

        PDNameTreeNode<PDStructureElement> srcIDTree = src.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        Map<String, PDStructureElement> srcIDTreeMap = PDFMergerUtility.getIDTreeAsMap(srcIDTree);
        PDNameTreeNode<PDStructureElement> dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        Map<String, PDStructureElement> dstIDTreeMap = PDFMergerUtility.getIDTreeAsMap(dstIDTree);
        int expectedTotal = srcIDTreeMap.size() + dstIDTreeMap.size();
        assertEquals(192, expectedTotal);

        // PDFBOX-4009, test that empty dest doc still merges structure tree
        // (empty dest doc is used in command line app)
        PDDocument emptyDest = new PDDocument();
        pdfMergerUtility.appendDocument(emptyDest, src);
        src.close();
        src = emptyDest;
        assertEquals(4, src.getDocumentCatalog().getStructureTreeRoot().getParentTreeNextKey());

        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
        dst.close();
        dst = Loader.loadPDF(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        dstIDTreeMap = PDFMergerUtility.getIDTreeAsMap(dstIDTree);
        assertEquals(expectedTotal, dstIDTreeMap.size());

        dst.close();
        checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
    }

    /**
     * PDFBOX-4429: merge into destination that has /StructParent(s) entries in the destination file
     * but no structure tree.
     *
     * @throws IOException
     */
    @Test
    void testMergeBogusStructParents1() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        try (PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
             PDDocument dst = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf")))
        {
            dst.getDocumentCatalog().setStructureTreeRoot(null);
            dst.getPage(0).setStructParents(9999);
            dst.getPage(0).getAnnotations().get(0).setStructParent(9998);
            pdfMergerUtility.appendDocument(dst, src);
            checkWithNumberTree(dst);
            checkForPageOrphans(dst);
        }
    }

    /**
     * PDFBOX-4429: merge into destination that has /StructParent(s) entries in the source file but
     * no structure tree.
     *
     * @throws IOException
     */
    @Test
    void testMergeBogusStructParents2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        try (PDDocument src = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
             PDDocument dst = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4408.pdf")))
        {
            src.getDocumentCatalog().setStructureTreeRoot(null);
            src.getPage(0).setStructParents(9999);
            src.getPage(0).getAnnotations().get(0).setStructParent(9998);
            pdfMergerUtility.appendDocument(dst, src);
            checkWithNumberTree(dst);
            checkForPageOrphans(dst);
        }
    }

    /**
     * Test of the parent tree. Didn't work before PDFBOX-4003 because of incompatible class for
     * PDNumberTreeNode.
     *
     * @throws IOException
     */
    @Test
    void testParentTree() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf")))
        {
            PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
            PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
            parentTree.getValue(0);
            Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
            assertEquals(31, numberTreeAsMap.size());
            assertEquals(31, Collections.max(numberTreeAsMap.keySet()) + 1);
            assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
            assertEquals(31, structureTreeRoot.getParentTreeNextKey());
        }
    }

    // PDFBOX-4417: check for multiple /StructTreeRoot entries that was due to
    // incorrect merging of /K entries
    private void checkStructTreeRootCount(File file) throws IOException
    {
        try (PDDocument pdf = Loader.loadPDF(file))
        {
            List<COSObject> structTreeRootObjects = pdf.getDocument().getObjectsByType(COSName.STRUCT_TREE_ROOT);
            assertEquals(1, structTreeRootObjects.size(), file.getPath() + " " + structTreeRootObjects);
        }
    }

    /**
     * PDFBOX-4408: Check that /StructParents values from pages and /StructParent values from
     * annotations are found in the /ParentTree.
     *
     * @param document
     */
    void checkWithNumberTree(PDDocument document) throws IOException
    {
        PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
        PDNumberTreeNode parentTree = documentCatalog.getStructureTreeRoot().getParentTree();
        Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
        Set<Integer> keySet = numberTreeAsMap.keySet();
        PDAcroForm acroForm = documentCatalog.getAcroForm();
        if (acroForm != null)
        {
            for (PDField field : acroForm.getFieldTree())
            {
                for (PDAnnotationWidget widget : field.getWidgets())
                {
                    if (widget.getStructParent() >= 0)
                    {
                        assertTrue(keySet.contains(widget.getStructParent()),
                                "field '" + field.getFullyQualifiedName() + "' /StructParent "
                                        + widget.getStructParent() + " missing in /ParentTree");
                    }
                }
            }
        }
        for (PDPage page : document.getPages())
        {
            if (page.getStructParents() >= 0)
            {
                assertTrue(keySet.contains(page.getStructParents()));
            }
            for (PDAnnotation ann : page.getAnnotations())
            {
                if (ann.getStructParent() >= 0)
                {
                    assertTrue(keySet.contains(ann.getStructParent()),
                            "/StructParent " + ann.getStructParent() + " missing in /ParentTree");
                }
            }
        }

        // might also test image and form dictionaries...
    }

    /**
     * PDFBOX-4383: Test that file can be deleted after merge.
     *
     * @throws IOException 
     */
    @Test
    void testFileDeletion() throws IOException
    {
        File outFile = new File(TARGETTESTDIR, "PDFBOX-4383-result.pdf");

        File inFile1 = new File(TARGETTESTDIR, "PDFBOX-4383-src1.pdf");
        File inFile2 = new File(TARGETTESTDIR, "PDFBOX-4383-src2.pdf");

        createSimpleFile(inFile1);
        createSimpleFile(inFile2);

        try (OutputStream out = new FileOutputStream(outFile))
        {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationStream(out);

            merger.addSource(inFile1);
            merger.addSource(inFile2);

            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
        }

        Files.delete(inFile1.toPath());
        Files.delete(inFile2.toPath());
        Files.delete(outFile.toPath());
    }

    /**
     * Check that there is a top level Document and Parts below in a merge of 2 documents.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox5198_2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFA3A-merged2.pdf");
        pdfMergerUtility.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

        checkParts(new File(TARGETTESTDIR + "PDFA3A-merged2.pdf"));
    }
    
    /**
     * Check that there is a top level Document and Parts below in a merge of 3 documents.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBox5198_3() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFA3A-merged3.pdf");
        pdfMergerUtility.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

        checkParts(new File(TARGETTESTDIR + "PDFA3A-merged3.pdf"));
    }

    /**
     * Check that there is a top level Document and Parts below.
     * @param file
     * @throws IOException 
     */
    private void checkParts(File file) throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
            COSDictionary topDict = (COSDictionary) structureTreeRoot.getK();
            assertEquals(COSName.DOCUMENT, topDict.getItem(COSName.S));
            assertEquals(structureTreeRoot.getCOSObject(), topDict.getCOSDictionary(COSName.P));
            COSArray kArray = topDict.getCOSArray(COSName.K);
            assertEquals(doc.getNumberOfPages(), kArray.size());
            for (int i = 0; i < kArray.size(); ++i)
            {
                COSDictionary dict = (COSDictionary) kArray.getObject(i);
                assertEquals(COSName.PART, dict.getItem(COSName.S));
                assertEquals(topDict, dict.getCOSDictionary(COSName.P));
            }
        }
    }

    private void checkForPageOrphans(PDDocument doc) throws IOException
    {
        // check for orphan pages in the StructTreeRoot/K, StructTreeRoot/ParentTree and
        // StructTreeRoot/IDTree trees.
        PDPageTree pageTree = doc.getPages();
        PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
        checkElement(pageTree, structureTreeRoot.getParentTree().getCOSObject());
        checkElement(pageTree, structureTreeRoot.getK());
        checkForIDTreeOrphans(pageTree, structureTreeRoot);
    }

    private void checkForIDTreeOrphans(PDPageTree pageTree, PDStructureTreeRoot structureTreeRoot)
            throws IOException
    {
        PDNameTreeNode<PDStructureElement> idTree = structureTreeRoot.getIDTree();
        if (idTree == null)
        {
            return;
        }
        Map<String, PDStructureElement> map = PDFMergerUtility.getIDTreeAsMap(idTree);
        for (PDStructureElement element : map.values())
        {
            if (element.getPage() != null)
            {
                checkForPage(pageTree, element);
            }
            if (!element.getKids().isEmpty())
            {
                checkElement(pageTree, element.getCOSObject().getDictionaryObject(COSName.K));
            }
        }
    }

    private void createSimpleFile(File file) throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            doc.addPage(new PDPage());
            doc.save(file);
        }
    }

    private class ElementCounter
    {
        int cnt = 0;
        final Set<COSBase> set = new HashSet<>();

        void walk(COSBase base)
        {
            if (base instanceof COSArray)
            {
                for (COSBase base2 : (COSArray) base)
                {
                    if (base2 instanceof COSObject)
                    {
                        base2 = ((COSObject) base2).getObject();
                    }
                    walk(base2);
                }
            }
            else if (base instanceof COSDictionary)
            {
                COSDictionary kdict = (COSDictionary) base;
                if (kdict.containsKey(COSName.PG))
                {
                    ++cnt;
                    set.add(kdict);
                }
                if (kdict.containsKey(COSName.K))
                {
                    walk(kdict.getDictionaryObject(COSName.K));
                }
            }
        }
    }

    // Each element can be an array, a dictionary or a number.
    // See PDF specification Table 37 - Entries in a number tree node dictionary
    // See PDF specification Table 322 - Entries in the structure tree root
    // See PDF specification Table 323 - Entries in a structure element dictionary
    // See PDF specification Table 325 – Entries in an object reference dictionary
    // example of file with /Kids: 000153.pdf 000208.pdf 000314.pdf 000359.pdf 000671.pdf
    // from digitalcorpora site
    private void checkElement(PDPageTree pageTree, COSBase base) throws IOException
    {
        if (base instanceof COSArray)
        {
            for (COSBase base2 : (COSArray) base)
            {
                if (base2 instanceof COSObject)
                {
                    base2 = ((COSObject) base2).getObject();
                }
                checkElement(pageTree, base2);
            }
        }
        else if (base instanceof COSDictionary)
        {
            COSDictionary kdict = (COSDictionary) base;
            if (kdict.containsKey(COSName.PG))
            {
                PDStructureElement structureElement = new PDStructureElement(kdict);
                checkForPage(pageTree, structureElement);
            }
            if (kdict.containsKey(COSName.K))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.K));
                return;
            }

            // if we're in a number tree, check /Nums and /Kids
            if (kdict.containsKey(COSName.KIDS))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.KIDS));
            }
            else if (kdict.containsKey(COSName.NUMS))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.NUMS));
            }

            // if we're an object reference dictionary (/OBJR), check the obj
            if (kdict.containsKey(COSName.OBJ))
            {
                COSDictionary obj = (COSDictionary) kdict.getDictionaryObject(COSName.OBJ);
                COSBase type = obj.getDictionaryObject(COSName.TYPE);
                if (COSName.ANNOT.equals(type))
                {
                    PDAnnotation annotation = PDAnnotation.createAnnotation(obj);
                    PDPage page = annotation.getPage();
                    if (page != null)
                    {
                        if (pageTree.indexOf(page) == -1)
                        {
                            COSBase item = kdict.getItem(COSName.OBJ);
                            if (item instanceof COSObject)
                            {
                                assertNotEquals(-1, pageTree.indexOf(page),
                                        "Annotation page is not in the page tree: " + item);
                            }
                            else
                            {
                                // don't display because of stack overflow
                                assertNotEquals(-1, pageTree.indexOf(page),
                                        "Annotation page is not in the page tree");
                            }
                        }
                    }
                }
                else
                {
                    //TODO needs to be investigated. Specification mentions
                    // "such as an XObject or an annotation"
                    fail("Other type: " + type);
                }
            }
        }
    }

    // checks that the result file of a merge has the same rendering as the two source files
    private void checkMergeIdentical(String filename1, String filename2, String mergeFilename, 
            StreamCacheCreateFunction streamCache)
            throws IOException
    {
        int src1PageCount;
        BufferedImage[] src1ImageTab;
        try (PDDocument srcDoc1 = Loader.loadPDF(new File(SRCDIR, filename1), (String) null))
        {
            src1PageCount = srcDoc1.getNumberOfPages();
            PDFRenderer src1PdfRenderer = new PDFRenderer(srcDoc1);
            src1ImageTab = new BufferedImage[src1PageCount];
            for (int page = 0; page < src1PageCount; ++page)
            {
                src1ImageTab[page] = src1PdfRenderer.renderImageWithDPI(page, DPI);
            }
        }

        int src2PageCount;
        BufferedImage[] src2ImageTab;
        try (PDDocument srcDoc2 = Loader.loadPDF(new File(SRCDIR, filename2), (String) null))
        {
            src2PageCount = srcDoc2.getNumberOfPages();
            PDFRenderer src2PdfRenderer = new PDFRenderer(srcDoc2);
            src2ImageTab = new BufferedImage[src2PageCount];
            for (int page = 0; page < src2PageCount; ++page)
            {
                src2ImageTab[page] = src2PdfRenderer.renderImageWithDPI(page, DPI);
            }
        }

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, filename1));
        pdfMergerUtility.addSource(new File(SRCDIR, filename2));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + mergeFilename);
        pdfMergerUtility.mergeDocuments(streamCache);

        try (PDDocument mergedDoc = Loader.loadPDF(new File(TARGETTESTDIR, mergeFilename),
                (String) null))
        {
            PDFRenderer mergePdfRenderer = new PDFRenderer(mergedDoc);
            int mergePageCount = mergedDoc.getNumberOfPages();
            assertEquals(src1PageCount + src2PageCount, mergePageCount);
            for (int page = 0; page < src1PageCount; ++page)
            {
                BufferedImage bim = mergePdfRenderer.renderImageWithDPI(page, DPI);
                checkImagesIdentical(bim, src1ImageTab[page]);
            }
            for (int page = 0; page < src2PageCount; ++page)
            {
                int mergePage = page + src1PageCount;
                BufferedImage bim = mergePdfRenderer.renderImageWithDPI(mergePage, DPI);
                checkImagesIdentical(bim, src2ImageTab[page]);
            }
        }
    }

    private void checkImagesIdentical(BufferedImage bim1, BufferedImage bim2)
    {
        assertEquals(bim1.getHeight(), bim2.getHeight());
        assertEquals(bim1.getWidth(), bim2.getWidth());
        int w = bim1.getWidth();
        int h = bim1.getHeight();
        for (int i = 0; i < w; ++i)
        {
            for (int j = 0; j < h; ++j)
            {
                assertEquals(bim1.getRGB(i, j), bim2.getRGB(i, j));
            }
        }
    }

    private void checkForPage(PDPageTree pageTree, PDStructureElement structureElement)
    {
        PDPage page = structureElement.getPage();
        if (page != null)
        {
            assertNotEquals(-1, pageTree.indexOf(page), "Page is not in the page tree");
        }
    }

    @Test
    void testSplitWithStructureTree() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(SRCDIR, "PDFBOX-4417-001031.pdf")))
        {
            Splitter splitter = new Splitter();
            splitter.setStartPage(1);
            splitter.setEndPage(2);
            splitter.setSplitAtPage(2);
            List<PDDocument> splitResult = splitter.split(doc);
            assertEquals(1, splitResult.size());
            try (PDDocument dstDoc = splitResult.get(0))
            {
                assertEquals(2, dstDoc.getNumberOfPages());
                checkForPageOrphans(dstDoc);
                // these tests just verify the status quo. Changes should be checked visually with
                // a PDF viewer that can display structural information.
                PDStructureTreeRoot structureTreeRoot = dstDoc.getDocumentCatalog().getStructureTreeRoot();
                assertEquals(126, PDFMergerUtility.getIDTreeAsMap(structureTreeRoot.getIDTree()).size());
                assertEquals(2, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot.getParentTree()).size());
                assertEquals(6, structureTreeRoot.getRoleMap().size());
            }
        }
    }

    @Test
    void testSplitWithStructureTreeAndDestinations() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(SRCDIR,"PDFBOX-5762-722238.pdf")))
        {
            Splitter splitter = new Splitter();
            splitter.setStartPage(1);
            splitter.setEndPage(2);
            splitter.setSplitAtPage(2);
            List<PDDocument> splitResult = splitter.split(doc);
            assertEquals(1, splitResult.size());
            try (PDDocument dstDoc = splitResult.get(0))
            {
                assertEquals(2, dstDoc.getNumberOfPages());
                checkForPageOrphans(dstDoc);
                // these tests just verify the status quo. Changes should be checked visually with
                // a PDF viewer that can display structural information.
                PDStructureTreeRoot structureTreeRoot = dstDoc.getDocumentCatalog().getStructureTreeRoot();
                assertEquals(7, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot.getParentTree()).size());
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
            }
        }
    }

    /**
     * Check for the bug that happened in PDFBOX-5792, where a destination was outside a target
     * document and hit an NPE in the next call of Splitter.fixDestinations().
     *
     * @throws IOException
     */
    @Test
    void testSinglePageSplit() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(SRCDIR, "PDFBOX-5792-240045.pdf")))
        {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(1);
            List<PDDocument> splitResult = splitter.split(doc);
            assertEquals(6, splitResult.size());
            for (PDDocument dstDoc : splitResult)
            {
                assertEquals(1, dstDoc.getNumberOfPages());
                checkForPageOrphans(dstDoc);
                for (PDAnnotation ann : dstDoc.getPage(0).getAnnotations())
                {
                    PDAnnotationLink link = (PDAnnotationLink) ann;
                    PDActionGoTo action = (PDActionGoTo) link.getAction();
                    PDPageDestination destination = (PDPageDestination) ((PDActionGoTo) action).getDestination();
                    assertNull(destination.getPage());
                }
            }
            PDStructureTreeRoot structureTreeRoot1 = splitResult.get(0).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(6, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot1.getParentTree()).size());
            assertEquals(3, structureTreeRoot1.getRoleMap().size());
            PDStructureTreeRoot structureTreeRoot2 = splitResult.get(1).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(6, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot2.getParentTree()).size());
            assertEquals(3, structureTreeRoot2.getRoleMap().size());
            PDStructureTreeRoot structureTreeRoot3 = splitResult.get(2).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(6, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot3.getParentTree()).size());
            assertEquals(4, structureTreeRoot3.getRoleMap().size());
            PDStructureTreeRoot structureTreeRoot4 = splitResult.get(3).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(5, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot4.getParentTree()).size());
            assertEquals(4, structureTreeRoot4.getRoleMap().size());
            PDStructureTreeRoot structureTreeRoot5 = splitResult.get(4).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(1, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot5.getParentTree()).size());
            assertEquals(6, structureTreeRoot5.getRoleMap().size());
            PDStructureTreeRoot structureTreeRoot6 = splitResult.get(5).getDocumentCatalog().getStructureTreeRoot();
            assertEquals(1, PDFMergerUtility.getNumberTreeAsMap(structureTreeRoot6.getParentTree()).size());
            assertEquals(7, structureTreeRoot6.getRoleMap().size());
            for (PDDocument dstDoc : splitResult)
            {
                dstDoc.close();
            }
        }
    }
}
