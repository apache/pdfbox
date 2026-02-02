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

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PDFieldTreeTest
{

    /**
     * PDFBOX-5044 stack overflow
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test5044() throws IOException, URISyntaxException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13016994/PDFBOX-4131-0.pdf";

        try (PDDocument doc = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            int count = 0;
            for (PDField field : acroForm.getFieldTree())
            {
                ++count;
            }
            Assertions.assertEquals(4, count);
        }
    }
}
