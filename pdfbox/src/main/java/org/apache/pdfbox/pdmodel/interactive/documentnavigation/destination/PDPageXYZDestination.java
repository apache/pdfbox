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

/**
 * This represents a destination to a page at an x,y coordinate with a zoom setting.
 * The default x,y,z will be whatever is the current value in the viewer application and
 * are not required.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
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
            array.set( 2, (COSBase)null );
        }
        else
        {
            array.setInt( 2, x );
        }
    }

    /**
     * Get the top y coordinate.  A return value of -1 implies that the current y-coordinate
     * will be used.
     *
     * @return The top y coordinate.
     */
    public int getTop()
    {
        return array.getInt( 3 );
    }

    /**
     * Set the top y-coordinate, a value of -1 implies that the current y-coordinate
     * will be used.
     * @param y The top ycoordinate.
     */
    public void setTop( int y )
    {
        array.growToSize( 4 );
        if( y == -1 )
        {
            array.set( 3, (COSBase)null );
        }
        else
        {
            array.setInt( 3, y );
        }
    }

    /**
     * Get the zoom value.  A return value of -1 implies that the current zoom
     * will be used.
     *
     * @return The zoom value for the page.
     */
    public int getZoom()
    {
        return array.getInt( 4 );
    }

    /**
     * Set the zoom value for the page, a value of -1 implies that the current zoom
     * will be used.
     * @param zoom The zoom value.
     */
    public void setZoom( int zoom )
    {
        array.growToSize( 5 );
        if( zoom == -1 )
        {
            array.set( 4, (COSBase)null );
        }
        else
        {
            array.setInt( 4, zoom );
        }
    }
}
