/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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

import org.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.pdfbox.pdmodel.interactive.form.PDField;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentCatalog;

import org.pdfbox.exceptions.COSVisitorException;

import org.pdfbox.examples.AbstractExample;

/**
 * This example will take a PDF document and set a FDF field in it.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class SetField extends AbstractExample
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
    public void setField( PDDocument pdfDocument, String name, String value ) throws IOException
    {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        PDField field = acroForm.getField( name );
        if( field != null )
        {
            field.setValue( value );
        }
        else
        {
            System.err.println( "No field found with name:" + name );
        }

    }

    /**
     * This will read a PDF file and set a field and then write it the pdf out again.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     *
     * @throws IOException If there is an error importing the FDF document.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public static void main(String[] args) throws IOException, COSVisitorException
    {
        SetField setter = new SetField();
        setter.setField( args );
    }

    private void setField( String[] args ) throws IOException, COSVisitorException
    {
        PDDocument pdf = null;
        try
        {
            if( args.length != 3 )
            {
                usage();
            }
            else
            {
                SetField example = new SetField();

                pdf = PDDocument.load( args[0] );
                example.setField( pdf, args[1], args[2] );
                pdf.save( args[0] );
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
        System.err.println( "usage: org.pdfbox.examples.fdf.SetField <pdf-file> <field-name> <field-value>" );
    }
}