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
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT_UNKNOWN;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_FORMAT_XPACKET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_UNKNOWN_VALUETYPE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
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
    
    private static final Set<String> MANDATORYFIELDS;
    
    static 
    {
        MANDATORYFIELDS = new HashSet<>();
        MANDATORYFIELDS.add(COSName.FLAGS.getName());
        MANDATORYFIELDS.add(COSName.ITALIC_ANGLE.getName());
        MANDATORYFIELDS.add(COSName.CAP_HEIGHT.getName());
        MANDATORYFIELDS.add(COSName.FONT_BBOX.getName());
        MANDATORYFIELDS.add(COSName.ASCENT.getName());
        MANDATORYFIELDS.add(COSName.DESCENT.getName());
        MANDATORYFIELDS.add(COSName.STEM_V.getName());
        MANDATORYFIELDS.add(COSName.FONT_NAME.getName());
        MANDATORYFIELDS.add(COSName.TYPE.getName());
    }

    public FontDescriptorHelper(final PreflightContext context, final PDFontLike font, final T fontContainer)
    {
        super();
        this.fContainer = fontContainer;
        this.context = context;
        this.font = font;
    }

    public void validate()
    {
        final PDFontDescriptor fd = this.font.getFontDescriptor();
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
                final PDStream fontFile = extractFontFile(fontDescriptor);
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

    protected boolean checkMandatoryFields(final COSDictionary fDescriptor)
    {
        boolean result = true;
        final StringBuilder missingFields = new StringBuilder();
        for (final String field : MANDATORYFIELDS)
        {
            if (!fDescriptor.containsKey(field))
            {
                if (missingFields.length() > 1)
                {
                    missingFields.append(", ");
                }
                missingFields.append(field);
            }
        }        
        if (fDescriptor.containsKey(COSName.TYPE))
        {
            final COSBase type = fDescriptor.getItem(COSName.TYPE);
            if (!COSName.FONT_DESC.equals(type))
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                        this.font.getName()
                        + ": /Type in FontDescriptor must be /FontDescriptor, but is " + type));
                result = false;
            }
        }
        if (missingFields.length() > 0)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_DESCRIPTOR_INVALID,
                    this.font.getName()
                    + ": some mandatory fields are missing from the FontDescriptor: " + missingFields + "."));
            result = false;
        }
        return result;
    }

    public abstract PDStream extractFontFile(PDFontDescriptor fontDescriptor);

    /**
     * Return true if the FontDescriptor has only one FontFile entry.
     * 
     * @param fontDescriptor
     * @return true if the FontDescriptor has only one FontFile entry.
     */
    protected boolean hasOnlyOneFontFile(final PDFontDescriptor fontDescriptor)
    {
        final PDStream ff1 = fontDescriptor.getFontFile();
        final PDStream ff2 = fontDescriptor.getFontFile2();
        final PDStream ff3 = fontDescriptor.getFontFile3();
        return (ff1 != null ^ ff2 != null ^ ff3 != null);
    }

    protected boolean fontFileNotEmbedded(final PDFontDescriptor fontDescriptor)
    {
        final PDStream ff1 = fontDescriptor.getFontFile();
        final PDStream ff2 = fontDescriptor.getFontFile2();
        final PDStream ff3 = fontDescriptor.getFontFile3();
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
    protected void checkFontFileMetaData(final PDFontDescriptor fontDescriptor, final PDStream fontFile)
    {
        try
        {
            final PDMetadata metadata = fontFile.getMetadata();

            if (metadata != null)
            {
                // Filters are forbidden in a XMP stream
                if (!metadata.getFilters().isEmpty())
                {
                    this.fContainer.push(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                            this.font.getName() + ": Filter specified in font file metadata dictionary"));
                    return;
                }

                final byte[] mdAsBytes = getMetaDataStreamAsBytes(metadata);

                try
                {

                    final DomXmpParser xmpBuilder = new DomXmpParser();
                    final XMPMetadata xmpMeta = xmpBuilder.parse(mdAsBytes);

                    final FontMetaDataValidation fontMDval = new FontMetaDataValidation();
                    final List<ValidationError> ve = new ArrayList<>();
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
            this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_UNKNOWN,
                    this.font.getName() + ": The Metadata entry doesn't reference a stream object", e));
        }
    }

    protected final byte[] getMetaDataStreamAsBytes(final PDMetadata metadata)
    {
        try (InputStream metaDataContent = metadata.createInputStream())
        {
            return IOUtils.toByteArray(metaDataContent);
        }
        catch (IOException e)
        {
            this.fContainer.push(new ValidationError(ERROR_METADATA_FORMAT_STREAM,
                    this.font.getName() + ": Unable to read font metadata due to : " + e.getMessage(), e));
            return null;
        }
    }

    public static boolean isSubSet(final String fontName)
    {
        return fontName != null && fontName.matches("^[A-Z]{6}\\+.*");
    }

}
