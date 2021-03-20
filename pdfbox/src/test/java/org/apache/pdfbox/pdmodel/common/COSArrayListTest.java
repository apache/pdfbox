/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.pdfbox.pdmodel.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class COSArrayListTest
{
    // next two entries are to be used for comparison with
    // COSArrayList behaviour in order to ensure that the
    // intended object is now at the correct position.
    // Will also be used for Collection/Array based setting
    // and comparison
    static List<PDAnnotation> tbcAnnotationsList;
    static COSBase[] tbcAnnotationsArray;

    // next entries are to be used within COSArrayList
    static List<PDAnnotation> annotationsList;
    static COSArray annotationsArray;

    // to be used when testing retrieving filtered items as can be done with
    // {@link PDPage.getAnnotations(AnnotationFilter annotationFilter)}
    static PDPage pdPage;

    private static final File OUT_DIR = new File("target/test-output/pdmodel/common");


    /*
     * Create three new different annotations and add them to the Java List/Array as
     * well as PDFBox List/Array implementations.
     */
    @BeforeEach
    public void setUp() throws Exception {
        annotationsList = new ArrayList<>();
        PDAnnotationHighlight txtMark = new PDAnnotationHighlight();
        PDAnnotationLink txtLink = new PDAnnotationLink();
        PDAnnotationCircle aCircle = new PDAnnotationCircle();

        annotationsList.add(txtMark);
        annotationsList.add(txtLink);
        annotationsList.add(aCircle);
        annotationsList.add(txtLink);
        assertEquals(4, annotationsList.size());

        tbcAnnotationsList = new ArrayList<>();
        tbcAnnotationsList.add(txtMark);
        tbcAnnotationsList.add(txtLink);
        tbcAnnotationsList.add(aCircle);
        tbcAnnotationsList.add(txtLink);
        assertEquals(4, tbcAnnotationsList.size());

        annotationsArray = new COSArray();
        annotationsArray.add(txtMark);
        annotationsArray.add(txtLink);
        annotationsArray.add(aCircle);
        annotationsArray.add(txtLink);
        assertEquals(4, annotationsArray.size());

        tbcAnnotationsArray = new COSBase[4];
        tbcAnnotationsArray[0] = txtMark.getCOSObject();
        tbcAnnotationsArray[1] = txtLink.getCOSObject();
        tbcAnnotationsArray[2] = aCircle.getCOSObject();
        tbcAnnotationsArray[3] = txtLink.getCOSObject();
        assertEquals(4, tbcAnnotationsArray.length);

        // add the annotations to the page
        pdPage = new PDPage();
        pdPage.setAnnotations(annotationsList);

        // create test output directory
        OUT_DIR.mkdirs();
    }

    /**
     * Test getting a PDModel element is in sync with underlying COSArray
     */
    @Test
    void getFromList() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        for (int i = 0; i < cosArrayList.size(); i++) {
            PDAnnotation annot = cosArrayList.get(i);
            assertEquals(annotationsArray.get(i), annot.getCOSObject(),
                    "PDAnnotations cosObject at " + i + " shall be equal to index " + i
                            + " of COSArray");

            // compare with Java List/Array
            assertEquals(tbcAnnotationsList.get(i), annot,
                    "PDAnnotations at " + i + " shall be at index " + i + " of List");
            assertEquals(tbcAnnotationsArray[i], annot.getCOSObject(),
                    "PDAnnotations cosObject at " + i + " shall be at position " + i + " of Array");
        }
    }

    /**
     * Test adding a PDModel element is in sync with underlying COSArray
     */
    // @Test
    public void addToList() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        // add new annotation
        PDAnnotationSquare aSquare = new PDAnnotationSquare();
        cosArrayList.add(aSquare);

        assertEquals(5, annotationsList.size(), "List size shall be 5");
        assertEquals(5, annotationsArray.size(), "COSArray size shall be 5");

        PDAnnotation annot = annotationsList.get(4);
        assertEquals(4, annotationsArray.indexOf(annot.getCOSObject()),
                "Added annotation shall be 4th entry in COSArray");
        assertEquals(annotationsArray, cosArrayList.toList(),
                "Provided COSArray and underlying COSArray shall be equal");
    }

    /**
     * Test removing a PDModel element by index is in sync with underlying COSArray
     */
    @Test
    void removeFromListByIndex() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        assertEquals(toBeRemoved, cosArrayList.remove(positionToRemove),
                "Remove operation shall return the removed object");
        assertEquals(3, cosArrayList.size(), "List size shall be 3");
        assertEquals(3, annotationsArray.size(), "COSArray size shall be 3");

        assertEquals(-1, cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)),
                "PDAnnotation shall no longer exist in List");
        assertEquals(-1, annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]),
                "COSObject shall no longer exist in COSArray");
    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying
     * COSArray
     */
    @Test
    void removeUniqueFromListByObject() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        assertTrue(cosArrayList.remove(toBeRemoved), "Remove operation shall return true");
        assertEquals(3, cosArrayList.size(), "List size shall be 3");
        assertEquals(3, annotationsArray.size(), "COSArray size shall be 3");

        // compare with Java List/Array to ensure correct object at position
        assertEquals(cosArrayList.get(2), tbcAnnotationsList.get(3),
                "List object at 3 is at position 2 in COSArrayList now");
        assertEquals(annotationsArray.get(2), tbcAnnotationsList.get(3).getCOSObject(),
                "COSObject of List object at 3 is at position 2 in COSArray now");
        assertEquals(annotationsArray.get(2), tbcAnnotationsArray[3],
                "Array object at 3 is at position 2 in underlying COSArray now");

        assertEquals(-1, cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)),
                "PDAnnotation shall no longer exist in List");
        assertEquals(-1, annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]),
                "COSObject shall no longer exist in COSArray");

        assertFalse(cosArrayList.remove(toBeRemoved), "Remove shall not remove any object");

    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying
     * COSArray
     */
    @Test
    void removeAllUniqueFromListByObject() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue(cosArrayList.removeAll(toBeRemovedInstances),
                "Remove operation shall return true");
        assertEquals(3, cosArrayList.size(), "List size shall be 3");
        assertEquals(3, annotationsArray.size(), "COSArray size shall be 3");

        assertFalse(cosArrayList.removeAll(toBeRemovedInstances),
                "Remove shall not remove any object");
    }

    /**
     * Test removing a multiple appearing PDModel element by index is in sync with
     * underlying COSArray
     */
    @Test
    void removeMultipleFromListByObject() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = tbcAnnotationsList.get(positionToRemove);

        assertTrue(cosArrayList.remove(toBeRemoved), "Remove operation shall return true");
        assertEquals(3, cosArrayList.size(), "List size shall be 3");
        assertEquals(3, annotationsArray.size(), "COSArray size shall be 3");

        assertTrue(cosArrayList.remove(toBeRemoved), "Remove operation shall return true");
        assertEquals(2, cosArrayList.size(), "List size shall be 2");
        assertEquals(2, annotationsArray.size(), "COSArray size shall be 2");
    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying
     * COSArray
     */
    @Test
    void removeAllMultipleFromListByObject() throws Exception
    {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue(cosArrayList.removeAll(toBeRemovedInstances),
                "Remove operation shall return true");
        assertEquals(2, cosArrayList.size(), "List size shall be 2");
        assertEquals(2, annotationsArray.size(), "COSArray size shall be 2");

        assertFalse(cosArrayList.removeAll(toBeRemovedInstances),
                "Remove shall not remove any object");
    }

    @Test
    void removeFromFilteredListByIndex() throws Exception
    {
        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = annotation -> !(annotation instanceof PDAnnotationLink);

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // this call should fail
        assertThrows(UnsupportedOperationException.class, () -> cosArrayList.remove(1));
    }

    @Test
    void removeFromFilteredListByObject() throws Exception
    {
        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = annotation -> !(annotation instanceof PDAnnotationLink);

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // remove object
        int positionToRemove = 1;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        // this call should fail
        assertThrows(UnsupportedOperationException.class, () -> cosArrayList.remove(toBeRemoved));
    }

    @Test
    void removeSingleDirectObject() throws IOException
    {

        // generate test file
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);

            ArrayList<PDAnnotation> pageAnnots = new ArrayList<>();
            PDAnnotationHighlight txtMark = new PDAnnotationHighlight();
            PDAnnotationLink txtLink = new PDAnnotationLink();

            // enforce the COSDictionaries to be written directly into the COSArray
            txtMark.getCOSObject().getCOSObject().setDirect(true);
            txtLink.getCOSObject().getCOSObject().setDirect(true);

            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtLink);
            assertEquals(4, pageAnnots.size(), "There shall be 4 annotations generated");

            page.setAnnotations(pageAnnots);

            pdf.save(OUT_DIR + "/removeSingleDirectObjectTest.pdf");
        }

        try (PDDocument pdf = Loader.loadPDF(new File(OUT_DIR + "/removeSingleDirectObjectTest.pdf"))) {
            PDPage page = pdf.getPage(0);
        
            COSArrayList<PDAnnotation> annotations = (COSArrayList<PDAnnotation>) page.getAnnotations();

            assertEquals(4, annotations.size(), "There shall be 4 annotations retrieved");
            assertEquals(4, annotations.toList().size(),
                    "The size of the internal COSArray shall be 4");

            PDAnnotation toBeRemoved = annotations.get(0);
            annotations.remove(toBeRemoved);

            assertEquals(3, annotations.size(), "There shall be 3 annotations left");
            assertEquals(3, annotations.toList().size(),
                    "The size of the internal COSArray shall be 3");
        }
    }

    @Test
    void removeSingleIndirectObject() throws IOException
    {

        // generate test file
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);

            ArrayList<PDAnnotation> pageAnnots = new ArrayList<>();
            PDAnnotationHighlight txtMark = new PDAnnotationHighlight();
            PDAnnotationLink txtLink = new PDAnnotationLink();

            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtLink);
            assertEquals(4, pageAnnots.size(), "There shall be 4 annotations generated");

            page.setAnnotations(pageAnnots);

            pdf.save(OUT_DIR + "/removeSingleIndirectObjectTest.pdf");
        }

        try (PDDocument pdf = Loader.loadPDF(new File(OUT_DIR + "/removeSingleIndirectObjectTest.pdf"))) {
            PDPage page = pdf.getPage(0);
        
            COSArrayList<PDAnnotation> annotations = (COSArrayList<PDAnnotation>) page.getAnnotations();

            assertEquals(4, annotations.size(), "There shall be 4 annotations retrieved");
            assertEquals(4, annotations.toList().size(),
                    "The size of the internal COSArray shall be 4");

            PDAnnotation toBeRemoved = annotations.get(0);

            annotations.remove(toBeRemoved);

            assertEquals(3, annotations.size(), "There shall be 3 annotations left");
            assertEquals(3, annotations.toList().size(),
                    "The size of the internal COSArray shall be 2");
        }
    }

    @Test
    void retainIndirectObject() throws IOException
    {

        // generate test file
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);

            ArrayList<PDAnnotation> pageAnnots = new ArrayList<>();
            PDAnnotationHighlight txtMark = new PDAnnotationHighlight();
            PDAnnotationLink txtLink = new PDAnnotationLink();

            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtMark);
            pageAnnots.add(txtLink);
            assertEquals(4, pageAnnots.size(), "There shall be 4 annotations generated");

            page.setAnnotations(pageAnnots);

            pdf.save(OUT_DIR + "/removeIndirectObjectTest.pdf");
        }

        try (PDDocument pdf = Loader.loadPDF(new File(OUT_DIR + "/removeIndirectObjectTest.pdf"))) {
            PDPage page = pdf.getPage(0);
        
            COSArrayList<PDAnnotation> annotations = (COSArrayList<PDAnnotation>) page.getAnnotations();

            assertEquals(4, annotations.size(), "There shall be 4 annotations retrieved");
            assertEquals(4, annotations.toList().size(),
                    "The size of the internal COSArray shall be 4");

            ArrayList<PDAnnotation> toBeRetained = new ArrayList<>();
            toBeRetained.add(annotations.get(0));

            annotations.retainAll(toBeRetained);

            assertEquals(3, annotations.size(), "There shall be 3 annotations left");
            assertEquals(3, annotations.toList().size(),
                    "The size of the internal COSArray shall be 3");
        }
    }
}
