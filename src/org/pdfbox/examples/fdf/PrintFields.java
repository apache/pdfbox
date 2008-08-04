/**
 * Copyright (c) 2003-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.examples.fdf;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.pdfbox.pdmodel.interactive.form.PDField;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentCatalog;

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
        System.err.println( "usage: org.pdfbox.examples.fdf.PrintFields <pdf-file>" );
    }
}