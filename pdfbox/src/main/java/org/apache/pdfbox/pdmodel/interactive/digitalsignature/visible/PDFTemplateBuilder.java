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
     * @param params
     * @deprecated use {@link #createAffineTransform(java.awt.geom.AffineTransform) }
     */
    @Deprecated
    void createAffineTransform(byte[] params);

    /**
     * In order to create Affine Transform, using parameters.
     * @param affineTransform
     */
    void createAffineTransform(AffineTransform affineTransform);

    /**
     * Creates specified size page.
     * 
     * @param properties
     */
    void createPage(PDVisibleSignDesigner properties);

    /**
     * Creates template using page.
     * 
     * @param page
     * @throws IOException
     */
    void createTemplate(PDPage page) throws IOException;

    /**
     * Creates Acro forms in the template.
     * 
     * @param template
     */
    void createAcroForm(PDDocument template);

    /**
     * Creates signature fields.
     * 
     * @param acroForm
     * @throws IOException
     */
    void createSignatureField(PDAcroForm acroForm) throws IOException;

    /**
     * Creates the signature with the given name and assign it to the signature field parameter and
     * assign the page parameter to the widget.
     *
     * @param pdSignatureField
     * @param page
     * @param signerName the name of the person or authority signing the document. According to the
     * PDF specification, this value should be used only when it is not possible to extract the name
     * from the signature.
     * @throws IOException
     */
    void createSignature(PDSignatureField pdSignatureField, PDPage page, String signerName)
            throws IOException;

    /**
     * Create AcroForm Dictionary.
     * 
     * @param acroForm
     * @param signatureField
     * @throws IOException
     */
    void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField)
            throws IOException;

    /**
     * Creates SignatureRectangle.
     * 
     * @param signatureField
     * @param properties
     * @throws IOException
     */
    void createSignatureRectangle(PDSignatureField signatureField,
            PDVisibleSignDesigner properties) throws IOException;

    /**
     * Creates procSetArray of PDF,Text,ImageB,ImageC,ImageI.
     */
    void createProcSetArray();

    /**
     * Creates signature image.
     * @param template
     * @param image
     * @throws IOException
     */
    void createSignatureImage(PDDocument template, BufferedImage image) throws IOException;

    /**
     * 
     * @param params
     */
    void createFormatterRectangle(byte[] params);

    /**
     * 
     * @param template
     */
    void createHolderFormStream(PDDocument template);

    /**
     * Creates resources of form
     */
    void createHolderFormResources();

    /**
     * Creates Form
     * 
     * @param holderFormResources
     * @param holderFormStream
     * @param formrect
     */
    void createHolderForm(PDResources holderFormResources, PDStream holderFormStream,
            PDRectangle formrect);

    /**
     * Creates appearance dictionary
     * 
     * @param holderForml
     * @param signatureField
     * @throws IOException
     */
    void createAppearanceDictionary(PDFormXObject holderForml,
            PDSignatureField signatureField) throws IOException;

    /**
     * 
     * @param template
     */
    void createInnerFormStream(PDDocument template);

    /**
     * Creates InnerForm
     */
    void createInnerFormResource();

    /**
     * 
     * @param innerFormResources
     * @param innerFormStream
     * @param formrect
     */
    void createInnerForm(PDResources innerFormResources, PDStream innerFormStream,
            PDRectangle formrect);

    /**
     * 
     * @param innerForm
     * @param holderFormResources
     */
    void insertInnerFormToHolderResources(PDFormXObject innerForm,
            PDResources holderFormResources);

    /**
     * 
     * @param template
     */
    void createImageFormStream(PDDocument template);

    /**
     * Create resource of image form
     */
    void createImageFormResources();

    /**
     * Creates Image form
     * 
     * @param imageFormResources
     * @param innerFormResource
     * @param imageFormStream
     * @param formrect
     * @param affineTransform
     * @param img
     * @throws IOException
     */
    void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
            PDStream imageFormStream, PDRectangle formrect, AffineTransform affineTransform,
            PDImageXObject img) throws IOException;

    /**
     * Inject procSetArray
     * 
     * @param innerForm
     * @param page
     * @param innerFormResources
     * @param imageFormResources
     * @param holderFormResources
     * @param procSet
     */
    void injectProcSetArray(PDFormXObject innerForm, PDPage page,
            PDResources innerFormResources, PDResources imageFormResources,
            PDResources holderFormResources, COSArray procSet);

    /**
     * injects appearance streams
     * 
     * @param holderFormStream
     * @param innerFormStream
     * @param imageFormStream
     * @param imageObjectName
     * @param imageName
     * @param innerFormName
     * @param properties
     * @throws IOException
     */
    void injectAppearanceStreams(PDStream holderFormStream, PDStream innerFormStream,
            PDStream imageFormStream, COSName imageObjectName, COSName imageName,
            COSName innerFormName, PDVisibleSignDesigner properties) throws IOException;

    /**
     * just to create visible signature
     * 
     * @param template
     */
    void createVisualSignature(PDDocument template);

    /**
     * adds Widget Dictionary
     * 
     * @param signatureField
     * @param holderFormResources
     * @throws IOException
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
     * @param template
     * @throws IOException
     */
    void closeTemplate(PDDocument template) throws IOException;
}
