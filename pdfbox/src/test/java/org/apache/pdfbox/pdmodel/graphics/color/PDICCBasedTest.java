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

import java.awt.color.ColorSpace;
import java.io.IOException;
import junit.framework.Assert;
import static junit.framework.Assert.assertFalse;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;


/**
 *
 * @author Tilman Hausherr
 */
public class PDICCBasedTest extends TestCase
{

    public PDICCBasedTest()
    {
    }

    /**
     * Test of Constructor for PDFBOX-2812.
     */
    public void testConstructor() throws IOException
    {
        PDDocument doc = new PDDocument();
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        assertFalse(cs.isCS_sRGB()); // this test doesn't work with CS_sRGB
        PDICCBased iccBased = (PDICCBased) PDColorSpaceFactory.createColorSpace(doc, cs);
        Assert.assertEquals("ICCBased", iccBased.getName());
    }

}
