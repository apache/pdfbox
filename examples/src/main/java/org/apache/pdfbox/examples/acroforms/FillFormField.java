package org.apache.pdfbox.examples.acroforms;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

/**
 * Example to show filling form fields.
 * 
 */
public class FillFormField
{

    public static void main(String[] args) throws IOException
    {
        String formTemplate = "src/main/resources/org/apache/pdfbox/examples/acroforms/FillFormField.pdf";
        String filledForm = "target/examples-output/FillFormField.pdf";
        
        // load the document
        PDDocument pdfDocument = PDDocument
                .loadNonSeq(new File(formTemplate),null);

        // get the document catalog
        PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
        
        // as there might not be an AcroForm entry a null check is necessary
        if (acroForm != null)
        {
            // Retrieve an individual field and set it's value.
            PDFieldTreeNode field = acroForm.getField( "sampleField" );
            field.setValue("Text Entry");

            // If a field is nested within the form tree a fully qualified name 
            // might be provided to access the field.
            field = acroForm.getField( "fieldsContainer.nestedSampleField" );
            field.setValue("Text Entry");
        }

        // Save and close the filled out form.
        pdfDocument.save(filledForm);
        pdfDocument.close();
    }

}
