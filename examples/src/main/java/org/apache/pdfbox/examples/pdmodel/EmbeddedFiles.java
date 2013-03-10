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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


/**
 * This is an example that creates a simple document and embeds a file into it..
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class EmbeddedFiles
{
    /**
     * Constructor.
     */
    public EmbeddedFiles()
    {
        super();
    }

    /**
     * create the second sample document from the PDF file format specification.
     *
     * @param file The file to write the PDF to.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public void doIt( String file) throws IOException, COSVisitorException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            PDPage page = new PDPage();
            doc.addPage( page );
            PDFont font = PDType1Font.HELVETICA_BOLD;

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.setFont( font, 12 );
            contentStream.moveTextPositionByAmount( 100, 700 );
            contentStream.drawString( "Go to Document->File Attachments to View Embedded Files" );
            contentStream.endText();
            contentStream.close();

            //embedded files are stored in a named tree
            PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

            //first create the file specification, which holds the embedded file
            PDComplexFileSpecification fs = new PDComplexFileSpecification();
            fs.setFile( "Test.txt" );
            //create a dummy file stream, this would probably normally be a FileInputStream
            byte[] data = "This is the contents of the embedded file".getBytes("ISO-8859-1");
            ByteArrayInputStream fakeFile =
                new ByteArrayInputStream( data );
            PDEmbeddedFile ef = new PDEmbeddedFile(doc, fakeFile );
            //now lets some of the optional parameters
            ef.setSubtype( "test/plain" );
            ef.setSize( data.length );
            ef.setCreationDate( new GregorianCalendar() );
            fs.setEmbeddedFile( ef );

            // create a new tree node and add the embedded file 
            PDEmbeddedFilesNameTreeNode treeNode = new PDEmbeddedFilesNameTreeNode();
            treeNode.setNames( Collections.singletonMap( "My first attachment",  fs ) );
            // add the new node as kid to the root node
            List<PDEmbeddedFilesNameTreeNode> kids = new ArrayList<PDEmbeddedFilesNameTreeNode>();
            kids.add(treeNode);
            efTree.setKids(kids);
            // add the tree to the document catalog
            PDDocumentNameDictionary names = new PDDocumentNameDictionary( doc.getDocumentCatalog() );
            names.setEmbeddedFiles( efTree );
            doc.getDocumentCatalog().setNames( names );


            doc.save( file );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    /**
     * This will create a hello world PDF document with an embedded file.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        EmbeddedFiles app = new EmbeddedFiles();
        try
        {
            if( args.length != 1 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0] );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <output-file>" );
    }
}
