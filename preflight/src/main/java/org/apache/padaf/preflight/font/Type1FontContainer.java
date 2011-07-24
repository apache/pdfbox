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


import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFFont.Mapping;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.font.type1.Type1;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class Type1FontContainer extends AbstractFontContainer {

	private List<?> widthsArray = new ArrayList(0);
	private int firstCharInWidthsArray = 0;

	/**
	 * Represent the missingWidth value of the FontDescriptor dictionary.
	 * According to the PDF Reference, if this value is missing, the default 
	 * one is 0.
	 */
	private float defaultGlyphWidth = 0;
	/**
	 * Object which contains the Type1Font data extracted by the
	 * Type1Parser object
	 */
	private Type1 fontObject = null;

	private List<CFFFont> cffFonts= null;

	public Type1FontContainer(PDFont fd) {
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

	void setFontObject(Type1 fontObject) {
		this.fontObject = fontObject;
	}

	void setCFFFontObjects(List<CFFFont> fontObject) {
		this.cffFonts = fontObject;
	}

	@Override
	public void checkCID(int cid) throws GlyphException {
		if (isAlreadyComputedCid(cid)) {
			return;
		}

		int indexOfWidth = (cid - firstCharInWidthsArray);
		float widthProvidedByPdfDictionary = this.defaultGlyphWidth;

		int widthInFontProgram =0;
		try {
			if (this.fontObject != null) {
				widthInFontProgram = this.fontObject.getWidthOfCID(cid);
			} else {
				// -- Retrieves the SID with the Character Name in the encoding map
				// -- Need more PDF with a Type1C subfont to valid this implementation
				String name = this.font.getFontEncoding().getName(cid);
				for (CFFFont cff : cffFonts) {
					int SID = cff.getEncoding().getSID(cid);
					for (Mapping m : cff.getMappings() ){
						if (m.getName().equals(name)) {
							SID = m.getSID();
							break;
						}
					}
					widthInFontProgram = cff.getWidth(SID);
					if (widthInFontProgram != defaultGlyphWidth) {
						break;
					}
				}
			}
		} catch (GlyphException e) {
			addKnownCidElement(new GlyphDetail(cid, e));
			throw e;
		} catch (IOException e) {
			GlyphException ge = new GlyphException(ValidationConstants.ERROR_FONTS_DAMAGED, cid, 
					"Unable to get width of the CID/SID : " + cid);
			addKnownCidElement(new GlyphDetail(cid, ge));
			throw ge;
		}

		if (indexOfWidth >= 0 && indexOfWidth < this.widthsArray.size()) {
			COSInteger w = (COSInteger)this.widthsArray.get(indexOfWidth);
			widthProvidedByPdfDictionary = w.intValue(); 
		}

		checkWidthsConsistency(cid, widthProvidedByPdfDictionary, widthInFontProgram);
		addKnownCidElement(new GlyphDetail(cid));
	}

}