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
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;

/**
 * This represents a destination to a page at an x,y coordinate with a zoom setting.
 * The default x,y,z will be whatever is the current value in the viewer application and
 * are not required.
 *
 * @author Ben Litchfield
 */
public class PDPageXYZDestination extends PDPageDestination
{
    /**
     * The type of this destination.
     */
    protected static final String TYPE = "XYZ";

    /**
     * Default constructor.
     *
     */
    public PDPageXYZDestination()
    {
        super();
        array.growToSize(5);
        array.setName( 1, TYPE );
    }

    /**
     * Constructor from an existing destination array.
     *
     * @param arr The destination array.
     */
    public PDPageXYZDestination( COSArray arr )
    {
        super( arr );
    }

    /**
     * Get the left x coordinate.  Return values of 0 or -1 imply that the current x-coordinate
     * will be used.
     *
     * @return The left x coordinate.
     */
    public int getLeft()
    {
        return array.getInt( 2 );
    }

    /**
     * Set the left x-coordinate, values 0 or -1 imply that the current x-coordinate
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
     * Get the top y coordinate.  Return values of 0 or -1 imply that the current y-coordinate
     * will be used.
     *
     * @return The top y coordinate.
     */
    public int getTop()
    {
        return array.getInt( 3 );
    }

    /**
     * Set the top y-coordinate, values 0 or -1 imply that the current y-coordinate
     * will be used.
     * @param y The top ycoordinate.
     */
    public void setTop( int y )
    {
        array.growToSize( 4 );
        if( y == -1 )
        {
            array.set(3, null);
        }
        else
        {
            array.setInt( 3, y );
        }
    }

    /**
     * Get the zoom value.  Return values of 0 or -1 imply that the current zoom
     * will be used.
     *
     * @return The zoom value for the page.
     */
    public float getZoom()
    {
        COSBase obj = array.getObject(4);
        if (obj instanceof COSNumber)
        {
            return ((COSNumber) obj).floatValue();
        }
        return -1;
    }

    /**
     * Set the zoom value for the page, values 0 or -1 imply that the current zoom
     * will be used.
     * @param zoom The zoom value.
     */
    public void setZoom( float zoom )
    {
        array.growToSize( 5 );
        if( zoom == -1 )
        {
            array.set(4, null);
        }
        else
        {
            array.set( 4, new COSFloat(zoom) );
        }
    }
}
