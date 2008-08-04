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
package org.pdfbox.pdmodel.graphics.color;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;

import org.pdfbox.cos.COSArray;

/**
 * This class represents a color space and the color value for that colorspace.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class PDColorSpaceInstance implements Cloneable
{
    private PDColorSpace colorSpace = new PDDeviceGray();
    private COSArray colorSpaceValue = new COSArray();
    
    /**
     * Default constructor.
     *
     */
    public PDColorSpaceInstance()
    {
        setColorSpaceValue( new float[] {0});
    }
    
    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDColorSpaceInstance retval = new PDColorSpaceInstance();
        retval.colorSpace = this.colorSpace;
        retval.colorSpaceValue.clear();
        retval.colorSpaceValue.addAll( this.colorSpaceValue );
        return retval;
    }
    
    /**
     * Create the current color from the colorspace and values.
     * @return The current awt color.
     * @throws IOException If there is an error creating the color.
     */
    public Color createColor() throws IOException
    {
        Color retval = null;
        float[] components = colorSpaceValue.toFloatArray();
        if( components.length == 3 )
        {
            //for some reason, when using RGB and the RGB colorspace
            //the new Color doesn't maintain exactly the same values
            //I think some color conversion needs to take place first
            //for now we will just make rgb a special case.
            retval = new Color( components[0], components[1], components[2] );
        }
        else
        {
            ColorSpace cs = colorSpace.createColorSpace();
            retval = new Color( cs, components, 1f );
        }
        return retval;
    }
    
    /**
     * Constructor with an existing color set.  Default colorspace is PDDeviceGray.
     * 
     * @param csValues The color space values.
     */
    public PDColorSpaceInstance( COSArray csValues )
    {
        colorSpaceValue = csValues;
    }


    /**
     * This will get the current colorspace.
     *
     * @return The current colorspace.
     */
    public PDColorSpace getColorSpace()
    {
        return colorSpace;
    }

    /**
     * This will set the current colorspace.
     *
     * @param value The new colorspace.
     */
    public void setColorSpace(PDColorSpace value)
    {
        colorSpace = value;
    }

    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public float[] getColorSpaceValue()
    {
        return colorSpaceValue.toFloatArray();
    }
    
    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public COSArray getCOSColorSpaceValue()
    {
        return colorSpaceValue;
    }

    /**
     * This will update the colorspace values.
     *
     * @param value The new colorspace values.
     */
    public void setColorSpaceValue(float[] value)
    {
        colorSpaceValue.setFloatArray( value );
    }
}
