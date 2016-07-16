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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.util.Matrix;

/**
 * This class allows to import pages as Form XObjects into a PDF file and use them to create
 * layers (optional content groups).
 *
 */
public class LayerUtility
{
    private static final boolean DEBUG = true;

    private final PDDocument targetDoc;
    private final PDFCloneUtility cloner;

    /**
     * Creates a new instance.
     * @param document the PDF document to modify
     */
    public LayerUtility(PDDocument document)
    {
        this.targetDoc = document;
        this.cloner = new PDFCloneUtility(document);
    }

    /**
     * Returns the PDF document we work on.
     * @return the PDF document
     */
    public PDDocument getDocument()
    {
        return this.targetDoc;
    }

    /**
     * Some applications may not wrap their page content in a save/restore (q/Q) pair which can
     * lead to problems with coordinate system transformations when content is appended. This
     * method lets you add a q/Q pair around the existing page's content.
     * @param page the page
     * @throws IOException if an I/O error occurs
     */
    public void wrapInSaveRestore(PDPage page) throws IOException
    {
        COSStream saveGraphicsStateStream = getDocument().getDocument().createCOSStream();
        OutputStream saveStream = saveGraphicsStateStream.createOutputStream();
        saveStream.write("q\n".getBytes("ISO-8859-1"));
        saveStream.close();

        COSStream restoreGraphicsStateStream = getDocument().getDocument().createCOSStream();
        OutputStream restoreStream = restoreGraphicsStateStream.createOutputStream();
        restoreStream.write("Q\n".getBytes("ISO-8859-1"));
        restoreStream.close();

        //Wrap the existing page's content in a save/restore pair (q/Q) to have a controlled
        //environment to add additional content.
        COSDictionary pageDictionary = page.getCOSObject();
        COSBase contents = pageDictionary.getDictionaryObject(COSName.CONTENTS);
        if (contents instanceof COSStream)
        {
            COSStream contentsStream = (COSStream)contents;

            COSArray array = new COSArray();
            array.add(saveGraphicsStateStream);
            array.add(contentsStream);
            array.add(restoreGraphicsStateStream);

            pageDictionary.setItem(COSName.CONTENTS, array);
        }
        else if( contents instanceof COSArray )
        {
            COSArray contentsArray = (COSArray)contents;

            contentsArray.add(0, saveGraphicsStateStream);
            contentsArray.add(restoreGraphicsStateStream);
        }
        else
        {
            throw new IOException("Contents are unknown type: " + contents.getClass().getName());
        }
    }

    /**
     * Imports a page from some PDF file as a Form XObject so it can be placed on another page
     * in the target document.
     * @param sourceDoc the source PDF document that contains the page to be copied
     * @param pageNumber the page number of the page to be copied
     * @return a Form XObject containing the original page's content
     * @throws IOException if an I/O error occurs
     */
    public PDFormXObject importPageAsForm(PDDocument sourceDoc, int pageNumber) throws IOException
    {
        PDPage page = sourceDoc.getPage(pageNumber);
        return importPageAsForm(sourceDoc, page);
    }

    private static final Set<String> PAGE_TO_FORM_FILTER = new java.util.HashSet<String>(
            Arrays.asList(new String[] {"Group", "LastModified", "Metadata"}));

    /**
     * Imports a page from some PDF file as a Form XObject so it can be placed on another page
     * in the target document.
     * @param sourceDoc the source PDF document that contains the page to be copied
     * @param page the page in the source PDF document to be copied
     * @return a Form XObject containing the original page's content
     * @throws IOException if an I/O error occurs
     */
    public PDFormXObject importPageAsForm(PDDocument sourceDoc, PDPage page) throws IOException
    {
        PDStream newStream = new PDStream(targetDoc, page.getContents(), COSName.FLATE_DECODE);
        PDFormXObject form = new PDFormXObject(newStream);

        //Copy resources
        PDResources pageRes = page.getResources();
        PDResources formRes = new PDResources();
        cloner.cloneMerge(pageRes, formRes);
        form.setResources(formRes);

        //Transfer some values from page to form
        transferDict(page.getCOSObject(), form.getCOSObject(), PAGE_TO_FORM_FILTER, true);

        Matrix matrix = form.getMatrix();
        AffineTransform at = matrix.createAffineTransform();
        PDRectangle mediaBox = page.getMediaBox();
        PDRectangle cropBox = page.getCropBox();
        PDRectangle viewBox = (cropBox != null ? cropBox : mediaBox);

        //Handle the /Rotation entry on the page dict
        int rotation = page.getRotation();

        //Transform to FOP's user space
        //at.scale(1 / viewBox.getWidth(), 1 / viewBox.getHeight());
        at.translate(mediaBox.getLowerLeftX() - viewBox.getLowerLeftX(),
                mediaBox.getLowerLeftY() - viewBox.getLowerLeftY());
        switch (rotation)
        {
        case 90:
            at.scale(viewBox.getWidth() / viewBox.getHeight(), viewBox.getHeight() / viewBox.getWidth());
            at.translate(0, viewBox.getWidth());
            at.rotate(-Math.PI / 2.0);
            break;
        case 180:
            at.translate(viewBox.getWidth(), viewBox.getHeight());
            at.rotate(-Math.PI);
            break;
        case 270:
            at.scale(viewBox.getWidth() / viewBox.getHeight(), viewBox.getHeight() / viewBox.getWidth());
            at.translate(viewBox.getHeight(), 0);
            at.rotate(-Math.PI * 1.5);
            break;
        default:
            //no additional transformations necessary
        }
        //Compensate for Crop Boxes not starting at 0,0
        at.translate(-viewBox.getLowerLeftX(), -viewBox.getLowerLeftY());
        if (!at.isIdentity())
        {
            form.setMatrix(at);
        }

        BoundingBox bbox = new BoundingBox();
        bbox.setLowerLeftX(viewBox.getLowerLeftX());
        bbox.setLowerLeftY(viewBox.getLowerLeftY());
        bbox.setUpperRightX(viewBox.getUpperRightX());
        bbox.setUpperRightY(viewBox.getUpperRightY());
        form.setBBox(new PDRectangle(bbox));

        return form;
    }

    /**
     * Places the given form over the existing content of the indicated page (like an overlay).
     * The form is enveloped in a marked content section to indicate that it's part of an
     * optional content group (OCG), here used as a layer. This optional group is returned and
     * can be enabled and disabled through methods on {@link PDOptionalContentProperties}.
     * @param targetPage the target page
     * @param form the form to place
     * @param transform the transformation matrix that controls the placement
     * @param layerName the name for the layer/OCG to produce
     * @return the optional content group that was generated for the form usage
     * @throws IOException if an I/O error occurs
     */
    public PDOptionalContentGroup appendFormAsLayer(PDPage targetPage,
            PDFormXObject form, AffineTransform transform,
            String layerName) throws IOException
    {
        PDDocumentCatalog catalog = targetDoc.getDocumentCatalog();
        PDOptionalContentProperties ocprops = catalog.getOCProperties();
        if (ocprops == null)
        {
            ocprops = new PDOptionalContentProperties();
            catalog.setOCProperties(ocprops);
        }
        if (ocprops.hasGroup(layerName))
        {
            throw new IllegalArgumentException("Optional group (layer) already exists: " + layerName);
        }

        PDOptionalContentGroup layer = new PDOptionalContentGroup(layerName);
        ocprops.addGroup(layer);

        PDPageContentStream contentStream = new PDPageContentStream(
                targetDoc, targetPage, AppendMode.APPEND, !DEBUG);
        contentStream.beginMarkedContent(COSName.OC, layer);
        contentStream.saveGraphicsState();
        contentStream.transform(new Matrix(transform));
        contentStream.drawForm(form);
        contentStream.restoreGraphicsState();
        contentStream.endMarkedContent();
        contentStream.close();

        return layer;
    }

    private void transferDict(COSDictionary orgDict, COSDictionary targetDict,
            Set<String> filter, boolean inclusive) throws IOException
    {
        for (Map.Entry<COSName, COSBase> entry : orgDict.entrySet())
        {
            COSName key = entry.getKey();
            if (inclusive && !filter.contains(key.getName()))
            {
                continue;
            }
            else if (!inclusive && filter.contains(key.getName()))
            {
                continue;
            }
            targetDict.setItem(key,
                    cloner.cloneForNewDocument(entry.getValue()));
        }
    }
}
