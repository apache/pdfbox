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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_TYPE1_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_LENGTH2;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_LENGTH3;
import static org.apache.pdfbox.preflight.font.FontValidator.isSubSet;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.Type1Container;
import org.apache.pdfbox.preflight.font.util.Type1;
import org.apache.pdfbox.preflight.font.util.Type1Parser;

public class Type1DescriptorHelper extends FontDescriptorHelper<Type1Container>
{
    private boolean isFontFile1 = true;

    public Type1DescriptorHelper(PreflightContext context, PDFont font, Type1Container fontContainer)
    {
        super(context, font, fontContainer);
    }

    protected boolean checkMandatoryFields(COSDictionary fDescriptor)
    {
        boolean result = super.checkMandatoryFields(fDescriptor);
        /*
         * if the this font is a Subset, the CharSet entry must be present in the FontDescriptor
         */
        if (isSubSet(fontDescriptor.getFontName()))
        {
            String charsetStr = fontDescriptor.getCharSet();
            if (charsetStr == null || "".equals(charsetStr))
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET,
                        "The Charset entry is missing for the Type1 Subset"));
                result = false;
            }
        }
        return result;
    }

    @Override
    public PDStream extractFontFile(PDFontDescriptorDictionary fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff3 = fontDescriptor.getFontFile3();

        if (ff1 != null)
        {
            COSStream stream = ff1.getStream();
            if (stream == null)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is missing for "
                        + fontDescriptor.getFontName()));
                this.fContainer.notEmbedded();
                return null;
            }

            boolean hasLength1 = stream.getInt(COSName.LENGTH1) > 0;
            boolean hasLength2 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH2)) > 0;
            boolean hasLength3 = stream.getInt(COSName.getPDFName(FONT_DICTIONARY_KEY_LENGTH3)) >= 0;
            if (!(hasLength1 && hasLength2 && hasLength3))
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is invalid for "
                        + fontDescriptor.getFontName()));
                return null;
            }

            return ff1;
        }
        else
        {
            this.isFontFile1 = false;
            this.fContainer.setFontFile1(isFontFile1);
            return ff3;
        }
    }

    @Override
    protected void processFontFile(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile)
    {
        if (isFontFile1)
        {
            processFontFile1(fontDescriptor, fontFile);
        }
        else
        {
            processFontFile3(fontDescriptor, fontFile);
        }
    }

    /**
     * Try to load the font using the java.awt.font object. if the font is invalid, an exception will be pushed in the
     * font container
     * 
     * @param fontDescriptor
     * @param fontFile
     */
    protected void processFontFile1(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile)
    {
        ByteArrayInputStream bis = null;
        try
        {
            bis = new ByteArrayInputStream(fontFile.getByteArray());
            Font.createFont(Font.TYPE1_FONT, bis);
            IOUtils.closeQuietly(bis);

            // Parse the Type1 Font program in order to extract Glyph Width
            COSStream streamObj = fontFile.getStream();
            int length1 = streamObj.getInt(COSName.LENGTH1);
            int length2 = streamObj.getInt(COSName.LENGTH2);
            bis = new ByteArrayInputStream(fontFile.getByteArray());
            Type1Parser parserForMetrics = Type1Parser.createParserWithEncodingObject(bis, length1, length2,
                    font.getFontEncoding());
            Type1 parsedData = parserForMetrics.parse();

            this.fContainer.setType1Font(parsedData);

        }
        catch (IOException e)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_TYPE1_DAMAGED, "The FontFile can't be read"));
        }
        catch (FontFormatException e)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_TYPE1_DAMAGED, "The FontFile is damaged"));
        }
        finally
        {
            IOUtils.closeQuietly(bis);
        }
    }

    /**
     * Type1C is a CFF font format, extract all CFFFont object from the stream
     * 
     * @param fontStream
     * @return
     * @throws ValidationException
     */
    protected void processFontFile3(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile)
    {
        try
        {
            CFFParser cffParser = new CFFParser();
            List<CFFFont> lCFonts = cffParser.parse(fontFile.getByteArray());
            if (lCFonts == null || lCFonts.isEmpty())
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
            }
            this.fContainer.setCFFFontObjects(lCFonts);
        }
        catch (IOException e)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
        }
    }
}
