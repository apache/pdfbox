/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.common;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSNumber;

import org.fontbox.util.BoundingBox;

import java.awt.Dimension;

/**
 * This represents a rectangle in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.12 $
 */
public class PDRectangle implements COSObjectable
{
    private COSArray rectArray;

    /**
     * Constructor.
     *
     * Initializes to 0,0,0,0
     */
    public PDRectangle()
    {
        rectArray = new COSArray();
        rectArray.add( new COSFloat( 0.0f ) );
        rectArray.add( new COSFloat( 0.0f ) );
        rectArray.add( new COSFloat( 0.0f ) );
        rectArray.add( new COSFloat( 0.0f ) );
    }
    
    /**
     * Constructor.
     *
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public PDRectangle( float width, float height )
    {
        rectArray = new COSArray();
        rectArray.add( new COSFloat( 0.0f ) );
        rectArray.add( new COSFloat( 0.0f ) );
        rectArray.add( new COSFloat( width ) );
        rectArray.add( new COSFloat( height ) );
    }

    /**
     * Constructor.
     *
     * @param box The non PD bouding box.
     */
    public PDRectangle( BoundingBox box )
    {
        rectArray = new COSArray();
        rectArray.add( new COSFloat( box.getLowerLeftX() ) );
        rectArray.add( new COSFloat( box.getLowerLeftY() ) );
        rectArray.add( new COSFloat( box.getUpperRightX() ) );
        rectArray.add( new COSFloat( box.getUpperRightY() ) );
    }

    /**
     * Constructor.
     *
     * @param array An array of numbers as specified in the PDF Reference for a rectangle type.
     */
    public PDRectangle( COSArray array )
    {
        rectArray = array;
    }
    
    /**
     * Method to determine if the x/y point is inside this rectangle.
     * @param x The x-coordinate to test.
     * @param y The y-coordinate to test.
     * @return True if the point is inside this rectangle.
     */
    public boolean contains( float x, float y )
    {
        float llx = getLowerLeftX();
        float urx = getUpperRightX();
        float lly = getLowerLeftY();
        float ury = getUpperRightY();
        return x >= llx && x <= urx &&
               y >= lly && y <= ury;
    }

    /**
     * This will create a translated rectangle based off of this rectangle, such
     * that the new rectangle retains the same dimensions(height/width), but the
     * lower left x,y values are zero. <br />
     * 100, 100, 400, 400 (llx, lly, urx, ury ) <br />
     * will be translated to 0,0,300,300
     *
     * @return A new rectangle that has been translated back to the origin.
     */
    public PDRectangle createRetranslatedRectangle()
    {
        PDRectangle retval = new PDRectangle();
        retval.setUpperRightX( getWidth() );
        retval.setUpperRightY( getHeight() );
        return retval;
    }

    /**
     * This will get the underlying array for this rectangle.
     *
     * @return The cos array.
     */
    public COSArray getCOSArray()
    {
        return rectArray;
    }

    /**
     * This will get the lower left x coordinate.
     *
     * @return The lower left x.
     */
    public float getLowerLeftX()
    {
        return ((COSNumber)rectArray.get(0)).floatValue();
    }

    /**
     * This will set the lower left x coordinate.
     *
     * @param value The lower left x.
     */
    public void setLowerLeftX(float value)
    {
        rectArray.set(0, new COSFloat( value ) );
    }

    /**
     * This will get the lower left y coordinate.
     *
     * @return The lower left y.
     */
    public float getLowerLeftY()
    {
        return ((COSNumber)rectArray.get(1)).floatValue();
    }

    /**
     * This will set the lower left y coordinate.
     *
     * @param value The lower left y.
     */
    public void setLowerLeftY(float value)
    {
        rectArray.set(1, new COSFloat( value ) );
    }

    /**
     * This will get the upper right x coordinate.
     *
     * @return The upper right x .
     */
    public float getUpperRightX()
    {
        return ((COSNumber)rectArray.get(2)).floatValue();
    }

    /**
     * This will set the upper right x coordinate.
     *
     * @param value The upper right x .
     */
    public void setUpperRightX(float value)
    {
        rectArray.set(2, new COSFloat( value ) );
    }

    /**
     * This will get the upper right y coordinate.
     *
     * @return The upper right y.
     */
    public float getUpperRightY()
    {
        return ((COSNumber)rectArray.get(3)).floatValue();
    }

    /**
     * This will set the upper right y coordinate.
     *
     * @param value The upper right y.
     */
    public void setUpperRightY(float value)
    {
        rectArray.set(3, new COSFloat( value ) );
    }

    /**
     * This will get the width of this rectangle as calculated by
     * upperRightX - lowerLeftX.
     *
     * @return The width of this rectangle.
     */
    public float getWidth()
    {
        return getUpperRightX() - getLowerLeftX();
    }

    /**
     * This will get the height of this rectangle as calculated by
     * upperRightY - lowerLeftY.
     *
     * @return The height of this rectangle.
     */
    public float getHeight()
    {
        return getUpperRightY() - getLowerLeftY();
    }

    /**
     * A convenience method to create a dimension object for AWT operations.
     *
     * @return A dimension that matches the width and height of this rectangle.
     */
    public Dimension createDimension()
    {
        return new Dimension( (int)getWidth(), (int)getHeight() );
    }
    
    /**
    * This will move the rectangle the given relative amount.
    * 
    * @param horizontalAmount positive values will move rectangle to the right, negative's to the left.
    * @param verticalAmount positive values will move the rectangle up, negative's down.
    */
    public void move(float horizontalAmount, float verticalAmount) 
    {
        setUpperRightX(getUpperRightX() + horizontalAmount);
        setLowerLeftX(getLowerLeftX() + horizontalAmount);
        setUpperRightY(getUpperRightY() + verticalAmount);
        setLowerLeftY(getLowerLeftY() + verticalAmount);
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return rectArray;
    }


    /**
     * This will return a string representation of this rectangle.
     *
     * @return This object as a string.
     */
    public String toString()
    {
        return "[" + getLowerLeftX() + "," + getLowerLeftY() + "," +
                     getUpperRightX() + "," + getUpperRightY() +"]";
    }
}