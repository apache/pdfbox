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
package org.apache.pdfbox.examples.util;

import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.contentstream.operator.Operator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example on how to remove all text from PDF document.
 *
 * Usage: java org.apache.pdfbox.examples.util.RemoveAllText &lt;input-pdf&gt; &lt;output-pdf&gt;
 *
 * @author Ben Litchfield
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
                document = PDDocument.load( new File(args[0]) );
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Encrypted documents are not supported for this example." );
                    System.exit( 1 );
                }
                for( PDPage page : document.getPages() )
                {
                    PDFStreamParser parser = new PDFStreamParser(page.getStream());
                    parser.parse();
                    List<Object> tokens = parser.getTokens();
                    List<Object> newTokens = new ArrayList<Object>();
                    for( int j=0; j<tokens.size(); j++)
                    {
                        Object token = tokens.get( j );
                        if( token instanceof Operator)
                        {
                            Operator op = (Operator)token;
                            if( op.getName().equals( "TJ") || op.getName().equals( "Tj" ))
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
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.RemoveAllText <input-pdf> <output-pdf>" );
    }

}
