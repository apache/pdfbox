/**
 * Copyright (c) 2005-2007, www.pdfbox.org
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

import org.jempbox.xmp.XMPMetadata;
import org.jempbox.xmp.XMPSchemaBasic;
import org.jempbox.xmp.XMPSchemaDublinCore;
import org.jempbox.xmp.XMPSchemaPDF;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentCatalog;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.common.PDMetadata;

import java.util.GregorianCalendar;

/**
 * This is an example on how to add metadata to a document.
 *
 * Usage: java org.pdfbox.examples.pdmodel.AddMetadataToDocument &lt;input-pdf&gt; &lt;output-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class AddMetadataFromDocInfo
{    
    private AddMetadataFromDocInfo()
    {
        //utility class
    }
    
    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            
            try
            {
                document = PDDocument.load( args[0] );
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Cannot add metadata to encrypted document." );
                    System.exit( 1 );
                }
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDDocumentInformation info = document.getDocumentInformation();
                
                XMPMetadata metadata = new XMPMetadata();
                
                XMPSchemaPDF pdfSchema = metadata.addPDFSchema();
                pdfSchema.setKeywords( info.getKeywords() );
                pdfSchema.setProducer( info.getProducer() );
                
                XMPSchemaBasic basicSchema = metadata.addBasicSchema();
                basicSchema.setModifyDate( info.getModificationDate() );
                basicSchema.setCreateDate( info.getCreationDate() );
                basicSchema.setCreatorTool( info.getCreator() );
                basicSchema.setMetadataDate( new GregorianCalendar() );
                
                XMPSchemaDublinCore dcSchema = metadata.addDublinCoreSchema();
                dcSchema.setTitle( info.getTitle() );
                dcSchema.addCreator( "PDFBox" );
                dcSchema.setDescription( info.getSubject() );
                
                PDMetadata metadataStream = new PDMetadata(document);
                metadataStream.importXMPMetadata( metadata );
                catalog.setMetadata( metadataStream );
                
                document.save( args[1] );
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.pdfbox.examples.pdmodel.AddMetadataFromDocInfo <input-pdf> <output-pdf>" );
    }
}