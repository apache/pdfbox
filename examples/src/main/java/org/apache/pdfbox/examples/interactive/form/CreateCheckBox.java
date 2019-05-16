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

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;

/**
 * Example to create a checkbox.
 *
 * @author Tilman Hausherr
 */
public class CreateCheckBox
{
    private CreateCheckBox()
    {
    }

    public static void main(String[] args) throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);

        // if you want to see what Adobe does, activate this, open with Adobe
        // save the file, and then open it with PDFDebugger
        //acroForm.setNeedAppearances(true)

        float x = 50;
        float y = page.getMediaBox().getHeight() - 50;

        PDRectangle rect = new PDRectangle(x, y, 20, 20);

        PDCheckBox checkbox = new PDCheckBox(acroForm);
        checkbox.setPartialName("MyCheckBox");
        PDAnnotationWidget widget = checkbox.getWidgets().get(0);
        widget.setPage(page);
        widget.setRectangle(rect);
        widget.setPrinted(true);

        PDAppearanceCharacteristicsDictionary appearanceCharacteristics = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
        appearanceCharacteristics.setBorderColour(new PDColor(new float[]{1, 0, 0}, PDDeviceRGB.INSTANCE));
        appearanceCharacteristics.setBackground(new PDColor(new float[]{1, 1, 0}, PDDeviceRGB.INSTANCE));
        // 8 = cross; 4 = checkmark; H = star; u = diamond; n = square, l = dot
        appearanceCharacteristics.setNormalCaption("4");
        widget.setAppearanceCharacteristics(appearanceCharacteristics);

        PDBorderStyleDictionary borderStyleDictionary = new PDBorderStyleDictionary();
        borderStyleDictionary.setWidth(1);
        borderStyleDictionary.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
        widget.setBorderStyle(borderStyleDictionary);

        PDAppearanceDictionary ap = new PDAppearanceDictionary();
        widget.setAppearance(ap);
        PDAppearanceEntry normalAppearance = ap.getNormalAppearance();

        COSDictionary normalAppearanceDict = (COSDictionary) normalAppearance.getCOSObject();
        normalAppearanceDict.setItem(COSName.Off, createAppearanceStream(document, widget, false));
        normalAppearanceDict.setItem(COSName.YES, createAppearanceStream(document, widget, true));

        // If we ever decide to implement a /D (down) appearance, just 
        // replace the background colors c with c * 0.75
        page.getAnnotations().add(checkbox.getWidgets().get(0));
        acroForm.getFields().add(checkbox);

        checkbox.check();

        document.save("CheckBoxSample.pdf");
        document.close();
    }

    private static PDAppearanceStream createAppearanceStream(
            final PDDocument document, PDAnnotationWidget widget, boolean on) throws IOException
    {
        PDRectangle rect = widget.getRectangle();
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics;
        PDAppearanceStream yesAP = new PDAppearanceStream(document);
        yesAP.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));
        yesAP.setResources(new PDResources());
        PDPageContentStream yesAPCS = new PDPageContentStream(document, yesAP);
        appearanceCharacteristics = widget.getAppearanceCharacteristics();
        PDColor backgroundColor = appearanceCharacteristics.getBackground();
        PDColor borderColor = appearanceCharacteristics.getBorderColour();
        float lineWidth = getLineWidth(widget);
        yesAPCS.setLineWidth(lineWidth); // border style (dash) ignored
        yesAPCS.setNonStrokingColor(backgroundColor);
        yesAPCS.addRect(0, 0, rect.getWidth(), rect.getHeight());
        yesAPCS.fill();
        yesAPCS.setStrokingColor(borderColor);
        yesAPCS.addRect(lineWidth / 2, lineWidth / 2, rect.getWidth() - lineWidth, rect.getHeight() - lineWidth);
        yesAPCS.stroke();
        if (!on)
        {
            yesAPCS.close();
            return yesAP;
        }

        yesAPCS.addRect(lineWidth, lineWidth, rect.getWidth() - lineWidth * 2, rect.getHeight() - lineWidth * 2);
        yesAPCS.clip();

        String normalCaption = appearanceCharacteristics.getNormalCaption();
        if (normalCaption == null)
        {
            normalCaption = "4"; // Adobe behaviour
        }
        if ("8".equals(normalCaption))
        {
            // Adobe paints a cross instead of using the Zapf Dingbats cross symbol
            yesAPCS.setStrokingColor(0f);
            yesAPCS.moveTo(lineWidth * 2, rect.getHeight() - lineWidth * 2);
            yesAPCS.lineTo(rect.getWidth() - lineWidth * 2, lineWidth * 2);
            yesAPCS.moveTo(rect.getWidth() - lineWidth * 2, rect.getHeight() - lineWidth * 2);
            yesAPCS.lineTo(lineWidth * 2, lineWidth * 2);
            yesAPCS.stroke();
        }
        else
        {
            // The caption is not unicode, but the Zapf Dingbats code in the PDF
            // Thus convert it back to unicode
            // Assume that only the first character is used.
            String name = PDType1Font.ZAPF_DINGBATS.codeToName(normalCaption.codePointAt(0));
            String unicode = PDType1Font.ZAPF_DINGBATS.getGlyphList().toUnicode(name);
            Rectangle2D bounds = PDType1Font.ZAPF_DINGBATS.getPath(name).getBounds2D();
            float size = (float) Math.min(bounds.getWidth(), bounds.getHeight()) / 1000;
            // assume that checkmark has square size
            // the calculations approximate what Adobe is doing, i.e. put the glyph in the middle
            float fontSize = (rect.getWidth() - lineWidth * 2) / size * 0.6666f;
            float xOffset = (float) (rect.getWidth() - (bounds.getWidth()) / 1000 * fontSize) / 2;
            xOffset -= bounds.getX() / 1000 * fontSize;
            float yOffset = (float) (rect.getHeight() - (bounds.getHeight()) / 1000 * fontSize) / 2;
            yOffset -= bounds.getY() / 1000 * fontSize;
            yesAPCS.setNonStrokingColor(0f);
            yesAPCS.beginText();
            yesAPCS.setFont(PDType1Font.ZAPF_DINGBATS, fontSize);
            yesAPCS.newLineAtOffset(xOffset, yOffset);
            yesAPCS.showText(unicode);
            yesAPCS.endText();
        }
        yesAPCS.close();
        return yesAP;
    }

    static float getLineWidth(PDAnnotationWidget widget)
    {
        PDBorderStyleDictionary bs = widget.getBorderStyle();
        if (bs != null)
        {
            return bs.getWidth();
        }
        return 1;
    }
}
