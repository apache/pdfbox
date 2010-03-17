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
package org.apache.pdfbox.examples.fdf;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

/**
 * This example will take a PDF document and print all the fields from the file.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.16 $
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
    public void printFields( PDDocument pdfDocument ) throws IOException
    {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        List fields = acroForm.getFields();
        Iterator fieldsIter = fields.iterator();

        System.out.println(new Integer(fields.size()).toString() + " top-level fields were found on the form");

        while( fieldsIter.hasNext())
        {
            PDField field = (PDField)fieldsIter.next();
               processField(field, "|--", field.getPartialName());
        }
    }

    private void processField(PDField field, String sLevel, String sParent) throws IOException
    {
        List kids = field.getKids();
        if(kids != null)
        {
            Iterator kidsIter = kids.iterator();
            if(!sParent.equals(field.getPartialName()))
            {
               sParent = sParent + "." + field.getPartialName();
            }
            System.out.println(sLevel + sParent);
            //System.out.println(sParent + " is of type " + field.getClass().getName());
            while(kidsIter.hasNext())
            {
               Object pdfObj = kidsIter.next();
               if(pdfObj instanceof PDField)
               {
                   PDField kid = (PDField)pdfObj;
                   processField(kid, "|  " + sLevel, sParent);
               }
            }
         }
         else
         {
             String outputString = sLevel + sParent + "." + field.getPartialName() + " = " + field.getValue() +
                 ",  type=" + field.getClass().getName();

             System.out.println(outputString);
         }
    }

    /**
     * This will read a PDF file and print out the form elements.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     *
     * @throws IOException If there is an error importing the FDF document.
     * @throws CryptographyException If there is an error decrypting the document.
     */
    public static void main(String[] args) throws IOException, CryptographyException
    {
        PDDocument pdf = null;
        try
        {
            if( args.length != 1 )
            {
                usage();
            }
            else
            {
                pdf = PDDocument.load( args[0] );
                PrintFields exporter = new PrintFields();
                if( pdf.isEncrypted() )
                {
                    try
                    {
                        pdf.decrypt( "" );
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: The document is encrypted." );
                        usage();
                    }
                }
                exporter.printFields( pdf );
            }
        }
        finally
        {
            if( pdf != null )
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
        System.err.println( "usage: org.apache.pdfbox.examples.fdf.PrintFields <pdf-file>" );
    }
}
