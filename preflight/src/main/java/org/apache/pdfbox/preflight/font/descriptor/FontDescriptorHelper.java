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
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
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
    protected PDFont font;

    protected PDFontDescriptorDictionary fontDescriptor;

    public FontDescriptorHelper(PreflightContext context, PDFont font, T fontContainer)
    {
        super();
        this.fContainer = fontContainer;
        this.context = context;
        this.font = font;
    }

    public void validate()
    {
        PDFontDescriptor fd = this.font.getFontDescriptor();
        // Only a PDFontDescriptorDictionary provides a way to embedded the font program.
        if (fd != null && fd instanceof PDFontDescriptorDictionary)
        {
            fontDescriptor = (PDFontDescriptorDictionary) fd;

            if (checkMandatoryFields(fontDescriptor.getCOSDictionary()))
            {
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
                                "FontFile entry is missing from FontDescriptor for " + fontDescriptor.getFontName()));
                        this.fContainer.notEmbedded();
                    }
                    else
                    {
                        this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                                "They are more than one FontFile for " + fontDescriptor.getFontName()));
                    }
                }
            }
        }
        else
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                    "FontDescriptor is null or is a AFM Descriptor"));
            this.fContainer.notEmbedded();
        }
    }

    protected boolean checkMandatoryFields(COSDictionary fDescriptor)
    {
        boolean areFieldsPresent = fDescriptor.containsKey(FONT_DICTIONARY_KEY_FONTNAME);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_FLAGS);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_ITALICANGLE);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_CAPHEIGHT);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_FONTBBOX);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_ASCENT);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_DESCENT);
        areFieldsPresent &= fDescriptor.containsKey(FONT_DICTIONARY_KEY_STEMV);
        areFieldsPresent &= fDescriptor.containsKey(COSName.FONT_NAME);
        if (!areFieldsPresent)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                    "Some mandatory fields are missing from the FontDescriptor"));
        }
        return areFieldsPresent;
    }

    public abstract PDStream extractFontFile(PDFontDescriptorDictionary fontDescriptor);

    /**
     * Return true if the FontDescriptor has only one FontFile entry.
     * 
     * @param fontDescriptor
     * @return
     */
    protected boolean hasOnlyOneFontFile(PDFontDescriptorDictionary fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff2 = fontDescriptor.getFontFile2();
        PDStream ff3 = fontDescriptor.getFontFile3();
        return (ff1 != null ^ ff2 != null ^ ff3 != null);
    }

    protected boolean fontFileNotEmbedded(PDFontDescriptorDictionary fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff2 = fontDescriptor.getFontFile2();
        PDStream ff3 = fontDescriptor.getFontFile3();
        return (ff1 == null && ff2 == null && ff3 == null);
    }

    protected abstract void processFontFile(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile);

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
        PDMetadata metadata = null;
        try
        {
            metadata = fontFile.getMetadata();

            if (metadata != null)
            {
                // Filters are forbidden in a XMP stream
                if (metadata.getFilters() != null && !metadata.getFilters().isEmpty())
                {
                    this.fContainer.push(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                            "Filter specified in font file metadata dictionnary"));
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
                        this.fContainer.push(new ValidationError(ERROR_METADATA_UNKNOWN_VALUETYPE, e.getMessage()));
                    }
                    else if (e.getErrorType() == ErrorType.XpacketBadEnd)
                    {
                        this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_XPACKET,
                                "Unable to parse font metadata due to : " + e.getMessage()));
                    }
                    else
                    {
                        this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT, e.getMessage()));
                    }
                }
            }
        }
        catch (IllegalStateException e)
        {
            this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_UNKOWN,
                    "The Metadata entry doesn't reference a stream object"));
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
                    "Unable to read font metadata due to : " + e.getMessage()));
        }
        finally
        {
            IOUtils.closeQuietly(metaDataContent);
            IOUtils.closeQuietly(bos);
        }
        return result;
    }
}
