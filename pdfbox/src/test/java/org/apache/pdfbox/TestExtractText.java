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
package org.apache.pdfbox;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

/**
 * Test suite for ExtractText. 
 */
public class TestExtractText extends TestCase
{
    
    /**
     * Run the text extraction test using a pdf with embedded pdfs.
     * 
     * @throws Exception if something went wrong
     */
    public void testEmbeddedPDFs() throws Exception 
    {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(outBytes));
        try 
        {
            ExtractText.main(new String[] {"src/test/resources/org/apache/pdfbox/testPDFPackage.pdf", 
                    "-console", "-encoding UTF-8"});
        } 
        finally 
        {
            // Restore stdout
            System.setOut(stdout);
        }

        String result = outBytes.toString("UTF-8");
        assertTrue(result.indexOf("PDF1") != -1);
        assertTrue(result.indexOf("PDF2") != -1);
    }
}
