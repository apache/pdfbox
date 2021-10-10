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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TestQuality
{
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    /**
     * PDFBOX-4831: PDF with a 300 dpi bitonal scan must be bitonal when rendered PDF at 300 dpi.
     *
     * @throws IOException 
     */
    @Test
    void testPDFBox4831() throws IOException
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-4831.pdf");
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage bim = renderer.renderImageWithDPI(0, 300);
            Assertions.assertEquals(4, ValidateXImage.colorCount(bim)); //TODO must be 2 when fixed
        }
    }
}
