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
package org.apache.pdfbox.pdmodel.documentinterchange.prepress;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * The Box Style specifies visual characteristics for displaying box areas.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDBoxStyle implements COSObjectable
{
    /**
     * Style for guideline.
     */
    public static final String GUIDELINE_STYLE_SOLID = "S";
    /**
     * Style for guideline.
     */
    public static final String GUIDELINE_STYLE_DASHED = "D";

    private COSDictionary dictionary;

    /**
     * Default Constructor.
     *
     */
    public PDBoxStyle()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor for an existing BoxStyle element.
     *
     * @param dic The existing dictionary.
     */
    public PDBoxStyle( COSDictionary dic )
    {
        dictionary = dic;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * Get the color to be used for the guidelines.  This is guaranteed to
     * not return null.  The color space will always be DeviceRGB and the
     * default color is [0,0,0].
     *
     *@return The guideline color.
     */
    public PDColorState getGuidelineColor()
    {
        COSArray colorValues = (COSArray)dictionary.getDictionaryObject( "C" );
        if( colorValues == null )
        {
            colorValues = new COSArray();
            colorValues.add( COSInteger.ZERO );
            colorValues.add( COSInteger.ZERO );
            colorValues.add( COSInteger.ZERO );
            dictionary.setItem( "C", colorValues );
        }
        PDColorState instance = new PDColorState( colorValues );
        instance.setColorSpace( PDDeviceRGB.INSTANCE );
        return instance;
    }

    /**
     * Set the color space instance for this box style.  This must be a
     * PDDeviceRGB!
     *
     * @param color The new colorspace value.
     */
    public void setGuideLineColor( PDColorState color )
    {
        COSArray values = null;
        if( color != null )
        {
            values = color.getCOSColorSpaceValue();
        }
        dictionary.setItem( "C", values );
    }

    /**
     * Get the width of the of the guideline in default user space units.
     * The default is 1.
     *
     * @return The width of the guideline.
     */
    public float getGuidelineWidth()
    {
        return dictionary.getFloat( "W", 1 );
    }

    /**
     * Set the guideline width.
     *
     * @param width The width in default user space units.
     */
    public void setGuidelineWidth( float width )
    {
        dictionary.setFloat( "W", width );
    }

    /**
     * Get the style for the guideline.  The default is "S" for solid.
     *
     * @return The guideline style.
     * @see PDBoxStyle#GUIDELINE_STYLE_DASHED
     * @see PDBoxStyle#GUIDELINE_STYLE_SOLID
     */
    public String getGuidelineStyle()
    {
        return dictionary.getNameAsString( "S", GUIDELINE_STYLE_SOLID );
    }

    /**
     * Set the style for the box.
     *
     * @param style The style for the box line.
     * @see PDBoxStyle#GUIDELINE_STYLE_DASHED
     * @see PDBoxStyle#GUIDELINE_STYLE_SOLID
     */
    public void setGuidelineStyle( String style )
    {
        dictionary.setName( "S", style );
    }

    /**
     * Get the line dash pattern for this box style.  This is guaranteed to not
     * return null.  The default is [3],0.
     *
     * @return The line dash pattern.
     */
    public PDLineDashPattern getLineDashPattern()
    {
        PDLineDashPattern pattern = null;
        COSArray d = (COSArray)dictionary.getDictionaryObject( "D" );
        if( d == null )
        {
            d = new COSArray();
            d.add( new COSInteger(3) );
            dictionary.setItem( "D", d );
        }
        COSArray lineArray = new COSArray();
        lineArray.add( d );
        //dash phase is not specified and assumed to be zero.
        lineArray.add( new COSInteger( 0 ) );
        pattern = new PDLineDashPattern( lineArray );
        return pattern;
    }

    /**
     * Set the line dash pattern associated with this box style.
     *
     * @param pattern The patter for this box style.
     */
    public void setLineDashPattern( PDLineDashPattern pattern )
    {
        COSArray array = null;
        if( pattern != null )
        {
            array = pattern.getCOSDashPattern();
        }
        dictionary.setItem( "D", array );
    }
}
