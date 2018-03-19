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
     * @param params parameter values
     * @deprecated use {@link #createAffineTransform(java.awt.geom.AffineTransform) }
     */
    @Deprecated
    void createAffineTransform(byte[] params);

    /**
     * In order to create Affine Transform, using parameters.
     * 
     * @param affineTransform the transformation
     */
    void createAffineTransform(AffineTransform affineTransform);

    /**
     * Creates specified size page.
     * 
     * @param properties property value
     */
    void createPage(PDVisibleSignDesigner properties);

    /**
     * Creates template using page.
     * 
     * @param page the given page
     * @throws IOException if something went wrong
     */
    void createTemplate(PDPage page) throws IOException;

    /**
     * Creates Acro forms in the template.
     * 
     * @param template the template document
     */
    void createAcroForm(PDDocument template);

    /**
     * Creates signature fields.
     * 
     * @param acroForm the acroform
     * @throws IOException if something went wrong
     */
    void createSignatureField(PDAcroForm acroForm) throws IOException;

    /**
     * Creates the signature with the given name and assign it to the signature field parameter and assign the page
     * parameter to the widget.
     *
     * @param pdSignatureField signature filed
     * @param page the given page
     * @param signerName the name of the person or authority signing the document. According to the PDF specification,
     * this value should be used only when it is not possible to extract the name from the signature.
     * @throws IOException if something went wrong
     */
    void createSignature(PDSignatureField pdSignatureField, PDPage page, String signerName)
            throws IOException;

    /**
     * Create AcroForm Dictionary.
     * 
     * @param acroForm the acroform
     * @param signatureField the signature field
     * @throws IOException if something went wrong
     */
    void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField)
            throws IOException;

    /**
     * Creates SignatureRectangle.
     * 
     * @param signatureField the signature field
     * @param properties properties
     * @throws IOException if something went wrong
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
     * @param template template document
     * @param image signature image
     * @throws IOException if something went wrong
     */
    void createSignatureImage(PDDocument template, BufferedImage image) throws IOException;

    /**
     * An array of four numbers in the form coordinate system, giving the coordinates of the left, bottom, right, and
     * top edges, respectively, of the form XObject’s bounding box. These boundaries shall be used to clip the form
     * XObject and to determine its size for caching.
     *
     * @param params parameters
     * 
     * @deprecated use {@link #createFormatterRectangle(int[]) createFormatterRectangle(int[])}
     */
    @Deprecated
    void createFormatterRectangle(byte[] params);

    /**
     * An array of four numbers in the form coordinate system, giving the coordinates of the left, bottom, right, and
     * top edges, respectively, of the form XObject’s bounding box. These boundaries shall be used to clip the form
     * XObject and to determine its size for caching.
     *
     * @param params parameters
     */
    void createFormatterRectangle(int[] params);

    /**
     * 
     * @param template template document
     */
    void createHolderFormStream(PDDocument template);

    /**
     * Creates resources of form
     */
    void createHolderFormResources();

    /**
     * Creates Form
     * 
     * @param holderFormResources holder form resources
     * @param holderFormStream holder stream
     * @param bbox bounding box
     */
    void createHolderForm(PDResources holderFormResources, PDStream holderFormStream,
            PDRectangle bbox);

    /**
     * Creates appearance dictionary
     * 
     * @param holderForml holder XObject
     * @param signatureField the signature field
     * @throws IOException if something went wrong
     */
    void createAppearanceDictionary(PDFormXObject holderForml,
            PDSignatureField signatureField) throws IOException;

    /**
     * 
     * @param template template document
     */
    void createInnerFormStream(PDDocument template);

    /**
     * Creates InnerForm
     */
    void createInnerFormResource();

    /**
     * 
     * @param innerFormResources inner form resources
     * @param innerFormStream inner form stream
     * @param bbox bounding box
     */
    void createInnerForm(PDResources innerFormResources, PDStream innerFormStream, PDRectangle bbox);

    /**
     * 
     * @param innerForm inner form XObject
     * @param holderFormResources holder form resources
     */
    void insertInnerFormToHolderResources(PDFormXObject innerForm,
            PDResources holderFormResources);

    /**
     * 
     * @param template template document
     */
    void createImageFormStream(PDDocument template);

    /**
     * Create resource of image form
     */
    void createImageFormResources();

    /**
     * Creates Image form
     * 
     * @param imageFormResources image form resources
     * @param innerFormResource inner form resources
     * @param imageFormStream image from stream
     * @param bbox bounding box
     * @param affineTransform transformation
     * @param img ImageXObject
     * @throws IOException if something went wrong
     */
    void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
            PDStream imageFormStream, PDRectangle bbox, AffineTransform affineTransform,
            PDImageXObject img) throws IOException;

    /**
     * Creates the background layer form (n0).
     *
     * @param innerFormResource inner acroform resources
     * @param formatter rectangle of the formatter
     * @throws IOException if something went wrong
     */
    void createBackgroundLayerForm(PDResources innerFormResource, PDRectangle formatter)
            throws IOException;

    /**
     * Inject procSetArray
     * 
     * @param innerForm inner form
     * @param page the given page
     * @param innerFormResources inner form resources
     * @param imageFormResources image form resources
     * @param holderFormResources holder form resources
     * @param procSet procset values
     */
    void injectProcSetArray(PDFormXObject innerForm, PDPage page,
            PDResources innerFormResources, PDResources imageFormResources,
            PDResources holderFormResources, COSArray procSet);

    /**
     * injects appearance streams
     * 
     * @param holderFormStream holder form stream
     * @param innerFormStream inner form stream
     * @param imageFormStream image form stream
     * @param imageFormName image form name
     * @param imageName image name
     * @param innerFormName inner form name
     * @param properties property values
     * @throws IOException if something went wrong
     */
    void injectAppearanceStreams(PDStream holderFormStream, PDStream innerFormStream,
            PDStream imageFormStream, COSName imageFormName, COSName imageName,
            COSName innerFormName, PDVisibleSignDesigner properties) throws IOException;

    /**
     * just to create visible signature
     * 
     * @param template template document
     */
    void createVisualSignature(PDDocument template);

    /**
     * adds Widget Dictionary
     * 
     * @param signatureField the signature field
     * @param holderFormResources holder form resources
     * @throws IOException if something went wrong
     */
    void createWidgetDictionary(PDSignatureField signatureField,
            PDResources holderFormResources) throws IOException;

    /**
     * 
     * @return - PDF template Structure
     */
    PDFTemplateStructure getStructure();

    /**
     * Closes template
     * 
     * @param template template document
     * @throws IOException if something went wrong
     */
    void closeTemplate(PDDocument template) throws IOException;
}
