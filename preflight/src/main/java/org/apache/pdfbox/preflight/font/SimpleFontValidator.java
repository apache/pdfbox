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

package org.apache.pdfbox.preflight.font;


import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorAFM;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

public abstract class SimpleFontValidator extends AbstractFontValidator {
	protected String basefont;
	protected int firstChar;
	protected int lastChar;
	protected List<Float> widths;
	/**
	 * The PdfBox font descriptor dictionary wrapper.
	 */
	protected PDFontDescriptor pFontDesc = null;

	public SimpleFontValidator(PreflightContext context, PDFont font)
			throws ValidationException {
		super(context, font);
		this.pFontDesc = font.getFontDescriptor();
	}

	/**
	 * Extract element from the COSObject to avoid useless access to this object.
	 */
	private void extractElementsToCheck() {
		// here is required elements
		this.basefont = pFont.getBaseFont();
		this.firstChar = pFont.getFirstChar();
		this.lastChar = pFont.getLastChar();
		this.widths = pFont.getWidths();
	}

	/**
	 * Check if All required fields of a Font Dictionary are present. If there are
	 * some missing fields, this method returns false and the FontContainer is
	 * updated.
	 * 
	 * @return
	 */
	protected boolean checkMandatoryFields() {
		String type = pFont.getBaseFont();
		String subtype = pFont.getSubType();

		if (this.pFontDesc == null) {
			this.fontContainer
			.addError(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
					"The FontDescriptor is missing, so the Font Program isn't embedded."));
			this.fontContainer.setFontProgramEmbedded(false);
			return false;
		}

		if ((type == null || "".equals(type))
				|| (subtype == null || "".equals(subtype))) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"Type and/or Subtype keys are missing"));
			return false;
		} else {
			extractElementsToCheck();
			// ---- according to the subtype, the validation process isn't the same.
			return checkSpecificMandatoryFields();
		}
	}

	/**
	 * This method checks the presence of some fields according to the Font type
	 * 
	 * @return
	 */
	protected abstract boolean checkSpecificMandatoryFields();

	/**
	 * Check if the widths array contains integer and if its length is valid. If
	 * the validation fails, the FontContainer is updated.
	 * 
	 * @param cDoc
	 */
	protected boolean checkWidthsArray(COSDocument cDoc) {
		if (widths == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "The Widths array is unreachable"));
			return false;
		}

		int expectedLength = (lastChar - firstChar) + 1;
		if (widths.size() != expectedLength) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"The length of Witdhs array is invalid. Expected : \""
							+ expectedLength + "\" Current : \"" + widths.size() + "\""));
			return false;
		}
		return true;
	}

	protected boolean checkEncoding(COSDocument cDoc) {
		return true;
	}

	protected boolean checkToUnicode(COSDocument cDoc) {
		// Check the toUnicode -- Useless for PDF/A 1-b
		return true;
	}

	/**
	 * This method checks the font descriptor dictionary and embedded font files.
	 * If the FontDescriptor validation fails, the FontContainer is updated.
	 * 
	 * @return
	 */
	protected abstract boolean checkFontDescriptor() throws ValidationException;

	/**
	 * Check if all required fields are present in the PDF file to describe the
	 * Font Descriptor. If validation fails, FontConatiner is updated and false is
	 * returned.
	 */
	protected boolean checkFontDescriptorMandatoryFields() throws ValidationException {
		boolean fname = false, flags = false, itangle = false, cheight = false;
		boolean fbbox = false, asc = false, desc = false, stemv = false;

		if(pFontDesc instanceof PDFontDescriptorDictionary) {
			COSDictionary fDescriptor = ((PDFontDescriptorDictionary) pFontDesc).getCOSDictionary();
			for (Object key : fDescriptor.keySet()) {
				if (!(key instanceof COSName)) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							PreflightConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
							"Invalid key in The font descriptor"));
					return false;
				}

				String cosName = ((COSName) key).getName();
				if (cosName.equals(FONT_DICTIONARY_KEY_FONTNAME)) {
					fname = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_FLAGS)) {
					flags = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_ITALICANGLE)) {
					itangle = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_CAPHEIGHT)) {
					cheight = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_FONTBBOX)) {
					fbbox = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_ASCENT)) {
					asc = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_DESCENT)) {
					desc = true;
				}
				if (cosName.equals(FONT_DICTIONARY_KEY_STEMV)) {
					stemv = true;
				}
			}

			if (!(fname && flags && itangle && cheight && fbbox && asc && desc && stemv)) {
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_DESCRIPTOR_INVALID, "Some mandatory fields are missing"));
				return false;
			}
		} else if(pFontDesc instanceof PDFontDescriptorAFM) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DESCRIPTOR_INVALID, "Font Descriptor is missing"));
			return false;
		} else {
			throw new ValidationException("Invalid FontDescription object, expected PDFontDescriptorDictionary");
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.font.FontValidator#validate(java.util.List)
	 */
	public void validate() throws ValidationException {
		COSDocument cDoc = context.getDocument().getDocument();
		if (!checkMandatoryFields()) {
			return ;
		}

		boolean result = true;
		result = result && checkWidthsArray(cDoc);
		result = result && checkFontDescriptor();
		result = result && checkEncoding(cDoc);
		result = result && checkToUnicode(cDoc);
	}
}
