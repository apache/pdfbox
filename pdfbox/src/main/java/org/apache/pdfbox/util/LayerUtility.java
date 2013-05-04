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
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;

/**
 * This class allows to import pages as Form XObjects into a PDF file and use them to create
 * layers (optional content groups).
 *
 * @version $Revision$
 */
public class LayerUtility
{
    private static final boolean DEBUG = true;

    private PDDocument targetDoc;
    private PDFCloneUtility cloner;

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
        COSDictionary saveGraphicsStateDic = new COSDictionary();
        COSStream saveGraphicsStateStream = getDocument().getDocument().createCOSStream(saveGraphicsStateDic);
        OutputStream saveStream = saveGraphicsStateStream.createUnfilteredStream();
        saveStream.write("q\n".getBytes("ISO-8859-1"));
        saveStream.flush();

        COSStream restoreGraphicsStateStream = getDocument().getDocument().createCOSStream(saveGraphicsStateDic);
        OutputStream restoreStream = restoreGraphicsStateStream.createUnfilteredStream();
        restoreStream.write("Q\n".getBytes("ISO-8859-1"));
        restoreStream.flush();

        //Wrap the existing page's content in a save/restore pair (q/Q) to have a controlled
        //environment to add additional content.
        COSDictionary pageDictionary = page.getCOSDictionary();
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
    public PDXObjectForm importPageAsForm(PDDocument sourceDoc, int pageNumber) throws IOException
    {
        PDPage page = (PDPage)sourceDoc.getDocumentCatalog().getAllPages().get(pageNumber);
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
    public PDXObjectForm importPageAsForm(PDDocument sourceDoc, PDPage page) throws IOException
    {
        COSStream pageStream = (COSStream)page.getContents().getCOSObject();
        PDStream newStream = new PDStream(targetDoc,
                pageStream.getUnfilteredStream(), false);
        PDXObjectForm form = new PDXObjectForm(newStream);

        //Copy resources
        PDResources pageRes = page.findResources();
        PDResources formRes = new PDResources();
        cloner.cloneMerge(pageRes, formRes);
        form.setResources(formRes);

        //Transfer some values from page to form
        transferDict(page.getCOSDictionary(), form.getCOSStream(), PAGE_TO_FORM_FILTER, true);

        Matrix matrix = form.getMatrix();
        AffineTransform at = matrix != null ? matrix.createAffineTransform() : new AffineTransform();
        PDRectangle mediaBox = page.findMediaBox();
        PDRectangle cropBox = page.findCropBox();
        PDRectangle viewBox = (cropBox != null ? cropBox : mediaBox);

        //Handle the /Rotation entry on the page dict
        int rotation = getNormalizedRotation(page);

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
            PDXObjectForm form, AffineTransform transform,
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

        PDResources resources = targetPage.findResources();
        PDPropertyList props = resources.getProperties();
        if (props == null)
        {
            props = new PDPropertyList();
            resources.setProperties(props);
        }

        //Find first free resource name with the pattern "MC<index>"
        int index = 0;
        PDOptionalContentGroup ocg;
        COSName resourceName;
        do
        {
            resourceName = COSName.getPDFName("MC" + index);
            ocg = props.getOptionalContentGroup(resourceName);
            index++;
        } while (ocg != null);
        //Put mapping for our new layer/OCG
        props.putMapping(resourceName, layer);

        PDPageContentStream contentStream = new PDPageContentStream(
                targetDoc, targetPage, true, !DEBUG);
        contentStream.beginMarkedContentSequence(COSName.OC, resourceName);
        contentStream.drawXObject(form, transform);
        contentStream.endMarkedContentSequence();
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

    private static int getNormalizedRotation(PDPage page)
    {
        //Handle the /Rotation entry on the page dict
        int rotation = page.findRotation();
        while (rotation >= 360)
        {
            rotation -= 360;
        }
        if (rotation < 0)
        {
            rotation = 0;
        }
        switch (rotation)
        {
        case 90:
        case 180:
        case 270:
            return rotation;
        default:
            return 0;
        }
    }


}
