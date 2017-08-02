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
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the FDFAnnotation class.
 * 
 * @author Johanneke Lamberink
 *
 */
public class FDFAnnotationTest
{
    @Test
    public void loadXFDFAnnotations() throws IOException, URISyntaxException
    {
        File f = new File(FDFAnnotationTest.class.getResource("xfdf-test-document-annotations.xml").toURI());
        try (FDFDocument fdfDoc = FDFDocument.loadXFDF(f))
        {
            List<FDFAnnotation> fdfAnnots = fdfDoc.getCatalog().getFDF().getAnnotations();
            assertEquals(17, fdfAnnots.size());
        }
    }
}