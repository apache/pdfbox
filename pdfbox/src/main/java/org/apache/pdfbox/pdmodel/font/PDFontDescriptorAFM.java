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

import org.apache.fontbox.afm.FontMetric;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.fontbox.util.BoundingBox;

/**
 * This class represents the font descriptor when the font information
 * is coming from an AFM file.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDFontDescriptorAFM extends PDFontDescriptor
{
    private FontMetric afm;

    /**
     * Constructor.
     *
     * @param afmFile The AFM file.
     */
    public PDFontDescriptorAFM( FontMetric afmFile )
    {
        afm = afmFile;
    }

    /**
     * Get the font name.
     *
     * @return The name of the font.
     */
    public String getFontName()
    {
        return afm.getFontName();
    }

    /**
     * This will set the font name.
     *
     * @param fontName The new name for the font.
     */
    public void setFontName( String fontName )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * A string representing the preferred font family.
     *
     * @return The font family.
     */
    public String getFontFamily()
    {
        return afm.getFamilyName();
    }

    /**
     * This will set the font family.
     *
     * @param fontFamily The font family.
     */
    public void setFontFamily( String fontFamily )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
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
        String weight = afm.getWeight();
        float retval = 500;
        if( weight != null && weight.equalsIgnoreCase( "bold" ) )
        {
            retval = 900;
        }
        else if( weight != null && weight.equalsIgnoreCase( "light" ) )
        {
            retval = 100;
        }
        return retval;
    }

    /**
     * Set the weight of the font.
     *
     * @param fontWeight The new weight of the font.
     */
    public void setFontWeight( float fontWeight )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * A string representing the preferred font stretch.
     *
     * @return The font stretch.
     */
    public String getFontStretch()
    {
        return null;
    }

    /**
     * This will set the font stretch.
     *
     * @param fontStretch The font stretch
     */
    public void setFontStretch( String fontStretch )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the font flags.
     *
     * @return The font flags.
     */
    public int getFlags()
    {
        //I believe that the only flag that AFM supports is the is fixed pitch
        return afm.isFixedPitch() ? 1 : 0;
    }

    /**
     * This will set the font flags.
     *
     * @param flags The new font flags.
     */
    public void setFlags( int flags )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the fonts bouding box.
     *
     * @return The fonts bouding box.
     */
    public PDRectangle getFontBoundingBox()
    {
        BoundingBox box = afm.getFontBBox();
        PDRectangle retval = null;
        if( box != null )
        {
            retval = new PDRectangle( box );
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
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the italic angle for the font.
     *
     * @return The italic angle.
     */
    public float getItalicAngle()
    {
        return afm.getItalicAngle();
    }

    /**
     * This will set the italic angle for the font.
     *
     * @param angle The new italic angle for the font.
     */
    public void setItalicAngle( float angle )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the ascent for the font.
     *
     * @return The ascent.
     */
    public float getAscent()
    {
        return afm.getAscender();
    }

    /**
     * This will set the ascent for the font.
     *
     * @param ascent The new ascent for the font.
     */
    public void setAscent( float ascent )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the descent for the font.
     *
     * @return The descent.
     */
    public float getDescent()
    {
        return afm.getDescender();
    }

    /**
     * This will set the descent for the font.
     *
     * @param descent The new descent for the font.
     */
    public void setDescent( float descent )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the leading for the font.
     *
     * @return The leading.
     */
    public float getLeading()
    {
        //AFM does not support setting the leading so we will just ignore it.
        return 0f;
    }

    /**
     * This will set the leading for the font.
     *
     * @param leading The new leading for the font.
     */
    public void setLeading( float leading )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the CapHeight for the font.
     *
     * @return The cap height.
     */
    public float getCapHeight()
    {
        return afm.getCapHeight();
    }

    /**
     * This will set the cap height for the font.
     *
     * @param capHeight The new cap height for the font.
     */
    public void setCapHeight( float capHeight )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the x height for the font.
     *
     * @return The x height.
     */
    public float getXHeight()
    {
        return afm.getXHeight();
    }

    /**
     * This will set the x height for the font.
     *
     * @param xHeight The new x height for the font.
     */
    public void setXHeight( float xHeight )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the stemV for the font.
     *
     * @return The stem v value.
     */
    public float getStemV()
    {
        //afm does not have a stem v
        return 0;
    }

    /**
     * This will set the stem V for the font.
     *
     * @param stemV The new stem v for the font.
     */
    public void setStemV( float stemV )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the stemH for the font.
     *
     * @return The stem h value.
     */
    public float getStemH()
    {
        //afm does not have a stem h
        return 0;
    }

    /**
     * This will set the stem H for the font.
     *
     * @param stemH The new stem h for the font.
     */
    public void setStemH( float stemH )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the average width for the font.
     *
     * @return The average width value.
     *
     * @throws IOException If there is an error calculating the average width.
     */
    public float getAverageWidth() throws IOException
    {
        return afm.getAverageCharacterWidth();
    }

    /**
     * This will set the average width for the font.
     *
     * @param averageWidth The new average width for the font.
     */
    public void setAverageWidth( float averageWidth )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the max width for the font.
     *
     * @return The max width value.
     */
    public float getMaxWidth()
    {
        //afm does not support max width;
        return 0;
    }

    /**
     * This will set the max width for the font.
     *
     * @param maxWidth The new max width for the font.
     */
    public void setMaxWidth( float maxWidth )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the missing width for the font.
     *
     * @return The missing width value.
     */
    public float getMissingWidth()
    {
        return 0;
    }

    /**
     * This will set the missing width for the font.
     *
     * @param missingWidth The new missing width for the font.
     */
    public void setMissingWidth( float missingWidth )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }

    /**
     * This will get the character set for the font.
     *
     * @return The character set value.
     */
    public String getCharSet()
    {
        return afm.getCharacterSet();
    }

    /**
     * This will set the character set for the font.
     *
     * @param charSet The new character set for the font.
     */
    public void setCharacterSet( String charSet )
    {
        throw new UnsupportedOperationException( "The AFM Font descriptor is immutable" );
    }
}
