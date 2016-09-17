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
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * A font descriptor.
 *
 * @author Ben Litchfield
 */
public final class PDFontDescriptor implements COSObjectable
{
    private static final int FLAG_FIXED_PITCH = 1;
    private static final int FLAG_SERIF = 2;
    private static final int FLAG_SYMBOLIC = 4;
    private static final int FLAG_SCRIPT = 8;
    private static final int FLAG_NON_SYMBOLIC = 32;
    private static final int FLAG_ITALIC = 64;
    private static final int FLAG_ALL_CAP = 65536;
    private static final int FLAG_SMALL_CAP = 131072;
    private static final int FLAG_FORCE_BOLD = 262144;

    private final COSDictionary dic;
    private float xHeight = Float.NEGATIVE_INFINITY;
    private float capHeight = Float.NEGATIVE_INFINITY;
    private int flags = -1;


    /**
     * Package-private constructor, for embedding.
     */
    PDFontDescriptor()
    {
        dic = new COSDictionary();
        dic.setItem( COSName.TYPE, COSName.FONT_DESC );
    }

    /**
     * Creates a PDFontDescriptor from a COS dictionary.
     *
     * @param desc The wrapped COS Dictionary.
     */
    public PDFontDescriptor( COSDictionary desc )
    {
        dic = desc;
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isFixedPitch()
    {
        return isFlagBitOn( FLAG_FIXED_PITCH );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setFixedPitch( boolean flag )
    {
        setFlagBit( FLAG_FIXED_PITCH, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isSerif()
    {
        return isFlagBitOn( FLAG_SERIF );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setSerif( boolean flag )
    {
        setFlagBit( FLAG_SERIF, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isSymbolic()
    {
        return isFlagBitOn( FLAG_SYMBOLIC );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setSymbolic( boolean flag )
    {
        setFlagBit( FLAG_SYMBOLIC, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isScript()
    {
        return isFlagBitOn( FLAG_SCRIPT );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setScript( boolean flag )
    {
        setFlagBit( FLAG_SCRIPT, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isNonSymbolic()
    {
        return isFlagBitOn( FLAG_NON_SYMBOLIC );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setNonSymbolic( boolean flag )
    {
        setFlagBit( FLAG_NON_SYMBOLIC, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isItalic()
    {
        return isFlagBitOn( FLAG_ITALIC );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setItalic( boolean flag )
    {
        setFlagBit( FLAG_ITALIC, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isAllCap()
    {
        return isFlagBitOn( FLAG_ALL_CAP);
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setAllCap( boolean flag )
    {
        setFlagBit( FLAG_ALL_CAP, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isSmallCap()
    {
        return isFlagBitOn( FLAG_SMALL_CAP );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setSmallCap( boolean flag )
    {
        setFlagBit( FLAG_SMALL_CAP, flag );
    }

    /**
     * A convenience method that checks the flag bit.
     *
     * @return The flag value.
     */
    public boolean isForceBold()
    {
        return isFlagBitOn( FLAG_FORCE_BOLD );
    }

    /**
     * A convenience method that sets the flag bit.
     *
     * @param flag The flag value.
     */
    public void setForceBold( boolean flag )
    {
        setFlagBit( FLAG_FORCE_BOLD, flag );
    }

    private boolean isFlagBitOn( int bit )
    {
        return (getFlags() & bit) != 0;
    }

    private void setFlagBit( int bit, boolean value )
    {
        int flags = getFlags();
        if( value )
        {
            flags = flags | bit;
        }
        else
        {
            flags = flags & (~bit);
        }
        setFlags( flags );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
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
        COSBase base = dic.getDictionaryObject(COSName.FONT_NAME);
        if (base instanceof COSName)
        {
            retval = ((COSName) base).getName();
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
     * Returns true if widths are present in the font descriptor.
     */
    public boolean hasWidths()
    {
        return dic.containsKey(COSName.WIDTHS) || dic.containsKey(COSName.MISSING_WIDTH);
    }

    /**
     * Returns true if the missing widths entry is present in the font descriptor.
     */
    public boolean hasMissingWidth()
    {
        return dic.containsKey(COSName.MISSING_WIDTH);
    }

    /**
     * This will get the missing width for the font from the /MissingWidth dictionary entry.
     *
     * @return The missing width value, or 0 if there is no such dictionary entry.
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
        COSBase obj = dic.getDictionaryObject(COSName.FONT_FILE);
        if (obj instanceof COSStream)
        {
            retval = new PDStream((COSStream) obj);
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
        COSBase obj = dic.getDictionaryObject(COSName.FONT_FILE2);
        if (obj instanceof COSStream)
        {
            retval = new PDStream((COSStream) obj);
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
        COSBase obj = dic.getDictionaryObject(COSName.FONT_FILE3);
        if (obj instanceof COSStream)
        {
            retval = new PDStream((COSStream) obj);
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

    /**
     * Get the CIDSet stream.
     *
     * @return A stream containing a CIDSet.
     */
    public PDStream getCIDSet()
    {
        COSObjectable cidSet = dic.getDictionaryObject(COSName.CID_SET);
        if (cidSet instanceof COSStream)
        {
            return new PDStream((COSStream) cidSet);
        }
        return null;
    }

    /**
     * Set a stream containing a CIDSet.
     *
     * @param stream The font program stream.
     */
    public void setCIDSet( PDStream stream )
    {
        dic.setItem( COSName.CID_SET, stream );
    }

    /**
     * Returns the Panose entry of the Style dictionary, if any.
     *
     * @return A Panose wrapper object.
     */
    public PDPanose getPanose()
    {
        COSDictionary style = (COSDictionary)dic.getDictionaryObject(COSName.STYLE);
        if (style != null)
        {
            COSString panose = (COSString)style.getDictionaryObject(COSName.PANOSE);
            byte[] bytes = panose.getBytes();
            return new PDPanose(bytes);
        }
        return null;
    }
}
