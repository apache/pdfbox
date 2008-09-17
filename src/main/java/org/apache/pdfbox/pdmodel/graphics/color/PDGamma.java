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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A gamma array, or collection of three floating point parameters used for
 * color operations.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDGamma implements COSObjectable
{
    private COSArray values = null;

    /**
     * Constructor.  Defaults all values to 0, 0, 0.
     */
    public PDGamma()
    {
        values = new COSArray();
        values.add( new COSFloat( 0.0f ) );
        values.add( new COSFloat( 0.0f ) );
        values.add( new COSFloat( 0.0f ) );
    }

    /**
     * Constructor from COS object.
     *
     * @param array The array containing the XYZ values.
     */
    public PDGamma( COSArray array )
    {
        values = array;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return values;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSArray getCOSArray()
    {
        return values;
    }

    /**
     * This will get the r value of the tristimulus.
     *
     * @return The R value.
     */
    public float getR()
    {
        return ((COSNumber)values.get( 0 )).floatValue();
    }

    /**
     * This will set the r value of the tristimulus.
     *
     * @param r The r value for the tristimulus.
     */
    public void setR( float r )
    {
        values.set( 0, new COSFloat( r ) );
    }

    /**
     * This will get the g value of the tristimulus.
     *
     * @return The g value.
     */
    public float getG()
    {
        return ((COSNumber)values.get( 1 )).floatValue();
    }

    /**
     * This will set the g value of the tristimulus.
     *
     * @param g The g value for the tristimulus.
     */
    public void setG( float g )
    {
        values.set( 1, new COSFloat( g ) );
    }

    /**
     * This will get the b value of the tristimulus.
     *
     * @return The B value.
     */
    public float getB()
    {
        return ((COSNumber)values.get( 2 )).floatValue();
    }

    /**
     * This will set the b value of the tristimulus.
     *
     * @param b The b value for the tristimulus.
     */
    public void setB( float b )
    {
        values.set( 2, new COSFloat( b ) );
    }
}
