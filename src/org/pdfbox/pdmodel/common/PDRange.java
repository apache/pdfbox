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
package org.pdfbox.pdmodel.common;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSNumber;

/**
 * This class will be used to signify a range.  a(min) <= a* <= a(max)
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDRange implements COSObjectable
{
    private COSArray rangeArray;
    private int startingIndex;

    /**
     * Constructor with an initial range of 0..1.
     */
    public PDRange()
    {
        rangeArray = new COSArray();
        rangeArray.add( new COSFloat( 0.0f ) );
        rangeArray.add( new COSFloat( 1.0f ) );
        startingIndex = 0;
    }

    /**
     * Constructor assumes a starting index of 0.
     *
     * @param range The array that describes the range.
     */
    public PDRange( COSArray range )
    {
        rangeArray = range;
    }

    /**
     * Constructor with an index into an array.  Because some arrays specify
     * multiple ranges ie [ 0,1,  0,2,  2,3 ] It is convenient for this
     * class to take an index into an array.  So if you want this range to
     * represent 0,2 in the above example then you would say <code>new PDRange( array, 1 )</code>.
     *
     * @param range The array that describes the index
     * @param index The range index into the array for the start of the range.
     */
    public PDRange( COSArray range, int index )
    {
        rangeArray = range;
        startingIndex = index;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return rangeArray;
    }

    /**
     * This will get the underlying array value.
     *
     * @return The cos object that this object wraps.
     */
    public COSArray getCOSArray()
    {
        return rangeArray;
    }

    /**
     * This will get the minimum value of the range.
     *
     * @return The min value.
     */
    public float getMin()
    {
        COSNumber min = (COSNumber)rangeArray.getObject( startingIndex*2 );
        return min.floatValue();
    }

    /**
     * This will set the minimum value for the range.
     *
     * @param min The new minimum for the range.
     */
    public void setMin( float min )
    {
        rangeArray.set( startingIndex*2, new COSFloat( min ) );
    }

    /**
     * This will get the maximum value of the range.
     *
     * @return The max value.
     */
    public float getMax()
    {
        COSNumber max = (COSNumber)rangeArray.getObject( startingIndex*2+1 );
        return max.floatValue();
    }

    /**
     * This will set the maximum value for the range.
     *
     * @param max The new maximum for the range.
     */
    public void setMax( float max )
    {
        rangeArray.set( startingIndex*2+1, new COSFloat( max ) );
    }
}