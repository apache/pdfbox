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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This unit test validates the correct working behavior of PDPage annotations
 * filtering
 * 
 * @author <a href="mailto:maxime.veron.pro@gmail.com">Maxime Veron</a>
 *
 */
class TestPDPageAnnotationsFiltering
{
    // test mock page for annotations filtering
    private PDPage page;

    @BeforeEach
    public void initMock()
    {
        COSDictionary mockedPageWithAnnotations = new COSDictionary();
        COSArray annotsDictionary = new COSArray();
        annotsDictionary.add(new PDAnnotationRubberStamp().getCOSObject());
        annotsDictionary.add(new PDAnnotationSquare().getCOSObject());
        annotsDictionary.add(new PDAnnotationLink().getCOSObject());
        mockedPageWithAnnotations.setItem(COSName.ANNOTS, annotsDictionary);
        page = new PDPage(mockedPageWithAnnotations);
    }

    @Test
     void validateNoFiltering() throws IOException
    {
        List<PDAnnotation> annotations = page.getAnnotations();
        assertEquals(3, annotations.size());
        assertTrue(annotations.get(0) instanceof PDAnnotationRubberStamp);
        assertTrue(annotations.get(1) instanceof PDAnnotationSquare);
        assertTrue(annotations.get(2) instanceof PDAnnotationLink);
    }

    @Test
     void validateAllFiltered() throws IOException
    {
        List<PDAnnotation> annotations = page.getAnnotations(annotation -> false);
        assertEquals(0, annotations.size());
    }

    @Test
     void validateSelectedFew() throws IOException
    {
        List<PDAnnotation> annotations = page.getAnnotations(annotation -> 
            (annotation instanceof PDAnnotationLink || annotation instanceof PDAnnotationSquare));
        assertEquals(2, annotations.size());
        assertTrue(annotations.get(0) instanceof PDAnnotationSquare);
        assertTrue(annotations.get(1) instanceof PDAnnotationLink);
    }
}
