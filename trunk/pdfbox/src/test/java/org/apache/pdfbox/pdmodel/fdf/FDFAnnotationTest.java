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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.pdfbox.Loader;

import org.junit.jupiter.api.Test;

/**
 * Tests for the FDFAnnotation class.
 * 
 * @author Johanneke Lamberink
 *
 */
class FDFAnnotationTest
{
    @Test
    void loadXFDFAnnotations() throws IOException, URISyntaxException
    {
        File f = new File(FDFAnnotationTest.class.getResource("xfdf-test-document-annotations.xml").toURI());
        try (FDFDocument fdfDoc = Loader.loadXFDF(f))
        {
            List<FDFAnnotation> fdfAnnots = fdfDoc.getCatalog().getFDF().getAnnotations();
            assertEquals(18, fdfAnnots.size());
            
            // test PDFBOX-4345 and PDFBOX-3646
            // before the fix, the richtext output was
            // <body style="font:12pt Helvetica; color:#D66C00;" xfa:APIVersion="Acrobat:7.0.8" xfa:spec="2.0.2" xmlns="http://www.w3.org/1999/xhtml" xmlns:xfa="http://www.xfa.org/schema/xfa-data/1.0/"><p dir="ltr"><span style="text-decoration:word;font-family:Helvetica">P&2</span></p></body>
            // i.e. the & was not escaped, and P&amp;1 and P&amp;3 was missing
            boolean testedPDFBox4345andPDFBox3646 = false;
            for (FDFAnnotation ann : fdfAnnots)
            {
                if (ann instanceof FDFAnnotationFreeText)
                {
                    FDFAnnotationFreeText annotationFreeText = (FDFAnnotationFreeText) ann;
                    if ("P&1 P&2 P&3".equals(annotationFreeText.getContents()))
                    {
                        testedPDFBox4345andPDFBox3646 = true;
                        assertEquals("<body style=\"font:12pt Helvetica; "
                                + "color:#D66C00;\" xfa:APIVersion=\"Acrobat:7.0.8\" "
                                + "xfa:spec=\"2.0.2\" xmlns=\"http://www.w3.org/1999/xhtml\" "
                                + "xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">\n" 
                                + "          <p dir=\"ltr\">P&amp;1 <span style=\"text-"
                                + "decoration:word;font-family:Helvetica\">P&amp;2</span> "
                                + "P&amp;3</p>\n"
                                + "        </body>", annotationFreeText.getRichContents().trim());
                    }
                }
            }
            assertTrue(testedPDFBox4345andPDFBox3646);
        }
    }
}
