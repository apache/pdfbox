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

package org.apache.pdfbox.rendering;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Functional test for PDF rendering. This test simply tries to render
 * a series of PDFs using PDFBox to make sure that no exceptions are thrown.
 *
 * It does not attempt to detect if rendering is correct, see {@link org.apache.pdfbox.rendering.TestPDFToImage}.
 *
 * @author John Hewson
 */
@Execution(ExecutionMode.CONCURRENT)
class TestRendering
{
    private static final String INPUT_DIR = "src/test/resources/input/rendering";
    private static final String OUTPUT_DIR = "target/test-output/rendering";
    private static final int MAX_NUM_FILES = 20;

    private static Collection<Arguments> data()
    {
        File[] testFiles = new File(INPUT_DIR).listFiles(
                (dir, name) -> (name.endsWith(".pdf") || name.endsWith(".ai")));
        return Stream.of(testFiles).map(file -> Arguments.of(file.getName())).collect(Collectors.toList());
    }

    private static Collection<Arguments> dataSubset()
    {
        File[] testFiles = new File(INPUT_DIR).listFiles(
                (dir, name) -> (name.endsWith(".pdf") || name.endsWith(".ai")));
        return Stream.of(testFiles).map(file -> Arguments.of(file.getName())).limit(MAX_NUM_FILES).collect(Collectors.toList());
    }

    @ParameterizedTest(name = "{index} render running for {0}")
    @MethodSource("dataSubset")
    void render(String fileName) throws IOException
    {
        File file = new File(INPUT_DIR, fileName);
        try (PDDocument document = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(document);
            
            // We don't actually do anything with the image for the same reason that
            // TestPDFToImage is disabled - different JVMs produce different results
            // but at least we can make sure that PDFBox did not throw any exceptions
            // during the rendering process.
            
            assertDoesNotThrow(() -> renderer.renderImage(0));
        }
    }

    /*
     * Test currently disabled as different JVMs produce different results.
     * Enable and visually inspect failing tests files.
     */
    // @ParameterizedTest(name = "{index} render and compare running for {0}")
    @MethodSource("data")
    void renderAndCompare(String fileName) throws IOException
    {

        new File(OUTPUT_DIR).mkdirs();
        // compare rendering
        if (!TestPDFToImage.doTestFile(new File(INPUT_DIR, fileName), INPUT_DIR, OUTPUT_DIR))
        {
            fail("Rendering of " + fileName + " failed or is not identical to expected rendering in " + INPUT_DIR + " directory");
        }
    }
}
