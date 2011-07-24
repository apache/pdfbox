/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.preflight;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;

import org.apache.padaf.preflight.javacc.PDFParser;
import org.apache.padaf.preflight.javacc.extractor.ExtractorTokenManager;

import org.apache.padaf.preflight.font.AbstractFontContainer;
import org.apache.padaf.preflight.graphics.ICCProfileWrapper;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * The DocumentHandler is used to store useful information or Objects during the
 * validation of the PDF file.
 */
public class DocumentHandler {
	/**
	 * Contains the list of font name embedded in the PDF document.
	 */
	protected Map<COSBase, AbstractFontContainer> embeddedFonts = new HashMap<COSBase, AbstractFontContainer>();

	/**
	 * The javacc parser used to parse the document
	 */
	protected PDFParser parser = null;

	/**
	 * The PDFbox object representation of the PDF source.
	 */
	protected PDDocument document = null;

	/**
	 * The datasource to load the document from
	 */
	protected DataSource source = null;

	/**
	 * JavaCC Token Manager used to get some content of the PDF file as string (ex
	 * : Trailers)
	 */
	protected ExtractorTokenManager pdfExtractor = null;

	/**
	 * This wrapper contains the ICCProfile used by the PDF file.
	 */
	protected ICCProfileWrapper iccProfileWrapper = null;
	/**
	 * MetaData of the current pdf file. 
	 */
	protected XMPMetadata metadata = null;

	/**
	 * Create the DocumentHandler using the DataSource which represent the PDF
	 * file to check.
	 * 
	 * @param source
	 */
	protected DocumentHandler(DataSource source) {
		this.source = source;
	}

	/**
	 * @return the metadata
	 */
	public XMPMetadata getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(XMPMetadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the PDFParser used to parse the document
	 */
	public PDFParser getParser() {
		return parser;
	}

	/**
	 * Initialize the JavaCC parser which checks the syntax of the PDF.
	 * 
	 * @param parser
	 */
	protected void setParser(PDFParser parser) {
		this.parser = parser;
	}

	/**
	 * @return the value of the pdfExtractor attribute.
	 */
	public ExtractorTokenManager getPdfExtractor() {
		return pdfExtractor;
	}

	/**
	 * Initialize the pdfExtractor attribute.
	 * 
	 * @param pdfExtractor
	 */
	protected void setPdfExtractor(ExtractorTokenManager pdfExtractor) {
		this.pdfExtractor = pdfExtractor;
	}

	/**
	 * @return the PDFBox object representation of the document
	 */
	public PDDocument getDocument() {
		return document;
	}

	/**
	 * Initialize the PDFBox object which present the PDF File.
	 * 
	 * @param document
	 */
	protected void setDocument(PDDocument document) {
		this.document = document;
	}

	/**
	 * 
	 * @return The datasource of the pdf document
	 */
	public DataSource getSource() {
		return source;
	}

	public boolean isComplete() {
		return (document != null) && (source != null) && (parser != null);
	}

	/**
	 * Add a FontContainer to allow TextObject validation.
	 * 
	 * @param fKey
	 * @param fc
	 */
	public void addFont(COSBase fKey, AbstractFontContainer fc) {
		this.embeddedFonts.put(fKey, fc);
	}



	/**
	 * Return the FontContainer identified by the COSBase. If the given object
	 * is missing from the emmbeddedFont map, the null value is returned.
	 * 
	 * @param fKey
	 * @return
	 */
	public AbstractFontContainer getFont(COSBase fKey) {
		return this.embeddedFonts.get(fKey);
	}

	/**
	 * @return the iccProfileWrapper
	 */
	public ICCProfileWrapper getIccProfileWrapper() {
		return iccProfileWrapper;
	}

	/**
	 * @param iccProfileWrapper
	 *          the iccProfileWrapper to set
	 */
	public void setIccProfileWrapper(ICCProfileWrapper iccProfileWrapper) {
		this.iccProfileWrapper = iccProfileWrapper;
	}

	/**
	 * Close all opened resources
	 */
	public void close() {
		COSUtils.closeDocumentQuietly(document);
	}
}
