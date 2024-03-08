/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.ResourceCache;

/**
 * Creates the appropriate font subtype based on information in the dictionary.
 * @author Ben Litchfield
 */
public final class PDFontFactory
{
    private static final Log LOG = LogFactory.getLog(PDFontFactory.class);

    private static final String FONT_TYPE1C = "Type1C";
    private static final String FONT_OPEN_TYPE = "OTTO";
    private static final String FONT_TTF_COLLECTION = "ttcf";
    private static final String FONT_TRUE_TYPE = "true";
    private static final byte[] TTF_HEADER = new byte[] { 0, 1, 0, 0 };

    private PDFontFactory()
    {
    }

    /**
     * Creates a new PDFont instance with the appropriate subclass.
     *
     * @param dictionary a font dictionary
     * @return a PDFont instance, based on the SubType entry of the dictionary
     * @throws IOException if something goes wrong
     */
    public static PDFont createFont(COSDictionary dictionary) throws IOException
    {
        return createFont(dictionary, null);
    }

    private static class FontType
    {
        private static final List<String> cidType0Types //
                = Arrays.asList(COSName.TYPE1.getName(), FONT_TYPE1C);
        private static final List<String> cidType2Types //
                = Arrays.asList(COSName.TRUE_TYPE.getName(), COSName.OPEN_TYPE.getName());
        private final COSName type;
        private final COSName subtype;

        public FontType(COSName type, String subtypeString)
        {
            this.type = type;
            if (cidType0Types.contains(subtypeString))
            {
                subtype = COSName.CID_FONT_TYPE0;
            }
            else if (cidType2Types.contains(subtypeString))
            {
                subtype = COSName.CID_FONT_TYPE2;
            }
            else
            {
                subtype = null;
            }
        }

        public FontType(COSName type, COSName subtype)
        {
            this.type = type;
            this.subtype = subtype;
        }

        public FontType(COSName type)
        {
            this(type, (COSName) null);
        }

        public COSName getSubtype()
        {
            return subtype;
        }

        public boolean isCIDSubtype(COSName cidSubtype)
        {
            if (!COSName.TYPE0.equals(type))
            {
                return false;
            }
            return subtype != null && subtype.equals(cidSubtype);
        }
    }

    /**
     * Creates a new PDFont instance with the appropriate subclass.
     *
     * @param dictionary a font dictionary
     * @param resourceCache resource cache, only useful for type 3 fonts, can be null
     * @return a PDFont instance, based on the SubType entry of the dictionary
     * @throws IOException if something goes wrong
     */
    public static PDFont createFont(COSDictionary dictionary, ResourceCache resourceCache) throws IOException
    {
        COSName type = dictionary.getCOSName(COSName.TYPE, COSName.FONT);
        if (!COSName.FONT.equals(type))
        {
            LOG.error("Expected 'Font' dictionary but found '" + type.getName() + "'");
        }

        COSName subType = dictionary.getCOSName(COSName.SUBTYPE);
        if (COSName.TYPE1.equals(subType))
        {
            COSDictionary fd = dictionary.getCOSDictionary(COSName.FONT_DESC);
            if (fd != null && fd.containsKey(COSName.FONT_FILE3))
            {
                return new PDType1CFont(dictionary);
            }
            return new PDType1Font(dictionary);
        }
        else if (COSName.MM_TYPE1.equals(subType))
        {
            COSDictionary fd = dictionary.getCOSDictionary(COSName.FONT_DESC);
            if (fd != null && fd.containsKey(COSName.FONT_FILE3))
            {
                return new PDType1CFont(dictionary);
            }
            return new PDMMType1Font(dictionary);
        }
        else if (COSName.TRUE_TYPE.equals(subType))
        {
            return new PDTrueTypeFont(dictionary);
        }
        else if (COSName.TYPE3.equals(subType))
        {
            return new PDType3Font(dictionary, resourceCache);
        }
        else if (COSName.TYPE0.equals(subType))
        {
            COSDictionary fontDescriptor = getFontDescriptor(dictionary);
            FontType fontTypeFromFont = getFontTypeFromFont(fontDescriptor, subType);
            if (fontTypeFromFont != null)
            {
                COSDictionary descendantFont = getDescendantFont(dictionary);
                COSName descFontType = descendantFont != null
                        ? descendantFont.getCOSName(COSName.SUBTYPE) : null;
                if (descFontType != null && !fontTypeFromFont.isCIDSubtype(descFontType))
                {
                    fixType0Subtype(descendantFont, fontDescriptor, fontTypeFromFont.getSubtype());
                }
            }
            return new PDType0Font(dictionary);
        }
        else if (COSName.CID_FONT_TYPE0.equals(subType))
        {
            throw new IOException("Type 0 descendant font not allowed");
        }
        else if (COSName.CID_FONT_TYPE2.equals(subType))
        {
            throw new IOException("Type 2 descendant font not allowed");
        }
        else
        {
            // assuming Type 1 font (see PDFBOX-1988) because it seems that Adobe Reader does this
            // however, we may need more sophisticated logic perhaps looking at the FontFile
            LOG.warn("Invalid font subtype '" + subType + "'");
            return new PDType1Font(dictionary);
        }
    }

    private static void fixType0Subtype(COSDictionary descendantFont, COSDictionary fontDescriptor,
            COSName newSubType)
    {
        LOG.warn("Try to fix different descendant font types for font "
                + fontDescriptor.getNameAsString(COSName.FONT_NAME));
        if (COSName.CID_FONT_TYPE0.equals(newSubType)
                && !fontDescriptor.containsKey(COSName.FONT_FILE3)
                && fontDescriptor.containsKey(COSName.FONT_FILE2))
        {
            fontDescriptor.setItem(COSName.FONT_FILE3, fontDescriptor.getItem(COSName.FONT_FILE2));
            fontDescriptor.removeItem(COSName.FONT_FILE2);
        }
        if (COSName.CID_FONT_TYPE2.equals(newSubType)
                && fontDescriptor.containsKey(COSName.FONT_FILE3)
                && !fontDescriptor.containsKey(COSName.FONT_FILE2))
        {
            fontDescriptor.setItem(COSName.FONT_FILE2, fontDescriptor.getItem(COSName.FONT_FILE3));
            fontDescriptor.removeItem(COSName.FONT_FILE3);
        }
        descendantFont.setItem(COSName.SUBTYPE, newSubType);
    }

    private static FontType getFontTypeFromFont(COSDictionary fontDescriptor, COSName fontType)
            throws IOException
    {
        byte[] fontHeader = getFontHeader(fontDescriptor);
        if (fontHeader == null)
        {
            return null;
        }
        boolean isComposite = COSName.TYPE0.equals(fontType);
        if (isTrueTypeFile(fontHeader) || isTrueTypeCollectionFile(fontHeader))
        {
            return isComposite //
                    ? new FontType(COSName.TYPE0, COSName.TRUE_TYPE.getName())
                    : new FontType(COSName.TRUE_TYPE);
        }
        if (isOpenTypeFile(fontHeader))
        {
            return isComposite //
                    ? new FontType(COSName.TYPE0, COSName.OPEN_TYPE.getName())
                    : new FontType(COSName.OPEN_TYPE);
        }
        if (isType1File(fontHeader) || isPfbFile(fontHeader))
        {
            if (isComposite)
            {
                return new FontType(COSName.TYPE0, COSName.TYPE1.getName());
            }
            return fontType.equals(COSName.MM_TYPE1)
                    ? new FontType(COSName.MM_TYPE1, COSName.TYPE1.getName())
                    : new FontType(COSName.TYPE1);
        }
        // CFF fonts have a more or less variable header so that the check should be done
        // after all others to avoid wrong classifications
        if (isCFFFile(fontHeader))
        {
            if (isComposite)
            {
                return new FontType(COSName.TYPE0, FONT_TYPE1C);
            }
            return fontType.equals(COSName.MM_TYPE1) //
                    ? new FontType(COSName.MM_TYPE1, FONT_TYPE1C)
                    : new FontType(COSName.TYPE1, FONT_TYPE1C);
        }
        return null;
    }

    private static boolean isTrueTypeFile(byte[] header)
    {
        return Arrays.equals(TTF_HEADER, header)
                || FONT_TRUE_TYPE.equals(new String(header, StandardCharsets.US_ASCII));
    }

    private static boolean isTrueTypeCollectionFile(byte[] header)
    {
        return FONT_TTF_COLLECTION.equals(new String(header, StandardCharsets.US_ASCII));
    }

    private static boolean isOpenTypeFile(byte[] header)
    {
        return FONT_OPEN_TYPE.equals(new String(header, StandardCharsets.US_ASCII));
    }

    private static boolean isType1File(byte[] header)
    {
        // All Type1 font programs must begin with the comment '%!' (0x25 + 0x21).
        return header[0] == 0x25 && header[1] == 0x21;
    }

    private static boolean isPfbFile(byte[] header)
    {
        // all PFB fonts start with 0x80 followed by either 0x01 or 0x02
        return header[0] == 0x80 && (header[1] == 0x01 || header[1] == 0x02);
    }

    private static boolean isCFFFile(byte[] header)
    {
        // the header consist of 4 values
        // major version, minor version, header size, offset size
        // the major version must be >= 1 and the offset size >= 1 and <= 4
        return header[0] >= 1 && header[3] >= 1 && header[3] <= 4;
    }

    private static COSDictionary getFontDescriptor(COSDictionary dictionary)
    {
        COSDictionary fontDescriptor = dictionary.getCOSDictionary(COSName.FONT_DESC);
        if (fontDescriptor == null)
        {
            COSDictionary descendantFont = getDescendantFont(dictionary);
            if (descendantFont != null)
            {
                fontDescriptor = descendantFont.getCOSDictionary(COSName.FONT_DESC);
            }
        }
        return fontDescriptor;
    }

    private static COSDictionary getDescendantFont(COSDictionary dictionary)
    {
        COSArray descendantFonts = dictionary.getCOSArray(COSName.DESCENDANT_FONTS);
        if (descendantFonts != null && descendantFonts.size() > 0)
        {
            COSBase descendantFontDictBase = descendantFonts.getObject(0);
            if (descendantFontDictBase instanceof COSDictionary)
            {
                return (COSDictionary) descendantFontDictBase;
            }
        }
        return null;
    }

    private static byte[] getFontHeader(COSDictionary fontDescriptor) throws IOException
    {
        if (fontDescriptor == null)
        {
            return null;
        }
        COSStream fontFile = fontDescriptor.getCOSStream(COSName.FONT_FILE);
        if (fontFile == null)
        {
            fontFile = fontDescriptor.getCOSStream(COSName.FONT_FILE2);
        }
        if (fontFile == null)
        {
            fontFile = fontDescriptor.getCOSStream(COSName.FONT_FILE3);
        }
        byte[] header = null;
        if (fontFile != null)
        {
            try (RandomAccessRead fontView = fontFile.createView())
            {
                int headerLength = 4;
                header = new byte[headerLength];
                int remainingBytes = headerLength;
                int amountRead;
                while ((amountRead = fontView.read(header, headerLength - remainingBytes,
                        remainingBytes)) > 0)
                {
                    remainingBytes -= amountRead;
                }
            }
            catch (IOException ex)
            {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return header;
    }

    /**
     * Creates a new PDCIDFont instance with the appropriate subclass.
     *
     * @param dictionary descendant font dictionary
     * @return a PDCIDFont instance, based on the SubType entry of the dictionary
     * @throws IOException if something goes wrong
     */
    static PDCIDFont createDescendantFont(COSDictionary dictionary, PDType0Font parent)
            throws IOException
    {
        COSName type = dictionary.getCOSName(COSName.TYPE, COSName.FONT);
        if (!COSName.FONT.equals(type))
        {
            throw new IOException("Expected 'Font' dictionary but found '" + type.getName() + "'");
        }

        COSName subType = dictionary.getCOSName(COSName.SUBTYPE);
        if (COSName.CID_FONT_TYPE0.equals(subType))
        {
            return new PDCIDFontType0(dictionary, parent);
        }
        else if (COSName.CID_FONT_TYPE2.equals(subType))
        {
            return new PDCIDFontType2(dictionary, parent);
        }
        else
        {
            throw new IOException("Invalid font type: " + type);
        }
    }
}
