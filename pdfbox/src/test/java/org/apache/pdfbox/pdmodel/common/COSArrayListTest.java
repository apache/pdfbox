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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class COSArrayListTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    @Before
    public void setUp() throws Exception {
        annotationsList = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();
        PDAnnotationSquareCircle aCircle = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_CIRCLE);

        annotationsList.add(txtMark);
        annotationsList.add(txtLink);
        annotationsList.add(aCircle);
        annotationsList.add(txtLink);
        assertTrue(annotationsList.size() == 4);

        tbcAnnotationsList = new ArrayList<PDAnnotation>();
        tbcAnnotationsList.add(txtMark);
        tbcAnnotationsList.add(txtLink);
        tbcAnnotationsList.add(aCircle);
        tbcAnnotationsList.add(txtLink);
        assertTrue(tbcAnnotationsList.size() == 4);

        annotationsArray = new COSArray();
        annotationsArray.add(txtMark);
        annotationsArray.add(txtLink);
        annotationsArray.add(aCircle);
        annotationsArray.add(txtLink);
        assertTrue(annotationsArray.size() == 4);

        tbcAnnotationsArray = new COSBase[4];
        tbcAnnotationsArray[0] = txtMark.getCOSObject();
        tbcAnnotationsArray[1] = txtLink.getCOSObject();
        tbcAnnotationsArray[2] = aCircle.getCOSObject();
        tbcAnnotationsArray[3] = txtLink.getCOSObject();
        assertTrue(tbcAnnotationsArray.length == 4);

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
    public void getFromList() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        for (int i = 0; i < cosArrayList.size(); i++) {
            PDAnnotation annot = (PDAnnotation) cosArrayList.get(i);
            assertTrue("PDAnnotations cosObject at " + i + " shall be equal to index " + i + " of COSArray",
                annotationsArray.get(i).equals(annot.getCOSObject()));

            // compare with Java List/Array
            assertTrue("PDAnnotations at " + i + " shall be at index " + i + " of List",
                tbcAnnotationsList.get(i).equals((annot)));
            assertEquals("PDAnnotations cosObject at " + i + " shall be at position " + i + " of Array",
                tbcAnnotationsArray[i], annot.getCOSObject());
        }
    }

    /**
     * Test adding a PDModel element is in sync with underlying COSArray
     */
    @Test
    public void addToList() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        // add new annotation
        PDAnnotationSquareCircle aSquare = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
        cosArrayList.add(aSquare);

        assertTrue("List size shall be 5", annotationsList.size() == 5);
        assertTrue("COSArray size shall be 5", annotationsArray.size() == 5);

        PDAnnotation annot = (PDAnnotation) annotationsList.get(4);
        assertTrue("Added annotation shall be 4th entry in COSArray", annotationsArray.indexOf(annot.getCOSObject()) == 4);
        assertEquals("Provided COSArray and underlying COSArray shall be equal", annotationsArray, cosArrayList.getCOSArray());
    }

    /**
     * Test removing a PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeFromListByIndex() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        assertEquals("Remove operation shall return the removed object",toBeRemoved, cosArrayList.remove(positionToRemove));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertTrue("PDAnnotation shall no longer exist in List",
            cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
            annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);
    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeUniqueFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        assertTrue("Remove operation shall return true",cosArrayList.remove(toBeRemoved));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        // compare with Java List/Array to ensure correct object at position
        assertTrue("List object at 3 is at position 2 in COSArrayList now",
            cosArrayList.get(2).equals(tbcAnnotationsList.get(3)));
        assertTrue("COSObject of List object at 3 is at position 2 in COSArray now",
            annotationsArray.get(2).equals(tbcAnnotationsList.get(3).getCOSObject()));
        assertTrue("Array object at 3 is at position 2 in underlying COSArray now",
            annotationsArray.get(2).equals(tbcAnnotationsArray[3]));

        assertTrue("PDAnnotation shall no longer exist in List",
            cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
            annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);

        assertFalse("Remove shall not remove any object",cosArrayList.remove(toBeRemoved));

    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeAllUniqueFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue("Remove operation shall return true",cosArrayList.removeAll(toBeRemovedInstances));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertFalse("Remove shall not remove any object",cosArrayList.removeAll(toBeRemovedInstances));
    }


    /**
     * Test removing a multiple appearing PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeMultipleFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = tbcAnnotationsList.get(positionToRemove);

        assertTrue("Remove operation shall return true",cosArrayList.remove(toBeRemoved));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertTrue("Remove operation shall return true",cosArrayList.remove(toBeRemoved));
        assertTrue("List size shall be 2", cosArrayList.size() == 2);
        assertTrue("COSArray size shall be 2", annotationsArray.size() == 2);

    }

        /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeAllMultipleFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue("Remove operation shall return true",cosArrayList.removeAll(toBeRemovedInstances));
        assertTrue("List size shall be 2", cosArrayList.size() == 2);
        assertTrue("COSArray size shall be 2", annotationsArray.size() == 2);

        assertFalse("Remove shall not remove any object",cosArrayList.removeAll(toBeRemovedInstances));
    }

    @Test
    public void removeFromFilteredListByIndex() throws Exception
    {
        // removing from a filtered list is not permitted
        thrown.expect(UnsupportedOperationException.class);

        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = new AnnotationFilter()
        {
            @Override
            public boolean accept(PDAnnotation annotation)
            {
                return !(annotation instanceof PDAnnotationLink);
            }
        };

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // this call should fail
        cosArrayList.remove(1);
    }
    

    @Test
    public void removeFromFilteredListByObject() throws Exception
    {
        // removing from a filtered list is not permitted
        thrown.expect(UnsupportedOperationException.class);
        
        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = new AnnotationFilter()
        {
            @Override
            public boolean accept(PDAnnotation annotation)
            {
                return !(annotation instanceof PDAnnotationLink);
            }
        };

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // remove object
        int positionToRemove = 1;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        // this call should fail
        cosArrayList.remove(toBeRemoved);

    }

    @Test
    public void removeSingleDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();

        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // enforce the COSDictionaries to be written directly into the COSArray
        txtMark.getCOSObject().getCOSObject().setDirect(true);
        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/removeSingleDirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/removeSingleDirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        PDAnnotation toBeRemoved = annotations.get(0);
        annotations.remove(toBeRemoved);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
        pdf.close();
    }

    @Test
    public void removeSingleIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/removeSingleIndirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/removeSingleIndirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        PDAnnotation toBeRemoved = annotations.get(0);

        annotations.remove(toBeRemoved);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 2", annotations.getCOSArray().size() == 3);
        pdf.close();
    }

    // @Test
    // PDFBOX-4669, PDFBOX-4723
    // This test is currently disabled with the removeAll function not properly
    // working. See the discussion in above mentioned tickets about currently not implementing equals which the 
    // functionality would need to rely on.
    public void removeDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // enforce the COSDictionaries to be written directly into the COSArray
        txtMark.getCOSObject().getCOSObject().setDirect(true);
        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/removeDirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/removeDirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRemoved = new ArrayList<PDAnnotation>();

        toBeRemoved.add(annotations.get(0));
        annotations.removeAll(toBeRemoved);

        assertTrue("There shall be 1 annotations left", annotations.size() == 1);
        assertTrue("The size of the internal COSArray shall be 1", annotations.getCOSArray().size() == 1);
    }

    @Test
    public void removeIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/removeIndirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/removeIndirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRemoved = new ArrayList<PDAnnotation>();
        toBeRemoved.add(annotations.get(0));

        annotations.removeAll(toBeRemoved);

        assertTrue("There shall be 1 annotations left", annotations.size() == 1);
        assertTrue("The size of the internal COSArray shall be 1", annotations.getCOSArray().size() == 1);
    }

    // @Test
    // PDFBOX-4669, PDFBOX-4723
    // This test is currently disabled with the retainAll function not properly
    // working. See the discussion in above mentioned tickets about currently not implementing equals which the 
    // functionality would need to rely on.
    public void retainDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // enforce the COSDictionaries to be written directly into the COSArray
        txtMark.getCOSObject().getCOSObject().setDirect(true);
        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/retainDirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/retainDirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRetained = new ArrayList<PDAnnotation>();

        toBeRetained.add(annotations.get(0));
        annotations.retainAll(toBeRetained);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
    }

    // @Test
    // PDFBOX-4669, PDFBOX-4723
    // This test is currently disabled with the retainAll function not properly
    // working. See the discussion in above mentioned tickets about currently not implementing equals which the 
    // functionality would need to rely on.
    public void retainIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // enforce the COSDictionaries to be written directly into the COSArray
        txtMark.getCOSObject().getCOSObject().setDirect(true);
        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.save(OUT_DIR + "/retainIndirectObjectTest.pdf");
        pdf.close();

        pdf = PDDocument.load(new File(OUT_DIR + "/retainIndirectObjectTest.pdf"));
        page = pdf.getPage(0);
        
        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRetained = new ArrayList<PDAnnotation>();

        toBeRetained.add(annotations.get(0));
        annotations.retainAll(toBeRetained);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
    }
}