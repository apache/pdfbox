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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the calibration of CalGray. A neutral gray must only depend on the gamma, not on the
 * whitepoint the space is declared relative to.
 */
public class PDCalGrayTest
{
    private static final float[][] WHITEPOINTS =
    {
        { 1f, 1f, 1f },
        { 0.9642f, 1f, 0.8249f },  // D50
        { 0.9505f, 1f, 1.089f },   // D65
        { 0.95045f, 1f, 1.08905f } // D65, more precise
    };

    private static PDCalGray create(float[] whitepoint, float gamma)
    {
        PDCalGray calGray = new PDCalGray();
        PDTristimulus tristimulus = new PDTristimulus();
        tristimulus.setX(whitepoint[0]);
        tristimulus.setY(whitepoint[1]);
        tristimulus.setZ(whitepoint[2]);
        calGray.setWhitePoint(tristimulus);
        calGray.setGamma(gamma);
        return calGray;
    }

    /**
     * PDFBOX-2971 and PDFBOX-6260: white and black must stay white and black for every whitepoint
     * (white was once rendered as cyan because the whitepoint wasn't adapted).
     */
    @Test
    public void testWhiteAndBlack()
    {
        for (float[] whitepoint : WHITEPOINTS)
        {
            PDCalGray calGray = create(whitepoint, 2.2f);
            assertEquals(1f, calGray.toRGB(new float[] { 1f })[0], 0.01f);
            assertEquals(1f, calGray.toRGB(new float[] { 1f })[1], 0.01f);
            assertEquals(1f, calGray.toRGB(new float[] { 1f })[2], 0.01f);
            assertEquals(0f, calGray.toRGB(new float[] { 0f })[0], 0.01f);
            assertEquals(0f, calGray.toRGB(new float[] { 0f })[1], 0.01f);
            assertEquals(0f, calGray.toRGB(new float[] { 0f })[2], 0.01f);
        }
    }

    /**
     * A gray must be neutral and must not depend on the whitepoint, only on the gamma.
     */
    @Test
    public void testIndependentOfWhitepoint()
    {
        for (float gamma : new float[] { 1f, 1.8f, 2.2f, 3f })
        {
            float[] reference = create(WHITEPOINTS[0], gamma).toRGB(new float[] { 0.5f });
            for (float[] whitepoint : WHITEPOINTS)
            {
                float[] rgb = create(whitepoint, gamma).toRGB(new float[] { 0.5f });
                for (int i = 0; i < 3; i++)
                {
                    assertEquals("not neutral, gamma " + gamma, rgb[0], rgb[i], 0.01f);
                    assertEquals("gamma " + gamma, reference[i], rgb[i], 0.01f);
                }
            }
        }
    }

    /**
     * The gamma must be applied also for a whitepoint other than (1 1 1); before, it was ignored.
     * The expected values are the sRGB encoding of the linear value 0.5^gamma.
     */
    @Test
    public void testGamma()
    {
        for (float[] whitepoint : WHITEPOINTS)
        {
            // linear value 0.5 is 0.735 in sRGB
            assertEquals(0.735f, create(whitepoint, 1f).toRGB(new float[] { 0.5f })[0], 0.01f);
            // gamma 2.2 is close to the sRGB curve, so the value stays about the same
            assertEquals(0.5f, create(whitepoint, 2.2f).toRGB(new float[] { 0.5f })[0], 0.02f);
        }
    }
}
