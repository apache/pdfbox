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
import java.awt.color.ICC_ColorSpace;
import java.io.IOException;
import java.io.OutputStream;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a color space in a pdf document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.11 $
 */
public final class PDColorSpaceFactory
{
    /**
     * Private constructor for utility classes.
     */
    private PDColorSpaceFactory()
    {
        //utility class should not be implemented
    }

    /**
     * This will create the correct color space given the name.
     *
     * @param colorSpace The color space object.
     *
     * @return The color space.
     *
     * @throws IOException If the color space name is unknown.
     */
    public static PDColorSpace createColorSpace( COSBase colorSpace ) throws IOException
    {
        PDColorSpace retval = null;
        if( colorSpace instanceof COSName )
        {
            retval = createColorSpace( ((COSName)colorSpace).getName() );
        }
        else if( colorSpace instanceof COSArray )
        {
            COSArray array = (COSArray)colorSpace;
            COSName type = (COSName)array.getObject( 0 );
            if( type.getName().equals( PDCalGray.NAME ) )
            {
                retval = new PDCalGray( array );
            }
            else if( type.getName().equals( PDCalRGB.NAME ) )
            {
                retval = new PDCalRGB( array );
            }
            else if( type.getName().equals( PDDeviceN.NAME ) )
            {
                retval = new PDDeviceN( array );
            }
            else if( type.getName().equals( PDIndexed.NAME ) ||
                   type.getName().equals( PDIndexed.ABBREVIATED_NAME ))
            {
                retval = new PDIndexed( array );
            }
            else if( type.getName().equals( PDLab.NAME ) )
            {
                retval = new PDLab( array );
            }
            else if( type.getName().equals( PDSeparation.NAME ) )
            {
                retval = new PDSeparation( array );
            }
            else if( type.getName().equals( PDICCBased.NAME ) )
            {
                retval = new PDICCBased( array );
            }
            else if( type.getName().equals( PDPattern.NAME ) )
            {
                retval = new PDPattern( array );
            }
            else
            {
                throw new IOException( "Unknown colorspace array type:" + type );
            }
        }
        else
        {
            throw new IOException( "Unknown colorspace type:" + colorSpace );
        }
        return retval;
    }

    /**
     * This will create the correct color space given the name.
     *
     * @param colorSpaceName The name of the colorspace.
     *
     * @return The color space.
     *
     * @throws IOException If the color space name is unknown.
     */
    public static PDColorSpace createColorSpace( String colorSpaceName ) throws IOException
    {
        PDColorSpace cs = null;
        if( colorSpaceName.equals( PDDeviceCMYK.NAME ) ||
                 colorSpaceName.equals( PDDeviceCMYK.ABBREVIATED_NAME ) )
        {
            cs = PDDeviceCMYK.INSTANCE;
        }
        else if( colorSpaceName.equals( PDDeviceRGB.NAME ) ||
                 colorSpaceName.equals( PDDeviceRGB.ABBREVIATED_NAME ) )
        {
            cs = PDDeviceRGB.INSTANCE;
        }
        else if( colorSpaceName.equals( PDDeviceGray.NAME ) ||
                 colorSpaceName.equals( PDDeviceGray.ABBREVIATED_NAME ))
        {
            cs = new PDDeviceGray();
        }
        else if( colorSpaceName.equals( PDLab.NAME ) )
        {
            cs = new PDLab();
        }
        else if( colorSpaceName.equals( PDPattern.NAME ) )
        {
            cs = new PDPattern();
        }
        else
        {
            throw new IOException( "Error: Unknown colorspace '" + colorSpaceName + "'" );
        }
        return cs;
    }
    
    /**
     * This will create the correct color space from a java colorspace.
     *
     * @param doc The doc to potentiall write information to.
     * @param cs The awt colorspace.
     *
     * @return The color space.
     *
     * @throws IOException If the color space name is unknown.
     */
    public static PDColorSpace createColorSpace( PDDocument doc, ColorSpace cs ) throws IOException
    {
        PDColorSpace retval = null;
        if( cs.isCS_sRGB() )
        {
            retval = PDDeviceRGB.INSTANCE;
        }
        else if( cs instanceof ICC_ColorSpace )
        {
            ICC_ColorSpace ics = (ICC_ColorSpace)cs;
            PDICCBased pdCS = new PDICCBased( doc );
            retval = pdCS;
            COSArray ranges = new COSArray();
            for( int i=0; i<cs.getNumComponents(); i++ )
            {
                ranges.add( new COSFloat( ics.getMinValue( i ) ) );
                ranges.add( new COSFloat( ics.getMaxValue( i ) ) );       
            }
            PDStream iccData = pdCS.getPDStream();
            OutputStream output = null;
            try
            {
                output = iccData.createOutputStream();
                output.write( ics.getProfile().getData() );
            }
            finally
            {
                if( output != null )
                {
                    output.close();
                }
            }
            pdCS.setNumberOfComponents( cs.getNumComponents() );
        }
        else
        {
            throw new IOException( "Not yet implemented:" + cs );
        }
        return retval;
    }
}