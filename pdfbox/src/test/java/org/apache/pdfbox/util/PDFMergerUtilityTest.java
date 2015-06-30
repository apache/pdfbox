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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

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
     * second file. Revisions before 1617990 fail this test because global
     * resources were merged which made trouble when resources of the same kind
     * had the same name.
     *
     * @throws IOException if something goes wrong.
     * @throws COSVisitorException if something goes wrong when visiting a COS
     * object.
     */
    public void testPDFMergerUtility() throws IOException, COSVisitorException
    {
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult.pdf", null);
        
        // once again, with scratch file
        File scratchFile = new File(TARGETTESTDIR, "mergeDocumentsNonSeqScratch.bin");
        RandomAccessFile randomAccessFile = new RandomAccessFile(scratchFile, "rw");
        checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
                "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf",
                "GlobalResourceMergeTestResult2.pdf", randomAccessFile);
        scratchFile.delete();
    }

    // checks that the result file of a merge has the same rendering as the two
    // source files
    private void checkMergeIdentical(String filename1, String filename2, String mergeFilename, 
            RandomAccessFile scratchFile)
            throws IOException, COSVisitorException
    {
        PDDocument srcDoc1 = PDDocument.loadNonSeq(new File(SRCDIR, filename1), null);
        int src1PageCount = srcDoc1.getNumberOfPages();
        BufferedImage[] src1ImageTab = new BufferedImage[src1PageCount];
        List<PDPage> pageList1 = srcDoc1.getDocumentCatalog().getAllPages();
        for (int page = 0; page < src1PageCount; ++page)
        {
            src1ImageTab[page] = 
                    pageList1.get(page).convertToImage(BufferedImage.TYPE_INT_RGB, DPI);
        }
        srcDoc1.close();

        PDDocument srcDoc2 = PDDocument.loadNonSeq(new File(SRCDIR, filename2), null);
        int src2PageCount = srcDoc2.getNumberOfPages();
        BufferedImage[] src2ImageTab = new BufferedImage[src2PageCount];
        List<PDPage> pageList2 = srcDoc2.getDocumentCatalog().getAllPages();
        for (int page = 0; page < src2PageCount; ++page)
        {
            src2ImageTab[page] = 
                    pageList2.get(page).convertToImage(BufferedImage.TYPE_INT_RGB, DPI);
        }
        srcDoc2.close();

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(new File(SRCDIR, filename1));
        pdfMergerUtility.addSource(new File(SRCDIR, filename2));
        pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + mergeFilename);
        pdfMergerUtility.mergeDocumentsNonSeq(scratchFile);

        PDDocument mergedDoc
                = PDDocument.loadNonSeq(new File(TARGETTESTDIR, mergeFilename), null);
        int mergePageCount = mergedDoc.getNumberOfPages();
        assertEquals(src1PageCount + src2PageCount, mergePageCount);
        List<PDPage> pageListMerged = mergedDoc.getDocumentCatalog().getAllPages();
        for (int page = 0; page < src1PageCount; ++page)
        {
            BufferedImage bim = 
                    pageListMerged.get(page).convertToImage(BufferedImage.TYPE_INT_RGB, DPI);
            checkImagesIdentical(bim, src1ImageTab[page]);
        }
        for (int page = 0; page < src2PageCount; ++page)
        {
            int mergePage = page + src1PageCount;
            BufferedImage bim = 
                    pageListMerged.get(mergePage).convertToImage(BufferedImage.TYPE_INT_RGB, DPI);
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

}
