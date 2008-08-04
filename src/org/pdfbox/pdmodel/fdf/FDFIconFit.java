/**
 * Copyright (c) 2004-2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.fdf;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.PDRange;

/**
 * This represents an Icon fit dictionary for an FDF field.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class FDFIconFit implements COSObjectable
{
    private COSDictionary fit;

    /**
     * A scale option.
     */
    public static final String SCALE_OPTION_ALWAYS = "A";
    /**
     * A scale option.
     */
    public static final String SCALE_OPTION_ONLY_WHEN_ICON_IS_BIGGER = "B";
    /**
     * A scale option.
     */
    public static final String SCALE_OPTION_ONLY_WHEN_ICON_IS_SMALLER = "S";
    /**
     * A scale option.
     */
    public static final String SCALE_OPTION_NEVER = "N";

    /**
     * Scale to fill with of annotation, disregarding aspect ratio.
     */
    public static final String SCALE_TYPE_ANAMORPHIC = "A";
    /**
     * Scale to fit width or height, smaller of two, while retaining aspect ration.
     */
    public static final String SCALE_TYPE_PROPORTIONAL = "P";



    /**
     * Default constructor.
     */
    public FDFIconFit()
    {
        fit = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param f The icon fit dictionary.
     */
    public FDFIconFit( COSDictionary f )
    {
        fit = f;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return fit;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return fit;
    }

    /**
     * This will get the scale option.  See the SCALE_OPTION_XXX constants.  This
     * is guaranteed to never return null.  Default: Always
     *
     * @return The scale option.
     */
    public String getScaleOption()
    {
        String retval =  fit.getNameAsString( "SW" );
        if( retval == null )
        {
            retval = SCALE_OPTION_ALWAYS;
        }
        return retval;
    }

    /**
     * This will set the scale option for the icon.  Set the SCALE_OPTION_XXX constants.
     *
     * @param option The scale option.
     */
    public void setScaleOption( String option )
    {
        fit.setName( "SW", option );
    }

    /**
     * This will get the scale type.  See the SCALE_TYPE_XXX constants.  This is
     * guaranteed to never return null.  Default: Proportional
     *
     * @return The scale type.
     */
    public String getScaleType()
    {
        String retval =  fit.getNameAsString( "S" );
        if( retval == null )
        {
            retval = SCALE_TYPE_PROPORTIONAL;
        }
        return retval;
    }

    /**
     * This will set the scale type.  See the SCALE_TYPE_XXX constants.
     *
     * @param scale The scale type.
     */
    public void setScaleType( String scale )
    {
        fit.setName( "S", scale );
    }

    /**
     * This is guaranteed to never return null.<br />
     *
     * To quote the PDF Spec
     * "An array of two numbers between 0.0 and 1.0 indicating the fraction of leftover
     * space to allocate at the left and bottom of the icon. A value of [0.0 0.0] positions the
     * icon at the bottom-left corner of the annotation rectangle; a value of [0.5 0.5] centers it
     * within the rectangle. This entry is used only if the icon is scaled proportionally. Default
     * value: [0.5 0.5]."
     *
     * @return The fractional space to allocate.
     */
    public PDRange getFractionalSpaceToAllocate()
    {
        PDRange retval = null;
        COSArray array = (COSArray)fit.getDictionaryObject( "A" );
        if( array == null )
        {
            retval = new PDRange();
            retval.setMin( .5f );
            retval.setMax( .5f );
            setFractionalSpaceToAllocate( retval );
        }
        else
        {
            retval = new PDRange( array );
        }
        return retval;
    }

    /**
     * This will set frational space to allocate.
     *
     * @param space The space to allocate.
     */
    public void setFractionalSpaceToAllocate( PDRange space )
    {
        fit.setItem( "A", space );
    }

    /**
     * This will tell if the icon should scale to fit the annotation bounds.  Default: false
     *
     * @return A flag telling if the icon should scale.
     */
    public boolean shouldScaleToFitAnnotation()
    {
        return fit.getBoolean( "FB", false );
    }

    /**
     * This will tell the icon to scale.
     *
     * @param value The flag value.
     */
    public void setScaleToFitAnnotation( boolean value )
    {
        fit.setBoolean( "FB", value );
    }
}