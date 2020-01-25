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
import org.apache.pdfbox.ParallelParameterized;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

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
@RunWith(ParallelParameterized.class)
public class TestRendering
{
    private static final String INPUT_DIR = "src/test/resources/input/rendering";

    @Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        File[] testFiles = new File(INPUT_DIR).listFiles(
                (dir, name) -> (name.endsWith(".pdf") || name.endsWith(".ai")));
        return Stream.of(testFiles).map(file -> new Object[] { file.getName() }).collect(Collectors.toList());
    }

    private final String fileName;

    public TestRendering(String fileName)
    {
        this.fileName = fileName;
    }

    @Test
    public void render() throws IOException
    {
        File file = new File(INPUT_DIR, fileName);
        PDDocument document = Loader.loadPDF(file);
        PDFRenderer renderer = new PDFRenderer(document);
        renderer.renderImage(0);

        // We don't actually do anything with the image for the same reason that
        // TestPDFToImage is disabled - different JVMs produce different results
        // but at least we can make sure that PDFBox did not throw any exceptions
        // during the rendering process.

        document.close();
    }
}
