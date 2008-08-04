/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.documentinterchange.prepress;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceInstance;
import org.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

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
    public PDColorSpaceInstance getGuidelineColor()
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
        PDColorSpaceInstance instance = new PDColorSpaceInstance( colorValues );
        instance.setColorSpace( PDDeviceRGB.INSTANCE );
        return instance;
    }
    
    /**
     * Set the color space instance for this box style.  This must be a 
     * PDDeviceRGB!
     * 
     * @param color The new colorspace value.
     */
    public void setGuideLineColor( PDColorSpaceInstance color )
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