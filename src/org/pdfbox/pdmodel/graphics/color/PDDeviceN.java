/**
 * Copyright (c) 2004, www.pdfbox.org
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
package org.pdfbox.pdmodel.graphics.color;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import java.io.IOException;

import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNull;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.function.PDFunction;

/**
 * This class represents a DeviceN color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDDeviceN extends PDColorSpace
{
    /**
     * The name of this color space.
     */
    public static final String NAME = "DeviceN";

    private COSArray array;

    /**
     * Constructor.
     */
    public PDDeviceN()
    {
        array = new COSArray();
        array.add( COSName.getPDFName( NAME ) );
        array.add( COSName.getPDFName( "" ) );
    }

    /**
     * Constructor.
     *
     * @param separation The array containing all separation information.
     */
    public PDDeviceN( COSArray separation )
    {
        array = separation;
    }

    /**
     * This will return the name of the color space.  For a PDSeparation object
     * this will always return "Separation"
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
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
        return getColorantNames().size();
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    public ColorSpace createColorSpace() throws IOException
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
     * This will get the colorant names.  A list of string objects.
     *
     * @return A list of colorants
     */
    public List getColorantNames()
    {
        COSArray names = (COSArray)array.getObject( 1 );
        return COSArrayList.convertCOSNameCOSArrayToList( names );
    }

    /**
     * This will set the list of colorants.
     *
     * @param names The list of colorant names.
     */
    public void setColorantNames( List names )
    {
        COSArray namesArray = COSArrayList.convertStringListToCOSNameCOSArray( names );
        array.set( 1, namesArray );
    }

    /**
     * This will get the alternate color space for this separation.
     *
     * @return The alternate color space.
     *
     * @throws IOException If there is an error getting the alternate color space.
     */
    public PDColorSpace getAlternateColorSpace() throws IOException
    {
        COSBase alternate = array.getObject( 2 );
        return PDColorSpaceFactory.createColorSpace( alternate );
    }

    /**
     * This will set the alternate color space.
     *
     * @param cs The alternate color space.
     */
    public void setAlternateColorSpace( PDColorSpace cs )
    {
        COSBase space = null;
        if( cs != null )
        {
            space = cs.getCOSObject();
        }
        array.set( 2, space );
    }

    /**
     * This will get the tint transform function.  
     *
     * @return The tint transform function.
     * 
     * @throws IOException if there is an error creating the function.
     */
    public PDFunction getTintTransform() throws IOException
    {
        return  PDFunction.create( array.getObject( 3 ) );
    }

    /**
     * This will set the tint transform function.  
     *
     * @param tint The tint transform function.
     */
    public void setTintTransform( PDFunction tint )
    {
        array.set( 3, tint );
    }

    /**
     * This will get the attributes that are associated with the deviceN
     * color space.
     *
     * @return The DeviceN attributes.
     */
    public PDDeviceNAttributes getAttributes()
    {
        PDDeviceNAttributes retval = null;
        if( array.size() <5)
        {
            retval = new PDDeviceNAttributes();
            setAttributes( retval );
        }
        return retval;
    }

    /**
     * This will set the color space attributes.  If null is passed in then
     * all attribute will be removed.
     *
     * @param attributes The color space attributes.
     */
    public void setAttributes( PDDeviceNAttributes attributes )
    {
        if( attributes == null )
        {
            array.remove( 4 );
        }
        else
        {
            //make sure array is large enough
            while( array.size() < 5 )
            {
                array.add( COSNull.NULL );
            }
            array.set( 4, attributes.getCOSDictionary() );
        }
    }
}