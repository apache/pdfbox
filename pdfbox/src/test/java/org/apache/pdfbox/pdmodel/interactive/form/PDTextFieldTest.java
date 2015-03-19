package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDSignatureField class.
 *
 */
public class PDTextFieldTest
{
    private PDDocument document;
    private PDAcroForm acroForm;

    @Before
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
    }

    @Test
    public void createDefaultTextField()
    {
        PDFieldTreeNode textField = new PDTextField(acroForm);
        
        assertEquals(textField.getFieldType(), textField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(textField.getFieldType(), "Tx");
    }

    @Test
    public void createWidgetForGet()
    {
        PDTextField textField = new PDTextField(acroForm);

        assertNull(textField.getDictionary().getItem(COSName.TYPE));
        assertNull(textField.getDictionary().getNameAsString(COSName.SUBTYPE));
        
        PDAnnotationWidget widget = textField.getWidget();
        
        assertEquals(COSName.ANNOT, textField.getDictionary().getItem(COSName.TYPE));
        assertEquals(PDAnnotationWidget.SUB_TYPE, textField.getDictionary().getNameAsString(COSName.SUBTYPE));
        
        assertEquals(widget.getDictionary(), textField.getDictionary());
    }

}
