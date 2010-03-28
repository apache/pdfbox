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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.PDRange;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import java.io.IOException;

/**
 * This class represents a Lab color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDLab extends PDColorSpace
{
    /**
     * The name of this color space.
     */
    public static final String NAME = "Lab";

    private COSArray array;
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDLab()
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add( COSName.LAB );
        array.add( dictionary );
    }

    /**
     * Constructor with array.
     *
     * @param lab The underlying color space.
     */
    public PDLab( COSArray lab )
    {
        array = lab;
        dictionary = (COSDictionary)array.getObject( 1 );
    }

    /**
     * This will return the name of the color space.
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return array;
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    protected ColorSpace createColorSpace() throws IOException
    {
        throw new IOException( "Not implemented" );
    }

    /**
     * Create a Java color model for this colorspace.
     *
     * @param bpc The number of bits per component.
     *
     * @return A color model that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel( int bpc ) throws IOException
    {
        throw new IOException( "Not implemented" );
    }

    /**
     * This will get the number of components that this color space is made up of.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        //BJL
        //hmm is this correct, I am not 100% sure.
        return 3;
    }

    /**
     * This will return the whitepoint tristimulus.  As this is a required field
     * this will never return null.  A default of 1,1,1 will be returned if the
     * pdf does not have any values yet.
     *
     * @return The whitepoint tristimulus.
     */
    public PDTristimulus getWhitepoint()
    {
        COSArray wp = (COSArray)dictionary.getDictionaryObject( COSName.WHITE_POINT );
        if( wp == null )
        {
            wp = new COSArray();
            wp.add( new COSFloat( 1.0f ) );
            wp.add( new COSFloat( 1.0f ) );
            wp.add( new COSFloat( 1.0f ) );
            dictionary.setItem( COSName.WHITE_POINT, wp );
        }
        return new PDTristimulus( wp );
    }

    /**
     * This will set the whitepoint tristimulus.  As this is a required field
     * this null should not be passed into this function.
     *
     * @param wp The whitepoint tristimulus.
     */
    public void setWhitepoint( PDTristimulus wp )
    {
        COSBase wpArray = wp.getCOSObject();
        if( wpArray != null )
        {
            dictionary.setItem( COSName.WHITE_POINT, wpArray );
        }
    }

    /**
     * This will return the BlackPoint tristimulus.  This is an optional field but
     * has defaults so this will never return null.
     * A default of 0,0,0 will be returned if the pdf does not have any values yet.
     *
     * @return The blackpoint tristimulus.
     */
    public PDTristimulus getBlackPoint()
    {
        COSArray bp = (COSArray)dictionary.getDictionaryObject( COSName.BLACK_POINT );
        if( bp == null )
        {
            bp = new COSArray();
            bp.add( new COSFloat( 0.0f ) );
            bp.add( new COSFloat( 0.0f ) );
            bp.add( new COSFloat( 0.0f ) );
            dictionary.setItem( COSName.BLACK_POINT, bp );
        }
        return new PDTristimulus( bp );
    }

    /**
     * This will set the BlackPoint tristimulus.  As this is a required field
     * this null should not be passed into this function.
     *
     * @param bp The BlackPoint tristimulus.
     */
    public void setBlackPoint( PDTristimulus bp )
    {

        COSBase bpArray = null;
        if( bp != null )
        {
            bpArray = bp.getCOSObject();
        }
        dictionary.setItem( COSName.BLACK_POINT, bpArray );
    }

    private COSArray getRangeArray()
    {
        COSArray range = (COSArray)dictionary.getDictionaryObject( COSName.RANGE );
        if( range == null )
        {
            range = new COSArray();
            dictionary.setItem( COSName.RANGE, array );
            range.add( new COSFloat( -100 ) );
            range.add( new COSFloat( 100 ) );
            range.add( new COSFloat( -100 ) );
            range.add( new COSFloat( 100 ) );
        }
        return range;
    }

    /**
     * This will get the valid range for the a component.  If none is found
     * then the default will be returned, which is -100 to 100.
     *
     * @return The a range.
     */
    public PDRange getARange()
    {
        COSArray range = getRangeArray();
        return new PDRange( range, 0 );
    }

    /**
     * This will set the a range for this color space.
     *
     * @param range The new range for the a component.
     */
    public void setARange( PDRange range )
    {
        COSArray rangeArray = null;
        //if null then reset to defaults
        if( range == null )
        {
            rangeArray = getRangeArray();
            rangeArray.set( 0, new COSFloat( -100 ) );
            rangeArray.set( 1, new COSFloat( 100 ) );
        }
        else
        {
            rangeArray = range.getCOSArray();
        }
        dictionary.setItem( COSName.RANGE, rangeArray );
    }

    /**
     * This will get the valid range for the b component.  If none is found
     * then the default will be returned, which is -100 to 100.
     *
     * @return The b range.
     */
    public PDRange getBRange()
    {
        COSArray range = getRangeArray();
        return new PDRange( range, 1 );
    }

    /**
     * This will set the b range for this color space.
     *
     * @param range The new range for the b component.
     */
    public void setBRange( PDRange range )
    {
        COSArray rangeArray = null;
        //if null then reset to defaults
        if( range == null )
        {
            rangeArray = getRangeArray();
            rangeArray.set( 2, new COSFloat( -100 ) );
            rangeArray.set( 3, new COSFloat( 100 ) );
        }
        else
        {
            rangeArray = range.getCOSArray();
        }
        dictionary.setItem( COSName.RANGE, rangeArray );
    }
}
