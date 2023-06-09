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

package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
class PDAcroFormGenerateAppearancesTest
{
    @ParameterizedTest
    @ValueSource(strings =
    {
        // PDFBOX-5041 Missing font descriptor
        "https://issues.apache.org/jira/secure/attachment/13016941/REDHAT-1301016-0.pdf",

        // PDFBOX-4086 Character missing for encoding
        "https://issues.apache.org/jira/secure/attachment/12908175/AML1.PDF",

        // PDFBOX-5043 PaperMetaData
        "https://issues.apache.org/jira/secure/attachment/13016992/PDFBOX-3891-5.pdf"
    })
    void testGetAcroForm(String sourceUrl) throws IOException, URISyntaxException
    {
        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();

            assertDoesNotThrow(() -> catalog.getAcroForm(), "Getting the AcroForm shall not throw an exception");
        }
    }
}
