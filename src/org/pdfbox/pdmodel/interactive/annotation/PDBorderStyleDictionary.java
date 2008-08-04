/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.interactive.annotation;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSInteger;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.graphics.PDLineDashPattern;

/**
 * This class represents a PDF /BS entry the border style dictionary.
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public class PDBorderStyleDictionary implements COSObjectable
{

    /*
     * The various values of the style for the border as defined in the PDF 1.6
     * reference Table 8.13
     */

    /**
     * Constant for the name of a solid style.
     */
    public static final String STYLE_SOLID = "S";

    /**
     * Constant for the name of a dashed style.
     */
    public static final String STYLE_DASHED = "D";

    /**
     * Constant for the name of a beveled style.
     */
    public static final String STYLE_BEVELED = "B";

    /**
     * Constant for the name of a inset style.
     */
    public static final String STYLE_INSET = "I";

    /**
     * Constant for the name of a underline style.
     */
    public static final String STYLE_UNDERLINE = "U";

    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDBorderStyleDictionary()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     * 
     * @param dict
     *            a border style dictionary.
     */
    public PDBorderStyleDictionary( COSDictionary dict )
    {
        dictionary = dict;
    }

    /**
     * returns the dictionary.
     * 
     * @return the dictionary
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * returns the dictionary.
     * 
     * @return the dictionary
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will set the border width in points, 0 = no border.
     * 
     * @param w
     *            float the width in points
     */
    public void setWidth( float w )
    {
        getDictionary().setFloat( "W", w );
    }

    /**
     * This will retrieve the border width in points, 0 = no border.
     * 
     * @return flaot the width of the border in points
     */
    public float getWidth()
    {
        return getDictionary().getFloat( "W", 1 );
    }

    /**
     * This will set the border style, see the STYLE_* constants for valid values.
     * 
     * @param s
     *            the border style to use
     */
    public void setStyle( String s )
    {
        getDictionary().setName( "S", s );
    }

    /**
     * This will retrieve the border style, see the STYLE_* constants for valid
     * values.
     * 
     * @return the style of the border
     */
    public String getStyle()
    {
        return getDictionary().getNameAsString( "S", STYLE_SOLID );
    }

    /**
     * This will set the dash style used for drawing the border.
     * 
     * @param d
     *            the dash style to use
     */
    public void setDashStyle( PDLineDashPattern d )
    {
        COSArray array = null;
        if( d != null )
        {
            array = d.getCOSDashPattern();
        }
        getDictionary().setItem( "D", array );
    }

    /**
     * This will retrieve the dash style used for drawing the border.
     * 
     * @return the dash style of the border
     */
    public PDLineDashPattern getDashStyle()
    {
        COSArray d = (COSArray) getDictionary().getDictionaryObject( "D" );
        if (d == null)
        {
            d = new COSArray();
            d.add( new COSInteger( 3 ) );
            getDictionary().setItem( "D", d );
        }
        return new PDLineDashPattern( d, 0 );
    }

}