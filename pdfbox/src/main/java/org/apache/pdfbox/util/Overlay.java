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

import java.awt.geom.AffineTransform;
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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

/**
 * Adds an overlay to an existing PDF document.
 * 
 * Based on code contributed by Balazs Jerk.
 * 
 */
public class Overlay
{
    /**
     * Possible location of the overlayed pages: foreground or background.
     */
    public enum Position
    {
        FOREGROUND, BACKGROUND
    };

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
    private String allPagesOverlayFilename = null;
    private String oddPageOverlayFilename = null;
    private String evenPageOverlayFilename = null;
    
    private int numberOfOverlayPages = 0;
    private boolean useAllOverlayPages = false;

    /**
     * This will add overlays to a documents.
     * 
     * @param specificPageOverlayFile map of overlay files for specific pages
     * @throws IOException if something went wrong
     */
    public void overlay(Map<Integer, String> specificPageOverlayFile)
            throws IOException
    {
        PDDocument sourcePDFDocument = null;
        PDDocument defaultOverlay = null;
        PDDocument firstPageOverlay = null;
        PDDocument lastPageOverlay = null;
        PDDocument allPagesOverlay = null;
        PDDocument oddPageOverlay = null;
        PDDocument evenPageOverlay = null;
        try
        {
            sourcePDFDocument = loadPDF(inputFileName);
            if (defaultOverlayFilename != null)
            {
                defaultOverlay = loadPDF(defaultOverlayFilename);
                defaultOverlayPage = getLayoutPage(defaultOverlay);
            }
            if (firstPageOverlayFilename != null)
            {
                firstPageOverlay = loadPDF(firstPageOverlayFilename);
                firstPageOverlayPage = getLayoutPage(firstPageOverlay);
            }
            if (lastPageOverlayFilename != null)
            {
                lastPageOverlay = loadPDF(lastPageOverlayFilename);
                lastPageOverlayPage = getLayoutPage(lastPageOverlay);
            }
            if (oddPageOverlayFilename != null)
            {
                oddPageOverlay = loadPDF(oddPageOverlayFilename);
                oddPageOverlayPage = getLayoutPage(oddPageOverlay);
            }
            if (evenPageOverlayFilename != null)
            {
                evenPageOverlay = loadPDF(evenPageOverlayFilename);
                evenPageOverlayPage = getLayoutPage(evenPageOverlay);
            }
            if (allPagesOverlayFilename != null)
            {
                allPagesOverlay = loadPDF(allPagesOverlayFilename);
                specificPageOverlayPage = getLayoutPages(allPagesOverlay);
                useAllOverlayPages = true;
                numberOfOverlayPages = specificPageOverlayPage.size();
            }
            for (Map.Entry<Integer, String> e : specificPageOverlayFile.entrySet())
            {
                PDDocument doc = loadPDF(e.getValue());
                specificPageOverlay.put(e.getKey(), doc);
                specificPageOverlayPage.put(e.getKey(), getLayoutPage(doc));
            }
            processPages(sourcePDFDocument);

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
            if (allPagesOverlay != null)
            {
                allPagesOverlay.close();
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

    private PDDocument loadPDF(String pdfName) throws IOException
    {
        return PDDocument.load(new File(pdfName));
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
        PDPage page = doc.getPage(0);
        COSBase contents = page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
        PDResources resources = page.getResources();
        if (resources == null)
        {
            resources = new PDResources();
        }
        return new LayoutPage(page.getMediaBox(), createContentStream(contents),
                resources.getCOSObject());
    }
    
    private HashMap<Integer,LayoutPage> getLayoutPages(PDDocument doc) throws IOException
    {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        int numberOfPages = doc.getNumberOfPages();
        HashMap<Integer,LayoutPage> layoutPages = new HashMap<Integer, Overlay.LayoutPage>(numberOfPages);
        for (int i=0;i<numberOfPages;i++)
        {
            PDPage page = doc.getPage(i);
            COSBase contents = page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            PDResources resources = page.getResources();
            if (resources == null)
            {
                resources = new PDResources();
            }
            layoutPages.put(i,new LayoutPage(page.getMediaBox(), createContentStream(contents), resources.getCOSObject()));
        }
        return layoutPages;
    }
    
    private COSStream createContentStream(COSBase contents) throws IOException
    {
        List<COSStream> contentStreams = createContentStreamList(contents);
        // concatenate streams
        COSStream concatStream = new COSStream();
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

    private void processPages(PDDocument document) throws IOException
    {
        int pageCount = 0;
        for (PDPage page : document.getPages())
        {
            COSDictionary pageDictionary = page.getCOSObject();
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
                overlayPage(contentArray, page, pageCount + 1, document.getNumberOfPages());
                break;
            case BACKGROUND:
                // overlay content
                overlayPage(contentArray, page, pageCount + 1, document.getNumberOfPages());
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
        if (!useAllOverlayPages && specificPageOverlayPage.containsKey(pageNumber))
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
        else if (useAllOverlayPages)
        {
            int usePageNum = (pageNumber -1 ) % numberOfOverlayPages;
            layoutPage = specificPageOverlayPage.get(usePageNum);
        }
        if (layoutPage != null)
        {
            PDResources resources = page.getResources();
            if (resources == null)
            {
                resources = new PDResources();
                page.setResources(resources);
            }
            COSName xObjectId = createOverlayXObject(page, layoutPage,
                    layoutPage.overlayContentStream);
            array.add(createOverlayStream(page, layoutPage, xObjectId));
        }
    }

    private COSName createOverlayXObject(PDPage page, LayoutPage layoutPage, COSStream contentStream)
    {
        PDFormXObject xobjForm = new PDFormXObject(new PDStream(contentStream));
        xobjForm.setResources(new PDResources(layoutPage.overlayResources));
        xobjForm.setFormType(1);
        xobjForm.setBBox( layoutPage.overlayMediaBox.createRetranslatedRectangle());
        xobjForm.setMatrix(new AffineTransform());
        PDResources resources = page.getResources();
        return resources.add(xobjForm, "OL");
    }

    private COSStream createOverlayStream(PDPage page, LayoutPage layoutPage, COSName xObjectId)
            throws IOException
    {
        // create a new content stream that executes the XObject content
        PDRectangle pageMediaBox = page.getMediaBox();
        float scale = 1;
        float hShift = (pageMediaBox.getWidth() - layoutPage.overlayMediaBox.getWidth()) / 2.0f;
        float vShift = (pageMediaBox.getHeight() - layoutPage.overlayMediaBox.getHeight()) / 2.0f;
        return createStream("q\nq " + scale + " 0 0 " + scale + " " + hShift + " " + vShift
                + " cm /" + xObjectId.getName() + " Do Q\nQ\n");
    }

    private COSStream createStream(String content) throws IOException
    {
        COSStream stream = new COSStream();
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
     * Sets the all pages overlay file.
     * 
     * @param allPagesOverlayFile the all pages overlay file
     */
    public void setAllPagesOverlayFile(String allPagesOverlayFile)
    {
        allPagesOverlayFilename = allPagesOverlayFile;
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
