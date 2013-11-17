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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDKEYED_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDKEYED_SYSINFO;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_CMAP_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DICTIONARY_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_DEFAULT_CMAP_WMODE;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CMAP_NAME;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CMAP_USECMAP;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CMAP_WMODE;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_TYPE0;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_TYPE2;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_TYPE_CMAP;

import java.io.IOException;

import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0Font;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.container.Type0Container;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class Type0FontValidator extends FontValidator<Type0Container>
{

    protected COSDocument cosDocument = null;

    public Type0FontValidator(PreflightContext context, PDFont font)
    {
        super(context, font, new Type0Container(font));
        cosDocument = this.context.getDocument().getDocument();
    }

    @Override
    public void validate() throws ValidationException
    {
        checkMandatoryFields();

        processDescendantFont();

        checkEncoding();
        checkToUnicode();
    }

    /**
     * This methods extracts from the Font dictionary all mandatory fields. If a mandatory field is missing, the list of
     * ValidationError in the FontContainer is updated.
     */
    protected void checkMandatoryFields()
    {
        COSDictionary fontDictionary = (COSDictionary) font.getCOSObject();
        boolean areFieldsPResent = fontDictionary.containsKey(COSName.TYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.SUBTYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.BASE_FONT);
        areFieldsPResent &= fontDictionary.containsKey(COSName.DESCENDANT_FONTS);
        areFieldsPResent &= fontDictionary.containsKey(COSName.ENCODING);

        if (!areFieldsPResent)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "Some keys are missing from composite font dictionary"));
        }
    }

    /**
     * Extract the single CIDFont from the Descendant array. Create a FontValidator for this CODFont and launch its
     * validation.
     */
    protected void processDescendantFont() throws ValidationException
    {
        COSDictionary fontDictionary = (COSDictionary) font.getCOSObject();
        // a CIDFont is contained in the DescendantFonts array
        COSArray array = COSUtils.getAsArray(fontDictionary.getItem(COSName.DESCENDANT_FONTS), cosDocument);
        if (array == null || array.size() != 1)
        {
            /*
             * in PDF 1.4, this array must contain only one element, because of a PDF/A should be a PDF 1.4, this method
             * returns an error if the array has more than one element.
             */
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                    "CIDFont is missing from the DescendantFonts array or the size of array is greater than 1"));
            return;
        }

        COSDictionary cidFont = COSUtils.getAsDictionary(array.get(0), cosDocument);
        if (cidFont == null)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                    "The DescendantFonts array should have one element with is a dictionary."));
            return;
        }

        FontValidator<? extends FontContainer> cidFontValidator = createDescendantValidator(cidFont);
        if (cidFontValidator != null)
        {
            this.fontContainer.setDelegateFontContainer(cidFontValidator.getFontContainer());
            cidFontValidator.validate();
        }
    }

    protected FontValidator<? extends FontContainer> createDescendantValidator(COSDictionary cidFont)
    {
        String subtype = cidFont.getNameAsString(COSName.SUBTYPE);
        FontValidator<? extends FontContainer> cidFontValidator = null;
        if (FONT_DICTIONARY_VALUE_TYPE0.equals(subtype))
        {
            cidFontValidator = createCIDType0FontValidator(cidFont);
        }
        else if (FONT_DICTIONARY_VALUE_TYPE2.equals(subtype))
        {
            cidFontValidator = createCIDType2FontValidator(cidFont);
        }
        else
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "Type and/or Subtype keys are missing"));
        }
        return cidFontValidator;
    }

    /**
     * Create the validation object for CIDType0 Font
     * 
     * @return
     */
    protected FontValidator<? extends FontContainer> createCIDType0FontValidator(COSDictionary fDict)
    {
        return new CIDType0FontValidator(context, new PDCIDFontType0Font(fDict));
    }

    /**
     * Create the validation object for CIDType2 Font
     * 
     * @return
     */
    protected FontValidator<? extends FontContainer> createCIDType2FontValidator(COSDictionary fDict)
    {
        return new CIDType2FontValidator(context, new PDCIDFontType2Font(fDict));
    }

    /**
     * Check the CMap entry.
     * 
     * The CMap entry must be a dictionary in a PDF/A. This entry can be a String only if the String value is Identity-H
     * or Identity-V
     * 
     * @param encoding
     */
    protected void checkEncoding()
    {
        COSBase encoding = ((COSDictionary) font.getCOSObject()).getItem(COSName.ENCODING);
        checkCMapEncoding(encoding);
    }

    protected void checkCMapEncoding(COSBase encoding)
    {
        if (COSUtils.isString(encoding, cosDocument))
        {
            // if encoding is a string, only 2 values are allowed
            String str = COSUtils.getAsString(encoding, cosDocument);
            if (!(FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V.equals(str) || FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H
                    .equals(str)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                        "The CMap is a string but it isn't an Identity-H/V"));
                return;
            }
        }
        else if (COSUtils.isStream(encoding, cosDocument))
        {
            /*
             * If the CMap is a stream, some fields are mandatory and the CIDSytemInfo must be compared with the
             * CIDSystemInfo entry of the CIDFont.
             */
            processCMapAsStream(COSUtils.getAsStream(encoding, cosDocument));
        }
        else
        {
            // CMap type is invalid
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                    "The CMap type is invalid"));
        }
    }

    /**
     * Standard information of a stream element will be checked by the StreamValidationProcess.
     * 
     * This method checks mandatory fields of the CMap stream. This method checks too if the CMap stream is damaged
     * using the CMapParser of the fontbox api.
     * 
     * @param aCMap
     * @return
     */
    private void processCMapAsStream(COSStream aCMap)
    {

        COSBase sysinfo = aCMap.getItem(COSName.CIDSYSTEMINFO);
        checkCIDSystemInfo(sysinfo);

        try
        {
            // extract information from the CMap stream
            CMap fontboxCMap = new CMapParser().parse(null, aCMap.getUnfilteredStream());
            int wmValue = fontboxCMap.getWMode();
            String cmnValue = fontboxCMap.getName();

            /*
             * According to the getInt javadoc, -1 is returned if there are no result. In the PDF Reference v1.7 p449,
             * we can read that Default value is 0.
             */
            int wmode = aCMap.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_CMAP_WMODE),
                    FONT_DICTIONARY_DEFAULT_CMAP_WMODE);
            String type = aCMap.getNameAsString(COSName.TYPE);
            String cmapName = aCMap.getNameAsString(COSName.getPDFName(FONT_DICTIONARY_KEY_CMAP_NAME));

            if (cmapName == null || "".equals(cmapName) || wmode > 1)
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        "Some elements in the CMap dictionary are missing or invalid"));
            }
            else if (!(wmValue == wmode && cmapName.equals(cmnValue)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        "CMapName or WMode is inconsistent"));
            }
            else if (!FONT_DICTIONARY_VALUE_TYPE_CMAP.equals(type))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        "The CMap type is invalid"));
            }
        }
        catch (IOException e)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CID_CMAP_DAMAGED, "The CMap type is damaged"));
        }

        COSDictionary cmapUsed = (COSDictionary) aCMap.getDictionaryObject(COSName
                .getPDFName(FONT_DICTIONARY_KEY_CMAP_USECMAP));
        if (cmapUsed != null)
        {
            checkCMapEncoding(cmapUsed);
        }
        compareCIDSystemInfo(aCMap);
    }

    /**
     * Check the content of the CIDSystemInfo dictionary. A CIDSystemInfo dictionary must contain :
     * <UL>
     * <li>a Name - Registry
     * <li>a Name - Ordering
     * <li>a Integer - Supplement
     * </UL>
     * 
     * @param sysinfo
     * @return
     */
    protected boolean checkCIDSystemInfo(COSBase sysinfo)
    {
        boolean result = true;
        COSDictionary cidSysInfo = COSUtils.getAsDictionary(sysinfo, cosDocument);

        if (cidSysInfo != null)
        {
            COSBase reg = cidSysInfo.getItem(COSName.REGISTRY);
            COSBase ord = cidSysInfo.getItem(COSName.ORDERING);
            COSBase sup = cidSysInfo.getItem(COSName.SUPPLEMENT);

            if (!(COSUtils.isString(reg, cosDocument) && COSUtils.isString(ord, cosDocument) && COSUtils.isInteger(sup,
                    cosDocument)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_SYSINFO));
                result = false;
            }

        }
        else
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_SYSINFO));
            result = false;
        }
        return result;
    }

    /**
     * The CIDSystemInfo must have the same Registry and Ordering for CMap and CIDFont. This control is useless if CMap
     * is Identity-H or Identity-V so this method is called by the checkCMap method.
     * 
     * @param cmap
     * @return
     */
    private void compareCIDSystemInfo(COSDictionary cmap)
    {
        COSDictionary fontDictionary = (COSDictionary) font.getCOSObject();
        COSArray array = COSUtils.getAsArray(fontDictionary.getItem(COSName.DESCENDANT_FONTS), cosDocument);

        if (array != null && array.size() > 0)
        {
            COSDictionary cidFont = COSUtils.getAsDictionary(array.get(0), cosDocument);
            COSDictionary cmsi = COSUtils.getAsDictionary(cmap.getItem(COSName.CIDSYSTEMINFO), cosDocument);
            COSDictionary cfsi = COSUtils.getAsDictionary(cidFont.getItem(COSName.CIDSYSTEMINFO), cosDocument);

            String regCM = COSUtils.getAsString(cmsi.getItem(COSName.REGISTRY), cosDocument);
            String ordCM = COSUtils.getAsString(cmsi.getItem(COSName.ORDERING), cosDocument);
            String regCF = COSUtils.getAsString(cfsi.getItem(COSName.REGISTRY), cosDocument);
            String ordCF = COSUtils.getAsString(cfsi.getItem(COSName.ORDERING), cosDocument);

            if (!regCF.equals(regCM) || !ordCF.equals(ordCM))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_SYSINFO,
                        "The CIDSystemInfo is inconsistent"));
            }
        }
    }
}
