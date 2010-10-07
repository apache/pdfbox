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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.WinAnsiEncoding;

/**
 * This is implementation of the Type1 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.11 $
 */
public class PDType1Font extends PDSimpleFont
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDType1Font.class);

    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ROMAN = new PDType1Font( "Times-Roman" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD = new PDType1Font( "Times-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ITALIC = new PDType1Font( "Times-Italic" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD_ITALIC = new PDType1Font( "Times-BoldItalic" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA = new PDType1Font( "Helvetica" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD = new PDType1Font( "Helvetica-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font( "Helvetica-Oblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font( "Helvetica-BoldOblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER = new PDType1Font( "Courier" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD = new PDType1Font( "Courier-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_OBLIQUE = new PDType1Font( "Courier-Oblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD_OBLIQUE = new PDType1Font( "Courier-BoldOblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font SYMBOL = new PDType1Font( "Symbol" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font ZAPF_DINGBATS = new PDType1Font( "ZapfDingbats" );

    private static final Map<String, PDType1Font> STANDARD_14 = new HashMap<String, PDType1Font>();
    static
    {
        STANDARD_14.put( TIMES_ROMAN.getBaseFont(), TIMES_ROMAN );
        STANDARD_14.put( TIMES_BOLD.getBaseFont(), TIMES_BOLD );
        STANDARD_14.put( TIMES_ITALIC.getBaseFont(), TIMES_ITALIC );
        STANDARD_14.put( TIMES_BOLD_ITALIC.getBaseFont(), TIMES_BOLD_ITALIC );
        STANDARD_14.put( HELVETICA.getBaseFont(), HELVETICA );
        STANDARD_14.put( HELVETICA_BOLD.getBaseFont(), HELVETICA_BOLD );
        STANDARD_14.put( HELVETICA_OBLIQUE.getBaseFont(), HELVETICA_OBLIQUE );
        STANDARD_14.put( HELVETICA_BOLD_OBLIQUE.getBaseFont(), HELVETICA_BOLD_OBLIQUE );
        STANDARD_14.put( COURIER.getBaseFont(), COURIER );
        STANDARD_14.put( COURIER_BOLD.getBaseFont(), COURIER_BOLD );
        STANDARD_14.put( COURIER_OBLIQUE.getBaseFont(), COURIER_OBLIQUE );
        STANDARD_14.put( COURIER_BOLD_OBLIQUE.getBaseFont(), COURIER_BOLD_OBLIQUE );
        STANDARD_14.put( SYMBOL.getBaseFont(), SYMBOL );
        STANDARD_14.put( ZAPF_DINGBATS.getBaseFont(), ZAPF_DINGBATS );
    }

    private Font awtFont = null;

    /**
     * Constructor.
     */
    public PDType1Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TYPE1 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType1Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * Constructor.
     *
     * @param baseFont The base font for this font.
     */
    public PDType1Font( String baseFont )
    {
        this();
        setBaseFont( baseFont );
        setEncoding(new WinAnsiEncoding());
    }

    /**
     * A convenience method to get one of the standard 14 font from name.
     *
     * @param name The name of the font to get.
     *
     * @return The font that matches the name or null if it does not exist.
     */
    public static PDType1Font getStandardFont( String name )
    {
        return (PDType1Font)STANDARD_14.get( name );
    }

    /**
     * This will get the names of the standard 14 fonts.
     *
     * @return An array of the names of the standard 14 fonts.
     */
    public static String[] getStandard14Names()
    {
        return (String[])STANDARD_14.keySet().toArray( new String[14] );
    }

    /**
     * {@inheritDoc}
     */
    public Font getawtFont() throws IOException
    {
        if( awtFont == null )
        {
            String baseFont = getBaseFont();
            PDFontDescriptor fd = getFontDescriptor();
            if (fd != null && fd instanceof PDFontDescriptorDictionary)
            {
                PDFontDescriptorDictionary fdDictionary = (PDFontDescriptorDictionary)fd;
                if( fdDictionary.getFontFile() != null )
                {
                    try 
                    {
                        // create a type1 font with the embedded data
                        awtFont = Font.createFont( Font.TYPE1_FONT, fdDictionary.getFontFile().createInputStream() );
                    } 
                    catch (FontFormatException e) 
                    {
                        log.info("Can't read the embedded type1 font " + fd.getFontName() );
                    }
                }
                else if ( fdDictionary.getFontFile2() != null)
                {
                    try 
                    {
                        // create a true type font with the embedded data
                        awtFont = Font.createFont( Font.TRUETYPE_FONT, fdDictionary.getFontFile2().createInputStream() );
                    } 
                    catch (FontFormatException e) 
                    {
                        log.info("Can't read the embedded true type font " + fd.getFontName() );
                    }
                }
                else if( fdDictionary.getFontFile3() != null)
                {
                    PDType1CFont type1CFont = new PDType1CFont( super.font );
                    awtFont = type1CFont.getawtFont();
                }
                
                if (awtFont == null)
                {
                    // check if the font is part of our environment
                    awtFont = FontManager.getAwtFont(fd.getFontName());
                    if (awtFont == null)
                    {
                        log.info("Can't find the specified font " + fd.getFontName() );
                    }
                }
            }
            else
            {
                // check if the font is part of our environment
                awtFont = FontManager.getAwtFont(baseFont);
                if (awtFont == null) 
                {
                    log.info("Can't find the specified basefont " + baseFont );
                }
            }
            if (awtFont == null)
            {
                // we can't find anything, so we have to use the standard font
                awtFont = FontManager.getStandardFont();
                log.info("Using font "+awtFont.getName()+ " instead");
            }
        }
        return awtFont;
    }
    
}
