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

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This class represents an interface to the font description.  This will depend
 * on the font type for the actual implementation.  If it is a AFM/cmap/or embedded font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public abstract class PDFontDescriptor
{
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_FIXED_PITCH = 1;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_SERIF = 2;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_SYMBOLIC = 4;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_SCRIPT = 8;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_NON_SYMBOLIC = 32;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_ITALIC = 64;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_ALL_CAP = 65536;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_SMALL_CAP = 131072;
    /**
     * A font descriptor flag.  See PDF Reference for description.
     */
    private static final int FLAG_FORCE_BOLD = 262144;


    /**
     * Get the font name.
     *
     * @return The name of the font.
     */
    public abstract String getFontName();

    /**
     * This will set the font name.
     *
     * @param fontName The new name for the font.
     */
    public abstract void setFontName( String fontName );

    /**
     * A string representing the preferred font family.
     *
     * @return The font family.
     */
    public abstract String getFontFamily();

    /**
     * This will set the font family.
     *
     * @param fontFamily The font family.
     */
    public abstract void setFontFamily( String fontFamily );

    /**
     * A string representing the preferred font stretch.
     * According to the PDF Spec:
     * The font stretch value; it must be one of the following (ordered from
     * narrowest to widest): UltraCondensed, ExtraCondensed, Condensed, SemiCondensed,
     * Normal, SemiExpanded, Expanded, ExtraExpanded or UltraExpanded.
     *
     * @return The font stretch.
     */
    public abstract String getFontStretch();

    /**
     * This will set the font stretch.
     *
     * @param fontStretch The font stretch
     */
    public abstract void setFontStretch( String fontStretch );

    /**
     * The weight of the font.  According to the PDF spec "possible values are
     * 100, 200, 300, 400, 500, 600, 700, 800 or 900"  Where a higher number is
     * more weight and appears to be more bold.
     *
     * @return The font weight.
     */
    public abstract float getFontWeight();

    /**
     * Set the weight of the font.
     *
     * @param fontWeight The new weight of the font.
     */
    public abstract void setFontWeight( float fontWeight );

    /**
     * This will get the font flags.
     *
     * @return The font flags.
     */
    public abstract int getFlags();

    /**
     * This will set the font flags.
     *
     * @param flags The new font flags.
     */
    public abstract void setFlags( int flags );

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
            flags = flags & (0xFFFFFFFF ^ bit);
        }
        setFlags( flags );
    }

    /**
     * This will get the fonts bouding box.
     *
     * @return The fonts bouding box.
     */
    public abstract PDRectangle getFontBoundingBox();

    /**
     * Set the fonts bounding box.
     *
     * @param rect The new bouding box.
     */
    public abstract void setFontBoundingBox( PDRectangle rect );

    /**
     * This will get the italic angle for the font.
     *
     * @return The italic angle.
     */
    public abstract float getItalicAngle();

    /**
     * This will set the italic angle for the font.
     *
     * @param angle The new italic angle for the font.
     */
    public abstract void setItalicAngle( float angle );

    /**
     * This will get the ascent for the font.
     *
     * @return The ascent.
     */
    public abstract float getAscent();

    /**
     * This will set the ascent for the font.
     *
     * @param ascent The new ascent for the font.
     */
    public abstract void setAscent( float ascent );

    /**
     * This will get the descent for the font.
     *
     * @return The descent.
     */
    public abstract float getDescent();

    /**
     * This will set the descent for the font.
     *
     * @param descent The new descent for the font.
     */
    public abstract void setDescent( float descent );

    /**
     * This will get the leading for the font.
     *
     * @return The leading.
     */
    public abstract float getLeading();

    /**
     * This will set the leading for the font.
     *
     * @param leading The new leading for the font.
     */
    public abstract void setLeading( float leading );

    /**
     * This will get the CapHeight for the font.
     *
     * @return The cap height.
     */
    public abstract float getCapHeight();

    /**
     * This will set the cap height for the font.
     *
     * @param capHeight The new cap height for the font.
     */
    public abstract void setCapHeight( float capHeight );

    /**
     * This will get the x height for the font.
     *
     * @return The x height.
     */
    public abstract float getXHeight();

    /**
     * This will set the x height for the font.
     *
     * @param xHeight The new x height for the font.
     */
    public abstract void setXHeight( float xHeight );

    /**
     * This will get the stemV for the font.
     *
     * @return The stem v value.
     */
    public abstract float getStemV();

    /**
     * This will set the stem V for the font.
     *
     * @param stemV The new stem v for the font.
     */
    public abstract void setStemV( float stemV );

    /**
     * This will get the stemH for the font.
     *
     * @return The stem h value.
     */
    public abstract float getStemH();

    /**
     * This will set the stem H for the font.
     *
     * @param stemH The new stem h for the font.
     */
    public abstract void setStemH( float stemH );

    /**
     * This will get the average width for the font.  This is part of the
     * definition in the font description.  If it is not present then PDFBox
     * will make an attempt to calculate it.
     *
     * @return The average width value.
     *
     * @throws IOException If there is an error calculating the average width.
     */
    public abstract float getAverageWidth() throws IOException;

    /**
     * This will set the average width for the font.
     *
     * @param averageWidth The new average width for the font.
     */
    public abstract void setAverageWidth( float averageWidth );

    /**
     * This will get the max width for the font.
     *
     * @return The max width value.
     */
    public abstract float getMaxWidth();

    /**
     * This will set the max width for the font.
     *
     * @param maxWidth The new max width for the font.
     */
    public abstract void setMaxWidth( float maxWidth );

    /**
     * This will get the character set for the font.
     *
     * @return The character set value.
     */
    public abstract String getCharSet();

    /**
     * This will set the character set for the font.
     *
     * @param charSet The new character set for the font.
     */
    public abstract void setCharacterSet( String charSet );

    /**
     * This will get the missing width for the font.
     *
     * @return The missing width value.
     */
    public abstract float getMissingWidth();

    /**
     * This will set the missing width for the font.
     *
     * @param charSet The new missing width for the font.
     */
    public abstract void setMissingWidth( float missingWidth );

}
