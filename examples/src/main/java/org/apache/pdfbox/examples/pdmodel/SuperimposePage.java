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

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.util.Matrix;

/**
 * Example to show superimposing a PDF page onto another PDF.
 */
public final class SuperimposePage
{
    
    private SuperimposePage()
    {
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 2)
        {
            System.err.println("usage: " + SuperimposePage.class.getName() +
                    " <source-pdf> <dest-pdf>");
            System.exit(1);
        }
        String sourcePath = args[0];
        String destPath = args[1];
        
        PDDocument sourceDoc = null;
        try
        {
            // load the source PDF
            sourceDoc = PDDocument.load(new File(sourcePath));
            int sourcePage = 1;
            
            // create a new PDF and add a blank page
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);

            // write some sample text to the new page
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contents.newLineAtOffset(2, PDRectangle.LETTER.getHeight() - 12);
            contents.showText("Sample text");
            contents.endText();
            
            // Create a Form XObject from the source document using LayerUtility
            LayerUtility layerUtility = new LayerUtility(doc);
            PDFormXObject form = layerUtility.importPageAsForm(sourceDoc, sourcePage - 1);
            
            // draw the full form
            contents.drawForm(form);
            
            // draw a scaled form
            contents.saveGraphicsState();
            Matrix matrix = Matrix.getScaleInstance(0.5f, 0.5f);
            contents.transform(matrix);
            contents.drawForm(form);
            contents.restoreGraphicsState();

            // draw a scaled and rotated form
            contents.saveGraphicsState();
            matrix.rotate(1.8 * Math.PI); // radians
            contents.transform(matrix);
            contents.drawForm(form);
            contents.restoreGraphicsState();

            contents.close();
            doc.save(destPath);
            doc.close();
        }
        finally
        {
            if (sourceDoc != null)
            {
                sourceDoc.close();
            }
        }
    }
}
