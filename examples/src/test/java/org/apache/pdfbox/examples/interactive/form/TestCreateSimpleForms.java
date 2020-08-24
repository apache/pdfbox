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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of some the form examples.
 *
 * @author Tilman Hausherr
 */
public class TestCreateSimpleForms
{
    public TestCreateSimpleForms()
    {
    }

    /**
     * Test of CreateSimpleForm
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateSimpleForm() throws IOException
    {
        CreateSimpleForm.main(null);
        PDDocument doc = PDDocument.load(new File("target/SimpleForm.pdf"));
        new PDFRenderer(doc).renderImage(0);
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDTextField textBox = (PDTextField) acroForm.getField("SampleField");
        Assert.assertEquals("Sample field content", textBox.getValue());
        try
        {
            textBox.setValue("Łódź");
            Assert.fail("should have failed with IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
            Assert.assertTrue(ex.getMessage().contains("U+0141 ('Lslash') is not available"));
        }

        PDFont font = getFontFromWidgetResources(textBox, "Helv");
        Assert.assertEquals("Helvetica", font.getName());
        Assert.assertTrue(font.isStandard14());
        doc.close();
    }

    @Test
    public void testAddBorderToField() throws IOException
    {
        CreateSimpleForm.main(null);

        PDDocument doc = PDDocument.load(new File("target/SimpleForm.pdf"));
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDTextField textBox = (PDTextField) acroForm.getField("SampleField");
        PDAnnotationWidget widget = textBox.getWidgets().get(0);
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics = widget.getAppearanceCharacteristics();
        PDColor borderColour = appearanceCharacteristics.getBorderColour();
        PDColor backgroundColour = appearanceCharacteristics.getBackground();
        Assert.assertEquals(PDDeviceRGB.INSTANCE, borderColour.getColorSpace());
        Assert.assertEquals(PDDeviceRGB.INSTANCE, backgroundColour.getColorSpace());
        Assert.assertArrayEquals(new float[]{0,1,0}, borderColour.getComponents(), 0);
        Assert.assertArrayEquals(new float[]{1,1,0}, backgroundColour.getComponents(), 0);
        doc.close();

        AddBorderToField.main(null);

        PDDocument doc2 = PDDocument.load(new File("target/AddBorderToField.pdf"));
        new PDFRenderer(doc2).renderImage(0);
        PDAcroForm acroForm2 = doc2.getDocumentCatalog().getAcroForm();
        PDTextField textBox2 = (PDTextField) acroForm2.getField("SampleField");
        PDAnnotationWidget widget2 = textBox2.getWidgets().get(0);
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics2 = widget2.getAppearanceCharacteristics();
        PDColor borderColour2 = appearanceCharacteristics2.getBorderColour();
        Assert.assertEquals(PDDeviceRGB.INSTANCE, borderColour2.getColorSpace());
        Assert.assertArrayEquals(new float[]{1,0,0}, borderColour2.getComponents(), 0);

        doc2.close();
    }

    /**
     * Test of CreateSimpleFormWithEmbeddedFont
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateSimpleFormWithEmbeddedFont() throws IOException
    {
        CreateSimpleFormWithEmbeddedFont.main(null);
        PDDocument doc = PDDocument.load(new File("target/SimpleFormWithEmbeddedFont.pdf"));
        new PDFRenderer(doc).renderImage(0);
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDTextField textBox = (PDTextField) acroForm.getField("SampleField");
        Assert.assertEquals("Sample field İ", textBox.getValue());
        textBox.setValue("Łódź");
        PDFont font = getFontFromWidgetResources(textBox, "F1");
        Assert.assertEquals("LiberationSans", font.getName());
        doc.close();
    }

    /**
     * Test of CreateSimpleFormWithEmbeddedFont
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMultiWidgetsForm() throws IOException
    {
        CreateMultiWidgetsForm.main(null);

        PDDocument doc = PDDocument.load(new File("target/MultiWidgetsForm.pdf"));
        Assert.assertEquals(2, doc.getNumberOfPages());
        new PDFRenderer(doc).renderImage(0);
        new PDFRenderer(doc).renderImage(1);
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDTextField textBox = (PDTextField) acroForm.getField("SampleField");
        Assert.assertEquals("Sample field", textBox.getValue());
        List<PDAnnotationWidget> widgets = textBox.getWidgets();
        Assert.assertEquals(2, widgets.size());
        PDAnnotationWidget w1 = widgets.get(0);
        PDAnnotationWidget w2 = widgets.get(1);
        PDPage page1 = w1.getPage();
        PDPage page2 = w2.getPage();
        Assert.assertNotEquals(page1.getCOSObject(), page2.getCOSObject());
        Assert.assertEquals(page1, doc.getPage(0));
        Assert.assertEquals(page2, doc.getPage(1));
        Assert.assertEquals(page1.getAnnotations().get(0), w1);
        Assert.assertEquals(page2.getAnnotations().get(0), w2);
        Assert.assertNotEquals(w1, w2);
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics1 = w1.getAppearanceCharacteristics();
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics2 = w2.getAppearanceCharacteristics();
        PDColor backgroundColor1 = appearanceCharacteristics1.getBackground();
        PDColor backgroundColor2 = appearanceCharacteristics2.getBackground();
        PDColor borderColour1 = appearanceCharacteristics1.getBorderColour();
        PDColor borderColour2 = appearanceCharacteristics2.getBorderColour();
        Assert.assertEquals(PDDeviceRGB.INSTANCE, backgroundColor1.getColorSpace());
        Assert.assertEquals(PDDeviceRGB.INSTANCE, backgroundColor2.getColorSpace());
        Assert.assertEquals(PDDeviceRGB.INSTANCE, borderColour1.getColorSpace());
        Assert.assertEquals(PDDeviceRGB.INSTANCE, borderColour2.getColorSpace());
        Assert.assertArrayEquals(new float[]{1,1,0}, backgroundColor1.getComponents(), 0);
        Assert.assertArrayEquals(new float[]{0,1,0}, backgroundColor2.getComponents(), 0);
        Assert.assertArrayEquals(new float[]{0,1,0}, borderColour1.getComponents(), 0);
        Assert.assertArrayEquals(new float[]{1,0,0}, borderColour2.getComponents(), 0);
        doc.close();
    }

    @Test
    public void testCreateCheckBox() throws IOException
    {
        CreateCheckBox.main(null);
        PDDocument doc1 = PDDocument.load(new File("target/CheckBoxSample.pdf"));
        new PDFRenderer(doc1).renderImage(0);
        PDAcroForm acroForm1 = doc1.getDocumentCatalog().getAcroForm();
        PDCheckBox checkbox1 = (PDCheckBox) acroForm1.getField("MyCheckBox");
        Assert.assertEquals("Yes", checkbox1.getOnValue());
        Assert.assertEquals("Off", checkbox1.getValue());
        checkbox1.check();
        Assert.assertEquals("Yes", checkbox1.getValue());
        doc1.save("target/CheckBoxSample-modified.pdf");
        doc1.close();

        PDDocument doc2 = PDDocument.load(new File("target/CheckBoxSample-modified.pdf"));
        new PDFRenderer(doc2).renderImage(0);
        PDAcroForm acroForm2 = doc2.getDocumentCatalog().getAcroForm();
        PDCheckBox checkbox2 = (PDCheckBox) acroForm2.getField("MyCheckBox");
        Assert.assertEquals("Yes", checkbox2.getValue());
        doc2.close();
    }

    @Test
    public void testRadioButtons() throws IOException
    {
        CreateRadioButtons.main(null);
        PDDocument doc1 = PDDocument.load(new File("target/RadioButtonsSample.pdf"));
        new PDFRenderer(doc1).renderImage(0);
        PDAcroForm acroForm1 = doc1.getDocumentCatalog().getAcroForm();
        PDRadioButton radioButton1 = (PDRadioButton) acroForm1.getField("MyRadioButton");
        Assert.assertEquals(3, radioButton1.getWidgets().size());
        Assert.assertEquals("c", radioButton1.getValue());
        Assert.assertEquals(1, radioButton1.getSelectedExportValues().size());
        Assert.assertEquals("c", radioButton1.getSelectedExportValues().get(0));
        Assert.assertEquals(3, radioButton1.getExportValues().size());
        Assert.assertEquals("a", radioButton1.getExportValues().get(0));
        Assert.assertEquals("b", radioButton1.getExportValues().get(1));
        Assert.assertEquals("c", radioButton1.getExportValues().get(2));
        radioButton1.setValue("b");
        doc1.save("target/RadioButtonsSample-modified.pdf");
        doc1.close();

        PDDocument doc2 = PDDocument.load(new File("target/RadioButtonsSample-modified.pdf"));
        new PDFRenderer(doc2).renderImage(0);
        PDAcroForm acroForm2 = doc2.getDocumentCatalog().getAcroForm();
        PDRadioButton radioButton2 = (PDRadioButton) acroForm2.getField("MyRadioButton");
        Assert.assertEquals("b", radioButton2.getValue());
        Assert.assertEquals(1, radioButton2.getSelectedExportValues().size());
        Assert.assertEquals("b", radioButton2.getSelectedExportValues().get(0));
        Assert.assertEquals(3, radioButton2.getExportValues().size());
        doc2.close();
    }

    private PDFont getFontFromWidgetResources(PDTextField textBox, String fontResourceName) throws IOException
    {
        PDAnnotationWidget widget = textBox.getWidgets().get(0);
        PDAppearanceDictionary appearance = widget.getAppearance();
        PDAppearanceEntry normalAppearance = appearance.getNormalAppearance();
        PDAppearanceStream appearanceStream = normalAppearance.getAppearanceStream();
        PDResources resources = appearanceStream.getResources();
        return resources.getFont(COSName.getPDFName(fontResourceName));
    }
}
