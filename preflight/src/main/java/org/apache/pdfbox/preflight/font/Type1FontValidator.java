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

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.type1.Type1;
import org.apache.pdfbox.preflight.font.type1.Type1Parser;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class Type1FontValidator extends SimpleFontValidator {

	public Type1FontValidator(PreflightContext context, PDFont font)
			throws ValidationException {
		super(context, font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.awl.edoc.pdfa.validation.font.SimpleFontValidator#
	 * checkSpecificMandatoryFields()
	 */
	protected boolean checkSpecificMandatoryFields() {
		// ---- name is required only in a PDF-1.0.
		// ---- Currently our grammar matches only with PDF-1.[1-4]
		// ---- baseFont is required and is usually the FontName
		if (basefont == null || "".equals(basefont)) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "BaseFont is missing"));
			return false;
		}

		boolean allPresent = (firstChar >= 0 && lastChar >= 0 && widths != null);
		if (!allPresent) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "Required keys are missing"));
			return false;
		} 
		// else
		// ---- Event if the Font is one of the 14 standard Fonts, those keys are
		// mandatory for a PDF/A
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.font.SimpleFontValidator#checkEncoding(org
	 * .apache.pdfbox.cos.COSDocument)
	 */
	protected boolean checkEncoding() {
		COSBase encoding = ((COSDictionary)pFont.getCOSObject()).getItem(COSName.ENCODING);
		if (COSUtils.isString(encoding, cosDocument)) {
			String encodingName = COSUtils.getAsString(encoding, cosDocument);
			if (!(encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_MAC)
					|| encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP)
					|| encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_WIN)
					|| encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_PDFDOC) || encodingName
					.equals(FONT_DICTIONARY_VALUE_ENCODING_STD))) {
				this.fontContainer.addError(new ValidationError(ERROR_FONTS_ENCODING));
				return false;
			}
		} else if (COSUtils.isDictionary(encoding, cosDocument)) {
			this.pFont.getFontEncoding();
		} else if (encoding != null) {
			this.fontContainer.addError(new ValidationError(ERROR_FONTS_ENCODING));
			return false;
		} 
		//else
		// ---- According to PDF Reference, the encoding entry is optional.
		// PDF/A specification only speaks of TrueType encoding

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.font.SimpleFontValidator#checkFontDescriptor()
	 */
	@Override
	protected boolean checkFontDescriptor() throws ValidationException {
		boolean res = checkFontDescriptorMandatoryFields();
		res = res && checkFontName();
		res = res && checkFontFileElement();
		return res;
	}

	/**
	 * Check if the font name is present and if fontName equals to the baseName.
	 * If the validation fails, false is returned and the FontContainer is
	 * updated.
	 * 
	 * @return
	 */
	boolean checkFontName() {
		String fontName = this.pFontDesc.getFontName();
		if (fontName == null) {
			this.fontContainer
			.addError(new ValidationResult.ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
					"The FontName in font descriptor isn't the same as the BaseFont in the Font dictionary"));
			return false;
		}

		return true;
	}

	/**
	 * This methods validates the Font Stream, if the font program is damaged or
	 * missing the FontContainer is updated and false is returned.
	 * 
	 * @throws ValidationException
	 */
	protected boolean checkFontFileElement() throws ValidationException {
		// ---- if the this font is a Subset, the CharSet entry must be present in
		// the FontDescriptor
		if (isSubSet(pFontDesc.getFontName())) {
			String charsetStr = pFontDesc.getCharSet();
			if (charsetStr == null || "".equals(charsetStr)) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET,
						"The Charset entry is missing for the Type1 Subset"));
				return false;
			}
		}

		// ---- FontFile Validation
		if (pFontDesc instanceof PDFontDescriptorDictionary) {
			PDStream ff1 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile();
			PDStream ff2 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile2();
			PDStream ff3 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile3();
			boolean onlyOne = (ff1 != null && ff2 == null && ff3 == null)
					|| (ff1 == null && ff2 != null && ff3 == null)
					|| (ff1 == null && ff2 == null && ff3 != null);

			if ((ff1 == null && (ff3 == null || !"Type1C".equals(((COSDictionary)ff3.getCOSObject()).getNameAsString(COSName.SUBTYPE)))) || !onlyOne) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is invalid"));
				return false;
			}

			if (ff1 != null) {
				COSStream stream = ff1.getStream();
				if (stream == null) {
					this.fontContainer.addError(new ValidationResult.ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is missing"));
					this.fontContainer.setFontProgramEmbedded(false);
					return false;
				}

				boolean hasLength1 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH1)) > 0;
				boolean hasLength2 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH2)) > 0;
				boolean hasLength3 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH3)) > 0;
				if (!(hasLength1 && hasLength2 && hasLength3)) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is invalid"));
					return false;
				}

				// ---- Stream validation should be done by the StreamValidateHelper.
				// ---- Process font specific check
				// ---- try to load the font using the java.awt.font object.
				// ---- if the font is invalid, an exception will be thrown
				ByteArrayInputStream bis = null;
				try {
					bis = new ByteArrayInputStream(ff1.getByteArray());
					Font.createFont(Font.TYPE1_FONT, bis);
					return checkFontMetricsDataAndFeedFontContainer(ff1) && checkFontFileMetaData(pFontDesc, ff1);
				} catch (IOException e) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							ERROR_FONTS_TYPE1_DAMAGED, "The FontFile can't be read"));
					return false;
				} catch (FontFormatException e) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							ERROR_FONTS_TYPE1_DAMAGED, "The FontFile is damaged"));
					return false;
				} finally {
					if (bis != null) {
						IOUtils.closeQuietly(bis);
					}
				}
			} else {
				return checkCIDFontWidths(ff3) && checkFontFileMetaData(pFontDesc, ff3);
			}
		} else {
			throw new ValidationException("Invalid FontDescription object, expected PDFontDescriptorDictionary");
		}
	}

	/**
	 * Type1C is a CFF font format, extract all CFFFont object from the stream
	 * 
	 * @param fontStream
	 * @return
	 * @throws ValidationException
	 */
	protected boolean checkCIDFontWidths(PDStream fontStream)
			throws ValidationException {
		try {
			CFFParser cffParser = new CFFParser();
			List<CFFFont> lCFonts = cffParser.parse(fontStream.getByteArray());

			if (lCFonts == null || lCFonts.isEmpty()) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
				return false;
			}

			((Type1FontContainer)this.fontContainer).setCFFFontObjects(lCFonts);

			return true;
		} catch (IOException e) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
			return false;
		}
	}
	/**
	 * This method checks the metric consistency and adds the FontContainer in the
	 * DocumentHandler.
	 * 
	 * @param fontStream
	 * @return
	 * @throws ValidationException
	 */
	protected boolean checkFontMetricsDataAndFeedFontContainer(PDStream fontStream)
			throws ValidationException {
		try {

			// ---- Parse the Type1 Font program
			ByteArrayInputStream bis = new ByteArrayInputStream(fontStream
					.getByteArray());
			COSStream streamObj = fontStream.getStream();
			int length1 = streamObj.getInt(COSName
					.getPDFName(FONT_DICTIONARY_KEY_LENGTH1));
			int length2 = streamObj.getInt(COSName
					.getPDFName(FONT_DICTIONARY_KEY_LENGTH2));

			Type1Parser parserForMetrics = Type1Parser.createParserWithEncodingObject(bis, length1, length2, pFont.getFontEncoding());
			Type1 parsedData = parserForMetrics.parse();

			((Type1FontContainer)this.fontContainer).setFontObject(parsedData);

			return true;
		} catch (IOException e) {
			throw new ValidationException("Unable to check Type1 metrics due to : "
					+ e.getMessage(), e);
		}
	}
}
