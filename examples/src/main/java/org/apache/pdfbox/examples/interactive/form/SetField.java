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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDListBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * This example will take a PDF document and set a form field in it.
 *
 * @author Ben Litchfield
 *
 */
public class SetField
{
    /**
     * This will set a single field in the document.
     *
     * @param pdfDocument The PDF to set the field in.
     * @param name The name of the field to set.
     * @param value The new value of the field.
     *
     * @throws IOException If there is an error setting the field.
     */
    public void setField(final PDDocument pdfDocument, final String name, final String value) throws IOException
    {
        final PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        final PDAcroForm acroForm = docCatalog.getAcroForm();
        final PDField field = acroForm.getField(name);
        if (field != null)
        {
            if (field instanceof PDCheckBox)
            {
                final PDCheckBox checkbox = (PDCheckBox) field;
                if (value.isEmpty())
                {
                    checkbox.unCheck();
                }
                else
                {
                    checkbox.check();
                }
            }
            else if (field instanceof PDComboBox)
            {
                field.setValue(value);
            }
            else if (field instanceof PDListBox)
            {
                field.setValue(value);
            }
            else if (field instanceof PDRadioButton)
            {
                field.setValue(value);
            }
            else if (field instanceof PDTextField)
            {
                field.setValue(value);
            } 
        }
        else
        {
            System.err.println("No field found with name:" + name);
        }
    }

    /**
     * This will read a PDF file and set a field and then write it the pdf out
     * again. <br>
     * see usage() for commandline
     *
     * @param args command line arguments
     *
     * @throws IOException If there is an error importing the FDF document.
     */
    public static void main(final String[] args) throws IOException
    {
        final SetField setter = new SetField();
        setter.setField(args);
    }

    private void setField(final String[] args) throws IOException
    {
        PDDocument pdf = null;
        try
        {
            if (args.length != 3)
            {
                usage();
            }
            else
            {
                final SetField example = new SetField();
                pdf = Loader.loadPDF(new File(args[0]));
                example.setField(pdf, args[1], args[2]);
                pdf.save(args[0]);
            }
        }
        finally
        {
            if (pdf != null)
            {
                pdf.close();
            }
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println("usage: org.apache.pdfbox.examples.interactive.form.SetField <pdf-file> <field-name> <field-value>");
    }
}
