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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

class COSWriterTest
{
    /**
     * PDFBOX-4321: check whether the output stream is closed after saving.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBox4321() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            
            PDPage page = new PDPage();
            doc.addPage(page);
            doc.save(new BufferedOutputStream(new ByteArrayOutputStream(1024)
            {
                @Override
                public void close() throws IOException
                {
                    throw new IOException("Stream was closed");
                }
            }));
        }
    }
}
