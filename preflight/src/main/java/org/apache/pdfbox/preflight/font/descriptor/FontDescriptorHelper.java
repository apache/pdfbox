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

package org.apache.pdfbox.preflight.font.descriptor;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DESCRIPTOR_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT_STREAM;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT_UNKOWN;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT_XPACKET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_UNKNOWN_VALUETYPE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_ASCENT;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CAPHEIGHT;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_DESCENT;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_FLAGS;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_FONTBBOX;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_FONTNAME;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_ITALICANGLE;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_STEMV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.util.FontMetaDataValidation;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;

public abstract class FontDescriptorHelper<T extends FontContainer>
{

    protected T fContainer;

    protected PreflightContext context;
    protected PDFontLike font;

    protected PDFontDescriptor fontDescriptor;

    public FontDescriptorHelper(PreflightContext context, PDFontLike font, T fontContainer)
    {
        super();
        this.fContainer = fontContainer;
        this.context = context;
        this.font = font;
    }

    public void validate()
    {
        PDFontDescriptor fd = this.font.getFontDescriptor();
        boolean isStandard14 = false;
        if (this.font instanceof PDFont)
        {
            isStandard14 = ((PDFont) font).isStandard14();
        }

        // Only a PDFontDescriptorDictionary provides a way to embedded the font program.
        if (fd != null)
        {
            fontDescriptor = fd;

            if (!isStandard14)
            {
                checkMandatoryFields(fontDescriptor.getCOSObject());
            }
            if (hasOnlyOneFontFile(fontDescriptor))
            {
                PDStream fontFile = extractFontFile(fontDescriptor);
                if (fontFile != null)
                {
                    processFontFile(fontDescriptor, fontFile);
                    checkFontFileMetaData(fontDescriptor, fontFile);
                }
            }
            else
            {
                if (fontFileNotEmbedded(fontDescriptor))
                {
                    this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                        fontDescriptor.getFontName() + ": FontFile entry is missing from FontDescriptor"));
                    this.fContainer.notEmbedded();
                }
                else
                {
                    this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                        fontDescriptor.getFontName() + ": They is more than one FontFile"));
                }
            }
        }
        else
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                    this.font.getName() + ": FontDescriptor is null or is an AFM Descriptor"));
            this.fContainer.notEmbedded();
        }
    }

    protected boolean checkMandatoryFields(COSDictionary fDescriptor)
    {
        String missingFields = "";
        boolean areFieldsPresent = fDescriptor.containsKey(FONT_DICTIONARY_KEY_FONTNAME);
        if (!areFieldsPresent)
        {
            missingFields += FONT_DICTIONARY_KEY_FONTNAME + ", ";
        }
        boolean flags = fDescriptor.containsKey(FONT_DICTIONARY_KEY_FLAGS);
        areFieldsPresent &= flags;
        if (!flags)
        {
            missingFields += FONT_DICTIONARY_KEY_FLAGS + ", ";
        }
        boolean italicAngle = fDescriptor.containsKey(FONT_DICTIONARY_KEY_ITALICANGLE);
        areFieldsPresent &= italicAngle;
        if (!italicAngle)
        {
            missingFields += FONT_DICTIONARY_KEY_ITALICANGLE + ", ";
        }
        boolean capHeight = fDescriptor.containsKey(FONT_DICTIONARY_KEY_CAPHEIGHT);
        areFieldsPresent &= capHeight;
        if (!capHeight)
        {
            missingFields += FONT_DICTIONARY_KEY_CAPHEIGHT + ", ";
        }
        boolean fontBox = fDescriptor.containsKey(FONT_DICTIONARY_KEY_FONTBBOX);
        areFieldsPresent &= fontBox;
        if (!fontBox)
        {
            missingFields += FONT_DICTIONARY_KEY_FONTBBOX + ", ";
        }
        boolean ascent = fDescriptor.containsKey(FONT_DICTIONARY_KEY_ASCENT);
        areFieldsPresent &= ascent;
        if (!ascent)
        {
            missingFields += FONT_DICTIONARY_KEY_ASCENT + ", ";
        }
        boolean descent = fDescriptor.containsKey(FONT_DICTIONARY_KEY_DESCENT);
        areFieldsPresent &= descent;
        if (!descent)
        {
            missingFields += FONT_DICTIONARY_KEY_DESCENT + ", ";
        }
        boolean stemV = fDescriptor.containsKey(FONT_DICTIONARY_KEY_STEMV);
        areFieldsPresent &= stemV;
        if (!stemV)
        {
            missingFields += FONT_DICTIONARY_KEY_STEMV + ", ";
        }
        boolean name = fDescriptor.containsKey(COSName.FONT_NAME);
        areFieldsPresent &= name;
        if (!name)
        {
            missingFields += COSName.FONT_NAME + ", ";
        }
        if (!areFieldsPresent)
        {
            if (missingFields.endsWith(", "))
            {
                missingFields = missingFields.substring(0, missingFields.length() - 2);
            }
            this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                    this.font.getName()
                    + ": some mandatory fields are missing from the FontDescriptor: " + missingFields + "."));
        }
        return areFieldsPresent;
    }

    public abstract PDStream extractFontFile(PDFontDescriptor fontDescriptor);

    /**
     * Return true if the FontDescriptor has only one FontFile entry.
     * 
     * @param fontDescriptor
     * @return true if the FontDescriptor has only one FontFile entry.
     */
    protected boolean hasOnlyOneFontFile(PDFontDescriptor fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff2 = fontDescriptor.getFontFile2();
        PDStream ff3 = fontDescriptor.getFontFile3();
        return (ff1 != null ^ ff2 != null ^ ff3 != null);
    }

    protected boolean fontFileNotEmbedded(PDFontDescriptor fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff2 = fontDescriptor.getFontFile2();
        PDStream ff3 = fontDescriptor.getFontFile3();
        return (ff1 == null && ff2 == null && ff3 == null);
    }

    protected abstract void processFontFile(PDFontDescriptor fontDescriptor, PDStream fontFile);

    /**
     * Type0, Type1 and TrueType FontValidator call this method to check the FontFile meta data.
     * 
     * @param fontDescriptor
     *            The FontDescriptor which contains the FontFile stream
     * @param fontFile
     *            The font file stream to check
     */
    protected void checkFontFileMetaData(PDFontDescriptor fontDescriptor, PDStream fontFile)
    {
        try
        {
            PDMetadata metadata = fontFile.getMetadata();

            if (metadata != null)
            {
                // Filters are forbidden in a XMP stream
                if (metadata.getFilters() != null && !metadata.getFilters().isEmpty())
                {
                    this.fContainer.push(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                            this.font.getName() + ": Filter specified in font file metadata dictionnary"));
                    return;
                }

                byte[] mdAsBytes = getMetaDataStreamAsBytes(metadata);

                try
                {

                    DomXmpParser xmpBuilder = new DomXmpParser();
                    XMPMetadata xmpMeta = xmpBuilder.parse(mdAsBytes);

                    FontMetaDataValidation fontMDval = new FontMetaDataValidation();
                    List<ValidationError> ve = new ArrayList<ValidationError>();
                    fontMDval.analyseFontName(xmpMeta, fontDescriptor, ve);
                    fontMDval.analyseRights(xmpMeta, fontDescriptor, ve);
                    this.fContainer.push(ve);

                }
                catch (XmpParsingException e)
                {
                    if (e.getErrorType() == ErrorType.NoValueType)
                    {
                        this.fContainer.push(new ValidationError(ERROR_METADATA_UNKNOWN_VALUETYPE, e.getMessage(), e));
                    }
                    else if (e.getErrorType() == ErrorType.XpacketBadEnd)
                    {
                        this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_XPACKET,
                                this.font.getName() + ": Unable to parse font metadata due to : " + e.getMessage(), e));
                    }
                    else
                    {
                        this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT, e.getMessage(), e));
                    }
                }
            }
        }
        catch (IllegalStateException e)
        {
            this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_UNKOWN,
                    this.font.getName() + ": The Metadata entry doesn't reference a stream object", e));
        }
    }

    protected final byte[] getMetaDataStreamAsBytes(PDMetadata metadata)
    {
        byte[] result = null;
        ByteArrayOutputStream bos = null;
        InputStream metaDataContent = null;
        try
        {
            bos = new ByteArrayOutputStream();
            metaDataContent = metadata.createInputStream();
            IOUtils.copyLarge(metaDataContent, bos);
            result = bos.toByteArray();
        }
        catch (IOException e)
        {
            this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_STREAM,
                    this.font.getName() + ": Unable to read font metadata due to : " + e.getMessage(), e));
        }
        finally
        {
            IOUtils.closeQuietly(metaDataContent);
            IOUtils.closeQuietly(bos);
        }
        return result;
    }
}
