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
package org.apache.pdfbox.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Adds an overlay to an existing PDF document.
 * 
 * Based on code contributed by Balazs Jerk.
 * 
 */
public class Overlay
{
    /**
     * Possible loacation of the overlayed pages: foreground or background.
     */
    public enum Position
    {
        FOREGROUND, BACKGROUND
    };

    private static final String XOBJECT_PREFIX = "OL";

    private LayoutPage defaultOverlayPage;
    private LayoutPage firstPageOverlayPage;
    private LayoutPage lastPageOverlayPage;
    private LayoutPage oddPageOverlayPage;
    private LayoutPage evenPageOverlayPage;

    private Map<Integer, PDDocument> specificPageOverlay = new HashMap<Integer, PDDocument>();
    private Map<Integer, LayoutPage> specificPageOverlayPage = new HashMap<Integer, LayoutPage>();

    private Position position = Position.BACKGROUND;

    private String inputFileName = null;
    private String outputFilename = null;
    private String defaultOverlayFilename = null;
    private String firstPageOverlayFilename = null;
    private String lastPageOverlayFilename = null;
    private String oddPageOverlayFilename = null;
    private String evenPageOverlayFilename = null;

    /**
     * This will add overlays to a documents.
     * 
     * @param specificPageOverlayFile map of overlay files for specific pages
     * @param useNonSeqParser indicates whether the nonsequential parser is used
     * @throws IOException if something went wrong
     */
    public void overlay(Map<Integer, String> specificPageOverlayFile, boolean useNonSeqParser)
            throws IOException
    {
        PDDocument sourcePDFDocument = null;
        PDDocument defaultOverlay = null;
        PDDocument firstPageOverlay = null;
        PDDocument lastPageOverlay = null;
        PDDocument oddPageOverlay = null;
        PDDocument evenPageOverlay = null;
        try
        {
            sourcePDFDocument = PDDocument.load(inputFileName);
            if (defaultOverlayFilename != null)
            {
                defaultOverlay = loadPDF(defaultOverlayFilename, useNonSeqParser);
                defaultOverlayPage = getLayoutPage(defaultOverlay);
            }
            if (firstPageOverlayFilename != null)
            {
                firstPageOverlay = loadPDF(firstPageOverlayFilename, useNonSeqParser);
                firstPageOverlayPage = getLayoutPage(firstPageOverlay);
            }
            if (lastPageOverlayFilename != null)
            {
                lastPageOverlay = loadPDF(lastPageOverlayFilename, useNonSeqParser);
                lastPageOverlayPage = getLayoutPage(lastPageOverlay);
            }
            if (oddPageOverlayFilename != null)
            {
                oddPageOverlay = loadPDF(oddPageOverlayFilename, useNonSeqParser);
                oddPageOverlayPage = getLayoutPage(oddPageOverlay);
            }
            if (evenPageOverlayFilename != null)
            {
                if (useNonSeqParser)
                {
                    evenPageOverlay = PDDocument.loadNonSeq(new File(evenPageOverlayFilename), null);
                }
                else
                {
                    evenPageOverlay = PDDocument.load(evenPageOverlayFilename);
                }
                evenPageOverlay = loadPDF(evenPageOverlayFilename, useNonSeqParser);
                evenPageOverlayPage = getLayoutPage(evenPageOverlay);
            }
            for (Map.Entry<Integer, String> e : specificPageOverlayFile.entrySet())
            {
                PDDocument doc = loadPDF(e.getValue(), useNonSeqParser);
                specificPageOverlay.put(e.getKey(), doc);
                specificPageOverlayPage.put(e.getKey(), getLayoutPage(doc));
            }
            PDDocumentCatalog pdfCatalog = sourcePDFDocument.getDocumentCatalog();
            processPages(pdfCatalog.getAllPages());

            sourcePDFDocument.save(outputFilename);
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

    private PDDocument loadPDF(String pdfName, boolean useNonSeqParser) throws IOException
    {
        PDDocument pdf = null;
        if (useNonSeqParser)
        {
            pdf = PDDocument.loadNonSeq(new File(pdfName), null);
        }
        else
        {
            pdf = PDDocument.load(pdfName);
        }
        return pdf;
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
        PDPage page = (PDPage) catalog.getAllPages().get(0);
        COSBase contents = page.getCOSDictionary().getDictionaryObject(COSName.CONTENTS);
        PDResources resources = page.findResources();
        if (resources == null)
        {
            resources = new PDResources();
        }
        return new LayoutPage(page.getMediaBox(), createContentStream(contents),
                resources.getCOSDictionary());
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

    private void processPages(List<PDPage> pages) throws IOException
    {
        int pageCount = 0;
        for (PDPage page : pages)
        {
            COSDictionary pageDictionary = page.getCOSDictionary();
            COSBase contents = pageDictionary.getDictionaryObject(COSName.CONTENTS);
            COSArray contentArray = new COSArray();
            switch (position)
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
                throw new IOException("Unknown type of position:" + position);
            }
            pageDictionary.setItem(COSName.CONTENTS, contentArray);
            pageCount++;
        }
    }

    private void overlayPage(COSArray array, PDPage page, int pageNumber, int numberOfPages)
            throws IOException
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
            String xObjectId = createOverlayXObject(page, layoutPage,
                    layoutPage.overlayContentStream);
            array.add(createOverlayStream(page, layoutPage, xObjectId));
        }
    }

    private String createOverlayXObject(PDPage page, LayoutPage layoutPage, COSStream contentStream)
    {
        PDResources resources = page.findResources();
        // determine new ID
        COSDictionary dict = (COSDictionary) resources.getCOSDictionary().getDictionaryObject(
                COSName.XOBJECT);
        if (dict == null)
        {
            dict = new COSDictionary();
            resources.getCOSDictionary().setItem(COSName.XOBJECT, dict);
        }
        String xObjectId = getNextUniqueKey(resources.getXObjects(), XOBJECT_PREFIX);

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

    private static String getNextUniqueKey(Map<String, ?> map, String prefix)
    {
        int counter = 0;
        while (map != null && map.get(prefix + counter) != null)
        {
            counter++;
        }
        return prefix + counter;
    }

    private COSStream createOverlayStream(PDPage page, LayoutPage layoutPage, String xObjectId)
            throws IOException
    {
        // create a new content stream that executes the XObject content
        PDRectangle pageMediaBox = page.getMediaBox();
        float scale = 1;
        float hShift = (pageMediaBox.getWidth() - layoutPage.overlayMediaBox.getWidth()) / 2.0f;
        float vShift = (pageMediaBox.getHeight() - layoutPage.overlayMediaBox.getHeight()) / 2.0f;
        return createStream("q\nq " + scale + " 0 0 " + scale + " " + hShift + " " + vShift
                + " cm /" + xObjectId + " Do Q\nQ\n");
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

    /**
     * Sets the overlay position.
     * 
     * @param overlayPosition the overlay position
     */
    public void setOverlayPosition(Position overlayPosition)
    {
        position = overlayPosition;
    }

    /**
     * Sets the file to be overlayed.
     * 
     * @param inputFile the file to be overlayed
     */
    public void setInputFile(String inputFile)
    {
        inputFileName = inputFile;
    }

    /**
     * Returns the input file.
     * 
     * @return the input file
     */
    public String getInputFile()
    {
        return inputFileName;
    }

    /**
     * Sets the output file.
     * 
     * @param outputFile the output file
     */
    public void setOutputFile(String outputFile)
    {
        outputFilename = outputFile;
    }

    /**
     * Returns the output file.
     * 
     * @return the output file
     */
    public String getOutputFile()
    {
        return outputFilename;
    }

    /**
     * Sets the default overlay file.
     * 
     * @param defaultOverlayFile the default overlay file
     */
    public void setDefaultOverlayFile(String defaultOverlayFile)
    {
        defaultOverlayFilename = defaultOverlayFile;
    }

    /**
     * Returns the default overlay file.
     * 
     * @return the default overlay file
     */
    public String getDefaultOverlayFile()
    {
        return defaultOverlayFilename;
    }

    /**
     * Sets the first page overlay file.
     * 
     * @param firstPageOverlayFile the first page overlay file
     */
    public void setFirstPageOverlayFile(String firstPageOverlayFile)
    {
        firstPageOverlayFilename = firstPageOverlayFile;
    }

    /**
     * Sets the last page overlay file.
     * 
     * @param lastPageOverlayFile the last page overlay file
     */
    public void setLastPageOverlayFile(String lastPageOverlayFile)
    {
        lastPageOverlayFilename = lastPageOverlayFile;
    }

    /**
     * Sets the odd page overlay file.
     * 
     * @param oddPageOverlayFile the odd page overlay file
     */
    public void setOddPageOverlayFile(String oddPageOverlayFile)
    {
        oddPageOverlayFilename = oddPageOverlayFile;
    }

    /**
     * Sets the even page overlay file.
     * 
     * @param evenPageOverlayFile the even page overlay file
     */
    public void setEvenPageOverlayFile(String evenPageOverlayFile)
    {
        evenPageOverlayFilename = evenPageOverlayFile;
    }

}
