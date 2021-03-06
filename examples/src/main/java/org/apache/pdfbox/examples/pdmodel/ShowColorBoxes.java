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

import java.awt.Color;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.util.Matrix;

/**
 * Creates a simple document. The example is taken from the pdf file format specification.
 */
public final class ShowColorBoxes
{

    private ShowColorBoxes()
    {
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("usage: " +ShowColorBoxes.class.getName() + " <output-file>");
            System.exit(1);
        }
        
        String filename = args[0];

        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            // fill the entire background with cyan
            try (PDPageContentStream contents = new PDPageContentStream(doc, page))
            {
                // fill the entire background with cyan
                contents.setNonStrokingColor(Color.CYAN);
                contents.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                contents.fill();
                
                // draw a red box in the lower left hand corner
                contents.setNonStrokingColor(Color.RED);
                contents.addRect(10, 10, 100, 100);
                contents.fill();
                
                // draw a blue box with rect x=200, y=500, w=200, h=100
                // 105° rotation is around the bottom left corner
                contents.saveGraphicsState();
                contents.setNonStrokingColor(Color.BLUE);
                contents.transform(Matrix.getRotateInstance(Math.toRadians(105), 200, 500));
                contents.addRect(0, 0, 200, 100);
                contents.fill();
                contents.restoreGraphicsState();
            }
            doc.save(filename);
        }
    }
}
