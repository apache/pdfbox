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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.pdmodel.PDDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDICCBasedTest
{

    public PDICCBasedTest()
    {
    }

    /**
     * Test of Constructor for PDFBOX-2812.
     */
    @Test
    public void testConstructor()
    {
        PDDocument doc = new PDDocument();
        PDICCBased iccBased = new PDICCBased(doc);
        assertEquals("ICCBased", iccBased.getName());
        assertNotNull(iccBased.getPDStream());
    }

}
