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
package org.apache.pdfbox.examples.pdmodel;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 *
 * @author Tilman Hausherr
 */
@Execution(ExecutionMode.CONCURRENT)
class TestHelloWorld
{
    private static final String OUTPUT_DIR = "target/test-output";

    @BeforeAll
    public static void init() throws Exception
    {
        new File(OUTPUT_DIR).mkdirs();
    }

    @Test
    void testHelloWorldTTF() throws IOException
    {
        String outputFile = OUTPUT_DIR + "/HelloWorldTTF.pdf";
        String message = "HelloWorldTTF.pdf";
        String fontFile = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";

        new File(outputFile).delete();

        String[] args = { outputFile, message, fontFile };
        HelloWorldTTF.main(args);

        checkOutputFile(outputFile, message);

        new File(outputFile).delete();
    }

    @Test
    void testHelloWorld() throws IOException
    {
        String outputDir = "target/test-output";
        String outputFile = outputDir + "/HelloWorld.pdf";
        String message = "HelloWorld.pdf";

        new File(outputFile).delete();

        String[] args = { outputFile, message };
        HelloWorld.main(args);

        checkOutputFile(outputFile, message);

        new File(outputFile).delete();
    }

    private void checkOutputFile(String outputFile, String message) throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(outputFile)))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(doc).trim();
            Assertions.assertEquals(message, extractedText);
        }
    }
}
