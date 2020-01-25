/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This program will just save the loaded pdf without any changes. As PDFBox doesn't support writing compressed object
 * streams those streams are stripped and will be gone in the resulting file. This is very helpful when trying to debug
 * problems as it'll make it possible to easily look through a PDF using a text editor. It also exposes problems which
 * stem from objects inside object streams overwriting other objects.
 * 
 * @author Adam Nichols
 */
public final class DecompressObjectstreams 
{
    
    /**
     * private constructor.
     */
    private DecompressObjectstreams()
    {
    }

    /**
     * This is a very simple program, so everything is in the main method.
     * @param args arguments to the program
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        if(args.length < 1)
        {
            usage();
        }

        String inputFilename = args[0];
        String outputFilename;
        if(args.length > 1)
        {
            outputFilename = args[1];
        }
        else
        {
            if(inputFilename.matches(".*\\.[pP][dD][fF]$"))
            {
                outputFilename = inputFilename.replaceAll("\\.[pP][dD][fF]$", ".unc.pdf");
            }
            else
            {
                outputFilename = inputFilename + ".unc.pdf";
            }
        }

        try (PDDocument doc = Loader.loadPDF(new File(inputFilename)))
        {
            // It is sufficient to simply write the loaded pdf without further processing.
            // As PDFBox doesn't support writing compressed object streams that streams will
            // be simply omitted
            doc.save(outputFilename);
        }
        catch (IOException e)
        {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Explains how to use the program.
     */
    private static void usage()
    {
        String message = "Usage: java -cp pdfbox-app-x.y.z.jar "
                + "org.apache.pdfbox.tools.DecompressObjectstreams <inputfile> [<outputfile>]\n"
                + "\nOptions:\n"
                + "  <inputfile>  : The PDF document to decompress\n"
                + "  <outputfile> : The output filename (default is to replace .pdf with .unc.pdf)";
        
        System.err.println(message);
        System.exit(1);
    }
}
