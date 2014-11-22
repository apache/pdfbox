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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDKEYED_CIDTOGID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDKEYED_SYSINFO;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DICTIONARY_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_CMAP_IDENTITY;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.utils.COSUtils;

public abstract class DescendantFontValidator<T extends FontContainer> extends SimpleFontValidator<T>
{
    protected COSDocument cosDocument = null;

    public DescendantFontValidator(PreflightContext context, PDCIDFont font, T fContainer)
    {
        super(context, font, font.getCOSObject(), fContainer);
        cosDocument = context.getDocument().getDocument();
    }

    @Override
    protected void checkMandatoryField()
    {
        boolean arePresent = fontDictionary.containsKey(COSName.TYPE);
        arePresent &= fontDictionary.containsKey(COSName.SUBTYPE);
        arePresent &= fontDictionary.containsKey(COSName.BASE_FONT);
        arePresent &= fontDictionary.containsKey(COSName.CIDSYSTEMINFO);
        arePresent &= fontDictionary.containsKey(COSName.FONT_DESC);

        if (!arePresent)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID, "Required keys are missing"));
        }

        checkCIDSystemInfo(fontDictionary.getItem(COSName.CIDSYSTEMINFO));
        checkCIDToGIDMap(fontDictionary.getItem(COSName.CID_TO_GID_MAP));
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
     */
    protected void checkCIDSystemInfo(COSBase sysinfo)
    {
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
            }
        }
        else
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_SYSINFO));
        }
    }

    /**
     * This method checks the CIDtoGIDMap entry of the Font dictionary. call the
     * {@linkplain #checkCIDToGIDMap(COSBase, boolean)} with right parameters
     * according to the instance of DescendantFontValidator
     *
     * @param ctog
     */
    protected abstract void checkCIDToGIDMap(COSBase ctog);

    /**
     * This method checks the CIDtoGIDMap entry of the Font dictionary. This
     * element must be a Stream or a Name. If it is a name, it must be
     * "Identity" otherwise, the PDF file isn't a PDF/A-1b.
     *
     * If the validation fails the list of errors in the FontContainer is
     * updated.
     *
     * If the CIDtoGIDMap is a Stream, it is parsed as a CMap and the result is
     * returned.
     *
     * @param ctog
     * @param mandatory true for CIDType2 , false for CIDType0
     */
    protected void checkCIDToGIDMap(COSBase ctog, boolean mandatory)
    {
        if (COSUtils.isString(ctog, cosDocument))
        {
            // ---- valid only if the string is Identity
            String ctogStr = COSUtils.getAsString(ctog, cosDocument);
            if (!FONT_DICTIONARY_VALUE_CMAP_IDENTITY.equals(ctogStr))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CIDTOGID,
                        font.getName() + ": The CIDToGID entry is invalid"));
            }
        }
        else if (COSUtils.isStream(ctog, cosDocument))
        {
            try
            {
                COSStream stream = COSUtils.getAsStream(ctog, cosDocument);

                // todo: check the map's content? (won't pdfbox do this?)
                InputStream is = stream.getUnfilteredStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] map = os.toByteArray();
            }
            catch (IOException e)
            {
                // map can be invalid, return a Validation Error
                this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CIDTOGID, 
                        font.getName() + ": error getting CIDToGIDMap", e));
            }
        }
        else if (mandatory)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_CIDKEYED_CIDTOGID, 
                    font.getName() + ": mandatory CIDToGIDMap missing"));
        }
    }
}
