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
package org.apache.pdfbox.pdfwriter;

import java.io.IOException;

import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class COSWriterCompressionPoolTest
{
    /**
     * The old implementation may run into a stack overflow whenever the recursion depth gets too deep to be processed
     * when collecting the objects to be compressed.
     * 
     * The new solution replaces the recursion with an iteration.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBox6036() throws IOException
    {
        for (int i = 1; i <= 222_222; i *= 2)
        {
            try (PDDocument document = new PDDocument())
            {
                PDDocumentOutline outline = new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline(outline);
                for (int j = 0; j < i; j++)
                {
                    outline.addLast(new PDOutlineItem());
                }
                Assertions.assertDoesNotThrow(() ->
                        new COSWriterCompressionPool(document, CompressParameters.DEFAULT_COMPRESSION));
            }
        }
    }

}
