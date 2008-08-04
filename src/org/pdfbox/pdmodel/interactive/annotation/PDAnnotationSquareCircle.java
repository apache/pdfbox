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
package org.pdfbox.pdmodel.interactive.annotation;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.graphics.color.PDGamma;
import org.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is the class that represents a rectangular or eliptical annotation
 * Introduced in PDF 1.3 specification .
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public class PDAnnotationSquareCircle extends PDAnnotationMarkup
{

    /**
     * Constant for a Rectangular type of annotation.
     */
    public static final String SUB_TYPE_SQUARE = "Square";
    /**
     * Constant for an Eliptical type of annotation.
     */
    public static final String SUB_TYPE_CIRCLE = "Circle";

    private PDAnnotationSquareCircle()
    {
        // Must be constructed with a subType or dictionary parameter
    }

    
    /**
     * Creates a Circle or Square annotation of the specified sub type.
     * 
     * @param subType the subtype the annotation represents.
         */
    public PDAnnotationSquareCircle( String subType )
    {
        super();
        setSubtype( subType );
    }

    /**
     * Creates a Line annotation from a COSDictionary, expected to be a correct
     * object definition.
     * 
     * @param field
     *            the PDF objet to represent as a field.
     */
    public PDAnnotationSquareCircle( COSDictionary field )
    {
        super( field );
    }


    /**
     * This will set interior colour of the drawn area
     * Colour is in DeviceRGB colourspace.
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
     * This will retrieve the interior colour of the drawn area
     * Colour is in DeviceRGB colourspace.
     * 
     * 
     * @return PDGamma object representing the colour.
     * 
     */
    public PDGamma getInteriorColour()
    {

        COSArray ic = (COSArray) getDictionary().getItem(
                COSName.getPDFName( "IC" ) );
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
     * This will set the border effect dictionary, specifying effects to be applied
     * when drawing the line.
     * 
     * @param be The border effect dictionary to set.
     * 
     */
    public void setBorderEffect( PDBorderEffectDictionary be )
    {
        getDictionary().setItem( "BE", be );
    }

    /**
     * This will retrieve the border effect dictionary, specifying effects to be
     * applied used in drawing the line.
     * 
     * @return The border effect dictionary
     */
    public PDBorderEffectDictionary getBorderEffect()
    {
        COSDictionary be = (COSDictionary) getDictionary().getDictionaryObject( "BE" );
        if (be != null)
        {
            return new PDBorderEffectDictionary( be );
        } 
        else
        {
            return null;
        }
    }

    /**
     * This will set the rectangle difference rectangle. Giving the difference
     * between the annotations rectangle and where the drawing occurs.
         * (To take account of any effects applied through the BE entry forexample)
     * 
     * @param rd the rectangle difference
     * 
     */
    public void setRectDifference( PDRectangle rd )
    {
        getDictionary().setItem( "RD", rd );
    }

    /**
     * This will get the rectangle difference rectangle. Giving the difference
     * between the annotations rectangle and where the drawing occurs.
         * (To take account of any effects applied through the BE entry forexample)
     * 
     * @return the rectangle difference
     */
    public PDRectangle getRectDifference()
    {
        COSArray rd = (COSArray) getDictionary().getDictionaryObject( "RD" );
        if (rd != null)
        {
            return new PDRectangle( rd );
        } 
        else
        {
            return null;
        }
    }

    /**
     * This will set the sub type (and hence appearance, AP taking precedence) For
     * this annotation. See the SUB_TYPE_XXX constants for valid values.
     * 
     * @param subType The subtype of the annotation
     */
    public void setSubtype( String subType )
    {
        getDictionary().setName( COSName.SUBTYPE, subType );
    }

    /**
     * This will retrieve the sub type (and hence appearance, AP taking precedence)
     * For this annotation. 
     * 
     * @return The subtype of this annotation, see the SUB_TYPE_XXX constants.
     */
    public String getSubtype()
    {
        return getDictionary().getNameAsString( COSName.SUBTYPE);
    }

}