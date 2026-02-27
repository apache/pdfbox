package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.junit.Test;

public class PDVisibleSignDesignerTest {

    @Test
    public void testUseCustomCoordinates() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage(PDRectangle.A4));
        
        PDVisibleSignDesigner designer = new PDVisibleSignDesigner(doc, image, 1);
        assertFalse(designer.isUseCustomCoordinates());
        
        designer.useCustomCoordinates(true);
        assertTrue(designer.isUseCustomCoordinates());
        
        designer.xAxis(100).yAxis(200).width(50).height(30);
        
        PDVisibleSigBuilder builder = new PDVisibleSigBuilder();
        PDAcroForm acroForm = new PDAcroForm(doc);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        
        builder.createSignatureRectangle(signatureField, designer);
        PDRectangle rect = builder.getStructure().getSignatureRectangle();
        
        assertEquals(100f, rect.getLowerLeftX(), 0.01f);
        assertEquals(200f, rect.getLowerLeftY(), 0.01f);
        assertEquals(150f, rect.getUpperRightX(), 0.01f);
        assertEquals(230f, rect.getUpperRightY(), 0.01f);
        
        doc.close();
    }

    @Test
    public void testStandardCoordinates() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        float pageHeight = page.getMediaBox().getHeight();
        
        PDVisibleSignDesigner designer = new PDVisibleSignDesigner(doc, image, 1);
        assertFalse(designer.isUseCustomCoordinates());
        
        designer.xAxis(100).yAxis(200).width(50).height(30);
        
        PDVisibleSigBuilder builder = new PDVisibleSigBuilder();
        PDAcroForm acroForm = new PDAcroForm(doc);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        
        builder.createSignatureRectangle(signatureField, designer);
        PDRectangle rect = builder.getStructure().getSignatureRectangle();
        
        // Without custom coordinates, Y is transformed: pageHeight - yAxis
        assertEquals(100f, rect.getLowerLeftX(), 0.01f);
        assertEquals(pageHeight - 200f - 30f, rect.getLowerLeftY(), 0.01f);
        assertEquals(150f, rect.getUpperRightX(), 0.01f);
        assertEquals(pageHeight - 200f, rect.getUpperRightY(), 0.01f);
        
        doc.close();
    }
}
