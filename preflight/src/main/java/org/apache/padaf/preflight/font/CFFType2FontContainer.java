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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.padaf.preflight.ValidationConstants;


public class CFFType2FontContainer extends AbstractFontContainer {
	private Map<Integer, Integer> widthsArray = new LinkedHashMap<Integer, Integer>(0);
	/**
	 * Represent the missingWidth value of the FontDescriptor dictionary.
	 * According to the PDF Reference, if this value is missing, the default 
	 * one is 0.
	 */
	private float defaultGlyphWidth = 0;
	/**
	 * Object which contains the TrueType font data (used in the CFFType2 font) 
	 * extracted by the TrueTypeParser object
	 */
	private TrueTypeFont fontObject = null;
	private CMap cidToGidMap = null;
	
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

		float widthProvidedByPdfDictionary = this.defaultGlyphWidth;
		if (this.widthsArray.containsKey(cid)) {
			Integer i = this.widthsArray.get(cid);
			widthProvidedByPdfDictionary = i.floatValue();
		}

		int glyphIndex = getGlyphIndex(cid);
		
		if(this.fontObject.getGlyph().getGlyphs().length <= glyphIndex) {
			GlyphException ge = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
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
			byte[] cidAsByteArray = null;
			if (cid < 256) {
				cidAsByteArray = new byte[1];
				cidAsByteArray[0] = (byte) (cid & 0xFF);
			} else {
				cidAsByteArray = new byte[1];
				cidAsByteArray[0] = (byte) ((cid >> 8) & 0xFF);
				cidAsByteArray[1] = (byte) (cid & 0xFF);
			}

			String glyphIdAsString = this.cidToGidMap.lookup(cidAsByteArray, 0,	cidAsByteArray.length);
			// ---- glyphIdAsString should be a Integer
			// TODO OD-PDFA-4 : A vérifier avec un PDF qui contient une stream pour
			// CidToGid...
			try {
				glyphIndex = Integer.parseInt(glyphIdAsString);
			} catch (NumberFormatException e) {
				GlyphException ge = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
						"CID " + cid + " isn't linked with a GlyphIndex >> " + glyphIdAsString);
			  addKnownCidElement(new GlyphDetail(cid, ge));
				throw ge;
			}
		}
		return glyphIndex;
	}

	void setPdfWidths(Map<Integer, Integer> widthsArray) {
		this.widthsArray = widthsArray;
	}

	void setDefaultGlyphWidth(float defaultGlyphWidth) {
		this.defaultGlyphWidth = defaultGlyphWidth;
	}

	void setFontObject(TrueTypeFont fontObject) {
		this.fontObject = fontObject;
	}

	void setCmap(CMap cmap) {
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
