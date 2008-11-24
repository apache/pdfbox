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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This is implementation of the Type1 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.11 $
 */
public class PDType1Font extends PDSimpleFont
{
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

    /**
     * Hardcoded copy of the Font.TYPE1_FONT constant in Java 5. PDFBox should
     * compile and work also with Java 1.4, so we can't rely on Java 5 features
     * being always available. The code that uses this constant will fail
     * gracefully if support for Type 1 fonts are not available.
     *
     * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-379">PDFBOX-379</a>
     */
    private static final int TYPE1_FONT = 1;

    private static final Map STANDARD_14 = new HashMap();
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
        font.setItem( COSName.SUBTYPE, COSName.getPDFName( "Type1" ) );
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
    public void drawString( String string, Graphics g, float fontSize, AffineTransform at, float x, float y ) throws IOException
    {
        if( awtFont == null )
        {
            String baseFont = this.getBaseFont();
            if( baseFont.equals( TIMES_ROMAN.getBaseFont() ) )
            {
                awtFont = new Font( "Times New Roman", Font.PLAIN, 1 );
            }
            else if( baseFont.equals( TIMES_ITALIC.getBaseFont() ) )
            {
                awtFont = new Font( "Times New Roman", Font.ITALIC, 1 );
            }
            else if( baseFont.equals( TIMES_BOLD.getBaseFont() ) )
            {
                awtFont = new Font( "Times New Roman", Font.BOLD, 1 );
            }
            else if( baseFont.equals( TIMES_BOLD_ITALIC.getBaseFont() ) )
            {
                awtFont = new Font( "Times New Roman", Font.BOLD | Font.ITALIC, 1 );
            }
            else if( baseFont.equals( HELVETICA.getBaseFont() ) )
            {
                awtFont = new Font( "Helvetica", Font.PLAIN, 1 );
            }
            else if( baseFont.equals( HELVETICA_BOLD.getBaseFont() ) )
            {
                awtFont = new Font( "Helvetica", Font.BOLD, 1 );
            }
            else if( baseFont.equals( HELVETICA_BOLD_OBLIQUE.getBaseFont() ) )
            {
                awtFont = new Font( "Helvetica", Font.BOLD | Font.ITALIC, 1 );
            }
            else if( baseFont.equals( HELVETICA_OBLIQUE.getBaseFont() ) )
            {
                awtFont = new Font( "Helvetica", Font.ITALIC, 1 );
            }
            else if( baseFont.equals( COURIER.getBaseFont() ) )
            {
                awtFont = new Font( "Courier", Font.PLAIN, 1 );
            }
            else if( baseFont.equals( COURIER_BOLD.getBaseFont() ) )
            {
                awtFont = new Font( "Courier", Font.BOLD, 1 );
            }
            else if( baseFont.equals( COURIER_BOLD_OBLIQUE.getBaseFont() ) )
            {
                awtFont = new Font( "Courier", Font.BOLD | Font.ITALIC, 1 );
            }
            else if( baseFont.equals( COURIER_OBLIQUE.getBaseFont() ) )
            {
                awtFont = new Font( "Courier", Font.ITALIC, 1 );
            }
            else if( baseFont.equals( SYMBOL.getBaseFont() ) )
            {
                awtFont = new Font( "Symbol", Font.PLAIN, 1 );
            }
            else if( baseFont.equals( ZAPF_DINGBATS.getBaseFont() ) )
            {
                awtFont = new Font( "ZapfDingbats", Font.PLAIN, 1 );
            }
            else
            {
                PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary)getFontDescriptor();
		if (fd != null){
			PDStream ffStream = fd.getFontFile();
			if( ffStream != null )
			{
			    try {
					awtFont = Font.createFont( TYPE1_FONT, ffStream.createInputStream() );
				} catch (FontFormatException e) {
					logger().info("substituting Arial because we couldn't read the embedded Font " + fd.getFontName() );
					awtFont = new Font( "Arial", Font.PLAIN, 1 );
				}
			}
			else {
				// TODO try to load external Font. see also PDTrueTypeFont
						logger().info("substituting Arial because the specified font isn't embedded " + fd.getFontName() );
				awtFont = new Font( "Arial", Font.PLAIN, 1 );
			}
		}
		else{
			logger().info("substituting Arial because we failed to get a FontDescriptor" );
			awtFont = new Font( "Arial", Font.PLAIN, 1 );
		}
            }
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2d.setFont( awtFont.deriveFont( at ).deriveFont( fontSize ) );

        g2d.drawString( string, x, y );
    }
}
