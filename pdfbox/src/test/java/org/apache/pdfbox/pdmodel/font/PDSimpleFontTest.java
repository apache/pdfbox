/*
 *  Copyright 2011 adam.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.pdfbox.pdmodel.font;

import java.awt.image.BufferedImage;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author adam
 */
public class PDSimpleFontTest {

    public PDSimpleFontTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getawtFont method, of class PDSimpleFont.
     */
    @Test
    public void testPDFBox988() throws Exception {
        PDDocument doc = null;
        try {
           doc = PDDocument.load(PDSimpleFontTest.class.getResourceAsStream("F001u_3_7j.pdf"));
           List pages = doc.getDocumentCatalog().getAllPages();
           PDPage page = (PDPage)pages.get(0);
           BufferedImage image = page.convertToImage();
           // The alligation is that convertToImage() will crash the JVM or hang
        } finally {
            if(doc != null) {
                doc.close();
            }
        }
    }
}