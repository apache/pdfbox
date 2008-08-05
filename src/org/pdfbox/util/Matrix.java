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
package org.pdfbox.util;

import java.awt.geom.AffineTransform;

/**
 * This class will be used for matrix manipulation.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public class Matrix implements Cloneable
{
    private float[] single =
    {
        1,0,0,
        0,1,0,
        0,0,1
    };

    /**
     * Constructor.
     */
    public Matrix()
    {
        //default constructor
    }

    /**
     * Create an affine transform from this matrix's values.
     *
     * @return An affine transform with this matrix's values.
     */
    public AffineTransform createAffineTransform()
    {
        AffineTransform retval = new AffineTransform(
            single[0], single[1],
            single[3], single[4],
            single[6], single[7] );
        return retval;
    }

    /**
     * Set the values of the matrix from the AffineTransform.
     *
     * @param af The transform to get the values from.
     */
    public void setFromAffineTransform( AffineTransform af )
    {
        single[0] = (float)af.getScaleX();
        single[1] = (float)af.getShearY();
        single[3] = (float)af.getShearX();
        single[4] = (float)af.getScaleY();
        single[6] = (float)af.getTranslateX();
        single[7] = (float)af.getTranslateY();
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
        return single[row*3+column];
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
        single[row*3+column] = value;
    }

    /**
     * Return a single dimension array of all values in the matrix.
     *
     * @return The values ot this matrix.
     */
    public float[][] getValues()
    {
        float[][] retval = new float[3][3];
        retval[0][0] = single[0];
        retval[0][1] = single[1];
        retval[0][2] = single[2];
        retval[1][0] = single[3];
        retval[1][1] = single[4];
        retval[1][2] = single[5];
        retval[2][0] = single[6];
        retval[2][1] = single[7];
        retval[2][2] = single[8];
        return retval;
    }

    /**
     * Return a single dimension array of all values in the matrix.
     *
     * @return The values ot this matrix.
     */
    public double[][] getValuesAsDouble()
    {
        double[][] retval = new double[3][3];
        retval[0][0] = single[0];
        retval[0][1] = single[1];
        retval[0][2] = single[2];
        retval[1][0] = single[3];
        retval[1][1] = single[4];
        retval[1][2] = single[5];
        retval[2][0] = single[6];
        retval[2][1] = single[7];
        retval[2][2] = single[8];
        return retval;
    }

    /**
     * This will take the current matrix and multipy it with a matrix that is passed in.
     *
     * @param b The matrix to multiply by.
     *
     * @return The result of the two multiplied matrices.
     */
    public Matrix multiply( Matrix b )
    {
        Matrix result = new Matrix();

        if (b != null && b.single != null) {
        float[] bMatrix = b.single;
        float[] resultMatrix = result.single;
        resultMatrix[0] = single[0] * bMatrix[0] + single[1] * bMatrix[3] + single[2] * bMatrix[6];
        resultMatrix[1] = single[0] * bMatrix[1] + single[1] * bMatrix[4] + single[2] * bMatrix[7];
        resultMatrix[2] = single[0] * bMatrix[2] + single[1] * bMatrix[5] + single[2] * bMatrix[8];
        resultMatrix[3] = single[3] * bMatrix[0] + single[4] * bMatrix[3] + single[5] * bMatrix[6];
        resultMatrix[4] = single[3] * bMatrix[1] + single[4] * bMatrix[4] + single[5] * bMatrix[7];
        resultMatrix[5] = single[3] * bMatrix[2] + single[4] * bMatrix[5] + single[5] * bMatrix[8];
        resultMatrix[6] = single[6] * bMatrix[0] + single[7] * bMatrix[3] + single[8] * bMatrix[6];
        resultMatrix[7] = single[6] * bMatrix[1] + single[7] * bMatrix[4] + single[8] * bMatrix[7];
        resultMatrix[8] = single[6] * bMatrix[2] + single[7] * bMatrix[5] + single[8] * bMatrix[8];
        }
        return result;
    }

    /**
     * Create a new matrix with just the scaling operators.
     *
     * @return A new matrix with just the scaling operators.
     */
    public Matrix extractScaling()
    {
        Matrix retval = new Matrix();

        retval.single[0] = this.single[0];
        retval.single[4] = this.single[4];

        return retval;
    }

    /**
     * Convenience method to create a scaled instance.
     *
     * @param x The xscale operator.
     * @param y The yscale operator.
     * @return A new matrix with just the x/y scaling
     */
    public static Matrix getScaleInstance( float x, float y)
    {
        Matrix retval = new Matrix();

        retval.single[0] = x;
        retval.single[4] = y;

        return retval;
    }

    /**
     * Create a new matrix with just the translating operators.
     *
     * @return A new matrix with just the translating operators.
     */
    public Matrix extractTranslating()
    {
        Matrix retval = new Matrix();

        retval.single[6] = this.single[6];
        retval.single[7] = this.single[7];

        return retval;
    }

    /**
     * Convenience method to create a translating instance.
     *
     * @param x The x translating operator.
     * @param y The y translating operator.
     * @return A new matrix with just the x/y translating.
     */
    public static Matrix getTranslatingInstance( float x, float y)
    {
        Matrix retval = new Matrix();

        retval.single[6] = x;
        retval.single[7] = y;

        return retval;
    }

    /**
     * Clones this object.
     * @return cloned matrix as an object.
     */
    public Object clone()
    {
        Matrix clone = new Matrix();
        System.arraycopy( single, 0, clone.single, 0, 9 );
        return clone;
    }

    /**
     * This will copy the text matrix data.
     *
     * @return a matrix that matches this one.
     */
    public Matrix copy()
    {
        return (Matrix) clone();
    }

    /**
     * This will return a string representation of the matrix.
     *
     * @return The matrix as a string.
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer( "" );
        result.append( "[[" );
        result.append( single[0] + "," );
        result.append( single[1] + "," );
        result.append( single[2] + "][");
        result.append( single[3] + "," );
        result.append( single[4] + "," );
        result.append( single[5] + "][");
        result.append( single[6] + "," );
        result.append( single[7] + "," );
        result.append( single[8] + "]]");

        return result.toString();
    }

    /**
     * Get the xscaling factor of this matrix.
     * @return The x-scale.
     */
    public float getXScale()
    {
        float xScale = single[0];

        /**
         * BM: if the trm is rotated, the calculation is a little more complicated
         *
         * The rotation matrix multiplied with the scaling matrix is:
         * (   x   0   0)    ( cos  sin  0)    ( x*cos x*sin   0)
         * (   0   y   0) *  (-sin  cos  0)  = (-y*sin y*cos   0)
         * (   0   0   1)    (   0    0  1)    (     0     0   1)
         *
         * So, if you want to deduce x from the matrix you take
         * M(0,0) = x*cos and M(0,1) = x*sin and use the theorem of Pythagoras
         *
         * sqrt(M(0,0)^2+M(0,1)^2) =
         * sqrt(x2*cos2+x2*sin2) =
         * sqrt(x2*(cos2+sin2)) = <- here is the trick cos2+sin2 is one
         * sqrt(x2) =
         * abs(x)
         */
        if( !(single[1]==0.0f && single[3]==0.0f) )
        {
            xScale = (float)Math.sqrt(Math.pow(single[0], 2)+
                                      Math.pow(single[1], 2));
        }
        return xScale;
    }

    /**
     * Get the y scaling factor of this matrix.
     * @return The y-scale factor.
     */
    public float getYScale()
    {
        float yScale = single[4];
        if( !(single[1]==0.0f && single[3]==0.0f) )
        {
            yScale = (float)Math.sqrt(Math.pow(single[3], 2)+
                                      Math.pow(single[4], 2));
        }
        return yScale;
    }

    /**
     * Get the x position in the matrix.
     * @return The x-position.
     */
    public float getXPosition()
    {
        return single[6];
    }

    /**
     * Get the y position.
     * @return The y position.
     */
    public float getYPosition()
    {
        return single[7];
    }
}
