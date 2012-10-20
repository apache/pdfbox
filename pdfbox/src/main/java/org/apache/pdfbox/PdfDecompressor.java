/*
 *  Copyright 2010 adam.
 * 
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

package org.apache.pdfbox;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFObjectStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This program will just take all of the stream objects in a PDF and dereference
 * them.  The streams will be gone in the resulting file and the objects will be
 * present.  This is very helpful when trying to debug problems as it'll make
 * it possible to easily look through a PDF using a text editor.  It also exposes
 * problems which stem from objects inside object streams overwriting other
 * objects.
 * @author <a href="adam@apache.org">Adam Nichols</a>
 */
public class PdfDecompressor {

    /**
     * This is a very simple program, so everything is in the main method.
     * @param args arguments to the program
     */
    public static void main(String[] args) {
        if(args.length < 1)
            usage();

        String inputFilename = args[0];
        String outputFilename;
        if(args.length > 1) {
            outputFilename = args[1];
        } else {
            if(inputFilename.matches(".*\\.[pP][dD][fF]$"))
                outputFilename = inputFilename.replaceAll("\\.[pP][dD][fF]$", ".unc.pdf");
            else
                outputFilename = inputFilename + ".unc.pdf";
        }

        PDDocument doc = null;
        try {
            doc = PDDocument.load(inputFilename);
            for(COSObject objStream : doc.getDocument().getObjectsByType("ObjStm")) {
                COSStream stream = (COSStream)objStream.getObject();
                PDFObjectStreamParser sp = new PDFObjectStreamParser(stream, doc.getDocument());
                sp.parse();
                for(COSObject next : sp.getObjects()) {
                    COSObjectKey key = new COSObjectKey(next);
                    COSObject obj = doc.getDocument().getObjectFromPool(key);
                    obj.setObject(next.getObject());
                }
                doc.getDocument().removeObject(new COSObjectKey(objStream));
            }
            doc.save(outputFilename);
        } catch(Exception e) {
            System.out.println("Error processing file: " + e.getMessage());
        } finally {
            if(doc != null)
                try { doc.close(); } catch(Exception e) { }
        }
    }

    /**
     * Explains how to use the program.
     */
    private static void usage() {
        System.err.println( "Usage: java -cp /path/to/pdfbox.jar;/path/to/commons-logging-api.jar "
                + "org.apache.pdfbox.PdfDecompressor <input PDF File> [<Output PDF File>]\n"
                + "  <input PDF File>       The PDF document to decompress\n"
                + "  <output PDF File>      The output filename (default is to replace .pdf with .unc.pdf)");
        System.exit(1);
    }
}
