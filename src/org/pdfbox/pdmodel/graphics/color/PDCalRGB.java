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
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSName;

import org.pdfbox.pdmodel.common.PDMatrix;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import java.io.IOException;

/**
 * This class represents a Cal RGB color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDCalRGB extends PDColorSpace
{
    /**
     * The name of this color space.
     */
    public static final String NAME = "CalRGB";

    private COSArray array;
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDCalRGB()
    {
        array = new COSArray();
        dictionary = new COSDictionary();
        array.add( COSName.getPDFName( NAME ) );
        array.add( dictionary );
    }

    /**
     * Constructor with array.
     *
     * @param rgb The underlying color space.
     */
    public PDCalRGB( COSArray rgb )
    {
        array = rgb;
        dictionary = (COSDictionary)array.getObject( 1 );
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
        return 3;
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
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return array;
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
        COSArray wp = (COSArray)dictionary.getDictionaryObject( COSName.getPDFName( "WhitePoint" ) );
        if( wp == null )
        {
            wp = new COSArray();
            wp.add( new COSFloat( 1.0f ) );
            wp.add( new COSFloat( 1.0f ) );
            wp.add( new COSFloat( 1.0f ) );
            dictionary.setItem( COSName.getPDFName( "WhitePoint" ), wp );
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
            dictionary.setItem( COSName.getPDFName( "WhitePoint" ), wpArray );
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
        COSArray bp = (COSArray)dictionary.getDictionaryObject( COSName.getPDFName( "BlackPoint" ) );
        if( bp == null )
        {
            bp = new COSArray();
            bp.add( new COSFloat( 0.0f ) );
            bp.add( new COSFloat( 0.0f ) );
            bp.add( new COSFloat( 0.0f ) );
            dictionary.setItem( COSName.getPDFName( "BlackPoint" ), bp );
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
        dictionary.setItem( COSName.getPDFName( "BlackPoint" ), bpArray );
    }

    /**
     * This will get the gamma value.  If none is present then the default of 1,1,1
     * will be returned.
     *
     * @return The gamma value.
     */
    public PDGamma getGamma()
    {
        COSArray gamma = (COSArray)dictionary.getDictionaryObject( COSName.getPDFName( "Gamma" ) );
        if( gamma == null )
        {
            gamma = new COSArray();
            gamma.add( new COSFloat( 1.0f ) );
            gamma.add( new COSFloat( 1.0f ) );
            gamma.add( new COSFloat( 1.0f ) );
            dictionary.setItem( COSName.getPDFName( "Gamma" ), gamma );
        }
        return new PDGamma( gamma );
    }

    /**
     * Set the gamma value.
     *
     * @param value The new gamma value.
     */
    public void setGamma( PDGamma value )
    {
        COSArray gamma = null;
        if( value != null )
        {
            gamma = value.getCOSArray();
        }
        dictionary.setItem( COSName.getPDFName( "Gamma" ), gamma );
    }

    /**
     * This will get the linear interpretation array.  This is guaranteed to not
     * return null.  If the underlying dictionary contains null then the identity
     * matrix will be returned.
     *
     * @return The linear interpretation matrix.
     */
    public PDMatrix getLinearInterpretation()
    {
        PDMatrix retval = null;
        COSArray matrix = (COSArray)dictionary.getDictionaryObject( COSName.getPDFName( "Matrix" ) );
        if( matrix == null )
        {
            retval = new PDMatrix();
            setLinearInterpretation( retval );
        }
        else
        {
            retval = new PDMatrix( matrix );
        }
        return retval;
    }

    /**
     * This will set the linear interpretation matrix.  Passing in null will
     * clear the matrix.
     *
     * @param matrix The new linear interpretation matrix.
     */
    public void setLinearInterpretation( PDMatrix matrix )
    {
        COSArray matrixArray = null;
        if( matrix != null )
        {
            matrixArray = matrix.getCOSArray();
        }
        dictionary.setItem( COSName.getPDFName( "Matrix" ), matrixArray );
    }
}