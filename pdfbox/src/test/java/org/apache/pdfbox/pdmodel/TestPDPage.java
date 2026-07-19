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
package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestPDPage
{
    @Test
    void testAddingPageAfterCreatingAnnotation() throws IOException
    {
        // PDFBOX-6097
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            // Create AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Create a single text field
            PDTextField textField = new PDTextField(acroForm);
            textField.setPartialName("testField");
            PDAnnotationWidget widget = textField.getWidgets().get(0);
            widget.setRectangle(new PDRectangle(100, 700, 200, 20));
            widget.setPage(page);
            page.getAnnotations().add(widget);
            acroForm.getFields().add(textField);

            // Adding page AFTER creating form fields causes a StackOverflowError
            assertDoesNotThrow(() -> document.addPage(page));

            document.save(OutputStream.nullOutputStream());
            document.close();
        }
    }
    
    @Test
    void testNullThreadBeads() throws IOException
    {
        // PDFBOX-6186
        PDPage page = new PDPage();
        assertEquals(0, page.getThreadBeads().size());
        page.setThreadBeads(new ArrayList<>());
        assertEquals(0, page.getThreadBeads().size());
        page.setThreadBeads(null);
        assertEquals(0, page.getThreadBeads().size());
    }
}
