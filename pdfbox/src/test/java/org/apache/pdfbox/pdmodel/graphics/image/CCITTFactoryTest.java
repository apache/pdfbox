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
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tilman Hausherr
 */
public class CCITTFactoryTest extends TestCase
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * Test of createFromRandomAccess method, of class CCITTFactory.
     */
    @Test
    public void testCreateFromRandomAccess() throws Exception
    {
        PDDocument document = new PDDocument();
        RandomAccess reader = new RandomAccessFile(new File("src/test/resources/org/apache/pdfbox/pdmodel/graphics/image/ccittg4.tif"), "r");
        PDImageXObject ximage = CCITTFactory.createFromRandomAccess(document, reader);
        assertNotNull(ximage);
        assertNotNull(ximage.getCOSStream());
        assertTrue(ximage.getCOSStream().getFilteredLength() > 0);
        assertEquals(1, ximage.getBitsPerComponent());
        assertEquals(344, ximage.getWidth());
        assertEquals(287, ximage.getHeight());
        assertEquals("tiff", ximage.getSuffix());

        //TODO shouldn't ximage.getImage() return a real image?
//        assertNotNull(ximage.getImage());
//        assertEquals(344, ximage.getImage().getWidth());
//        assertEquals(287, ximage.getImage().getHeight());
        document.close();
    }

}
