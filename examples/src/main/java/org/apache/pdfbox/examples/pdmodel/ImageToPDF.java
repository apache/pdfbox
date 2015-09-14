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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Creates a PDF document from an image.
 *
 * The example is taken from the pdf file format specification.
 */
public final class ImageToPDF
{
    private ImageToPDF()
    {
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length != 2)
        {
            System.err.println("usage: " + ImageToPDF.class.getName() + " <image> <output-file>");
            System.exit(1);
        }
        
        String imagePath = args[0];
        String pdfPath = args[1];
        
        if (!pdfPath.endsWith(".pdf"))
        {
            System.err.println("Last argument must be the destination .pdf file");
            System.exit(1);
        }

        PDDocument doc = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            // createFromFile is the easiest way with an image file
            // if you already have the image in a BufferedImage, 
            // call LosslessFactory.createFromImage() instead
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
            
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            
            // draw the image at full size at (x=20, y=20)
            contents.drawImage(pdImage, 20, 20);
            
            // to draw the image at half size at (x=20, y=20) use
            // contents.drawImage(pdImage, 20, 20, pdImage.getWidth() / 2, pdImage.getHeight() / 2);
            
            contents.close();
            doc.save(pdfPath);
        }
        finally
        {
            doc.close();
        }
    }
}
