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

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSName;

/**
 * This is the abstract class that represents a text markup annotation
 * Introduced in PDF 1.3 specification, except Squiggly lines in 1.4.
 * 
 * @author Paul King
 * @version $Revision: 1.1 $
 */
public class PDAnnotationTextMarkup extends PDAnnotationMarkup
{

    /**
     * The types of annotation.
     */
    public static final String SUB_TYPE_HIGHLIGHT = "Highlight";
    /**
     * The types of annotation.
     */
    public static final String SUB_TYPE_UNDERLINE = "Underline";
    /**
     * The types of annotation.
     */
    public static final String SUB_TYPE_SQUIGGLY = "Squiggly";
    /**
     * The types of annotation.
     */
    public static final String SUB_TYPE_STRIKEOUT = "StrikeOut";


    private PDAnnotationTextMarkup() 
    {
        // Must be constructed with a subType or dictionary parameter
    }

    /**
     * Creates a TextMarkup annotation of the specified sub type.
     * 
     * @param subType the subtype the annotation represents
     */
    public PDAnnotationTextMarkup(String subType)
    {
        super();
        setSubtype( subType );

        // Quad points are required, set and empty array
        setQuadPoints( new float[0] );
    }

    /**
     * Creates a TextMarkup annotation from a COSDictionary, expected to be a
     * correct object definition.
     * 
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationTextMarkup( COSDictionary field )
    {
        super( field );
    }

    /**
     * This will set the set of quadpoints which encompass the areas of this
     * annotation.
     * 
     * @param quadPoints
     *            an array representing the set of area covered
     */
    public void setQuadPoints( float[] quadPoints )
    {
        COSArray newQuadPoints = new COSArray();
        newQuadPoints.setFloatArray( quadPoints );
        getDictionary().setItem( "QuadPoints", newQuadPoints );
    }

    /**
     * This will retrieve the set of quadpoints which encompass the areas of
     * this annotation.
     * 
     * @return An array of floats representing the quad points.
     */
    public float[] getQuadPoints()
    {
        COSArray quadPoints = (COSArray) getDictionary().getDictionaryObject( "QuadPoints" );
        if (quadPoints != null)
        {
            return quadPoints.toFloatArray();
        } 
        else
        {
            return null; // Should never happen as this is a required item
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