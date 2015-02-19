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

import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDTransitionDirectionTest
{

    @Test
    public void getCOSBase()
    {
        assertEquals(COSName.NONE, PDTransitionDirection.NONE.getCOSBase());
        assertEquals(0, ((COSInteger) PDTransitionDirection.LEFT_TO_RIGHT.getCOSBase()).intValue());
        assertEquals(90, ((COSInteger) PDTransitionDirection.BOTTOM_TO_TOP.getCOSBase()).intValue());
        assertEquals(180,
                ((COSInteger) PDTransitionDirection.RIGHT_TO_LEFT.getCOSBase()).intValue());
        assertEquals(270,
                ((COSInteger) PDTransitionDirection.TOP_TO_BOTTOM.getCOSBase()).intValue());
        assertEquals(315,
                ((COSInteger) PDTransitionDirection.TOP_LEFT_TO_BOTTOM_RIGHT.getCOSBase())
                        .intValue());
    }

}
