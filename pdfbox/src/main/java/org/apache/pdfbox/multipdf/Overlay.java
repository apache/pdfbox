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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
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
    private static final Log LOG = LogFactory.getLog(Overlay.class);

    /**
     * Possible location of the overlaid pages: foreground or background.
     */
    public enum Position
    {
        FOREGROUND, BACKGROUND
    }

    private LayoutPage defaultOverlayPage;
    private final Map<Integer,LayoutPage> rotatedDefaultOverlayPagesMap = new HashMap<>();
    private LayoutPage firstPageOverlayPage;
    private LayoutPage lastPageOverlayPage;
    private LayoutPage oddPageOverlayPage;
    private LayoutPage evenPageOverlayPage;

    private final Set<PDDocument> openDocumentsSet = new HashSet<>();
    private Map<Integer, LayoutPage> specificPageOverlayLayoutPageMap = new HashMap<>();

    private Position position = Position.BACKGROUND;

    private String inputFileName = null;
    private PDDocument inputPDFDocument = null;

    private String defaultOverlayFilename = null;
    private PDDocument defaultOverlayDocument = null;

    private String firstPageOverlayFilename = null;
    private PDDocument firstPageOverlayDocument = null;

    private String lastPageOverlayFilename = null;
    private PDDocument lastPageOverlayDocument = null;
    
    private String allPagesOverlayFilename = null;
    private PDDocument allPagesOverlayDocument = null;
    
    private String oddPageOverlayFilename = null;
    private PDDocument oddPageOverlayDocument = null;
    
    private String evenPageOverlayFilename = null;
    private PDDocument evenPageOverlayDocument = null;

    private int numberOfOverlayPages = 0;
    private boolean useAllOverlayPages = false;
    private boolean adjustRotation = false;

    /**
     * This will add overlays to a document.
     *
     * @param specificPageOverlayMap Optional map of overlay files of which the first page will be
     * used for specific pages of the input document. The page numbers are 1-based. The map must be
     * empty (but not null) if no specific mappings are used.
     *
     * @return The modified input PDF document, which has to be saved and closed by the caller. If
     * the input document was passed by {@link #setInputPDF(PDDocument) setInputPDF(PDDocument)}
     * then it is that object that is returned.
     *
     * @throws IOException if something went wrong.
     * @throws IllegalArgumentException if the input document is missing.
     */
    public PDDocument overlay(Map<Integer, String> specificPageOverlayMap) throws IOException
    {
        Map<String, LayoutPage> layouts = new HashMap<>();
        String path;
        loadPDFs();
        for (Map.Entry<Integer, String> e : specificPageOverlayMap.entrySet())
        {
            path = e.getValue();
            LayoutPage layoutPage = layouts.get(path);
            if (layoutPage == null)
            {
                PDDocument doc = loadPDF(path);
                layoutPage = createLayoutPageFromDocument(doc);
                layouts.put(path, layoutPage);
                openDocumentsSet.add(doc);
            }
            specificPageOverlayLayoutPageMap.put(e.getKey(), layoutPage);
        }
        processPages(inputPDFDocument);
        return inputPDFDocument;
    }

    /**
     * This will add overlays documents to a document. If you created the overlay documents with
     * subsetted fonts, you need to save them first so that the subsetting gets done.
     *
     * @param specificPageOverlayDocumentMap Optional map of overlay documents for specific pages. The
     * page numbers are 1-based. The map must be empty (but not null) if no specific mappings are
     * used.
     *
     * @return The modified input PDF document, which has to be saved and closed by the caller. If
     * the input document was passed by {@link #setInputPDF(PDDocument) setInputPDF(PDDocument)}
     * then it is that object that is returned.
     *
     * @throws IOException if something went wrong
     */
    public PDDocument overlayDocuments(Map<Integer, PDDocument> specificPageOverlayDocumentMap) throws IOException
    {
        loadPDFs();
        for (Map.Entry<Integer, PDDocument> e : specificPageOverlayDocumentMap.entrySet())
        {
            PDDocument doc = e.getValue();
            if (doc != null)
            {
                specificPageOverlayLayoutPageMap.put(e.getKey(), createLayoutPageFromDocument(doc));
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
        if (defaultOverlayDocument != null)
        {
            defaultOverlayDocument.close();
        }
        if (firstPageOverlayDocument != null)
        {
            firstPageOverlayDocument.close();
        }
        if (lastPageOverlayDocument != null)
        {
            lastPageOverlayDocument.close();
        }
        if (allPagesOverlayDocument != null)
        {
            allPagesOverlayDocument.close();
        }
        if (oddPageOverlayDocument != null)
        {
            oddPageOverlayDocument.close();
        }
        if (evenPageOverlayDocument != null)
        {
            evenPageOverlayDocument.close();
        }
        for (PDDocument doc : openDocumentsSet)
        {
            doc.close();
        }
        openDocumentsSet.clear();
        specificPageOverlayLayoutPageMap.clear();
        rotatedDefaultOverlayPagesMap.clear();
    }

    private void loadPDFs() throws IOException
    {
        // input PDF
        if (inputFileName != null)
        {
            inputPDFDocument = loadPDF(inputFileName);
        }
        if (inputPDFDocument == null)
        {
            throw new IllegalArgumentException("No input document");
        }
        // default overlay PDF
        if (defaultOverlayFilename != null)
        {
            defaultOverlayDocument = loadPDF(defaultOverlayFilename);
        }
        if (defaultOverlayDocument != null)
        {
            defaultOverlayPage = createLayoutPageFromDocument(defaultOverlayDocument);
        }
        // first page overlay PDF
        if (firstPageOverlayFilename != null)
        {
            firstPageOverlayDocument = loadPDF(firstPageOverlayFilename);
        }
        if (firstPageOverlayDocument != null)
        {
            firstPageOverlayPage = createLayoutPageFromDocument(firstPageOverlayDocument);
        }
        // last page overlay PDF
        if (lastPageOverlayFilename != null)
        {
            lastPageOverlayDocument = loadPDF(lastPageOverlayFilename);
        }
        if (lastPageOverlayDocument != null)
        {
            lastPageOverlayPage = createLayoutPageFromDocument(lastPageOverlayDocument);
        }
        // odd pages overlay PDF
        if (oddPageOverlayFilename != null)
        {
            oddPageOverlayDocument = loadPDF(oddPageOverlayFilename);
        }
        if (oddPageOverlayDocument != null)
        {
            oddPageOverlayPage = createLayoutPageFromDocument(oddPageOverlayDocument);
        }
        // even pages overlay PDF
        if (evenPageOverlayFilename != null)
        {
            evenPageOverlayDocument = loadPDF(evenPageOverlayFilename);
        }
        if (evenPageOverlayDocument != null)
        {
            evenPageOverlayPage = createLayoutPageFromDocument(evenPageOverlayDocument);
        }
        // all pages overlay PDF
        if (allPagesOverlayFilename != null)
        {
            allPagesOverlayDocument = loadPDF(allPagesOverlayFilename);
        }
        if (allPagesOverlayDocument != null)
        {
            specificPageOverlayLayoutPageMap = createPageOverlayLayoutPageMap(allPagesOverlayDocument);
            useAllOverlayPages = true;
            numberOfOverlayPages = specificPageOverlayLayoutPageMap.size();
        }
    }
    
    private PDDocument loadPDF(String pdfName) throws IOException
    {
        return Loader.loadPDF(new File(pdfName));
    }

    /**
     * Stores the overlay page information.
     */
    private static final class LayoutPage
    {
        private final PDRectangle overlayMediaBox;
        private final COSStream overlayCOSStream;
        private final COSDictionary overlayResources;
        private int overlayRotation;

        private LayoutPage(PDRectangle mediaBox, COSStream contentStream, COSDictionary resources, int rotation)
        {
            overlayMediaBox = mediaBox;
            overlayCOSStream = contentStream;
            overlayResources = resources;
            overlayRotation = rotation;
        }
    }

    /**
     * Create a LayoutPage object from the first page of the given document.
     *
     * @param doc
     * @return
     * @throws IOException 
     */
    private LayoutPage createLayoutPageFromDocument(PDDocument doc) throws IOException
    {
        return createLayoutPage(doc.getPage(0));
    }

    /**
     * Create a LayoutPage object from given PDPage object.
     *
     * @return
     * @throws IOException 
     */
    private LayoutPage createLayoutPage(PDPage page) throws IOException
    {
        COSBase contents = page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
        PDResources resources = page.getResources();
        if (resources == null)
        {
            resources = new PDResources();
        }
        return new LayoutPage(page.getMediaBox(), createCombinedContentStream(contents),
                resources.getCOSObject(), page.getRotation());
    }
    
    private Map<Integer,LayoutPage> createPageOverlayLayoutPageMap(PDDocument doc) throws IOException
    {
        int i = 0;
        PDPageTree pageTree = doc.getPages();
        Map<Integer, LayoutPage> layoutPages = new HashMap<>(pageTree.getCount());
        for (PDPage page : pageTree)
        {
            layoutPages.put(i, createLayoutPage(page));
            i++;
        }
        return layoutPages;
    }
    
    private COSStream createCombinedContentStream(COSBase contents) throws IOException
    {
        List<COSStream> contentStreams = createContentStreamList(contents);
        // concatenate streams
        COSStream concatStream = inputPDFDocument.getDocument().createCOSStream();
        try (OutputStream out = concatStream.createOutputStream(COSName.FLATE_DECODE))
        {
            for (COSStream contentStream : contentStreams)
            {
                try (InputStream in = contentStream.createInputStream())
                {
                    IOUtils.copy(in, out);
                    out.flush();
                }
            }
        }
        return concatStream;
    }

    // get the content streams as a list
    private List<COSStream> createContentStreamList(COSBase contents) throws IOException
    {
        if (contents == null)
        {
            return Collections.emptyList();
        }
        if (contents instanceof COSStream)
        {
            return Collections.singletonList((COSStream) contents);
        }

        List<COSStream> contentStreams = new ArrayList<>();
        if (contents instanceof COSArray)
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
        PDFCloneUtility cloner = new PDFCloneUtility(document);
        PDPageTree pageTree = document.getPages();
        int numberOfPages = pageTree.getCount();
        for (PDPage page : pageTree)
        {
            pageCounter++;
            LayoutPage layoutPage = getLayoutPage(pageCounter, numberOfPages);
            if (layoutPage == null)
            {
                continue;
            }
            COSDictionary pageDictionary = page.getCOSObject();
            COSBase originalContent = pageDictionary.getDictionaryObject(COSName.CONTENTS);
            COSArray newContentArray = new COSArray();
            switch (position)
            {
                case FOREGROUND:
                    // save state
                    newContentArray.add(createStream("q\n"));
                    addOriginalContent(originalContent, newContentArray);
                    // restore state
                    newContentArray.add(createStream("Q\n"));
                    // overlay content last
                    overlayPage(page, layoutPage, newContentArray, cloner);
                    break;
                case BACKGROUND:
                    // overlay content first
                    overlayPage(page, layoutPage, newContentArray, cloner);

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

    private void overlayPage(PDPage page, LayoutPage layoutPage, COSArray array,
            PDFCloneUtility cloner)
            throws IOException
    {
        PDResources resources = page.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            page.setResources(resources);
        }
        PDFormXObject overlayFormXObject = createOverlayFormXObject(layoutPage, cloner);
        COSName formXObjectId = resources.add(overlayFormXObject, "OL");
        array.add(createOverlayStream(page, layoutPage, formXObjectId));
    }

    private LayoutPage getLayoutPage(int pageNumber, int numberOfPages) throws IOException
    {
        LayoutPage layoutPage = null;
        if (!useAllOverlayPages && specificPageOverlayLayoutPageMap.containsKey(pageNumber))
        {
            layoutPage = specificPageOverlayLayoutPageMap.get(pageNumber);
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

            if (adjustRotation)
            {
                // PDFBOX-6049: consider the rotation of the document page
                // Note that this segment is only the second best solution to the problem. The best
                // would be to make appropriate transforms in calculateAffineTransform()                
                PDPage page = inputPDFDocument.getPage(pageNumber - 1);
                int rotation = page.getRotation();
                if (rotation != 0)
                {
                    return createAdjustedLayoutPage(rotation);
                }
            }
        }
        else if (useAllOverlayPages)
        {
            int usePageNum = (pageNumber -1 ) % numberOfOverlayPages;
            layoutPage = specificPageOverlayLayoutPageMap.get(usePageNum);
        }
        return layoutPage;
    }

    private LayoutPage createAdjustedLayoutPage(int rotation) throws IOException
    {
        LayoutPage rotatedLayoutPage = rotatedDefaultOverlayPagesMap.get(rotation);
        if (rotatedLayoutPage == null)
        {
            // createLayoutPage must be called because we can't reuse the COSStream
            rotatedLayoutPage = createLayoutPage(defaultOverlayDocument.getPage(0));
            int newRotation = (rotatedLayoutPage.overlayRotation - rotation + 360) % 360;
            rotatedLayoutPage.overlayRotation = newRotation;
            rotatedDefaultOverlayPagesMap.put(rotation, rotatedLayoutPage);
        }
        return rotatedLayoutPage;
    }

    private PDFormXObject createOverlayFormXObject(LayoutPage layoutPage, PDFCloneUtility cloner)
            throws IOException
    {
        PDFormXObject xobjForm = new PDFormXObject(layoutPage.overlayCOSStream);
        xobjForm.setResources(new PDResources(
                cloner.cloneForNewDocument(layoutPage.overlayResources)));
        xobjForm.setFormType(1);
        xobjForm.setBBox(layoutPage.overlayMediaBox.createRetranslatedRectangle());
        AffineTransform at = new AffineTransform();
        switch (layoutPage.overlayRotation)
        {
            case 90:
                at.translate(0, layoutPage.overlayMediaBox.getWidth());
                at.quadrantRotate(3); // 270
                break;
            case 180:
                at.translate(layoutPage.overlayMediaBox.getWidth(), layoutPage.overlayMediaBox.getHeight());
                at.quadrantRotate(2); // 180
                break;
            case 270:
                at.translate(layoutPage.overlayMediaBox.getHeight(), 0);
                at.quadrantRotate(1); // 90
                break;
            default:
                break;
        }
        xobjForm.setMatrix(at);
        return xobjForm;
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
            overlayStream.append(' ');
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
     * centers on the destination and assumes (0,0) to be the lower left (This will be changed in
     * 4.0, see PDFBOX-6048 why). Override this method to do your own, e.g. move to a corner,
     * rotate, or zoom.
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
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Overlay position: (" + hShift + "," + vShift + ")");
        }
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
        try (OutputStream out = stream.createOutputStream(
                content.length() > 20 ? COSName.FLATE_DECODE : null))
        {
            out.write(content.getBytes(StandardCharsets.ISO_8859_1));
        }
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
     * Sets the file to be overlaid.
     *
     * @param inputFile the file to be overlaid. The {@link PDDocument} object gathered from
     * opening this file will be returned by
     * {@link #overlay(java.util.Map) overlay(Map&lt;Integer, String&gt;)}.
     */
    public void setInputFile(String inputFile)
    {
        inputFileName = inputFile;
    }

    /**
     * Sets the PDF to be overlaid.
     *
     * @param inputPDF the PDF to be overlaid. This will be the object that is returned by
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
     * Sets the default overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param defaultOverlayPDF the default overlay PDF
     */
    public void setDefaultOverlayPDF(PDDocument defaultOverlayPDF)
    {
        defaultOverlayDocument = defaultOverlayPDF;
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
     * Sets the first page overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param firstPageOverlayPDF the first page overlay PDF
     */
    public void setFirstPageOverlayPDF(PDDocument firstPageOverlayPDF)
    {
        firstPageOverlayDocument = firstPageOverlayPDF;
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
     * Sets the last page overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param lastPageOverlayPDF the last page overlay PDF
     */
    public void setLastPageOverlayPDF(PDDocument lastPageOverlayPDF)
    {
        lastPageOverlayDocument = lastPageOverlayPDF;
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
     * Sets the all pages overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param allPagesOverlayPDF the all pages overlay PDF. This should not be a PDDocument that you
     * created on the fly, it should be saved first, if it contains any fonts that are subset.
     */
    public void setAllPagesOverlayPDF(PDDocument allPagesOverlayPDF)
    {
        allPagesOverlayDocument = allPagesOverlayPDF;
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
     * Sets the odd page overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param oddPageOverlayPDF the odd page overlay PDF
     */
    public void setOddPageOverlayPDF(PDDocument oddPageOverlayPDF)
    {
        oddPageOverlayDocument = oddPageOverlayPDF;
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
     * Sets the even page overlay PDF. If you created the overlay document with
     * subsetted fonts, you need to save it first so that the subsetting gets done.
     * 
     * @param evenPageOverlayPDF the even page overlay PDF
     */
    public void setEvenPageOverlayPDF(PDDocument evenPageOverlayPDF)
    {
        evenPageOverlayDocument = evenPageOverlayPDF;
    }

    /**
     * This sets whether the overlay is to be rotated according to the rotation of the pages of the
     * source document. This may look weird if the content of the document is also rotated. So it's
     * really a users decision to activate this option if the overlay appears rotated in some pages
     * of the result document and this isn't wanted.
     * <p>
     * This setting will only apply to usage of the default overlay, because it is assumed that when
     * using specific overlays for specific pages, it is known in advance what kind of input there
     * is.
     *
     * @param adjustRotation if true, the overlay will always look the same when the result file is
     * displayed on the screen. If false (default) then it will be rotated if the page is rotated.
     */
    public void setAdjustRotation(boolean adjustRotation)
    {
        this.adjustRotation = adjustRotation;
    }
}
