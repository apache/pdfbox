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
package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.pdfbox.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

/**
 * Extract the XMP metadata from the document or from a page.
 *
 * @author Tilman Hausherr
 */
public class ExtractXMP
{
    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD = "-password";
    private static final String CONSOLE = "-console";
    private static final String PAGE = "-page";

    /**
     * private constructor.
    */
    public ExtractXMP()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws IOException if there is an error reading the document or extracting the XMP data.
     */
    public static void main( String[] args ) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        ExtractXMP extractor = new ExtractXMP();
        extractor.startExtraction(args);
    }

    public void startExtraction(String[] args) throws IOException
    {
        boolean toConsole = false;
        @SuppressWarnings({"squid:S2068"})
        String password = "";
        int page = 0;
        String pdfFile = null;
        String outputFile = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(PASSWORD))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                password = args[i];
            }
            else if (args[i].equals(PAGE))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                page = Integer.parseInt(args[i]);
            }
            else if (args[i].equals(CONSOLE))
            {
                toConsole = true;
            }
            else
            {
                if (pdfFile == null)
                {
                    pdfFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }

        if (pdfFile == null)
        {
            usage();
        }
        else
        {
            PrintStream output = null;
            PDDocument document = null;
            try
            {
                if (outputFile == null && pdfFile.length() > 4)
                {
                    outputFile = new File(pdfFile.substring(0, pdfFile.length() - 4) + ".xml").getAbsolutePath();
                }
                document = PDDocument.load(new File(pdfFile), password);

                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata meta;
                if (page == 0)
                {
                    meta = catalog.getMetadata();
                }
                else
                {
                    if (page > document.getNumberOfPages())
                    {
                        System.err.println("Page " + page + " doesn't exist");
                        System.exit(1);
                    }
                    meta = document.getPage(page - 1).getMetadata();
                }
                if (meta == null)
                {
                    System.err.println("No XMP metadata available");
                    System.exit(1);
                }
                if (toConsole)
                {
                    output = System.out;
                }
                else
                {
                    output = new PrintStream(outputFile);
                }
                output.write(meta.toByteArray());
                output.close();
            }
            finally
            {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(document);
            }
        }
        System.exit(0);
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar ExtractXMP [options] <inputfile> [output-text-file]\n"
            + "\nOptions:\n"
            + "  -password <password>        : Password to decrypt document\n"
            + "  -console                    : Send text to console instead of file\n"
            + "  -page <number>              : The optional page to extract XMP (1 based)\n"
            + "  <inputfile>                 : The PDF document to use\n"
            + "  [output-xml-file]           : The file to write the XMP to";
        System.err.println(message);
        System.exit( 1 );
    }
    
}
