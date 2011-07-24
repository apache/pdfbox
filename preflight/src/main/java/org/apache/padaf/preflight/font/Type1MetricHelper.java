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

import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_ENCODING_PDFDOC;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_ENCODING_WIN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CharStringCommand;
import org.apache.fontbox.cff.Type1CharStringParser;
import org.apache.fontbox.cff.Type1FontUtil;
import org.apache.padaf.preflight.ValidationException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.MacRomanEncoding;
import org.apache.pdfbox.encoding.PdfDocEncoding;
import org.apache.pdfbox.encoding.StandardEncoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;

/**
 * This class computes a Type1 font stream to extract Glyph Metrics. The given
 * stream must be a valid type 1 stream.
 * 
 * Remark : According to the PDF Reference only PostScript Type 1 binary fonts
 * are allowed in a conforming PDF file so the encrypted "eexec" data are
 * considered as binary data...
 * 
 * This class is depreciated, now it is better to use the Type1Parser. 
 */
@Deprecated 
public class Type1MetricHelper {
	protected static final char NAME_START = '/';

	protected static final int FULL_NAME_TOKEN = 1;
	protected static final int FAMILY_NAME_TOKEN = 2;
	protected static final int DUP_TOKEN = 3;
	protected static final int FONT_NAME_TOKEN = 4;
	protected static final int ENCODING_TOKEN = 5;
	protected static final int READONLY_TOKEN = 6;

	protected static final int LEN_IV_TOKEN = 7;
	protected static final int CHARSTRINGS_TOKEN = 8;
	protected static final int CHAR_LABEL_TOKEN = 9;

	protected static final int OBJ_NAME_TOKEN = 10;

	protected static final String NOTDEF = "/.notdef";

	private static final String PS_STANDARD_ENCODING = "StandardEncoding";
	private static final String PS_ISOLATIN_ENCODING = "ISOLatin1Encoding";

	/**
	 * The PostScript font stream.
	 */
	private InputStream font = null;
	/**
	 * The length in bytes of the clear-text portion of the Type1 font program.
	 */
	private int clearTextSize = 0;
	/**
	 * The length in bytes of the eexec encrypted portion of the type1 font
	 * program.
	 */
	private int eexecSize = 0;
	/**
	 * This map links the character identifier to a internal font program label
	 * which is different from the standard Encoding
	 */
	private Map<Integer, String> cidToLabel = new HashMap<Integer, String>(0);
	/**
	 * This map links the character label to a character identifier which is
	 * different from the standard Encoding.
	 */
	private Map<String, Integer> labelToCid = new HashMap<String, Integer>(0);
	/**
	 * This map link the character label to a container containing Glyph
	 * description.
	 */
	private Map<String, Type1GlyphDescription> labelToMetric = new HashMap<String, Type1GlyphDescription>(
			0);

	/**
	 * The character encoding of the Font
	 */
	private Encoding encoding = null;

	/**
	 * The family name of the font
	 */
	protected String familyName = null;
	/**
	 * The full name of the font
	 */
	protected String fullName = null;
	/**
	 * The font name of the font
	 */
	protected String fontName = null;

	/**
	 * 
	 * @param type1
	 *          The unfiltered PostScript Type 1 Font stream.
	 * @param length1
	 *          The length in bytes of the clear-text portion of the Type1 font
	 *          program.
	 * @param length2
	 *          The length in bytes of the eexec encrypted portion of the type1
	 *          font program.
	 * @param encodingName
	 *          the Encoding name, StandardEncoding is used for unknown name
	 */
	public Type1MetricHelper(InputStream type1, int length1, int length2,
			String encodingName) {
		super();
		this.font = type1;
		this.clearTextSize = length1;
		this.eexecSize = length2;
		this.cidToLabel.put(-1, NOTDEF);
		this.labelToCid.put(NOTDEF, -1);
		
		
		// ---- Instantiate the Encoding Map
		if (FONT_DICTIONARY_VALUE_ENCODING_MAC.equals(encodingName)) {
			this.encoding = new MacRomanEncoding();
		} else if (FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP.equals(encodingName)) {
			this.encoding = new MacRomanEncoding();
		} else if (FONT_DICTIONARY_VALUE_ENCODING_WIN.equals(encodingName)) {
			this.encoding = new WinAnsiEncoding();
		} else if (FONT_DICTIONARY_VALUE_ENCODING_PDFDOC.equals(encodingName)) {
			this.encoding = new PdfDocEncoding();
		} else {
			this.encoding = new StandardEncoding();
		}
	}

	/**
	 * 
	 * @param type1
	 *          The unfiltered PostScript Type 1 Font stream.
	 * @param length1
	 *          The length in bytes of the clear-text portion of the Type1 font
	 *          program.
	 * @param length2
	 *          The length in bytes of the eexec encrypted portion of the type1
	 *          font program.
	 * @param enc
	 *          The Encoding inherited Object
	 */
	public Type1MetricHelper(InputStream type1, int length1, int length2,
			Encoding enc) {
		super();
		this.font = type1;
		this.clearTextSize = length1;
		this.eexecSize = length2;
		this.cidToLabel.put(-1, NOTDEF);
		this.labelToCid.put(NOTDEF, -1);

		// ---- Instantiate the Encoding Map
		if (enc != null) {
			this.encoding = enc;
		} else {
			this.encoding = new StandardEncoding();
		}
	}

	/**
	 * Close the font stream
	 */
	public void close() {
		IOUtils.closeQuietly(this.font);
	}

	private void createStandardEncoding() {
		this.labelToCid.put("/A", 0101);
		this.labelToCid.put("/AE", 0341);
		this.labelToCid.put("/B", 0102);
		this.labelToCid.put("/C", 0103);
		this.labelToCid.put("/D", 0104);
		this.labelToCid.put("/E", 0105);
		this.labelToCid.put("/F", 0106);
		this.labelToCid.put("/G", 0107);
		this.labelToCid.put("/H", 0110);
		this.labelToCid.put("/I", 0111);
		this.labelToCid.put("/J", 0112);
		this.labelToCid.put("/K", 0113);
		this.labelToCid.put("/L", 0114);
		this.labelToCid.put("/Lslash", 0350);
		this.labelToCid.put("/M", 0115);
		this.labelToCid.put("/N", 0116);
		this.labelToCid.put("/O", 0117);
		this.labelToCid.put("/OE", 0352);
		this.labelToCid.put("/Oslash", 0351);
		this.labelToCid.put("/P", 0120);
		this.labelToCid.put("/Q", 0121);
		this.labelToCid.put("/R", 0122);
		this.labelToCid.put("/S", 0123);
		this.labelToCid.put("/T", 0124);
		this.labelToCid.put("/U", 0125);
		this.labelToCid.put("/V", 0126);
		this.labelToCid.put("/W", 0127);
		this.labelToCid.put("/X", 0130);
		this.labelToCid.put("/Y", 0131);
		this.labelToCid.put("/Z", 0132);
		this.labelToCid.put("/a", 0141);
		this.labelToCid.put("/acute", 0302);
		this.labelToCid.put("/acute", 0302);
		this.labelToCid.put("/ae", 0361);
		this.labelToCid.put("/ampersand", 046);
		this.labelToCid.put("/asciicircum", 0136);
		this.labelToCid.put("/asciitilde", 0176);
		this.labelToCid.put("/asterisk", 052);
		this.labelToCid.put("/at", 0100);
		this.labelToCid.put("/b", 0142);
		this.labelToCid.put("/backslash", 0134);
		this.labelToCid.put("/bar", 0174);
		this.labelToCid.put("/braceleft", 0173);
		this.labelToCid.put("/braceright", 0175);
		this.labelToCid.put("/bracketleft", 0133);
		this.labelToCid.put("/bracketright", 0135);
		this.labelToCid.put("/breve", 0306);
		this.labelToCid.put("/bullet", 0267);
		this.labelToCid.put("/c", 0143);
		this.labelToCid.put("/caron", 0317);
		this.labelToCid.put("/cedilla", 0313);
		this.labelToCid.put("/cent", 0242);
		this.labelToCid.put("/circumflex", 0303);
		this.labelToCid.put("/colon", 072);
		this.labelToCid.put("/comma", 054);
		this.labelToCid.put("/currency", 0250);
		this.labelToCid.put("/d", 0144);
		this.labelToCid.put("/dagger", 0262);
		this.labelToCid.put("/daggerdbl", 0263);
		this.labelToCid.put("/dieresis", 0310);
		this.labelToCid.put("/dollar", 044);
		this.labelToCid.put("/dotaccent", 0307);
		this.labelToCid.put("/dotlessi", 0365);
		this.labelToCid.put("/e", 0145);
		this.labelToCid.put("/eight", 070);
		this.labelToCid.put("/ellipsis", 274);
		this.labelToCid.put("/emdash", 0320);
		this.labelToCid.put("/endash", 0261);
		this.labelToCid.put("/equal", 075);
		this.labelToCid.put("/exclam", 041);
		this.labelToCid.put("/exclamdown", 0241);
		this.labelToCid.put("/f", 0146);
		this.labelToCid.put("/fi", 0256);
		this.labelToCid.put("/five", 0065);
		this.labelToCid.put("/fl", 0257);
		this.labelToCid.put("/florin", 0246);
		this.labelToCid.put("/four", 064);
		this.labelToCid.put("/fraction", 0244);
		this.labelToCid.put("/g", 0147);
		this.labelToCid.put("/germandbls", 0373);
		this.labelToCid.put("/grave", 0301);
		this.labelToCid.put("/greater", 0076);
		this.labelToCid.put("/guillemotleft", 0253);
		this.labelToCid.put("/guillemotright", 0273);
		this.labelToCid.put("/guilsinglleft", 0254);
		this.labelToCid.put("/guilsinglright", 0255);
		this.labelToCid.put("/h", 0150);
		this.labelToCid.put("/hungarumlaut", 0315);
		this.labelToCid.put("/hyphen", 055);
		this.labelToCid.put("/i", 0151);
		this.labelToCid.put("/j", 0152);
		this.labelToCid.put("/k", 0153);
		this.labelToCid.put("/l", 0154);
		this.labelToCid.put("/less", 0074);
		this.labelToCid.put("/lslash", 0370);
		this.labelToCid.put("/m", 0155);
		this.labelToCid.put("/macron", 0305);
		this.labelToCid.put("/n", 0156);
		this.labelToCid.put("/nine", 071);
		this.labelToCid.put("/numbersign", 043);
		this.labelToCid.put("/o", 0157);
		this.labelToCid.put("/oe", 0372);
		this.labelToCid.put("/ogonek", 0316);
		this.labelToCid.put("/one", 061);
		this.labelToCid.put("/ordfeminine", 0343);
		this.labelToCid.put("/ordmasculine", 0353);
		this.labelToCid.put("/oslash", 0371);
		this.labelToCid.put("/p", 0160);
		this.labelToCid.put("/paragraph", 0266);
		this.labelToCid.put("/parenleft", 050);
		this.labelToCid.put("/parenright", 051);
		this.labelToCid.put("/percent", 045);
		this.labelToCid.put("/period", 056);
		this.labelToCid.put("/periodcentered", 0264);
		this.labelToCid.put("/perthousand", 0275);
		this.labelToCid.put("/plus", 0053);
		this.labelToCid.put("/q", 0161);
		this.labelToCid.put("/question", 077);
		this.labelToCid.put("/questiondown", 0277);
		this.labelToCid.put("/quotedbl", 0042);
		this.labelToCid.put("/quotedblbase", 0271);
		this.labelToCid.put("/quotedblleft", 0252);
		this.labelToCid.put("/quotedblright", 0272);
		this.labelToCid.put("/quoteleft", 0140);
		this.labelToCid.put("/quoteright", 047);
		this.labelToCid.put("/quotesinglbase", 0270);
		this.labelToCid.put("/quotesingle", 0251);
		this.labelToCid.put("/r", 0162);
		this.labelToCid.put("/ring", 0312);
		this.labelToCid.put("/s", 0163);
		this.labelToCid.put("/section", 0247);
		this.labelToCid.put("/semicolon", 0073);
		this.labelToCid.put("/seven", 0067);
		this.labelToCid.put("/six", 066);
		this.labelToCid.put("/slash", 057);
		this.labelToCid.put("/space", 040);
		this.labelToCid.put("/sterling", 0243);
		this.labelToCid.put("/t", 0164);
		this.labelToCid.put("/three", 063);
		this.labelToCid.put("/tilde", 0304);
		this.labelToCid.put("/two", 062);
		this.labelToCid.put("/u", 0165);
		this.labelToCid.put("/underscore", 0137);
		this.labelToCid.put("/v", 0166);
		this.labelToCid.put("/w", 0167);
		this.labelToCid.put("/x", 0170);
		this.labelToCid.put("/y", 0171);
		this.labelToCid.put("/yen", 0245);
		this.labelToCid.put("/z", 0172);
		this.labelToCid.put("/zero", 060);
		transafertLTOCinCTIL();
	}

	private void transafertLTOCinCTIL() {
		for (Entry<String, Integer> entry : this.labelToCid.entrySet()) {
			this.cidToLabel.put(entry.getValue(), entry.getKey());
		}
	}

	private void createISOLatin1Encoding() {
		this.labelToCid.put("/A", 0101);
		this.labelToCid.put("/AE", 0306);
		this.labelToCid.put("/Aacute", 0301);
		this.labelToCid.put("/Acircumflex", 0302);
		this.labelToCid.put("/Adieresis", 0304);
		this.labelToCid.put("/Agrave", 0300);
		this.labelToCid.put("/Aring", 0305);
		this.labelToCid.put("/Atilde", 0303);
		this.labelToCid.put("/B", 0102);
		this.labelToCid.put("/C", 0103);
		this.labelToCid.put("/Ccedilla", 0307);
		this.labelToCid.put("/D", 0104);
		this.labelToCid.put("/E", 0105);
		this.labelToCid.put("/Eacute", 0311);
		this.labelToCid.put("/Ecircumflex", 0312);
		this.labelToCid.put("/Edieresis", 0313);
		this.labelToCid.put("/Egrave", 0310);
		this.labelToCid.put("/Eth", 0320);
		this.labelToCid.put("/F", 0106);
		this.labelToCid.put("/G", 0107);
		this.labelToCid.put("/H", 0110);
		this.labelToCid.put("/I", 0111);
		this.labelToCid.put("/Iacute", 0315);
		this.labelToCid.put("/Icircumflex", 0316);
		this.labelToCid.put("/Idieresis", 0317);
		this.labelToCid.put("/Igrave", 0314);
		this.labelToCid.put("/J", 0112);
		this.labelToCid.put("/K", 0113);
		this.labelToCid.put("/L", 0114);
		this.labelToCid.put("/M", 0115);
		this.labelToCid.put("/N", 0116);
		this.labelToCid.put("/Ntilde", 0321);
		this.labelToCid.put("/O", 0117);
		this.labelToCid.put("/Oacute", 0323);
		this.labelToCid.put("/Ocircumflex", 0324);
		this.labelToCid.put("/Odieresis", 0326);
		this.labelToCid.put("/Ograve", 0322);
		this.labelToCid.put("/Oslash", 0330);
		this.labelToCid.put("/Otilde", 0325);
		this.labelToCid.put("/P", 0120);
		this.labelToCid.put("/Q", 0121);
		this.labelToCid.put("/R", 0122);
		this.labelToCid.put("/S", 0123);
		this.labelToCid.put("/T", 0124);
		this.labelToCid.put("/Thorn", 0336);
		this.labelToCid.put("/U", 0125);
		this.labelToCid.put("/Uacute", 0332);
		this.labelToCid.put("/Ucircumflex", 0333);
		this.labelToCid.put("/Udieresis", 0334);
		this.labelToCid.put("/Ugrave", 0331);
		this.labelToCid.put("/V", 0126);
		this.labelToCid.put("/W", 0127);
		this.labelToCid.put("/X", 0130);
		this.labelToCid.put("/Y", 0131);
		this.labelToCid.put("/Yacute", 0335);
		this.labelToCid.put("/Z", 0132);
		this.labelToCid.put("/a", 0141);
		this.labelToCid.put("/aacute", 0341);
		this.labelToCid.put("/acircumflex", 0342);
		this.labelToCid.put("/acute", 0222);
		this.labelToCid.put("/acute", 0264);
		this.labelToCid.put("/adieresis", 0344);
		this.labelToCid.put("/ae", 0346);
		this.labelToCid.put("/agrave", 0340);
		this.labelToCid.put("/ampersand", 0046);
		this.labelToCid.put("/aring", 0345);
		this.labelToCid.put("/asciicircum", 0136);
		this.labelToCid.put("/asciitilde", 0176);
		this.labelToCid.put("/asterisk", 0052);
		this.labelToCid.put("/at", 0100);
		this.labelToCid.put("/atilde", 0343);
		this.labelToCid.put("/b", 0142);
		this.labelToCid.put("/backslash", 0134);
		this.labelToCid.put("/bar", 0174);
		this.labelToCid.put("/braceleft", 0173);
		this.labelToCid.put("/braceright", 0175);
		this.labelToCid.put("/bracketleft", 0133);
		this.labelToCid.put("/bracketright", 0135);
		this.labelToCid.put("/breve", 0226);
		this.labelToCid.put("/brokenbar", 0246);
		this.labelToCid.put("/c", 0143);
		this.labelToCid.put("/caron", 0237);
		this.labelToCid.put("/ccedilla", 0347);
		this.labelToCid.put("/cedilla", 0270);
		this.labelToCid.put("/cent", 0242);
		this.labelToCid.put("/circumflex", 0223);
		this.labelToCid.put("/colon", 0072);
		this.labelToCid.put("/comma", 0054);
		this.labelToCid.put("/copyright", 0251);
		this.labelToCid.put("/currency", 0244);
		this.labelToCid.put("/d", 0144);
		this.labelToCid.put("/degree", 0260);
		this.labelToCid.put("/dieresis", 0250);
		this.labelToCid.put("/divide", 0367);
		this.labelToCid.put("/dollar", 0044);
		this.labelToCid.put("/dotaccent", 0227);
		this.labelToCid.put("/dotlessi", 0220);
		this.labelToCid.put("/e", 0145);
		this.labelToCid.put("/eacute", 0351);
		this.labelToCid.put("/ecircumflex", 0352);
		this.labelToCid.put("/edieresis", 0353);
		this.labelToCid.put("/egrave", 0350);
		this.labelToCid.put("/eight", 0070);
		this.labelToCid.put("/equal", 0075);
		this.labelToCid.put("/eth", 0360);
		this.labelToCid.put("/exclam", 0041);
		this.labelToCid.put("/exclamdown", 0241);
		this.labelToCid.put("/f", 0146);
		this.labelToCid.put("/five", 0065);
		this.labelToCid.put("/four", 0064);
		this.labelToCid.put("/g", 0147);
		this.labelToCid.put("/germandbls", 0337);
		this.labelToCid.put("/grave", 0221);
		this.labelToCid.put("/greater", 0076);
		this.labelToCid.put("/guillemotleft", 0253);
		this.labelToCid.put("/guillemotright", 0273);
		this.labelToCid.put("/h", 0150);
		this.labelToCid.put("/hungarumlaut", 0235);
		this.labelToCid.put("/hyphen", 0255);
		this.labelToCid.put("/i", 0151);
		this.labelToCid.put("/iacute", 0355);
		this.labelToCid.put("/icircumflex", 0356);
		this.labelToCid.put("/idieresis", 0357);
		this.labelToCid.put("/igrave", 0354);
		this.labelToCid.put("/j", 0152);
		this.labelToCid.put("/k", 0153);
		this.labelToCid.put("/l", 0154);
		this.labelToCid.put("/less", 0074);
		this.labelToCid.put("/logicalnot", 0254);
		this.labelToCid.put("/m", 0155);
		this.labelToCid.put("/macron", 0257);
		this.labelToCid.put("/minus", 0055);
		this.labelToCid.put("/mu", 0265);
		this.labelToCid.put("/multiply", 0327);
		this.labelToCid.put("/n", 0156);
		this.labelToCid.put("/nine", 0071);
		this.labelToCid.put("/ntilde", 0361);
		this.labelToCid.put("/numbersign", 0043);
		this.labelToCid.put("/o", 0157);
		this.labelToCid.put("/oacute", 0363);
		this.labelToCid.put("/ocircumflex", 0364);
		this.labelToCid.put("/odieresis", 0366);
		this.labelToCid.put("/ogonek", 0236);
		this.labelToCid.put("/ograve", 0362);
		this.labelToCid.put("/one", 0061);
		this.labelToCid.put("/onehalf", 0275);
		this.labelToCid.put("/onequarter", 0274);
		this.labelToCid.put("/onesuperior", 0271);
		this.labelToCid.put("/ordfeminine", 0252);
		this.labelToCid.put("/ordmasculine", 0272);
		this.labelToCid.put("/oslash", 0370);
		this.labelToCid.put("/otilde", 0365);
		this.labelToCid.put("/p", 0160);
		this.labelToCid.put("/paragraph", 0266);
		this.labelToCid.put("/parenleft", 0050);
		this.labelToCid.put("/parenright", 0051);
		this.labelToCid.put("/percent", 0045);
		this.labelToCid.put("/period", 0056);
		this.labelToCid.put("/periodcentered", 0267);
		this.labelToCid.put("/plus", 0053);
		this.labelToCid.put("/plusminus", 0261);
		this.labelToCid.put("/q", 0161);
		this.labelToCid.put("/question", 0077);
		this.labelToCid.put("/questiondown", 0277);
		this.labelToCid.put("/quotedbl", 0042);
		this.labelToCid.put("/quoteleft", 0140);
		this.labelToCid.put("/quoteright", 0047);
		this.labelToCid.put("/r", 0162);
		this.labelToCid.put("/registered", 0256);
		this.labelToCid.put("/ring", 0232);
		this.labelToCid.put("/s", 0163);
		this.labelToCid.put("/section", 0247);
		this.labelToCid.put("/semicolon", 0073);
		this.labelToCid.put("/seven", 0067);
		this.labelToCid.put("/six", 0066);
		this.labelToCid.put("/slash", 0057);
		this.labelToCid.put("/space", 0040);
		this.labelToCid.put("/sterling", 0243);
		this.labelToCid.put("/t", 0164);
		this.labelToCid.put("/thorn", 0376);
		this.labelToCid.put("/three", 0063);
		this.labelToCid.put("/threequarters", 0276);
		this.labelToCid.put("/threesuperior", 0263);
		this.labelToCid.put("/tilde", 0224);
		this.labelToCid.put("/two", 0062);
		this.labelToCid.put("/twosuperior", 0262);
		this.labelToCid.put("/u", 0165);
		this.labelToCid.put("/uacute", 0372);
		this.labelToCid.put("/ucircumflex", 0373);
		this.labelToCid.put("/udieresis", 0374);
		this.labelToCid.put("/ugrave", 0371);
		this.labelToCid.put("/underscore", 0137);
		this.labelToCid.put("/v", 0166);
		this.labelToCid.put("/w", 0167);
		this.labelToCid.put("/x", 0170);
		this.labelToCid.put("/y", 0171);
		this.labelToCid.put("/yacute", 0375);
		this.labelToCid.put("/ydieresis", 0377);
		this.labelToCid.put("/yen", 0245);
		this.labelToCid.put("/z", 0172);
		this.labelToCid.put("/zero", 0060);
		transafertLTOCinCTIL();
	}

	/**
	 * Parse the font stream to feed cidToLabel and labelToMetric with Glyphs
	 * information.
	 * 
	 * @throws ValidationException
	 *           On unexpected error
	 */
	public void parse() throws ValidationException {
		readClearText();
		computeEexec();
	}

	/**
	 * Read eexecSize in the font stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected byte[] readEexec() throws IOException {
		int BUFFER_SIZE = 1024;
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream eexecPart = new ByteArrayOutputStream();
		int lr = 0;
		int total = 0;
		do {
			lr = this.font.read(buffer, 0, BUFFER_SIZE);
			if (lr == BUFFER_SIZE && (total + BUFFER_SIZE < eexecSize)) {
				eexecPart.write(buffer, 0, BUFFER_SIZE);
				total += BUFFER_SIZE;
			} else if (lr > 0 && (total + lr < eexecSize)) {
				eexecPart.write(buffer, 0, lr);
				total += lr;
			} else if (lr > 0 && (total + lr >= eexecSize)) {
				eexecPart.write(buffer, 0, eexecSize - total);
				total += (eexecSize - total);
			}
		} while (eexecSize > total && lr > 0);
		IOUtils.closeQuietly(eexecPart);
		return eexecPart.toByteArray();
	}

	/**
	 * This method read eexecSize bytes. Read bytes are decoded using the
	 * Type1FontUtils class and FontMetrics are computed.
	 * 
	 * @throws ValidationException
	 */
	protected void computeEexec() throws ValidationException {
		try {
			byte[] eexec = readEexec();
			byte[] decryptedEexec = decodeEexec(eexec);
//			// Uncomment this to see EExec as clear text 
//			System.out.println("DECODED EEXEC : ");
//			System.out.println(new String(decryptedEexec));

			parseDecodedEexec(decryptedEexec);
		} catch (IOException e) {
			throw new ValidationException("Unable to compute the eexec portion : "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Parse decoded eexec portion of the font program. Feeds the labelToMetric
	 * map.
	 * 
	 * @param eexec
	 * @throws IOException
	 */
	protected void parseDecodedEexec(byte[] eexec) throws IOException {
		ByteArrayInputStream baisEexec = new ByteArrayInputStream(eexec);
		boolean expectedCharString = false;
		// ---- According to Type1 specification 4 is the default value
		int lenIV = 4;
		int nChars = 0;
		for (;;) {

			byte[] token = readToken(baisEexec);

			switch (tokenIdentifier(token)) {
			case DUP_TOKEN:
				if (!expectedCharString) {
					byte[] tokenChoice = readToken(baisEexec); // ---- numeric code
					if (tokenIdentifier(tokenChoice) == CHARSTRINGS_TOKEN) {
						byte[] n = readToken(baisEexec);
						nChars = Integer.parseInt(new String(n, "US-ASCII"));
						expectedCharString = true;
						// ---- read the end of line "dict dup begin"
						readToken(baisEexec);
						readToken(baisEexec);
						readToken(baisEexec);
						break;
					} else if (!"begin".equals(new String(tokenChoice, "US-ASCII"))) {
						byte[] toskip = readToken(baisEexec); // ---- binary length
						readToken(baisEexec); // ---- skip RD
						int skip = Integer.parseInt(new String(toskip, "US-ASCII"));
						readBytes(baisEexec, skip);
					}
				}
				break;
			case LEN_IV_TOKEN:
				byte[] l = readToken(baisEexec);
				lenIV = Integer.parseInt(new String(l, "US-ASCII"));
				break;
			case CHARSTRINGS_TOKEN:
				byte[] n = readToken(baisEexec);
				nChars = Integer.parseInt(new String(n, "US-ASCII"));
				expectedCharString = true;
				// ---- read the end of line "dict dup begin"
				readToken(baisEexec);
				readToken(baisEexec);
				readToken(baisEexec);
				break;
			case OBJ_NAME_TOKEN:
				/*
				 * ---- OBJ_NAME_TOKEN : Some character's label aren't defined in the
				 * Encoding object but they should be defined by the Encoding array in
				 * the font ---- program.
				 */
				// break;
			case CHAR_LABEL_TOKEN:
				// case OBJ_NAME_TOKEN :
				if (expectedCharString) {
					String label = new String(token, "US-ASCII");
					byte[] csl = readToken(baisEexec);
					int length = Integer.parseInt(new String(csl, "US-ASCII"));
					// ---- read the RD token
					readToken(baisEexec);
					this.labelToMetric.put(label, getGlyphDescription(baisEexec, length,
							lenIV));
					nChars--;
					if (nChars == 0) {
						// ---- no more character
						return;
					}
				}
			default:
				// nothing to do
				break;
			}
		}
	}

	/**
	 * 
	 * @param is
	 * @param length
	 * @return
	 * @throws IOException
	 */
	protected byte[] readBytes(InputStream is, int length) throws IOException {
		byte[] charF = new byte[length];
		for (int i = 0; i < length; i++) {
			charF[i] = (byte) is.read();
		}
		return charF;
	}

	/**
	 * Read the CharString in the InputStream and decode it. The decoded
	 * CharString is used to create a GlyphDescription object.
	 * 
	 * @param is
	 *          the decoded eexec portion of the type 1 font program
	 * @param length
	 *          the number of bytes to read
	 * @param rdBytes
	 *          the number of padding bytes at the beginning of the decoded
	 *          CharString
	 * @return
	 * @throws IOException
	 */
	protected Type1GlyphDescription getGlyphDescription(InputStream is,
			int length, int rdBytes) throws IOException {
		byte[] charF = readBytes(is, length);
		byte[] dcs = Type1FontUtil.charstringDecrypt(charF, rdBytes);

		Type1CharStringParser t1p = new Type1CharStringParser();
		List<Object> lSequence = t1p.parse(dcs);

		return new Type1GlyphDescription(lSequence);
	}

	/**
	 * Call the Type1FontUtil.eexecDecrypt() method
	 * 
	 * @param eexec
	 * @return the decrypted eexec portion of the font program
	 */
	protected byte[] decodeEexec(byte[] eexec) {
		return Type1FontUtil.eexecDecrypt(eexec);
	}

	/**
	 * Read the clear-text portion of the Type1 font program.
	 * 
	 * If FamillyName, FullName and FontName exist in the font program,
	 * Type1MetricHelper updates its attributes. This method feeds the cidToLabel
	 * map.
	 * 
	 * @throws ValidationException
	 */
	protected void readClearText() throws ValidationException {
		int readBytes = 0;

		try {
			boolean dupAuth = false;
			while (clearTextSize - readBytes > 0) {

				byte[] token = readToken(this.font);
				switch (tokenIdentifier(token)) {
				case FAMILY_NAME_TOKEN:
					byte[] fname = readToken(this.font);
					readBytes += (fname.length + 1);

					this.familyName = new String(fname, "US-ASCII");
					break;
				case FULL_NAME_TOKEN:
					byte[] fullname = readToken(this.font);
					readBytes += (fullname.length + 1);

					this.fullName = new String(fullname, "US-ASCII");
					break;
				case DUP_TOKEN:
					if (dupAuth) {
						byte[] cid = readToken(this.font);
						readBytes += (cid.length + 1);

						byte[] label = readToken(this.font);
						readBytes += (label.length + 1);

						int cl = Integer.parseInt(new String(cid, "US-ASCII"));
						String lc = new String(label, "US-ASCII");
						this.cidToLabel.put(cl, lc);
						this.labelToCid.put(lc, cl);
					}
					break;
				case ENCODING_TOKEN:
					byte[] tmpTok = readToken(this.font);
					readBytes += (tmpTok.length + 1);
					String encoding = new String(tmpTok, "US-ASCII");
					if (PS_STANDARD_ENCODING.equals(encoding)) {
						createStandardEncoding();
					} else if (PS_ISOLATIN_ENCODING.equals(encoding)) {
						createISOLatin1Encoding();
					} else {
						dupAuth = true;
					}
					break;
				case READONLY_TOKEN:
					dupAuth = false;
					break;
				default:
					// nothing to go
				}

				// ---- add the token and the Space character length
				readBytes += (token.length + 1);
			}

		} catch (IOException e) {
			throw new ValidationException("Unable to read the clear text : "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns an int value which represent the token.
	 * 
	 * @param token
	 * @return -1 if the token must be ignored
	 * @throws IOException
	 */
	protected int tokenIdentifier(byte[] token) throws IOException {

		String tokenAsStr = new String(token, "US-ASCII");
		if ("/FamilyName".equals(tokenAsStr)) {
			return FAMILY_NAME_TOKEN;
		}

		if ("/FullName".equals(tokenAsStr)) {
			return FULL_NAME_TOKEN;
		}

		if ("/FontName".equals(tokenAsStr)) {
			return FONT_NAME_TOKEN;
		}

		if ("dup".equals(tokenAsStr)) {
			return DUP_TOKEN;
		}

		if ("/Encoding".equals(tokenAsStr)) {
			return ENCODING_TOKEN;
		}

		if ("readonly".equals(tokenAsStr)) {
			return READONLY_TOKEN;
		}

		if ("/lenIV".equals(tokenAsStr)) {
			return LEN_IV_TOKEN;
		}

		if ("/CharStrings".equals(tokenAsStr)) {
			return CHARSTRINGS_TOKEN;
		}

		if (labelToCid.containsKey(tokenAsStr)
				|| this.encoding.getNameToCodeMap().containsKey(
						COSName.getPDFName(tokenAsStr.replace("/", "")))) {
			return CHAR_LABEL_TOKEN;
		}

		String regex = "/[^\\s\\(\\)\\[\\]\\{\\}/<>%]+";
		if (tokenAsStr.matches(regex)) {
			return OBJ_NAME_TOKEN;
		}

		return -1;
	}

	/**
	 * Read the stream until a space character or EOL is reached.
	 * 
	 * @return byte array containing bytes read before the space character.
	 * @throws IOException
	 */
	protected byte[] readToken(InputStream stream) throws IOException {
		List<Integer> buffer = new ArrayList<Integer>();
		int currentByte = -1;
		do {
			currentByte = stream.read();
			// ---- Token is String literal
			if (currentByte > 0 && currentByte == '(') {
				int opened = 1;
				buffer.add(currentByte);

				while (opened != 0) {
					currentByte = stream.read();
					if (currentByte < 0) {
						throw new IOException("Unexpected End Of File");
					}

					if (currentByte == '(') {
						opened++;
					} else if (currentByte == ')') {
						opened--;
					}

					// ---- Add useful character
					buffer.add(currentByte);
				}
			} else if (currentByte > 0 && currentByte == '[') {
				// ---- token is an array
				int opened = 1;
				buffer.add(currentByte);

				while (opened != 0) {
					currentByte = stream.read();
					if (currentByte < 0) {
						throw new IOException("Unexpected End Of File");
					}

					if (currentByte == '[') {
						opened++;
					} else if (currentByte == ']') {
						opened--;
					}

					// ---- Add useful character
					buffer.add(currentByte);
				}
			} else if (currentByte > 0 && currentByte == '{') {
				// ---- token is an dictionary
				int opened = 1;
				buffer.add(currentByte);

				while (opened != 0) {
					currentByte = stream.read();
					if (currentByte < 0) {
						throw new IOException("Unexpected End Of File");
					}

					if (currentByte == '{') {
						opened++;
					} else if (currentByte == '}') {
						opened--;
					}

					// ---- Add useful character
					buffer.add(currentByte);
				}
			} else if (currentByte > 0
					&& (currentByte != ' ' && currentByte != '\n' && currentByte != '\r')) {
				// ---- Add useful character
				buffer.add(currentByte);
			} else if (currentByte < 0) {
				throw new IOException("Unexpected End Of File");
			} else {
				break;
			}
		} while (true);

		byte[] res = new byte[buffer.size()];
		for (int i = 0; i < res.length; ++i) {
			res[i] = buffer.get(i).byteValue();
		}

//		 System.out.println("### READ TOKEN : " + new String(res));
//		 if ("/CharStrings".equals(new String(res))) {
//		 System.err.println("POUET");
//		 }
		return res;
	}

	/**
	 * Returns the Character name as PDF Name Object. (Prefixed by '/'). If the
	 * name is missing from the cidToLabel map and missing from the encoding
	 * object, the "/.notdef" name is returned.
	 * The pdf encoding array is used before the cidToLabel map.
	 * @param cid
	 * @return
	 */
	protected String getLabelAsName(int cid) {
		String label = null;

		try {
			label = this.encoding.getName(cid);
		} catch (IOException e) {
			label = this.cidToLabel.get(cid);
		}

		if (label == null) {
			label = NOTDEF;
		}
		return label.charAt(0) == NAME_START ? label : NAME_START + label;
	}

	/**
	 * Return the Glyph width according to the Character identifier.
	 * 
	 * @param cid
	 * @return
	 */
	public int getWidth(int cid) {
		String label = getLabelAsName(cid);

		Type1GlyphDescription glyph = this.labelToMetric.get(label);
		if (glyph != null) {
			return glyph.getWidth();
		}

		return 0;
	}

	/**
	 * Container which contains all CharString Command and Operand. Currently,
	 * only the Glyph width can be access through the "hsdw" operator.
	 */
	public static class Type1GlyphDescription {
		private List<Object> lSequence = null;

		private Integer width = null;

		public Type1GlyphDescription(List<Object> ls) {
			this.lSequence = ls;
		}

		public int getWidth() {
			if (width != null) {
				return width;
			}

			for (int i = 0; lSequence != null && i < lSequence.size(); ++i) {
				Object obj = lSequence.get(i);
				if (obj instanceof CharStringCommand) {
					CharStringCommand csCmd = (CharStringCommand) obj;
					if ("hsbw".equals(CharStringCommand.TYPE1_VOCABULARY.get(csCmd
							.getKey()))) {
						width = (Integer) lSequence.get(i - 1);
						return width;
					}
				}
			}

			return 0;
		}
	}
}
