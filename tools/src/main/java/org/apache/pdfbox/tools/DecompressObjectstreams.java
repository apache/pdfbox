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
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * This program will just take all of the stream objects in a PDF and dereference
 * them.  The streams will be gone in the resulting file and the objects will be
 * present.  This is very helpful when trying to debug problems as it'll make
 * it possible to easily look through a PDF using a text editor.  It also exposes
 * problems which stem from objects inside object streams overwriting other
 * objects.
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

        try (PDDocument doc = PDFParser.load(new File(inputFilename)))
        {
            // It is sufficient to simply write the loaded pdf without further processing
            // as PDFBox doesn't support writing compressed object streams

            // Nevertheless so following code shows how to derefence all objects within a stream
            // and remove the stream objects itself
            COSDocument cosDocument = doc.getDocument();
            // collect all objects related to an object stream (offset < 0)
            List<Entry<COSObjectKey, Long>> streamObjects = cosDocument.getXrefTable().entrySet()
                    .stream().filter(e -> e.getValue() < 0L).collect(Collectors.toList());

            // dereference objects within a stream
            streamObjects.stream() //
                    .map(Entry::getKey) //
                    .forEach(key -> cosDocument.getObjectFromPool(key).getObject());

            // remove objects containing an object stream
            streamObjects.stream() //
                    .map(Entry::getValue) //
                    .distinct() //
                    .forEach(off -> cosDocument.removeObject(new COSObjectKey(-off, 0)));

            doc.save(outputFilename);
        }
        catch(Exception e) 
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
