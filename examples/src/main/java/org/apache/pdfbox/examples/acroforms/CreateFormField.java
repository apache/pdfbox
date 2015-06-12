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

package org.apache.pdfbox.examples.acroforms;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * An example of creating an AcroForm and a form field from scratch.
 */
public class CreateFormField
{
    public static void main(String[] args) throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        PDType0Font font = PDType0Font.load(document, new File(dir + "LiberationSans-Regular.ttf"));
        
        // add a new AcroForm and add that to the document
        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);
        
        // Add and set the resources and default appearance at the form level
        PDResources res = new PDResources();
        COSName fontName = res.add(font);
        acroForm.setDefaultResources(res);
        String da = "/" + fontName.getName() + " 12 Tf 0 g";
        acroForm.setDefaultAppearance(da);
        
        // add a form field to the form
        PDTextField textBox = new PDTextField(acroForm);
        textBox.setPartialName("SampleField");
        acroForm.getFields().add(textBox);
        
        // specify the annotation associated with the field
        // and add it to the page
        PDAnnotationWidget widget = textBox.getWidgets().get(0);
        PDRectangle rect = new PDRectangle();
        rect.setLowerLeftX(50);
        rect.setLowerLeftY(750);
        rect.setUpperRightX(250);
        rect.setUpperRightY(800);
        widget.setRectangle(rect);
        
        // add a green border
        PDAppearanceCharacteristicsDictionary fieldAppearance = 
                new PDAppearanceCharacteristicsDictionary(new COSDictionary());
        PDColor green = new PDColor(new float[] { 0, 1, 0 }, PDDeviceRGB.INSTANCE);
        fieldAppearance.setBorderColour(green);
        widget.setAppearanceCharacteristics(fieldAppearance);
        
        page.getAnnotations().add(widget);
        
        // set the field value
        textBox.setValue("English form contents");

        document.save("exampleForm.pdf");
        document.close();
    }
}
