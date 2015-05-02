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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination;

import org.apache.pdfbox.cos.COSArray;

/**
 * This represents a destination to a page at a x location and the height is magnified
 * to just fit on the screen.
 *
 * @author Ben Litchfield
 */
public class PDPageFitHeightDestination extends PDPageDestination
{
    /**
     * The type of this destination.
     */
    protected static final String TYPE = "FitV";
    /**
     * The type of this destination.
     */
    protected static final String TYPE_BOUNDED = "FitBV";

    /**
     * Default constructor.
     *
     */
    public PDPageFitHeightDestination()
    {
        super();
        array.growToSize(3);
        array.setName( 1, TYPE );

    }

    /**
     * Constructor from an existing destination array.
     *
     * @param arr The destination array.
     */
    public PDPageFitHeightDestination( COSArray arr )
    {
        super( arr );
    }

    /**
     * Get the left x coordinate.  A return value of -1 implies that the current x-coordinate
     * will be used.
     *
     * @return The left x coordinate.
     */
    public int getLeft()
    {
        return array.getInt( 2 );
    }

    /**
     * Set the left x-coordinate, a value of -1 implies that the current x-coordinate
     * will be used.
     * @param x The left x coordinate.
     */
    public void setLeft( int x )
    {
        array.growToSize( 3 );
        if( x == -1 )
        {
            array.set(2, null);
        }
        else
        {
            array.setInt( 2, x );
        }
    }

    /**
     * A flag indicating if this page destination should just fit bounding box of the PDF.
     *
     * @return true If the destination should fit just the bounding box.
     */
    public boolean fitBoundingBox()
    {
        return TYPE_BOUNDED.equals( array.getName( 1 ) );
    }

    /**
     * Set if this page destination should just fit the bounding box.  The default is false.
     *
     * @param fitBoundingBox A flag indicating if this should fit the bounding box.
     */
    public void setFitBoundingBox( boolean fitBoundingBox )
    {
        array.growToSize( 2 );
        if( fitBoundingBox )
        {
            array.setName( 1, TYPE_BOUNDED );
        }
        else
        {
            array.setName( 1, TYPE );
        }
    }
}
