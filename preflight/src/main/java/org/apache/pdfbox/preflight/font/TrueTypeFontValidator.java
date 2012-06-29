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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.MacRomanEncoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

public class TrueTypeFontValidator extends SimpleFontValidator {


	public TrueTypeFontValidator(PreflightContext context, PDFont font) 
			throws ValidationException {
		super(context, font);
	}

	/**
	 * Check if mandatory fields are present. Return false if a field is missing,
	 * true otherwise. If validation fails, the FontContainer is updated.
	 */
	protected boolean checkSpecificMandatoryFields() {
		// ---- name is required only in a PDF-1.0.
		// ---- Currently our grammar matches only with PDF-1.[1-4]
		// ---- BaseFont is required and is usually the FontName
		if (basefont == null || "".equals(basefont)) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "BaseFont is missing"));
			// continue to process this font dictionary is useless
			return false;
		}

		boolean allPresent = (firstChar >=  0 && lastChar >=  0 && widths != null);
		if (!allPresent) {
			this.fontContainer.addError(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID, "Required keys are missing"));
			return false;
		} // else  ok


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
	 * If the FontName is missing from the FontDescriptor dictionary, this method
	 * returns false and the FontContainer is updated.
	 * 
	 * @return
	 */
	protected boolean checkFontName() {
		String fontName = this.pFontDesc.getFontName();
		if (fontName == null) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					PreflightConstants.ERROR_FONTS_DESCRIPTOR_INVALID,
					"The FontName in font descriptor is null"));
			return false;
		}
		return true;
	}

	/**
	 * This methods validates the Font Stream. If the font is damaged or missing
	 * the FontContainer is updated and false is returned. Moreover, this method
	 * checks the Encoding property of the FontDescriptor dictionary.
	 * 
	 * @return
	 */
	protected boolean checkFontFileElement() throws ValidationException {
		if (pFontDesc instanceof PDFontDescriptorDictionary) {
			PDStream ff1 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile();
			PDStream ff2 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile2();
			PDStream ff3 = ((PDFontDescriptorDictionary)pFontDesc).getFontFile3();
			boolean onlyOne = (ff1 != null && ff2 == null && ff3 == null)
					|| (ff1 == null && ff2 != null && ff3 == null)
					|| (ff1 == null && ff2 == null && ff3 != null);

			if (ff2 == null || !onlyOne) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID,
						"The FontFile2 is invalid"));
				return false;
			}

			// ---- Stream validation should be done by the StreamValidateHelper.
			// ---- Process font specific check
			COSStream stream = ff2.getStream();
			if (stream == null) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID,
						"The FontFile is missing"));
				this.fontContainer.setFontProgramEmbedded(false);
				return false;
			}

			boolean hasLength1 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH1)) > 0;
			if (!hasLength1) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID,
						"The FontFile is invalid"));
				return false;
			}

			// ---- check the encoding part.
			if (pFontDesc.isNonSymbolic()) {
				// ---- only MacRomanEncoding or WinAnsiEncoding are allowed for a non
				// symbolic font
				Encoding encodingValue = this.pFont.getFontEncoding();
				if (encodingValue == null
						|| !(encodingValue instanceof MacRomanEncoding || encodingValue instanceof WinAnsiEncoding)) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							PreflightConstants.ERROR_FONTS_ENCODING,
							"The Encoding is invalid for the NonSymbolic TTF"));
					return false;
				}
			} else if (pFontDesc.isSymbolic()) {
				// ---- For symbolic font, no encoding entry is allowed and only one
				// encoding entry is expected into the FontFile CMap
				if (((COSDictionary) this.pFont.getCOSObject()).getItem(COSName
						.getPDFName(FONT_DICTIONARY_KEY_ENCODING)) != null) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							PreflightConstants.ERROR_FONTS_ENCODING,
							"The Encoding should be missing for the Symbolic TTF"));
					return false;
				} // else check the content of the Font CMap (see below)

			} else {
				// ----- should never happen
				return true;
			}

			/*
			 * ---- try to load the font using the TTFParser object. If the font is
			 * invalid, an exception will be thrown. Because of it is a Embedded Font
			 * Program, some tables are required and other are optional see PDF
			 * Reference (§5.8)
			 */
			ByteArrayInputStream bis = null;
			try {
				bis = new ByteArrayInputStream(ff2.getByteArray());
				TrueTypeFont ttf = new TTFParser(true).parseTTF(bis);
				if (pFontDesc.isSymbolic() && ttf.getCMAP().getCmaps().length != 1) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							PreflightConstants.ERROR_FONTS_ENCODING,
							"The Encoding should be missing for the Symbolic TTF"));
					return false;
				}

				((TrueTypeFontContainer)this.fontContainer).setFontObjectAndInitializeInnerFields(ttf);
				((TrueTypeFontContainer)this.fontContainer).setCMap(getCMapOfFontProgram(ttf));

				return checkFontFileMetaData(pFontDesc, ff2);
			} catch (IOException e) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						PreflightConstants.ERROR_FONTS_TRUETYPE_DAMAGED,
						"The FontFile can't be read"));
				return false;
			} finally {
				if (bis != null) {
					IOUtils.closeQuietly(bis);
				}
			}
		} else {
			throw new ValidationException("Invalid FontDescription object, expected PDFontDescriptorDictionary");
		}
	}

	/**
	 * Return the CMap encoding entry to use. This CMap belong to the TrueType
	 * Font Program.
	 * 
	 * Here the selection rules :
	 * <UL>
	 * <li>For a Symbolic TrueType, the Font Program has only one CMap (Checked in
	 * the checkFontFileElement method)
	 * <li>For a Non-Symbolic TrueType, only two CMap can be used (WinAnsi
	 * (plateformId : 3 / encodingId : 1) or MacRoman (plateformId : 1 /
	 * encodingId : 0) ). This CMap returns the CMap which corresponds to the
	 * Encoding value of the FontDescriptor dictionary.
	 * </UL>
	 * 
	 * @param ttf
	 *          The FontBox object which manages a TrueType Font program.
	 * @return
	 * @throws ValidationException
	 *           if the FontProgram doesn't have the expected CMap
	 */
	protected CMAPEncodingEntry[] getCMapOfFontProgram(TrueTypeFont ttf)
			throws ValidationException {
		CMAPTable cmap = ttf.getCMAP();
		if (this.pFontDesc.isSymbolic()) {
			return cmap.getCmaps();
		} else {
			List<CMAPEncodingEntry> res = new ArrayList<CMAPEncodingEntry>();
			boolean firstIs31 = false;
			for (CMAPEncodingEntry cmapEntry : cmap.getCmaps()) {
				// ---- Returns the WinAnsiEncoding CMap
				if ((cmapEntry.getPlatformId() == 3) && (cmapEntry.getPlatformEncodingId() == 1)) {
					res.add(0,cmapEntry);
					firstIs31 = true;
				} else if ((cmapEntry.getPlatformId() == 1)&& (cmapEntry.getPlatformEncodingId() == 0)) {
					if (firstIs31) {
						res.add(1, cmapEntry);
					} else {
						res.add(0, cmapEntry);
					}
				} else {
					res.add(cmapEntry);
				}
			}
			return res.toArray(new CMAPEncodingEntry[res.size()]);
		}
	}
}
