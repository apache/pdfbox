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
package org.apache.pdfbox.multipdf;

import java.awt.geom.AffineTransform;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

/**
 * Adds an overlay to an existing PDF document.
 * 
 * Based on code contributed by Balazs Jerk.
 * 
 */
public class Overlay implements Closeable
{
    /**
     * Possible location of the overlayed pages: foreground or background.
     */
    public enum Position
    {
        FOREGROUND, BACKGROUND
    }

    private LayoutPage defaultOverlayPage;
    private LayoutPage firstPageOverlayPage;
    private LayoutPage lastPageOverlayPage;
    private LayoutPage oddPageOverlayPage;
    private LayoutPage evenPageOverlayPage;

    private final Set<PDDocument> openDocuments = new HashSet<PDDocument>();
    private Map<Integer, LayoutPage> specificPageOverlayPage = new HashMap<Integer, LayoutPage>();

    private Position position = Position.BACKGROUND;

    private String inputFileName = null;
    private PDDocument inputPDFDocument = null;

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
     * This will add overlays to a document.
     *
     * @param specificPageOverlayFile Optional map of overlay files for specific pages. The page
     * numbers are 1-based. The map must be empty (but not null) if no specific mappings are used.
     *
     * @return The modified input PDF document, which has to be saved and closed by the caller. If
     * the input document was passed by {@link #setInputPDF(PDDocument) setInputPDF(PDDocument)}
     * then it is that object that is returned.
     *
     * @throws IOException if something went wrong
     */
    public PDDocument overlay(Map<Integer, String> specificPageOverlayFile) throws IOException
    {
        Map<String, PDDocument> loadedDocuments = new HashMap<String, PDDocument>();
        Map<PDDocument, LayoutPage> layouts = new HashMap<PDDocument, LayoutPage>();
        loadPDFs();
        for (Map.Entry<Integer, String> e : specificPageOverlayFile.entrySet())
        {
            PDDocument doc = loadedDocuments.get(e.getValue());
            if (doc == null)
            {
                doc = loadPDF(e.getValue());
                loadedDocuments.put(e.getValue(), doc);
                layouts.put(doc, getLayoutPage(doc));
            }
            openDocuments.add(doc);
            specificPageOverlayPage.put(e.getKey(), layouts.get(doc));
        }
        processPages(inputPDFDocument);
        return inputPDFDocument;
    }

    /**
     * This will add overlays documents to a document.
     *
     * @param specificPageOverlayDocuments Optional map of overlay documents for specific pages. The
     * page numbers are 1-based. The map must be empty (but not null) if no specific mappings are
     * used.
     *
     * @return The modified input PDF document, which has to be saved and closed by the caller. If
     * the input document was passed by {@link #setInputPDF(PDDocument) setInputPDF(PDDocument)}
     * then it is that object that is returned.
     *
     * @throws IOException if something went wrong
     */
    public PDDocument overlayDocuments(Map<Integer, PDDocument> specificPageOverlayDocuments) throws IOException
    {
        loadPDFs();
        for (Map.Entry<Integer, PDDocument> e : specificPageOverlayDocuments.entrySet())
        {
            PDDocument doc = e.getValue();
            if (doc != null)
            {
                specificPageOverlayPage.put(e.getKey(), getLayoutPage(doc));
            }
        }
        processPages(inputPDFDocument);
        return inputPDFDocument;
    }

    /**
     * Close all input documents which were used for the overlay and opened by this class.
     *
     * @throws IOException if something went wrong
     */
    @Override
    public void close() throws IOException
    {
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
        for (PDDocument doc : openDocuments)
        {
            doc.close();
        }
        openDocuments.clear();
        specificPageOverlayPage.clear();
    }

    private void loadPDFs() throws IOException
    {
        // input PDF
        if (inputFileName != null)
        {
            inputPDFDocument = loadPDF(inputFileName);
        }
        // default overlay PDF
        if (defaultOverlayFilename != null)
        {
            defaultOverlay = loadPDF(defaultOverlayFilename);
        }
        if (defaultOverlay != null)
        {
            defaultOverlayPage = getLayoutPage(defaultOverlay);
        }
        // first page overlay PDF
        if (firstPageOverlayFilename != null)
        {
            firstPageOverlay = loadPDF(firstPageOverlayFilename);
        }
        if (firstPageOverlay != null)
        {
            firstPageOverlayPage = getLayoutPage(firstPageOverlay);
        }
        // last page overlay PDF
        if (lastPageOverlayFilename != null)
        {
            lastPageOverlay = loadPDF(lastPageOverlayFilename);
        }
        if (lastPageOverlay != null)
        {
            lastPageOverlayPage = getLayoutPage(lastPageOverlay);
        }
        // odd pages overlay PDF
        if (oddPageOverlayFilename != null)
        {
            oddPageOverlay = loadPDF(oddPageOverlayFilename);
        }
        if (oddPageOverlay != null)
        {
            oddPageOverlayPage = getLayoutPage(oddPageOverlay);
        }
        // even pages overlay PDF
        if (evenPageOverlayFilename != null)
        {
            evenPageOverlay = loadPDF(evenPageOverlayFilename);
        }
        if (evenPageOverlay != null)
        {
            evenPageOverlayPage = getLayoutPage(evenPageOverlay);
        }
        // all pages overlay PDF
        if (allPagesOverlayFilename != null)
        {
            allPagesOverlay = loadPDF(allPagesOverlayFilename);
        }
        if (allPagesOverlay != null)
        {
            specificPageOverlayPage = getLayoutPages(allPagesOverlay);
            useAllOverlayPages = true;
            numberOfOverlayPages = specificPageOverlayPage.size();
        }
    }
    
    private PDDocument loadPDF(String pdfName) throws IOException
    {
        return PDDocument.load(new File(pdfName));
    }

    /**
     * Stores the overlay page information.
     */
    private static final class LayoutPage
    {
        private final PDRectangle overlayMediaBox;
        private final COSStream overlayContentStream;
        private final COSDictionary overlayResources;
        private final int overlayRotation;

        private LayoutPage(PDRectangle mediaBox, COSStream contentStream, COSDictionary resources, int rotation)
        {
            overlayMediaBox = mediaBox;
            overlayContentStream = contentStream;
            overlayResources = resources;
            overlayRotation = rotation;
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
        return new LayoutPage(page.getMediaBox(), createCombinedContentStream(contents),
                resources.getCOSObject(), page.getRotation());
    }
    
    private Map<Integer,LayoutPage> getLayoutPages(PDDocument doc) throws IOException
    {
        int numberOfPages = doc.getNumberOfPages();
        Map<Integer,LayoutPage> layoutPages = new HashMap<Integer, Overlay.LayoutPage>(numberOfPages);
        for (int i=0;i<numberOfPages;i++)
        {
            PDPage page = doc.getPage(i);
            COSBase contents = page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            PDResources resources = page.getResources();
            if (resources == null)
            {
                resources = new PDResources();
            }
            layoutPages.put(i, new LayoutPage(page.getMediaBox(), createCombinedContentStream(contents), 
                    resources.getCOSObject(), page.getRotation()));
        }
        return layoutPages;
    }
    
    private COSStream createCombinedContentStream(COSBase contents) throws IOException
    {
        List<COSStream> contentStreams = createContentStreamList(contents);
        // concatenate streams
        COSStream concatStream = inputPDFDocument.getDocument().createCOSStream();
        OutputStream out = concatStream.createOutputStream(COSName.FLATE_DECODE);
        for (COSStream contentStream : contentStreams)
        {
            InputStream in = contentStream.createInputStream();
            IOUtils.copy(in, out);
            out.flush();
            in.close();
        }
        out.close();
        return concatStream;
    }

    // get the content streams as a list
    private List<COSStream> createContentStreamList(COSBase contents) throws IOException
    {
        List<COSStream> contentStreams = new ArrayList<COSStream>();
        if (contents == null)
        {
            return contentStreams;
        }
        else if (contents instanceof COSStream)
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
            throw new IOException("Unknown content type: " + contents.getClass().getName());
        }
        return contentStreams;
    }

    private void processPages(PDDocument document) throws IOException
    {
        int pageCounter = 0;
        for (PDPage page : document.getPages())
        {
            pageCounter++;
            COSDictionary pageDictionary = page.getCOSObject();
            COSBase originalContent = pageDictionary.getDictionaryObject(COSName.CONTENTS);
            COSArray newContentArray = new COSArray();
            LayoutPage layoutPage = getLayoutPage(pageCounter, document.getNumberOfPages());
            if (layoutPage == null)
            {
                continue;
            }
            switch (position)
            {
                case FOREGROUND:
                    // save state
                    newContentArray.add(createStream("q\n"));
                    addOriginalContent(originalContent, newContentArray);
                    // restore state
                    newContentArray.add(createStream("Q\n"));
                    // overlay content last
                    overlayPage(page, layoutPage, newContentArray);
                    break;
                case BACKGROUND:
                    // overlay content first
                    overlayPage(page, layoutPage, newContentArray);

                    addOriginalContent(originalContent, newContentArray);
                    break;
                default:
                    throw new IOException("Unknown type of position:" + position);
            }
            pageDictionary.setItem(COSName.CONTENTS, newContentArray);
        }
    }

    private void addOriginalContent(COSBase contents, COSArray contentArray) throws IOException
    {
        if (contents == null)
        {
            return;
        }

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
            throw new IOException("Unknown content type: " + contents.getClass().getName());
        }
    }

    private void overlayPage(PDPage page, LayoutPage layoutPage, COSArray array)
            throws IOException
    {
        PDResources resources = page.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            page.setResources(resources);
        }
        COSName xObjectId = createOverlayXObject(page, layoutPage);
        array.add(createOverlayStream(page, layoutPage, xObjectId));
    }

    private LayoutPage getLayoutPage(int pageNumber, int numberOfPages)
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
        return layoutPage;
    }

    private COSName createOverlayXObject(PDPage page, LayoutPage layoutPage)
    {
        PDFormXObject xobjForm = new PDFormXObject(layoutPage.overlayContentStream);
        xobjForm.setResources(new PDResources(layoutPage.overlayResources));
        xobjForm.setFormType(1);
        xobjForm.setBBox(layoutPage.overlayMediaBox.createRetranslatedRectangle());
        AffineTransform at = new AffineTransform();
        switch (layoutPage.overlayRotation)
        {
            case 90:
                at.translate(0, layoutPage.overlayMediaBox.getWidth());
                at.rotate(Math.toRadians(-90));
                break;
            case 180:
                at.translate(layoutPage.overlayMediaBox.getWidth(), layoutPage.overlayMediaBox.getHeight());
                at.rotate(Math.toRadians(-180));
                break;
            case 270:
                at.translate(layoutPage.overlayMediaBox.getHeight(), 0);
                at.rotate(Math.toRadians(-270));
                break;
            default:
                break;
        }
        xobjForm.setMatrix(at);
        PDResources resources = page.getResources();
        return resources.add(xobjForm, "OL");
    }

    private COSStream createOverlayStream(PDPage page, LayoutPage layoutPage, COSName xObjectId)
            throws IOException
    {
        // create a new content stream that executes the XObject content
        StringBuilder overlayStream = new StringBuilder();
        overlayStream.append("q\nq\n");
        PDRectangle overlayMediaBox = new PDRectangle(layoutPage.overlayMediaBox.getCOSArray());
        if (layoutPage.overlayRotation == 90 || layoutPage.overlayRotation == 270)
        {
            overlayMediaBox.setLowerLeftX(layoutPage.overlayMediaBox.getLowerLeftY());
            overlayMediaBox.setLowerLeftY(layoutPage.overlayMediaBox.getLowerLeftX());
            overlayMediaBox.setUpperRightX(layoutPage.overlayMediaBox.getUpperRightY());
            overlayMediaBox.setUpperRightY(layoutPage.overlayMediaBox.getUpperRightX());
        }
        AffineTransform at = calculateAffineTransform(page, overlayMediaBox);
        double[] flatmatrix = new double[6];
        at.getMatrix(flatmatrix);
        for (double v : flatmatrix)
        {
            overlayStream.append(float2String((float) v));
            overlayStream.append(" ");
        }
        overlayStream.append(" cm\n");

        // if debugging, insert
        // 0 0 overlayMediaBox.getHeight() overlayMediaBox.getWidth() re\ns\n
        // into the content stream

        overlayStream.append(" /");
        overlayStream.append(xObjectId.getName());
        overlayStream.append(" Do Q\nQ\n");
        return createStream(overlayStream.toString());
    }

    /**
     * Calculate the transform to be used when positioning the overlay. The default implementation
     * centers on the destination. Override this method to do your own, e.g. move to a corner, or
     * rotate.
     *
     * @param page The page that will get the overlay.
     * @param overlayMediaBox The overlay media box.
     * @return The affine transform to be used.
     */
    protected AffineTransform calculateAffineTransform(PDPage page, PDRectangle overlayMediaBox)
    {
        AffineTransform at = new AffineTransform();
        PDRectangle pageMediaBox = page.getMediaBox();
        float hShift = (pageMediaBox.getWidth() - overlayMediaBox.getWidth()) / 2.0f;
        float vShift = (pageMediaBox.getHeight() - overlayMediaBox.getHeight()) / 2.0f;
        at.translate(hShift, vShift);
        return at;
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
        COSStream stream = inputPDFDocument.getDocument().createCOSStream();
        OutputStream out = stream.createOutputStream(
                content.length() > 20 ? COSName.FLATE_DECODE : null);
        out.write(content.getBytes("ISO-8859-1"));
        out.close();
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
     * @param inputFile the file to be overlayed. The {@link PDDocument} object gathered from
     * opening this file will be returned by
     * {@link #overlay(java.util.Map) overlay(Map&lt;Integer, String&gt;)}.
     */
    public void setInputFile(String inputFile)
    {
        inputFileName = inputFile;
    }

    /**
     * Sets the PDF to be overlayed.
     *
     * @param inputPDF the PDF to be overlayed. This will be the object that is returned by
     * {@link #overlay(java.util.Map) overlay(Map&lt;Integer, String&gt;)}.
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
     * @param allPagesOverlayPDF the all pages overlay PDF. This should not be a PDDocument that you
     * created on the fly, it should be saved first, if it contains any fonts that are subset.
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
