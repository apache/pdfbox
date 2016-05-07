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
package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;

/**
 * Add annotations to pages of a PDF document.
 */
public final class AddAnnotations
{
    static final float INCH = 72;

    private AddAnnotations()
    {
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: " + AddAnnotations.class.getName() + " <output-pdf>");
            System.exit(1);
        }

        PDDocument document = new PDDocument();
        try
        {
            PDPage page1 = new PDPage();
            PDPage page2 = new PDPage();
            PDPage page3 = new PDPage();
            document.addPage(page1);
            document.addPage(page2);
            document.addPage(page3);
            List<PDAnnotation> annotations = page1.getAnnotations();

            // Some basic reusable objects/constants
            // Annotations themselves can only be used once!
            PDColor red = new PDColor(new float[] { 1, 0, 0 }, PDDeviceRGB.INSTANCE);
            PDColor blue = new PDColor(new float[] { 0, 0, 1 }, PDDeviceRGB.INSTANCE);
            PDColor black = new PDColor(new float[] { 0, 0, 0 }, PDDeviceRGB.INSTANCE);

            PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
            borderThick.setWidth(INCH / 12);  // 12th inch
            
            PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
            borderThin.setWidth(INCH / 72); // 1 point
            
            PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
            borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            borderULine.setWidth(INCH / 72); // 1 point
            
            float pw = page1.getMediaBox().getUpperRightX();
            float ph = page1.getMediaBox().getUpperRightY();
            
            // First add some text, two lines we'll add some annotations to this later
            PDFont font = PDType1Font.HELVETICA_BOLD;
            PDPageContentStream contents = new PDPageContentStream(document, page1);
            contents.beginText();
            contents.setFont(font, 18);
            contents.newLineAtOffset(INCH, ph - INCH - 18);
            contents.showText("PDFBox");
            contents.newLineAtOffset(0, -(INCH / 2));
            contents.showText("External URL");
            contents.newLineAtOffset(0, -(INCH / 2));
            contents.showText("Jump to page three");
            contents.endText();
            contents.close();

            // Now add the markup annotation, a highlight to PDFBox text
            PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(
                    PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
            txtMark.setColor(blue);
            txtMark.setConstantOpacity((float)0.2); // 20% transparent

            // Set the rectangle containing the markup
            float textWidth = font.getStringWidth("PDFBox") / 1000 * 18;
            PDRectangle position = new PDRectangle();
            position.setLowerLeftX(INCH);
            position.setLowerLeftY(ph - INCH - 18);
            position.setUpperRightX(INCH + textWidth);
            position.setUpperRightY(ph - INCH);
            txtMark.setRectangle(position);

            // work out the points forming the four corners of the annotations
            // set out in anti clockwise form (Completely wraps the text)
            // OK, the below doesn't match that description.
            // It's what acrobat 7 does and displays properly!
            float[] quads = new float[8];
            quads[0] = position.getLowerLeftX();  // x1
            quads[1] = position.getUpperRightY()-2; // y1
            quads[2] = position.getUpperRightX(); // x2
            quads[3] = quads[1]; // y2
            quads[4] = quads[0];  // x3
            quads[5] = position.getLowerLeftY()-2; // y3
            quads[6] = quads[2]; // x4
            quads[7] = quads[5]; // y5

            txtMark.setQuadPoints(quads);
            txtMark.setContents("Highlighted since it's important");
            annotations.add(txtMark);

            // Now add the link annotation, so the click on "External URL" works
            PDAnnotationLink txtLink = new PDAnnotationLink();
            txtLink.setBorderStyle(borderULine);

            // Set the rectangle containing the link
            textWidth = font.getStringWidth("External URL") / 1000 * 18;
            position = new PDRectangle();
            position.setLowerLeftX(INCH);
            position.setLowerLeftY(ph - 1.5f * INCH -20);  // down a couple of points
            position.setUpperRightX(INCH + textWidth);
            position.setUpperRightY(ph - 1.5f * INCH);
            txtLink.setRectangle(position);

            // add an action
            PDActionURI action = new PDActionURI();
            action.setURI("http://pdfbox.apache.org");
            txtLink.setAction(action);
            annotations.add(txtLink);
            
            // Now draw a few more annotations
            PDAnnotationSquareCircle aCircle = new PDAnnotationSquareCircle(
                    PDAnnotationSquareCircle.SUB_TYPE_CIRCLE);
            aCircle.setContents("Circle Annotation");
            aCircle.setInteriorColor(red);  // Fill in circle in red
            aCircle.setColor(blue); // The border itself will be blue
            aCircle.setBorderStyle(borderThin);

            // Place the annotation on the page, we'll make this 1" round
            // 3" down, 1" in on the page
            position = new PDRectangle();
            position.setLowerLeftX(INCH);
            position.setLowerLeftY(ph - 3 * INCH -INCH); // 1" height, 3" down
            position.setUpperRightX(2 * INCH); // 1" in, 1" width
            position.setUpperRightY(ph - 3*INCH); // 3" down
            aCircle.setRectangle(position);
            annotations.add(aCircle);

            // Now a square annotation
            PDAnnotationSquareCircle aSquare = new PDAnnotationSquareCircle(
                    PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
            aSquare.setContents("Square Annotation");
            aSquare.setColor(red);  // Outline in red, not setting a fill
            aSquare.setBorderStyle(borderThick);

            // Place the annotation on the page, we'll make this 1" (72 points) square
            // 3.5" down, 1" in from the right on the page
            position = new PDRectangle(); // Reuse the variable, but note it's a new object!
            position.setLowerLeftX(pw - 2 * INCH);  // 1" in from right, 1" wide
            position.setLowerLeftY(ph - 3.5f * INCH - INCH); // 1" height, 3.5" down
            position.setUpperRightX(pw - INCH); // 1" in from right
            position.setUpperRightY(ph - 3.5f * INCH); // 3.5" down
            aSquare.setRectangle(position);
            annotations.add(aSquare);

            // Now we want to draw a line between the two, one end with an open arrow
            PDAnnotationLine aLine = new PDAnnotationLine();
            aLine.setEndPointEndingStyle(PDAnnotationLine.LE_OPEN_ARROW);
            aLine.setContents("Circle->Square");
            aLine.setCaption(true);  // Make the contents a caption on the line

            // Set the rectangle containing the line
            position = new PDRectangle(); // Reuse the variable, but note it's a new object!
            position.setLowerLeftX(2 * INCH);  // 1" in + width of circle
            position.setLowerLeftY(ph - 3.5f * INCH - INCH); // 1" height, 3.5" down
            position.setUpperRightX(pw - INCH-INCH); // 1" in from right, and width of square
            position.setUpperRightY(ph - 3 * INCH); // 3" down (top of circle)
            aLine.setRectangle(position);

            // Now set the line position itself
            float[] linepos = new float[4];
            linepos[0] = 2 * INCH;  // x1 = rhs of circle
            linepos[1] = ph - 3.5f * INCH; // y1 halfway down circle
            linepos[2] = pw- 2 * INCH;  // x2 = lhs of square
            linepos[3] = ph- 4 * INCH; // y2 halfway down square
            aLine.setLine(linepos);

            aLine.setBorderStyle(borderThick);
            aLine.setColor(black);
            annotations.add(aLine);
            
            
            // Now add the link annotation, so the click on "Jump to page three" works
            PDAnnotationLink pageLink = new PDAnnotationLink();
            pageLink.setBorderStyle(borderULine);

            // Set the rectangle containing the link
            textWidth = font.getStringWidth("Jump to page three") / 1000 * 18;
            position = new PDRectangle();
            position.setLowerLeftX(INCH);
            position.setLowerLeftY(ph - 2 * INCH - 20);  // down a couple of points
            position.setUpperRightX(INCH + textWidth);
            position.setUpperRightY(ph - 2 * INCH);
            pageLink.setRectangle(position);

            // add the GoTo action
            PDActionGoTo actionGoto = new PDActionGoTo();
            // see javadoc for other types of PDPageDestination
            PDPageDestination dest = new PDPageFitWidthDestination();
            // do not use setPageNumber(), this is for external destinations only
            dest.setPage(page3);
            actionGoto.setDestination(dest);
            pageLink.setAction(actionGoto);
            annotations.add(pageLink);      
            

            showPageNo(document, page1, "Page 1");
            showPageNo(document, page2, "Page 2");
            showPageNo(document, page3, "Page 3");
            
            // save the PDF
            document.save(args[0]);
        }
        finally
        {
            document.close();
        }
    }

    private static void showPageNo(PDDocument document, PDPage page, String pageText)
            throws IOException
    {
        int fontSize = 10;

        PDPageContentStream contents = 
                new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, true);
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        PDFont font = PDType1Font.HELVETICA;
        contents.setFont(font, fontSize);
        float textWidth = font.getStringWidth(pageText) / 1000 * fontSize;
        contents.beginText();
        contents.newLineAtOffset(pageWidth / 2 - textWidth / 2, pageHeight - INCH / 2);
        contents.showText(pageText);
        contents.endText();
        contents.close();
    }
}
