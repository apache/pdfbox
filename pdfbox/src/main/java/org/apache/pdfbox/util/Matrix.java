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
package org.apache.pdfbox.util;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * This class will be used for matrix manipulation.
 *
 * @author Ben Litchfield
 */
public final class Matrix implements Cloneable
{
    static final float[] DEFAULT_SINGLE =
    {
        1,0,0,  //  a  b  0     sx hy 0    note: hx and hy are reversed vs. the PDF spec as we use
        0,1,0,  //  c  d  0  =  hx sy 0          AffineTransform's definition x and y shear
        0,0,1   //  tx ty 1     tx ty 1
    };

    private final float[] single;

    /**
     * Constructor. This produces an identity matrix.
     */
    public Matrix()
    {
        single = new float[DEFAULT_SINGLE.length];
        System.arraycopy(DEFAULT_SINGLE, 0, single, 0, DEFAULT_SINGLE.length);
    }

    /**
     * Creates a matrix from a 6-element (a b c d e f) COS array.
     *
     * @param array
     */
    public Matrix(COSArray array)
    {
        single = new float[DEFAULT_SINGLE.length];
        single[0] = ((COSNumber)array.get(0)).floatValue();
        single[1] = ((COSNumber)array.get(1)).floatValue();
        single[3] = ((COSNumber)array.get(2)).floatValue();
        single[4] = ((COSNumber)array.get(3)).floatValue();
        single[6] = ((COSNumber)array.get(4)).floatValue();
        single[7] = ((COSNumber)array.get(5)).floatValue();
        single[8] = 1;
    }

    /**
     * Creates a transformation matrix with the given 6 elements. Transformation matrices are
     * discussed in 8.3.3, "Common Transformations" and 8.3.4, "Transformation Matrices" of the PDF
     * specification. For simple purposes (rotate, scale, translate) it is recommended to use the
     * static methods below.
     *
     * @see Matrix#getRotateInstance(double, float, float)
     * @see Matrix#getScaleInstance(float, float)
     * @see Matrix#getTranslateInstance(float, float)
     *
     * @param a the X coordinate scaling element (m00) of the 3x3 matrix
     * @param b the Y coordinate shearing element (m10) of the 3x3 matrix
     * @param c the X coordinate shearing element (m01) of the 3x3 matrix
     * @param d the Y coordinate scaling element (m11) of the 3x3 matrix
     * @param e the X coordinate translation element (m02) of the 3x3 matrix
     * @param f the Y coordinate translation element (m12) of the 3x3 matrix
     */
    public Matrix(float a, float b, float c, float d, float e, float f)
    {
        single = new float[DEFAULT_SINGLE.length];
        single[0] = a;
        single[1] = b;
        single[3] = c;
        single[4] = d;
        single[6] = e;
        single[7] = f;
        single[8] = 1;
    }

    /**
     * Creates a matrix with the same elements as the given AffineTransform.
     * @param at
     */
    public Matrix(AffineTransform at)
    {
        single = new float[DEFAULT_SINGLE.length];
        System.arraycopy(DEFAULT_SINGLE, 0, single, 0, DEFAULT_SINGLE.length);
        single[0] = (float)at.getScaleX();
        single[1] = (float)at.getShearY();
        single[3] = (float)at.getShearX();
        single[4] = (float)at.getScaleY();
        single[6] = (float)at.getTranslateX();
        single[7] = (float)at.getTranslateY();
    }

    /**
     * This method resets the numbers in this Matrix to the original values, which are
     * the values that a newly constructed Matrix would have.
     *
     * @deprecated This method will be removed.
     */
    @Deprecated
    public void reset()
    {
        System.arraycopy(DEFAULT_SINGLE, 0, single, 0, DEFAULT_SINGLE.length);
    }

    /**
     * Create an affine transform from this matrix's values.
     *
     * @return An affine transform with this matrix's values.
     */
    public AffineTransform createAffineTransform()
    {
        return new AffineTransform(
            single[0], single[1],   // m00 m10 = scaleX shearY
            single[3], single[4],   // m01 m11 = shearX scaleY
            single[6], single[7] ); // m02 m12 = tx ty
    }

    /**
     * Set the values of the matrix from the AffineTransform.
     *
     * @param af The transform to get the values from.
     * @deprecated Use the {@link #Matrix(AffineTransform)} constructor instead.
     */
    @Deprecated
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
     * @return The values of this matrix.
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
     * @deprecated Use {@link #getValues()} instead.
     */
    @Deprecated
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
     * Concatenates (premultiplies) the given matrix to this matrix.
     *
     * @param matrix The matrix to concatenate.
     */
    public void concatenate(Matrix matrix)
    {
        matrix.multiply(this, this);
    }

    /**
     * Translates this matrix by the given vector.
     *
     * @param vector 2D vector
     */
    public void translate(Vector vector)
    {
        Matrix m = Matrix.getTranslateInstance(vector.getX(), vector.getY());
        concatenate(m);
    }

    /**
     * Translates this matrix by the given ammount.
     *
     * @param tx x-translation
     * @param ty y-translation
     */
    public void translate(float tx, float ty)
    {
        Matrix m = Matrix.getTranslateInstance(tx, ty);
        concatenate(m);
    }

    /**
     * Scales this matrix by the given factors.
     *
     * @param sx x-scale
     * @param sy y-scale
     */
    public void scale(float sx, float sy)
    {
        Matrix m = Matrix.getScaleInstance(sx, sy);
        concatenate(m);
    }

    /**
     * Rotares this matrix by the given factors.
     *
     * @param theta The angle of rotation measured in radians
     */
    public void rotate(double theta)
    {
        Matrix m = Matrix.getRotateInstance(theta, 0, 0);
        concatenate(m);
    }

    /**
     * This will take the current matrix and multiply it with a matrix that is passed in.
     *
     * @param b The matrix to multiply by.
     *
     * @return The result of the two multiplied matrices.
     */
    public Matrix multiply( Matrix b )
    {
        return this.multiply(b, new Matrix());
    }

    /**
     * This method multiplies this Matrix with the specified other Matrix, storing the product in the specified
     * result Matrix. By reusing Matrix instances like this, multiplication chains can be executed without having
     * to create many temporary Matrix objects.
     * <p>
     * It is allowed to have (other == this) or (result == this) or indeed (other == result) but if this is done,
     * the backing float[] matrix values may be copied in order to ensure a correct product.
     *
     * @param other the second operand Matrix in the multiplication
     * @param result the Matrix instance into which the result should be stored. If result is null, a new Matrix
     *               instance is created.
     * @return the product of the two matrices.
     */
    public Matrix multiply( Matrix other, Matrix result )
    {
        if (result == null)
        {
            result = new Matrix();
        }

        if (other != null && other.single != null)
        {
            // the operands
            float[] thisOperand = this.single;
            float[] otherOperand = other.single;

            // We're multiplying 2 sets of floats together to produce a third, but we allow
            // any of these float[] instances to be the same objects.
            // There is the possibility then to overwrite one of the operands with result values
            // and therefore corrupt the result.

            // If either of these operands are the same float[] instance as the result, then
            // they need to be copied.

            if (this == result)
            {
                final float[] thisOrigVals = new float[this.single.length];
                System.arraycopy(this.single, 0, thisOrigVals, 0, this.single.length);

                thisOperand = thisOrigVals;
            }
            if (other == result)
            {
                final float[] otherOrigVals = new float[other.single.length];
                System.arraycopy(other.single, 0, otherOrigVals, 0, other.single.length);

                otherOperand = otherOrigVals;
            }

            result.single[0] = thisOperand[0] * otherOperand[0]
                             + thisOperand[1] * otherOperand[3]
                             + thisOperand[2] * otherOperand[6];
            result.single[1] = thisOperand[0] * otherOperand[1]
                             + thisOperand[1] * otherOperand[4]
                             + thisOperand[2] * otherOperand[7];
            result.single[2] = thisOperand[0] * otherOperand[2]
                             + thisOperand[1] * otherOperand[5]
                             + thisOperand[2] * otherOperand[8];
            result.single[3] = thisOperand[3] * otherOperand[0]
                             + thisOperand[4] * otherOperand[3]
                             + thisOperand[5] * otherOperand[6];
            result.single[4] = thisOperand[3] * otherOperand[1]
                             + thisOperand[4] * otherOperand[4]
                             + thisOperand[5] * otherOperand[7];
            result.single[5] = thisOperand[3] * otherOperand[2]
                             + thisOperand[4] * otherOperand[5]
                             + thisOperand[5] * otherOperand[8];
            result.single[6] = thisOperand[6] * otherOperand[0]
                             + thisOperand[7] * otherOperand[3]
                             + thisOperand[8] * otherOperand[6];
            result.single[7] = thisOperand[6] * otherOperand[1]
                             + thisOperand[7] * otherOperand[4]
                             + thisOperand[8] * otherOperand[7];
            result.single[8] = thisOperand[6] * otherOperand[2]
                             + thisOperand[7] * otherOperand[5]
                             + thisOperand[8] * otherOperand[8];
        }

        return result;
    }

    /**
     * Transforms the given point by this matrix.
     *
     * @param point point to transform
     */
    public void transform(Point2D point)
    {
        float x = (float)point.getX();
        float y = (float)point.getY();
        float a = single[0];
        float b = single[1];
        float c = single[3];
        float d = single[4];
        float e = single[6];
        float f = single[7];
        point.setLocation(x * a + y * c + e, x * b + y * d + f);
    }

    /**
     * Transforms the given point by this matrix.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Point2D.Float transformPoint(float x, float y)
    {
        float a = single[0];
        float b = single[1];
        float c = single[3];
        float d = single[4];
        float e = single[6];
        float f = single[7];
        return new Point2D.Float(x * a + y * c + e, x * b + y * d + f);
    }

    /**
     * Transforms the given point by this matrix.
     *
     * @param vector 2D vector
     */
    public Vector transform(Vector vector)
    {
        float a = single[0];
        float b = single[1];
        float c = single[3];
        float d = single[4];
        float e = single[6];
        float f = single[7];
        float x = vector.getX();
        float y = vector.getY();
        return new Vector(x * a + y * c + e, x * b + y * d + f);
    }

    /**
     * Create a new matrix with just the scaling operators.
     *
     * @return A new matrix with just the scaling operators.
     * @deprecated This method is due to be removed, please contact us if you make use of it.
     */
    @Deprecated
    public Matrix extractScaling()
    {
        Matrix matrix = new Matrix();
        matrix.single[0] = this.single[0];
        matrix.single[4] = this.single[4];
        return matrix;
    }

    /**
     * Convenience method to create a scaled instance.
     *
     * @param sx The xscale operator.
     * @param sy The yscale operator.
     * @return A new matrix with just the x/y scaling
     */
    public static Matrix getScaleInstance(float sx, float sy)
    {
        Matrix matrix = new Matrix();
        matrix.single[0] = sx;
        matrix.single[4] = sy;
        return matrix;
    }

    /**
     * Create a new matrix with just the translating operators.
     *
     * @return A new matrix with just the translating operators.
     * @deprecated This method is due to be removed, please contact us if you make use of it.
     */
    @Deprecated
    public Matrix extractTranslating()
    {
        Matrix matrix = new Matrix();
        matrix.single[6] = this.single[6];
        matrix.single[7] = this.single[7];
        return matrix;
    }

    /**
     * Convenience method to create a translating instance.
     *
     * @param tx The x translating operator.
     * @param ty The y translating operator.
     * @return A new matrix with just the x/y translating.
     * @deprecated Use {@link #getTranslateInstance} instead.
     */
    @Deprecated
    public static Matrix getTranslatingInstance(float tx, float ty)
    {
        return getTranslateInstance(tx, ty);
    }

    /**
     * Convenience method to create a translating instance.
     *
     * @param tx The x translating operator.
     * @param ty The y translating operator.
     * @return A new matrix with just the x/y translating.
     */
    public static Matrix getTranslateInstance(float tx, float ty)
    {
        Matrix matrix = new Matrix();
        matrix.single[6] = tx;
        matrix.single[7] = ty;
        return matrix;
    }

    /**
     * Convenience method to create a rotated instance.
     *
     * @param theta The angle of rotation measured in radians
     * @param tx The x translation.
     * @param ty The y translation.
     * @return A new matrix with the rotation and the x/y translating.
     */
    public static Matrix getRotateInstance(double theta, float tx, float ty)
    {
        float cosTheta = (float)Math.cos(theta);
        float sinTheta = (float)Math.sin(theta);

        Matrix matrix = new Matrix();
        matrix.single[0] = cosTheta;
        matrix.single[1] = sinTheta;
        matrix.single[3] = -sinTheta;
        matrix.single[4] = cosTheta;
        matrix.single[6] = tx;
        matrix.single[7] = ty;
        return matrix;
    }

    /**
     * Produces a copy of the first matrix, with the second matrix concatenated.
     *
     * @param a The matrix to copy.
     * @param b The matrix to concatenate.
     */
    public static Matrix concatenate(Matrix a, Matrix b)
    {
        Matrix copy = a.clone();
        copy.concatenate(b);
        return copy;
    }

    /**
     * Clones this object.
     * @return cloned matrix as an object.
     */
    @Override
    public Matrix clone()
    {
        Matrix clone = new Matrix();
        System.arraycopy( single, 0, clone.single, 0, 9 );
        return clone;
    }

    /**
     * Returns the x-scaling factor of this matrix. This is calculated from the scale and shear.
     *
     * @return The x-scaling factor.
     */
    public float getScalingFactorX()
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
     * Returns the y-scaling factor of this matrix. This is calculated from the scale and shear.
     *
     * @return The y-scaling factor.
     */
    public float getScalingFactorY()
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
     * Returns the x-scaling element of this matrix.
     * 
     * @see #getScalingFactorX() 
     */
    public float getScaleX()
    {
        return single[0];
    }

    /**
     * Returns the y-shear element of this matrix.
     */
    public float getShearY()
    {
        return single[1];
    }

    /**
     * Returns the x-shear element of this matrix.
     */
    public float getShearX()
    {
        return single[3];
    }

    /**
     * Returns the y-scaling element of this matrix.
     *
     * @see #getScalingFactorY()
     */
    public float getScaleY()
    {
        return single[4];
    }

    /**
     * Returns the x-translation element of this matrix.
     */
    public float getTranslateX()
    {
        return single[6];
    }

    /**
     * Returns the y-translation element of this matrix.
     */
    public float getTranslateY()
    {
        return single[7];
    }

    /**
     * Get the x position in the matrix. This method is deprecated as it is incorrectly named.
     *
     * @return The x-position.
     * @deprecated Use {@link #getTranslateX} instead
     */
    @Deprecated
    public float getXPosition()
    {
        return single[6];
    }

    /**
     * Get the y position. This method is deprecated as it is incorrectly named.
     *
     * @return The y position.
     * @deprecated Use {@link #getTranslateY} instead
     */
    @Deprecated
    public float getYPosition()
    {
        return single[7];
    }

    /**
     * Returns a COS array which represents this matrix.
     */
    public COSArray toCOSArray()
    {
        COSArray array = new COSArray();
        array.add(new COSFloat(single[0]));
        array.add(new COSFloat(single[1]));
        array.add(new COSFloat(single[3]));
        array.add(new COSFloat(single[4]));
        array.add(new COSFloat(single[6]));
        array.add(new COSFloat(single[7]));
        return array;
    }

    @Override
    public String toString()
    {
        String sb = "" + "[" +
                single[0] + "," +
                single[1] + "," +
                single[3] + "," +
                single[4] + "," +
                single[6] + "," +
                single[7] + "]";
        return sb;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(single);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        return Arrays.equals(this.single, ((Matrix) obj).single);
    }
}
