/*
 *  Copyright 2010 adam.
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

package org.apache.pdfbox.pdfparser;

import java.io.File;
import java.net.URL;
import org.apache.pdfbox.cos.COSDictionary;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author adam
 */
public class ConformingPDFParserTest {

    public ConformingPDFParserTest() {
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
     * Test of parse method, of class ConformingPDFParser.
     */
    @Test
    public void testParse() throws Exception {
        URL inputUrl = ConformingPDFParser.class.getResource("gdb-refcard.pdf");
        File inputFile = new File(inputUrl.toURI());
        ConformingPDFParser instance = new ConformingPDFParser(inputFile);
        instance.parse();
        
        COSDictionary trailer = instance.getDocument().getTrailer();
        assertNotNull(trailer);
        System.out.println("Trailer: " + instance.getDocument().getTrailer().toString());
        assertEquals(3, trailer.size());
        assertNotNull(trailer.getDictionaryObject("Root"));
        assertNotNull(trailer.getDictionaryObject("Info"));
        assertNotNull(trailer.getDictionaryObject("Size"));
    }
}