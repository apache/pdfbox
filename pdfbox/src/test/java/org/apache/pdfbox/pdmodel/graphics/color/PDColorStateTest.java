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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test cases for the {@link PDColorState} class.
 */
public class PDColorStateTest extends TestCase
{

    /**
     * This will test setting field flags on the PDField.
     *
     * @throws IOException If there is an error creating the field.
     */
    public void testUnsupportedColorSpace() throws IOException
    {
        PDColorState state = new PDColorState();
        state.setColorSpace(new UnsupportedColorSpace());
        try {
            assertEquals(Color.BLACK, state.getJavaColor());
            // TODO: Check for the warning log message
        } catch (IOException e) {
            fail("PDFBOX-580: Use a dummy color instead of"
                    + " failing with unsupported color spaces");
        }
    }

    /**
     * Dummy class used by the
     * {@link PDColorStateTest#testUnsupportedColorSpace()} method.
     */
    private static class UnsupportedColorSpace extends PDColorSpace {

        public int getNumberOfComponents() throws IOException {
            return 1;
        }

        public String getName() {
            return "unsupported color space";
        }

        protected ColorSpace createColorSpace() throws IOException {
            throw new IOException("unsupported color space");
        }

        public ColorModel createColorModel(int bpc) throws IOException {
            throw new IOException("unsupported color space");
        }

    }


}
