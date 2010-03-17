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
package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * This will create a blank PDF and write the contents to a file.
 *
 * usage: java org.apache.pdfbox.examples.pdmodel.CreateBlankPDF &lt;outputfile.pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class CreateBlankPDF
{

    /**
     * This will create a blank PDF and write the contents to a file.
     *
     * @param file The name of the file to write to.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error while generating the document.
     */
    public void create( String file ) throws IOException, COSVisitorException
    {
        PDDocument document = null;
        try
        {
            document = new PDDocument();
            //Every document requires at least one page, so we will add one
            //blank page.
            PDPage blankPage = new PDPage();
            document.addPage( blankPage );
            document.save( file );
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
    }

    /**
     * This will create a blank document.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error writing the document data.
     * @throws COSVisitorException If there is an error generating the data.
     */
    public static void main( String[] args ) throws IOException, COSVisitorException
    {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            CreateBlankPDF creator = new CreateBlankPDF();
            creator.create( args[0] );
        }
    }

    /**
     * This will print the usage of this class.
     */
    private static void usage()
    {
        System.err.println( "usage: java org.apache.pdfbox.examples.pdmodel.CreateBlankPDF <outputfile.pdf>" );
    }
}
