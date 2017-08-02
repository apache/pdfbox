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

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.util.Matrix;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Test for the PDAnnotation classes.
 *
 */
public class PDSquareAnnotationTest
{

    // delta for comparing equality of float values
    private static final double DELTA = 1e-4;
    
    // the location of the annotation
    static PDRectangle rectangle;
    
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/annotation");
    private static final String NAME_OF_PDF = "PDSquareAnnotationTest.pdf";

    @Before
    public void setUp() throws IOException
    {
        rectangle = new PDRectangle();
        rectangle.setLowerLeftX(91.5958f);
        rectangle.setLowerLeftY(741.91f);
        rectangle.setUpperRightX(113.849f);
        rectangle.setUpperRightY(757.078f);
    }

    @Test
    public void createDefaultSquareAnnotation()
    {
        PDAnnotation annotation = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
        assertEquals(COSName.ANNOT, annotation.getCOSObject().getItem(COSName.TYPE));
        assertEquals(PDAnnotationSquareCircle.SUB_TYPE_SQUARE,
                annotation.getCOSObject().getNameAsString(COSName.SUBTYPE));
    }

    @Test
    public void createWithAppearance() throws IOException
    {
        // the width of the <nnotations border
        final int borderWidth = 1;

        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage();
            document.addPage(page);
            List<PDAnnotation> annotations = page.getAnnotations();
            
            PDAnnotationSquareCircle annotation = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
            
            PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
            borderThin.setWidth(borderWidth);
            
            PDColor red = new PDColor(new float[] { 1, 0, 0 }, PDDeviceRGB.INSTANCE);
            annotation.setContents("Square Annotation");
            annotation.setColor(red);
            annotation.setBorderStyle(borderThin);
            
            
            annotation.setRectangle(rectangle);
            
            annotation.constructAppearances();
            annotations.add(annotation);
        }
    }
    
    @Test
    public void validateAppearance() throws IOException
    {
        // the width of the <nnotations border
        final int borderWidth = 1;

        File file = new File(IN_DIR, NAME_OF_PDF);
        try (PDDocument document = PDDocument.load(file))
        {
            PDPage page = document.getPage(0);
            List<PDAnnotation> annotations = page.getAnnotations();
            
            PDAnnotationSquareCircle annotation = (PDAnnotationSquareCircle) annotations.get(0);
            
            // test the correct setting of the appearance stream
            assertNotNull("Appearance dictionary shall not be null", annotation.getAppearance());
            assertNotNull("Normal appearance shall not be null", annotation.getAppearance().getNormalAppearance());
            PDAppearanceStream appearanceStream = annotation.getAppearance().getNormalAppearance().getAppearanceStream();
            assertNotNull("Appearance stream shall not be null", appearanceStream);
            assertEquals(rectangle.getLowerLeftX(), appearanceStream.getBBox().getLowerLeftX(), DELTA);
            assertEquals(rectangle.getLowerLeftY(), appearanceStream.getBBox().getLowerLeftY(), DELTA);
            assertEquals(rectangle.getWidth(), appearanceStream.getBBox().getWidth(), DELTA);
            assertEquals(rectangle.getHeight(), appearanceStream.getBBox().getHeight(), DELTA);
            
            Matrix matrix = appearanceStream.getMatrix();
            assertNotNull("Matrix shall not be null", matrix);
            
            // should have been translated to a 0 origin
            assertEquals(-rectangle.getLowerLeftX(), matrix.getTranslateX(), DELTA);
            assertEquals(-rectangle.getLowerLeftY(), matrix.getTranslateY(), DELTA);
            
            // test the content of the apperance stream
            PDStream contentStream = appearanceStream.getContentStream();
            assertNotNull("Content stream shall not be null", contentStream);
            PDFStreamParser parser = new PDFStreamParser(appearanceStream);
            parser.parse();
            List<Object> tokens = parser.getTokens();
            
            // the samples content stream should contain 10 tokens
            assertEquals(10, tokens.size());
            
            // setting of the stroking color
            assertEquals(1, ((COSInteger) tokens.get(0)).intValue());
            assertEquals(0, ((COSInteger) tokens.get(1)).intValue());
            assertEquals(0, ((COSInteger) tokens.get(2)).intValue());
            assertEquals("RG", ((Operator) tokens.get(3)).getName());
            
            // setting of the rectangle for the border
            // it shall be inset by the border width
            assertEquals(rectangle.getLowerLeftX() + borderWidth, ((COSFloat) tokens.get(4)).floatValue(), DELTA);
            assertEquals(rectangle.getLowerLeftY() + borderWidth, ((COSFloat) tokens.get(5)).floatValue(), DELTA);
            assertEquals(rectangle.getWidth() - 2 * borderWidth, ((COSFloat) tokens.get(6)).floatValue(), DELTA);
            assertEquals(rectangle.getHeight() - 2 * borderWidth, ((COSFloat) tokens.get(7)).floatValue(), DELTA);
            assertEquals("re", ((Operator) tokens.get(8)).getName());
            assertEquals("S", ((Operator) tokens.get(9)).getName());
        }
    }
}
