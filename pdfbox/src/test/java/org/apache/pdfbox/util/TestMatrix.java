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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the {@link Matrix} class.
 * @author Neil McErlean
 * @since 1.4.0
 */
public class TestMatrix extends TestCase
{
    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     *
     * @throws IOException If there is an error creating the test.
     */
    public TestMatrix( String name ) throws IOException
    {
        super( name );
    }

    public void testConstructionAndCopy() throws Exception
    {
        Matrix m1 = new Matrix();
        assertMatrixIsPristine(m1);

        Matrix m2 = m1.copy();
        assertNotSame(m1, m2);
        assertMatrixIsPristine(m2);
    }

    public void testMultiplication() throws Exception
    {
        // This matrix will not change - we use it to drive the various multiplications.
        final Matrix testMatrix = new Matrix();

        // Create matrix with values
        // [ 0, 1, 2
        //   1, 2, 3
        //   2, 3, 4]
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                testMatrix.setValue(x, y, x + y);
            }
        }

        Matrix m1 = testMatrix.copy();
        Matrix m2 = testMatrix.copy();

        // Multiply two matrices together producing a new result matrix.
        Matrix product = m1.multiply(m2);

        assertNotSame(m1, product);
        assertNotSame(m2, product);

        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m1);
        // Operand 2 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m2);
        assertMatrixValuesEqualTo(new float[] {5,  8,  11,
                                               8,  14, 20,
                                               11, 20, 29}, product);
        product.reset();
        assertMatrixIsPristine(product);



        // Multiply two matrices together with the result being written to a third matrix
        // (Any existing values there will be overwritten).
        Matrix resultMatrix = new Matrix();

        Matrix retVal = m1.multiply(m2, resultMatrix);
        assertSame(retVal, resultMatrix);
        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m1);
        // Operand 2 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m2);
        assertMatrixValuesEqualTo(new float[] {5,  8,  11,
                                               8,  14, 20,
                                               11, 20, 29}, resultMatrix);



        // Multiply two matrices together with the result being written into the other matrix
        retVal = m1.multiply(m2, m2);
        assertSame(retVal, m2);
        // Operand 1 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m1);
        assertMatrixValuesEqualTo(new float[] {5,  8,  11,
                                               8,  14, 20,
                                               11, 20, 29}, retVal);



        // Multiply two matrices together with the result being written into 'this' matrix
        m1 = testMatrix.copy();
        m2 = testMatrix.copy();

        retVal = m1.multiply(m2, m1);
        assertSame(retVal, m1);
        // Operand 2 should not have changed
        assertMatrixValuesEqualTo(new float[] {0,  1,  2,
                                               1,  2,  3,
                                               2,  3,  4}, m2);
        assertMatrixValuesEqualTo(new float[] {5,  8,  11,
                                               8,  14, 20,
                                               11, 20, 29}, retVal);



        // Multiply the same matrix with itself with the result being written into 'this' matrix
        m1 = testMatrix.copy();

        retVal = m1.multiply(m1, m1);
        assertSame(retVal, m1);
        assertMatrixValuesEqualTo(new float[] {5,  8,  11,
                                               8,  14, 20,
                                               11, 20, 29}, retVal);
    }

    /**
     * This method asserts that the matrix values for the given {@link Matrix} object are equal
     * to the pristine, or original, values.
     * @param m the Matrix to test.
     */
    private void assertMatrixIsPristine(Matrix m)
    {
        assertMatrixValuesEqualTo(new float[] {1 ,0 ,0,
                                               0, 1, 0,
                                               0, 0, 1}, m);
    }

    /**
     * This method asserts that the matrix values for the given {@link Matrix} object have
     * the specified values.
     * @param values the expected values
     * @param m the matrix to test
     */
    private void assertMatrixValuesEqualTo(float[] values, Matrix m)
    {
        float delta = 0.00001f;
        for (int i = 0; i < values.length; i++)
        {
            // Need to convert a (row, column) co-ordinate into a straight index.
            int row = (int)Math.floor(i / 3);
            int column = i % 3;
            StringBuilder failureMsg = new StringBuilder();
            failureMsg.append("Incorrect value for matrix[")
                    .append(row).append(",").append(column).append("]");
            assertEquals(failureMsg.toString(), values[i], m.getValue(row, column), delta);
        }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestMatrix.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestMatrix.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
