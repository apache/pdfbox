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

import java.io.IOException;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;

/**
 * This is the class that represents a link annotation.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Paul King
 * @version $Revision: 1.3 $
 */
public class PDAnnotationLink extends PDAnnotation
{

    
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_NONE = "N";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_INVERT = "I";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_OUTLINE = "O";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_PUSH = "P";
    
    
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Link";
    
    /**
     * Constructor.
     */
    public PDAnnotationLink()
    {
        super();
        getDictionary().setItem( COSName.SUBTYPE, COSName.getPDFName( SUB_TYPE ) );
    }

    /**
     * Creates a Link annotation from a COSDictionary, expected to be
     * a correct object definition.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationLink(COSDictionary field)
    {
        super( field );
    }

    /**
     * Get the destination to be displayed when the annotation is activated.  Either
     * this or the A should be set but not both.
     * 
     * @return The destination for this annotation.
     * 
     * @throws IOException If there is an error creating the destination.
     */
    public PDDestination getDestination() throws IOException
    {
        COSBase base = getDictionary().getDictionaryObject( COSName.DEST );
        PDDestination retval = PDDestination.create( base ); 
            
        return retval;
    }
    
    /**
     * The new destination value.
     * 
     * @param dest The updated destination.
     */
    public void setDestination( PDDestination dest )
    {
        getDictionary().setItem( COSName.DEST, dest );
    }
    
    /**
     * Set the highlight mode for when the mouse is depressed.  
     * See the HIGHLIGHT_MODE_XXX constants.
     * 
     * @return The string representation of the highlight mode.
     */
    public String getHighlightMode()
    {
        return getDictionary().getNameAsString( COSName.H, HIGHLIGHT_MODE_INVERT );
    }
    
    /**
     * Set the highlight mode.  See the HIGHLIGHT_MODE_XXX constants.
     * 
     * @param mode The new highlight mode.
     */
    public void setHighlightMode( String mode )
    {
        getDictionary().setName( COSName.H, mode );
    }
    
    /**
     * This will set the previous URI action, in case it
     * needs to be retrieved at later date.
     * 
     * @param pa The previous URI.
     */
    public void setPreviousURI( PDActionURI pa )
    {
        getDictionary().setItem( "PA", pa );
    }

    /**
     * This will set the previous URI action, in case it's
     * needed.
     * 
     * @return The previous URI.
     */
    public PDActionURI getPreviousURI() 
    {
        COSDictionary pa = (COSDictionary) getDictionary().getDictionaryObject("PA");
        if ( pa != null ) 
        {
            return new PDActionURI( pa );
        }
        else
        {
            return null;
        }
    }
    
    /**
     * This will set the set of quadpoints which encompass the areas of this
     * annotation which will activate.
     * 
     * @param quadPoints
     *            an array representing the set of area covered.
     */
    public void setQuadPoints( float[] quadPoints )
    {
        COSArray newQuadPoints = new COSArray();
        newQuadPoints.setFloatArray( quadPoints );
        getDictionary().setItem( "QuadPoints", newQuadPoints );
    }

    /**
     * This will retrieve the set of quadpoints which encompass the areas of
     * this annotation which will activate.
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
}