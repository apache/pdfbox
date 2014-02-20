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
 * A tristimulus, or collection of three floating point parameters used for color operations.
 *
 * @author Ben Litchfield
 */
public final class PDTristimulus implements COSObjectable
{
    private COSArray values = null;

    /**
     * Constructor. Defaults all values to 0, 0, 0.
     */
    public PDTristimulus()
    {
        values = new COSArray();
        values.add(new COSFloat(0.0f));
        values.add(new COSFloat(0.0f));
        values.add(new COSFloat(0.0f));
    }

    /**
     * Constructor from COS object.
     * @param array the array containing the XYZ values
     */
    public PDTristimulus(COSArray array)
    {
        values = array;
    }

    /**
     * Constructor from COS object.
     * @param array the array containing the XYZ values
     */
    public PDTristimulus(float[] array)
    {
        values = new COSArray();
        for(int i=0; i<array.length && i<3; i++)
        {
            values.add(new COSFloat(array[i]));
        }
    }

    /**
     * Convert this standard java object to a COS object.
     * @return the cos object that matches this Java object
     */
    public COSBase getCOSObject()
    {
        return values;
    }

    /**
     * Returns the x value of the tristimulus.
     * @return the X value
     */
    public float getX()
    {
        return ((COSNumber)values.get(0)).floatValue();
    }

    /**
     * Sets the x value of the tristimulus.
     * @param x the x value for the tristimulus
     */
    public void setX(float x)
    {
        values.set(0, new COSFloat(x));
    }

    /**
     * Returns the y value of the tristimulus.
     * @return the Y value
     */
    public float getY()
    {
        return ((COSNumber)values.get(1)).floatValue();
    }

    /**
     * Sets the y value of the tristimulus.
     * @param y the y value for the tristimulus
     */
    public void setY(float y)
    {
        values.set(1, new COSFloat(y));
    }

    /**
     * Returns the z value of the tristimulus.
     * @return the Z value
     */
    public float getZ()
    {
        return ((COSNumber)values.get(2)).floatValue();
    }

    /**
     * Sets the z value of the tristimulus.
     * @param z the z value for the tristimulus
     */
    public void setZ(float z)
    {
        values.set(2, new COSFloat(z));
    }
}
