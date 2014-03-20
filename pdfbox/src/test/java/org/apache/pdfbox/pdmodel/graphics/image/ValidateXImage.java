/*
 * Copyright 2014 The Apache Software Foundation.
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

package org.apache.pdfbox.pdmodel.graphics.image;

import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Helper class to do some validations for PDImageXObject.
 * 
 * @author Tilman Hausherr
 */
public class ValidateXImage
{
    static public void validate(PDImageXObject ximage, int bpc, int width, int height, String format) throws IOException
    {
        // check the dictionary
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(bpc, ximage.getBitsPerComponent());
        assertEquals(width, ximage.getWidth());
        assertEquals(height, ximage.getHeight());
        assertEquals(format, ximage.getSuffix());

        // check the image
        assertNotNull(ximage.getImage());
        assertEquals(ximage.getWidth(), ximage.getImage().getWidth());
        assertEquals(ximage.getHeight(), ximage.getImage().getHeight());

        boolean writeOk = ImageIOUtil.writeImage(ximage.getImage(), format, new NullOutputStream());
        assertTrue(writeOk);
    }

}
