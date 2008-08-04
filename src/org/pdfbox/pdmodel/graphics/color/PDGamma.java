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
 * A gamma array, or collection of three floating point parameters used for
 * color operations.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDGamma implements COSObjectable
{
    private COSArray values = null;

    /**
     * Constructor.  Defaults all values to 0, 0, 0.
     */
    public PDGamma()
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
    public PDGamma( COSArray array )
    {
        values = array;
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
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSArray getCOSArray()
    {
        return values;
    }

    /**
     * This will get the r value of the tristimulus.
     *
     * @return The R value.
     */
    public float getR()
    {
        return ((COSNumber)values.get( 0 )).floatValue();
    }

    /**
     * This will set the r value of the tristimulus.
     *
     * @param r The r value for the tristimulus.
     */
    public void setR( float r )
    {
        values.set( 0, new COSFloat( r ) );
    }

    /**
     * This will get the g value of the tristimulus.
     *
     * @return The g value.
     */
    public float getG()
    {
        return ((COSNumber)values.get( 1 )).floatValue();
    }

    /**
     * This will set the g value of the tristimulus.
     *
     * @param g The g value for the tristimulus.
     */
    public void setG( float g )
    {
        values.set( 1, new COSFloat( g ) );
    }

    /**
     * This will get the b value of the tristimulus.
     *
     * @return The B value.
     */
    public float getB()
    {
        return ((COSNumber)values.get( 2 )).floatValue();
    }

    /**
     * This will set the b value of the tristimulus.
     *
     * @param b The b value for the tristimulus.
     */
    public void setB( float b )
    {
        values.set( 2, new COSFloat( b ) );
    }
}