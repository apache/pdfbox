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

package org.apache.pdfbox.preflight.font.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.preflight.PreflightConstants;

public class Type1
{
    /**
     * This map links the character identifier to a internal font program label which is different from the standard
     * Encoding
     */
    private Map<Integer, String> cidToLabel = new HashMap<Integer, String>(0);
    /**
     * This map links the character label to a character identifier which is different from the standard Encoding.
     */
    private Map<String, Integer> labelToCid = new HashMap<String, Integer>(0);
    /**
     * This map link the character label to a container containing Glyph description.
     */
    private Map<String, GlyphDescription> labelToMetric = new HashMap<String, GlyphDescription>(0);

    /**
     * The character encoding of the Font
     */
    private Encoding encoding = null;

    public Type1(Encoding encoding)
    {
        this.encoding = encoding;
    }

    void addCidWithLabel(Integer cid, String label)
    {
        this.labelToCid.put(label, cid);
        this.cidToLabel.put(cid, label);
    }

    void addGlyphDescription(String glyphLabel, GlyphDescription description)
    {
        this.labelToMetric.put(glyphLabel, description);
    }

    void initEncodingWithStandardEncoding()
    {
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

    private void transafertLTOCinCTIL()
    {
        for (Entry<String, Integer> entry : this.labelToCid.entrySet())
        {
            this.cidToLabel.put(entry.getValue(), entry.getKey());
        }
    }

    void initEncodingWithISOLatin1Encoding()
    {
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

    public int getWidthOfCID(int cid) throws GlyphException
    {
        String label = getLabelAsName(cid);

        GlyphDescription glyph = this.labelToMetric.get(label);
        if (glyph != null)
        {
            return glyph.getGlyphWidth();
        }

        throw new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, cid, "Missing glyph for the CID " + cid);
    }

    private String getLabelAsName(int cid)
    {
        String label = null;

        try
        {
            label = this.encoding.getName(cid);
        }
        catch (IOException e)
        {
            label = this.cidToLabel.get(cid);
            if (label == null)
            {
                label = Type1Parser.NOTDEF;
            }
        }

        return label.charAt(0) == Type1Parser.NAME_START ? label : Type1Parser.NAME_START + label;
    }
}
