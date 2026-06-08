/*
 * Copyright 2026 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.interactive.form;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TestFieldRemover
{

    @Test
    void TestFieldRemoval() throws IOException
    {
        String inPath = "target/pdfs/PDFBOX-2469-1-AcroForm-AES128.pdf";
        String outPath = "target/test-output/PDFBOX-2469-1-AcroForm-saved.pdf";
        String fullyQualifiedFieldName = "form1[0].#subform[3].city[0]";

        try (PDDocument doc = Loader.loadPDF(new File(inPath)))
        {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField("form1[0].#subform[3].city[0]");
            assertNotNull(field);
            PDPage page4 = doc.getPage(3);
            assertEquals(48, page4.getAnnotations().size());
        }

        FieldRemover fieldRemover = new FieldRemover();
        fieldRemover.remove(inPath, outPath, fullyQualifiedFieldName);

        try (PDDocument doc = Loader.loadPDF(new File(outPath)))
        {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField("form1[0].#subform[3].city[0]");
            assertNull(field);
            PDPage page4 = doc.getPage(3);
            assertEquals(47, page4.getAnnotations().size());
        }
    }
}
