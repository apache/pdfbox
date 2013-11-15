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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * Using that class, we  build pdf template
 * @author <a href="mailto:vakhtang.koroghlishvili@gmail.com"> vakhtang koroghlishvili (gogebashvili) </a>
 */
public class PDFTemplateCreator
{

    PDFTemplateBuilder pdfBuilder;
    private static final Log logger = LogFactory.getLog(PDFTemplateCreator.class);

    /**
     * sets PDFBuilder
     * 
     * @param bookBuilder
     */
    public PDFTemplateCreator(PDFTemplateBuilder bookBuilder)
    {
        this.pdfBuilder = bookBuilder;
    }

    /**
     * that method returns object of PDFStructur
     * 
     * @return PDFStructure
     */
    public PDFTemplateStructure getPdfStructure()
    {
        return this.pdfBuilder.getStructure();
    }

    /**
     * this method builds pdf  step by step, and finally it returns stream of visible signature
     * @param properties
     * @return InputStream
     * @throws IOException
     * @throws COSVisitorException
     */

    public InputStream buildPDF(PDVisibleSignDesigner properties) throws IOException
    {
        logger.info("pdf building has been started");
        PDFTemplateStructure pdfStructure = pdfBuilder.getStructure();

        // we create array of [Text, ImageB, ImageC, ImageI]
        this.pdfBuilder.createProcSetArray();
        
        //create page
        this.pdfBuilder.createPage(properties);
        PDPage page = pdfStructure.getPage();

        //create template
        this.pdfBuilder.createTemplate(page);
        PDDocument template = pdfStructure.getTemplate();
        
        //create /AcroForm
        this.pdfBuilder.createAcroForm(template);
        PDAcroForm acroForm = pdfStructure.getAcroForm();

        // AcroForm contains singature fields
        this.pdfBuilder.createSignatureField(acroForm);
        PDSignatureField pdSignatureField = pdfStructure.getSignatureField();
        
        // create signature
        this.pdfBuilder.createSignature(pdSignatureField, page, properties.getSignatureFieldName());
       
        // that is /AcroForm/DR entry
        this.pdfBuilder.createAcroFormDictionary(acroForm, pdSignatureField);
        
        // create AffineTransform
        this.pdfBuilder.createAffineTransform(properties.getAffineTransformParams());
        AffineTransform transform = pdfStructure.getAffineTransform();
       
        // rectangle, formatter, image. /AcroForm/DR/XObject contains that form
        this.pdfBuilder.createSignatureRectangle(pdSignatureField, properties);
        this.pdfBuilder.createFormaterRectangle(properties.getFormaterRectangleParams());
        PDRectangle formater = pdfStructure.getFormaterRectangle();
        this.pdfBuilder.createSignatureImage(template, properties.getImageStream());

        // create form stream, form and  resource. 
        this.pdfBuilder.createHolderFormStream(template);
        PDStream holderFormStream = pdfStructure.getHolderFormStream();
        this.pdfBuilder.createHolderFormResources();
        PDResources holderFormResources = pdfStructure.getHolderFormResources();
        this.pdfBuilder.createHolderForm(holderFormResources, holderFormStream, formater);
        
        // that is /AP entry the appearance dictionary.
        this.pdfBuilder.createAppearanceDictionary(pdfStructure.getHolderForm(), pdSignatureField);
        
        // inner formstream, form and resource (hlder form containts inner form)
        this.pdfBuilder.createInnerFormStream(template);
        this.pdfBuilder.createInnerFormResource();
        PDResources innerFormResource = pdfStructure.getInnerFormResources();
        this.pdfBuilder.createInnerForm(innerFormResource, pdfStructure.getInnterFormStream(), formater);
        PDXObjectForm innerForm = pdfStructure.getInnerForm();
       
        // inner form must be in the holder form as we wrote
        this.pdfBuilder.insertInnerFormToHolerResources(innerForm, holderFormResources);
        
        //  Image form is in this structure: /AcroForm/DR/FRM0/Resources/XObject/n0
        this.pdfBuilder.createImageFormStream(template);
        PDStream imageFormStream = pdfStructure.getImageFormStream();
        this.pdfBuilder.createImageFormResources();
        PDResources imageFormResources = pdfStructure.getImageFormResources();
        this.pdfBuilder.createImageForm(imageFormResources, innerFormResource, imageFormStream, formater, transform,
                pdfStructure.getJpedImage());
       
        // now inject procSetArray
        this.pdfBuilder.injectProcSetArray(innerForm, page, innerFormResource, imageFormResources, holderFormResources,
                pdfStructure.getProcSet());

        String imgFormName = pdfStructure.getImageFormName();
        String imgName = pdfStructure.getImageName();
        String innerFormName = pdfStructure.getInnerFormName();

        // now create Streams of AP
        this.pdfBuilder.injectAppearanceStreams(holderFormStream, imageFormStream, imageFormStream, imgFormName,
                imgName, innerFormName, properties);
        this.pdfBuilder.createVisualSignature(template);
        this.pdfBuilder.createWidgetDictionary(pdSignatureField, holderFormResources);
        
        ByteArrayInputStream in = null;
        try
        {
            in = pdfStructure.getTemplateAppearanceStream();
        }
        catch (COSVisitorException e)
        {
            logger.error("COSVisitorException: can't get apereance stream ", e);
        }
        logger.info("stream returning started, size= " + in.available());
        
        // we must close the document
        template.close();
        
        // return result of the stream 
        return in;

    }
}
