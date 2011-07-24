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

package org.apache.padaf.preflight.font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.contentstream.ContentStreamException;
import org.apache.padaf.preflight.font.AbstractFontContainer.State;
import org.apache.padaf.preflight.graphics.ExtGStateContainer;
import org.apache.padaf.preflight.graphics.ShadingPattern;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType3Font;

public class Type3FontValidator extends AbstractFontValidator {
	protected PDType3Font pdType3 = null;

	protected COSBase fontBBox = null;
	protected COSBase fontMatrix = null;
	protected COSBase charProcs = null;
	protected COSBase fontEncoding = null;
	protected COSBase firstChar = null;
	protected COSBase lastChar = null;
	protected COSBase widths = null;
	protected COSBase toUnicode = null;
	protected COSBase resources = null;

	protected Encoding type3Encoding = null;

	public Type3FontValidator(DocumentHandler handler, COSObject obj)
	throws ValidationException {
		super(handler, obj);
		this.pdType3 = (PDType3Font) this.pFont;
	}

	/**
	 * This methods stores in attributes all required element. We extract these
	 * elements because of the PDType3Font object returns sometime default value
	 * if the field is missing, so to avoid mistake during required field
	 * validation we store them.
	 */
	private void extractFontDictionaryEntries() {
		// ---- required elements
		this.fontBBox = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_FONTBBOX));
		this.fontMatrix = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_FONTMATRIX));
		this.charProcs = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CHARPROCS));
		this.fontEncoding = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_ENCODING));
		this.firstChar = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_FIRSTCHAR));
		this.lastChar = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_LASTCHAR));
		this.widths = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_WIDTHS));

		// ---- Optional elements
		this.toUnicode = this.fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_TOUNICODE));
		this.resources = this.fDictionary.getItem(COSName
				.getPDFName(DICTIONARY_KEY_RESOURCES));
	}

	/**
	 * Returns true if all required fields are present. Otherwise, this method
	 * returns false and the FontContainer is updated.
	 * 
	 * @return
	 */
	private boolean checkMandatoryFields() {
		boolean all = (this.fontBBox != null);
		all = all && (this.fontMatrix != null);
		all = all && (this.charProcs != null);
		all = all && (this.fontEncoding != null);
		all = all && (this.firstChar != null);
		all = all && (this.lastChar != null);
		all = all && (this.widths != null);
		if (!all) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID));
		}

		/*
		 * ---- Since PDF 1.5 : FontDescriptor is mandatory for Type3 font. However
		 * because of the FontDescriptor is optional in PDF-1.4 no specific checks
		 * are processed for PDF/A validation.
		 */
		return all;
	}

	/**
	 * FontBBox and FontMatrix are required. This method checks the type and the
	 * content of the FontBBox and FontMatrix element (Array of 4/6 number). If a
	 * type is invalid, the FontContainer is updated and the method returns false.
	 * 
	 * @return
	 */
	private boolean checkFontBBoxMatrix() {
		COSDocument cDoc = this.handler.getDocument().getDocument();

		// ---- both elements are an array
		if (!COSUtils.isArray(this.fontBBox, cDoc)) {
			this.fontContainer
			.addError(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
					"The FontBBox element isn't an array"));
			return false;
		}

		if (!COSUtils.isArray(this.fontMatrix, cDoc)) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"The FontMatrix element isn't an array"));
			return false;
		}

		// ---- check the content of the FontBBox.
		// ---- Should be an array with 4 numbers
		COSArray bbox = COSUtils.getAsArray(fontBBox, cDoc);
		if (bbox.size() != 4) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "The FontBBox element is invalid"));
			return false;
		} else {
			for (int i = 0; i < 4; i++) {
				COSBase elt = bbox.get(i);
				if (!(COSUtils.isFloat(elt, cDoc) || COSUtils.isInteger(elt, cDoc))) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"An element of FontBBox isn't a number"));
					return false;
				}
			}
		}

		// ---- check the content of the FontMatrix.
		// ---- Should be an array with 6 numbers
		COSArray matrix = COSUtils.getAsArray(fontMatrix, cDoc);
		if (matrix.size() != 6) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "The FontMatrix element is invalid"));
			return false;
		} else {
			for (int i = 0; i < 6; i++) {
				COSBase elt = matrix.get(i);
				if (!(COSUtils.isFloat(elt, cDoc) || COSUtils.isInteger(elt, cDoc))) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"An element of FontMatrix isn't a number"));
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * For a Type3 font, the mapping between the Character Code and the Character
	 * name is entirely defined in the Encoding Entry. The Encoding Entry can be a
	 * Name (For the 5 predefined Encoding) or a Dictionary. If it is a
	 * dictionary, the "Differences" array contains the correspondence between a
	 * character code and a set of character name which are different from the
	 * encoding entry of the dictionary.
	 * 
	 * This method checks that the encoding is :
	 * <UL>
	 * <li>An existing encoding name.
	 * <li>A dictionary with an existing encoding name (the name is optional) and
	 * a well formed "Differences" array (the array is optional)
	 * </UL>
	 * 
	 * @return
	 */
	private boolean checkEncoding() {
		COSDocument cDoc = this.handler.getDocument().getDocument();

		EncodingManager emng = new EncodingManager();
		if (COSUtils.isString(this.fontEncoding, cDoc)) {
			// ---- Encoding is a Name, check if it is an Existing Encoding
			String enc = COSUtils.getAsString(this.fontEncoding, cDoc);
			try {
				type3Encoding = emng.getEncoding(COSName.getPDFName(enc));
			} catch (IOException e) {
				// ---- the encoding doesn't exist
				this.fontContainer.addError(new ValidationError(ERROR_FONTS_ENCODING));
				return false;
			}
		} else if (COSUtils.isDictionary(this.fontEncoding, cDoc)) {
			COSDictionary encodingDictionary = COSUtils.getAsDictionary(
					this.fontEncoding, cDoc);
			try {
				type3Encoding = new DictionaryEncoding(encodingDictionary);
			} catch (IOException e) {
				// ---- the encoding doesn't exist
				this.fontContainer.addError(new ValidationError(ERROR_FONTS_ENCODING));
				return false;
			}

			COSBase diff = encodingDictionary.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_DIFFERENCES));
			if (diff != null) {
				if (!COSUtils.isArray(diff, cDoc)) {
					this.fontContainer
					.addError(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
							"The differences element of the encoding dictionary isn't an array"));
					return false;
				}

				// ---- The DictionaryEncoding object doesn't throw exception if the
				// Differences isn't well formed.
				// So check if the array has the right format.
				COSArray differences = COSUtils.getAsArray(diff, cDoc);
				for (int i = 0; i < differences.size(); ++i) {
					COSBase item = differences.get(i);
					if (!(item instanceof COSInteger || item instanceof COSName)) {
						// ---- Error, the Differences array is invalid
						this.fontContainer
						.addError(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
						"Differences Array should contain COSInt or COSName, no other type"));
						return false;
					}
				}
			}
		} else {
			// ---- the encoding entry is invalid
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_TYPE3_DAMAGED,
					"The Encoding entry doesn't have the right type"));
			return false;
		}

		return true;
	}

	/**
	 * CharProcs is a dictionary where the key is a character name and the value
	 * is a Stream which contains the glyph representation of the key.
	 * 
	 * This method checks that all character code defined in the Widths Array
	 * exist in the CharProcs dictionary. If the CharProcs doesn't know the
	 * Character, it is mapped with the .notdef one.
	 * 
	 * For each character, the Glyph width must be the same as the Width value
	 * declared in the Widths array.
	 * 
	 * @param errors
	 * @return
	 */
	private boolean checkCharProcsAndMetrics() throws ValidationException {
		COSDocument cDoc = this.handler.getDocument().getDocument();

		// ---- the Widths value can be a reference to an object
		// ---- Access the object using the COSkey
		COSArray wArr = COSUtils.getAsArray(this.widths, cDoc);
		if (wArr == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, 
					"The Witdhs array is unreachable"));
			return false;
		}

		COSDictionary charProcsDictionary = COSUtils.getAsDictionary(this.charProcs, cDoc);
		if (charProcsDictionary == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"The CharProcs element isn't a dictionary"));
			return false;
		}

		// ---- firstChar and lastChar must be integer.
		int fc = ((COSInteger) this.firstChar).intValue();
		int lc = ((COSInteger) this.lastChar).intValue();

		// ---- wArr length = (lc - fc) +1 and it is an array of int.
		// ---- If FirstChar is greater than LastChar, the validation will fail
		// because of
		// ---- the array will have an expected size <= 0.
		int expectedLength = (lc - fc) + 1;
		if (wArr.size() != expectedLength) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"The length of Witdhs array is invalid. Expected : \""
					+ expectedLength + "\" Current : \"" + wArr.size() + "\""));
			return false;
		}

		// ---- Check width consistency
		for (int i = 0; i < expectedLength; i++) {
			int cid = fc + i;
			COSBase arrContent = wArr.get(i);
			if (COSUtils.isNumeric(arrContent, cDoc)) {
				float width = COSUtils.getAsFloat(arrContent, cDoc);

				String charName = null;
				try {
					charName = this.type3Encoding.getName(cid);
				} catch (IOException e) {
					// shouldn't occur
					throw new ValidationException("Unable to check Widths consistency", e);
				}

				COSBase item = charProcsDictionary.getItem(COSName.getPDFName(charName));
				COSStream charStream = COSUtils.getAsStream(item, cDoc);
				if (charStream == null && width != 0) {
					GlyphException glyphEx = new GlyphException(ERROR_FONTS_METRICS, cid, 
							"The CharProcs \"" + charName
							+ "\" doesn't exist but the width is " + width);
					GlyphDetail glyphDetail = new GlyphDetail(cid, glyphEx);
					this.fontContainer.addKnownCidElement(glyphDetail);
				} else {
					try {
						// --- Parse the Glyph description to obtain the Width
						PDFAType3StreamParser parser = new PDFAType3StreamParser(
								this.handler);
						PDResources pRes = null;
						if (this.resources != null) {
							COSDictionary resAsDict = COSUtils.getAsDictionary(
									this.resources, cDoc);
							if (resAsDict != null) {
								pRes = new PDResources(resAsDict);
							}
						}
						parser.resetEngine();
						parser.processSubStream(null, pRes, charStream);

						if (width != parser.getWidth()) {
							GlyphException glyphEx = new GlyphException(ERROR_FONTS_METRICS, cid, 
									"The CharProcs \"" + charName
									+ "\" should have a width equals to " + width);
							GlyphDetail glyphDetail = new GlyphDetail(cid, glyphEx);
							this.fontContainer.addKnownCidElement(glyphDetail);
						} else {
							// Glyph is OK, we keep the CID.
							GlyphDetail glyphDetail = new GlyphDetail(cid);
							this.fontContainer.addKnownCidElement(glyphDetail);
						}
					} catch (ContentStreamException e) {
						this.fontContainer.addError(new ValidationError(e
								.getValidationError()));
						return false;
					} catch (IOException e) {
						this.fontContainer.addError(new ValidationError(
								ERROR_FONTS_TYPE3_DAMAGED,
								"The CharProcs references an element which can't be read"));
						return false;
					}
				}

			} else {
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_DICTIONARY_INVALID,
						"The Witdhs array is invalid. (some element aren't integer)"));
				return false;
			}
		}
		return true;
	}

	/**
	 * If the Resources entry is present, this method check its content. Only
	 * fonts and Images are checked because this resource describes glyphs. REMARK
	 * : The font and the image aren't validated because they will be validated by
	 * an other ValidationHelper.
	 * 
	 * @return
	 */
	private boolean checkResources() throws ValidationException {
		if (this.resources == null) {
			// ---- No resources dictionary.
			return true;
		}

		COSDocument cDoc = this.handler.getDocument().getDocument();
		COSDictionary dictionary = COSUtils.getAsDictionary(this.resources, cDoc);
		if (dictionary == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
					"The Resources element isn't a dictionary"));
			return false;
		}

		COSBase cbImg = dictionary.getItem(COSName
				.getPDFName(DICTIONARY_KEY_XOBJECT));
		COSBase cbFont = dictionary
		.getItem(COSName.getPDFName(DICTIONARY_KEY_FONT));

		if (cbImg == null && cbFont == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_TYPE3_DAMAGED,
					"The Resources element doesn't have Glyph information"));
			return false;
		}

		if (cbImg != null) {
			// ---- the referenced objects must be present in the PDF file
			COSDictionary dicImgs = COSUtils.getAsDictionary(cbImg, cDoc);
			Set<COSName> keyList = dicImgs.keySet();
			for (Object key : keyList) {

				COSBase item = dictionary.getItem((COSName) key);
				COSDictionary xObjImg = COSUtils.getAsDictionary(item, cDoc);
				if (xObjImg == null) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"The Resources dictionary of type 3 font is invalid"));
					return false;
				}

				if (!XOBJECT_DICTIONARY_VALUE_SUBTYPE_IMG.equals(xObjImg
						.getString(COSName.getPDFName(DICTIONARY_KEY_SUBTYPE)))) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"The Resources dictionary of type 3 font is invalid"));
					return false;
				}
			}
		}

		if (cbFont != null) {
			// ---- the referenced object must be present in the PDF file
			COSDictionary dicFonts = COSUtils.getAsDictionary(cbFont, cDoc);
			Set<COSName> keyList = dicFonts.keySet();
			for (Object key : keyList) {

				COSBase item = dictionary.getItem((COSName) key);
				COSDictionary xObjFont = COSUtils.getAsDictionary(item, cDoc);
				if (xObjFont == null) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"The Resources dictionary of type 3 font is invalid"));
					return false;
				}

				if (!FONT_DICTIONARY_VALUE_FONT.equals(xObjFont.getString(COSName
						.getPDFName(DICTIONARY_KEY_TYPE)))) {
					this.fontContainer.addError(new ValidationError(
							ERROR_FONTS_DICTIONARY_INVALID,
							"The Resources dictionary of type 3 font is invalid"));
					return false;
				}

				try {
					PDFont aFont = PDFontFactory.createFont(xObjFont);
					// FontContainer aContainer = this.handler.retrieveFontContainer(aFont);
					AbstractFontContainer aContainer = this.handler.getFont(aFont.getCOSObject());
					// ---- another font is used in the Type3, check if the font is valid.
					if (aContainer.isValid() != State.VALID) {
						this.fontContainer
						.addError(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
								"The Resources dictionary of type 3 font contains invalid font"));
						return false;
					}
				} catch (IOException e) {
					throw new ValidationException("Unable to valid the Type3 : "
							+ e.getMessage());
				}
			}
		}

		List<ValidationError> errors = new ArrayList<ValidationError>();
		ExtGStateContainer extGStates = new ExtGStateContainer(dictionary, cDoc);
		boolean res = extGStates.validateTransparencyRules(errors);
		for (ValidationError err : errors) {
			this.fontContainer.addError(err);
		}

		return res && validateShadingPattern(dictionary, errors);
	}

	/**
	 * This method check the Shading entry of the resource dictionary if exists.
	 * 
	 * @param result
	 * @return
	 * @throws ValidationException
	 */
	protected boolean validateShadingPattern(COSDictionary dictionary,
			List<ValidationError> result) throws ValidationException {
		boolean res = true;
		if (dictionary != null) {
			COSDictionary shadings = (COSDictionary) dictionary
			.getDictionaryObject(PATTERN_KEY_SHADING);
			if (shadings != null) {
				for (COSName key : shadings.keySet()) {
					COSDictionary aShading = (COSDictionary) shadings.getDictionaryObject(key);
					ShadingPattern sp = new ShadingPattern(handler, aShading);
					List<ValidationError> lErrors = sp.validate();
					if (lErrors != null && !lErrors.isEmpty()) {
						result.addAll(lErrors);
						res = false;
					}
				}
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.awl.edoc.pdfa.validation.font.FontValidator#validate()
	 */
	public boolean validate() throws ValidationException {
		extractFontDictionaryEntries();
		boolean isValid = checkMandatoryFields();
		isValid = isValid && checkFontBBoxMatrix();
		isValid = isValid && checkEncoding();
		isValid = isValid && checkCharProcsAndMetrics();
		isValid = isValid && checkResources();
		return isValid;
	}
}
