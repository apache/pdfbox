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

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This is implementation of the CIDFontType2 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDCIDFontType2Font extends PDCIDFont
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDCIDFontType2Font.class);

    /**
     * Constructor.
     */
    public PDCIDFontType2Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.CID_FONT_TYPE2 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType2Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }
    
    /**
     * {@inheritDoc}
     */
    public Font getawtFont() throws IOException
    {
        Font awtFont = null;
        PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary)getFontDescriptor();
        PDStream ff2Stream = fd.getFontFile2();
        if( ff2Stream != null )
        {
            try
            {
                // create a font with the embedded data
                awtFont = Font.createFont( Font.TRUETYPE_FONT, ff2Stream.createInputStream() );
            }
            catch( FontFormatException f )
            {
                log.info("Can't read the embedded font " + fd.getFontName() );
            }
            if (awtFont == null)
            {
                awtFont = FontManager.getAwtFont(fd.getFontName());
                if (awtFont != null)
                {
                    log.info("Using font "+awtFont.getName()+ " instead");
                }
            }
        }
        // TODO FontFile3
        return awtFont;
    }

}
