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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

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
                .load(new File(formTemplate),null);

        // get the document catalog
        PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
        
        // as there might not be an AcroForm entry a null check is necessary
        if (acroForm != null)
        {
            // Retrieve an individual field and set it's value.
            PDTextField field = (PDTextField) acroForm.getField( "sampleField" );
            field.setValue("Text Entry");
            
            // If a field is nested within the form tree a fully qualified name 
            // might be provided to access the field.
            field = (PDTextField) acroForm.getField( "fieldsContainer.nestedSampleField" );
            field.setValue("Text Entry");
        }

        // Save and close the filled out form.
        pdfDocument.save(filledForm);
        pdfDocument.close();
    }

}
