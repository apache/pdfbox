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

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDAnnotationAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;

/**
 *
 * @author Tilman Hausherr
 */
public class CreatePushButton
{
    public static void main(String[] args) throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDAcroForm acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
            PDPushButton pushButton = new PDPushButton(acroForm);
            pushButton.setPartialName("push");
            acroForm.getFields().add(pushButton);
            PDAnnotationWidget widget = pushButton.getWidgets().get(0);
            page.getAnnotations().add(widget);
            widget.setRectangle(new PDRectangle(50, 500, 100, 100)); // position on the page
            widget.setPrinted(true);
            widget.setPage(page);
            PDActionJavaScript javascriptAction = new PDActionJavaScript("app.alert(\"button pressed\")");
            PDAnnotationAdditionalActions actions = new PDAnnotationAdditionalActions();
            actions.setU(javascriptAction);
            widget.setActions(actions);
            // Create a PDFormXObject
            PDFormXObject form = new PDFormXObject(doc);
            form.setResources(new PDResources());
            form.setBBox(new PDRectangle(100, 100));
            form.setFormType(1);
            PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary(new COSDictionary());
            widget.setAppearance(appearanceDictionary);
            PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
            appearanceDictionary.setNormalAppearance(appearanceStream);
            // Create the content stream
            BufferedImage bim = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB); // black
            PDImageXObject image = LosslessFactory.createFromImage(doc, bim);
            try (PDAppearanceContentStream cs = new PDAppearanceContentStream(appearanceStream))
            {
                cs.drawImage(image, 0, 0);
            }
            doc.save("target/PushButtonSample.pdf");
        }
    }
}
