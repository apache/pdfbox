/**
 * Copyright (c) 2006, www.pdfbox.org
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
package org.pdfbox.examples.util;

import org.pdfbox.pdfparser.PDFStreamParser;
import org.pdfbox.pdfwriter.ContentStreamWriter;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.common.PDStream;
import org.pdfbox.util.PDFOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an example on how to remove all text from PDF document.
 *
 * Usage: java org.pdfbox.examples.util.RemoveAllText &lt;input-pdf&gt; &lt;output-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class RemoveAllText
{
    /**
     * Default constructor.
     */
    private RemoveAllText()
    {
        //example class should not be instantiated
    }
    
    /**
     * This will remove all text from a PDF document.
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
                    System.err.println( "Error: Encrypted documents are not supported for this example." );
                    System.exit( 1 );
                }
                List allPages = document.getDocumentCatalog().getAllPages();
                for( int i=0; i<allPages.size(); i++ )
                {
                    PDPage page = (PDPage)allPages.get( i );
                    PDFStreamParser parser = new PDFStreamParser(page.getContents());
                    parser.parse();
                    List tokens = parser.getTokens();
                    List newTokens = new ArrayList();
                    for( int j=0; j<tokens.size(); j++)
                    {
                        Object token = tokens.get( j );
                        if( token instanceof PDFOperator )
                        {
                            PDFOperator op = (PDFOperator)token;
                            if( op.getOperation().equals( "TJ") || op.getOperation().equals( "Tj" ))
                            {
                                //remove the one argument to this operator
                                newTokens.remove( newTokens.size() -1 );
                                continue;
                            }
                        }
                        newTokens.add( token );
                        
                    }
                    PDStream newContents = new PDStream( document );
                    ContentStreamWriter writer = new ContentStreamWriter( newContents.createOutputStream() );
                    writer.writeTokens( newTokens );
                    newContents.addCompression();
                    page.setContents( newContents );
                }
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
        System.err.println( "Usage: java org.pdfbox.examples.pdmodel.RemoveAllText <input-pdf> <output-pdf>" );
    }

}