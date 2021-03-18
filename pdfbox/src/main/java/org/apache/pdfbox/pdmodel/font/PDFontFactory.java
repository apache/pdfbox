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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
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
            return new PDType0Font(dictionary);
        }
        else if (COSName.CID_FONT_TYPE0.equals(subType))
        {
            throw new IllegalArgumentException("Type 0 descendant font not allowed");
        }
        else if (COSName.CID_FONT_TYPE2.equals(subType))
        {
            throw new IllegalArgumentException("Type 2 descendant font not allowed");
        }
        else
        {
            // assuming Type 1 font (see PDFBOX-1988) because it seems that Adobe Reader does this
            // however, we may need more sophisticated logic perhaps looking at the FontFile
            LOG.warn("Invalid font subtype '" + subType + "'");
            return new PDType1Font(dictionary);
        }
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
            throw new IllegalArgumentException("Expected 'Font' dictionary but found '" + type.getName() + "'");
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
