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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * That class builds visible signature template which will be added in our PDF document.
 * @author Vakhtang Koroghlishvili
 */
public interface PDFTemplateBuilder
{
    /**
     * In order to create Affine Transform, using parameters.
     * 
     * @param affineTransform the affine transformation to be used
     */
    void createAffineTransform(AffineTransform affineTransform);

    /**
     * Creates specified size page.
     * 
     * @param properties page properties
     */
    void createPage(PDVisibleSignDesigner properties);

    /**
     * Creates template using page.
     * 
     * @param page page to be added
     * @throws IOException if the template could not be created
     */
    void createTemplate(PDPage page) throws IOException;

    /**
     * Creates Acro forms in the template.
     * 
     * @param template the document the acroform is added to
     */
    void createAcroForm(PDDocument template);

    /**
     * Creates signature fields.
     * 
     * @param acroForm the acroform the signature is added to
     * @throws IOException if the signature could not be created
     */
    void createSignatureField(PDAcroForm acroForm) throws IOException;

    /**
     * Creates the signature with the given name and assign it to the signature field parameter and assign the page
     * parameter to the widget.
     *
     * @param pdSignatureField the signature filed the signatur is added to
     * @param page the page the widgt ist added to
     * @param signerName the name of the person or authority signing the document. According to the PDF specification,
     * this value should be used only when it is not possible to extract the name from the signature.
     * @throws IOException if the signature could not be created
     */
    void createSignature(PDSignatureField pdSignatureField, PDPage page, String signerName)
            throws IOException;

    /**
     * Create AcroForm Dictionary.
     * 
     * @param acroForm the acroform the signature field is added to
     * @param signatureField the signature filed to be added
     * @throws IOException if the signature field could not be added
     */
    void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField)
            throws IOException;

    /**
     * Creates SignatureRectangle.
     * 
     * @param signatureField the signature field the rectangle is added to
     * @param properties the properties used to create the rectangle
     * @throws IOException if the rectangle could not be created
     */
    void createSignatureRectangle(PDSignatureField signatureField,
            PDVisibleSignDesigner properties) throws IOException;

    /**
     * Creates procSetArray of PDF,Text,ImageB,ImageC,ImageI.
     */
    void createProcSetArray();

    /**
     * Creates signature image.
     * 
     * @param template the document the image is added to
     * @param image to imager to be added
     * @throws IOException if the image could not be added
     */
    void createSignatureImage(PDDocument template, BufferedImage image) throws IOException;

    /**
     * An array of four numbers in the form coordinate system, giving the coordinates of the left, bottom, right, and
     * top edges, respectively, of the form XObject’s bounding box. These boundaries shall be used to clip the form
     * XObject and to determine its size for caching.
     *
     * @param params the parameters of the formatter rectangle
     */
    void createFormatterRectangle(int[] params);

    /**
     * Create a holder for the form stream.
     * 
     * @param template the document to be used to create the new stream
     */
    void createHolderFormStream(PDDocument template);

    /**
     * Creates resources of form
     */
    void createHolderFormResources();

    /**
     * Creates Form
     * 
     * @param holderFormResources resources to be used for the form object
     * @param holderFormStream the stream to be used for the form object
     * @param bbox the bounding box of the form object
     */
    void createHolderForm(PDResources holderFormResources, PDStream holderFormStream,
            PDRectangle bbox);

    /**
     * Creates appearance dictionary
     * 
     * @param holderForml form object to be used for the appearance stream
     * @param signatureField the signature field the appearance stream is added to
     * @throws IOException if the appearance stream could not be created
     */
    void createAppearanceDictionary(PDFormXObject holderForml,
            PDSignatureField signatureField) throws IOException;

    /**
     * Create a holder for the inner form stream.
     * 
     * @param template the document to be used to create the new stream
     */
    void createInnerFormStream(PDDocument template);

    /**
     * Creates InnerForm
     */
    void createInnerFormResource();

    /**
     * Creates InnerForm.
     * 
     * @param innerFormResources resources to be used for the inner form object
     * @param innerFormStream the stream to be used for the inner form object
     * @param bbox the bounding box of the inner form object
     */
    void createInnerForm(PDResources innerFormResources, PDStream innerFormStream, PDRectangle bbox);

    /**
     * Insert given from as inner form.
     * 
     * @param innerForm the form object to be inserted
     * @param holderFormResources resources the fomr object is added to
     */
    void insertInnerFormToHolderResources(PDFormXObject innerForm,
            PDResources holderFormResources);

    /**
     * Create image form stream.
     * 
     * @param template the document to be used to create the new stream
     */
    void createImageFormStream(PDDocument template);

    /**
     * Create resource of image form
     */
    void createImageFormResources();

    /**
     * Creates Image form
     * 
     * @param imageFormResources the resources of the form object
     * @param innerFormResource the resources the image object is added
     * @param imageFormStream the stream of the form object
     * @param bbox the bounding box of the form object
     * @param affineTransform the matrix of the form object
     * @param img the image object to be used for the form object
     * @throws IOException if the form object could not be created
     */
    void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
            PDStream imageFormStream, PDRectangle bbox, AffineTransform affineTransform,
            PDImageXObject img) throws IOException;

    /**
     * Creates the background layer form (n0).
     *
     * @param innerFormResource resources to be used for the form object
     * @param bbox the bounding box of the form object
     * @throws IOException if the form object could not be created
     */
    void createBackgroundLayerForm(PDResources innerFormResource, PDRectangle bbox)
            throws IOException;

    /**
     * Inject procSetArray
     * 
     * @param innerForm form object the given proc set array is added to
     * @param page page the given proc set array is added to
     * @param innerFormResources inner form resources the given proc set array is added to
     * @param imageFormResources inner image resources the given proc set array is added to
     * @param holderFormResources holder form resources the given proc set array is added to
     * @param procSet the pro set array to be added
     */
    void injectProcSetArray(PDFormXObject innerForm, PDPage page,
            PDResources innerFormResources, PDResources imageFormResources,
            PDResources holderFormResources, COSArray procSet);

    /**
     * injects appearance streams
     * 
     * @param holderFormStream the holder form stream
     * @param innerFormStream the inner form stream
     * @param imageFormStream the image form stream
     * @param imageFormName the name of the form image to be used
     * @param imageName the name of the image to be used
     * @param innerFormName the name of the form object to be used
     * @param properties properties to be used to create the appearance stream
     * @throws IOException if the appearance stream could not be created
     */
    void injectAppearanceStreams(PDStream holderFormStream, PDStream innerFormStream,
            PDStream imageFormStream, COSName imageFormName, COSName imageName,
            COSName innerFormName, PDVisibleSignDesigner properties) throws IOException;

    /**
     * just to create visible signature
     * 
     * @param template the document holding the visible signatue
     */
    void createVisualSignature(PDDocument template);

    /**
     * adds Widget Dictionary
     * 
     * @param signatureField the field to be used as widget dictionary
     * @param holderFormResources the resources to be added to the widget dictionary
     * @throws IOException if the widget dictionary could not be created
     */
    void createWidgetDictionary(PDSignatureField signatureField,
            PDResources holderFormResources) throws IOException;

    /**
     * Resturns the PDF template Structure
     * 
     * @return PDF template Structure
     */
    PDFTemplateStructure getStructure();

    /**
     * Closes template
     * 
     * @param template the document to be closed
     * 
     * @throws IOException if the document could not be closed
     */
    void closeTemplate(PDDocument template) throws IOException;
}
