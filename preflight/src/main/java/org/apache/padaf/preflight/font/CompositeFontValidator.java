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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;


import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.fontbox.ttf.CIDFontType2Parser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;

public class CompositeFontValidator extends AbstractFontValidator {
	protected String basefont;
	protected COSBase descendantFonts;
	protected COSDictionary cidFont;
	protected COSBase encoding;
	protected COSStream cmap;
	protected COSBase toUnicode;

	protected CMap cidToGidMap = null;

	protected boolean isIdentityCMap = false;

	public CompositeFontValidator(DocumentHandler handler, COSObject obj)
	throws ValidationException {
		super(handler, obj);
	}

	/**
	 * This methods extracts from the Font dictionary all mandatory fields. If a
	 * mandatory field is missing, the list of ValidationError in the
	 * FontContainer is updated. On error, the method returns false.
	 * 
	 * @return
	 */
	protected boolean checkMandatoryFields() {
		String type = fDictionary.getNameAsString(COSName
				.getPDFName(DICTIONARY_KEY_TYPE));
		String subtype = fDictionary.getNameAsString(COSName
				.getPDFName(DICTIONARY_KEY_SUBTYPE));

		// ---- just check if they are present because of the Helper has already
		// checked them.
		if ((type == null || "".equals(type))
				|| (subtype == null || "".equals(subtype))) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
			"Type and/or Subtype keys are missing"));
			return false;
		}

		// ---- Check presence of baseFont, CMap and CIDFont
		this.basefont = fDictionary.getNameAsString(COSName
				.getPDFName(FONT_DICTIONARY_KEY_BASEFONT));
		this.descendantFonts = fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_DESCENDANT_FONTS));
		this.encoding = fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_ENCODING));

		if ((basefont == null || "".equals(basefont)) || descendantFonts == null
				|| encoding == null) {
			// ---- baseFont syntax isn't checked because of it is a convention not a
			// rule
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
			"BaseFont, Encoding or DescendantFonts keys are missing"));
			return false;
		}

		// ---- toUnicode is optional, but keep the value if present.
		this.toUnicode = fDictionary.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_TOUNICODE));
		return true;
	}

	/**
	 * This method validates the CIDFont dictionary.
	 * 
	 * This method returns false and updates the list of errors in the
	 * FontContainer if some mandatory fields are missing.
	 * 
	 * This method calls the processCIDFontTypeX method to check if the font is
	 * damaged or not. If the font is damaged, the errors list is updated and the
	 * method return false.
	 * 
	 * @return
	 * @throws ValidationException
	 */
	protected boolean checkCIDFont() throws ValidationException {

		// ---- a CIDFont is contained in the DescendantFonts array
		COSDocument cDoc = this.handler.getDocument().getDocument();
		COSArray array = COSUtils.getAsArray(descendantFonts, cDoc);

		if (array == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_INVALID,
			"CIDFont is missing from the DescendantFonts array"));
			return false;
		}
		// ---- in PDF 1.4, this array must contain only one element,
		// because of a PDF/A should be a PDF 1.4, this method returns an error if
		// the array
		// has more than one element.
		if (array.size() != 1) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_INVALID,
			"The DescendantFonts array should have one element."));
			return false;
		}

		this.cidFont = COSUtils.getAsDictionary(array.get(0), cDoc);
		if (this.cidFont == null) {
			this.fontContainer
			.addError(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
			"The DescendantFonts array should have one element with is a dictionary."));
			return false;
		}

		String type = cidFont.getNameAsString(COSName
				.getPDFName(DICTIONARY_KEY_TYPE));
		String subtype = cidFont.getNameAsString(COSName
				.getPDFName(DICTIONARY_KEY_SUBTYPE));

		if ((type == null || "".equals(type))
				|| (subtype == null || "".equals(subtype))) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
			"Type and/or Subtype keys are missing"));
			return false;
		}

		boolean isT0 = FONT_DICTIONARY_VALUE_TYPE0.equals(subtype);
		boolean isT2 = FONT_DICTIONARY_VALUE_TYPE2.equals(subtype);
		// ---- Even if these entries are present, values must be checked.
		if (!FONT_DICTIONARY_VALUE_FONT.equals(type) || !(isT0 || isT2)) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID,
			"Type and/or Subtype keys are missing"));
			return false;
		}

		// ---- BaseFont is mandatory
		String bf = cidFont.getNameAsString(COSName
				.getPDFName(FONT_DICTIONARY_KEY_BASEFONT));
		if (bf == null || "".equals(bf)) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_DICTIONARY_INVALID, "BaseFont is missing"));
			return false;
		}

		// ---- checks others mandatory fields
		COSBase sysinfo = cidFont.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CID_SYSINFO));
		COSBase fontDesc = cidFont.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_FONT_DESC));
		COSBase cidToGid = cidFont.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CID_GIDMAP));

		boolean result = checkCIDSystemInfo(sysinfo, cDoc);
		if (isT0) {
			result = result && checkCIDToGIDMap(cidToGid, cDoc, false);
			result = result && processCIDFontType0(fontDesc);
		} else {
			result = result && checkCIDToGIDMap(cidToGid, cDoc, true);
			result = result && processCIDFontType2(fontDesc);
		}
		return result;
	}

	/**
	 * Check the content of the CIDSystemInfo dictionary. A CIDSystemInfo
	 * dictionary must contain :
	 * <UL>
	 * <li>a Name - Registry
	 * <li>a Name - Ordering
	 * <li>a Integer - Supplement
	 * </UL>
	 * 
	 * @param sysinfo
	 * @param cDoc
	 * @return
	 */
	private boolean checkCIDSystemInfo(COSBase sysinfo, COSDocument cDoc) {
		COSDictionary cidSysInfo = COSUtils.getAsDictionary(sysinfo, cDoc);
		if (cidSysInfo == null) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_SYSINFO));
			return false;
		}

		COSBase reg = cidSysInfo.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_REGISTRY));
		COSBase ord = cidSysInfo.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_ORDERING));
		COSBase sup = cidSysInfo.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_SUPPLEMENT));

		if (!(COSUtils.isString(reg, cDoc) && COSUtils.isString(ord, cDoc) && COSUtils
				.isInteger(sup, cDoc))) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_SYSINFO));
			return false;
		}

		return true;
	}

	/**
	 * This method checks the CIDtoGIDMap entry of the Font dictionary. This
	 * element must be a Stream or a Name. If it is a name, it must be "Identity"
	 * otherwise, the PDF file isn't a PDF/A-1b.
	 * 
	 * If the validation fails, the method returns false and the list of error in
	 * the FontContainer is updated.
	 * 
	 * If the CIDtoGIDMap is a Stream, it is parsed as a CMap and the result is
	 * kept in the cidToGidMap attribute.
	 * 
	 * @param ctog
	 * @param cDoc
	 * @param mandatory
	 * @return
	 */
	private boolean checkCIDToGIDMap(COSBase ctog, COSDocument cDoc,
			boolean mandatory) {
		if (COSUtils.isString(ctog, cDoc)) {
			// ---- valid only if the string is Identity
			String ctogStr = COSUtils.getAsString(ctog, cDoc);
			if (!FONT_DICTIONARY_VALUE_CMAP_IDENTITY.equals(ctogStr)) {
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_CIDTOGID,"The CIDToGID entry is invalid"));
				return false;
			}
		} else if (COSUtils.isStream(ctog, cDoc)) {
			try {
				COSStream ctogMap = COSUtils.getAsStream(ctog, cDoc);
				this.cidToGidMap = new CMapParser().parse(null, ctogMap
						.getUnfilteredStream());
			} catch (IOException e) {
				// ---- map can be invalid, return a Validation Error
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_CIDTOGID));
				return false;
			}
		} else if (mandatory) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_CIDTOGID));
			return false;
		}
		return true;
	}

	/**
	 * Check the CMap entry.
	 * 
	 * The CMap entry must be a dictionary in a PDF/A. This entry can be a String
	 * only if the String value is Identity-H or Identity-V
	 * 
	 * @param errors
	 * @return
	 */
	protected boolean checkCMap(COSBase aEncoding) {
		COSDocument cDoc = this.handler.getDocument().getDocument();

		if (COSUtils.isString(aEncoding, cDoc)) {
			// ---- if encoding is a string, only 2 values are allowed
			String str = COSUtils.getAsString(aEncoding, cDoc);
			if (!(FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V.equals(str) || FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H
					.equals(str))) {
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_INVALID,
				"The CMap is a string but it isn't an Identity-H/V"));
				return false;
			}
			isIdentityCMap = true;
		} else if (COSUtils.isStream(aEncoding, cDoc)) {
			// ---- If the CMap is a stream, some fields are mandatory
			// and the CIDSytemInfo must be compared with the CIDSystemInfo
			// entry of the CIDFont.
			return processCMapAsStream(COSUtils.getAsStream(aEncoding, cDoc));
		} else {
			// ---- CMap type is invalid
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
			"The CMap type is invalid"));
			return false;
		}
		return true;
	}

	/**
	 * Standard information of a stream element will be checked by the
	 * StreamHelper.
	 * 
	 * This method checks mandatory fields of the CMap stream. This method checks
	 * too if the CMap stream is damaged using the CMapParser of the fontbox api.
	 * 
	 * @param aCMap
	 * @return
	 */
	private boolean processCMapAsStream(COSStream aCMap) {
		COSDocument cDoc = handler.getDocument().getDocument();

		String type = aCMap
		.getNameAsString(COSName.getPDFName(DICTIONARY_KEY_TYPE));
		String cmapName = aCMap.getNameAsString(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CMAP_NAME));
		COSBase sysinfo = aCMap.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CID_SYSINFO));
		int wmode = aCMap
		.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_CMAP_WMODE));
		COSBase cmapUsed = aCMap.getItem(COSName
				.getPDFName(FONT_DICTIONARY_KEY_CMAP_USECMAP));

		if (!FONT_DICTIONARY_VALUE_TYPE_CMAP.equals(type)) {
			// ---- CMap type is invalid
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
			"The CMap type is invalid"));
			return false;
		}

		// ---- check the content of the CIDSystemInfo
		if (!checkCIDSystemInfo(sysinfo, cDoc)) {
			return false;
		}

		if (cmapName == null || "".equals(cmapName) || wmode > 1) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
			"Some elements in the CMap dictionary are missing or invalid"));
			return false;
		}

		try {

			CMap fontboxCMap = new CMapParser().parse(null, aCMap
					.getUnfilteredStream());
			int wmValue = fontboxCMap.getWMode();
			String cmnValue = fontboxCMap.getName(); //getCmapEntry("CMapName");


			if (wmValue != wmode) {

				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
				"WMode is inconsistent"));
				return false;
			}

			if (!cmnValue.equals(cmapName)) {

				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
				"CMapName is inconsistent"));
				return false;
			}

		} catch (IOException e) {
			this.fontContainer.addError(new ValidationError(
					ERROR_FONTS_CID_CMAP_DAMAGED, "The CMap type is damaged"));
			return false;
		}

		if (cmapUsed != null) {
			return checkCMap(cmapUsed);
		}

		return true;
	}

	/**
	 * The CIDSystemInfo must have the same Registry and Ordering for CMap and
	 * CIDFont. This control is useless if CMap is Identity-H or Identity-V so
	 * this method is called by the checkCMap method.
	 * 
	 * @param errors
	 * @return
	 */
	private boolean compareCIDSystemInfo() {
		COSDocument cDoc = handler.getDocument().getDocument();
		if (!isIdentityCMap) {
			COSDictionary cmsi = COSUtils.getAsDictionary(this.cmap.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_CID_SYSINFO)), cDoc);
			COSDictionary cfsi = COSUtils.getAsDictionary(this.cidFont
					.getItem(COSName.getPDFName(FONT_DICTIONARY_KEY_CID_SYSINFO)), cDoc);

			String regCM = COSUtils.getAsString(cmsi.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_REGISTRY)), cDoc);
			String ordCM = COSUtils.getAsString(cmsi.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_ORDERING)), cDoc);

			String regCF = COSUtils.getAsString(cfsi.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_REGISTRY)), cDoc);
			String ordCF = COSUtils.getAsString(cfsi.getItem(COSName
					.getPDFName(FONT_DICTIONARY_KEY_SYSINFO_ORDERING)), cDoc);

			if (!regCF.equals(regCM) || !ordCF.equals(ordCM)) {
				this.fontContainer.addError(new ValidationError(
						ERROR_FONTS_CIDKEYED_SYSINFO, "The CIDSystemInfo is inconsistent"));
				return false;
			}
		} // else cmap is null because it is a Identity-H/V
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.font.FontValidator#validate(java.util.List)
	 */
	public boolean validate() throws ValidationException {
		boolean result = checkMandatoryFields();
		result = result && checkCIDFont();
		result = result && checkCMap(encoding);
		if (result) {
			this.cmap = COSUtils.getAsStream(encoding, handler.getDocument()
					.getDocument());
		}
		result = result && compareCIDSystemInfo();
		return result;
	}

	/**
	 * Check if all required fields are present in the PDF file to describe the
	 * Font Descriptor.
	 * 
	 * @param handler
	 * @param fontDescriptor
	 * @param result
	 */
	protected boolean checkFontDescriptorMandatoryFields(
			PDFontDescriptorDictionary pdFontDesc) {
		boolean fname = false, flags = false, itangle = false, cheight = false;
		boolean fbbox = false, asc = false, desc = false, stemv = false;

		COSDictionary fontDescDictionary = pdFontDesc.getCOSDictionary();
		for (Object key : fontDescDictionary.keySet()) {

			if (!(key instanceof COSName)) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
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

		return true;
	}

	/**
	 * Process the CIDFontType0 validation.
	 * 
	 * @param fontDesc
	 *          The FontDescriptor which contains the Font Program
	 * @return
	 * @throws ValidationException
	 */
	protected boolean processCIDFontType0(COSBase fontDesc)
	throws ValidationException {
		COSDictionary fontDescDic = COSUtils.getAsDictionary(fontDesc, handler
				.getDocument().getDocument());
		if (fontDescDic == null) {
			throw new ValidationException(
			"Unable to process CIDFontType0 because of the font descriptor is invalid.");
		}
		PDFontDescriptorDictionary pfDescriptor = new PDFontDescriptorDictionary(
				fontDescDic);
		boolean isValid = checkFontDescriptorMandatoryFields(pfDescriptor);
		isValid = isValid && checkCIDKeyedFontName(pfDescriptor, true);
		isValid = isValid && checkFontFileElement_CIDFontType0(pfDescriptor);
		isValid = isValid && checkCIDSet(pfDescriptor);
		return isValid;
	}

	/**
	 * Checks if the FontName contained in the FontDescriptor dictionary of the
	 * CID-Keyed Font is present. (The FontName is mandatory according to the PDF
	 * Reference.) If the consistency must be checked, the FontName contained in
	 * the FontDescriptor is consistent with the BaseName of the font dictionary.
	 * If font name is invalid, the list of validation errors in the FontContainer
	 * is updated.
	 * 
	 * @param pfDescriptor
	 *          The FontDescriptor dictionary which contains the FontName to check
	 * @param checkConsistency
	 *          true if the font name must be consistent with the BaseName of the
	 *          Font dictionary
	 * @return
	 */
	protected boolean checkCIDKeyedFontName(
			PDFontDescriptorDictionary pfDescriptor, boolean checkConsistency) {
		String fontName = pfDescriptor.getFontName();
		String baseName = this.pFont.getBaseFont();

		if (fontName == null) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_DESCRIPTOR_INVALID,
			"The FontName in font descriptor is missing"));
			return false;
		}

		if (checkConsistency
				&& !(fontName.equals(baseName) || fontName.contains(baseName) || baseName
						.contains(fontName))) {
			this.fontContainer
			.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_DESCRIPTOR_INVALID,
			"The FontName in font descriptor isn't the same as the BaseFont in the Font dictionary"));
			return false;
		}
		return true;
	}

	/**
	 * This method return false and updates the FontContainer if the Composite
	 * Font TYPE 0 or TYPE1C is damaged. This method checks the Widths
	 * consistency.
	 * 
	 * @param pfDescriptor
	 * @return
	 */
	boolean checkFontFileElement_CIDFontType0(
			PDFontDescriptorDictionary pfDescriptor) throws ValidationException {
		// ---- FontFile Validation
		PDStream ff1 = pfDescriptor.getFontFile();
		PDStream ff2 = pfDescriptor.getFontFile2();
		PDStream ff3 = pfDescriptor.getFontFile3();

		boolean onlyOne = (ff1 != null && ff2 == null && ff3 == null)
		|| (ff1 == null && ff2 != null && ff3 == null)
		|| (ff1 == null && ff2 == null && ff3 != null);

		if ((ff3 == null) || !onlyOne) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is invalid"));
			return false;
		}

		// ---- Stream validation should be done by the StreamValidateHelper.
		// ---- Process font specific check
		COSStream stream = ff3.getStream();
		if (stream == null) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is missing"));
			this.fontContainer.setFontProgramEmbedded(false);
			return false;
		}

		// ---- Lengthx aren't mandatory for this type of font
		// ---- But the Subtype is a mandatory field with specific values
		String st = stream.getNameAsString(COSName
				.getPDFName(DICTIONARY_KEY_SUBTYPE));
		if (!(FONT_DICTIONARY_VALUE_TYPE0C.equals(st) || FONT_DICTIONARY_VALUE_TYPE1C
				.equals(st))) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_FONT_FILEX_INVALID,
			"The FontFile3 stream doesn't have the right Subtype"));
			return false;
		}

		// ---- try to load the font using the java.awt.font object.
		// ---- if the font is invalid, an exception will be thrown
		try {
			CFFParser cffParser = new CFFParser();
			List<CFFFont> lCFonts = cffParser.parse(ff3.getByteArray());

			if (lCFonts == null || lCFonts.isEmpty()) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
				return false;
			}

			return checkCIDFontWidths(lCFonts)
			&& checkFontFileMetaData(pfDescriptor, ff3);
		} catch (IOException e) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
			return false;
		}
	}

	/**
	 * This method checks the metric consistency of a CIDFontType2.
	 * 
	 * @param ttf
	 *          The TrueTypeFont object which represent the CIDFontType2 Font
	 *          Program.
	 * @return
	 * @throws ValidationException
	 */
	protected boolean checkTTFontMetrics(TrueTypeFont ttf)
	throws ValidationException {
		LinkedHashMap<Integer, Integer> widths = getWidthsArray();
		int defaultWidth = this.cidFont.getInt("DW", 1000);
		int unitsPerEm = ttf.getHeader().getUnitsPerEm();
		int[] glyphWidths = ttf.getHorizontalMetrics().getAdvanceWidth();
		/* In a Mono space font program, the length of the AdvanceWidth array must be one.
		 * According to the TrueType font specification, the Last Value of the AdvanceWidth array
		 * is apply to the subsequent glyphs. So if the GlyphId is greater than the length of the array
		 * the last entry is used.
		 */
		int numberOfLongHorMetrics = ttf.getHorizontalHeader().getNumberOfHMetrics();
		CFFType2FontContainer type2FontContainer = ((CompositeFontContainer)this.fontContainer).getCFFType2();
		type2FontContainer.setPdfWidths(widths);
		type2FontContainer.setCmap(this.cidToGidMap);
		type2FontContainer.setDefaultGlyphWidth(defaultWidth);
		type2FontContainer.setFontObject(ttf);
		type2FontContainer.setGlyphWidths(glyphWidths);
		type2FontContainer.setNumberOfLongHorMetrics(numberOfLongHorMetrics);
		type2FontContainer.setUnitsPerEm(unitsPerEm);

		return true;
	}

	/**
	 * This method check Metrics consistency of the CIDFontType0.
	 * 
	 * @param lCFonts
	 * @return
	 * @throws ValidationException
	 */
	protected boolean checkCIDFontWidths(List<CFFFont> lCFonts)
	throws ValidationException {
		// ---- Extract Widths and default Width from the CIDFont dictionary
		LinkedHashMap<Integer, Integer> widths = getWidthsArray();
		int defaultWidth = this.cidFont.getInt("DW", 1000);
		CFFType0FontContainer type0FontContainer = ((CompositeFontContainer)this.fontContainer).getCFFType0();
		type0FontContainer.setFontObject(lCFonts);
		type0FontContainer.setDefaultGlyphWidth(defaultWidth);
		type0FontContainer.setWidthsArray(widths);

		return true;
	}

	/**
	 * This method return false and updates the FontContainer if the Composite
	 * Font TYPE 2 is damaged or missing.
	 * 
	 * @param pfDescriptor
	 * @return
	 */
	boolean checkFontFileElement_CIDFontType2(
			PDFontDescriptorDictionary pfDescriptor) throws ValidationException {

		// ---- FontFile Validation
		PDStream ff1 = pfDescriptor.getFontFile();
		PDStream ff2 = pfDescriptor.getFontFile2();
		PDStream ff3 = pfDescriptor.getFontFile3();

		boolean onlyOne = (ff1 != null && ff2 == null && ff3 == null)
		|| (ff1 == null && ff2 != null && ff3 == null)
		|| (ff1 == null && ff2 == null && ff3 != null);

		if ((ff2 == null) || !onlyOne) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is invalid"));
			return false;
		}

		// ---- Stream validation should be done by the StreamValidateHelper.
		// ---- Process font specific check
		COSStream stream = ff2.getStream();
		if (stream == null) {
			this.fontContainer.addError(new ValidationResult.ValidationError(
					ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is missing"));
			this.fontContainer.setFontProgramEmbedded(false);
			return false;
		}

		boolean hasLength1 = stream.getInt(COSName
				.getPDFName(FONT_DICTIONARY_KEY_LENGTH1)) > 0;
				if (!hasLength1) {
					this.fontContainer.addError(new ValidationResult.ValidationError(
							ValidationConstants.ERROR_FONTS_FONT_FILEX_INVALID,
					"The FontFile is invalid"));
					return false;
				}

				// ---- try to load the font using the java.awt.font object.
				// ---- if the font is invalid, an exception will be thrown
				TrueTypeFont ttf = null;
				try {
					// ---- According to PDF Reference, CIDFontType2 is a TrueType font.
					// ---- Remark : Java.awt.Font throws exception when a CIDFontType2 is
					// parsed even if it is valid.
					ttf = new CIDFontType2Parser(true).parseTTF(new ByteArrayInputStream(ff2
							.getByteArray()));
				} catch (Exception e) {
					// ---- Exceptionally, Exception is catched Here because of damaged font
					// can throw NullPointer Exception...
					this.fontContainer.addError(new ValidationResult.ValidationError(
							ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
					return false;
				}

				return checkTTFontMetrics(ttf) && checkFontFileMetaData(pfDescriptor, ff2);
	}

	/**
	 * For a CIDFont the width array, there are two formats of width array :
	 * <UL>
	 * <li>C [W1...Wn] : C is an integer specifying a starting CID value and the
	 * array of n numbers specify widths for n consecutive CIDs.
	 * <li>Cf Cl W : Defines the same width W for the range Cf to Cl
	 * </UL>
	 * This method gets a linked hash map of width where the key is a CID and the
	 * value is the Width.
	 * 
	 * @return
	 * @throws ValidationException
	 */
	protected LinkedHashMap<Integer, Integer> getWidthsArray()
	throws ValidationException {
		LinkedHashMap<Integer, Integer> widthsMap = new LinkedHashMap<Integer, Integer>();
		COSDocument cDoc = handler.getDocument().getDocument();
		COSBase cBase = this.cidFont.getItem(COSName.getPDFName("W"));
		COSArray wArr = COSUtils.getAsArray(cBase, cDoc);

		for (int i = 0; i < wArr.size();) {

			int firstCid = wArr.getInt(i);

			if (i + 1 >= wArr.size()) {
				throw new ValidationException("Invalid format of the W entry");
			}

			COSBase cb = wArr.getObject(i + 1);
			if (COSUtils.isArray(cb, cDoc)) {

				// ---- First Format
				COSArray seqWidths = COSUtils.getAsArray(cb, cDoc);
				widthsMap.put(firstCid, seqWidths.getInt(0));
				for (int jw = 1; jw < seqWidths.size(); jw++) {
					widthsMap.put((firstCid + jw), seqWidths.getInt(jw));
				}

				i = i + 2;

			} else {

				// ---- Second Format
				if (i + 2 >= wArr.size()) {
					throw new ValidationException("Invalid format of the W entry");
				}

				int lastCid = wArr.getInt(i + 1);
				int commonWidth = wArr.getInt(i + 2);
				for (int jw = firstCid; jw <= lastCid; ++jw) {
					widthsMap.put((firstCid + jw), commonWidth);
				}

				i = i + 3;

			}

		}

		return widthsMap;
	}

	/**
	 * If the embedded font is a subset, the CIDSet entry is mandatory and must be
	 * a Stream. This method returns true if the CIDSet entry respects conditions,
	 * otherwise the method returns false and the FontContainer is updated.
	 * 
	 * @param pfDescriptor
	 * @return
	 */
	protected boolean checkCIDSet(PDFontDescriptorDictionary pfDescriptor) {
		if (isSubSet(pfDescriptor.getFontName())) {
			COSBase cidset = pfDescriptor.getCOSDictionary().getItem(
					COSName.getPDFName(FONT_DICTIONARY_KEY_CIDSET));
			if (cidset == null
					|| !COSUtils.isStream(cidset, this.handler.getDocument()
							.getDocument())) {
				this.fontContainer.addError(new ValidationResult.ValidationError(
						ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET,
				"The CIDSet entry is missing for the Composite Subset"));
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param fontDesc
	 * @return
	 * @throws ValidationException
	 */
	protected boolean processCIDFontType2(COSBase fontDesc)
	throws ValidationException {
		COSDictionary fontDescDic = COSUtils.getAsDictionary(fontDesc, handler
				.getDocument().getDocument());
		if (fontDescDic == null) {
			throw new ValidationException(
			"Unable to process CIDFontType2 because of the font descriptor is invalid.");
		}
		PDFontDescriptorDictionary pfDescriptor = new PDFontDescriptorDictionary(
				fontDescDic);
		boolean isValid = checkFontDescriptorMandatoryFields(pfDescriptor);
		isValid = isValid && checkCIDKeyedFontName(pfDescriptor, false);
		isValid = isValid && checkFontFileElement_CIDFontType2(pfDescriptor);
		isValid = isValid && checkCIDSet(pfDescriptor);
		return isValid;
	}
}
