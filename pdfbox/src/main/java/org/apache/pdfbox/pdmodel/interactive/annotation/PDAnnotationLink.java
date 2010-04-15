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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;

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
     * Get the action to be performed when this annotation is to be activated.
     *
     * @return The action to be performed when this annotation is activated.
     *
     * TODO not all annotations have an A entry
     */
    public PDAction getAction()
    {
        COSDictionary action = (COSDictionary)
            this.getDictionary().getDictionaryObject( COSName.A );
        return PDActionFactory.createAction( action );
    }

    /**
     * Set the annotation action.
     * As of PDF 1.6 this is only used for Widget Annotations
     * @param action The annotation action.
     * TODO not all annotations have an A entry
     */
    public void setAction( PDAction action )
    {
        this.getDictionary().setItem( COSName.A, action );
    }

    /**
     * This will set the border style dictionary, specifying the width and dash
     * pattern used in drawing the line.
     *
     * @param bs the border style dictionary to set.
     * TODO not all annotations may have a BS entry
     *
     */
    public void setBorderStyle( PDBorderStyleDictionary bs )
    {
        this.getDictionary().setItem( "BS", bs);
    }

    /**
     * This will retrieve the border style dictionary, specifying the width and
     * dash pattern used in drawing the line.
     *
     * @return the border style dictionary.
     * TODO not all annotations may have a BS entry
     */
    public PDBorderStyleDictionary getBorderStyle()
    {
        COSDictionary bs = (COSDictionary) this.getDictionary().getItem(
                COSName.getPDFName( "BS" ) );
        if (bs != null)
        {
            return new PDBorderStyleDictionary( bs );
        }
        else
        {
            return null;
        }
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
