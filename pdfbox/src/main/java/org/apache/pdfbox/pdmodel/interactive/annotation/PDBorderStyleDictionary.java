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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSInteger;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;

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
            d.add( COSInteger.THREE );
            getDictionary().setItem( "D", d );
        }
        return new PDLineDashPattern( d, 0 );
    }

}
