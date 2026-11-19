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
package org.apache.fontbox.cff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the matrix concatenation used by {@link CFFParser} to combine the FontMatrix of the top
 * DICT with the FontMatrix of a Font DICT of a CID keyed font.
 * <p>
 * The matrices are laid out as {@code [a b c d x y]} and are concatenated as row vector
 * transformations, i.e. {@code dest = dest x concat} (first {@code dest}, then {@code concat}):
 *
 * <pre>
 * a = a1*a2 + b1*c2
 * b = a1*b2 + b1*d2
 * c = c1*a2 + d1*c2
 * d = c1*b2 + d1*d2
 * x = x1*a2 + y1*c2 + x2
 * y = x1*b2 + y1*d2 + y2
 * </pre>
 *
 * Usual font matrices like {@code [0.001 0 0 0.001 0 0]} have {@code b == c == 0}, which hides
 * errors in the terms that involve {@code b} or {@code c}. Therefore most of these tests use
 * skewed matrices.
 * 
 * @author Claude Sonnet 5.5
 */
class CFFParserMatrixTest
{
    private static final double DELTA = 1e-12;

    static Stream<Arguments> matrixCases()
    {
        return Stream.of(
                // distinct values everywhere, so that every wrong operand changes the result
                Arguments.of("general", //
                        new double[] { 1, 2, 3, 4, 5, 6 }, //
                        new double[] { 7, 8, 9, 10, 11, 12 }, //
                        new double[] { 25, 28, 57, 64, 100, 112 }),
                // b1 != 0 and d1 != d2: b must be a1*b2 + b1*d2 = 0.25 * 0.002
                Arguments.of("skewed", //
                        new double[] { 1, 0.25, 0, 1, 0, 0 }, //
                        new double[] { 0.001, 0, 0, 0.002, 0, 0 }, //
                        new double[] { 0.001, 0.0005, 0, 0.002, 0, 0 }),
                // same matrices in the opposite order: the operation is not commutative
                Arguments.of("skewed, operands swapped", //
                        new double[] { 0.001, 0, 0, 0.002, 0, 0 }, //
                        new double[] { 1, 0.25, 0, 1, 0, 0 }, //
                        new double[] { 0.001, 0.00025, 0, 0.002, 0, 0 }),
                // synthetic oblique (c != 0) combined with a different y scale
                Arguments.of("oblique", //
                        new double[] { 0.001, 0, 0.0002, 0.001, 0, 0 }, //
                        new double[] { 1, 0, 0, 0.5, 0, 0 }, //
                        new double[] { 0.001, 0, 0.0002, 0.0005, 0, 0 }),
                // identity must not change a matrix, including its skew and translation
                Arguments.of("identity", //
                        new double[] { 0.001, 0.0003, 0.0002, 0.001, 10, 20 }, //
                        new double[] { 1, 0, 0, 1, 0, 0 }, //
                        new double[] { 0.001, 0.0003, 0.0002, 0.001, 10, 20 }),
                // the translation of dest is transformed by concat, then its translation is added
                // x = 3*2 + 4*(-1) + 5 = 7, y = 3*1 + 4*2 + 6 = 17
                Arguments.of("translation", //
                        new double[] { 1, 0, 0, 1, 3, 4 }, //
                        new double[] { 2, 1, -1, 2, 5, 6 }, //
                        new double[] { 2, 1, -1, 2, 7, 17 }),
                // the usual case: no skew, both matrices only scale
                Arguments.of("scale only", //
                        new double[] { 0.001, 0, 0, 0.001, 0, 0 }, //
                        new double[] { 2, 0, 0, 3, 0, 0 }, //
                        new double[] { 0.002, 0, 0, 0.003, 0, 0 }));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matrixCases")
    void testConcatenateMatrix(String name, double[] first, double[] second, double[] expected)
    {
        List<Number> dest = toList(first);
        List<Number> concat = toList(second);

        new CFFParser().concatenateMatrix(dest, concat);

        assertMatrix(expected, dest);
        // only the first matrix is changed
        assertMatrix(second, concat);
    }

    /**
     * Cross-check with {@link AffineTransform} as an independent implementation. {@code [a b c d x
     * y]} corresponds to {@code new AffineTransform(a, b, c, d, x, y)}, and "first dest, then
     * concat" is {@code preConcatenate}.
     */
    @Test
    void testConcatenateMatrixMatchesAffineTransform()
    {
        Random random = new Random(4711);
        for (int i = 0; i < 1000; i++)
        {
            double[] first = randomMatrix(random);
            double[] second = randomMatrix(random);

            AffineTransform expected = new AffineTransform(first);
            expected.preConcatenate(new AffineTransform(second));
            double[] expectedValues = new double[6];
            expected.getMatrix(expectedValues);

            List<Number> dest = toList(first);
            new CFFParser().concatenateMatrix(dest, toList(second));

            assertMatrix(expectedValues, dest);
        }
    }

    /**
     * The operands of a DICT entry are Integer or Double values depending on how they are encoded
     * in the font, so the result must not depend on the type of the operands.
     */
    @Test
    void testConcatenateMatrixWithMixedOperandTypes()
    {
        List<Number> dest = new ArrayList<>(List.of(2, 1, 0, 3, 0, 0));
        List<Number> concat = new ArrayList<>(List.of(0.5, 0, 0, 0.5f, 0L, 0));

        new CFFParser().concatenateMatrix(dest, concat);

        assertMatrix(new double[] { 1, 0.5, 0, 1.5, 0, 0 }, dest);
    }

    private static List<Number> toList(double[] values)
    {
        // the parser works on mutable lists of its DICT operands
        List<Number> result = new ArrayList<>();
        for (double value : values)
        {
            result.add(value);
        }
        return result;
    }

    private static double[] randomMatrix(Random random)
    {
        double[] result = new double[6];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = random.nextDouble() * 20 - 10;
        }
        return result;
    }

    private static void assertMatrix(double[] expected, List<Number> found)
    {
        assertEquals(expected.length, found.size(), "unexpected matrix size");
        String message = "unexpected matrix " + found;
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], found.get(i).doubleValue(), DELTA, message);
        }
    }
}
