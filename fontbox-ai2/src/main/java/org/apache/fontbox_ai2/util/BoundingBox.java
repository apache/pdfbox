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
package org.apache.fontbox_ai2.util;


import java.util.List;

/**
 * This is an implementation of a bounding box.  This was originally written for the
 * AMF parser.
 *
 * @author Ben Litchfield
 */
public class BoundingBox
{
    private float lowerLeftX;
    private float lowerLeftY;
    private float upperRightX;
    private float upperRightY;

    /**
     * Default constructor.
     */
    public BoundingBox() 
    {
    }
    
    /**
     * Constructor.
     * 
     * @param minX lower left x value
     * @param minY lower left y value
     * @param maxX upper right x value
     * @param maxY upper right y value
     */
    public BoundingBox(float minX, float minY, float maxX, float maxY) 
    {
        lowerLeftX = minX;
        lowerLeftY = minY;
        upperRightX = maxX;
        upperRightY = maxY;
    }

    /**
     * Constructor.
     *
     * @param numbers list of four numbers
     */
    public BoundingBox(List<Number> numbers)
    {
        lowerLeftX = numbers.get(0).floatValue();
        lowerLeftY = numbers.get(1).floatValue();
        upperRightX = numbers.get(2).floatValue();
        upperRightY = numbers.get(3).floatValue();
    }

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
     * This will return a string representation of this rectangle.
     *
     * @return This object as a string.
     */
    @Override
    public String toString()
    {
        return "[" + getLowerLeftX() + "," + getLowerLeftY() + "," +
                     getUpperRightX() + "," + getUpperRightY() +"]";
    }

}