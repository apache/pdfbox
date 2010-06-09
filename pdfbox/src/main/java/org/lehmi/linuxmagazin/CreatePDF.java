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
package org.lehmi.linuxmagazin;

import java.io.IOException;
import org.apache.pdfbox.exceptions.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.edit.*;
import org.apache.pdfbox.pdmodel.font.*;


/**
 * This is an example of how to use a text matrix.
 * @version $Revision: 1.0 $
 */
public class CreatePDF
{
    /**
     * creates a sample document with some text using a text matrix.
     *
     * @param message The message to write in the file.
     * @param outfile The resulting PDF.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public static void main(String[] args)
    {
        PDDocument document = null;
        try {
            document = new PDDocument();
            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA,1);
            contentStream.beginText();
            for (int i=0;i<9;i++) {
                contentStream.setNonStrokingColor(10*i,20*i,30*i);
                contentStream.setTextScaling(16+(i*6), 16+(i*6), 100, 100+i*60);
                contentStream.drawString("Linuxmagazin");
            }
            contentStream.endText();
            contentStream.close();
            document.save(args[0]);
            document.close();
        }
        catch (COSVisitorException exception) {
            System.err.println(exception);
        }
        catch (IOException exception) {
            System.err.println(exception);
        }
    }

}
