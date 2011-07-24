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


import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class TrueTypeFontContainer extends AbstractFontContainer {
	private List<?> widthsArray = new ArrayList(0);
	private int firstCharInWidthsArray = 0;

	/**
	 * Represent the missingWidth value of the FontDescriptor dictionary.
	 * According to the PDF Reference, if this value is missing, the default 
	 * one is 0.
	 */
	private float defaultGlyphWidth = 0;
	/**
	 * Object which contains the TrueType font data extracted by the
	 * TrueTypeParser object
	 */
	private TrueTypeFont fontObject = null;
	private CMAPEncodingEntry cmap = null;

	private int numberOfLongHorMetrics;
	private int unitsPerEm;
	private int[] glyphWidths;

	public TrueTypeFontContainer(PDFont fd) {
		super(fd);
	}

	void setWidthsArray(List<?> widthsArray) {
		this.widthsArray = widthsArray;
	}

	void setFirstCharInWidthsArray(int firstCharInWidthsArray) {
		this.firstCharInWidthsArray = firstCharInWidthsArray;
	}

	void setDefaultGlyphWidth(float defaultGlyphWidth) {
		this.defaultGlyphWidth = defaultGlyphWidth;
	}

	void setFontObjectAndInitializeInnerFields(TrueTypeFont fontObject) {
		this.fontObject = fontObject;
		this.unitsPerEm = this.fontObject.getHeader().getUnitsPerEm();
		this.glyphWidths = this.fontObject.getHorizontalMetrics().getAdvanceWidth();
		// ---- In a Mono space font program, the length of the AdvanceWidth array must be one.
		// ---- According to the TrueType font specification, the Last Value of the AdvanceWidth array
		// ---- is apply to the subsequent glyphs. So if the GlyphId is greater than the length of the array
		// ---- the last entry is used.
		this.numberOfLongHorMetrics = this.fontObject.getHorizontalHeader().getNumberOfHMetrics();
	}

	void setCMap(CMAPEncodingEntry cmap) {
		this.cmap = cmap;
	}

	@Override
	public void checkCID(int cid) throws GlyphException {
		if (isAlreadyComputedCid(cid)) {
			return;
		}

		int indexOfWidth = (cid - firstCharInWidthsArray);
		float widthProvidedByPdfDictionary = this.defaultGlyphWidth;
		float widthInFontProgram ;

		int innerFontCid = cid;
		if (cmap.getPlatformEncodingId() == 1 && cmap.getPlatformId() == 3) {
			try {
				Encoding fontEncoding = this.font.getFontEncoding();
				String character = fontEncoding.getCharacter(cid);
				if (character == null) {
					GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, 
							cid, 
							"The character \"" + cid 
							+ "\" in the font program \""
							+ this.font.getBaseFont() 
							+ "\"is missing from the Charater Encoding.");
					addKnownCidElement(new GlyphDetail(cid, e));
					throw e;	
				}

				char[] characterArray = character.toCharArray();
				if (characterArray.length == 1 ) {
					innerFontCid = (int)characterArray[0];
				} else {
					// TODO OD-PDFA-87 A faire?
					innerFontCid = (int)characterArray[0];
					for (int i = 1; i < characterArray.length; ++i) {
						if (cmap.getGlyphId((int)characterArray[i]) == 0) {
							GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, 
									cid, 
									"A glyph for the character \"" + cid 
									+ "\" in the font program \""
									+ this.font.getBaseFont() 
									+ "\"is missing. There are " + characterArray.length + " glyph used by this character...");
							addKnownCidElement(new GlyphDetail(cid, e));
							throw e;	
						}
					}
				}
			} catch (IOException ioe) {
				GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_ENCODING_IO, 
						cid, 
						"Unable to get the encoding object from the PDFont object during the validation of cid \"" + cid 
						+ "\" in the font program \""
						+ this.font.getBaseFont() 
						+ "\".");
				addKnownCidElement(new GlyphDetail(cid, e));
				throw e;	
			}
		}

		// search glyph
		int glyphId = cmap.getGlyphId(innerFontCid);
		if (glyphId == 0) {
			GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, 
					cid, 
					"Glyph for the character \"" + cid 
					+ "\" in the font program \""
					+ this.font.getBaseFont() 
					+ "\"is missing.");
			addKnownCidElement(new GlyphDetail(cid, e));
			throw e;	
		}

		// compute glyph width
		float glypdWidth = glyphWidths[numberOfLongHorMetrics - 1];
		if (glyphId < numberOfLongHorMetrics) {
			glypdWidth = glyphWidths[glyphId];
		}
		widthInFontProgram = ((glypdWidth * 1000) / unitsPerEm);

		// search width in the PDF file
		if (indexOfWidth >= 0 && indexOfWidth < this.widthsArray.size()) {
			COSInteger w = (COSInteger)this.widthsArray.get(indexOfWidth);
			widthProvidedByPdfDictionary = w.intValue(); 
		}

		checkWidthsConsistency(cid, widthProvidedByPdfDictionary, widthInFontProgram);
		addKnownCidElement(new GlyphDetail(cid));
	}

}