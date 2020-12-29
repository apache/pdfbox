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
package org.apache.pdfbox.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;


/**
 * Test suite for ExtractText. 
 */
class TestExtractText
{
    
    /**
     * Run the text extraction test using a pdf with embedded pdfs.
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testEmbeddedPDFs() throws Exception 
    {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(outBytes));
        try 
        {
            ExtractText app = new ExtractText();
            CommandLine cmd = new CommandLine(app);
            int exitCode = cmd.execute("-i", "src/test/resources/org/apache/pdfbox/testPDFPackage.pdf",
            "-console", "-encoding", "UTF-8");
            assertEquals(0, exitCode);
        } 
        finally 
        {
            // Restore stdout
            System.setOut(stdout);
        }

        String result = outBytes.toString("UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
    }
}
