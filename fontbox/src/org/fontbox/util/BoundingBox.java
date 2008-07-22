/**
 * Copyright (c) 2005, www.fontbox.org
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
 * 3. Neither the name of fontbox; nor the names of its
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
 * http://www.fontbox.org
 *
 */
package org.fontbox.util;

import java.awt.Point;

/**
 * This is an implementation of a bounding box.  This was originally written for the
 * AMF parser.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class BoundingBox
{
    private float lowerLeftX;
    private float lowerLeftY;
    private float upperRightX;
    private float upperRightY;

    /**
     * Getter for property lowerLeftX.
     *
     * @return Value of property lowerLeftX.
     */
    public float getLowerLeftX()
    {
        return lowerLeftX;
    }

    /**
     * Setter for property lowerLeftX.
     *
     * @param lowerLeftXValue New value of property lowerLeftX.
     */
    public void setLowerLeftX(float lowerLeftXValue)
    {
        this.lowerLeftX = lowerLeftXValue;
    }

    /**
     * Getter for property lowerLeftY.
     *
     * @return Value of property lowerLeftY.
     */
    public float getLowerLeftY()
    {
        return lowerLeftY;
    }

    /**
     * Setter for property lowerLeftY.
     *
     * @param lowerLeftYValue New value of property lowerLeftY.
     */
    public void setLowerLeftY(float lowerLeftYValue)
    {
        this.lowerLeftY = lowerLeftYValue;
    }

    /**
     * Getter for property upperRightX.
     *
     * @return Value of property upperRightX.
     */
    public float getUpperRightX()
    {
        return upperRightX;
    }

    /**
     * Setter for property upperRightX.
     *
     * @param upperRightXValue New value of property upperRightX.
     */
    public void setUpperRightX(float upperRightXValue)
    {
        this.upperRightX = upperRightXValue;
    }

    /**
     * Getter for property upperRightY.
     *
     * @return Value of property upperRightY.
     */
    public float getUpperRightY()
    {
        return upperRightY;
    }

    /**
     * Setter for property upperRightY.
     *
     * @param upperRightYValue New value of property upperRightY.
     */
    public void setUpperRightY(float upperRightYValue)
    {
        this.upperRightY = upperRightYValue;
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
     * Checks if a point is inside this rectangle.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * 
     * @return true If the point is on the edge or inside the rectangle bounds. 
     */
    public boolean contains( float x, float y )
    {
        return x >= lowerLeftX && x <= upperRightX &&
               y >= lowerLeftY && y <= upperRightY;
    }
    
    /**
     * Checks if a point is inside this rectangle.
     * 
     * @param point The point to check
     * 
     * @return true If the point is on the edge or inside the rectangle bounds. 
     */
    public boolean contains( Point point )
    {
        return contains( (float)point.getX(), (float)point.getY() );
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