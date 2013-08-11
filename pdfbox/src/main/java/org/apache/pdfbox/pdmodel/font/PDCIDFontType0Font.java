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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This is implementation of the CIDFontType0 Font.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDCIDFontType0Font extends PDCIDFont
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDCIDFontType0Font.class);

    private PDType1CFont type1CFont = null;

    /**
     * Constructor.
     */
    public PDCIDFontType0Font()
    {
        super();
        font.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE0);
    }

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType0Font(COSDictionary fontDictionary)
    {
        super(fontDictionary);
        PDFontDescriptor fd = getFontDescriptor();
        if (fd instanceof PDFontDescriptorDictionary)
        {
            PDFontDescriptorDictionary fdd = (PDFontDescriptorDictionary) fd;
            if (fdd.getFontFile3() != null)
            {
                try
                {
                    type1CFont = new PDType1CFont(font);
                }
                catch (IOException exception)
                {
                    LOG.error("Can't create the embedded CFF-font", exception);
                }
            }
        }
    }

    /**
     * Returns the embedded Type1CFont.
     * 
     * @return the Type1C font
     */
    public PDType1CFont getType1CFont()
    {
        return type1CFont;
    }
}
