package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDChoice class.
 *
 */
public class PDChoiceTest
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
    public void createListBox()
    {
        PDChoice choiceField = new PDListBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(choiceField.getFieldType(), "Ch");
        assertFalse(choiceField.isCombo());
    }

    @Test
    public void createComboBox()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getDictionary().getNameAsString(COSName.FT));
        assertEquals(choiceField.getFieldType(), "Ch");
        assertTrue(choiceField.isCombo());
    }

}

