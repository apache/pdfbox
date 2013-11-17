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
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * That class builds visible signature template
 * which will be added in our pdf document
 * @author Vakhtang koroghlishvili (Gogebashvili)
 *
 */
public interface PDFTemplateBuilder {

    /**
     * In order to create  Affine Transform, using parameters
     * @param params
     */
	public void createAffineTransform(byte [] params);
	
	/**
	 * Creates specified size page
	 * @param properties
	 */
	public void createPage(PDVisibleSignDesigner properties);
	
	/**
	 * Creates template using page
	 * @param page
	 * @throws IOException
	 */
	public void createTemplate(PDPage page) throws IOException;
	
	/**
	 * Creates Acro forms in the template
	 * @param template
	 */
	public void createAcroForm(PDDocument template);
	
	/**
	 * Creates signature fields
	 * @param acroForm
	 * @throws IOException
	 */
	public void createSignatureField(PDAcroForm acroForm) throws IOException;
	
	/**
	 * Creates PDSignature
	 * @param pdSignatureField
	 * @param page
	 * @param signatureName
	 * @throws IOException
	 */
	public void createSignature(PDSignatureField pdSignatureField, PDPage page, String signatureName) throws IOException;
	
	/**
	 * Create AcroForm Dictionary
	 * @param acroForm
	 * @param signatureField
	 * @throws IOException
	 */
	public void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField) throws IOException;
	
	/**
	 * Creates SingatureRectangle
	 * @param signatureField
	 * @param properties
	 * @throws IOException
	 */
	public void createSignatureRectangle(PDSignatureField signatureField, PDVisibleSignDesigner properties) throws IOException;
	
	/**
	 * Creates procSetArray of PDF,Text,ImageB,ImageC,ImageI    
	 */
	public void createProcSetArray();
	
    /**
     * Creates signature image
     * @param template
     * @param InputStream
     * @throws IOException
     */
	public void createSignatureImage(PDDocument template, InputStream InputStream) throws IOException;
	
	/**
	 * 
	 * @param params
	 */
	public void createFormaterRectangle(byte [] params);
	
	/**
	 * 
	 * @param template
	 */
	public void createHolderFormStream(PDDocument template);
	
	/**
	 * Creates resources of form
	 */
	public void createHolderFormResources();
	
	/**
	 * Creates Form
	 * @param holderFormResources
	 * @param holderFormStream
	 * @param formrect
	 */
	public void createHolderForm(PDResources holderFormResources, PDStream holderFormStream, PDRectangle formrect);
	
	/**
	 * Creates appearance dictionary
	 * @param holderForml
	 * @param signatureField
	 * @throws IOException
	 */
	public void createAppearanceDictionary(PDXObjectForm holderForml, PDSignatureField signatureField) throws IOException;
	
	/**
	 * 
	 * @param template
	 */
	public void createInnerFormStream(PDDocument template);
	
	
	/**
	 * Creates InnerForm
	 */
	public void createInnerFormResource();
	
	/**
	 * 
	 * @param innerFormResources
	 * @param innerFormStream
	 * @param formrect
	 */
	public void createInnerForm(PDResources innerFormResources, PDStream innerFormStream, PDRectangle formrect);
	
	
	/**
	 * 
	 * @param innerForm
	 * @param holderFormResources
	 */
	public void insertInnerFormToHolerResources(PDXObjectForm innerForm, PDResources holderFormResources);
	
	/**
	 * 
	 * @param template
	 */
	public void createImageFormStream(PDDocument template);
	
	/**
	 * Create resource of image form
	 */
	public void createImageFormResources();
	
	/**
	 * Creates Image form
	 * @param imageFormResources
	 * @param innerFormResource
	 * @param imageFormStream
	 * @param formrect
	 * @param affineTransform
	 * @param img
	 * @throws IOException
	 */
	public void createImageForm(PDResources imageFormResources, PDResources innerFormResource, PDStream imageFormStream, PDRectangle formrect,
			AffineTransform affineTransform, PDJpeg img) throws IOException;
	
	/**
	 * Inject procSetArray 
	 * @param innerForm
	 * @param page
	 * @param innerFormResources
	 * @param imageFormResources
	 * @param holderFormResources
	 * @param procSet
	 */
	public void injectProcSetArray(PDXObjectForm innerForm, PDPage page, PDResources innerFormResources, PDResources imageFormResources,
			PDResources holderFormResources, COSArray procSet);
	
	/**
	 * injects appearance streams
	 * @param holderFormStream
	 * @param innterFormStream
	 * @param imageFormStream
	 * @param imageObjectName
	 * @param imageName
	 * @param innerFormName
	 * @param properties
	 * @throws IOException
	 */
	public void injectAppearanceStreams(PDStream holderFormStream, PDStream innterFormStream, PDStream imageFormStream, String imageObjectName,
			String imageName, String innerFormName, PDVisibleSignDesigner properties) throws IOException;
	
	/**
	 * just to create visible signature
	 * @param template
	 */
	public void createVisualSignature(PDDocument template);
	
	/**
	 * adds Widget Dictionary
	 * @param signatureField
	 * @param holderFormResources
	 * @throws IOException
	 */
	public void createWidgetDictionary(PDSignatureField signatureField, PDResources holderFormResources) throws IOException;
	
	/**
	 * 
	 * @return - PDF template Structure
	 */
	public PDFTemplateStructure getStructure();
	
	/**
	 * Closes template
	 * @param template
	 * @throws IOException
	 */
	public void closeTemplate(PDDocument template) throws IOException;
}
