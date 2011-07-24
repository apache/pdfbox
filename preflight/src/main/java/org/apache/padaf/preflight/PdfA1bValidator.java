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

import java.io.IOException;
import java.util.ArrayList;

import javax.activation.DataSource;

import org.apache.padaf.preflight.javacc.PDFParser;
import org.apache.padaf.preflight.javacc.ParseException;
import org.apache.padaf.preflight.javacc.extractor.ExtractorTokenManager;
import org.apache.padaf.preflight.javacc.extractor.SimpleCharStream;

import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.helpers.AbstractValidationHelper;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfA1bValidator extends AbstractValidator {

	public PdfA1bValidator(ValidatorConfig cfg) throws ValidationException {
		super(cfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.PdfAValidator#validate(javax.activation.DataSource
	 * )
	 */
	public synchronized ValidationResult validate(DataSource source)
	throws ValidationException {
		DocumentHandler handler = createDocumentHandler(source);
		try {
			// syntax (javacc) validation
			try {
				PDFParser parser = new PDFParser(source.getInputStream());
				parser.PDF();
				handler.setParser(parser);
			} catch (IOException e) {
				throw new ValidationException("Failed to parse datasource due to : "
						+ e.getMessage(), e);
			} catch (ParseException e) {
				return createErrorResult(e);
			}

			// if here is reached, validate with helpers
			// init PDF Box document
			PDDocument document = null;
			try {
				document = PDDocument.load(handler.getSource().getInputStream());
				handler.setDocument(document);
			} catch (IOException e) {
				throw new ValidationException("PDFBox failed to parse datasource", e);
			}

			// init PDF Extractor
			try {
				SimpleCharStream scs = new SimpleCharStream(source.getInputStream());
				ExtractorTokenManager extractor = new ExtractorTokenManager(scs);
				extractor.parse();
				handler.setPdfExtractor(extractor);
			} catch (IOException e) {
				throw new ValidationException(
						"PDF ExtractorTokenMng failed to parse datasource", e);
			}

			// call all helpers
			ArrayList<ValidationError> allErrors = new ArrayList<ValidationError>();
		
			// Execute priority helpers.
			for ( AbstractValidationHelper helper : priorHelpers ) {
				runValidation(handler, helper, allErrors);	
			}

			// Execute other helpers.
			for ( AbstractValidationHelper helper : standHelpers ) {
				runValidation(handler, helper, allErrors);	
			}
			
			// check result
			ValidationResult valRes = null;
			if (allErrors.size() == 0) {
				valRes = new ValidationResult(true);
			} else {
				// there are some errors
				valRes = new ValidationResult(allErrors);
			}

			// addition of the some objects to avoid a second file parsing  
			valRes.setPdf(document);
			valRes.setXmpMetaData(handler.getMetadata());
			return valRes;
		} catch ( ValidationException e ) {
			// ---- Close all open resources if an error occurs.
			handler.close();
			throw e;
		}
	}

}
