/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.interactive.documentnavigation.destination;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;

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
