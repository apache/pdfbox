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

package org.apache.pdfbox.preflight.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import javax.activation.DataSource;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.PdfParseException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.javacc.ParseException;
import org.apache.pdfbox.preflight.javacc.extractor.ExtractorTokenManager;
import org.apache.pdfbox.preflight.javacc.extractor.SimpleCharStream;

public class PreflightParser extends PDFParser {
	/**
	 * Define a one byte encoding that hasn't specific encoding in UTF-8 charset.
	 * Avoid unexpected error when the encoding is Cp5816
	 */
	public static final Charset encoding = Charset.forName("ISO-8859-1");

	protected DataSource originalDocument;

	protected ValidationResult validationResult;

	protected PreflightDocument document;

	protected PreflightContext ctx;

	public PreflightParser(DataSource input, RandomAccess rafi, boolean force)	throws IOException {
		super(input.getInputStream(), rafi, force);
		this.originalDocument = input;
	}

	public PreflightParser(DataSource input, RandomAccess rafi) throws IOException {
		super(input.getInputStream(), rafi);
		this.originalDocument = input;
	}

	public PreflightParser(DataSource input) throws IOException {
		super(input.getInputStream());
		this.originalDocument = input;
	}


	/**
	 * Create an instance of ValidationResult. This object contains an instance of
	 * ValidationError. If the ParseException is an instance of PdfParseException,
	 * the embedded validation error is initialized with the error code of the
	 * exception, otherwise it is an UnknownError.
	 * 
	 * @param e
	 * @return
	 */
	protected static ValidationResult createErrorResult(ParseException e) {
		if (e instanceof PdfParseException) {
			if (e.getCause()==null) {
				return new ValidationResult(new ValidationError(((PdfParseException)e).getErrorCode()));
			} else if (e.getCause().getMessage()==null) {
				return new ValidationResult(new ValidationError(((PdfParseException)e).getErrorCode()));
			} else {
				return new ValidationResult(new ValidationError(((PdfParseException)e).getErrorCode(),e.getCause().getMessage()));
			}
		}
		return createUnknownErrorResult();
	}

	/**
	 * Create an instance of ValidationResult with a
	 * ValidationError(UNKNOWN_ERROR)
	 * 
	 * @return
	 */
	protected static ValidationResult createUnknownErrorResult() {
		ValidationError error = new ValidationError(PreflightConstants.ERROR_UNKOWN_ERROR);
		ValidationResult result = new ValidationResult(error);
		return result;
	}

	/**
	 * Add the error to the ValidationResult.
	 * If the validationResult is null, an instance is created using the isWarning boolean of the 
	 * ValidationError to know if the ValidationResult must be flagged as Valid.
	 * @param error
	 */
	protected void addValidationError(ValidationError error) {
		if (this.validationResult == null) {
			this.validationResult = new ValidationResult(error.isWarning());
		}
		this.validationResult.addError(error);
	}
	protected void addValidationErrors(List<ValidationError> errors) {
		for (ValidationError error : errors) {
			addValidationError(error);
		}
	}


	public void parse() throws IOException {
		parse(Format.PDF_A1B);
	}

	/**
	 * Parse the given file and check if it is a confirming file according to the given format.
	 * 
	 * @param format format that the document should follow (default {@link Format#PDF_A1B})
	 * @throws IOException
	 */
	public void parse(Format format) throws IOException {
		parse(format, null);
	}

	/**
	 * Parse the given file and check if it is a confirming file according to the given format.
	 * 
	 * @param format format that the document should follow (default {@link Format#PDF_A1B})
	 * @param config Configuration bean that will be used by the PreflightDocument. 
	 * If null the format is used to determine the default configuration. 
	 * @throws IOException
	 */
	public void parse(Format format, PreflightConfiguration config) throws IOException {
		checkFileSyntax();
		// run PDFBox Parser
		super.parse();
		Format formatToUse = (format == null ? Format.PDF_A1B : format);
		createPdfADocument(formatToUse, config);
		createContext();
		extractTrailers();
	}
	
	/**
	 * Run the JavaCC parser to check the PDF syntax.
	 * @throws ValidationException
	 */
	protected void checkFileSyntax() throws ValidationException {
		// syntax (javacc) validation
		try {
			InputStreamReader reader = new InputStreamReader(this.originalDocument.getInputStream(), encoding); 
			org.apache.pdfbox.preflight.javacc.PDFParser javaCCParser = new org.apache.pdfbox.preflight.javacc.PDFParser(reader);
			javaCCParser.PDF();
			IOUtils.closeQuietly(reader);
		} catch (IOException e) {
			throw new ValidationException("Failed to parse datasource due to : " + e.getMessage(), e);
		} catch (ParseException e) {
			this.validationResult = createErrorResult(e);
		}
	}

	protected void createPdfADocument(Format format, PreflightConfiguration config) throws IOException {
		this.document = new PreflightDocument(getDocument(), format, config);
	}

	protected void createContext() {
		this.ctx = new PreflightContext(this.originalDocument);
		ctx.setDocument(document);
		document.setContext(ctx);
	}

	protected void extractTrailers() throws IOException {
		SimpleCharStream scs = new SimpleCharStream(this.originalDocument.getInputStream());
		ExtractorTokenManager extractor = new ExtractorTokenManager(scs);
		extractor.parse();
		ctx.setPdfExtractor(extractor);
	}

	@Override
	public PDDocument getPDDocument() throws IOException {
		document.setResult(validationResult);
		// Add XMP MetaData
		return document;
	}
	
	public PreflightDocument getPreflightDocument() throws IOException {
		return (PreflightDocument)getPDDocument();
	}
}