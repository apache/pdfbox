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
package org.apache.pdfbox.pdmodel.interactive.pagenavigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.junit.jupiter.api.Test;

/**
 * @author Andrea Vacondio
 *
 */
class PDTransitionTest
{

    @Test
    void defaultStyle()
    {
        PDTransition transition = new PDTransition();
        assertEquals(COSName.TRANS, transition.getCOSObject().getCOSName(COSName.TYPE));
        assertEquals(PDTransitionStyle.R.name(), transition.getStyle());
    }

    @Test
    void getStyle()
    {
        PDTransition transition = new PDTransition(PDTransitionStyle.Fade);
        assertEquals(COSName.TRANS, transition.getCOSObject().getCOSName(COSName.TYPE));
        assertEquals(PDTransitionStyle.Fade.name(), transition.getStyle());
    }

    @Test
    void defaultValues()
    {
        PDTransition transition = new PDTransition(new COSDictionary());
        assertEquals(PDTransitionStyle.R.name(), transition.getStyle());
        assertEquals(PDTransitionDimension.H.name(), transition.getDimension());
        assertEquals(PDTransitionMotion.I.name(), transition.getMotion());
        assertEquals(COSInteger.ZERO, transition.getDirection());
        assertEquals(1, transition.getDuration(), 0);
        assertEquals(1, transition.getFlyScale(), 0);
        assertFalse(transition.isFlyAreaOpaque());
    }

    @Test
    void dimension()
    {
        PDTransition transition = new PDTransition();
        transition.setDimension(PDTransitionDimension.H);
        assertEquals(PDTransitionDimension.H.name(), transition.getDimension());
    }

    @Test
    void directionNone()
    {
        PDTransition transition = new PDTransition();
        transition.setDirection(PDTransitionDirection.NONE);
        assertEquals(COSName.class.getName(), transition.getDirection().getClass().getName());
        assertEquals(COSName.NONE, transition.getDirection());
    }

    @Test
    void directionNumber()
    {
        PDTransition transition = new PDTransition();
        transition.setDirection(PDTransitionDirection.LEFT_TO_RIGHT);
        assertEquals(COSInteger.class.getName(), transition.getDirection().getClass().getName());
        assertEquals(COSInteger.ZERO, transition.getDirection());
    }

    @Test
    void motion()
    {
        PDTransition transition = new PDTransition();
        transition.setMotion(PDTransitionMotion.O);
        assertEquals(PDTransitionMotion.O.name(), transition.getMotion());
    }

    @Test
    void duration()
    {
        PDTransition transition = new PDTransition();
        transition.setDuration(4);
        assertEquals(4, transition.getDuration(), 0);
    }

    @Test
    void flyScale()
    {
        PDTransition transition = new PDTransition();
        transition.setFlyScale(4);
        assertEquals(4, transition.getFlyScale(), 0);
    }

    @Test
    void flyArea()
    {
        PDTransition transition = new PDTransition();
        transition.setFlyAreaOpaque(true);
        assertTrue(transition.isFlyAreaOpaque());
    }
}
