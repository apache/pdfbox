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

package org.apache.fontbox.afm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.fontbox.util.BoundingBox;
import org.junit.jupiter.api.Test;

class CharMetricTest
{
    @Test
    void testCharMetricSimpleValues()
    {
        CharMetric charMetric = new CharMetric();
        charMetric.setCharacterCode(0);
        charMetric.setName("name");
        charMetric.setWx(10f);
        charMetric.setW0x(20f);
        charMetric.setW1x(30f);
        charMetric.setWy(40f);
        charMetric.setW0y(50f);
        charMetric.setW1y(60f);

        assertEquals(0, charMetric.getCharacterCode());
        assertEquals("name", charMetric.getName());
        assertEquals(10f, charMetric.getWx(), 0.0f);
        assertEquals(20f, charMetric.getW0x(), 0.0f);
        assertEquals(30f, charMetric.getW1x(), 0.0f);
        assertEquals(40f, charMetric.getWy(), 0.0f);
        assertEquals(50f, charMetric.getW0y(), 0.0f);
        assertEquals(60f, charMetric.getW1y(), 0.0f);
    }

    @Test
    void testCharMetricArrayValues()
    {
        CharMetric charMetric = new CharMetric();
        charMetric.setW(new float[] { 10f, 20f });
        charMetric.setW0(new float[] { 30f, 40f });
        charMetric.setW1(new float[] { 50f, 60f });
        charMetric.setVv(new float[] { 70f, 80f });
        assertEquals(10f, charMetric.getW()[0], 0.0f);
        assertEquals(20f, charMetric.getW()[1], 0.0f);
        assertEquals(30f, charMetric.getW0()[0], 0.0f);
        assertEquals(40f, charMetric.getW0()[1], 0.0f);
        assertEquals(50f, charMetric.getW1()[0], 0.0f);
        assertEquals(60f, charMetric.getW1()[1], 0.0f);
        assertEquals(70f, charMetric.getVv()[0], 0.0f);
        assertEquals(80f, charMetric.getVv()[1], 0.0f);
    }

    @Test
    void testCharMetricComplexValues()
    {
        CharMetric charMetric = new CharMetric();
        charMetric.setBoundingBox(new BoundingBox(10, 20, 30, 40));
        assertEquals(10, charMetric.getBoundingBox().getLowerLeftX(), 0);
        assertEquals(20, charMetric.getBoundingBox().getLowerLeftY(), 0);
        assertEquals(30, charMetric.getBoundingBox().getUpperRightX(), 0);
        assertEquals(40, charMetric.getBoundingBox().getUpperRightY(), 0);

        assertEquals(0, charMetric.getLigatures().size());
        Ligature ligature = new Ligature("successor", "ligature");
        charMetric.addLigature(ligature);
        List<Ligature> ligatures = charMetric.getLigatures();
        assertEquals(1, ligatures.size());
        assertEquals("successor", ligatures.get(0).getSuccessor());
        try
        {
            ligatures.add(ligature);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }
}
