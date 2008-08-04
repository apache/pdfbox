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
 * This represents a destination to a page at a x location and the height is magnified
 * to just fit on the screen.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
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
            array.set( 2, (COSBase)null );
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
