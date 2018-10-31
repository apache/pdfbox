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
package org.apache.pdfbox.examples.util;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * This is a simple text extraction example to get started. For more advance usage, see the
 * ExtractTextByArea and the DrawPrintTextLocations examples in this subproject, as well as the
 * ExtractText tool in the tools subproject.
 *
 * @author Tilman Hausherr
 */
public class ExtractTextSimple
{
    private ExtractTextSimple()
    {
        // example class should not be instantiated
    }

    /**
     * This will print the documents text page by page.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing or extracting the document.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
        }

        PDDocument document = PDDocument.load(new File(args[0]));
        AccessPermission ap = document.getCurrentAccessPermission();
        if (!ap.canExtractContent())
        {
            throw new IOException("You do not have permission to extract text");
        }

        PDFTextStripper stripper = new PDFTextStripper();

        // This example uses sorting, but in some cases it is more useful to switch it off,
        // e.g. in some files with columns where the PDF content stream respects the
        // column order.
        stripper.setSortByPosition(true);

        for (int p = 1; p <= document.getNumberOfPages(); ++p)
        {
            // Set the page interval to extract. If you don't, then all pages would be extracted.
            stripper.setStartPage(p);
            stripper.setEndPage(p);

            // let the magic happen
            String text = stripper.getText(document);

            // do some nice output with a header
            String pageStr = String.format("page %d:", p);
            System.out.println(pageStr);
            for (int i = 0; i < pageStr.length(); ++i)
            {
                System.out.print("-");
            }
            System.out.println();
            System.out.println(text.trim());
            System.out.println();

            // If the extracted text is empty or gibberish, please try extracting text
            // with Adobe Reader first before asking for help. Also read the FAQ
            // on the website: 
            // https://pdfbox.apache.org/2.0/faq.html#text-extraction
        }
        document.close();
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + ExtractTextSimple.class.getName() + " <input-pdf>");
        System.exit(-1);
    }
}
