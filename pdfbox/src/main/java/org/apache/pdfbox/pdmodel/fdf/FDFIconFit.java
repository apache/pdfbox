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
package org.apache.pdfbox.pdmodel.fdf;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRange;

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
