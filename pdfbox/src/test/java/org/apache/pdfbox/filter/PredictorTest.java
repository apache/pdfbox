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
package org.apache.pdfbox.filter;

import static org.apache.pdfbox.filter.Predictor.getBitSeq;
import static org.apache.pdfbox.filter.Predictor.calcSetBitSeq;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PredictorTest
{
    /**
     * Test of getBitSeq method, of class Predictor.
     */
    @Test
    public void testGetBitSeq()
    {
        Assert.assertEquals(Integer.parseInt("11111111", 2), getBitSeq(Integer.parseInt("11111111", 2), 0, 8));
        Assert.assertEquals(Integer.parseInt("00000000", 2), getBitSeq(Integer.parseInt("00000000", 2), 0, 8));
        Assert.assertEquals(Integer.parseInt("1", 2), getBitSeq(Integer.parseInt("11111111", 2), 0, 1));
        Assert.assertEquals(Integer.parseInt("0", 2), getBitSeq(Integer.parseInt("00000000", 2), 0, 1));
        Assert.assertEquals(Integer.parseInt("001", 2), getBitSeq(Integer.parseInt("00110001", 2), 0, 3));
        Assert.assertEquals(Integer.parseInt("10101010", 2), getBitSeq(Integer.parseInt("10101010", 2), 0, 8));
        Assert.assertEquals(Integer.parseInt("10", 2), getBitSeq(Integer.parseInt("10101010", 2), 0, 2));
        Assert.assertEquals(Integer.parseInt("01", 2), getBitSeq(Integer.parseInt("10101010", 2), 1, 2));
        Assert.assertEquals(Integer.parseInt("10", 2), getBitSeq(Integer.parseInt("10101010", 2), 2, 2));
        Assert.assertEquals(Integer.parseInt("101", 2), getBitSeq(Integer.parseInt("10101010", 2), 3, 3));
        Assert.assertEquals(Integer.parseInt("1010101", 2), getBitSeq(Integer.parseInt("10101010", 2), 1, 7));
        Assert.assertEquals(Integer.parseInt("01", 2), getBitSeq(Integer.parseInt("10101010", 2), 3, 2));
        Assert.assertEquals(Integer.parseInt("00110001", 2), getBitSeq(Integer.parseInt("00110001", 2), 0, 8));
        Assert.assertEquals(Integer.parseInt("10001", 2), getBitSeq(Integer.parseInt("00110001", 2), 0, 5));
        Assert.assertEquals(Integer.parseInt("0011", 2), getBitSeq(Integer.parseInt("00110001", 2), 4, 4));
        Assert.assertEquals(Integer.parseInt("110", 2), getBitSeq(Integer.parseInt("00110001", 2), 3, 3));
        Assert.assertEquals(Integer.parseInt("00", 2), getBitSeq(Integer.parseInt("00110001", 2), 6, 2));
        Assert.assertEquals(Integer.parseInt("1111", 2), getBitSeq(Integer.parseInt("11110000", 2), 4, 4));
        Assert.assertEquals(Integer.parseInt("11", 2), getBitSeq(Integer.parseInt("11110000", 2), 6, 2));
        Assert.assertEquals(Integer.parseInt("0000", 2), getBitSeq(Integer.parseInt("11110000", 2), 0, 4));
    }

    /**
     * Test of calcSetBitSeq method, of class Predictor.
     */
    @Test
    public void testCalcSetBitSeq()
    {
        Assert.assertEquals(Integer.parseInt("00000000", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 8, 0));
        Assert.assertEquals(Integer.parseInt("00000001", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 8, 1));
        Assert.assertEquals(Integer.parseInt("11111111", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 1, 1));
        Assert.assertEquals(Integer.parseInt("11111101", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 2, 1));
        Assert.assertEquals(Integer.parseInt("11111001", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 3, 1));
        Assert.assertEquals(Integer.parseInt("00000001", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 0, 2, 1));
        Assert.assertEquals(Integer.parseInt("11110001", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 4, 1));
        Assert.assertEquals(Integer.parseInt("11100011", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 1, 4, 1));
        Assert.assertEquals(Integer.parseInt("00000010", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 1, 1, 1));
        Assert.assertEquals(Integer.parseInt("11111111", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 7, 1, 1));
        Assert.assertEquals(Integer.parseInt("01111111", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 7, 1, 0));
        Assert.assertEquals(Integer.parseInt("10000000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 7, 1, 1));
        Assert.assertEquals(Integer.parseInt("00000000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 7, 1, 0));
        Assert.assertEquals(Integer.parseInt("01000000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 6, 1, 1));
        Assert.assertEquals(Integer.parseInt("00000000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 6, 1, 0));
        Assert.assertEquals(Integer.parseInt("00110000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 3, 3, 6));
        Assert.assertEquals(Integer.parseInt("01100000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 4, 3, 6));
        Assert.assertEquals(Integer.parseInt("11000000", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 5, 3, 6));
        Assert.assertEquals(Integer.parseInt("11111111", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 0, 8, 0xFF));
        Assert.assertEquals(Integer.parseInt("11111111", 2), calcSetBitSeq(Integer.parseInt("11111111", 2), 0, 8, 0xFF));
        Assert.assertEquals(0x7E, calcSetBitSeq(0xA5, 0, 8, 0xD9 + 0xA5));
        
        // check truncation
        Assert.assertEquals(Integer.parseInt("00000010", 2), calcSetBitSeq(Integer.parseInt("00000000", 2), 1, 1, 3));
    }
}
