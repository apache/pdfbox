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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFFont.Mapping;
import org.apache.padaf.preflight.ValidationConstants;


public class CFFType0FontContainer extends AbstractFontContainer {
	private Map<Integer, Integer> widthsArray = new LinkedHashMap<Integer, Integer>(0);
	/**
	 * Represent the missingWidth value of the FontDescriptor dictionary.
	 * According to the PDF Reference, if this value is missing, the default 
	 * one is 0.
	 */
	private float defaultGlyphWidth = 0;
	/**
	 * Object which contains the CFF data extracted by the
	 * CFFParser object
	 */
	private List<CFFFont> fontObject = null;

	public CFFType0FontContainer(CompositeFontContainer container) {
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

		// ---- build the font container and keep it in the Handler.
	  boolean cidFound = false;
		for (CFFFont font : this.fontObject) {
			Collection<Mapping> cMapping = font.getMappings();
			for (Mapping mapping : cMapping) {
				// -- REMARK : May be this code must be changed like the Type1FontContainer to Map the SID with the character name?
				// -- Not enough PDF with this kind of Font to test the current implementation
				if (mapping.getSID()==cid) {
					cidFound = true;
				}
			}
		}

		if (!cidFound) {
			GlyphException ge = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
																							"CID " + cid + " is missing from the Composite Font format \"" 
																							+ this.font.getBaseFont()+"\""	);
		  addKnownCidElement(new GlyphDetail(cid, ge));
			throw ge;
		}


		float widthProvidedByPdfDictionary = this.defaultGlyphWidth;
		if (this.widthsArray.containsKey(cid)) {
			widthProvidedByPdfDictionary = this.widthsArray.get(cid);
		}
		float widthInFontProgram = 0;

		try {
			// ---- Search the CID in all CFFFont in the FontProgram
			for (CFFFont cff : fontObject) {
				widthInFontProgram = cff.getWidth(cid);
				if (widthInFontProgram != defaultGlyphWidth) {
					break;
				}
			}
		} catch (IOException e) {
			GlyphException ge = new GlyphException(ValidationConstants.ERROR_FONTS_DAMAGED, cid, 
					"Unable to get width of the CID/SID : " + cid);
			addKnownCidElement(new GlyphDetail(cid, ge));
			throw ge;
		}

		checkWidthsConsistency(cid, widthProvidedByPdfDictionary, widthInFontProgram);
	  addKnownCidElement(new GlyphDetail(cid));
	}

	void setWidthsArray(Map<Integer, Integer> widthsArray) {
		this.widthsArray = widthsArray;
	}

	void setDefaultGlyphWidth(float defaultGlyphWidth) {
		this.defaultGlyphWidth = defaultGlyphWidth;
	}

	void setFontObject(List<CFFFont> fontObject) {
		this.fontObject = fontObject;
	}
}