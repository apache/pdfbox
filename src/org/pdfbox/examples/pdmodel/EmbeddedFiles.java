/**
 * Copyright (c) 2004, www.pdfbox.org
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
package org.pdfbox.examples.pdmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.pdfbox.exceptions.COSVisitorException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.pdfbox.pdmodel.edit.PDPageContentStream;

import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.pdmodel.font.PDType1Font;


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
            byte[] data = "This is the contents of the embedded file".getBytes();
            ByteArrayInputStream fakeFile = 
                new ByteArrayInputStream( data );
            PDEmbeddedFile ef = new PDEmbeddedFile(doc, fakeFile );
            //now lets some of the optional parameters
            ef.setSubtype( "test/plain" );
            ef.setSize( data.length );
            ef.setCreationDate( new GregorianCalendar() );
            fs.setEmbeddedFile( ef );
            
            //now add the entry to the embedded file tree and set in the document.
            Map efMap = new HashMap();
            efMap.put( "My first attachment", fs );
            efTree.setNames( efMap );
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