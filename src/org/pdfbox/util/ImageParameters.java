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
package org.pdfbox.util;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;

import java.io.IOException;
import java.util.List;

/**
 * This contains all of the image parameters for in inlined image.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class ImageParameters
{
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public ImageParameters()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param params The image parameters.
     */
    public ImageParameters( COSDictionary params )
    {
        dictionary = params;
    }
    
    /**
     * This will get the dictionary that stores the image parameters.
     * 
     * @return The COS dictionary that stores the image parameters.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    private COSBase getCOSObject( String abbreviatedName, String name )
    {
        COSBase retval = dictionary.getDictionaryObject( COSName.getPDFName( abbreviatedName ) );
        if( retval == null )
        {
            retval = dictionary.getDictionaryObject( COSName.getPDFName( name ) );
        }
        return retval;
    }

    private int getNumberOrNegativeOne( String abbreviatedName, String name )
    {
        int retval = -1;
        COSNumber number = (COSNumber)getCOSObject( abbreviatedName, name );
        if( number != null )
        {
            retval = number.intValue();
        }
        return retval;
    }

    /**
     * The bits per component of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getNumberOrNegativeOne( "BPC", "BitsPerComponent" );
    }

    /**
     * Set the number of bits per component.
     *
     * @param bpc The number of bits per component.
     */
    public void setBitsPerComponent( int bpc )
    {
        dictionary.setItem( COSName.getPDFName( "BPC" ), new COSInteger( bpc ) );
    }


    /**
     * This will get the color space or null if none exists.
     *
     * @return The color space for this image.
     *
     * @throws IOException If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        COSBase cs = getCOSObject( "CS", "ColorSpace" );
        PDColorSpace retval = null;
        if( cs != null )
        {
            retval = PDColorSpaceFactory.createColorSpace( cs );
        }
        return retval;
    }

    /**
     * This will set the color space for this image.
     *
     * @param cs The color space for this image.
     */
    public void setColorSpace( PDColorSpace cs )
    {
        COSBase base = null;
        if( cs != null )
        {
            base = cs.getCOSObject();
        }
        dictionary.setItem( COSName.getPDFName( "CS" ), base );
    }

    /**
     * The height of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The height.
     */
    public int getHeight()
    {
        return getNumberOrNegativeOne( "H", "Height" );
    }

    /**
     * Set the height of the image.
     *
     * @param h The height of the image.
     */
    public void setHeight( int h )
    {
        dictionary.setItem( COSName.getPDFName( "H" ), new COSInteger( h ) );
    }

    /**
     * The width of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The width.
     */
    public int getWidth()
    {
        return getNumberOrNegativeOne( "W", "Width" );
    }

    /**
     * Set the width of the image.
     *
     * @param w The width of the image.
     */
    public void setWidth( int w )
    {
        dictionary.setItem( COSName.getPDFName( "W" ), new COSInteger( w ) );
    }
    
    /**
     * This will get the list of filters that are associated with this stream.  Or
     * null if there are none.
     * @return A list of all encoding filters to apply to this stream.
     */
    public List getFilters()
    {
        List retval = null;
        COSBase filters = dictionary.getDictionaryObject( new String[] {"Filter", "F"} );
        if( filters instanceof COSName )
        {
            COSName name = (COSName)filters;
            retval = new COSArrayList( name.getName(), name, dictionary, "Filter" );
        }
        else if( filters instanceof COSArray )
        {
            retval = COSArrayList.convertCOSNameCOSArrayToList( (COSArray)filters );
        }
        return retval;
    }
    
    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters The filters that are part of this stream.
     */
    public void setFilters( List filters )
    {
        COSBase obj = COSArrayList.convertStringListToCOSNameCOSArray( filters );
        dictionary.setItem( "Filter", obj );
    }
}