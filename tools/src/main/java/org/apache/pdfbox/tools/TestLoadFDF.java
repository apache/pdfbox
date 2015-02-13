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
package org.apache.pdfbox.tools;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.fdf.FDFAnnotation;
import org.apache.pdfbox.pdmodel.fdf.FDFCatalog;
import org.apache.pdfbox.pdmodel.fdf.FDFDictionary;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

/**
 * This example will take a PDF document and fill the fields with data from the
 * FDF fields.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class TestLoadFDF
{
    /**
     * Creates a new instance of ImportFDF.
     */
    public TestLoadFDF()
    {
    }

    /**
     * This will takes the values from the fdf document and import them into the
     * PDF document.
     *
     * @param pdfDocument The document to put the fdf data into.
     * @param fdfDocument The FDF document to get the data from.
     *
     * @throws IOException If there is an error setting the data in the field.
     */
    public void importFDF( PDDocument pdfDocument, FDFDocument fdfDocument ) throws IOException
    {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        acroForm.setCacheFields( true );
        acroForm.importFDF( fdfDocument );
    }

    /**
     * This will import an fdf document and write out another pdf.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     *
     * @throws Exception If there is an error importing the FDF document.
     */
    public static void main(String[] args) throws Exception
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        TestLoadFDF importer = new TestLoadFDF();
        importer.importFDF( args );
    }

    private void importFDF( String[] args ) throws Exception
    {
        FDFDocument fdf = null;

        try
        {
            if( args.length != 1 )
            {
                usage();
            }
            else
            {
                fdf = FDFDocument.load( args[0] );
                FDFDictionary fdfDictionary = fdf.getCatalog().getFDF();
                List<?> fields = fdfDictionary.getFields();
                if( fields != null )
                {
                    for (Object field : fields)
                    {
                        FDFField fdfField = (FDFField) field;
                        if (fdfField.getValue() instanceof PDTextStream)
                        	System.out.println(((PDTextStream)fdfField.getValue()).getAsString());
                        else
                        	System.out.println(fdfField.getValue());
                    }
                }
                List<FDFAnnotation> annotations = fdfDictionary.getAnnotations();
                if( annotations != null )
                {
                    for (FDFAnnotation annotation : annotations)
                    {
                    	if (annotation != null)
                    		System.out.println(annotation.getName());
                    }
                }
            }
        }
        finally
        {
            close( fdf );
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: org.apache.pdfbox.tools.ImportFDF <pdf-file> <fdf-file> <output-file>" );
    }

    /**
     * Close the document.
     *
     * @param doc The doc to close.
     *
     * @throws IOException If there is an error closing the document.
     */
    public void close( FDFDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }

}
