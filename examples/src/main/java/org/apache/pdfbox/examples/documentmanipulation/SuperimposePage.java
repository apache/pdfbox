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
package org.apache.pdfbox.examples.documentmanipulation;

import java.awt.geom.AffineTransform;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.LayerUtility;

/**
 * Example to show superimposing a PDF page onto another PDF.
 *
 */
public class SuperimposePage {

    public static void main(String[] args)
    {
        try {

            // Create a new document with some basic content
            PDDocument aDoc = new PDDocument();
            PDPage aPage = new PDPage();
            aDoc.addPage(aPage);

            PDPageContentStream aContent = new PDPageContentStream(aDoc, aPage);

            PDFont font = PDType1Font.HELVETICA_BOLD;
            aContent.beginText();
            aContent.setFont(font, 12);
            aContent.moveTextPositionByAmount(2, 5);
            aContent.drawString("Import a pdf file:");
            aContent.endText();
            aContent.close();
            
            // Superimpose a page form a source document

            // This will handle the actual import and resources
            LayerUtility layerUtility = new LayerUtility(aDoc);

            PDDocument toBeImported = PDDocument.load(new File(args[0]));
            
            // Get the page as a PDXObjectForm to place it
            PDFormXObject mountable = layerUtility.importPageAsForm(
                    toBeImported, 0);
            // add compression to the stream (import deactivates compression)
            mountable.getPDStream().addCompression();

            // add to the existing content stream
            PDPageContentStream contentStream = new PDPageContentStream(aDoc,
                    aPage, true, true);

            // Store the graphics state
            contentStream.appendRawCommands("q\n".getBytes("ISO-8859-1"));

            // use a transformation to be able to scale and move easily
            AffineTransform transform = new AffineTransform();

            // draw the PDXObjectForm
            contentStream.drawXObject(mountable, transform);

            // restore former graphics state
            contentStream.appendRawCommands("Q\n".getBytes("ISO-8859-1"));
            contentStream.close();

            // close the imported document
            toBeImported.close();

            aDoc.save(args[1]);
            aDoc.close();
        }
        catch (Exception e)
        {
            System.out.println(" error creating pdf file." + e.toString());
        }
    }
}