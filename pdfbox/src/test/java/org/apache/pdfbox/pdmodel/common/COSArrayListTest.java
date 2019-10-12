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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.junit.Before;
import org.junit.Test;

public class COSArrayListTest {
    // next two entries are to be used for comparison with
    // COSArrayList behaviour in order to ensure that the
    // intented object is now at the correct position.
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

    /*
     * Create thre new different annotations an add them to the Java List/Array as
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
        assertTrue(annotationsList.size() == 3);

        tbcAnnotationsList = new ArrayList<PDAnnotation>();
        tbcAnnotationsList.add(txtMark);
        tbcAnnotationsList.add(txtLink);
        tbcAnnotationsList.add(aCircle);
        assertTrue(tbcAnnotationsList.size() == 3);

        annotationsArray = new COSArray();
        annotationsArray.add(txtMark);
        annotationsArray.add(txtLink);
        annotationsArray.add(aCircle);
        assertTrue(annotationsArray.size() == 3);

        tbcAnnotationsArray = new COSBase[3];
        tbcAnnotationsArray[0] = txtMark.getCOSObject();
        tbcAnnotationsArray[1] = txtLink.getCOSObject();
        tbcAnnotationsArray[2] = aCircle.getCOSObject();
        assertTrue(tbcAnnotationsArray.length == 3);

        // add the annotations to the page
        pdPage = new PDPage();
        pdPage.setAnnotations(annotationsList);
    }

    /**
     * Test getting a PDModel element is in sync with underlying COSArray
     */
    @Test
    public void getFromList() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        for (int i = 0; i < 3; i++) {
            PDAnnotation annot = (PDAnnotation) cosArrayList.get(i);
            // compare position using COSArrayList
            assertTrue("PDAnnotations cosObject at " + i + " shall be at index " + i + " of COSArray",
                    annotationsArray.indexOf(annot.getCOSObject()) == i);

            // compare with Java List/Array
            assertTrue("PDAnnotations at " + i + " shall be at index " + i + " of List",
                    tbcAnnotationsList.indexOf(annot) == i);
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

        assertTrue("List size shall be 4", annotationsList.size() == 4);
        assertTrue("COSArray size shall be 4", annotationsArray.size() == 4);

        PDAnnotation annot = (PDAnnotation) annotationsList.get(3);
        assertTrue("Added annotation shall be 4th entry in COSArray", annotationsArray.indexOf(annot.getCOSObject()) == 3);
        assertEquals("Provided COSArray and underlying COSArray shall be equal", annotationsArray, cosArrayList.toList());
    }

    /**
     * Test removing a PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeFromListByIndex() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        cosArrayList.remove(positionToRemove);

        assertTrue("List size shall be 2", cosArrayList.size() == 2);
        assertTrue("COSArray size shall be 2", annotationsArray.size() == 2);

        PDAnnotation annot = (PDAnnotation) cosArrayList.get(1);
        assertTrue("Object at original position 2 shall now be at position 1 in underlying COSArray",
                annotationsArray.indexOf(annot.getCOSObject()) == 1);

        // compare with Java List/Array to ensure correct object at position
        assertTrue("List object at 2 is at position 1 in COSArrayList now",
                cosArrayList.indexOf(tbcAnnotationsList.get(2)) == 1);
        assertTrue("COSObject of List object at 2 is at position 1 in COSArray now",
                annotationsArray.indexOf(tbcAnnotationsList.get(2).getCOSObject()) == 1);
        assertTrue("Array object at 2 is at position 1 in underlying COSArray now",
                annotationsArray.indexOf(tbcAnnotationsArray[2]) == 1);

        assertTrue("PDAnnotation shall no longer exist in List",
                cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
                annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);
    }

    /**
     * Test removing a PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = tbcAnnotationsList.get(positionToRemove);

        cosArrayList.remove(toBeRemoved);

        assertTrue("List size shall be 2", cosArrayList.size() == 2);
        assertTrue("COSArray size shall be 2", annotationsArray.size() == 2);

        PDAnnotation annot = (PDAnnotation) cosArrayList.get(1);
        assertTrue("Object at original position 2 shall now be at position 1 in underlying COSArray",
                annotationsArray.indexOf(annot.getCOSObject()) == 1);

        // compare with Java List/Array to ensure correct object at position
        assertTrue("List object at 2 is at position 1 in COSArrayList now",
                cosArrayList.indexOf(tbcAnnotationsList.get(2)) == 1);
        assertTrue("COSObject of List object at 2 is at position 1 in COSArray now",
                annotationsArray.indexOf(tbcAnnotationsList.get(2).getCOSObject()) == 1);
        assertTrue("Array object at 2 is at position 1 in underlying COSArray now",
                annotationsArray.indexOf(tbcAnnotationsArray[2]) == 1);

        assertTrue("PDAnnotation shall no longer exist in List",
                cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
                annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);
    }

    @Test
    public void removeFromFilteredListByIndex() throws Exception
    {
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
        COSArray underlyingCOSArray = pdPage.getCOSObject().getCOSArray(COSName.ANNOTS);

        assertTrue("Filtered COSArrayList size shall be 2", cosArrayList.size() == 2);
        assertTrue("Underlying COSArray shall have 3 entries", underlyingCOSArray.size() == 3);
        assertTrue("Backed COSArray shall have 3 entries", cosArrayList.toList().size() == 3);

        // remove aCircle annotation
        int positionToRemove = 1;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);
        assertTrue("We should remove the circle annotation", toBeRemoved.getSubtype().equals(PDAnnotationSquareCircle.SUB_TYPE_CIRCLE));
        cosArrayList.remove(positionToRemove);

        assertTrue("List size shall be 2", cosArrayList.size() == 1);
        assertTrue("COSArray size shall be 2", underlyingCOSArray.size() == 2);
        assertTrue("Backed COSArray size shall be 2", cosArrayList.toList().size() == 2);

        assertTrue("Removed annotation shall no longer appear in COSArrayList", cosArrayList.indexOf(toBeRemoved) == -1);
        assertTrue("Removed annotation shall no longer appear in underlying COSArray", underlyingCOSArray.indexOf(toBeRemoved.getCOSObject()) == -1);
        assertTrue("Removed annotation shall no longer appear in backed COSArray", cosArrayList.toList().indexOf(toBeRemoved.getCOSObject()) == -1);
    }
    

    @Test
    public void removeFromFilteredListByObject() throws Exception
    {
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
        COSArray underlyingCOSArray = pdPage.getCOSObject().getCOSArray(COSName.ANNOTS);

        assertTrue("Filtered COSArrayList size shall be 2", cosArrayList.size() == 2);
        assertTrue("Underlying COSArray shall have 3 entries", underlyingCOSArray.size() == 3);
        assertTrue("Backed COSArray shall have 3 entries", cosArrayList.toList().size() == 3);

        // remove aCircle annotation
        int positionToRemove = 1;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);
        assertTrue("We should remove the circle annotation", toBeRemoved.getSubtype().equals(PDAnnotationSquareCircle.SUB_TYPE_CIRCLE));
        cosArrayList.remove(toBeRemoved);

        assertTrue("List size shall be 2", cosArrayList.size() == 1);
        assertTrue("COSArray size shall be 2", underlyingCOSArray.size() == 2);
        assertTrue("Backed COSArray size shall be 2", cosArrayList.toList().size() == 2);

        assertTrue("Removed annotation shall no longer appear in COSArrayList", cosArrayList.indexOf(toBeRemoved) == -1);
        assertTrue("Removed annotation shall no longer appear in underlying COSArray", underlyingCOSArray.indexOf(toBeRemoved.getCOSObject()) == -1);
        assertTrue("Removed annotation shall no longer appear in backed COSArray", cosArrayList.toList().indexOf(toBeRemoved.getCOSObject()) == -1);
    }
}