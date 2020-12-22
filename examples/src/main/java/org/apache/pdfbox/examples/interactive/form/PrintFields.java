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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;

/**
 * This example will take a PDF document and print all the fields from the file.
 * 
 * @author Ben Litchfield
 * 
 */
public class PrintFields
{

    /**
     * This will print all the fields from the document.
     * 
     * @param pdfDocument The PDF to get the fields from.
     * 
     * @throws IOException If there is an error getting the fields.
     */
    public void printFields(final PDDocument pdfDocument) throws IOException
    {
        final PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        final PDAcroForm acroForm = docCatalog.getAcroForm();
        final List<PDField> fields = acroForm.getFields();

        System.out.println(fields.size() + " top-level fields were found on the form");

        for (final PDField field : fields)
        {
            processField(field, "|--", field.getPartialName());
        }
    }

    private void processField(final PDField field, final String sLevel, String sParent) throws IOException
    {
        final String partialName = field.getPartialName();
        
        if (field instanceof PDNonTerminalField)
        {
            if (!sParent.equals(field.getPartialName()) && partialName != null)
            {
                sParent = sParent + "." + partialName;
            }
            System.out.println(sLevel + sParent);

            for (final PDField child : ((PDNonTerminalField)field).getChildren())
            {
                processField(child, "|  " + sLevel, sParent);
            }
        }
        else
        {
            final String fieldValue = field.getValueAsString();
            final StringBuilder outputString = new StringBuilder(sLevel);
            outputString.append(sParent);
            if (partialName != null)
            {
                outputString.append(".").append(partialName);
            }
            outputString.append(" = ").append(fieldValue);
            outputString.append(",  type=").append(field.getClass().getName());
            System.out.println(outputString);
        }
    }

    /**
     * This will read a PDF file and print out the form elements. <br>
     * see usage() for commandline
     * 
     * @param args command line arguments
     * 
     * @throws IOException If there is an error importing the FDF document.
     */
    public static void main(final String[] args) throws IOException
    {
        PDDocument pdf = null;
        try
        {
            if (args.length != 1)
            {
                usage();
            }
            else
            {
                pdf = Loader.loadPDF(new File(args[0]));
                final PrintFields exporter = new PrintFields();
                exporter.printFields(pdf);
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
        System.err.println("usage: org.apache.pdfbox.examples.interactive.form.PrintFields <pdf-file>");
    }
}
