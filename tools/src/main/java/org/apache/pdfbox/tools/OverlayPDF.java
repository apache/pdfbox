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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.Overlay.Position;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * 
 * Adds an overlay to an existing PDF document.
 *  
 * Based on code contributed by Balazs Jerk. 
 * 
 */
public final class OverlayPDF 
{
    private static final Log LOG = LogFactory.getLog(OverlayPDF.class);

    // Command line options
    private static final String POSITION = "-position";
    private static final String ODD = "-odd";
    private static final String EVEN = "-even";
    private static final String FIRST = "-first";
    private static final String LAST = "-last";
    private static final String PAGE = "-page";
    private static final String USEALLPAGES = "-useAllPages";

    private OverlayPDF()
    {
    }    
    
    /**
     * This will overlay a document and write out the results.
     *
     * @param args command line arguments
     * @throws IOException if something went wrong
     */
    public static void main(final String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        String outputFilename = null;
        Overlay overlayer = new Overlay();
        Map<Integer, String> specificPageOverlayFile = new HashMap<Integer, String>();
        // input arguments
        for (int i = 0; i < args.length; i++) 
        {
            String arg = args[i].trim();
            if (i == 0) 
            {
                overlayer.setInputFile(arg);
            } 
            else if (i == (args.length - 1)) 
            {
                outputFilename = arg;
            } 
            else if (arg.equals(POSITION) && ((i + 1) < args.length)) 
            {
                if (Position.FOREGROUND.toString().equalsIgnoreCase(args[i + 1].trim())) 
                {
                    overlayer.setOverlayPosition(Position.FOREGROUND);
                }
                else if (Position.BACKGROUND.toString().equalsIgnoreCase(args[i + 1].trim())) 
                {
                    overlayer.setOverlayPosition(Position.BACKGROUND);
                }
                else
                {
                    usage();
                }
                i += 1;
            } 
            else if (arg.equals(ODD) && ((i + 1) < args.length)) 
            {
                overlayer.setOddPageOverlayFile(args[i + 1].trim());
                i += 1;
            } 
            else if (arg.equals(EVEN) && ((i + 1) < args.length)) 
            {
                overlayer.setEvenPageOverlayFile(args[i + 1].trim());
                i += 1;
            } 
            else if (arg.equals(FIRST) && ((i + 1) < args.length)) 
            {
                overlayer.setFirstPageOverlayFile(args[i + 1].trim());
                i += 1;
            } 
            else if (arg.equals(LAST) && ((i + 1) < args.length)) 
            {
                overlayer.setLastPageOverlayFile(args[i + 1].trim());
                i += 1;
            } 
            else if (arg.equals(USEALLPAGES) && ((i + 1) < args.length)) 
            {
                overlayer.setAllPagesOverlayFile(args[i + 1].trim());
                i += 1;
            } 
            else if (arg.equals(PAGE) && ((i + 2) < args.length) && (isInteger(args[i + 1].trim()))) 
            {
                specificPageOverlayFile.put(Integer.parseInt(args[i + 1].trim()), args[i + 2].trim());
                i += 2;
            } 
            else if (overlayer.getDefaultOverlayFile() == null) 
            {
                overlayer.setDefaultOverlayFile(arg);
            } 
            else 
            {
                usage();
            }
        }
        
        if (overlayer.getInputFile() == null || outputFilename == null) 
        {
            usage();
        }
        
        try 
        {
            PDDocument result = overlayer.overlay(specificPageOverlayFile);
            result.save(outputFilename);
            result.close();
            // close the input files AFTER saving the resulting file as some 
            // streams are shared among the input and the output files
            overlayer.close();
        } 
        catch (IOException e) 
        {
            LOG.error("Overlay failed: " + e.getMessage(), e);
            throw e;
        }
    }

    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar OverlayPDF <inputfile> [options] <outputfile>\n"
                + "\nOptions:\n"
                + "  <inputfile>                                  : input file\n"
                + "  <defaultOverlay.pdf>                         : default overlay file\n"
                + "  -odd <oddPageOverlay.pdf>                    : overlay file used for odd pages\n"
                + "  -even <evenPageOverlay.pdf>                  : overlay file used for even pages\n"
                + "  -first <firstPageOverlay.pdf>                : overlay file used for the first page\n"
                + "  -last <lastPageOverlay.pdf>                  : overlay file used for the last page\n"
                + "  -useAllPages <allPagesOverlay.pdf>           : overlay file used for overlay, all pages"
                + " are used by simply repeating them\n"
                + "  -page <pageNumber> <specificPageOverlay.pdf> : overlay file used for "
                + "the given page number, may occur more than once\n"
                + "  -position foreground|background              : where to put the overlay "
                + "file: foreground or background\n"
                + "  <outputfile>                                 : output file";

        System.err.println(message);
        System.exit( 1 );
    }

    private static boolean isInteger(String str) 
    {
        try 
        {
            Integer.parseInt(str);
        } 
        catch (NumberFormatException nfe) 
        {
            return false;
        }
        return true;
    }

}
