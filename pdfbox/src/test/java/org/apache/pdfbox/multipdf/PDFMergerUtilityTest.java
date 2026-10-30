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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkedContentReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDObjectReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDParentTreeValue;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.junit.Assert;
import static org.junit.Assert.assertNotEquals;

/**
 * Test suite for PDFMergerUtility.
 *
 * @author Maruan Sahyoun (PDF files)
 * @author Tilman Hausherr (code)
 */
public class PDFMergerUtilityTest extends TestCase
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
    public void testPDFMergerUtility() throws IOException
    {
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult.pdf", 
                MemoryUsageSetting.setupMainMemoryOnly());
        
        // once again, with scratch file
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult2.pdf", 
                MemoryUsageSetting.setupTempFileOnly());
    }

    /**
     * Tests whether the merge of two PDF files with JPEG and CCITT works. A few revisions before
     * 1704911 this test failed because the clone utility attempted to decode and re-encode the
     * streams, see PDFBOX-2893 on 23.9.2015.
     *
     * @throws IOException if something goes wrong.
     */
    public void testJpegCcitt() throws IOException
    {
        checkMergeIdentical("jpegrgb.pdf",
                "multitiff.pdf",
                "JpegMultiMergeTestResult.pdf",
                MemoryUsageSetting.setupMainMemoryOnly());

        // once again, with scratch file
        checkMergeIdentical("jpegrgb.pdf",
                "multitiff.pdf",
                "JpegMultiMergeTestResult.pdf",
                MemoryUsageSetting.setupTempFileOnly());
    }

    // see PDFBOX-2893
    public void testPDFMergerUtility2() throws IOException
    {
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
                "GlobalResourceMergeTestResult.pdf",
                MemoryUsageSetting.setupMainMemoryOnly());

        // once again, with scratch file
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
                "GlobalResourceMergeTestResult2.pdf",
                MemoryUsageSetting.setupTempFileOnly());
    }

    /**
     * PDFBOX-3972: Test that OpenAction page destination isn't lost after merge.
     * 
     * @throws IOException 
     */
    public void testPDFMergerOpenAction() throws IOException
    {
        PDDocument doc1 = new PDDocument();
        doc1.addPage(new PDPage());
        doc1.addPage(new PDPage());
        doc1.addPage(new PDPage());
        doc1.save(new File(TARGETTESTDIR,"MergerOpenActionTest1.pdf"));
        doc1.close();

        PDDocument doc2 = new PDDocument();
        doc2.addPage(new PDPage());
        doc2.addPage(new PDPage());
        doc2.addPage(new PDPage());
        PDPageDestination dest = new PDPageFitDestination();
        dest.setPage(doc2.getPage(1));
        doc2.getDocumentCatalog().setOpenAction(dest);
        doc2.save(new File(TARGETTESTDIR,"MergerOpenActionTest2.pdf"));
        doc2.close();

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest1.pdf"));
        pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest2.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "MergerOpenActionTestResult.pdf");
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        PDDocument mergedDoc = PDDocument.load(new File(TARGETTESTDIR, "MergerOpenActionTestResult.pdf"));
        PDDocumentCatalog documentCatalog = mergedDoc.getDocumentCatalog();
        dest = (PDPageDestination) documentCatalog.getOpenAction();
        assertEquals(4, documentCatalog.getPages().indexOf(dest.getPage()));
        mergedDoc.close();
    }

    /**
     * PDFBOX-3999: check that page entries in the structure tree only reference pages from the page
     * tree, i.e. that no orphan pages exist.
     * 
     * @throws IOException 
     */
    public void testStructureTreeMerge() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(134, singleCnt);
        assertEquals(134, singleSetSize);

        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));
        dst.close();

        PDDocument doc = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));

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
    public void testStructureTreeMerge2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument doc = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
        doc.getDocumentCatalog().getAcroForm().flatten();
        doc.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(134, singleCnt);
        assertEquals(134, singleSetSize);

        doc.close();

        PDDocument src = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
        PDDocument dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        // before solving PDFBOX-3999, the close() below brought
        // IOException: COSStream has been closed and cannot be read.
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));
        dst.close();

        doc = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));

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
    public void testStructureTreeMerge3() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(25, singleCnt);
        assertEquals(25, singleSetSize);

        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));
        dst.close();

        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));

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
    public void testStructureTreeMerge4() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();
        assertEquals(104, singleCnt);
        assertEquals(104, singleSetSize);

        PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));
        dst.close();
        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));

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
    public void testStructureTreeMerge5() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
        int singleCnt = elementCounter.cnt;
        int singleSetSize = elementCounter.set.size();

        PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
        dst.close();
        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
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
    public void testStructureTreeMerge6() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4418-000671.pdf"));

        PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
        PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> numberTreeAsMap = parentTree.getTreeAsMap();
        assertEquals(381, numberTreeAsMap.size());
        assertEquals(743, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(743, structureTreeRoot.getParentTreeNextKey());        

        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = parentTree.getTreeAsMap();
        assertEquals(7, numberTreeAsMap.size());
        assertEquals(328, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(321, (int) Collections.min(numberTreeAsMap.keySet()));
        // ParentTreeNextKey should be 321 but PDF has a higher value
        assertEquals(408, structureTreeRoot.getParentTreeNextKey());

        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
        dst.close();

        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = parentTree.getTreeAsMap();
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
    public void testStructureTreeMerge7() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4423-000746.pdf"));

        PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
        PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> numberTreeAsMap = parentTree.getTreeAsMap();
        assertEquals(33, numberTreeAsMap.size());
        assertEquals(64, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(31, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(126, structureTreeRoot.getParentTreeNextKey());        

        PDDocument dst = new PDDocument();

        pdfMergerUtility.appendDocument(dst, src);
        src.close();
        dst.save(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
        dst.close();

        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
        parentTree = structureTreeRoot.getParentTree();
        numberTreeAsMap = parentTree.getTreeAsMap();
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
    public void testMissingParentTreeNextKey() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
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
        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4418-000314-merged.pdf"));
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
    public void testStructureTreeMergeIDTree() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
        PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

        PDNameTreeNode<PDStructureElement> srcIDTree = src.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        Map<String, PDStructureElement> srcIDTreeMap = srcIDTree.getTreeAsMap();
        PDNameTreeNode<PDStructureElement> dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        Map<String, PDStructureElement> dstIDTreeMap = dstIDTree.getTreeAsMap();
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
        dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);

        dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
        dstIDTreeMap = dstIDTree.getTreeAsMap();
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
    public void testMergeBogusStructParents1() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        dst.getDocumentCatalog().setStructureTreeRoot(null);
        dst.getPage(0).setStructParents(9999);
        dst.getPage(0).getAnnotations().get(0).setStructParent(9998);
        pdfMergerUtility.appendDocument(dst, src);
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);
        src.close();
        dst.close();
    }

    /**
     * PDFBOX-4429: merge into destination that has /StructParent(s) entries in the source file but
     * no structure tree.
     *
     * @throws IOException
     */
    public void testMergeBogusStructParents2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        PDDocument dst = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4408.pdf"));
        src.getDocumentCatalog().setStructureTreeRoot(null);
        src.getPage(0).setStructParents(9999);
        src.getPage(0).getAnnotations().get(0).setStructParent(9998);
        pdfMergerUtility.appendDocument(dst, src);
        checkWithNumberTree(dst);
        checkForPageOrphans(dst);
        src.close();
        dst.close();
    }

    /**
     * Test of the parent tree. Didn't work before PDFBOX-4003 because of incompatible class for
     * PDNumberTreeNode.
     *
     * @throws IOException
     */
    public void testParentTree() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
        PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
        PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
        parentTree.getValue(0);
        Map<Integer, COSObjectable> numberTreeAsMap = parentTree.getTreeAsMap();
        assertEquals(31, numberTreeAsMap.size());
        assertEquals(31, Collections.max(numberTreeAsMap.keySet()) + 1);
        assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
        assertEquals(31, structureTreeRoot.getParentTreeNextKey());
        doc.close();
    }

    // PDFBOX-4417: check for multiple /StructTreeRoot entries that was due to
    // incorrect merging of /K entries
    private void checkStructTreeRootCount(File file) throws IOException
    {
        int count = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        while ((line = br.readLine()) != null)
        {
            if (line.equals("/Type /StructTreeRoot"))
            {
                ++count;
            }
        }
        br.close();
        assertEquals(file.getPath(), 1, count);
    }

    /**
     * PDFBOX-6277: check that index 0 is present despite missing page labels in destination.
     */
    public void testPageLabels() throws IOException
    {
        PDDocument doc1 = new PDDocument();
        PDDocument doc2 = new PDDocument();

        doc1.addPage(new PDPage());
        doc1.addPage(new PDPage());
        doc1.addPage(new PDPage());
        PDPageLabels pageLabels = new PDPageLabels(doc1);
        PDPageLabelRange pageLabelRange1 = new PDPageLabelRange();
        pageLabelRange1.setPrefix("RO ");
        pageLabelRange1.setStart(3);
        pageLabelRange1.setStyle(PDPageLabelRange.STYLE_ROMAN_UPPER);
        pageLabels.setLabelItem(0, pageLabelRange1);
        PDPageLabelRange pageLabelRange2 = new PDPageLabelRange();
        pageLabelRange2.setStart(1);
        pageLabelRange2.setStyle(PDPageLabelRange.STYLE_DECIMAL);
        pageLabels.setLabelItem(2, pageLabelRange2);
        doc1.getDocumentCatalog().setPageLabels(pageLabels);

        doc2.addPage(new PDPage());
        doc2.addPage(new PDPage());
        doc2.addPage(new PDPage());

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.appendDocument(doc2, doc1);

        COSArray array = doc2.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.PAGE_LABELS).getCOSArray(COSName.NUMS);
        assertEquals(COSInteger.ZERO, array.getObject(0));
        assertTrue(array.getObject(1) instanceof COSDictionary);

        String[] labels = doc2.getDocumentCatalog().getPageLabels().getLabelsByPageIndices();
        assertEquals("[1, 2, 3, RO III, RO IV, 1]", Arrays.toString(labels));

        doc1.close();
        doc2.close();
    }

    /**
     * PDFBOX-4408: Check that /StructParents values from pages and /StructParent values from
     * annotations are found in the /ParentTree.
     * <p>
     * Expanded in 2025 to check that all MCIDs of a page content stream have an entry in the
     * ParentTree.
     *
     * @param document
     */
    static void checkWithNumberTree(PDDocument document) throws IOException
    {
        PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
        PDNumberTreeNode parentTree = documentCatalog.getStructureTreeRoot().getParentTree();
        assertNotEquals(-1, documentCatalog.getStructureTreeRoot().getParentTreeNextKey());
        Map<Integer, COSObjectable> numberTreeAsMap = parentTree.getTreeAsMap();
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
                        assertTrue("field '" + field.getFullyQualifiedName() + "' /StructParent " +
                                   widget.getStructParent() + " missing in /ParentTree",
                                   keySet.contains(widget.getStructParent()));
                    }
                }
            }
        }
        PDPageTree pageTree = document.getPages();
        for (PDPage page : pageTree)
        {
            int pageNum = pageTree.indexOf(page) + 1;
            if (page.getStructParents() >= 0)
            {
                assertTrue("/StructParents " + page.getStructParents() + " from page " + pageNum + " not found in /ParentTree",
                        keySet.contains(page.getStructParents()));
                PDParentTreeValue obj = (PDParentTreeValue) numberTreeAsMap.get(page.getStructParents());
                assertTrue("Expected array in page " + pageNum + ", got " + obj.getClass(),
                        obj.getCOSObject() instanceof COSArray);
                COSArray array = (COSArray) obj.getCOSObject();

                PDFMarkedContentExtractor markedContentExtractor = new PDFMarkedContentExtractor();
                markedContentExtractor.processPage(page);
                List<PDMarkedContent> markedContents = markedContentExtractor.getMarkedContents();
                TreeSet<Integer> set = new TreeSet<Integer>();
                for (PDMarkedContent pdMarkedContent : markedContents)
                {
                    COSDictionary pdmcProperties = pdMarkedContent.getProperties();
                    if (pdmcProperties == null)
                    {
                        continue;
                    }
                    int mcid = pdMarkedContent.getMCID();
                    if (mcid >= 0)
                    {
                        // "For a page object (...), the value shall be an array of references
                        // to the parent elements of those marked-content sequences."
                        // this means that the /Pg entry doesn't have to match the page
                        COSDictionary dict = (COSDictionary) array.getObject(mcid);
                        assertNotNull(dict);
                        set.add(mcid);
                        PDStructureElement structureElemen = (PDStructureElement) PDStructureNode.create(dict);
                        List<Object> kids = structureElemen.getKids();
                        boolean found = false;
                        for (Object kid : kids)
                        {
                            if (kid instanceof Integer && ((Integer) kid) == mcid)
                            {
                                found = true;
                                break;
                            }
                            if (kid instanceof PDMarkedContentReference)
                            {
                                PDMarkedContentReference mcr = (PDMarkedContentReference) kid;
                                if (mcid == mcr.getMCID())
                                {
                                    found = true;
                                    if (mcr.getPage() != null)
                                    {
                                        assertEquals(page, mcr.getPage());
                                    }
                                    else
                                    {
                                        assertEquals(page, structureElemen.getPage());
                                    }
                                    break;
                                }
                            }
                        }
                        assertTrue("page: " + pageNum + ", mcid: " + mcid +
                                " not found in /ParentTree entry " + array.get(mcid) +
                                " for number " + page.getStructParents(), found);
                    }
                }
                // actual count may be larger if last element is null, e.g. PDFBOX-4408
                // set can be empty, see last page of pdf_32000_2008.pdf
                assertTrue(set.isEmpty() || set.last() <= array.size() - 1);
            }
            for (PDAnnotation ann : page.getAnnotations())
            {
                int structParent = ann.getStructParent();
                if (structParent >= 0)
                {
                    assertTrue("/StructParent " + structParent + " missing in /ParentTree", keySet.contains(structParent));

                    // but is it the SAME? Example of a fail:
                    // https://github.com/veraPDF/veraPDF-library/issues/1630
                    PDParentTreeValue obj = (PDParentTreeValue) numberTreeAsMap.get(structParent);
                    PDStructureElement structureElement = new PDStructureElement((COSDictionary) obj.getCOSObject());
                    // search kids for OBJR entry
                    boolean found = false;
                    for (Object kid : structureElement.getKids())
                    {
                        if (kid instanceof PDObjectReference)
                        {
                            PDObjectReference objRef = (PDObjectReference) kid;
                            assertEquals(String.format("/StructureTree entry %d for page %d doesn't have the expected annotation", structParent, pageNum),
                                    ann, objRef.getReferencedObject());
                            if (objRef.getPage() != null)
                            {
                                assertEquals(String.format("/StructureTree entry %d for page %d doesn't have the expected page", structParent, pageNum),
                                        page, objRef.getPage());
                            }
                            found = true; // don't break, there might be another (wrong) OBJR entry
                        }
                    }
                    assertTrue(String.format("/StructureTree entry %d for page %d doesn't have expected page and annotation", structParent, pageNum),
                            found);
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
    public void testFileDeletion() throws IOException
    {
        File outFile = new File(TARGETTESTDIR, "PDFBOX-4383-result.pdf");

        File inFile1 = new File(TARGETTESTDIR, "PDFBOX-4383-src1.pdf");
        File inFile2 = new File(TARGETTESTDIR, "PDFBOX-4383-src2.pdf");

        createSimpleFile(inFile1);
        createSimpleFile(inFile2);
        
        // Unrelated: increase test coverage by testing inputStream
        InputStream is1 = new FileInputStream(inFile1);
        InputStream is2 = new FileInputStream(inFile1);

        OutputStream out = new FileOutputStream(outFile);
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationStream(out);
        assertEquals(out, merger.getDestinationStream());
        merger.addSource(is1);
        merger.addSource(is2);
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        is1.close();
        is2.close();
        out.close();

        PDDocument doc = PDDocument.load(outFile);
        assertEquals(2, doc.getNumberOfPages());
        doc.close();

        assertTrue(inFile1.delete());
        assertTrue(inFile2.delete());
        assertTrue(outFile.delete());
    }


    /**
     * Check that there is a top level Document and Parts below in a merge of 2 documents.
     *
     * @throws IOException
     */
    public void testPDFBox5198_2() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFA3A-merged2.pdf");
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        checkParts(new File(TARGETTESTDIR + "PDFA3A-merged2.pdf"));
    }
    
    /**
     * Check that there is a top level Document and Parts below in a merge of 3 documents.
     *
     * @throws IOException
     */
    public void testPDFBox5198_3() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.addSource(new File(SRCDIR, "PDFA3A.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFA3A-merged3.pdf");
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        checkParts(new File(TARGETTESTDIR + "PDFA3A-merged3.pdf"));
    }

    /**
     * Check that there is a top level Document and Parts below.
     * @param file
     * @throws IOException 
     */
    private void checkParts(File file) throws IOException
    {
        PDDocument doc = PDDocument.load(file);
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
        doc.close();
    }

    static void checkForPageOrphans(PDDocument doc) throws IOException
    {
        // check for orphan pages in the StructTreeRoot/K, StructTreeRoot/ParentTree and
        // StructTreeRoot/IDTree trees.
        PDPageTree pageTree = doc.getPages();
        PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
        PDNameTreeNode<PDStructureElement> idTree = structureTreeRoot.getIDTree();
        Map<String, PDStructureElement> idTreeMap = Collections.emptyMap();
        if (idTree != null)
        {
            idTreeMap = idTree.getTreeAsMap();
        }
        checkElement(pageTree, structureTreeRoot.getParentTree().getCOSObject(), structureTreeRoot.getCOSObject(), idTreeMap);
        assertNotNull(structureTreeRoot.getK());
        checkElement(pageTree, structureTreeRoot.getK(), structureTreeRoot.getCOSObject(), idTreeMap);
        checkForIDTreeOrphans(pageTree, idTreeMap);
        checkParentTreeAgainstK(structureTreeRoot);
    }

    static private void checkParentTreeAgainstK(PDStructureTreeRoot structureTreeRoot) throws IOException
    {
        // check that elements in the /ParentTree are in the /K tree
        ElementCounter elementCounter = new ElementCounter();
        elementCounter.walk(structureTreeRoot.getK());
        Map<Integer, COSObjectable> numberTreeAsMap = structureTreeRoot.getParentTree().getTreeAsMap();
        for (Map.Entry<Integer, COSObjectable> entry : numberTreeAsMap.entrySet())
        {
            PDParentTreeValue val = (PDParentTreeValue) entry.getValue(); // array or dictionary
            COSBase base = val.getCOSObject();
            if (base instanceof COSArray)
            {
                COSArray array = (COSArray) base;
                for (int i = 0; i < array.size(); ++i)
                {
                    COSBase arrayElement = array.getObject(i);
                    if (arrayElement instanceof COSDictionary)
                    {
                        assertTrue("Element " + entry.getKey() + ":" + i + " from /ParentTree missing in /K ",
                                elementCounter.set.contains(arrayElement));
                    }
                }
            }
            // can't check this COSDictionary; ElementsCounter only counts those with a /Pg entry
        }
    }

    static private void checkForIDTreeOrphans(PDPageTree pageTree, Map<String, PDStructureElement> idTreeMap)
            throws IOException
    {
        for (PDStructureElement element : idTreeMap.values())
        {
            if (element.getPage() != null)
            {
                checkForPage(pageTree, element);
            }
            if (!element.getKids().isEmpty())
            {
                checkElement(pageTree, element.getCOSObject().getDictionaryObject(COSName.K), element.getCOSObject(), idTreeMap);
            }
        }
    }

    private void createSimpleFile(File file) throws IOException
    {
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.save(file);
        doc.close();
    }

    static private class ElementCounter
    {
        int cnt = 0;
        Set<COSBase> set = new HashSet<COSBase>();

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
                else if (kdict.containsKey(COSName.K))
                {
                    // at least 1 kid with dict with /Pg, /MCID and type /MCR
                    // happens with confidential file from PDFBOX-6009
                    COSArray kidArray = kdict.getCOSArray(COSName.K);
                    if (kidArray != null)
                    {
                        for (int i = 0; i < kidArray.size(); ++i)
                        {
                            COSBase base2 = kidArray.getObject(i);
                            if (base2 instanceof COSDictionary &&
                                    ((COSDictionary) base2).containsKey(COSName.PG) &&
                                    ((COSDictionary) base2).containsKey(COSName.MCID))
                            {
                                ++cnt;
                                set.add(kdict);
                                break;
                            }
                        }
                    }
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
    static private void checkElement(PDPageTree pageTree, COSBase base, COSDictionary parentDict,
                               Map<String, PDStructureElement> idTreeMap) throws IOException
    {
        if (base instanceof COSArray)
        {
            for (COSBase base2 : (COSArray) base)
            {
                if (base2 instanceof COSObject)
                {
                    base2 = ((COSObject) base2).getObject();
                }
                checkElement(pageTree, base2, parentDict, idTreeMap);
            }
        }
        else if (base instanceof COSDictionary)
        {
            COSDictionary kdict = (COSDictionary) base;
            if (kdict.containsKey(COSName.ID))
            {
                // PDFBOX-6273: check that /ID is in the /IDTree
                PDStructureElement structureElement = new PDStructureElement(kdict);
                String elementIdentifier = structureElement.getElementIdentifier();
                if (elementIdentifier != null)
                {
                    assertTrue(idTreeMap.containsKey(elementIdentifier));
                }
            }
            if (kdict.containsKey(COSName.PG))
            {
                PDStructureElement structureElement = new PDStructureElement(kdict);
                checkForPage(pageTree, structureElement);
            }
            if (kdict.containsKey(COSName.K))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.K), kdict, idTreeMap);
                
                // Check that the /P entry points to the correct object
                PDStructureNode node = PDStructureNode.create(kdict);
                for (Object obj : node.getKids())
                {
                    if (obj instanceof PDStructureElement)
                    {
                        PDStructureNode parent = ((PDStructureElement) obj).getParent();
                        assertTrue(parent.getCOSObject() == kdict);
                    }
                }
                return;
            }

            // if we're in a number tree, check /Nums and /Kids
            if (kdict.containsKey(COSName.KIDS))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.KIDS), kdict, idTreeMap);
            }
            else if (kdict.containsKey(COSName.NUMS))
            {
                checkElement(pageTree, kdict.getDictionaryObject(COSName.NUMS), kdict, idTreeMap);
            }

            if (COSName.OBJR.equals(kdict.getDictionaryObject(COSName.TYPE)) ||
                COSName.MCR.equals(kdict.getDictionaryObject(COSName.TYPE)))
            {
                assertFalse(kdict.getCOSDictionary(COSName.PG) == null && parentDict.getCOSDictionary(COSName.PG) == null);
            }

            // if we're an object reference dictionary (/OBJR), check the obj
            if (kdict.containsKey(COSName.OBJ))
            {
                COSDictionary obj = (COSDictionary) kdict.getDictionaryObject(COSName.OBJ);
                COSBase type = obj.getDictionaryObject(COSName.TYPE);
                COSBase subtype = obj.getDictionaryObject(COSName.SUBTYPE);
                if (COSName.ANNOT.equals(type) || COSName.LINK.equals(subtype))
                {
                    PDAnnotation annotation = PDAnnotation.createAnnotation(obj);
                    PDPage page = annotation.getPage();
                    if (annotation instanceof PDAnnotationLink)
                    {
                        // PDFBOX-5928: check whether the destination of a link annotation is an orphan
                        PDAnnotationLink link = (PDAnnotationLink) annotation;
                        PDDestination destination = link.getDestination();
                        if (destination == null)
                        {
                            PDAction action = link.getAction();
                            if (action instanceof PDActionGoTo)
                            {
                                PDActionGoTo goToAction = (PDActionGoTo) action;
                                destination = goToAction.getDestination();
                            }
                        }
                        if (destination instanceof PDPageDestination)
                        {
                            PDPageDestination pageDestination = (PDPageDestination) destination;
                            PDPage destPage = pageDestination.getPage();
                            if (destPage != null)
                            {
                                Assert.assertNotEquals("Annotation destination page is not in the page tree: " + destPage, 
                                        -1, pageTree.indexOf(destPage));
                            }
                        }
                    }
                    if (page != null && pageTree.indexOf(page) == -1)
                    {
                        COSBase item = kdict.getItem(COSName.OBJ);
                        if (item instanceof COSObject)
                        {
                            Assert.assertNotEquals("Annotation page is not in the page tree: " + item, -1, pageTree.indexOf(page));
                        }
                        else
                        {
                            // don't display because of stack overflow
                            Assert.assertNotEquals("Annotation page is not in the page tree", -1, pageTree.indexOf(page));
                        }
                    }
                }
                else
                {
                    //TODO needs to be investigated. Specification mentions
                    // "such as an XObject or an annotation"
                    fail("Other type: " + type + ", obj: " + obj);
                }
            }
        }
    }

    // checks that the result file of a merge has the same rendering as the two
    // source files
    private void checkMergeIdentical(String filename1, String filename2, String mergeFilename, 
            MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        PDDocument srcDoc1 = PDDocument.load(new File(SRCDIR, filename1), (String)null);
        int src1PageCount = srcDoc1.getNumberOfPages();
        PDFRenderer src1PdfRenderer = new PDFRenderer(srcDoc1);
        BufferedImage[] src1ImageTab = new BufferedImage[src1PageCount];
        for (int page = 0; page < src1PageCount; ++page)
        {
            src1ImageTab[page] = src1PdfRenderer.renderImageWithDPI(page, DPI);
        }
        srcDoc1.close();

        PDDocument srcDoc2 = PDDocument.load(new File(SRCDIR, filename2), (String)null);
        int src2PageCount = srcDoc2.getNumberOfPages();
        PDFRenderer src2PdfRenderer = new PDFRenderer(srcDoc2);
        BufferedImage[] src2ImageTab = new BufferedImage[src2PageCount];
        for (int page = 0; page < src2PageCount; ++page)
        {
            src2ImageTab[page] = src2PdfRenderer.renderImageWithDPI(page, DPI);
        }
        srcDoc2.close();

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, filename1));
        pdfMergerUtility.addSource(new File(SRCDIR, filename2));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + mergeFilename);
        pdfMergerUtility.mergeDocuments(memUsageSetting);

        PDDocument mergedDoc
                = PDDocument.load(new File(TARGETTESTDIR, mergeFilename), (String)null);
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
        mergedDoc.close();
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

    static private void checkForPage(PDPageTree pageTree, PDStructureElement structureElement)
    {
        PDPage page = structureElement.getPage();
        if (page != null)
        {
            Assert.assertNotEquals("Page is not in the page tree", -1, pageTree.indexOf(page));
        }
    }

    /**
     * PDFBOX-5939: merge a file with an outline that has itself as a parent without producing a
     * stack overflow.
     *
     * @throws IOException 
     */
    public void testOutlinesSelfParent() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(TARGETPDFDIR, "PDFBOX-5939-google-docs-1.pdf"));
        pdfMergerUtility.addSource(new File(TARGETPDFDIR, "PDFBOX-5939-google-docs-1.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFBOX-5939-google-docs-result.pdf");
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        PDDocument mergedDoc =
                PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-5939-google-docs-result.pdf"));
        assertEquals(2, mergedDoc.getNumberOfPages());
        mergedDoc.close();
    }

    /**
     * PDFBOX-515 / PDFBOX-5950: test merging of two files where one file has a stream deep down in
     * the info dictionary (Info/ImPDF/Images/Kids/[0]). This test will pass only if the source file
     * isn't closed prematurely, or if deep cloning is applied.
     *
     * @throws IOException
     */
    public void testPDFBox515() throws IOException
    {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(TARGETPDFDIR, "ComSquare1.pdf"));
        pdfMergerUtility.addSource(new File(TARGETPDFDIR, "Ghostscript1.pdf"));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "PDFBOX-515-result.pdf");
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        PDDocument mergedDoc =
                PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-515-result.pdf"));
        assertEquals(2, mergedDoc.getNumberOfPages());
        COSDictionary imageDict = (COSDictionary) mergedDoc.getDocumentInformation().getCOSObject().
                getCOSDictionary(COSName.getPDFName("ImPDF")).
                getCOSDictionary(COSName.getPDFName("Images")).
                getCOSArray(COSName.KIDS).getObject(0);
        PDImageXObject imageXObject = (PDImageXObject) PDImageXObject.createXObject(imageDict, new PDResources());
        BufferedImage bim = imageXObject.getImage();
        assertEquals(909, bim.getWidth());
        assertEquals(233, bim.getHeight());
        mergedDoc.close();
    }
}