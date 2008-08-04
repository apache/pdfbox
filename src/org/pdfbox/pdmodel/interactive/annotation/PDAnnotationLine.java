/**
 * Copyright (c) 2003, www.pdfbox.org
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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.graphics.color.PDGamma;

/**
 * This is the class that represents a line annotation.
 * Introduced in PDF 1.3 specification
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public class PDAnnotationLine extends PDAnnotationMarkup
{
    

    /*
     * The various values for intent (get/setIT, see the PDF 1.6 reference Table
     * 8.22
     */

    /**
     * Constant for annotation intent of Arrow.
     */
    public static final String IT_LINE_ARROW = "LineArrow";

    /**
     * Constant for annotation intent of a dimension line.
     */
    public static final String IT_LINE_DIMENSION = "LineDimension";

    /*
     * The various values for line ending styles, see the PDF 1.6 reference
     * Table 8.23
     */

    /**
     * Constant for a square line ending.
     */
    public static final String LE_SQUARE = "Square";

    /**
     * Constant for a circle line ending.
     */
    public static final String LE_CIRCLE = "Circle";

    /**
     * Constant for a diamond line ending.
     */
    public static final String LE_DIAMOND = "Diamond";

    /**
     * Constant for a open arrow line ending.
     */
    public static final String LE_OPEN_ARROW = "OpenArrow";

    /**
     * Constant for a closed arrow line ending.
     */
    public static final String LE_CLOSED_ARROW = "ClosedArrow";

    /**
     * Constant for no line ending.
     */
    public static final String LE_NONE = "None";

    /**
     * Constant for a butt line ending.
     */
    public static final String LE_BUTT = "Butt";

    /**
     * Constant for a reversed open arrow line ending.
     */
    public static final String LE_R_OPEN_ARROW = "ROpenArrow";

    /**
     * Constant for a revered closed arrow line ending.
     */
    public static final String LE_R_CLOSED_ARROW = "RClosedArrow";

    /**
     * Constant for a slash line ending.
     */
    public static final String LE_SLASH = "Slash";

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Line";

    /**
     * Constructor.
     */
    public PDAnnotationLine()
    {
        super();
        getDictionary().setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
        // Dictionary value L is mandatory, fill in with arbitary value
        setLine( new float[] { 0, 0, 0, 0 } );

    }

    /**
     * Creates a Line annotation from a COSDictionary, expected to be a correct
     * object definition.
     * 
     * @param field
     *            the PDF objet to represent as a field.
     */
    public PDAnnotationLine( COSDictionary field )
    {
        super( field );
    }

    /**
     * This will set start and end coordinates of the line (or leader line if LL
     * entry is set).
     * 
     * @param l
     *            array of 4 floats [x1, y1, x2, y2] line start and end points
     *            in default user space.
     */
    public void setLine( float[] l )
    {
        COSArray newL = new COSArray();
        newL.setFloatArray( l );
        getDictionary().setItem( "L", newL );
    }

    /**
     * This will retrieve the start and end coordinates of the line (or leader
     * line if LL entry is set).
     * 
     * @return array of floats [x1, y1, x2, y2] line start and end points in
     *         default user space.
     */
    public float[] getLine()
    {
        COSArray l = (COSArray) getDictionary().getDictionaryObject( "L" );
        return l.toFloatArray();
    }
    
    /**
     * This will set the line ending style for the start point, 
     * see the LE_ constants for the possible values.
     * 
     * @param style The new style.
     */
    public void setStartPointEndingStyle( String style )
    {
        if( style == null )
        {
            style = LE_NONE;
        }
        COSArray array = (COSArray)getDictionary().getDictionaryObject( "LE" );
        if( array == null )
        {
            array = new COSArray();
            array.add( COSName.getPDFName( style ) );
            array.add( COSName.getPDFName( LE_NONE ) );
            getDictionary().setItem( "LE", array );
        }
        else
        {
            array.setName( 0, style );
        }
    }
    
    /**
     * This will retrieve the line ending style for the start point, 
     * possible values shown in the LE_ constants section.
     * 
     * @return The ending style for the start point.
     */
    public String getStartPointEndingStyle()
    {
        String retval = LE_NONE;
        COSArray array = (COSArray)getDictionary().getDictionaryObject( "LE" );
        if( array != null )
        {
            retval = array.getName( 0 );
        }
        
        return retval;
    }
    
    /**
     * This will set the line ending style for the end point, 
     * see the LE_ constants for the possible values.
     * 
     * @param style The new style.
     */
    public void setEndPointEndingStyle( String style )
    {
        if( style == null )
        {
            style = LE_NONE;
        }
        COSArray array = (COSArray)getDictionary().getDictionaryObject( "LE" );
        if( array == null )
        {
            array = new COSArray();
            array.add( COSName.getPDFName( LE_NONE ) );
            array.add( COSName.getPDFName( style ) );
            getDictionary().setItem( "LE", array );
        }
        else
        {
            array.setName( 1, style );
        }
    }
    
    /**
     * This will retrieve the line ending style for the end point, 
     * possible values shown in the LE_ constants section.
     * 
     * @return The ending style for the end point.
     */
    public String getEndPointEndingStyle()
    {
        String retval = LE_NONE;
        COSArray array = (COSArray)getDictionary().getDictionaryObject( "LE" );
        if( array != null )
        {
            retval = array.getName( 1 );
        }
        
        return retval;
    }

    /**
     * This will set interior colour of the line endings defined in the LE
     * entry. Colour is in DeviceRGB colourspace.
     * 
     * @param ic
     *            colour in the DeviceRGB colourspace.
     * 
     */
    public void setInteriorColour( PDGamma ic )
    {
        getDictionary().setItem( "IC", ic );
    }

    /**
     * This will retrieve the interior colour of the line endings defined in the
     * LE entry. Colour is in DeviceRGB colourspace.
     * 
     * 
     * @return PDGamma object representing the colour.
     * 
     */
    public PDGamma getInteriorColour()
    {

        COSArray ic = (COSArray) getDictionary().getDictionaryObject( "IC" );
        if (ic != null)
        {
            return new PDGamma( ic );
        } 
        else
        {
            return null;
        }
    }

    /**
     * This will set if the contents are shown as a caption to the line.
     * 
     * @param cap
     *            Boolean value.
     */
    public void setCaption( boolean cap )
    {
        getDictionary().setBoolean( "Cap", cap );
    }

    /**
     * This will retrieve if the contents are shown as a caption or not.
     * 
     * @return boolean if the content is shown as a caption.
     */
    public boolean getCaption()
    {
        return getDictionary().getBoolean( "Cap", false );
    }

}