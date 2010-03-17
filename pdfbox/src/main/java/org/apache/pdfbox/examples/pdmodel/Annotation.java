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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;


import java.util.List;

/**
 * This is an example on how to add annotations to pages of a PDF document.
 *
 * @author Paul King
 * @version $Revision: 1.2 $
 */
public class Annotation
{
    private Annotation()
    {
        //utility class, should not be instantiated.
    }

    /**
     * This will create a doucument showing various annotations.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            PDDocument document = new PDDocument();

            try
            {
                PDPage page = new PDPage();
                document.addPage(page);
                List annotations = page.getAnnotations();

                // Setup some basic reusable objects/constants
                // Annotations themselves can only be used once!

                float inch = 72;
                PDGamma colourRed = new PDGamma();
                colourRed.setR(1);
                PDGamma colourBlue = new PDGamma();
                colourBlue.setB(1);
                PDGamma colourBlack = new PDGamma();

                PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
                borderThick.setWidth(inch/12);  // 12th inch
                PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
                borderThin.setWidth(inch/72); // 1 point
                PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
                borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
                borderULine.setWidth(inch/72); // 1 point


                float pw = page.getMediaBox().getUpperRightX();
                float ph = page.getMediaBox().getUpperRightY();


                // First add some text, two lines we'll add some annotations to this later


                PDFont font = PDType1Font.HELVETICA_BOLD;

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont( font, 18 );
                contentStream.moveTextPositionByAmount( inch, ph-inch-18);
                contentStream.drawString( "PDFBox" );
                contentStream.moveTextPositionByAmount( 0,-(inch/2));
                contentStream.drawString( "Click Here" );
                contentStream.endText();

                contentStream.close();

                // Now add the markup annotation, a highlight to PDFBox text
                PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
                txtMark.setColour(colourBlue);
                txtMark.setConstantOpacity((float)0.2);   // Make the highlight 20% transparent

                // Set the rectangle containing the markup

                float textWidth = (font.getStringWidth( "PDFBox" )/1000) * 18;
                PDRectangle position = new PDRectangle();
                position.setLowerLeftX(inch);
                position.setLowerLeftY( ph-inch-18 );
                position.setUpperRightX(72 + textWidth);
                position.setUpperRightY(ph-inch);
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

                // Now add the link annotation, so the clickme works
                PDAnnotationLink txtLink = new PDAnnotationLink();
                txtLink.setBorderStyle(borderULine);

                // Set the rectangle containing the link

                textWidth = (font.getStringWidth( "Click Here" )/1000) * 18;
                position = new PDRectangle();
                position.setLowerLeftX(inch);
                position.setLowerLeftY( ph-(float)(1.5*inch)-20);  // down a couple of points
                position.setUpperRightX(72 + textWidth);
                position.setUpperRightY(ph-(float)(1.5*inch));
                txtLink.setRectangle(position);

                // add an action
                PDActionURI action = new PDActionURI();
                action.setURI("http://www.pdfbox.org");
                txtLink.setAction(action);

                annotations.add(txtLink);


                // Now draw a few more annotations

                PDAnnotationSquareCircle aCircle =
                    new PDAnnotationSquareCircle( PDAnnotationSquareCircle.SUB_TYPE_CIRCLE);
                aCircle.setContents("Circle Annotation");
                aCircle.setInteriorColour(colourRed);  // Fill in circle in red
                aCircle.setColour(colourBlue); // The border itself will be blue
                aCircle.setBorderStyle(borderThin);

                // Place the annotation on the page, we'll make this 1" round
                // 3" down, 1" in on the page

                position = new PDRectangle();
                position.setLowerLeftX(inch);
                position.setLowerLeftY(ph-(3*inch)-inch); // 1" height, 3" down
                position.setUpperRightX(2*inch); // 1" in, 1" width
                position.setUpperRightY(ph-(3*inch)); // 3" down
                aCircle.setRectangle(position);

                //  add to the annotations on the page
                annotations.add(aCircle);

                // Now a square annotation

                PDAnnotationSquareCircle aSquare =
                    new PDAnnotationSquareCircle( PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
                aSquare.setContents("Square Annotation");
                aSquare.setColour(colourRed);  // Outline in red, not setting a fill
                aSquare.setBorderStyle(borderThick);

                // Place the annotation on the page, we'll make this 1" (72points) square
                // 3.5" down, 1" in from the right on the page

                position = new PDRectangle(); // Reuse the variable, but note it's a new object!
                position.setLowerLeftX(pw-(2*inch));  // 1" in from right, 1" wide
                position.setLowerLeftY(ph-(float)(3.5*inch) - inch); // 1" height, 3.5" down
                position.setUpperRightX(pw-inch); // 1" in from right
                position.setUpperRightY(ph-(float)(3.5*inch)); // 3.5" down
                aSquare.setRectangle(position);

                //  add to the annotations on the page
                annotations.add(aSquare);

                // Now we want to draw a line between the two, one end with an open arrow

                PDAnnotationLine aLine = new PDAnnotationLine();

                aLine.setEndPointEndingStyle( PDAnnotationLine.LE_OPEN_ARROW );
                aLine.setContents("Circle->Square");
                aLine.setCaption(true);  // Make the contents a caption on the line

                // Set the rectangle containing the line

                position = new PDRectangle(); // Reuse the variable, but note it's a new object!
                position.setLowerLeftX(2*inch);  // 1" in + width of circle
                position.setLowerLeftY(ph-(float)(3.5*inch)-inch); // 1" height, 3.5" down
                position.setUpperRightX(pw-inch-inch); // 1" in from right, and width of square
                position.setUpperRightY(ph-(3*inch)); // 3" down (top of circle)
                aLine.setRectangle(position);

                // Now set the line position itself
                float[] linepos = new float[4];
                linepos[0] = 2*inch;  // x1 = rhs of circle
                linepos[1] = ph-(float)(3.5*inch); // y1 halfway down circle
                linepos[2] = pw-(2*inch);  // x2 = lhs of square
                linepos[3] = ph-(4*inch); // y2 halfway down square
                aLine.setLine(linepos);

                aLine.setBorderStyle(borderThick);
                aLine.setColour(colourBlack);

                // add to the annotations on the page
                annotations.add(aLine);


                // Finally all done


                document.save( args[0] );
            }
            finally
            {
                document.close();
            }
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.Annotation <output-pdf>" );
    }
}
