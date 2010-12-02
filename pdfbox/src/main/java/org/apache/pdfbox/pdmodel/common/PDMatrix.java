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
package org.apache.pdfbox.pdmodel.common;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;

/**
 * This class will be used for matrix manipulation.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDMatrix implements COSObjectable
{
    private COSArray matrix;
    // the number of row elements depends on the number of elements
    // within the given matrix
    // 3x3 e.g. Matrix of a CalRGB colorspace dictionary
    // 3x2 e.g. FontMatrix of a type 3 font
    private int numberOfRowElements = 3;
    
    /**
     * Constructor.
     */
    public PDMatrix()
    {
        matrix = new COSArray();
        matrix.add( new COSFloat( 1.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 1.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 0.0f ) );
        matrix.add( new COSFloat( 1.0f ) );
    }

    /**
     * Constructor.
     *
     * @param array The array that describes the matrix.
     */
    public PDMatrix( COSArray array )
    {
        if ( array.size() == 6) 
        {
            numberOfRowElements = 2;
        }
        matrix = array;
    }

    /**
     * This will get the underlying array value.
     *
     * @return The cos object that this object wraps.
     */
    public COSArray getCOSArray()
    {
        return matrix;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return matrix;
    }


    /**
     * This will get a matrix value at some point.
     *
     * @param row The row to get the value from.
     * @param column The column to get the value from.
     *
     * @return The value at the row/column position.
     */
    public float getValue( int row, int column )
    {
        return ((COSNumber)matrix.get( row*numberOfRowElements + column )).floatValue();
    }

    /**
     * This will set a value at a position.
     *
     * @param row The row to set the value at.
     * @param column the column to set the value at.
     * @param value The value to set at the position.
     */
    public void setValue( int row, int column, float value )
    {
        matrix.set( row*numberOfRowElements+column, new COSFloat( value ) );
    }
}
