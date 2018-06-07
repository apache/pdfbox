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
import org.junit.Test;

public class COSWriterTest
{
    /**
     * PDFBOX-4241: check whether the output stream is closed twice.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox4241() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        doc.save(new BufferedOutputStream(new ByteArrayOutputStream(1024)
        {
            private boolean open = true;

            @Override
            public void close() throws IOException
            {
                //Thread.dumpStack();

                open = false;
                super.close();
            }

            @Override
            public void flush() throws IOException
            {
                if (!open)
                {
                    throw new IOException("Stream already closed");
                }

                //Thread.dumpStack();
            }
        }));
        doc.close();
    }
}
