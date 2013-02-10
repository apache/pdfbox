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
package org.apache.pdfbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.BaseParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.MapUtil;

/**
 * Adds an overlay to an existing PDF document.
 *  
 * Based on code contributed by Balazs Jerk. 
 * 
 */
public class OverlayPDF 
{
    /**
     *   Possible loacation of the overlayed pages: foreground or background.
     */
    private enum Position 
    { 
        FOREGROUND, BACKGROUND 
    };

    private static final Log LOG = LogFactory.getLog(BaseParser.class);

    private static final String XOBJECT_PREFIX = "OL";
    
    private LayoutPage defaultOverlayPage;
    private LayoutPage firstPageOverlayPage;
    private LayoutPage lastPageOverlayPage;
    private LayoutPage oddPageOverlayPage;
    private LayoutPage evenPageOverlayPage;

    private static Map<Integer, PDDocument> specificPageOverlay = new HashMap<Integer, PDDocument>();
    private static Map<Integer, LayoutPage> specificPageOverlayPage = new HashMap<Integer, LayoutPage>();

    private static Position overlayPosition = Position.BACKGROUND;

    private static boolean useNonSeqParser = false;
    private static String inputFile = null;
    private static String outputFile = null;
    private static String defaultOverlayFile = null;
    private static String firstPageOverlayFile = null;
    private static String lastPageOverlayFile = null;
    private static String oddPageOverlayFile = null;
    private static String evenPageOverlayFile = null;
    
    // Command line options
    private static final String POSITION = "-position";
    private static final String ODD = "-odd";
    private static final String EVEN = "-even";
    private static final String FIRST = "-first";
    private static final String LAST = "-last";
    private static final String PAGE = "-page";
    private static final String NONSEQ = "-nonSeq";

    /**
     * This will overlay a document and write out the results.
     *
     * @param args command line arguments
     * @throws Exception if something went wrong
     * @see #USAGE
     */
    public static void main(final String[] args) throws Exception 
    {
        Map<Integer, String> specificPageOverlayFile = new HashMap<Integer, String>();
        
        // input arguments
        for (int i = 0; i < args.length; i++) 
        {
            String arg = args[i].trim();
            if (i == 0) 
            {
                inputFile = arg;
            } 
            else if (i == (args.length - 1)) 
            {
                outputFile = arg;
            } 
            else if (arg.equals(POSITION) && ((i + 1) < args.length)) 
            {
                if (Position.FOREGROUND.toString().equalsIgnoreCase(args[i + 1].trim())) 
                {
                    overlayPosition = Position.FOREGROUND;
                }
                else if (Position.BACKGROUND.toString().equalsIgnoreCase(args[i + 1].trim())) 
                {
                    overlayPosition = Position.BACKGROUND;
                }
                else
                {
                    usage();
                }
                i += 1;
            } 
            else if (arg.equals(ODD) && ((i + 1) < args.length)) 
            {
                oddPageOverlayFile = args[i + 1].trim();
                i += 1;
            } 
            else if (arg.equals(EVEN) && ((i + 1) < args.length)) 
            {
                evenPageOverlayFile = args[i + 1].trim();
                i += 1;
            } 
            else if (arg.equals(FIRST) && ((i + 1) < args.length)) 
            {
                firstPageOverlayFile = args[i + 1].trim();
                i += 1;
            } 
            else if (arg.equals(LAST) && ((i + 1) < args.length)) 
            {
                lastPageOverlayFile = args[i + 1].trim();
                i += 1;
            } 
            else if (arg.equals(PAGE) && ((i + 2) < args.length) && (isInteger(args[i + 1].trim()))) 
            {
                specificPageOverlayFile.put(Integer.parseInt(args[i + 1].trim()), args[i + 2].trim());
                i += 2;
            } 
            else if( args[i].equals( NONSEQ ) )
            {
                useNonSeqParser = true;
            }
            else if (defaultOverlayFile == null) 
            {
                defaultOverlayFile = arg;
            } 
            else 
            {
                usage();
            }
        }
        
        if ((inputFile == null) || (outputFile == null)) 
        {
            usage();
        }
        
        try 
        {
            OverlayPDF overlayer = new OverlayPDF();
            overlayer.overlay(specificPageOverlayFile);
        } 
        catch (Exception e) 
        {
            LOG.error("Overlay failed: " + e.getMessage(), e);
            throw e;
        }
    }

    private static void usage()
    {
        StringBuilder message = new StringBuilder();
        message.append("usage: java -jar pdfbox-app-x.y.z.jar OverlayPDF <input.pdf> [OPTIONS] <output.pdf>\n");
        message.append("  <input.pdf>                                        input file\n");
        message.append("  <defaultOverlay.pdf>                               default overlay file\n");
        message.append("  -odd <oddPageOverlay.pdf>                          overlay file used for odd pages\n");
        message.append("  -even <evenPageOverlay.pdf>                        overlay file used for even pages\n");
        message.append("  -first <firstPageOverlay.pdf>                      overlay file used for the first page\n");
        message.append("  -last <lastPageOverlay.pdf>                        overlay file used for the last page\n");
        message.append("  -page <pageNumber> <specificPageOverlay.pdf>       overlay file used for " +
                "the given page number, may occur more than once\n");
        message.append("  -position foreground|background                    where to put the overlay " +
                "file: foreground or background\n");
        message.append("  -nonSeq                                            enables the new non-sequential parser\n");
        message.append("  <output.pdf>                                       output file\n");
        System.err.println(message.toString());
        System.exit( 1 );
    }

    /**
     * This will add overlays to a documents.
     *
     * @param specificPageOverlayFile map of overlay files for specific pages
     * @throws IOException exception
     * @throws COSVisitorException exception
     */
    public void overlay(Map<Integer, String> specificPageOverlayFile) throws IOException, COSVisitorException 
    {
        PDDocument sourcePDFDocument = null;
        PDDocument defaultOverlay = null;
        PDDocument firstPageOverlay = null;
        PDDocument lastPageOverlay = null;
        PDDocument oddPageOverlay = null;
        PDDocument evenPageOverlay = null;
        try 
        {
            sourcePDFDocument = PDDocument.load(inputFile);
            if (defaultOverlayFile != null) 
            {
                if (useNonSeqParser)
                {
                    defaultOverlay = PDDocument.loadNonSeq(new File(defaultOverlayFile), null);
                }
                else
                {
                    defaultOverlay = PDDocument.load(defaultOverlayFile);
                }
                defaultOverlayPage = getLayoutPage(defaultOverlay); 
            }
            if (firstPageOverlayFile != null) 
            {
                if (useNonSeqParser)
                {
                    firstPageOverlay = PDDocument.loadNonSeq(new File(firstPageOverlayFile), null);
                }
                else
                {
                    firstPageOverlay = PDDocument.load(firstPageOverlayFile);
                }
                firstPageOverlayPage = getLayoutPage(firstPageOverlay); 
            }
            if (lastPageOverlayFile != null) 
            {
                if (useNonSeqParser)
                {
                    lastPageOverlay = PDDocument.loadNonSeq(new File(lastPageOverlayFile), null);
                }
                else
                {
                    lastPageOverlay = PDDocument.load(lastPageOverlayFile);
                }
                lastPageOverlayPage = getLayoutPage(lastPageOverlay); 
            }
            if (oddPageOverlayFile != null) 
            {
                if (useNonSeqParser)
                {
                    oddPageOverlay = PDDocument.loadNonSeq(new File(oddPageOverlayFile), null);
                }
                else
                {
                    oddPageOverlay = PDDocument.load(oddPageOverlayFile);
                }
                oddPageOverlayPage = getLayoutPage(oddPageOverlay); 
            }
            if (evenPageOverlayFile != null) 
            {
                if (useNonSeqParser)
                {
                    evenPageOverlay = PDDocument.loadNonSeq(new File(evenPageOverlayFile), null);
                }
                else
                {
                    evenPageOverlay = PDDocument.load(evenPageOverlayFile);
                }
                evenPageOverlayPage = getLayoutPage(evenPageOverlay); 
            }
            for (Map.Entry<Integer, String> e : specificPageOverlayFile.entrySet()) 
            {
                PDDocument doc = null;
                if (useNonSeqParser)
                {
                    doc = PDDocument.loadNonSeq(new File(e.getValue()), null);
                }
                else
                {
                    doc = PDDocument.load(e.getValue());
                }
                specificPageOverlay.put(e.getKey(), doc);
                specificPageOverlayPage.put(e.getKey(), getLayoutPage(doc)); 
            }
            PDDocumentCatalog pdfCatalog = sourcePDFDocument.getDocumentCatalog();
            processPages(pdfCatalog.getAllPages());

            sourcePDFDocument.save(outputFile);
        } 
        finally 
        {
            if (sourcePDFDocument != null) 
            {
                sourcePDFDocument.close();
            }
            if (defaultOverlay != null) 
            {
                defaultOverlay.close();
            }
            if (firstPageOverlay != null) 
            {
                firstPageOverlay.close();
            }
            if (lastPageOverlay != null) 
            {
                lastPageOverlay.close();
            }
            if (oddPageOverlay != null) 
            {
                oddPageOverlay.close();
            }
            if (evenPageOverlay != null) 
            {
                evenPageOverlay.close();
            }
            for (Map.Entry<Integer, PDDocument> e : specificPageOverlay.entrySet()) 
            {
                e.getValue().close();
            }
            specificPageOverlay.clear();
            specificPageOverlayPage.clear();
        }
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

    /**
     * Stores the overlay page information.
     */
    private static class LayoutPage 
    {
        private final PDRectangle overlayMediaBox;
        private final COSStream overlayContentStream;
        private final COSDictionary overlayResources;

        private LayoutPage(PDRectangle mediaBox, COSStream contentStream, COSDictionary resources) 
        {
            overlayMediaBox = mediaBox;
            overlayContentStream = contentStream;
            overlayResources = resources;
        }
    }

    private LayoutPage getLayoutPage(PDDocument doc) throws IOException 
    {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDPage page = (PDPage)catalog.getAllPages().get(0);
        COSBase contents = page.getCOSDictionary().getDictionaryObject(COSName.CONTENTS);
        PDResources resources = page.findResources();
        if (resources == null) 
        {
            resources = new PDResources();
        }
        return new LayoutPage(page.getMediaBox(), createContentStream(contents), resources.getCOSDictionary());
    }
    
    private COSStream createContentStream(COSBase contents) throws IOException 
    {
        List<COSStream> contentStreams = createContentStreamList(contents);
        // concatenate streams
        COSStream concatStream = new COSStream(new RandomAccessBuffer());
        OutputStream out = concatStream.createUnfilteredStream();
        for (COSStream contentStream : contentStreams) 
        {
            InputStream in = contentStream.getUnfilteredStream();
            byte[] buf = new byte[2048];
            int n;
            while ((n = in.read(buf)) > 0) 
            {
                out.write(buf, 0, n);
            }
            out.flush();
        }
        out.close();
        concatStream.setFilters(COSName.FLATE_DECODE);
        return concatStream;
    }
    
    private List<COSStream> createContentStreamList(COSBase contents) throws IOException 
    {
        List<COSStream> contentStreams = new ArrayList<COSStream>();
        if (contents instanceof COSStream) 
        {
            contentStreams.add((COSStream) contents);
        } 
        else if (contents instanceof COSArray) 
        {
            for (COSBase item : (COSArray) contents) 
            {
                contentStreams.addAll(createContentStreamList(item));
            }
        } 
        else if (contents instanceof COSObject) 
        {
            contentStreams.addAll(createContentStreamList(((COSObject) contents).getObject()));
        } 
        else 
        {
            throw new IOException("Contents are unknown type:" + contents.getClass().getName());
        }
        return contentStreams;
    }
    
    private void processPages(List<?> pages) throws IOException 
    {
        int pageCount = 0;
        for (Object pageObject : pages) 
        {
            PDPage page = (PDPage) pageObject;
            COSDictionary pageDictionary = page.getCOSDictionary();
            COSBase contents = pageDictionary.getDictionaryObject(COSName.CONTENTS);
            COSArray contentArray = new COSArray();
            switch (overlayPosition) 
            {
            case FOREGROUND:
                // save state
                contentArray.add(createStream("q\n"));
                // original content
                if (contents instanceof COSStream) 
                {
                    contentArray.add(contents);
                } 
                else if (contents instanceof COSArray) 
                {
                    contentArray.addAll((COSArray) contents);
                } 
                else 
                {
                    throw new IOException("Unknown content type:" + contents.getClass().getName());
                }
                // restore state
                contentArray.add(createStream("Q\n"));
                // overlay content
                overlayPage(contentArray, page, pageCount + 1, pages.size());
                break;
            case BACKGROUND:
                // overlay content
                overlayPage(contentArray, page, pageCount + 1, pages.size());
                // original content
                if (contents instanceof COSStream) 
                {
                    contentArray.add(contents);
                } 
                else if (contents instanceof COSArray) 
                {
                    contentArray.addAll((COSArray) contents);
                } 
                else 
                {
                    throw new IOException("Unknown content type:" + contents.getClass().getName());
                }
                break;
            default:
                throw new IOException("Unknown type of position:" + overlayPosition);
            }
            pageDictionary.setItem(COSName.CONTENTS, contentArray);
            pageCount++;
        }
    }

    private void overlayPage(COSArray array, PDPage page, int pageNumber, int numberOfPages) throws IOException 
    {
        LayoutPage layoutPage = null;
        if (specificPageOverlayPage.containsKey(pageNumber)) 
        {
            layoutPage = specificPageOverlayPage.get(pageNumber);
        } 
        else if ((pageNumber == 1) && (firstPageOverlayPage != null)) 
        {
            layoutPage = firstPageOverlayPage;
        } 
        else if ((pageNumber == numberOfPages) && (lastPageOverlayPage != null)) 
        {
            layoutPage = lastPageOverlayPage;
        } 
        else if ((pageNumber % 2 == 1) && (oddPageOverlayPage != null)) 
        {
            layoutPage = oddPageOverlayPage;
        } 
        else if ((pageNumber % 2 == 0) && (evenPageOverlayPage != null)) 
        {
            layoutPage = evenPageOverlayPage;
        } 
        else if (defaultOverlayPage != null) 
        {
            layoutPage = defaultOverlayPage;
        }
        if (layoutPage != null) 
        {
            PDResources resources = page.findResources();
            if (resources == null) 
            {
                resources = new PDResources();
                page.setResources(resources);
            }
            String xObjectId = createOverlayXObject(page, layoutPage, layoutPage.overlayContentStream);
            array.add(createOverlayStream(page, layoutPage, xObjectId));
        }
    }
    
    private String createOverlayXObject(PDPage page, LayoutPage layoutPage, COSStream contentStream) 
    {
        PDResources resources = page.findResources();
        // determine new ID
        COSDictionary dict = (COSDictionary) resources.getCOSDictionary().getDictionaryObject(COSName.XOBJECT);
        if (dict == null) 
        {
            dict = new COSDictionary();
            resources.getCOSDictionary().setItem(COSName.XOBJECT, dict);
        }
        String xObjectId = MapUtil.getNextUniqueKey( resources.getXObjects(), XOBJECT_PREFIX );

        // wrap the layout content in a BBox and add it to page
        COSStream xobj = contentStream;
        xobj.setItem(COSName.RESOURCES, layoutPage.overlayResources);
        xobj.setItem(COSName.TYPE, COSName.XOBJECT);
        xobj.setItem(COSName.SUBTYPE, COSName.FORM);
        xobj.setInt(COSName.FORMTYPE, 1);
        COSArray matrix = new COSArray();
        matrix.add(COSInteger.get(1));
        matrix.add(COSInteger.get(0));
        matrix.add(COSInteger.get(0));
        matrix.add(COSInteger.get(1));
        matrix.add(COSInteger.get(0));
        matrix.add(COSInteger.get(0));
        xobj.setItem(COSName.MATRIX, matrix);
        COSArray bbox = new COSArray();
        bbox.add(COSInteger.get(0));
        bbox.add(COSInteger.get(0));
        bbox.add(COSInteger.get((int) layoutPage.overlayMediaBox.getWidth()));
        bbox.add(COSInteger.get((int) layoutPage.overlayMediaBox.getHeight()));
        xobj.setItem(COSName.BBOX, bbox);
        dict.setItem(xObjectId, xobj);
        
        return xObjectId;
    }

    private COSStream createOverlayStream(PDPage page, LayoutPage layoutPage, String xObjectId) throws IOException 
    {
        // create a new content stream that executes the XObject content
        PDRectangle pageMediaBox = page.getMediaBox();
        float scale = 1;
        float hShift = (pageMediaBox.getWidth() - layoutPage.overlayMediaBox.getWidth()) / 2.0f;
        float vShift = (pageMediaBox.getHeight() - layoutPage.overlayMediaBox.getHeight()) / 2.0f;
        return createStream("q\nq " + scale + " 0 0 " + scale + " " + hShift + " " + vShift + " cm /" 
                + xObjectId + " Do Q\nQ\n");
    }

    private COSStream createStream(String content) throws IOException 
    {
        COSStream stream = new COSStream(new RandomAccessBuffer());
        OutputStream out = stream.createUnfilteredStream();
        out.write(content.getBytes("ISO-8859-1"));
        out.close();
        stream.setFilters(COSName.FLATE_DECODE);
        return stream;
    }
    
}
