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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Unit tests for CCITTFactory
 * @author Tilman Hausherr
 */
public class CCITTFactoryTest extends TestCase
{
    /**
     * Tests CCITTFactory#createFromRandomAccess(PDDocument document, RandomAccess reader)
     */
    public void testCreateFromRandomAccess() throws IOException
    {
        String tiffPath = "src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4.tif";
        PDDocument document = new PDDocument();
        RandomAccess reader = new RandomAccessFile(new File(tiffPath), "r");
        PDImageXObject ximage = CCITTFactory.createFromRandomAccess(document, reader);

        // check the dictionary
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(1, ximage.getBitsPerComponent());
        assertEquals(344, ximage.getWidth());
        assertEquals(287, ximage.getHeight());
        assertEquals("tiff", ximage.getSuffix());

        // check the image
        assertNotNull(ximage.getImage());
        assertEquals(344, ximage.getImage().getWidth());
        assertEquals(287, ximage.getImage().getHeight());

        document.close();
    }
}
