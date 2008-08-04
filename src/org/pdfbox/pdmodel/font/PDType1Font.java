/**
 * Copyright (c) 2004-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.font;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

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
    public void drawString( String string, Graphics g, float fontSize, 
        float xScale, float yScale, float x, float y ) throws IOException
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
                awtFont = new Font( "Arial", Font.PLAIN, 1 );
                //throw new IOException( "Not yet implemented:" + getClass().getName() + " " +  
                //this.getBaseFont() + 
                //" "  + this + " " + TIMES_ROMAN );
            }
        }
        AffineTransform at = new AffineTransform();
        at.scale( xScale, yScale );
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2d.setFont( awtFont.deriveFont( at ).deriveFont( fontSize ) );
        //g2d.getFontRenderContext().getTransform().scale( xScale, yScale );
        
        g2d.drawString( string, (int)x, (int)y );
    }
}