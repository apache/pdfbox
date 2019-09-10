/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.font;


/**
 * Standard PostScript Type 1 fonts 
 */
public enum Type1Fonts {
	TIMES_ROMAN("Times-Roman"),
    TIMES_BOLD("Times-Bold"),
    TIMES_ITALIC("Times-Italic"),
    TIMES_BOLD_ITALIC("Times-BoldItalic"),
    HELVETICA("Helvetica"),
    HELVETICA_BOLD("Helvetica-Bold"),
    HELVETICA_OBLIQUE("Helvetica-Oblique"),
    HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique"),
    COURIER("Courier"),
    COURIER_BOLD("Courier-Bold"),
    COURIER_OBLIQUE("Courier-Oblique"),
    COURIER_BOLD_OBLIQUE("Courier-BoldOblique"),
    SYMBOL("Symbol"),
    ZAPF_DINGBATS("ZapfDingbats");
	
	private String fontName;
	private volatile PDType1Font font;
	
	private Type1Fonts(String fontName) {
		this.fontName = fontName;
	}
	
	public String fontName() {
		return fontName;
	}
	
	public PDType1Font font() {
		if (font == null) {
			font = new PDType1Font(fontName);
		}
		return font;
	}
}