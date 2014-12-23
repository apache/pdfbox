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
import java.math.BigDecimal;
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
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;

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
    private PDDocument inputPDFDocument = null;

    private String outputFilename = null;
    
    private String defaultOverlayFilename = null;
    private PDDocument defaultOverlay = null;

    private String firstPageOverlayFilename = null;
    private PDDocument firstPageOverlay = null;

    private String lastPageOverlayFilename = null;
    private PDDocument lastPageOverlay = null;
    
    private String allPagesOverlayFilename = null;
    private PDDocument allPagesOverlay = null;
    
    private String oddPageOverlayFilename = null;
    private PDDocument oddPageOverlay = null;
    
    private String evenPageOverlayFilename = null;
    private PDDocument evenPageOverlay = null;

    
    private int numberOfOverlayPages = 0;
    private boolean useAllOverlayPages = false;

    /**
     * This will add overlays to a documents.
     * 
     * @param specificPageOverlayFile map of overlay files for specific pages
     * @param useNonSeqParser indicates whether the non-sequential parser is used
     * @throws IOException if something went wrong
     * @throws COSVisitorException if something went wrong when savin the pdf
     */
    public void overlay(Map<Integer, String> specificPageOverlayFile, boolean useNonSeqParser) throws IOException,
            COSVisitorException
    {
        try
        {
            loadPDFs(useNonSeqParser);
            for (Map.Entry<Integer, String> e : specificPageOverlayFile.entrySet())
            {
                PDDocument doc = loadPDF(e.getValue(), useNonSeqParser);
                specificPageOverlay.put(e.getKey(), doc);
                specificPageOverlayPage.put(e.getKey(), getLayoutPage(doc));
            }
            PDDocumentCatalog pdfCatalog = inputPDFDocument.getDocumentCatalog();
            processPages(pdfCatalog.getAllPages());

            inputPDFDocument.save(outputFilename);
        }
        finally
        {
            if (inputPDFDocument != null)
            {
                inputPDFDocument.close();
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

    private void loadPDFs(boolean useNonSeqParser) throws IOException
    {
        // input PDF
        if (inputFileName != null)
        {
            inputPDFDocument = loadPDF(inputFileName, useNonSeqParser);
        }
        // default overlay PDF
        if (defaultOverlayFilename != null)
        {
            defaultOverlay = loadPDF(defaultOverlayFilename, useNonSeqParser);
        }
        if (defaultOverlay != null)
        {
            defaultOverlayPage = getLayoutPage(defaultOverlay);
        }
        // first page overlay PDF
        if (firstPageOverlayFilename != null)
        {
            firstPageOverlay = loadPDF(firstPageOverlayFilename, useNonSeqParser);
        }
        if (firstPageOverlay != null)
        {
            firstPageOverlayPage = getLayoutPage(firstPageOverlay);
        }
        // last page overlay PDF
        if (lastPageOverlayFilename != null)
        {
            lastPageOverlay = loadPDF(lastPageOverlayFilename, useNonSeqParser);
        }
        if (lastPageOverlay != null)
        {
            lastPageOverlayPage = getLayoutPage(lastPageOverlay);
        }
        // odd pages overlay PDF
        if (oddPageOverlayFilename != null)
        {
            oddPageOverlay = loadPDF(oddPageOverlayFilename, useNonSeqParser);
        }
        if (oddPageOverlay != null)
        {
            oddPageOverlayPage = getLayoutPage(oddPageOverlay);
        }
        // even pages overlay PDF
        if (evenPageOverlayFilename != null)
        {
            evenPageOverlay = loadPDF(evenPageOverlayFilename, useNonSeqParser);
        }
        if (evenPageOverlay != null)
        {
            evenPageOverlayPage = getLayoutPage(evenPageOverlay);
        }
        // all pages overlay PDF
        if (allPagesOverlayFilename != null)
        {
            allPagesOverlay = loadPDF(allPagesOverlayFilename, useNonSeqParser);
        }
        if (allPagesOverlay != null)
        {
            specificPageOverlayPage = getLayoutPages(allPagesOverlay);
            useAllOverlayPages = true;
            numberOfOverlayPages = specificPageOverlayPage.size();
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
        return new LayoutPage(page.getMediaBox(), createContentStream(contents), resources.getCOSDictionary());
    }

    private HashMap<Integer,LayoutPage> getLayoutPages(PDDocument doc) throws IOException
    {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        int numberOfPages = doc.getNumberOfPages();
        HashMap<Integer,LayoutPage> layoutPages = new HashMap<Integer, Overlay.LayoutPage>(numberOfPages);
        for (int i=0;i<numberOfPages;i++)
        {
            PDPage page = (PDPage) catalog.getAllPages().get(i);
            COSBase contents = page.getCOSDictionary().getDictionaryObject(COSName.CONTENTS);
            PDResources resources = page.findResources();
            if (resources == null)
            {
                resources = new PDResources();
            }
            layoutPages.put(i,new LayoutPage(page.getMediaBox(), createContentStream(contents), 
                    resources.getCOSDictionary()));
        }
        return layoutPages;
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

    private void overlayPage(COSArray array, PDPage page, int pageNumber, int numberOfPages) throws IOException
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
            int usePageNum = (pageNumber - 1) % numberOfOverlayPages;
            layoutPage = specificPageOverlayPage.get(usePageNum);
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
        PDXObjectForm xobjForm = new PDXObjectForm(contentStream);
        xobjForm.setResources(new PDResources(layoutPage.overlayResources));
        xobjForm.setFormType(1);
        xobjForm.setBBox(layoutPage.overlayMediaBox.createRetranslatedRectangle());
        xobjForm.setMatrix(new AffineTransform());
        PDResources resources = page.findResources();
        return resources.addXObject(xobjForm, XOBJECT_PREFIX);
    }

    private COSStream createOverlayStream(PDPage page, LayoutPage layoutPage, String xObjectId) throws IOException
    {
        // create a new content stream that executes the XObject content
        PDRectangle pageMediaBox = page.getMediaBox();
        float hShift = (pageMediaBox.getWidth() - layoutPage.overlayMediaBox.getWidth()) / 2.0f;
        float vShift = (pageMediaBox.getHeight() - layoutPage.overlayMediaBox.getHeight()) / 2.0f;
        StringBuilder overlayStream = new StringBuilder();
        overlayStream.append("q\nq 1 0 0 1 ");
        overlayStream.append(float2String(hShift));
        overlayStream.append(" ");
        overlayStream.append(float2String(vShift) );
        overlayStream.append(" cm /");
        overlayStream.append(xObjectId);
        overlayStream.append(" Do Q\nQ\n");
        return createStream(overlayStream.toString());
    }

    private String float2String(float floatValue)
    {
        // use a BigDecimal as intermediate state to avoid 
        // a floating point string representation of the float value
        BigDecimal value = new BigDecimal(String.valueOf(floatValue));
        String stringValue = value.toPlainString();
        // remove fraction digit "0" only
        if (stringValue.indexOf('.') > -1 && !stringValue.endsWith(".0"))
        {
            while (stringValue.endsWith("0") && !stringValue.endsWith(".0"))
            {
                stringValue = stringValue.substring(0,stringValue.length()-1);
            }
        }
        return stringValue;
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
     * Sets the PDF to be overlayed.
     * 
     * @param inputPDF the PDF to be overlayed
     */
    public void setInputPDF(PDDocument inputPDF)
    {
        inputPDFDocument = inputPDF;
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
     * Sets the default overlay PDF.
     * 
     * @param defaultOverlayPDF the default overlay PDF
     */
    public void setDefaultOverlayPDF(PDDocument defaultOverlayPDF)
    {
        defaultOverlay = defaultOverlayPDF;
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
     * Sets the first page overlay PDF.
     * 
     * @param firstPageOverlayPDF the first page overlay PDF
     */
    public void setFirstPageOverlayPDF(PDDocument firstPageOverlayPDF)
    {
        firstPageOverlay = firstPageOverlayPDF;
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
     * Sets the last page overlay PDF.
     * 
     * @param lastPageOverlayPDF the last page overlay PDF
     */
    public void setLastPageOverlayPDF(PDDocument lastPageOverlayPDF)
    {
        lastPageOverlay = lastPageOverlayPDF;
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
     * Sets the all pages overlay PDF.
     * 
     * @param allPagesOverlayPDF the all pages overlay PDF
     */
    public void setAllPagesOverlayPDF(PDDocument allPagesOverlayPDF)
    {
        allPagesOverlay = allPagesOverlayPDF;
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
     * Sets the odd page overlay PDF.
     * 
     * @param oddPageOverlayPDF the odd page overlay PDF
     */
    public void setOddPageOverlayPDF(PDDocument oddPageOverlayPDF)
    {
        oddPageOverlay = oddPageOverlayPDF;
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

    /**
     * Sets the even page overlay PDF.
     * 
     * @param evenPageOverlayPDF the even page overlay PDF
     */
    public void setEvenPageOverlayPDF(PDDocument evenPageOverlayPDF)
    {
        evenPageOverlay = evenPageOverlayPDF;
    }
}
