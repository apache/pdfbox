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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSNumber;

import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * A tristimulus, or collection of three floating point parameters used for
 * color operations.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDTristimulus implements COSObjectable
{
    private COSArray values = null;

    /**
     * Constructor.  Defaults all values to 0, 0, 0.
     */
    public PDTristimulus()
    {
        values = new COSArray();
        values.add( new COSFloat( 0.0f ) );
        values.add( new COSFloat( 0.0f ) );
        values.add( new COSFloat( 0.0f ) );
    }

    /**
     * Constructor from COS object.
     *
     * @param array The array containing the XYZ values.
     */
    public PDTristimulus( COSArray array )
    {
        values = array;
    }

    /**
     * Constructor from COS object.
     *
     * @param array The array containing the XYZ values.
     */
    public PDTristimulus( float[] array )
    {
        values = new COSArray();
        for( int i=0; i<array.length && i<3; i++ )
        {
            values.add( new COSFloat( array[i] ) );
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return values;
    }

    /**
     * This will get the x value of the tristimulus.
     *
     * @return The X value.
     */
    public float getX()
    {
        return ((COSNumber)values.get( 0 )).floatValue();
    }

    /**
     * This will set the x value of the tristimulus.
     *
     * @param x The x value for the tristimulus.
     */
    public void setX( float x )
    {
        values.set( 0, new COSFloat( x ) );
    }

    /**
     * This will get the y value of the tristimulus.
     *
     * @return The Y value.
     */
    public float getY()
    {
        return ((COSNumber)values.get( 1 )).floatValue();
    }

    /**
     * This will set the y value of the tristimulus.
     *
     * @param y The y value for the tristimulus.
     */
    public void setY( float y )
    {
        values.set( 1, new COSFloat( y ) );
    }

    /**
     * This will get the z value of the tristimulus.
     *
     * @return The Z value.
     */
    public float getZ()
    {
        return ((COSNumber)values.get( 2 )).floatValue();
    }

    /**
     * This will set the z value of the tristimulus.
     *
     * @param z The z value for the tristimulus.
     */
    public void setZ( float z )
    {
        values.set( 2, new COSFloat( z ) );
    }
}