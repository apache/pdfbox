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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDestinationDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test merging different PDFs with Annotations.
 */
class MergeAnnotationsTest
{
    private static final File OUT_DIR = new File("target/test-output/merge/");
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    @BeforeEach
    public void setUp()
    {
        OUT_DIR.mkdirs();
    }
    
    /*
     * PDFBOX-1065 Ensure that after merging the PDFs there are all link
     * annotations and they point to the correct page.
     */
    @Test
    void testLinkAnnotations() throws IOException
    {
        // Merge the PDFs from PDFBOX-1065
        PDFMergerUtility merger = new PDFMergerUtility();
        File file1 = new File(TARGET_PDF_DIR, "PDFBOX-1065-1.pdf");
        File file2 = new File(TARGET_PDF_DIR, "PDFBOX-1065-2.pdf");

        try (InputStream is1 = new FileInputStream(file1);
             InputStream is2 = new FileInputStream(file2))
        {
            File pdfOutput = new File(OUT_DIR, "PDFBOX-1065.pdf");
            merger.setDestinationFileName(pdfOutput.getAbsolutePath());
            merger.addSource(is1);
            merger.addSource(is2);
            merger.mergeDocuments(null);

            // Test merge result
            try (PDDocument mergedPDF = Loader.loadPDF(pdfOutput))
            {
                assertEquals(6, mergedPDF.getNumberOfPages(), "There shall be 6 pages");
                
                PDDocumentNameDestinationDictionary destinations = mergedPDF.getDocumentCatalog().getDests();
                
                // Each document has 3 annotations with 2 entries in the /Dests dictionary per annotation. One for the
                // source and one for the target.
                assertEquals(12, destinations.getCOSObject().entrySet().size(),
                        "There shall be 12 entries");
                
                List<PDAnnotation> sourceAnnotations01 = mergedPDF.getPage(0).getAnnotations();
                List<PDAnnotation> sourceAnnotations02 = mergedPDF.getPage(3).getAnnotations();
                
                List<PDAnnotation> targetAnnotations01 = mergedPDF.getPage(2).getAnnotations();
                List<PDAnnotation> targetAnnotations02 = mergedPDF.getPage(5).getAnnotations();
                
                // Test for the first set of annotations to be merged an linked correctly
                assertEquals(3, sourceAnnotations01.size(),
                        "There shall be 3 source annotations at the first page");
                assertEquals(3, targetAnnotations01.size(),
                        "There shall be 3 source annotations at the third page");
                assertTrue(testAnnotationsMatch(sourceAnnotations01, targetAnnotations01),
                        "The annotations shall match to each other");
                
                // Test for the second set of annotations to be merged an linked correctly
                assertEquals(3, sourceAnnotations02.size(),
                        "There shall be 3 source annotations at the first page");
                assertEquals(3, targetAnnotations02.size(),
                        "There shall be 3 source annotations at the third page");
                assertTrue(testAnnotationsMatch(sourceAnnotations02, targetAnnotations02),
                        "The annotations shall match to each other");
            }
        }
    }    

    /*
     * Source and target annotations are línked by name with the target annotation's name
     * being the source annotation's name prepended with 'annoRef_'
     */
    private boolean testAnnotationsMatch(List<PDAnnotation> sourceAnnots, List<PDAnnotation> targetAnnots)
    {
        Map<String, PDAnnotation> targetAnnotsByName = new HashMap<>();
        COSName destinationName;
        
        // fill the map with the annotations destination name
        for (PDAnnotation targetAnnot : targetAnnots)
        {
            destinationName = (COSName) targetAnnot.getCOSObject().getDictionaryObject(COSName.DEST);
            targetAnnotsByName.put(destinationName.getName(), targetAnnot);
        }
        
        // try to lookup the target annotation for the source annotation by destination name
        for (PDAnnotation sourceAnnot : sourceAnnots)
        {
            destinationName = (COSName) sourceAnnot.getCOSObject().getDictionaryObject(COSName.DEST);
            if (targetAnnotsByName.get("annoRef_" + destinationName.getName()) == null)
            {
                return false;
            }
        }
        return true;
    }
}
