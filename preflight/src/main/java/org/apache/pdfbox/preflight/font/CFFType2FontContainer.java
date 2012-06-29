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

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.preflight.PreflightConstants;


public class CFFType2FontContainer extends AbstractFontContainer {
	/**
	 * Object which contains the TrueType font data (used in the CFFType2 font) 
	 * extracted by the TrueTypeParser object
	 */
	private TrueTypeFont fontObject = null;
	private CIDToGIDMap cidToGidMap = null;

	private int numberOfLongHorMetrics;
	private int unitsPerEm;
	private int[] glyphWidths;

	public CFFType2FontContainer(CompositeFontContainer container) {
		super(container.getFont());
		this.cidKnownByFont.putAll(container.cidKnownByFont);
		this.isFontProgramEmbedded = container.isFontProgramEmbedded;
		this.errors.addAll(container.errors);
	}

	@Override
	public void checkCID(int cid) throws GlyphException {
		if (isAlreadyComputedCid(cid)) {
			return;
		}

		final float widthProvidedByPdfDictionary = this.font.getFontWidth(cid);

		final int glyphIndex = getGlyphIndex(cid);

		if(this.fontObject.getGlyph().getGlyphs().length <= glyphIndex) {
			GlyphException ge = new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
					"CID " + cid + " is missing from font \"" + this.font.getBaseFont() + "\"");
			addKnownCidElement(new GlyphDetail(cid, ge));
			throw ge;
		}

		// glyph exists we can check the width
		float glypdWidth = glyphWidths[numberOfLongHorMetrics - 1];
		if (glyphIndex < numberOfLongHorMetrics) {
			glypdWidth = glyphWidths[glyphIndex];
		}
		float widthInFontProgram = ((glypdWidth * 1000) / unitsPerEm);

		checkWidthsConsistency(cid, widthProvidedByPdfDictionary, widthInFontProgram);
		addKnownCidElement(new GlyphDetail(cid));
	}

	/**
	 * If CIDToGID map is Identity, the GID equals to the CID.
	 * Otherwise the conversion is done by the CIDToGID map
	 * @param cid
	 * @return
	 * @throws GlyphException
	 */
	private int getGlyphIndex(int cid) throws GlyphException {
		int glyphIndex = cid;

		if (this.cidToGidMap != null) {
			glyphIndex = cidToGidMap.getGID(cid);
			if (glyphIndex==cidToGidMap.NOTDEF_GLYPH_INDEX) {
				GlyphException ge = new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
						"CID " + cid + " can't be found in the cidToGid map");
				addKnownCidElement(new GlyphDetail(cid, ge));
				throw ge;
			}
		}
		return glyphIndex;
	}

	void setFontObject(TrueTypeFont fontObject) {
		this.fontObject = fontObject;
	}

	void setCmap(CIDToGIDMap cmap) {
		this.cidToGidMap = cmap;
	}

	void setNumberOfLongHorMetrics(int numberOfLongHorMetrics) {
		this.numberOfLongHorMetrics = numberOfLongHorMetrics;
	}

	void setUnitsPerEm(int unitsPerEm) {
		this.unitsPerEm = unitsPerEm;
	}

	void setGlyphWidths(int[] _glyphWidths) {
		this.glyphWidths = new int[_glyphWidths.length];
		for( int i =0 ; i < _glyphWidths.length; ++i) {
			this.glyphWidths[i] = _glyphWidths[i];
		}
	}

}
