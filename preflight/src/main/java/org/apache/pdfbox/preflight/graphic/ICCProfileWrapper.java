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

package org.apache.pdfbox.preflight.graphic;

import static org.apache.pdfbox.preflight.PreflightConstants.DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.*;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;

/**
 * This class embeds an instance of java.awt.color.ICC_Profile which represent the ICCProfile defined by the
 * DestOutputItents key of the OutputIntents of the PDF.
 */
public class ICCProfileWrapper
{
    /**
     * The ICCProfile extracted from DestOutputItents
     */
    private final ICC_Profile profile;

    /**
     * The ICC ColorSpace created using the ICCProfile
     */
    private final ICC_ColorSpace colorSpace;

    public ICCProfileWrapper(final ICC_Profile _profile)
    {
        this.profile = _profile;
        this.colorSpace = new ICC_ColorSpace(_profile);
    }

    /**
     * Call the ICC_ColorSpace.getType method and return the value.
     * 
     * @return
     */
    public int getColorSpaceType()
    {
        return colorSpace.getType();
    }

    /**
     * @return the profile
     */
    public ICC_Profile getProfile()
    {
        return profile;
    }

    /**
     * Return true if the ColourSpace is RGB
     * 
     * @return
     */
    public boolean isRGBColorSpace()
    {
        return ICC_ColorSpace.TYPE_RGB == colorSpace.getType();
    }

    /**
     * Return true if the ColourSpace is CMYK
     * 
     * @return
     */
    public boolean isCMYKColorSpace()
    {
        return ICC_ColorSpace.TYPE_CMYK == colorSpace.getType();
    }

    /**
     * Return true if the ColourSpace is Gray scale
     * 
     * @return
     */
    public boolean isGrayColorSpace()
    {
        return ICC_ColorSpace.TYPE_GRAY == colorSpace.getType();
    }

    /**
     * This method read all outputIntent dictionary until on of them have a destOutputProfile stream. This stream is
     * parsed and is used to create a IccProfileWrapper.
     * 
     * @param context
     * @return an instance of ICCProfileWrapper or null if there are no DestOutputProfile
     * @throws ValidationException
     *             if an IOException occurs during the DestOutputProfile parsing
     */
    private static ICCProfileWrapper searchFirstICCProfile(PreflightContext context) throws ValidationException
    {
        PreflightDocument document = context.getDocument();
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSBase cBase = catalog.getCOSDictionary().getItem(COSName.getPDFName(DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS));
        COSArray outputIntents = COSUtils.getAsArray(cBase, document.getDocument());

        for (int i = 0; outputIntents != null && i < outputIntents.size(); ++i)
        {
            COSDictionary outputIntentDict = COSUtils.getAsDictionary(outputIntents.get(i), document.getDocument());
            COSBase destOutputProfile = outputIntentDict.getItem(OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE);
            if (destOutputProfile != null)
            {
                try
                {
                    PDStream stream = PDStream.createFromCOS(COSUtils.getAsStream(destOutputProfile,
                            document.getDocument()));
                    if (stream != null)
                    {
                        ICC_Profile iccp = ICC_Profile.getInstance(stream.getByteArray());
                        return new ICCProfileWrapper(iccp);
                    }
                }
                catch (IllegalArgumentException e)
                {
                    context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID,
                            "DestOutputProfile isn't a valid ICCProfile. Caused by : " + e.getMessage()));
                }
                catch (IOException e)
                {            
                    context.addValidationError(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID,
                        "Unable to parse the ICCProfile. Caused by : " + e.getMessage()));
                }
            }
        }
        return null;
    }

    public static ICCProfileWrapper getOrSearchICCProfile(PreflightContext context) throws ValidationException
    {
        ICCProfileWrapper profileWrapper = context.getIccProfileWrapper();
        if (profileWrapper == null && !context.isIccProfileAlreadySearched())
        {
            profileWrapper = searchFirstICCProfile(context);
            context.setIccProfileAlreadySearched(true);
        }
        return profileWrapper;
    }
}
