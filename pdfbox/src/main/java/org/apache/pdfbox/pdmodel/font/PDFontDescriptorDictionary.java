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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents an implementation to the font descriptor that gets its
 * information from a COS Dictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDFontDescriptorDictionary extends PDFontDescriptor implements COSObjectable
{
    private COSDictionary dic;
    private float xHeight = Float.NEGATIVE_INFINITY;
    private float capHeight = Float.NEGATIVE_INFINITY;
    private int flags = -1;

    /**
     * Constructor.
     */
    public PDFontDescriptorDictionary()
    {
        dic = new COSDictionary();
        dic.setItem( COSName.TYPE, COSName.FONT_DESC );
    }

    /**
     * Constructor.
     *
     * @param desc The wrapped COS Dictionary.
     */
    public PDFontDescriptorDictionary( COSDictionary desc )
    {
        dic = desc;
    }

    /**
     * This will get the dictionary for this object.
     *
     * @return The COS dictionary.
     */
    public COSDictionary getCOSDictionary()
    {
        return dic;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return dic;
    }

    /**
     * Get the font name.
     *
     * @return The name of the font.
     */
    public String getFontName()
    {
        String retval = null;
        COSName name = (COSName)dic.getDictionaryObject( COSName.FONT_NAME );
        if( name != null )
        {
            retval = name.getName();
        }
        return retval;
    }

    /**
     * This will set the font name.
     *
     * @param fontName The new name for the font.
     */
    public void setFontName( String fontName )
    {
        COSName name = null;
        if( fontName != null )
        {
            name = COSName.getPDFName( fontName );
        }
        dic.setItem( COSName.FONT_NAME, name );
    }

    /**
     * A string representing the preferred font family.
     *
     * @return The font family.
     */
    public String getFontFamily()
    {
        String retval = null;
        COSString name = (COSString)dic.getDictionaryObject( COSName.FONT_FAMILY );
        if( name != null )
        {
            retval = name.getString();
        }
        return retval;
    }

    /**
     * This will set the font family.
     *
     * @param fontFamily The font family.
     */
    public void setFontFamily( String fontFamily )
    {
        COSString name = null;
        if( fontFamily != null )
        {
            name = new COSString( fontFamily );
        }
        dic.setItem( COSName.FONT_FAMILY, name );
    }

    /**
     * The weight of the font.  According to the PDF spec "possible values are
     * 100, 200, 300, 400, 500, 600, 700, 800 or 900"  Where a higher number is
     * more weight and appears to be more bold.
     *
     * @return The font weight.
     */
    public float getFontWeight()
    {
        return dic.getFloat( COSName.FONT_WEIGHT,0 );
    }

    /**
     * Set the weight of the font.
     *
     * @param fontWeight The new weight of the font.
     */
    public void setFontWeight( float fontWeight )
    {
        dic.setFloat( COSName.FONT_WEIGHT, fontWeight );
    }

    /**
     * A string representing the preferred font stretch.
     * According to the PDF Spec:
     * The font stretch value; it must be one of the following (ordered from
     * narrowest to widest): UltraCondensed, ExtraCondensed, Condensed, SemiCondensed,
     * Normal, SemiExpanded, Expanded, ExtraExpanded or UltraExpanded.
     *
     * @return The stretch of the font.
     */
    public String getFontStretch()
    {
        String retval = null;
        COSName name = (COSName)dic.getDictionaryObject( COSName.FONT_STRETCH );
        if( name != null )
        {
            retval = name.getName();
        }
        return retval;
    }

    /**
     * This will set the font stretch.
     *
     * @param fontStretch The new stretch for the font.
     */
    public void setFontStretch( String fontStretch )
    {
        COSName name = null;
        if( fontStretch != null )
        {
            name = COSName.getPDFName( fontStretch );
        }
        dic.setItem( COSName.FONT_STRETCH, name );
    }

    /**
     * This will get the font flags.
     *
     * @return The font flags.
     */
    public int getFlags()
    {
        if (flags == -1)
        {
            flags = dic.getInt( COSName.FLAGS, 0 );
        }
        return flags;
    }

    /**
     * This will set the font flags.
     *
     * @param flags The new font flags.
     */
    public void setFlags( int flags )
    {
        dic.setInt( COSName.FLAGS, flags );
        this.flags = flags;
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     */
    public PDRectangle getFontBoundingBox()
    {
        COSArray rect = (COSArray)dic.getDictionaryObject( COSName.FONT_BBOX );
        PDRectangle retval = null;
        if( rect != null )
        {
            retval = new PDRectangle( rect );
        }
        return retval;
    }

    /**
     * Set the fonts bounding box.
     *
     * @param rect The new bouding box.
     */
    public void setFontBoundingBox( PDRectangle rect )
    {
        COSArray array = null;
        if( rect != null )
        {
            array = rect.getCOSArray();
        }
        dic.setItem( COSName.FONT_BBOX, array );
    }

    /**
     * This will get the italic angle for the font.
     *
     * @return The italic angle.
     */
    public float getItalicAngle()
    {
        return dic.getFloat( COSName.ITALIC_ANGLE, 0 );
    }

    /**
     * This will set the italic angle for the font.
     *
     * @param angle The new italic angle for the font.
     */
    public void setItalicAngle( float angle )
    {
        dic.setFloat( COSName.ITALIC_ANGLE, angle );
    }

    /**
     * This will get the ascent for the font.
     *
     * @return The ascent.
     */
    public float getAscent()
    {
        return dic.getFloat( COSName.ASCENT, 0 );
    }

    /**
     * This will set the ascent for the font.
     *
     * @param ascent The new ascent for the font.
     */
    public void setAscent( float ascent )
    {
        dic.setFloat( COSName.ASCENT, ascent );
    }

    /**
     * This will get the descent for the font.
     *
     * @return The descent.
     */
    public float getDescent()
    {
        return dic.getFloat( COSName.DESCENT, 0 );
    }

    /**
     * This will set the descent for the font.
     *
     * @param descent The new descent for the font.
     */
    public void setDescent( float descent )
    {
        dic.setFloat( COSName.DESCENT, descent );
    }

    /**
     * This will get the leading for the font.
     *
     * @return The leading.
     */
    public float getLeading()
    {
        return dic.getFloat( COSName.LEADING, 0 );
    }

    /**
     * This will set the leading for the font.
     *
     * @param leading The new leading for the font.
     */
    public void setLeading( float leading )
    {
        dic.setFloat( COSName.LEADING, leading );
    }

    /**
     * This will get the CapHeight for the font.
     *
     * @return The cap height.
     */
    public float getCapHeight()
    {
        if(capHeight==Float.NEGATIVE_INFINITY)
        {
            /* We observed a negative value being returned with
             * the Scheherazade font. PDFBOX-429 was logged for this.
             * We are not sure if returning the absolute value
             * is the correct fix, but it seems to work.  */
            capHeight = java.lang.Math.abs(dic.getFloat( COSName.CAP_HEIGHT, 0 ));
        }
        return capHeight;
    }


    /**
     * This will set the cap height for the font.
     *
     * @param capHeight The new cap height for the font.
     */
    public void setCapHeight( float capHeight )
    {
        dic.setFloat( COSName.CAP_HEIGHT, capHeight );
        this.capHeight = capHeight;
    }

    /**
     * This will get the x height for the font.
     *
     * @return The x height.
     */
    public float getXHeight()
    {
        if(xHeight==Float.NEGATIVE_INFINITY)
        {
            /* We observed a negative value being returned with
             * the Scheherazade font. PDFBOX-429 was logged for this.
             * We are not sure if returning the absolute value
             * is the correct fix, but it seems to work.  */
            xHeight = java.lang.Math.abs(dic.getFloat( COSName.XHEIGHT, 0 ));
        }
        return xHeight;
    }

    /**
     * This will set the x height for the font.
     *
     * @param xHeight The new x height for the font.
     */
    public void setXHeight( float xHeight )
    {
        dic.setFloat( COSName.XHEIGHT, xHeight );
        this.xHeight = xHeight;
    }

    /**
     * This will get the stemV for the font.
     *
     * @return The stem v value.
     */
    public float getStemV()
    {
        return dic.getFloat( COSName.STEM_V, 0 );
    }

    /**
     * This will set the stem V for the font.
     *
     * @param stemV The new stem v for the font.
     */
    public void setStemV( float stemV )
    {
        dic.setFloat( COSName.STEM_V, stemV );
    }

    /**
     * This will get the stemH for the font.
     *
     * @return The stem h value.
     */
    public float getStemH()
    {
        return dic.getFloat( COSName.STEM_H, 0 );
    }

    /**
     * This will set the stem H for the font.
     *
     * @param stemH The new stem h for the font.
     */
    public void setStemH( float stemH )
    {
        dic.setFloat( COSName.STEM_H, stemH );
    }

    /**
     * This will get the average width for the font.
     *
     * @return The average width value.
     */
    public float getAverageWidth()
    {
        return dic.getFloat( COSName.AVG_WIDTH, 0 );
    }

    /**
     * This will set the average width for the font.
     *
     * @param averageWidth The new average width for the font.
     */
    public void setAverageWidth( float averageWidth )
    {
        dic.setFloat( COSName.AVG_WIDTH, averageWidth );
    }

    /**
     * This will get the max width for the font.
     *
     * @return The max width value.
     */
    public float getMaxWidth()
    {
        return dic.getFloat( COSName.MAX_WIDTH, 0 );
    }

    /**
     * This will set the max width for the font.
     *
     * @param maxWidth The new max width for the font.
     */
    public void setMaxWidth( float maxWidth )
    {
        dic.setFloat( COSName.MAX_WIDTH, maxWidth );
    }

    /**
     * This will get the missing width for the font.
     *
     * @return The missing width value.
     */
    public float getMissingWidth()
    {
        return dic.getFloat( COSName.MISSING_WIDTH, 0 );
    }

    /**
     * This will set the missing width for the font.
     *
     * @param missingWidth The new missing width for the font.
     */
    public void setMissingWidth( float missingWidth )
    {
        dic.setFloat( COSName.MISSING_WIDTH, missingWidth );
    }

    /**
     * This will get the character set for the font.
     *
     * @return The character set value.
     */
    public String getCharSet()
    {
        String retval = null;
        COSString name = (COSString)dic.getDictionaryObject( COSName.CHAR_SET );
        if( name != null )
        {
            retval = name.getString();
        }
        return retval;
    }

    /**
     * This will set the character set for the font.
     *
     * @param charSet The new character set for the font.
     */
    public void setCharacterSet( String charSet )
    {
        COSString name = null;
        if( charSet != null )
        {
            name = new COSString( charSet );
        }
        dic.setItem( COSName.CHAR_SET, name );
    }

    /**
     * A stream containing a Type 1 font program.
     *
     * @return A stream containing a Type 1 font program.
     */
    public PDStream getFontFile()
    {
        PDStream retval = null;
        COSStream stream = (COSStream)dic.getDictionaryObject( COSName.FONT_FILE );
        if( stream != null )
        {
            retval = new PDStream( stream );
        }
        return retval;
    }

    /**
     * Set the type 1 font program.
     *
     * @param type1Stream The type 1 stream.
     */
    public void setFontFile( PDStream type1Stream )
    {
        dic.setItem( COSName.FONT_FILE, type1Stream );
    }

    /**
     * A stream containing a true type font program.
     *
     * @return A stream containing a true type font program.
     */
    public PDStream getFontFile2()
    {
        PDStream retval = null;
        COSStream stream = (COSStream)dic.getDictionaryObject( COSName.FONT_FILE2 );
        if( stream != null )
        {
            retval = new PDStream( stream );
        }
        return retval;
    }

    /**
     * Set the true type font program.
     *
     * @param ttfStream The true type stream.
     */
    public void setFontFile2( PDStream ttfStream )
    {
        dic.setItem( COSName.FONT_FILE2, ttfStream );
    }

    /**
     * A stream containing a font program that is not true type or type 1.
     *
     * @return A stream containing a font program.
     */
    public PDStream getFontFile3()
    {
        PDStream retval = null;
        COSStream stream = (COSStream)dic.getDictionaryObject( COSName.FONT_FILE3 );
        if( stream != null )
        {
            retval = new PDStream( stream );
        }
        return retval;
    }

    /**
     * Set a stream containing a font program that is not true type or type 1.
     *
     * @param stream The font program stream.
     */
    public void setFontFile3( PDStream stream )
    {
        dic.setItem( COSName.FONT_FILE3, stream );
    }
}
