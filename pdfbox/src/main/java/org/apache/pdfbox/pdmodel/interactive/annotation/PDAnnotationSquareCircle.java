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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

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

}
