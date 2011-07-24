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

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Object returned by the validate method of the PDFValidator. This object
 * contains a boolean to know if the PDF is PDF/A-1<I>x</I> compliant. If the
 * document isn't PDF/A-1<I>x</I> a list of errors is provided.
 */
public class ValidationResult {
	/**
	 * Boolean to know if the PDF is a valid PDF/A
	 */
	private boolean isValid = false;

	/**
	 * Errors to know why the PDF isn't valid. If the PDF is valid, this list is
	 * empty.
	 */
	private List<ValidationError> lErrors = new ArrayList<ValidationError>();

	/**
	 * Object representation of the PDF file.
	 * This object has to be closed explicitly by the user using the
	 * close method of the ValidationResult object or directly by the 
	 * close method of the PDDocument object.
	 * 
	 * This attribute can be null if the Validation fails during the 
	 * validation of the document syntax using the PDFParser object.
	 */
	private PDDocument pdf = null;

	/**
	 * Object representation of the XMPMetaData contained by the pdf file
	 * This attribute can be null if the Validation fails.
	 */
	private XMPMetadata xmpMetaData = null;

	/**
	 * Create a Validation result object
	 * 
	 * @param isValid
	 */
	public ValidationResult(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * Create a Validation Result object. This constructor force the isValid to
	 * false and add the given error to the list or ValidationErrors.
	 * 
	 * @param error
	 *          if error is null, no error is added to the list.
	 */
	public ValidationResult(ValidationError error) {
		this.isValid = false;
		if (error != null) {
			this.lErrors.add(error);
		}
	}

	/**
	 * Create a Validation Result object. This constructor force the isValid to
	 * false and add all the given errors to the list or ValidationErrors.
	 * 
	 * @param error
	 *          if error is null, no error is added to the list.
	 */
	public ValidationResult(List<ValidationError> errors) {
		this.isValid = false;
		this.lErrors = errors;
	}

	/**
	 * @return the xmpMetaData
	 */
	public XMPMetadata getXmpMetaData() {
		return xmpMetaData;
	}

	/**
	 * @param xmpMetaData the xmpMetaData to set
	 */
	void setXmpMetaData(XMPMetadata xmpMetaData) {
		this.xmpMetaData = xmpMetaData;
	}

	/**
	 * @return the pdf
	 */
	public PDDocument getPdf() {
		return pdf;
	}

	/**
	 * @param pdf the pdf to set
	 */
	void setPdf(PDDocument pdf) {
		this.pdf = pdf;
	}

	/**
	 * Close the instance of PDDocument contained by this 
	 * ValidationResult object
	 */
	public void closePdf() {
		COSUtils.closeDocumentQuietly(this.pdf);
	}
	
	/**
	 * @return true if the PDF is valid,false otherwise
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Add error to the list of ValidationError. If the given error is null, this
	 * method does nothing
	 * 
	 * @param error
	 */
	public void addError(ValidationError error) {
		if (error != null) {
			this.lErrors.add(error);
		}
	}

	/**
	 * @return the list of validation errors
	 */
	public List<ValidationError> getErrorsList() {
		return this.lErrors;
	}

	/**
	 * This Class represents an error of validation. It contains an error code and
	 * an error explanation.
	 */
	public static class ValidationError {
		/**
		 * Error identifier. This error code can be used as identifier to
		 * internationalize the logging message using i18n.
		 */
		private String errorCode;

		/**
		 * Error details
		 */
		private String details;

		/**
		 * Create a validation error with the given error code
		 * 
		 * @param errorCode
		 */
		public ValidationError(String errorCode) {
			this.errorCode = errorCode;
			if (errorCode.startsWith(ValidationConstants.ERROR_SYNTAX_COMMON)){
				this.details = "Syntax error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_SYNTAX_HEADER)){
				this.details = "Body Syntax error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_SYNTAX_BODY)){
				this.details = "Body Syntax error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_SYNTAX_CROSS_REF)){
				this.details = "CrossRef Syntax error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_SYNTAX_TRAILER)){
				this.details = "Trailer Syntax error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_GRAPHIC_INVALID)){
				this.details = "Invalid Graphis object";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_GRAPHIC_TRANSPARENCY)){
				this.details = "Invalid Graphis transparency";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY)){
				this.details = "Unexpected key in Graphic object definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE)){
				this.details = "Invalid Color space";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_FONTS_INVALID_DATA)){
				this.details = "Invalid Font definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_FONTS_DAMAGED)){
				this.details = "Font damaged";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_FONTS_GLYPH)){
				this.details = "Glyph error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_TRANSPARENCY_EXT_GRAPHICAL_STATE)){
				this.details = "Transparency error";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_ANNOT_MISSING_FIELDS)){
				this.details = "Missing field in an annotation definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_ANNOT_FORBIDDEN_ELEMENT)){
				this.details = "Forbidden field in an annotation definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_ANNOT_INVALID_ELEMENT)){
				this.details = "Invalid field value in an annotation definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_ACTION_INVALID_ACTIONS)){
				this.details = "Invalid action definition";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_ACTION_FORBIDDEN_ACTIONS)){
				this.details = "Action is forbidden";
			} else if (errorCode.startsWith(ValidationConstants.ERROR_METADATA_MAIN)){
				this.details = "Error on MetaData";
			}
		}

		/**
		 * Create a validation error with the given error code and the error
		 * explanation.
		 * 
		 * @param errorCode
		 *          the error code
		 * @param details
		 *          the error explanation
		 */
		public ValidationError(String errorCode, String details) {
			this(errorCode);
			StringBuilder sb = new StringBuilder(this.details.length()+details.length()+2);
			sb.append(this.details).append(", ").append(details);
			this.details = sb.toString();
		}

		/**
		 * @return the error code
		 */
		public String getErrorCode() {
			return errorCode;
		}

		/**
		 * @return the error explanation
		 */
		public String getDetails() {
			return details;
		}

		/**
		 * Set the error explanation
		 * 
		 * @param details
		 */
		public void setDetails(String details) {
			this.details = details;
		}

	}
}
