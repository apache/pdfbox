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
import org.apache.pdfbox.cos.COSBase;

/**
 * This class will be used for matrix manipulation.
 *
 * @author Ben Litchfield
 */
public final class Matrix implements Cloneable
{
    public static final int SIZE = 9;
    private float[] single;

    /**
     * Constructor. This produces an identity matrix.
     */
    public Matrix()
    {
        // a b 0
        // c d 0
        // tx ty 1
        // note: hx and hy are reversed vs.the PDF spec as we use AffineTransform's definition x and y shear
        // sx hy 0
        // hx sy 0
        // tx ty 1
        single = new float[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
    }

    /**
     * Constructor. This produces a matrix with the given array as data.
     * The source array is not copied or cloned.
     */
    private Matrix(float[] src)
    {
        single = src;
    }

    /**
     * Creates a matrix from a 6-element (a b c d e f) COS array.
     *
     * @param array source array, elements must be or extend COSNumber
     */
    private Matrix(COSArray array)
    {
        single = new float[SIZE];
        single[0] = ((COSNumber)array.getObject(0)).floatValue();
        single[1] = ((COSNumber)array.getObject(1)).floatValue();
        single[3] = ((COSNumber)array.getObject(2)).floatValue();
        single[4] = ((COSNumber)array.getObject(3)).floatValue();
        single[6] = ((COSNumber)array.getObject(4)).floatValue();
        single[7] = ((COSNumber)array.getObject(5)).floatValue();
        single[8] = 1;
    }

    /**
     * Creates a transformation matrix with the given 6 elements. Transformation matrices are
     * discussed in 8.3.3, "Common Transformations" and 8.3.4, "Transformation Matrices" of the PDF
     * specification. For simple purposes (rotate, scale, translate) it is recommended to use the
     * static methods below.
     *
     * Produces the following matrix:
     * a b 0
     * c d 0
     * e f 1
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
        single = new float[SIZE];
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
     * @param at matrix elements will be initialize with the values from this affine transformation, as follows:
     *
     *           scaleX shearY 0
     *           shearX scaleY 0
     *           transX transY 1
     *
     */
    public Matrix(AffineTransform at)
    {
        single = new float[SIZE];
        single[0] = (float)at.getScaleX();
        single[1] = (float)at.getShearY();
        single[3] = (float)at.getShearX();
        single[4] = (float)at.getScaleY();
        single[6] = (float)at.getTranslateX();
        single[7] = (float)at.getTranslateY();
        single[8] = 1;
    }

    /**
     * Convenience method to be used when creating a matrix from unverified data. If the parameter
     * is a COSArray with at least six numbers, a Matrix object is created from the first six
     * numbers and returned. If not, then the identity Matrix is returned.
     *
     * @param base a COS object, preferably a COSArray with six numbers.
     *
     * @return a Matrix object.
     */
    public static Matrix createMatrix(COSBase base)
    {
        if (!(base instanceof COSArray))
        {
            return new Matrix();
        }
        COSArray array = (COSArray) base;
        if (array.size() < 6)
        {
            return new Matrix();
        }
        for (int i = 0; i < 6; ++i)
        {
            if (!(array.getObject(i) instanceof COSNumber))
            {
                return new Matrix();
            }
        }
        return new Matrix(array);
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
     * Concatenates (premultiplies) the given matrix to this matrix.
     *
     * @param matrix The matrix to concatenate.
     */
    public void concatenate(Matrix matrix)
    {
        single = checkFloatValues(multiplyArrays(matrix.single, single));
    }

    /**
     * Translates this matrix by the given vector.
     *
     * @param vector 2D vector
     */
    public void translate(Vector vector)
    {
        translate(vector.getX(), vector.getY());
    }

    /**
     * Translates this matrix by the given amount.
     *
     * @param tx x-translation
     * @param ty y-translation
     */
    public void translate(float tx, float ty)
    {
        single[6] += tx * single[0] + ty * single[3];
        single[7] += tx * single[1] + ty * single[4];
        single[8] += tx * single[2] + ty * single[5];
        checkFloatValues(single);
    }

    /**
     * Scales this matrix by the given factors.
     *
     * @param sx x-scale
     * @param sy y-scale
     */
    public void scale(float sx, float sy)
    {
        single[0] *= sx;
        single[1] *= sx;
        single[2] *= sx;
        single[3] *= sy;
        single[4] *= sy;
        single[5] *= sy;
        checkFloatValues(single);
    }

    /**
     * Rotates this matrix by the given factors.
     *
     * @param theta The angle of rotation measured in radians
     */
    public void rotate(double theta)
    {
        concatenate(Matrix.getRotateInstance(theta, 0, 0));
    }

    /**
     * This method multiplies this Matrix with the specified other Matrix, storing the product in a 
     * new instance. It is allowed to have (other == this).
     *
     * @param other the second operand Matrix in the multiplication; required
     * @return the product of the two matrices.
     */
    public Matrix multiply(Matrix other)
    {
        return new Matrix(checkFloatValues(multiplyArrays(single, other.single)));
    }

    private float[] checkFloatValues(float[] values)
    {
        if (!Float.isFinite(values[0]) || !Float.isFinite(values[1]) || !Float.isFinite(values[2])
                || !Float.isFinite(values[3]) || !Float.isFinite(values[4]) || !Float.isFinite(values[5])
                || !Float.isFinite(values[6]) || !Float.isFinite(values[7]) || !Float.isFinite(values[8]))
            throw new IllegalArgumentException("Multiplying two matrices produces illegal values");
        return values;
    }


    private float[] multiplyArrays(float[] a, float[] b)
    {
        float[] c = new float[SIZE];
        c[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
        c[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
        c[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];
        c[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
        c[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
        c[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];
        c[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
        c[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
        c[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];
        return c;
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
     * Convenience method to create a scaled instance.
     *
     * Produces the following matrix:
     * x 0 0
     * 0 y 0
     * 0 0 1
     *
     * @param x The xscale operator.
     * @param y The yscale operator.
     * @return A new matrix with just the x/y scaling
     */
    public static Matrix getScaleInstance(float x, float y)
    {
        return new Matrix(x, 0, 0, y, 0, 0);
    }

    /**
     * Convenience method to create a translating instance.
     *
     * Produces the following matrix:
     * 1 0 0
     * 0 1 0
     * x y 1
     *
     * @param x The x translating operator.
     * @param y The y translating operator.
     * @return A new matrix with just the x/y translating.
     */
    public static Matrix getTranslateInstance(float x, float y)
    {
        return new Matrix(1, 0, 0, 1, x, y);
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

        return new Matrix(cosTheta, sinTheta, -sinTheta, cosTheta, tx, ty);
    }

    /**
     * Produces a copy of the first matrix, with the second matrix concatenated.
     *
     * @param a The matrix to copy.
     * @param b The matrix to concatenate.
     */
    public static Matrix concatenate(Matrix a, Matrix b)
    {
        return b.multiply(a);
    }

    /**
     * Clones this object.
     * @return cloned matrix as an object.
     */
    @Override
    public Matrix clone()
    {
        return new Matrix(single.clone());
    }

    /**
     * Returns the x-scaling factor of this matrix. This is calculated from the scale and shear.
     *
     * @return The x-scaling factor.
     */
    public float getScalingFactorX()
    {
        /*
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
        if (Float.compare(single[1], 0.0f) != 0)
        {
            return (float) Math.sqrt(Math.pow(single[0], 2) +
                                      Math.pow(single[1], 2));
        }
        return single[0];
    }

    /**
     * Returns the y-scaling factor of this matrix. This is calculated from the scale and shear.
     *
     * @return The y-scaling factor.
     */
    public float getScalingFactorY()
    {
        if (Float.compare(single[3], 0.0f) != 0)
        {
            return (float) Math.sqrt(Math.pow(single[3], 2) +
                                      Math.pow(single[4], 2));
        }
        return single[4];
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
     * Returns a COS array which represent the geometric relevant
     * components of the matrix. The last column of the matrix is ignored,
     * only the first two columns are returned. This is analog to the
     * Matrix(COSArray) constructor.
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
        return "[" +
            single[0] + "," +
            single[1] + "," +
            single[3] + "," +
            single[4] + "," +
            single[6] + "," +
            single[7] + "]";
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
