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


import org.apache.padaf.preflight.ValidationConstants;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Because Type3 font program is an inner type of the PDF file, 
 * this font container is quite different from the other because
 * all character/glyph are already checked.
 * 
 */
public class Type3FontContainer extends AbstractFontContainer {

  public Type3FontContainer(PDFont fd) {
    super(fd);
  }

  @Override
  public void checkCID(int cid) throws GlyphException {
  	if (!isAlreadyComputedCid(cid)) {
  		// missing glyph
  		GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_GLYPH_MISSING, cid, 
  																					"There are no glyph in the Type 3 font for the character \"" + cid + "\"");
	  	addKnownCidElement(new GlyphDetail(cid, e));
	  	throw e;
  	}  	
  } 
}