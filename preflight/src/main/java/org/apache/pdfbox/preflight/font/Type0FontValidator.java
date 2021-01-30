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
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_CMAP_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DICTIONARY_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_DEFAULT_CMAP_WMODE;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V;

import java.io.IOException;
import java.io.InputStream;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.container.Type0Container;

public class Type0FontValidator extends FontValidator<Type0Container>
{
    protected final PDFont font;

    public Type0FontValidator(PreflightContext context, PDFont font)
    {
        super(context, font.getCOSObject(), new Type0Container(font));
        this.font = font;
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
        COSDictionary fontDictionary = font.getCOSObject();
        boolean areFieldsPResent = fontDictionary.containsKey(COSName.TYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.SUBTYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.BASE_FONT);
        areFieldsPResent &= fontDictionary.containsKey(COSName.DESCENDANT_FONTS);
        areFieldsPResent &= fontDictionary.containsKey(COSName.ENCODING);

        if (!areFieldsPResent)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    font.getName() + ": Some keys are missing from composite font dictionary"));
        }
    }

    /**
     * Extract the single CIDFont from the descendant array. Create a FontValidator for this CIDFont
     * and launch its validation.
     *
     * @throws org.apache.pdfbox.preflight.exception.ValidationException if there is an error
     * validating the CIDFont.
     */
    protected void processDescendantFont() throws ValidationException
    {
        COSDictionary fontDictionary = font.getCOSObject();
        // a CIDFont is contained in the DescendantFonts array
        COSArray array = fontDictionary.getCOSArray(COSName.DESCENDANT_FONTS);
        if (array == null || array.size() != 1)
        {
            /*
             * in PDF 1.4, this array must contain only one element, because of a PDF/A should be a PDF 1.4, this method
             * returns an error if the array has more than one element.
             */
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                    font.getName() + ": CIDFont is missing from the DescendantFonts array or the size of array is greater than 1"));
            return;
        }

        COSDictionary cidFont = (COSDictionary) array.getObject(0);
        if (cidFont == null)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                    font.getName() + ": The DescendantFonts array should have one element with is a dictionary."));
            return;
        }

        FontValidator<? extends FontContainer<? extends PDCIDFont>> cidFontValidator = createDescendantValidator(cidFont);
        if (cidFontValidator != null)
        {
            this.fontContainer.setDelegateFontContainer(cidFontValidator.getFontContainer());
            cidFontValidator.validate();
        }
    }

    protected FontValidator<? extends FontContainer<? extends PDCIDFont>> createDescendantValidator(COSDictionary cidFont)
    {
        COSName subtype = cidFont.getCOSName(COSName.SUBTYPE);
        FontValidator<? extends FontContainer<? extends PDCIDFont>> cidFontValidator = null;
        if (COSName.CID_FONT_TYPE0.equals(subtype))
        {
            cidFontValidator = createCIDType0FontValidator(cidFont);
        }
        else if (COSName.CID_FONT_TYPE2.equals(subtype))
        {
            cidFontValidator = createCIDType2FontValidator(cidFont);
        }
        else
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    font.getName() + ": Type and/or Subtype keys are missing"));
        }
        return cidFontValidator;
    }

    /**
     * Create the validation object for CIDType0 Font
     */
    protected FontValidator<? extends FontContainer<PDCIDFontType0>> createCIDType0FontValidator(COSDictionary fDict)
    {
        try
        {
            return new CIDType0FontValidator(context, new PDCIDFontType0(fDict, (PDType0Font)font));
        }
        catch (IOException e)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, 
                    font.getName() + ": The CIDType0 font is damaged", e));
            return null;
        }
    }

    /**
     * Create the validation object for CIDType2 Font
     *
     * @param fDict a CIDType2 font dictionary.
     * @return a CIDType2 tont font validator.
     */
    protected FontValidator<? extends FontContainer<PDCIDFontType2>> createCIDType2FontValidator(COSDictionary fDict)
    {
        try
        {
            return new CIDType2FontValidator(context, new PDCIDFontType2(fDict, (PDType0Font)font));
        }
        catch (IOException e)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, 
                    font.getName() + ": The CIDType2 font is damaged", e));
            return null;
        }
    }

    /**
     * Check the CMap entry.
     * 
     * The CMap entry must be a dictionary in a PDF/A. This entry can be a String only if the String value is Identity-H
     * or Identity-V
     */
    @Override
    protected void checkEncoding()
    {
        COSBase encoding = (font.getCOSObject()).getDictionaryObject(COSName.ENCODING);
        checkCMapEncoding(encoding);
    }

    protected void checkCMapEncoding(COSBase encoding)
    {
        if (encoding instanceof COSName || encoding instanceof COSString)
        {
            // if encoding is a string, only 2 values are allowed
            String str = encoding instanceof COSName ? ((COSName) encoding).getName()
                    : ((COSString) encoding).getString();
            if (!(FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V.equals(str) || FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H
                    .equals(str)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_INVALID,
                        font.getName() + ": The CMap is a string but it isn't an Identity-H/V"));
            }
        }
        else if (encoding instanceof COSStream)
        {
            /*
             * If the CMap is a stream, some fields are mandatory and the CIDSytemInfo must be compared with the
             * CIDSystemInfo entry of the CIDFont.
             */
            processCMapAsStream((COSStream) encoding);
        }
        else
        {
            // CMap type is invalid
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                    font.getName() + ": The CMap type is invalid"));
        }
    }

    /**
     *
     *
     * This method checks mandatory fields of the CMap stream. This method also checks if the CMap
     * stream is damaged using the CMapParser of the fontbox api. The standard information of a
     * stream element will be checked by the StreamValidationProcess.
     *
     * @param aCMap the cmap stream.
     */
    private void processCMapAsStream(COSStream aCMap)
    {
        checkCIDSystemInfo(aCMap.getCOSDictionary(COSName.CIDSYSTEMINFO));

        try (InputStream cmapStream = aCMap.createInputStream())
        {
            // extract information from the CMap stream using strict mode
            CMap fontboxCMap = new CMapParser(true).parse(cmapStream);
            int wmValue = fontboxCMap.getWMode();
            String cmnValue = fontboxCMap.getName();

            /*
             * According to the getInt javadoc, -1 is returned if there is no result. In the PDF Reference v1.7 p449,
             * we can read that the default value is 0.
             */
            int wmode = aCMap.getInt(COSName.WMODE, FONT_DICTIONARY_DEFAULT_CMAP_WMODE);
            COSName type = aCMap.getCOSName(COSName.TYPE);
            String cmapName = aCMap.getNameAsString(COSName.CMAPNAME);

            if (cmapName == null || "".equals(cmapName) || wmode > 1)
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        font.getName() + ": Some elements in the CMap dictionary are missing or invalid"));
            }
            else if (!(wmValue == wmode && cmapName.equals(cmnValue)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        font.getName() + ": CMapName or WMode is inconsistent"));
            }
            else if (!COSName.CMAP.equals(type))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING,
                        font.getName() + ": The CMap type is invalid"));
            }
        }
        catch (IOException e)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CID_CMAP_DAMAGED, font.getName() + ": The CMap type is damaged", e));
        }

        COSDictionary cmapUsed = (COSDictionary) aCMap.getDictionaryObject(COSName.USE_CMAP);
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
     * @param cidSysInfo
     * @return the validation result.
     */
    protected boolean checkCIDSystemInfo(COSDictionary cidSysInfo)
    {
        boolean result = true;
        if (cidSysInfo != null)
        {
            String reg = cidSysInfo.getString(COSName.REGISTRY);
            String ord = cidSysInfo.getString(COSName.ORDERING);
            COSBase sup = cidSysInfo.getDictionaryObject(COSName.SUPPLEMENT);

            if (!(reg != null && ord != null && sup instanceof COSInteger))
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
     */
    private void compareCIDSystemInfo(COSDictionary cmap)
    {
        COSDictionary fontDictionary = font.getCOSObject();
        COSArray array = fontDictionary.getCOSArray(COSName.DESCENDANT_FONTS);

        if (array != null && array.size() > 0)
        {
            COSDictionary cidFont = (COSDictionary) array.getObject(0);
            COSDictionary cmsi = cmap.getCOSDictionary(COSName.CIDSYSTEMINFO);
            COSDictionary cfsi = cidFont.getCOSDictionary(COSName.CIDSYSTEMINFO);

            String regCM = cmsi.getString(COSName.REGISTRY);
            String ordCM = cmsi.getString(COSName.ORDERING);
            String regCF = cfsi.getString(COSName.REGISTRY);
            String ordCF = cfsi.getString(COSName.ORDERING);

            if (!regCF.equals(regCM) || !ordCF.equals(ordCM))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_SYSINFO,
                        font.getName() + ": The CIDSystemInfo is inconsistent"));
            }
        }
    }
}
