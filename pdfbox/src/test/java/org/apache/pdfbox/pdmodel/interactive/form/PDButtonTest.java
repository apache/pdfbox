package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class PDButtonTest
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
    public void createCheckBox()
    {
        PDButton buttonField = new PDCheckbox(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertFalse(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    public void createPushButton()
    {
        PDButton buttonField = new PDPushButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertTrue(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    public void createRadioButton()
    {
        PDButton buttonField = new PDRadioButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertTrue(buttonField.isRadioButton());
        assertFalse(buttonField.isPushButton());
    }
}

