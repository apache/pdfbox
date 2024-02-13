/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.pdfbox.cos.COSName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Neil McErlean
 * @author Tilman Hausherr
 */
class MatrixTest
{
    
    @Test
    void testConstructionAndCopy() throws Exception
    {
        Matrix m1 = new Matrix();
        assertMatrixIsPristine(m1);

        Matrix m2 = m1.clone();
        assertNotSame(m1, m2);
        assertMatrixIsPristine(m2);
    }

    @Test
    void testGetScalingFactor()
    {
        // check scaling factor of an initial matrix
        Matrix m1 = new Matrix();
        assertEquals(1, m1.getScalingFactorX(), 0);
        assertEquals(1, m1.getScalingFactorY(), 0);

        // check scaling factor of an initial matrix
        Matrix m2 = new Matrix(2, 4, 4, 2, 0, 0);
        assertEquals((float) Math.sqrt(20), m2.getScalingFactorX(), 0);
        assertEquals((float) Math.sqrt(20), m2.getScalingFactorY(), 0);
    }

    @Test
    void testCreateMatrixUsingInvalidInput()
    {
        // anything but a COSArray is invalid and leads to an initial matrix
        Matrix createMatrix = Matrix.createMatrix(COSName.A);
        assertMatrixIsPristine(createMatrix);

        // a COSArray with fewer than 6 entries leads to an initial matrix
        COSArray cosArray = new COSArray();
        cosArray.add(COSName.A);
        createMatrix = Matrix.createMatrix(cosArray);
        assertMatrixIsPristine(createMatrix);

        // a COSArray containing other kind of objects than COSNumber leads to an initial matrix
        cosArray = new COSArray();
        for (int i = 0; i < 6; i++)
        {
            cosArray.add(COSName.A);
        }
        createMatrix = Matrix.createMatrix(cosArray);
        assertMatrixIsPristine(createMatrix);
    }

    @Test
    void testMultiplication()
    {
        // These matrices will not change - we use it to drive the various multiplications.
        final Matrix const1 = new Matrix();
        final Matrix const2 = new Matrix();

        // Create matrix with values
        // [ 0, 1, 2
        // 1, 2, 3
        // 2, 3, 4]
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                const1.setValue(x, y, x + y);
                const2.setValue(x, y, 8 + x + y);
            }
        }

        float[] m1MultipliedByM1 = new float[] { 5,  8,  11,  8, 14, 20, 11, 20,  29 };
        float[] m1MultipliedByM2 = new float[] { 29, 32, 35, 56, 62, 68, 83, 92, 101 };
        float[] m2MultipliedByM1 = new float[] { 29, 56, 83, 32, 62, 92, 35, 68, 101 };

        Matrix var1 = const1.clone();
        Matrix var2 = const2.clone();

        // Multiply two matrices together producing a new result matrix.
        Matrix result = var1.multiply(var2);
        assertEquals(const1, var1);
        assertEquals(const2, var2);
        assertMatrixValuesEqualTo(m1MultipliedByM2, result);

        // Multiply two matrices together with the result being written to a third matrix
        // (Any existing values there will be overwritten).
        result = var1.multiply(var2);
        assertEquals(const1, var1);
        assertEquals(const2, var2);
        assertMatrixValuesEqualTo(m1MultipliedByM2, result);

        // Multiply two matrices together with the result being written into 'this' matrix
        var1 = const1.clone();
        var2 = const2.clone();
        var1.concatenate(var2);
        assertEquals(const2, var2);
        assertMatrixValuesEqualTo(m2MultipliedByM1, var1);

        var1 = const1.clone();
        var2 = const2.clone();
        result = Matrix.concatenate(var1, var2);
        assertEquals(const1, var1);
        assertEquals(const2, var2);
        assertMatrixValuesEqualTo(m2MultipliedByM1, result);

        // Multiply the same matrix with itself with the result being written into 'this' matrix
        var1 = const1.clone();
        result = var1.multiply(var1);
        assertEquals(const1, var1);
        assertMatrixValuesEqualTo(m1MultipliedByM1, result);
    }

    @Test
    void testOldMultiplication() throws Exception
    {
        // This matrix will not change - we use it to drive the various multiplications.
        final Matrix testMatrix = new Matrix();

        // Create matrix with values
        // [ 0, 1, 2
        // 1, 2, 3
        // 2, 3, 4]
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                testMatrix.setValue(x, y, x + y);
            }
        }

        Matrix m1 = testMatrix.clone();
        Matrix m2 = testMatrix.clone();

        // Multiply two matrices together producing a new result matrix.
        Matrix product = m1.multiply(m2);

        assertNotSame(m1, product);
        assertNotSame(m2, product);

        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] { 0, 1, 2, 1, 2, 3, 2, 3, 4 }, m1);
        // Operand 2 should not have changed
        assertMatrixValuesEqualTo(new float[] { 0, 1, 2, 1, 2, 3, 2, 3, 4 }, m2);
        assertMatrixValuesEqualTo(new float[] { 5, 8, 11, 8, 14, 20, 11, 20, 29 }, product);

        Matrix retVal = m1.multiply(m2);
        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] { 0, 1, 2, 1, 2, 3, 2, 3, 4 }, m1);
        // Operand 2 should not have changed
        assertMatrixValuesEqualTo(new float[] { 0, 1, 2, 1, 2, 3, 2, 3, 4 }, m2);
        assertMatrixValuesEqualTo(new float[] { 5, 8, 11, 8, 14, 20, 11, 20, 29 }, retVal);

        // Multiply the same matrix with itself with the result being written into 'this' matrix
        m1 = testMatrix.clone();

        retVal = m1.multiply(m1);
        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] { 0, 1, 2, 1, 2, 3, 2, 3, 4 }, m1);
        assertMatrixValuesEqualTo(new float[] { 5, 8, 11, 8, 14, 20, 11, 20, 29 }, retVal);
    }

    @Test
    void testIllegalValueNaN1()
    {
        Matrix m = new Matrix();
        m.setValue(0, 0, Float.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> m.multiply(m));
    }

    @Test
    void testIllegalValueNaN2()
    {
        Matrix m = new Matrix();
        m.setValue(0, 0, Float.NaN);
        assertThrows(IllegalArgumentException.class, () -> m.multiply(m));
    }

    @Test
    void testIllegalValuePositiveInfinity()
    {
        Matrix m = new Matrix();
        m.setValue(0, 0, Float.POSITIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> m.multiply(m));
    }

    @Test
    void testIllegalValueNegativeInfinity()
    {
        Matrix m = new Matrix();
        m.setValue(0, 0, Float.NEGATIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> m.multiply(m));
    }

    /**
     * Test of PDFBOX-2872 bug
     */
    @Test
    void testPdfbox2872()
    {
        Matrix m = new Matrix(2, 4, 5, 8, 2, 0);
        COSArray toCOSArray = m.toCOSArray();
        assertEquals(new COSFloat(2), toCOSArray.get(0));
        assertEquals(new COSFloat(4), toCOSArray.get(1));
        assertEquals(new COSFloat(5), toCOSArray.get(2));
        assertEquals(new COSFloat(8), toCOSArray.get(3));
        assertEquals(new COSFloat(2), toCOSArray.get(4));
        assertEquals(COSFloat.ZERO, toCOSArray.get(5));
        
    }

    @Test
    void testGetValues()
    {
        Matrix m = new Matrix(2, 4, 4, 2, 15, 30);
        float[][] values = m.getValues();
        assertEquals(2, values[0][0], 0);
        assertEquals(4, values[0][1], 0);
        assertEquals(0, values[0][2], 0);
        assertEquals(4, values[1][0], 0);
        assertEquals(2, values[1][1], 0);
        assertEquals(0, values[1][2], 0);
        assertEquals(15, values[2][0], 0);
        assertEquals(30, values[2][1], 0);
        assertEquals(1, values[2][2], 0);
    }

    @Test
    void testScaling()
    {
        Matrix m = new Matrix(2, 4, 4, 2, 15, 30);
        m.scale(2, 3);
        // first row, multiplication with 2
        assertEquals(4, m.getValue(0, 0), 0);
        assertEquals(8, m.getValue(0, 1), 0);
        assertEquals(0, m.getValue(0, 2), 0);

        // second row, multiplication with 3
        assertEquals(12, m.getValue(1, 0), 0);
        assertEquals(6, m.getValue(1, 1), 0);
        assertEquals(0, m.getValue(1, 2), 0);

        // third row, no changes at all
        assertEquals(15, m.getValue(2, 0), 0);
        assertEquals(30, m.getValue(2, 1), 0);
        assertEquals(1, m.getValue(2, 2), 0);
    }

    @Test
    void testTranslation()
    {
        Matrix m = new Matrix(2, 4, 4, 2, 15, 30);
        m.translate(2, 3);
        // first row, no changes at all
        assertEquals(2, m.getValue(0, 0), 0);
        assertEquals(4, m.getValue(0, 1), 0);
        assertEquals(0, m.getValue(0, 2), 0);

        // second row, no changes at all
        assertEquals(4, m.getValue(1, 0), 0);
        assertEquals(2, m.getValue(1, 1), 0);
        assertEquals(0, m.getValue(1, 2), 0);

        // third row, translated values
        assertEquals(31, m.getValue(2, 0), 0);
        assertEquals(44, m.getValue(2, 1), 0);
        assertEquals(1, m.getValue(2, 2), 0);
    }

    /**
     * This method asserts that the matrix values for the given {@link Matrix} object are equal to the pristine, or
     * original, values.
     * 
     * @param m the Matrix to test.
     */
    private void assertMatrixIsPristine(Matrix m)
    {
        assertMatrixValuesEqualTo(new float[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 }, m);
    }

    /**
     * This method asserts that the matrix values for the given {@link Matrix} object have the specified values.
     * 
     * @param values the expected values
     * @param m the matrix to test
     */
    private void assertMatrixValuesEqualTo(float[] values, Matrix m)
    {
        float delta = 0.00001f;
        for (int i = 0; i < values.length; i++)
        {
            // Need to convert a (row, column) coordinate into a straight index.
            int row = (int) Math.floor(i / 3);
            int column = i % 3;
            StringBuilder failureMsg = new StringBuilder();
            failureMsg.append("Incorrect value for matrix[").append(row).append(",").append(column)
                    .append("]");
            assertEquals(values[i], m.getValue(row, column), delta, failureMsg.toString());
        }
    }

    //Uncomment annotation to run the test
    // @Test
    public void testMultiplicationPerformance() {
        long start = System.currentTimeMillis();
        Matrix c;
        Matrix d;
        for (int i=0; i<100000000; i++) {
            c = new Matrix(15, 3, 235, 55, 422, 1);
            d = new Matrix(45, 345, 23, 551, 66, 832);
            c.multiply(d);
            c.concatenate(d);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Matrix multiplication took " + (stop - start) + "ms.");
    }
}
