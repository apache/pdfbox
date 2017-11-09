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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.rendering.PDFRenderer;

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
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        try (PDDocument mergedDoc = PDDocument.load(new File(TARGETTESTDIR, "MergerOpenActionTestResult.pdf")))
        {
            PDDocumentCatalog documentCatalog = mergedDoc.getDocumentCatalog();
            dest = (PDPageDestination) documentCatalog.getOpenAction();
            assertEquals(4, documentCatalog.getPages().indexOf(dest.getPage()));
        }
    }

    /**
     * PDFBOX-3999: check that entries in the number tree only reference pages from the page tree.
     * 
     * @throws IOException 
     */
    public void testStructureTreeMerge() throws IOException
    {
        InputStream is;
        try
        {
            System.out.println("Downloading GeneralForbearance.pdf...");
            is = new URL("https://issues.apache.org/jira/secure/attachment/12896905/GeneralForbearance.pdf").openStream();
            FileOutputStream fos = new FileOutputStream(new File("target", "GeneralForbearance.pdf"));
            IOUtils.copy(is, fos);
            is.close();
            fos.close();
            System.out.println("Download finished!");
        }
        catch (IOException ex)
        {
            System.err.println("GeneralForbearance.pdf could not be downloaded, test skipped");
            return;
        }
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        PDDocument src = PDDocument.load(new File("target", "GeneralForbearance.pdf"));
        PDDocument dst = PDDocument.load(new File("target", "GeneralForbearance.pdf"));
        pdfMergerUtility.appendDocument(dst, src);
        src.close(); //if we don't close the src then we don't have an error
        dst.save(new File("target", "GovFormPreFlattened-merged.pdf"));
        dst.close();

        PDDocument doc = PDDocument.load(new File("target", "GovFormPreFlattened-merged.pdf"));
        PDPageTree pageTree = doc.getPages();
        PDNumberTreeNode parentTree = doc.getDocumentCatalog().getStructureTreeRoot().getParentTree();
        COSArray numArray = (COSArray) parentTree.getCOSObject().getDictionaryObject(COSName.NUMS);
        for (COSBase base : numArray)
        {
            if (base instanceof COSObject)
            {
                base = ((COSObject) base).getObject();
            }
            if (base instanceof COSArray)
            {
                for (COSBase base2 : (COSArray) base)
                {
                    if (base2 instanceof COSObject)
                    {
                        base2 = ((COSObject) base2).getObject();
                    }
                    checkForPage(pageTree, base2);
                }
            }
            else if (base instanceof COSDictionary)
            {
                checkForPage(pageTree, base);
            }
        }
    }

    // checks that the result file of a merge has the same rendering as the two source files
    private void checkMergeIdentical(String filename1, String filename2, String mergeFilename, 
            MemoryUsageSetting memUsageSetting)
            throws IOException
    {
        int src1PageCount;
        BufferedImage[] src1ImageTab;
        try (PDDocument srcDoc1 = PDDocument.load(new File(SRCDIR, filename1), (String) null))
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
        try (PDDocument srcDoc2 = PDDocument.load(new File(SRCDIR, filename2), (String) null))
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
        pdfMergerUtility.mergeDocuments(memUsageSetting);

        try (PDDocument mergedDoc = PDDocument.load(new File(TARGETTESTDIR, mergeFilename), (String) null))
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

    private void checkForPage(PDPageTree pageTree, COSBase base2)
    {
        COSDictionary dict = (COSDictionary) base2;
        if (dict.containsKey(COSName.PG))
        {
            PDPage page = new PDPage((COSDictionary) dict.getDictionaryObject(COSName.PG));
            assertTrue("Page is not in the page tree", pageTree.indexOf(page) != -1);
        }
    }
}
