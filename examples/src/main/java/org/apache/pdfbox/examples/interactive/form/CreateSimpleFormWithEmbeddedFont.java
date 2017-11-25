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

package org.apache.pdfbox.examples.interactive.form;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * An example of creating an AcroForm and a form field from scratch with a font fully embedded to
 * allow non-WinAnsiEncoding input.
 *
 * The form field is created with properties similar to creating a form with default settings in
 * Adobe Acrobat.
 *
 */
public class CreateSimpleFormWithEmbeddedFont
{
    private CreateSimpleFormWithEmbeddedFont()
    {
    }

    public static void main(String[] args) throws IOException
    {
        // Create a new document with an empty page.
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);

        // Note that the font is fully embedded. If you use a different font, make sure that 
        // its license allows full embedding.
        PDFont formFont = PDType0Font.load(doc, CreateSimpleFormWithEmbeddedFont.class.getResourceAsStream(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"), false);

        // Add and set the resources and default appearance at the form level
        final PDResources resources = new PDResources();
        acroForm.setDefaultResources(resources);
        final String fontName = resources.add(formFont).getName();

        // Acrobat sets the font size on the form level to be
        // auto sized as default. This is done by setting the font size to '0'
        acroForm.setDefaultResources(resources);
        String defaultAppearanceString = "/" + fontName + " 0 Tf 0 g";

        PDTextField textBox = new PDTextField(acroForm);
        textBox.setPartialName("SampleField");
        textBox.setDefaultAppearance(defaultAppearanceString);
        acroForm.getFields().add(textBox);

        // Specify the widget annotation associated with the field
        PDAnnotationWidget widget = textBox.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 700, 200, 50);
        widget.setRectangle(rect);
        widget.setPage(page);
        page.getAnnotations().add(widget);

        // set the field value. Note that the last character is a turkish capital I with a dot,
        // which is not part of WinAnsiEncoding
        textBox.setValue("Sample field İ");

        doc.save("target/SimpleFormWithEmbeddedFont.pdf");
        doc.close();
    }
}
