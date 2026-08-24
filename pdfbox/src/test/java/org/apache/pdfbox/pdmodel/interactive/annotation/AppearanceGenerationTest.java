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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.junit.Test;

public class AppearanceGenerationTest
{

    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/annotation");

    /**
     * Test the generation of /AP content with our handlers. The test file was created by running
     * the AddAnnotations example.
     * 
     * @throws IOException
     */
    @Test
    public void testAnnotationAppearancesGeneration() throws IOException
    {
        BufferedImage expectedImage;
        BufferedImage actualImage;
        // 1. render with existing /AP
        PDDocument doc = PDDocument.load(new File(IN_DIR, "Annotations.pdf"));
        PDFRenderer r = new PDFRenderer(doc);
        expectedImage = r.renderImage(0);
        doc.close();
        // 2. render with removed /AP
        doc = PDDocument.load(new File(IN_DIR, "Annotations.pdf"));
        List<PDAnnotation> annotations = doc.getPage(0).getAnnotations();
        for (PDAnnotation ann : annotations)
        {
            assertNotNull(ann.getAppearance());
            ann.setAppearance(null);
        }

        // need to set rectangle because the handler in the example has modified it
        PDAnnotationMarkup freeTextAnnotation = (PDAnnotationMarkup) annotations.get(6);
        PDRectangle rectangle = freeTextAnnotation.getRectangle();
        rectangle.setLowerLeftX(72);
        rectangle.setLowerLeftY(216);
        freeTextAnnotation.setRectangle(rectangle);
        PDAnnotationMarkup annotationCaret = (PDAnnotationMarkup) annotations.get(8);
        annotationCaret.setRectangle(new PDRectangle(300, 50, 100, 100));

        r = new PDFRenderer(doc);
        actualImage = r.renderImage(0);
        doc.close();
        // 3. compare
        ValidateXImage.checkIdent(expectedImage, actualImage);
    }

}
